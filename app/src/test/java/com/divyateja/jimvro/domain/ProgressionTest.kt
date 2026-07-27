package com.divyateja.jimvro.domain

import com.divyateja.jimvro.data.PreviousSet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressionTest {
    @Test
    fun increasesWeightWhenEveryTopSetReachesRepCeiling() {
        val result = progressionSuggestion(
            listOf(PreviousSet(1, 10, 60.0, "2026-07-20"), PreviousSet(2, 11, 60.0, "2026-07-20")),
            targetRepLow = 8,
            targetRepHigh = 10,
        )!!

        assertEquals(62.5, result.weightKg, 0.001)
        assertTrue(result.increase)
    }

    @Test
    fun repeatsWeightUntilAllTopSetsReachCeiling() {
        val result = progressionSuggestion(
            listOf(PreviousSet(1, 10, 60.0, "2026-07-20"), PreviousSet(2, 8, 60.0, "2026-07-20")),
            targetRepLow = 8,
            targetRepHigh = 10,
        )!!

        assertEquals(60.0, result.weightKg, 0.001)
        assertFalse(result.increase)
    }

    @Test
    fun requiresTemplateRepRangeAndPreviousWeight() {
        assertNull(progressionSuggestion(emptyList(), 8, 10))
        assertNull(progressionSuggestion(listOf(PreviousSet(1, 10, 60.0, "2026-07-20")), null, 10))
    }
}
