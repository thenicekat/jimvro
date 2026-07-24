package app.jimvro.data

import android.content.Context
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray

class JimvroRepository(private val database: JimvroDatabase) {
    val measurements = database.measurementDao().observeAll()
    val latestMeasurement = database.measurementDao().observeLatest()
    val workouts = database.workoutDao().observeSummaries()
    val exercises = database.exerciseDao().observeAll()
    val foodEntries = database.foodDao().observeAll()
    val templates = database.templateDao().observeTemplates()
    val personalRecords = database.workoutDao().observePersonalRecords()

    fun foodOn(date: String): Flow<List<FoodEntryEntity>> = database.foodDao().observeOn(date)
    fun nutritionOn(date: String): Flow<DailyNutrition> = database.foodDao().observeNutritionOn(date)
    fun workoutsOn(date: String): Flow<Int> = database.workoutDao().observeCountOn(date)
    fun workout(id: Long): Flow<WorkoutEntity?> = database.workoutDao().observeWorkout(id)
    fun workoutSets(id: Long): Flow<List<WorkoutSetDetail>> = database.workoutDao().observeSetDetails(id)
    fun previousSets(workoutId: Long, exerciseId: Long): Flow<List<PreviousSet>> =
        database.workoutDao().observePreviousSets(workoutId, exerciseId)
    fun templateLines(id: Long): Flow<List<TemplateLine>> = database.templateDao().observeLines(id)

    suspend fun addMeasurement(value: MeasurementEntity) = database.measurementDao().insert(value)
    suspend fun deleteMeasurement(value: MeasurementEntity) = database.measurementDao().delete(value)
    suspend fun addWorkout(value: WorkoutEntity) = database.workoutDao().insertWorkout(value)
    suspend fun createWorkout(value: WorkoutEntity, sets: List<WorkoutSetEntity>) =
        database.workoutDao().createWorkout(value, sets)
    suspend fun deleteWorkout(id: Long) = database.workoutDao().deleteWorkout(id)
    suspend fun appendSet(workoutId: Long, exerciseId: Long, reps: Int? = null, weightKg: Double? = null) =
        database.workoutDao().appendSet(workoutId, exerciseId, reps, weightKg)
    suspend fun updateSet(setId: Long, reps: Int?, weightKg: Double?, rpe: Double? = null) =
        database.workoutDao().updateSet(setId, reps, weightKg, rpe)
    suspend fun deleteSet(setId: Long) = database.workoutDao().deleteSet(setId)
    suspend fun createTemplate(name: String, notes: String? = null) =
        database.templateDao().insertTemplate(WorkoutTemplateEntity(name = name, notes = notes))
    suspend fun createTemplate(name: String, targets: List<TemplateTarget>) =
        database.templateDao().createTemplate(name, targets)
    suspend fun deleteTemplate(id: Long) = database.templateDao().deleteTemplate(id)
    suspend fun addTemplateLine(templateId: Long, exerciseId: Long, targetSets: Int, repLow: Int?, repHigh: Int?) =
        database.templateDao().insertLine(TemplateExerciseEntity(templateId = templateId, exerciseId = exerciseId, targetSets = targetSets, repLow = repLow, repHigh = repHigh))
    suspend fun deleteTemplateLine(id: Long) = database.templateDao().deleteLine(id)
    suspend fun startFromTemplate(templateId: Long, date: String): Long {
        val template = database.templateDao().template(templateId) ?: error("Template not found")
        val stubs = database.templateDao().lines(templateId).flatMap { line ->
            (1..line.targetSets).map { number -> WorkoutSetEntity(workoutId = 0, exerciseId = line.exerciseId, setNumber = number) }
        }
        return database.workoutDao().createWorkout(WorkoutEntity(performedOn = date, name = template.name), stubs)
    }
    suspend fun addFood(value: FoodEntryEntity) = database.foodDao().insert(value)
    suspend fun deleteFood(value: FoodEntryEntity) = database.foodDao().delete(value)

    suspend fun seedExercises(context: Context) {
        database.exerciseDao().insertAll(
            listOf(
                ExerciseEntity(name = "Back Squat", muscleGroup = "legs"),
                ExerciseEntity(name = "Bench Press", muscleGroup = "chest"),
                ExerciseEntity(name = "Deadlift", muscleGroup = "back"),
                ExerciseEntity(name = "Lat Pulldown", muscleGroup = "back"),
                ExerciseEntity(name = "Overhead Press", muscleGroup = "shoulders"),
                ExerciseEntity(name = "Pull-up", muscleGroup = "back"),
                ExerciseEntity(name = "Romanian Deadlift", muscleGroup = "legs"),
                ExerciseEntity(name = "Seated Cable Row", muscleGroup = "back"),
            ),
        )
        if (database.exerciseDao().importedCount() > 1_200) return
        val raw = context.assets.open("exercises.json").bufferedReader().use { it.readText() }
        val source = JSONArray(raw)
        val imported = ArrayList<ExerciseEntity>(source.length())
        for (index in 0 until source.length()) {
            val item = source.getJSONObject(index)
            imported += ExerciseEntity(
                name = item.getString("name").replaceFirstChar(Char::uppercase),
                muscleGroup = item.optString("muscleGroup", "other"),
                sourceId = item.getString("sourceId"),
                bodyPart = item.optString("bodyPart").ifBlank { null },
                equipment = item.optString("equipment").ifBlank { null },
                target = item.optString("target").ifBlank { null },
                secondaryMuscles = item.optString("secondaryMuscles").ifBlank { null },
                instructions = item.optString("instructions").ifBlank { null },
            )
        }
        imported.chunked(250).forEach { database.exerciseDao().insertAll(it) }
    }

    suspend fun lookupBarcode(rawCode: String): Result<BarcodeProductEntity> = withContext(Dispatchers.IO) {
        runCatching {
            val barcode = rawCode.filter(Char::isDigit)
            require(barcode.length >= 6) { "Invalid barcode" }
            database.foodDao().getBarcodeProduct(barcode)?.let { return@runCatching it }

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
                fun number(key: String): Double? =
                    nutrients.optDouble(key, Double.NaN).takeUnless(Double::isNaN)

                BarcodeProductEntity(
                    barcode = barcode,
                    name = name,
                    caloriesPer100g = number("energy-kcal_100g"),
                    proteinPer100g = number("proteins_100g"),
                    carbsPer100g = number("carbohydrates_100g"),
                    fatPer100g = number("fat_100g"),
                    servingG = product.optDouble("serving_quantity", Double.NaN)
                        .takeUnless(Double::isNaN),
                ).also { database.foodDao().cacheBarcodeProduct(it) }
            } finally {
                connection.disconnect()
            }
        }
    }
}
