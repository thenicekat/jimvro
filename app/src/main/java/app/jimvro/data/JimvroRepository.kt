package app.jimvro.data

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject

class JimvroRepository(private val database: JimvroDatabase) {
    val measurements = database.measurementDao().observeAll()
    val latestMeasurement = database.measurementDao().observeLatest()
    val workouts = database.workoutDao().observeSummaries()
    val exercises = database.exerciseDao().observeAll()
    val foodEntries = database.foodDao().observeAll()

    fun foodOn(date: String): Flow<List<FoodEntryEntity>> = database.foodDao().observeOn(date)
    fun nutritionOn(date: String): Flow<DailyNutrition> = database.foodDao().observeNutritionOn(date)
    fun workoutsOn(date: String): Flow<Int> = database.workoutDao().observeCountOn(date)

    suspend fun addMeasurement(value: MeasurementEntity) = database.measurementDao().insert(value)
    suspend fun deleteMeasurement(value: MeasurementEntity) = database.measurementDao().delete(value)
    suspend fun addWorkout(value: WorkoutEntity) = database.workoutDao().insertWorkout(value)
    suspend fun createWorkout(value: WorkoutEntity, sets: List<WorkoutSetEntity>) =
        database.workoutDao().createWorkout(value, sets)
    suspend fun deleteWorkout(id: Long) = database.workoutDao().deleteWorkout(id)
    suspend fun addFood(value: FoodEntryEntity) = database.foodDao().insert(value)
    suspend fun deleteFood(value: FoodEntryEntity) = database.foodDao().delete(value)

    suspend fun seedExercises() {
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
