package app.jimvro.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {
    @Test fun `pounds round trip preserves stored kilograms`() {
        val settings = AppSettings(weightUnit = "lb")
        assertEquals(100.0, 100.0.displayWeight(settings).storageWeight(settings), 0.0001)
    }

    @Test fun `inches round trip preserves stored centimeters`() {
        val settings = AppSettings(lengthUnit = "in")
        assertEquals(40.0, 40.0.displayLength(settings).storageLength(settings), 0.0001)
    }
}
