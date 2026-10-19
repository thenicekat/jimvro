package com.divyateja.jimvro.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NumberInputTest {
    @Test fun `optional blank and legitimate zero remain valid`() {
        assertNull(numberInputError(""))
        assertNull(numberInputError("  "))
        assertNull(decimalValue(""))
        assertNull(numberInputError("0"))
        assertEquals(0.0, decimalValue("0")!!, 0.001)
    }

    @Test fun `invalid negative nonfinite and overflowing inputs are rejected`() {
        listOf("abc", "1.2.3", "1,2,3", "-1", "NaN", "Infinity", "1e309").forEach {
            assertNotNull("Expected error for $it", numberInputError(it))
        }
    }

    @Test fun `decimal keyboard separators and surrounding spaces are supported`() {
        assertEquals(72.5, decimalValue(" 72,5 ")!!, 0.001)
        assertEquals(72.5, decimalValue("72.5")!!, 0.001)
        assertNull(numberInputError("72,5"))
    }

    @Test fun `measurement bounds reject zero and percentages above 100`() {
        assertNotNull(numberInputError("0", allowZero = false))
        assertNotNull(numberInputError("101", maximum = 100.0))
        assertNull(numberInputError("99.5", allowZero = false, maximum = 100.0))
    }
}
