package app.divyateja.jimvro

import android.app.Application
import app.divyateja.jimvro.data.JimvroDatabase
import app.divyateja.jimvro.data.JimvroRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JimvroApplication : Application() {
    private lateinit var database: JimvroDatabase
    lateinit var repository: JimvroRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = JimvroDatabase.create(this)
        repository = JimvroRepository(database)
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            repository.removeBundledExercises()
            val preferences = getSharedPreferences("jimvro_settings", MODE_PRIVATE)
            if (preferences.getInt("stock_templates_version", 0) < 2) {
                repository.seedStockTemplates()
                preferences.edit().putInt("stock_templates_version", 2).apply()
            }
        }
    }

    suspend fun reloadAfterRestore() = withContext(Dispatchers.IO) {
        database = JimvroDatabase.create(this@JimvroApplication)
        repository = JimvroRepository(database)
        repository.seedStockTemplates()
        getSharedPreferences("jimvro_settings", MODE_PRIVATE)
            .edit().putInt("stock_templates_version", 2).apply()
    }
}
