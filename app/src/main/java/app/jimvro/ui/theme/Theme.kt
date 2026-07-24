package app.jimvro.ui.theme

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
import app.jimvro.R

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

@Composable
fun JimvroTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val baseTypography = androidx.compose.material3.Typography()
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = baseTypography.copy(
            labelLarge = baseTypography.labelLarge.copy(fontWeight = FontWeight.Normal),
            labelMedium = baseTypography.labelMedium.copy(fontWeight = FontWeight.Normal),
            titleLarge = baseTypography.titleLarge.copy(fontWeight = FontWeight.Normal),
            titleMedium = baseTypography.titleMedium.copy(fontWeight = FontWeight.Normal),
            titleSmall = baseTypography.titleSmall.copy(fontWeight = FontWeight.Normal),
        ),
        content = content,
    )
}
