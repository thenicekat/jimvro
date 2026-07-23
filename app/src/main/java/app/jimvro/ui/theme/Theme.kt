package app.jimvro.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Paper = Color(0xFFF7F3EA)
val PaperCard = Color(0xFFFFFCF5)
val Ink = Color(0xFF3C332C)
val MutedInk = Color(0xFF7B7067)
val Clay = Color(0xFFA65F47)
val ClayMuted = Color(0xFFF0D9CD)
val Espresso = Color(0xFF2A2421)
val EspressoCard = Color(0xFF38302C)

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
    surfaceVariant = Color(0xFFEFE9DF),
    onSurfaceVariant = MutedInk,
    outline = Color(0xFFD8CEC2),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF0E7DE),
    onPrimary = Espresso,
    secondary = Color(0xFFD99172),
    onSecondary = Espresso,
    tertiary = Color(0xFFD99172),
    background = Espresso,
    onBackground = Color(0xFFF4ECE4),
    surface = EspressoCard,
    onSurface = Color(0xFFF4ECE4),
    surfaceVariant = Color(0xFF463C37),
    onSurfaceVariant = Color(0xFFC9BBB1),
    outline = Color(0xFF665850),
)

@Composable
fun JimvroTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = androidx.compose.material3.Typography(),
        content = content,
    )
}
