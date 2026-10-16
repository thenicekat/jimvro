package com.divyateja.jimvro.ui

import com.divyateja.jimvro.data.FoodEntryEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class FoodDayMetricsTest {
    @Test
    fun `totals include only selected day and handle missing macros`() {
        val entries = listOf(
            FoodEntryEntity(consumedOn = "2026-10-12", name = "Breakfast", calories = 300.0, proteinG = 20.0, carbsG = 35.0),
            FoodEntryEntity(consumedOn = "2026-10-12", name = "Dinner", calories = 550.0, proteinG = 42.0, fatG = 18.0),
            FoodEntryEntity(consumedOn = "2026-10-13", name = "Lunch", calories = 700.0, proteinG = 50.0),
        )

        assertEquals(FoodDayMetrics(850.0, 62.0, 35.0, 18.0), foodDayMetrics(entries, "2026-10-12"))
        assertEquals(FoodDayMetrics(700.0, 50.0, 0.0, 0.0), foodDayMetrics(entries, "2026-10-13"))
        assertEquals(FoodDayMetrics(0.0, 0.0, 0.0, 0.0), foodDayMetrics(entries, "2026-10-11"))
        assertEquals(listOf(entries[0], entries[1]), foodEntriesOn(entries, "2026-10-12"))
        assertEquals(listOf(entries[2]), foodEntriesOn(entries, "2026-10-13"))
        assertEquals(emptyList<FoodEntryEntity>(), foodEntriesOn(entries, "2026-10-11"))
    }
}
