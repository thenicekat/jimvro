package com.divyateja.jimvro.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FoodPhotoEstimateTest {
    @Test
    fun parsesPlainJson() {
        val result = parseFoodPhotoEstimate(
            """{"name":"Chicken bowl","calories":520,"proteinG":42,"carbsG":48,"fatG":16}""",
        )
        assertEquals("Chicken bowl", result.name)
        assertEquals(520.0, result.calories!!, 0.001)
        assertEquals(42.0, result.proteinG!!, 0.001)
        assertEquals(48.0, result.carbsG!!, 0.001)
        assertEquals(16.0, result.fatG!!, 0.001)
    }

    @Test
    fun stripsMarkdownFenceAndAlternateKeys() {
        val result = parseFoodPhotoEstimate(
            """
            ```json
            {"name":"Oats","kcal":300,"protein":12,"carbohydrates":45,"fat":8}
            ```
            """.trimIndent(),
        )
        assertEquals("Oats", result.name)
        assertEquals(300.0, result.calories!!, 0.001)
        assertEquals(12.0, result.proteinG!!, 0.001)
        assertEquals(45.0, result.carbsG!!, 0.001)
        assertEquals(8.0, result.fatG!!, 0.001)
    }

    @Test
    fun blankNameFallsBack() {
        val result = parseFoodPhotoEstimate("""{"name":"  ","calories":100}""")
        assertEquals("Food from photo", result.name)
        assertEquals(100.0, result.calories!!, 0.001)
        assertNull(result.proteinG)
    }
}
