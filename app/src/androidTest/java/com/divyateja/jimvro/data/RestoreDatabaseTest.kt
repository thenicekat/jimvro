package com.divyateja.jimvro.data

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
        val exercise = repository.findOrCreateExercise("Backup press")
        val workoutId = repository.createWorkout(
            WorkoutEntity(performedOn = "2026-07-23", name = "Backup workout"),
            listOf(WorkoutSetEntity(workoutId = 0, exerciseId = exercise.id, reps = 8, weightKg = 50.0)),
        )
        repository.addFood(
            FoodEntryEntity(consumedOn = "2026-07-24", name = "Backup meal", calories = 420.0, proteinG = 35.0),
            saveForReuse = true,
        )
        repository.addProgressPhoto(ProgressPhotoEntity(capturedOn = "2026-07-24", uri = "/private/progress.jpg"))
        repository.seedStockTemplates()
        val backup = ByteArrayOutputStream().also { repository.backupDatabase(it) }.toByteArray()
        repository.addMeasurement(MeasurementEntity(measuredOn = "2026-07-25", weightKg = 71.0))

        repository.restoreDatabase(ByteArrayInputStream(backup))

        val reopened = Room.databaseBuilder(context, JimvroDatabase::class.java, name).build()
        assertEquals(listOf("2026-07-24"), reopened.measurementDao().observeAll().first().map { it.measuredOn })
        assertEquals(5, reopened.templateDao().observeTemplates().first().size)
        assertEquals(listOf("Backup workout"), reopened.workoutDao().observeSummaries().first().map { it.name })
        assertEquals(400.0, reopened.workoutDao().observeSummaries().first().single().volumeKg, 0.001)
        assertEquals(1, reopened.workoutDao().observeSetDetails(workoutId).first().size)
        assertEquals(listOf("Backup meal"), reopened.foodDao().observeAll().first().map { it.name })
        assertEquals(listOf("Backup meal"), reopened.foodDao().observeSaved().first().map { it.name })
        assertEquals(listOf("/private/progress.jpg"), reopened.progressPhotoDao().observeAll().first().map { it.uri })
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
