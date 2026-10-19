package com.divyateja.jimvro.ui

import com.divyateja.jimvro.data.FoodEntryEntity
import com.divyateja.jimvro.data.SavedFoodEntity

internal data class FoodDayMetrics(
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val entryCount: Int = 0,
    val missingCalories: Int = 0,
    val missingProtein: Int = 0,
    val missingCarbs: Int = 0,
    val missingFat: Int = 0,
)

internal fun Double?.knownNutrition(): Double? = this?.takeIf { it.isFinite() && it >= 0 }

internal fun nutritionAmount(value: Double?): String = value.knownNutrition()?.let {
    if (it % 1.0 == 0.0) "%.0f".format(it) else "%.1f".format(it)
} ?: "—"

internal fun nutritionTotal(value: Double, entryCount: Int, missing: Int): String = when {
    entryCount > 0 && missing == entryCount -> "—"
    missing > 0 -> "≥ ${nutritionAmount(value)}"
    else -> nutritionAmount(value)
}

internal fun SavedFoodEntity.readyForQuickAdd(): Boolean =
    calories.knownNutrition() != null && proteinG.knownNutrition() != null &&
        listOfNotNull(carbsG, fatG).all { it.knownNutrition() != null }

internal fun foodEntriesOn(entries: List<FoodEntryEntity>, date: String): List<FoodEntryEntity> =
    entries.filter { it.consumedOn == date }

internal fun foodDayMetrics(entries: List<FoodEntryEntity>, date: String): FoodDayMetrics {
    val dayEntries = foodEntriesOn(entries, date)
    return FoodDayMetrics(
        calories = dayEntries.sumOf { it.calories.knownNutrition() ?: 0.0 },
        proteinG = dayEntries.sumOf { it.proteinG.knownNutrition() ?: 0.0 },
        carbsG = dayEntries.sumOf { it.carbsG.knownNutrition() ?: 0.0 },
        fatG = dayEntries.sumOf { it.fatG.knownNutrition() ?: 0.0 },
        entryCount = dayEntries.size,
        missingCalories = dayEntries.count { it.calories.knownNutrition() == null },
        missingProtein = dayEntries.count { it.proteinG.knownNutrition() == null },
        missingCarbs = dayEntries.count { it.carbsG.knownNutrition() == null },
        missingFat = dayEntries.count { it.fatG.knownNutrition() == null },
    )
}
