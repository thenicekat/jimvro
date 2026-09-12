package com.divyateja.jimvro.domain

import com.divyateja.jimvro.data.FoodEntryEntity
import com.divyateja.jimvro.data.MeasurementEntity
import com.divyateja.jimvro.data.WorkoutSummary
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek
import kotlin.math.roundToInt

data class WeeklyScore(
    val score: Int,
    val trainingDone: Int,
    val trainingTarget: Int,
    val proteinDays: Int,
    val calorieDays: Int,
    val daysElapsed: Int,
    val weighedIn: Boolean,
    val weeklyWeightChangeKg: Double?,
)

fun weeklyScore(
    today: LocalDate,
    workouts: List<WorkoutSummary>,
    foods: List<FoodEntryEntity>,
    measurements: List<MeasurementEntity>,
    trainingTarget: Int,
    proteinTarget: Int,
    calorieTarget: Int,
): WeeklyScore {
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val inWeek: (String) -> Boolean = { runCatching { LocalDate.parse(it) in weekStart..today }.getOrDefault(false) }
    val daysElapsed = ChronoUnit.DAYS.between(weekStart, today).toInt() + 1
    val trainingDone = workouts.filter { it.finishedAt != null && inWeek(it.performedOn) }.map { it.performedOn }.distinct().size
    val dailyFood = foods.filter { inWeek(it.consumedOn) }.groupBy { it.consumedOn }
    val proteinDays = dailyFood.count { (_, entries) -> proteinTarget > 0 && entries.sumOf { it.proteinG ?: 0.0 } >= proteinTarget }
    val calorieDays = dailyFood.count { (_, entries) -> calorieTarget > 0 && entries.sumOf { it.calories ?: 0.0 } in 1.0..calorieTarget.toDouble() }
    val weighedIn = measurements.any { inWeek(it.measuredOn) && it.weightKg != null }
    val score = (
        40.0 * trainingDone.coerceAtMost(trainingTarget) / trainingTarget.coerceAtLeast(1) +
            30.0 * proteinDays / daysElapsed +
            20.0 * calorieDays / daysElapsed +
            if (weighedIn) 10 else 0
        ).roundToInt().coerceIn(0, 100)

    fun average(from: LocalDate, through: LocalDate) = measurements
        .filter { row -> row.weightKg != null && runCatching { LocalDate.parse(row.measuredOn) in from..through }.getOrDefault(false) }
        .mapNotNull { it.weightKg }
        .takeIf { it.isNotEmpty() }
        ?.average()

    val currentAverage = average(today.minusDays(6), today)
    val priorAverage = average(today.minusDays(13), today.minusDays(7))
    return WeeklyScore(
        score, trainingDone, trainingTarget, proteinDays, calorieDays, daysElapsed, weighedIn,
        if (currentAverage != null && priorAverage != null) currentAverage - priorAverage else null,
    )
}
