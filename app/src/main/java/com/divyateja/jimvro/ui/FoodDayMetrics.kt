package com.divyateja.jimvro.ui

import com.divyateja.jimvro.data.FoodEntryEntity

internal data class FoodDayMetrics(
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
)

internal fun foodEntriesOn(entries: List<FoodEntryEntity>, date: String): List<FoodEntryEntity> =
    entries.filter { it.consumedOn == date }

internal fun foodDayMetrics(entries: List<FoodEntryEntity>, date: String): FoodDayMetrics {
    val dayEntries = foodEntriesOn(entries, date)
    return FoodDayMetrics(
        calories = dayEntries.sumOf { it.calories ?: 0.0 },
        proteinG = dayEntries.sumOf { it.proteinG ?: 0.0 },
        carbsG = dayEntries.sumOf { it.carbsG ?: 0.0 },
        fatG = dayEntries.sumOf { it.fatG ?: 0.0 },
    )
}
