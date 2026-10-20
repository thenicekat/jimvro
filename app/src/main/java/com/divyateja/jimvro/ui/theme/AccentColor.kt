package com.divyateja.jimvro.ui.theme

import androidx.compose.ui.graphics.Color

enum class AccentColor(val label: String) {
    OLIVE("Olive"), BLUE("Blue"), VIOLET("Violet"), ROSE("Rose"), AMBER("Amber");

    companion object {
        fun fromPreference(value: String?): AccentColor = entries.firstOrNull { it.name == value } ?: OLIVE
    }
}

internal data class AccentPalette(
    val color: Color,
    val onColor: Color,
    val container: Color,
    val onContainer: Color,
)

/** Paired roles, rather than a raw hue, keep each accent readable in both themes. */
internal fun accentPalette(accent: AccentColor, dark: Boolean): AccentPalette = when (accent) {
    AccentColor.OLIVE -> (if (dark) DarkColors else LightColors).let {
        AccentPalette(it.secondary, it.onSecondary, it.secondaryContainer, it.onSecondaryContainer)
    }
    AccentColor.BLUE -> if (dark) {
        AccentPalette(Color(0xFFADC7FF), Color(0xFF092F65), Color(0xFF233E67), Color(0xFFD6E3FF))
    } else {
        AccentPalette(Color(0xFF305DA8), Color.White, Color(0xFFD6E3FF), Color(0xFF102E59))
    }
    AccentColor.VIOLET -> if (dark) {
        AccentPalette(Color(0xFFD0BCFF), Color(0xFF381E72), Color(0xFF493468), Color(0xFFECDDFF))
    } else {
        AccentPalette(Color(0xFF6947A5), Color.White, Color(0xFFECDDFF), Color(0xFF2B0F52))
    }
    AccentColor.ROSE -> if (dark) {
        AccentPalette(Color(0xFFFFB0C8), Color(0xFF5E1133), Color(0xFF612D40), Color(0xFFFFD9E2))
    } else {
        AccentPalette(Color(0xFF98435E), Color.White, Color(0xFFFFD9E2), Color(0xFF3E001D))
    }
    AccentColor.AMBER -> if (dark) {
        AccentPalette(Color(0xFFEDC069), Color(0xFF412D00), Color(0xFF51401C), Color(0xFFFFDF9B))
    } else {
        AccentPalette(Color(0xFF785900), Color.White, Color(0xFFFFDF9B), Color(0xFF261A00))
    }
}
