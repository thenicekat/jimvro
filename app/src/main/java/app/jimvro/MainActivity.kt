package app.jimvro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import app.jimvro.ui.JimvroApp
import app.jimvro.ui.theme.JimvroTheme
import app.jimvro.ui.theme.ThemeMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as JimvroApplication).repository
        val preferences = getSharedPreferences("jimvro_settings", MODE_PRIVATE)
        setContent {
            var themeMode by remember {
                mutableStateOf(
                    runCatching { ThemeMode.valueOf(preferences.getString("theme_mode", ThemeMode.SYSTEM.name)!!) }
                        .getOrDefault(ThemeMode.SYSTEM),
                )
            }
            JimvroTheme(themeMode) {
                val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory(repository))
                JimvroApp(appViewModel, themeMode) { mode ->
                    themeMode = mode
                    preferences.edit().putString("theme_mode", mode.name).apply()
                }
            }
        }
    }
}
