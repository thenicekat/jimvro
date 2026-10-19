package com.divyateja.jimvro.domain

import com.divyateja.jimvro.data.FoodEntryEntity
import com.divyateja.jimvro.data.SavedFoodEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NutritionGuidanceTest {
    private val meals = listOf(8 * 60, 13 * 60, 20 * 60)

    @Test fun `next meal portions reflect remaining goals and meal times`() {
        val food = FoodEntryEntity(consumedOn = "2026-10-18", name = "Breakfast", calories = 300.0, proteinG = 40.0)
        val guide = nutritionGuidance(listOf(food), emptyList(), 2_100, 120, meals, 11 * 60)!!

        assertEquals(1_800, guide.caloriesLeft)
        assertEquals(80, guide.proteinLeftG)
        assertEquals(840, guide.nextMealCalories) // Small catch-up cap, not 1,500 kcal in one meal.
        assertEquals(40, guide.nextMealProteinG)
        assertEquals(2, guide.mealsLeft)
    }

    @Test fun `adequate protein or tiny gap does not trigger reminder`() {
        val food = FoodEntryEntity(consumedOn = "2026-10-18", name = "Food", calories = 1_200.0, proteinG = 77.0)
        val guide = nutritionGuidance(listOf(food), emptyList(), 2_000, 120, meals, 13 * 60)!!
        assertFalse(guide.proteinBehindPace) // 77 g logged vs 80 g expected by lunch.

        val complete = nutritionGuidance(listOf(food.copy(proteinG = 120.0)), emptyList(), 2_000, 120, meals, 20 * 60)!!
        assertFalse(complete.proteinBehindPace)
        assertNull(complete.nextMealProteinG)
    }

    @Test fun `behind pace at lunch suggests protein without giant dinner catch up`() {
        val food = FoodEntryEntity(consumedOn = "2026-10-18", name = "Breakfast", calories = 400.0, proteinG = 15.0)
        val guide = nutritionGuidance(listOf(food), emptyList(), 2_000, 120, meals, 13 * 60)!!
        assertTrue(guide.proteinBehindPace)

        val dinner = nutritionGuidance(emptyList(), emptyList(), 2_000, 120, meals, 20 * 60)!!
        assertEquals(40, dinner.nextMealProteinG)
        assertEquals(667, dinner.nextMealCalories)
        assertFalse(dinner.proteinBehindPace) // No logs: one initial nudge, not three repeat pings.
    }

    @Test fun `empty log gives a starting split without assuming earlier meals were skipped`() {
        val guide = nutritionGuidance(emptyList(), emptyList(), 2_000, 150, meals, 9 * 60)!!
        assertFalse(guide.hasLoggedFood)
        assertEquals(667, guide.nextMealCalories)
        assertEquals(50, guide.nextMealProteinG)
    }

    @Test fun `missing macros never imply the person ate nothing`() {
        val food = FoodEntryEntity(consumedOn = "2026-10-18", name = "Unknown", calories = 350.0)
        val guide = nutritionGuidance(listOf(food), emptyList(), 2_000, 120, meals, 13 * 60)!!
        assertTrue(guide.incompleteLog)
        assertNull(guide.proteinLeftG)
        assertFalse(guide.proteinBehindPace)
    }

    @Test fun `reaching calorie target does not prompt extra food for protein`() {
        val food = FoodEntryEntity(consumedOn = "2026-10-18", name = "Food", calories = 2_000.0, proteinG = 20.0)
        val guide = nutritionGuidance(listOf(food), emptyList(), 2_000, 120, meals, 13 * 60)!!
        assertEquals(0, guide.caloriesLeft)
        assertFalse(guide.proteinBehindPace)
        assertNull(guide.nextMealProteinG)
    }

    @Test fun `protein suggestion fits remaining energy budget`() {
        val food = FoodEntryEntity(consumedOn = "2026-10-18", name = "Food", calories = 1_900.0, proteinG = 20.0)
        val guide = nutritionGuidance(listOf(food), emptyList(), 2_000, 150, meals, 13 * 60)!!
        assertEquals(50, guide.nextMealCalories)
        assertEquals(12, guide.nextMealProteinG)
    }

    @Test fun `saved idea uses a known portion rather than an invented food`() {
        val saved = SavedFoodEntity(name = "Rice and chicken", calories = 650.0, proteinG = 40.0)
        val guide = nutritionGuidance(emptyList(), listOf(saved), 1_950, 120, meals, 8 * 60)!!
        assertEquals("Rice and chicken", guide.foodIdea?.name)
        assertEquals(1.0, guide.foodIdea?.portions)
        assertNull(nutritionGuidance(emptyList(), listOf(saved.copy(proteinG = null)), 1_950, 120, meals, 8 * 60)?.foodIdea)
        assertNull(nutritionGuidance(emptyList(), listOf(saved.copy(proteinG = 0.0)), 1_950, 120, meals, 8 * 60)?.foodIdea)
    }

    @Test fun `past last meal and unset goals produce no new meal instruction`() {
        val guide = nutritionGuidance(emptyList(), emptyList(), 2_000, 120, meals, 22 * 60)!!
        assertEquals(0, guide.mealsLeft)
        assertNull(guide.nextMealCalories)
        assertNull(guide.nextMealProteinG)
        assertNull(nutritionGuidance(emptyList(), emptyList(), 0, 0, meals, 8 * 60))
    }
}
