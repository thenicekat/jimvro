package com.divyateja.jimvro.domain

import kotlin.math.min
import kotlin.math.roundToInt

enum class NutritionGoal(val label: String, val description: String, val proteinPerKg: Double) {
    CUT("Cut", "Lose weight gradually while keeping strength.", 2.0),
    RECOMP("Recomp", "Train around maintenance and build better habits.", 1.8),
    BULK("Bulk", "Gain weight gradually while training.", 1.6),
}

enum class MaintenanceMethod { PROFILE, BODY_SCAN, KNOWN }

enum class EstimateSex(val label: String, val adjustment: Int) {
    FEMALE("Female equation", -161),
    MALE("Male equation", 5),
}

enum class ActivityLevel(val label: String, val multiplier: Double) {
    LOW("Mostly sitting", 1.2),
    LIGHT("Lightly active", 1.375),
    MODERATE("Moderately active", 1.55),
    HIGH("Very active", 1.725),
}

data class NutritionPlanInput(
    val goal: NutritionGoal,
    val weightKg: Double,
    val heightCm: Double,
    val method: MaintenanceMethod,
    val activity: ActivityLevel = ActivityLevel.LIGHT,
    val age: Int? = null,
    val estimateSex: EstimateSex? = null,
    val measuredBmrKcal: Double? = null,
    val knownMaintenanceKcal: Double? = null,
)

data class NutritionPlanEstimate(
    val maintenanceKcal: Int,
    val calorieTarget: Int,
    val proteinTargetG: Int,
)

/** Conservative adult starting estimate; user reviews and can edit both targets. */
fun estimateNutritionPlan(input: NutritionPlanInput): NutritionPlanEstimate? {
    if (!input.weightKg.isFinite() || input.weightKg !in 35.0..250.0 ||
        !input.heightCm.isFinite() || input.heightCm !in 130.0..220.0) return null
    val heightM = input.heightCm / 100.0
    val bmi = input.weightKg / (heightM * heightM)
    // Outside this range, a generic energy formula is not a suitable automatic starting point.
    if (bmi !in 18.5..45.0) return null

    val maintenance = when (input.method) {
        MaintenanceMethod.KNOWN -> input.knownMaintenanceKcal?.takeIf { it.isFinite() && it in 1_400.0..5_500.0 }
        MaintenanceMethod.BODY_SCAN -> input.measuredBmrKcal
            ?.takeIf { it.isFinite() && it in 900.0..3_500.0 }
            ?.times(input.activity.multiplier)
        MaintenanceMethod.PROFILE -> {
            val age = input.age?.takeIf { it in 18..85 } ?: return null
            val sex = input.estimateSex ?: return null
            val bmr = 10 * input.weightKg + 6.25 * input.heightCm - 5 * age + sex.adjustment
            bmr * input.activity.multiplier
        }
    } ?: return null

    val adjustedCalories = when (input.goal) {
        NutritionGoal.CUT -> maintenance - min(maintenance * 0.12, 350.0)
        NutritionGoal.RECOMP -> maintenance
        NutritionGoal.BULK -> maintenance + min(maintenance * 0.08, 250.0)
    }
    if (adjustedCalories !in 1_400.0..5_000.0) return null

    // Very high body weight can overstate a weight-based protein target.
    val referenceWeightKg = min(input.weightKg, 30.0 * heightM * heightM)
    val protein = (referenceWeightKg * input.goal.proteinPerKg / 5.0).roundToInt() * 5
    return NutritionPlanEstimate(
        maintenanceKcal = (maintenance / 50.0).roundToInt() * 50,
        calorieTarget = (adjustedCalories / 50.0).roundToInt() * 50,
        proteinTargetG = protein.coerceAtMost(220),
    )
}
