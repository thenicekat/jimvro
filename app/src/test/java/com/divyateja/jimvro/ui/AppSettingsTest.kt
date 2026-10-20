package com.divyateja.jimvro.ui

import com.divyateja.jimvro.ui.theme.AccentColor
import org.junit.Assert.assertEquals
import org.junit.Test

class AppSettingsTest {
    @Test fun `missing or unrecognized accent preserves existing olive default`() {
        assertEquals(AccentColor.OLIVE, AppSettings().accentColor)
        assertEquals(AccentColor.OLIVE, AccentColor.fromPreference(null))
        assertEquals(AccentColor.OLIVE, AccentColor.fromPreference("future-color"))
    }

    @Test fun `every accent round trips through its persisted name`() {
        AccentColor.entries.forEach { accent ->
            assertEquals(accent, AccentColor.fromPreference(accent.name))
        }
    }

    @Test fun `pounds round trip preserves stored kilograms`() {
        val settings = AppSettings(weightUnit = "lb")
        assertEquals(100.0, 100.0.displayWeight(settings).storageWeight(settings), 0.0001)
    }

    @Test fun `inches round trip preserves stored centimeters`() {
        val settings = AppSettings(lengthUnit = "in")
        assertEquals(40.0, 40.0.displayLength(settings).storageLength(settings), 0.0001)
    }
}
