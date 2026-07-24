package app.divyateja.jimvro

import android.app.Application
import app.divyateja.jimvro.data.JimvroDatabase
import app.divyateja.jimvro.data.JimvroRepository
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
            if (preferences.getInt("stock_templates_version", 0) < 2) {
                repository.seedStockTemplates()
                preferences.edit().putInt("stock_templates_version", 2).apply()
            }
        }
    }
}
