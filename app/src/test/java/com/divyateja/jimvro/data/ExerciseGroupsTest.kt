package com.divyateja.jimvro.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseGroupsTest {
    @Test fun catalogTargetsMapToPickerGroups() {
        assertEquals("chest", canonicalMuscleGroup("pectorals"))
        assertEquals("back", canonicalMuscleGroup("upper back"))
        assertEquals("quads", canonicalMuscleGroup("quads"))
        assertEquals("cardio", canonicalMuscleGroup("cardiovascular system"))
        assertTrue(stockExerciseGroups.values.all { it in MUSCLE_GROUPS })
        assertTrue(LEGACY_MUSCLE_GROUPS.none { it in MUSCLE_GROUPS })
    }
}
