package app.divyateja.jimvro.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RestoreDatabaseTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun restoreReplacesDatabaseAndCanBeReopened() {
        runBlocking {
        val name = "restore-valid.db"
        context.deleteDatabase(name)
        val database = Room.databaseBuilder(context, JimvroDatabase::class.java, name).build()
        val repository = JimvroRepository(database)
        repository.addMeasurement(MeasurementEntity(measuredOn = "2026-07-24", weightKg = 70.0))
        repository.seedStockTemplates()
        val backup = ByteArrayOutputStream().also { repository.backupDatabase(it) }.toByteArray()
        repository.addMeasurement(MeasurementEntity(measuredOn = "2026-07-25", weightKg = 71.0))

        repository.restoreDatabase(ByteArrayInputStream(backup))

        val reopened = Room.databaseBuilder(context, JimvroDatabase::class.java, name).build()
        assertEquals(listOf("2026-07-24"), reopened.measurementDao().observeAll().first().map { it.measuredOn })
        assertEquals(5, reopened.templateDao().observeTemplates().first().size)
        reopened.close()
        context.deleteDatabase(name)
        }
    }

    @Test
    fun invalidBackupDoesNotReplaceOpenDatabase() {
        runBlocking {
        val name = "restore-invalid.db"
        context.deleteDatabase(name)
        val database = Room.databaseBuilder(context, JimvroDatabase::class.java, name).build()
        val repository = JimvroRepository(database)
        repository.addMeasurement(MeasurementEntity(measuredOn = "2026-07-25", weightKg = 71.0))

        val failure = runCatching {
            repository.restoreDatabase(ByteArrayInputStream("not a database".encodeToByteArray()))
        }.exceptionOrNull()
        assertTrue(failure is IllegalArgumentException)
        assertEquals(1, database.measurementDao().observeAll().first().size)
        database.close()
        context.deleteDatabase(name)
        }
    }
}
