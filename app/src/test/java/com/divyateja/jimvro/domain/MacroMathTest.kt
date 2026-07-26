package com.divyateja.jimvro.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MacroMathTest {
    @Test
    fun `scales per 100g macros to serving`() {
        val result = scaleMacros(
            Macros(calories = 250.0, proteinG = 10.0, carbsG = 30.0, fatG = 8.0),
            servingG = 40.0,
        )

        assertEquals(100.0, result.calories!!, 0.001)
        assertEquals(4.0, result.proteinG!!, 0.001)
        assertEquals(12.0, result.carbsG!!, 0.001)
        assertEquals(3.2, result.fatG!!, 0.001)
    }

    @Test
    fun `preserves unknown macro and clamps negative serving`() {
        val result = scaleMacros(Macros(calories = 200.0), servingG = -5.0)

        assertEquals(0.0, result.calories!!, 0.001)
        assertNull(result.proteinG)
    }
}
