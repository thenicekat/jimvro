package com.divyateja.jimvro.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.divyateja.jimvro.R

val Paper = Color(0xFFF7F8F4)
val PaperCard = Color(0xFFFFFFFF)
val Ink = Color(0xFF141813)
val MutedInk = Color(0xFF62695E)
val Clay = Color(0xFF577B20)
val ClayMuted = Color(0xFFE1EBCF)
val Espresso = Color(0xFF0D0F0E)
val EspressoCard = Color(0xFF171A18)

val Fraunces = FontFamily(
    Font(R.font.fraunces, FontWeight.Normal),
    Font(R.font.fraunces, FontWeight.Medium),
    Font(R.font.fraunces, FontWeight.SemiBold),
)

private val LightColors = lightColorScheme(
    primary = Ink,
    onPrimary = PaperCard,
    secondary = Clay,
    onSecondary = Color.White,
    tertiary = Clay,
    background = Paper,
    onBackground = Ink,
    surface = PaperCard,
    onSurface = Ink,
    surfaceVariant = Color(0xFFE8ECE1),
    onSurfaceVariant = MutedInk,
    surfaceContainerLowest = PaperCard,
    surfaceContainerLow = PaperCard,
    surfaceContainer = PaperCard,
    surfaceContainerHigh = PaperCard,
    surfaceContainerHighest = Color(0xFFE9EDE3),
    outline = Color(0xFFC8CEC1),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF1F4EC),
    onPrimary = Espresso,
    secondary = Color(0xFFA6D85D),
    onSecondary = Color(0xFF152000),
    tertiary = Color(0xFFA6D85D),
    background = Espresso,
    onBackground = Color(0xFFF0F3EB),
    surface = EspressoCard,
    onSurface = Color(0xFFF0F3EB),
    surfaceVariant = Color(0xFF242A21),
    onSurfaceVariant = Color(0xFFBCC4B5),
    surfaceContainerLowest = Espresso,
    surfaceContainerLow = EspressoCard,
    surfaceContainer = EspressoCard,
    surfaceContainerHigh = Color(0xFF20241F),
    surfaceContainerHighest = Color(0xFF292E27),
    outline = Color(0xFF51594F),
)

enum class ThemeMode { SYSTEM, LIGHT, DARK }

@Composable
fun JimvroTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val baseTypography = androidx.compose.material3.Typography()
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = baseTypography.copy(
            displaySmall = baseTypography.displaySmall.copy(fontSize = 36.sp, lineHeight = 40.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.5).sp),
            headlineLarge = baseTypography.headlineLarge.copy(fontSize = 30.sp, lineHeight = 35.sp, fontWeight = FontWeight.Normal, letterSpacing = (-0.35).sp),
            headlineMedium = baseTypography.headlineMedium.copy(fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.Normal),
            titleLarge = baseTypography.titleLarge.copy(fontSize = 20.sp, lineHeight = 25.sp, fontWeight = FontWeight.Medium),
            titleMedium = baseTypography.titleMedium.copy(fontSize = 16.sp, lineHeight = 21.sp, fontWeight = FontWeight.Medium),
            titleSmall = baseTypography.titleSmall.copy(fontSize = 14.sp, lineHeight = 19.sp, fontWeight = FontWeight.Medium),
            bodyLarge = baseTypography.bodyLarge.copy(fontSize = 16.sp, lineHeight = 23.sp, fontWeight = FontWeight.Normal),
            bodyMedium = baseTypography.bodyMedium.copy(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal),
            labelLarge = baseTypography.labelLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
            labelMedium = baseTypography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
        ),
        content = content,
    )
}
