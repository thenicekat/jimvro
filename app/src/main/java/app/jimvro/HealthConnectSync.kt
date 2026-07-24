package app.jimvro

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.MealType
import androidx.health.connect.client.records.NutritionRecord
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Energy
import androidx.health.connect.client.units.Mass
import androidx.health.connect.client.units.Percentage
import app.jimvro.data.FoodEntryEntity
import app.jimvro.data.MeasurementEntity
import app.jimvro.data.WorkoutSummary
import java.time.Instant
import java.time.ZoneId

object HealthConnectSync {
    val permissions = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(WeightRecord::class),
        HealthPermission.getWritePermission(BodyFatRecord::class),
        HealthPermission.getWritePermission(NutritionRecord::class),
    )

    fun availability(context: Context): Int =
        HealthConnectClient.getSdkStatus(context)

    suspend fun sync(
        context: Context,
        workouts: List<WorkoutSummary>,
        measurements: List<MeasurementEntity>,
        foods: List<FoodEntryEntity>,
    ): Int {
        val client = HealthConnectClient.getOrCreate(context)
        check(client.permissionController.getGrantedPermissions().containsAll(permissions))

        val records = buildList<Record> {
            workouts.filter { it.finishedAt != null && it.finishedAt > it.createdAt }.forEach { workout ->
                val start = Instant.ofEpochMilli(workout.createdAt)
                val end = Instant.ofEpochMilli(workout.finishedAt!!)
                add(
                    ExerciseSessionRecord(
                        startTime = start,
                        startZoneOffset = zoneOffset(start),
                        endTime = end,
                        endZoneOffset = zoneOffset(end),
                        exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING,
                        title = workout.name ?: "Strength workout",
                        notes = "${workout.setCount} sets · ${workout.volumeKg.pretty()} kg volume",
                        metadata = Metadata.manualEntry(
                            clientRecordId = "jimvro-workout-${workout.id}",
                            clientRecordVersion = 1,
                        ),
                    ),
                )
            }
            measurements.forEach { measurement ->
                val time = Instant.ofEpochMilli(measurement.createdAt)
                measurement.weightKg?.let { weight ->
                    add(
                        WeightRecord(
                            time = time,
                            zoneOffset = zoneOffset(time),
                            weight = Mass.kilograms(weight),
                            metadata = Metadata.manualEntry(
                                clientRecordId = "jimvro-weight-${measurement.id}",
                                clientRecordVersion = 1,
                            ),
                        ),
                    )
                }
                measurement.bodyFatPct?.let { bodyFat ->
                    add(
                        BodyFatRecord(
                            time = time,
                            zoneOffset = zoneOffset(time),
                            percentage = Percentage(bodyFat),
                            metadata = Metadata.manualEntry(
                                clientRecordId = "jimvro-body-fat-${measurement.id}",
                                clientRecordVersion = 1,
                            ),
                        ),
                    )
                }
            }
            foods.forEach { food ->
                val start = Instant.ofEpochMilli(food.createdAt)
                val end = start.plusSeconds(1)
                add(
                    NutritionRecord(
                        startTime = start,
                        startZoneOffset = zoneOffset(start),
                        endTime = end,
                        endZoneOffset = zoneOffset(end),
                        name = food.name,
                        mealType = food.meal.healthMealType(),
                        energy = food.calories?.let(Energy::kilocalories),
                        protein = food.proteinG?.let(Mass::grams),
                        totalCarbohydrate = food.carbsG?.let(Mass::grams),
                        totalFat = food.fatG?.let(Mass::grams),
                        metadata = Metadata.manualEntry(
                            clientRecordId = "jimvro-food-${food.id}",
                            clientRecordVersion = 1,
                        ),
                    ),
                )
            }
        }
        if (records.isNotEmpty()) client.insertRecords(records)
        return records.size
    }

    private fun zoneOffset(instant: Instant) = ZoneId.systemDefault().rules.getOffset(instant)
    private fun Double.pretty() = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)
    private fun String.healthMealType() = when (lowercase()) {
        "breakfast" -> MealType.MEAL_TYPE_BREAKFAST
        "lunch" -> MealType.MEAL_TYPE_LUNCH
        "dinner" -> MealType.MEAL_TYPE_DINNER
        else -> MealType.MEAL_TYPE_SNACK
    }
}
