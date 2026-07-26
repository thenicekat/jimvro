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

val Paper = Color(0xFFF2F0EA)
val PaperCard = Color(0xFFFAF9F5)
val Ink = Color(0xFF20221F)
val MutedInk = Color(0xFF6F716A)
val Clay = Color(0xFF8B7045)
val ClayMuted = Color(0xFFE6DFD1)
val Espresso = Color(0xFF181A17)
val EspressoCard = Color(0xFF232520)

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
    surfaceVariant = Color(0xFFE9E6DE),
    onSurfaceVariant = MutedInk,
    surfaceContainerLowest = PaperCard,
    surfaceContainerLow = PaperCard,
    surfaceContainer = PaperCard,
    surfaceContainerHigh = PaperCard,
    surfaceContainerHighest = Color(0xFFEDEAE3),
    outline = Color(0xFFCECBC2),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF1EEE6),
    onPrimary = Espresso,
    secondary = Color(0xFFB99A67),
    onSecondary = Espresso,
    tertiary = Color(0xFFB99A67),
    background = Espresso,
    onBackground = Color(0xFFF0EDE5),
    surface = EspressoCard,
    onSurface = Color(0xFFF0EDE5),
    surfaceVariant = Color(0xFF30332C),
    onSurfaceVariant = Color(0xFFBEBDB5),
    surfaceContainerLowest = Espresso,
    surfaceContainerLow = EspressoCard,
    surfaceContainer = EspressoCard,
    surfaceContainerHigh = Color(0xFF2B2E28),
    surfaceContainerHighest = Color(0xFF34372F),
    outline = Color(0xFF50534A),
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
