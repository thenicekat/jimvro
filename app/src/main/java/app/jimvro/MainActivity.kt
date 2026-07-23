package app.jimvro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import app.jimvro.ui.JimvroApp
import app.jimvro.ui.theme.JimvroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as JimvroApplication).repository
        setContent {
            JimvroTheme {
                val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory(repository))
                JimvroApp(appViewModel)
            }
        }
    }
}
