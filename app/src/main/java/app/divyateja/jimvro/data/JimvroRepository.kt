package app.divyateja.jimvro.data

import android.database.sqlite.SQLiteDatabase
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.io.File

class JimvroRepository(private val database: JimvroDatabase) {
    val measurements = database.measurementDao().observeAll()
    val latestMeasurement = database.measurementDao().observeLatest()
    val workouts = database.workoutDao().observeSummaries()
    val exercises = database.exerciseDao().observeAll()
    val foodEntries = database.foodDao().observeAll()
    val savedFoods = database.foodDao().observeSaved()
    val templates = database.templateDao().observeTemplates()
    val personalRecords = database.workoutDao().observePersonalRecords()
    val recentExercises = database.exerciseDao().observeRecent()
    val progressPhotos = database.progressPhotoDao().observeAll()

    fun foodOn(date: String): Flow<List<FoodEntryEntity>> = database.foodDao().observeOn(date)
    fun nutritionOn(date: String): Flow<DailyNutrition> = database.foodDao().observeNutritionOn(date)
    fun workoutsOn(date: String): Flow<Int> = database.workoutDao().observeCountOn(date)
    fun workout(id: Long): Flow<WorkoutEntity?> = database.workoutDao().observeWorkout(id)
    fun workoutSets(id: Long): Flow<List<WorkoutSetDetail>> = database.workoutDao().observeSetDetails(id)
    fun previousSets(workoutId: Long, exerciseId: Long): Flow<List<PreviousSet>> =
        database.workoutDao().observePreviousSets(workoutId, exerciseId)
    fun exerciseProgress(exerciseId: Long): Flow<List<ExerciseProgressPoint>> =
        database.workoutDao().observeExerciseProgress(exerciseId)
    fun templateLines(id: Long): Flow<List<TemplateLine>> = database.templateDao().observeLines(id)

    suspend fun addMeasurement(value: MeasurementEntity) = database.measurementDao().insert(value)
    suspend fun deleteMeasurement(value: MeasurementEntity) = database.measurementDao().delete(value)
    suspend fun addProgressPhoto(value: ProgressPhotoEntity) = database.progressPhotoDao().insert(value)
    suspend fun deleteProgressPhoto(value: ProgressPhotoEntity) = database.progressPhotoDao().delete(value)
    suspend fun addWorkout(value: WorkoutEntity) = database.workoutDao().insertWorkout(value)
    suspend fun createWorkout(value: WorkoutEntity, sets: List<WorkoutSetEntity>) =
        database.workoutDao().createWorkout(value, sets)
    suspend fun deleteWorkout(id: Long) = database.workoutDao().deleteWorkout(id)
    suspend fun appendSet(workoutId: Long, exerciseId: Long, reps: Int? = null, weightKg: Double? = null) =
        database.workoutDao().appendSet(workoutId, exerciseId, reps, weightKg)
    suspend fun updateSet(setId: Long, reps: Int?, weightKg: Double?, rpe: Double? = null) =
        database.workoutDao().updateSet(setId, reps, weightKg, rpe)
    suspend fun updateSetType(setId: Long, setType: String) = database.workoutDao().updateSetType(setId, setType)
    suspend fun finishWorkout(workoutId: Long) = database.workoutDao().finishWorkout(workoutId, System.currentTimeMillis())
    suspend fun setSuperset(workoutId: Long, exerciseIds: List<Long>, groupId: Int?) = database.workoutDao().setSuperset(workoutId, exerciseIds, groupId)
    suspend fun toggleFavorite(exerciseId: Long) = database.exerciseDao().toggleFavorite(exerciseId)
    suspend fun deleteSet(setId: Long) = database.workoutDao().deleteSet(setId)
    suspend fun createTemplate(name: String, notes: String? = null) =
        database.templateDao().insertTemplate(WorkoutTemplateEntity(name = name, notes = notes))
    suspend fun createTemplate(name: String, targets: List<TemplateTarget>) =
        database.templateDao().createTemplate(name, targets)
    suspend fun deleteTemplate(id: Long) = database.templateDao().deleteTemplate(id)
    suspend fun addTemplateLine(templateId: Long, exerciseId: Long, targetSets: Int, repLow: Int?, repHigh: Int?) =
        database.templateDao().insertLine(TemplateExerciseEntity(templateId = templateId, exerciseId = exerciseId, targetSets = targetSets, repLow = repLow, repHigh = repHigh))
    suspend fun deleteTemplateLine(id: Long) = database.templateDao().deleteLine(id)
    suspend fun updateTemplateLine(id: Long, sets: Int, low: Int?, high: Int?) = database.templateDao().updateLine(id, sets, low, high)
    suspend fun reorderTemplateLines(lines: List<TemplateLine>) = lines.forEachIndexed { index, line -> database.templateDao().updatePosition(line.id, index) }
    suspend fun startFromTemplate(templateId: Long, date: String): Long {
        val template = database.templateDao().template(templateId) ?: error("Template not found")
        val stubs = database.templateDao().lines(templateId).flatMap { line ->
            (1..line.targetSets).map { number -> WorkoutSetEntity(workoutId = 0, exerciseId = line.exerciseId, setNumber = number, setType = line.setType, supersetGroup = line.supersetGroup) }
        }
        return database.workoutDao().createWorkout(WorkoutEntity(performedOn = date, name = template.name), stubs)
    }
    suspend fun addFood(value: FoodEntryEntity, saveForReuse: Boolean = false) {
        database.foodDao().insert(value)
        if (saveForReuse) database.foodDao().save(SavedFoodEntity(name = value.name, calories = value.calories, proteinG = value.proteinG, carbsG = value.carbsG, fatG = value.fatG))
    }
    suspend fun deleteFood(value: FoodEntryEntity) = database.foodDao().delete(value)
    suspend fun deleteSavedFood(value: SavedFoodEntity) = database.foodDao().deleteSaved(value.name)

    suspend fun removeBundledExercises() = database.exerciseDao().removeBundledCatalog()

    suspend fun seedStockTemplates() {
        data class Stock(val name: String, val day: String, val lines: List<Triple<String, Int, IntRange>>)
        val stocks = listOf(
            Stock("Upper A", "Monday · Strength", listOf(
                Triple("Bench Press (Smith/Barbell)", 4, 5..8), Triple("Iso-Lateral Row", 3, 6..8),
                Triple("Overhead Press", 3, 8..10), Triple("Lat Pulldown", 3, 8..10),
                Triple("Preacher Curl", 3, 8..10), Triple("Rope Pushdown", 3, 10..12),
            )),
            Stock("Lower A", "Tuesday · Strength", listOf(
                Triple("Squat Machine", 4, 8..10), Triple("Romanian Deadlift", 3, 8..8),
                Triple("Assisted Pull-ups", 3, 8..10), Triple("Seated Leg Curl", 3, 10..12),
                Triple("Seated Calf Raise", 3, 12..15), Triple("Hanging Leg Raise", 3, 12..15),
                Triple("Cable Crunch", 3, 12..15),
            )),
            Stock("Upper B", "Thursday · Hypertrophy", listOf(
                Triple("Incline Smith Press", 3, 8..10), Triple("Seated Cable Row (V-Grip)", 3, 10..12),
                Triple("Lat Pulldown", 3, 10..12), Triple("Chest Fly (Machine/Cable)", 3, 12..15),
                Triple("Machine/Cable Lateral Raise", 3, 12..15), Triple("Reverse Pec Deck / Rear Delt Fly", 3, 15..20),
                Triple("Cross-Body Hammer Curl", 3, 10..12), Triple("Overhead Rope Triceps Extension", 3, 10..12),
            )),
            Stock("Lower B", "Friday · Hypertrophy", listOf(
                Triple("Front Squat", 3, 8..10), Triple("Bulgarian Split Squat", 3, 10..10),
                Triple("Leg Curl", 3, 12..15), Triple("Leg Extension", 3, 12..15),
                Triple("Seated Calf Raise", 3, 15..20), Triple("Decline Sit-ups", 3, 10..15),
                Triple("Ab Wheel Rollouts", 3, 8..12),
            )),
            Stock("Upper C", "Saturday · Aesthetics", listOf(
                Triple("Incline Dumbbell Press", 3, 8..10), Triple("Pull-ups / Lat Pulldown", 3, 8..10),
                Triple("Cable/Machine Lateral Raise", 4, 12..15), Triple("Reverse Pec Deck", 3, 12..15),
                Triple("Preacher Curl", 3, 10..12), Triple("Rope Pushdown (or Overhead Extension)", 3, 10..12),
            )),
        )
        stocks.forEachIndexed { index, stock ->
            val notes = "${stock.day} · Run 8–12 weeks · Keep 1–2 RIR; add weight after reaching top reps."
            val existing = database.templateDao().templateByName(stock.name)
            if (existing != null && existing.notes?.contains("Run 8–12 weeks") != true) return@forEachIndexed
            val templateId = existing?.id ?: database.templateDao().insertTemplate(WorkoutTemplateEntity(name = stock.name, notes = notes, position = -100 + index))
            if (existing != null) {
                database.templateDao().deleteLines(templateId)
                database.templateDao().updateStockTemplate(templateId, notes, -100 + index)
            }
            stock.lines.forEachIndexed { position, (name, sets, reps) ->
                val exercise = findOrCreateExercise(name)
                database.templateDao().insertLine(
                    TemplateExerciseEntity(templateId = templateId, exerciseId = exercise.id, position = position, targetSets = sets, repLow = reps.first, repHigh = reps.last),
                )
            }
        }
    }

    suspend fun backupDatabase(output: OutputStream) = withContext(Dispatchers.IO) {
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        File(requireNotNull(database.openHelper.writableDatabase.path)).inputStream().use { it.copyTo(output) }
    }

    suspend fun restoreDatabase(input: InputStream) = withContext(Dispatchers.IO) {
        val path = requireNotNull(database.openHelper.writableDatabase.path)
        val target = File(path)
        val staged = File(target.parentFile, "${target.name}.restore")
        val previous = File(target.parentFile, "${target.name}.before-restore")
        staged.outputStream().use { input.copyTo(it) }
        validateBackup(staged)
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }
        database.close()
        previous.delete()
        check(target.renameTo(previous)) { "Could not prepare current database for restore" }
        if (!staged.renameTo(target)) {
            previous.renameTo(target)
            error("Could not install restored database")
        }
        File("$path-wal").delete()
        File("$path-shm").delete()
        previous.delete()
    }

    private fun validateBackup(file: File) {
        val header = ByteArray(16)
        file.inputStream().use { stream ->
            require(stream.read(header) == header.size && header.decodeToString() == "SQLite format 3\u0000") {
                "Selected file is not a Jimvro database backup"
            }
        }
        val restored = SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY)
        restored.use { db ->
            val version = db.rawQuery("PRAGMA user_version", null).use { cursor ->
                check(cursor.moveToFirst())
                cursor.getInt(0)
            }
            require(version in 1..4) { "Unsupported backup version: $version" }
            val required = setOf("measurements", "workouts", "workout_sets", "food_entries")
            val present = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null).use { cursor ->
                buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) }
            }
            require(present.containsAll(required)) { "Selected database is missing Jimvro data tables" }
        }
    }

    suspend fun findOrCreateExercise(rawName: String): ExerciseEntity {
        val name = rawName.trim().replaceFirstChar(Char::uppercase)
        require(name.isNotBlank()) { "Exercise name is required" }
        database.exerciseDao().findByName(name)?.let { return it }
        val id = database.exerciseDao().insert(ExerciseEntity(name = name))
        return database.exerciseDao().findByName(name) ?: ExerciseEntity(id = id, name = name)
    }

    suspend fun lookupBarcode(rawCode: String): Result<BarcodeProductEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val barcode = rawCode.filter(Char::isDigit)
            require(barcode.length >= 6) { "Invalid barcode" }
            database.foodDao().getBarcodeProduct(barcode)?.takeIf {
                it.caloriesPer100g != null || it.proteinPer100g != null
            }?.let { return@runCatching it }

            val url = URL(
                "https://world.openfoodfacts.org/api/v2/product/$barcode.json" +
                    "?fields=code,product_name,brands,nutriments,serving_quantity",
            )
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", "jimvro-android/0.1 (local-first food diary)")
            try {
                require(connection.responseCode in 200..299) { "Product lookup failed" }
                val root = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
                require(root.optInt("status") == 1) { "Product not found" }
                val product = root.getJSONObject("product")
                val nutrients = product.optJSONObject("nutriments") ?: JSONObject()
                val name = product.optString("product_name").ifBlank {
                    product.optString("brands").ifBlank { "Scanned food" }
                }
                fun number(vararg keys: String): Double? = keys.firstNotNullOfOrNull { key ->
                    nutrients.optDouble(key, Double.NaN).takeUnless(Double::isNaN)
                }
                val servingG = product.optDouble("serving_quantity", Double.NaN).takeUnless(Double::isNaN)
                fun per100(per100Keys: Array<String>, servingKeys: Array<String>): Double? =
                    number(*per100Keys) ?: number(*servingKeys)?.let { servingValue ->
                        servingG?.takeIf { it > 0 }?.let { servingValue * 100.0 / it }
                    }
                val calories = per100(arrayOf("energy-kcal_100g", "energy_kcal_100g"), arrayOf("energy-kcal_serving", "energy_kcal_serving"))
                    ?: number("energy_100g")?.div(4.184)

                BarcodeProductEntity(
                    barcode = barcode,
                    name = name,
                    caloriesPer100g = calories,
                    proteinPer100g = per100(arrayOf("proteins_100g", "protein_100g"), arrayOf("proteins_serving", "protein_serving")),
                    carbsPer100g = per100(arrayOf("carbohydrates_100g", "carbs_100g"), arrayOf("carbohydrates_serving", "carbs_serving")),
                    fatPer100g = per100(arrayOf("fat_100g"), arrayOf("fat_serving")),
                    servingG = servingG,
                ).also { database.foodDao().cacheBarcodeProduct(it) }
            } finally {
                connection.disconnect()
            }
        }
    }
}
