package com.divyateja.jimvro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NutritionPlanTest {
    private fun profile(goal: NutritionGoal) = NutritionPlanInput(
        goal = goal,
        weightKg = 80.0,
        heightCm = 180.0,
        method = MaintenanceMethod.PROFILE,
        activity = ActivityLevel.MODERATE,
        age = 30,
        estimateSex = EstimateSex.MALE,
    )

    @Test fun `profile estimate changes gently for each goal`() {
        val recomp = estimateNutritionPlan(profile(NutritionGoal.RECOMP))!!
        val cut = estimateNutritionPlan(profile(NutritionGoal.CUT))!!
        val bulk = estimateNutritionPlan(profile(NutritionGoal.BULK))!!

        assertEquals(2_750, recomp.maintenanceKcal)
        assertEquals(2_750, recomp.calorieTarget)
        assertEquals(145, recomp.proteinTargetG)
        assertEquals(2_450, cut.calorieTarget)
        assertEquals(160, cut.proteinTargetG)
        assertEquals(3_000, bulk.calorieTarget)
        assertEquals(130, bulk.proteinTargetG)
    }

    @Test fun `measured BMR and known maintenance use their own sources`() {
        val measured = estimateNutritionPlan(profile(NutritionGoal.RECOMP).copy(
            method = MaintenanceMethod.BODY_SCAN, measuredBmrKcal = 1_600.0,
            age = null, estimateSex = null,
        ))!!
        assertEquals(2_500, measured.maintenanceKcal)
        val known = estimateNutritionPlan(profile(NutritionGoal.CUT).copy(
            method = MaintenanceMethod.KNOWN, knownMaintenanceKcal = 2_500.0,
            activity = ActivityLevel.LOW, age = null, estimateSex = null,
        ))!!
        assertEquals(2_200, known.calorieTarget)
    }

    @Test fun `unsupported inputs do not yield a calorie prescription`() {
        assertNull(estimateNutritionPlan(profile(NutritionGoal.CUT).copy(age = 16)))
        assertNull(estimateNutritionPlan(profile(NutritionGoal.CUT).copy(weightKg = 48.0, heightCm = 180.0)))
        assertNull(estimateNutritionPlan(profile(NutritionGoal.BULK).copy(estimateSex = null)))
        assertNull(estimateNutritionPlan(profile(NutritionGoal.RECOMP).copy(method = MaintenanceMethod.KNOWN, knownMaintenanceKcal = Double.NaN)))
    }
}
