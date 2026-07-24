package app.jimvro

import android.app.Application
import app.jimvro.data.JimvroDatabase
import app.jimvro.data.JimvroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class JimvroApplication : Application() {
    private val database by lazy { JimvroDatabase.create(this) }
    val repository by lazy { JimvroRepository(database) }

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            repository.removeBundledExercises()
            val preferences = getSharedPreferences("jimvro_settings", MODE_PRIVATE)
            if (!preferences.getBoolean("stock_templates_seeded", false)) {
                repository.seedStockTemplates()
                preferences.edit().putBoolean("stock_templates_seeded", true).apply()
            }
        }
    }
}
