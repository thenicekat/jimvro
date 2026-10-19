package com.divyateja.jimvro.ui

import com.divyateja.jimvro.data.FoodEntryEntity
import com.divyateja.jimvro.data.SavedFoodEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FoodDayMetricsTest {
    @Test
    fun `totals include only selected day and handle missing macros`() {
        val entries = listOf(
            FoodEntryEntity(consumedOn = "2026-10-12", name = "Breakfast", calories = 300.0, proteinG = 20.0, carbsG = 35.0),
            FoodEntryEntity(consumedOn = "2026-10-12", name = "Dinner", calories = 550.0, proteinG = 42.0, fatG = 18.0),
            FoodEntryEntity(consumedOn = "2026-10-13", name = "Lunch", calories = 700.0, proteinG = 50.0),
        )

        assertEquals(FoodDayMetrics(850.0, 62.0, 35.0, 18.0, entryCount = 2, missingCarbs = 1, missingFat = 1), foodDayMetrics(entries, "2026-10-12"))
        assertEquals(FoodDayMetrics(700.0, 50.0, 0.0, 0.0, entryCount = 1, missingCarbs = 1, missingFat = 1), foodDayMetrics(entries, "2026-10-13"))
        assertEquals(FoodDayMetrics(0.0, 0.0, 0.0, 0.0), foodDayMetrics(entries, "2026-10-11"))
        assertEquals(listOf(entries[0], entries[1]), foodEntriesOn(entries, "2026-10-12"))
        assertEquals(listOf(entries[2]), foodEntriesOn(entries, "2026-10-13"))
        assertEquals(emptyList<FoodEntryEntity>(), foodEntriesOn(entries, "2026-10-11"))
    }

    @Test fun `unknown zero and partial totals stay distinct`() {
        assertEquals("—", nutritionAmount(null))
        assertEquals("0", nutritionAmount(0.0))
        assertEquals("—", nutritionAmount(Double.NaN))
        assertEquals("—", nutritionAmount(-1.0))
        assertEquals("—", nutritionTotal(0.0, entryCount = 1, missing = 1))
        assertEquals("≥ 20", nutritionTotal(20.0, entryCount = 2, missing = 1))
        assertEquals("0", nutritionTotal(0.0, entryCount = 0, missing = 0))
    }

    @Test fun `legacy invalid values cannot reduce known totals`() {
        val entries = listOf(
            FoodEntryEntity(consumedOn = "2026-10-19", name = "Known", calories = 100.0, proteinG = 0.0),
            FoodEntryEntity(consumedOn = "2026-10-19", name = "Invalid", calories = -10.0, proteinG = Double.POSITIVE_INFINITY),
        )
        val totals = foodDayMetrics(entries, "2026-10-19")
        assertEquals(100.0, totals.calories, 0.001)
        assertEquals(1, totals.missingCalories)
        assertEquals(1, totals.missingProtein)
    }

    @Test fun `quick add reviews missing or invalid essentials but permits zero and optional blanks`() {
        val known = SavedFoodEntity(name = "Known", calories = 100.0, proteinG = 0.0)
        assertTrue(known.readyForQuickAdd())
        assertFalse(known.copy(calories = null).readyForQuickAdd())
        assertFalse(known.copy(proteinG = -2.0).readyForQuickAdd())
        assertFalse(known.copy(carbsG = Double.NaN).readyForQuickAdd())
    }
}
