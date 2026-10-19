package com.divyateja.jimvro.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeContrastTest {
    private fun contrast(a: Color, b: Color): Float {
        val light = maxOf(a.luminance(), b.luminance())
        val dark = minOf(a.luminance(), b.luminance())
        return (light + 0.05f) / (dark + 0.05f)
    }

    @Test fun `accent text and selected calendar dates meet normal text contrast in both themes`() {
        listOf(LightColors, DarkColors).forEach { colors ->
            assertTrue(contrast(colors.secondary, colors.background) >= 4.5f)
            assertTrue(contrast(colors.secondary, colors.surface) >= 4.5f)
            assertTrue(contrast(colors.onSecondaryContainer, colors.secondaryContainer) >= 4.5f)
        }
    }

    @Test fun `field boundaries meet nontext contrast in both themes`() {
        listOf(LightColors, DarkColors).forEach { colors ->
            assertTrue(contrast(colors.outline, colors.surface) >= 3f)
        }
    }
}
