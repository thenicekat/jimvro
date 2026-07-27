package com.divyateja.jimvro.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryBehaviorTest {
    @Test
    fun backdatedWorkoutIsStoredOnSelectedDay() = withRepository { repository ->
        val workoutId = repository.addWorkout(WorkoutEntity(performedOn = "2026-07-20", name = "Backdated"))

        assertEquals(1, repository.workoutsOn("2026-07-20").first())
        assertEquals(0, repository.workoutsOn("2026-07-27").first())
        assertEquals("2026-07-20", repository.workout(workoutId).first()?.performedOn)
    }

    @Test
    fun workoutVolumeAndCascadeDeleteStayConsistent() = withRepository { repository ->
        val exercise = repository.findOrCreateExercise("Test press")
        val workoutId = repository.createWorkout(
            WorkoutEntity(performedOn = "2026-07-27", name = "Volume test"),
            listOf(
                WorkoutSetEntity(workoutId = 0, exerciseId = exercise.id, setNumber = 1, reps = 8, weightKg = 60.0),
                WorkoutSetEntity(workoutId = 0, exerciseId = exercise.id, setNumber = 2, reps = 10, weightKg = 60.0),
            ),
        )

        val summary = repository.workouts.first().single()
        assertEquals(2, summary.setCount)
        assertEquals(1_080.0, summary.volumeKg, 0.001)

        repository.deleteWorkout(workoutId)
        assertEquals(emptyList<WorkoutSetDetail>(), repository.workoutSets(workoutId).first())
        assertNull(repository.workout(workoutId).first())
    }

    @Test
    fun startingTemplatePreservesDateTargetsAndSetMetadata() = withRepository { repository ->
        val press = repository.findOrCreateExercise("Template press")
        val row = repository.findOrCreateExercise("Template row")
        val templateId = repository.createTemplate(
            "Test template",
            listOf(
                TemplateTarget(press.id, targetSets = 3, repLow = 5, repHigh = 8, setType = "working", supersetGroup = 4),
                TemplateTarget(row.id, targetSets = 2, repLow = 10, repHigh = 12, setType = "warmup", supersetGroup = 4),
            ),
        )

        val workoutId = repository.startFromTemplate(templateId, "2026-07-19")
        val workout = repository.workout(workoutId).first()
        val sets = repository.workoutSets(workoutId).first()

        assertEquals("2026-07-19", workout?.performedOn)
        assertEquals("Test template", workout?.name)
        assertEquals(listOf(1, 2, 3), sets.filter { it.exerciseId == press.id }.map { it.setNumber })
        assertEquals(listOf("warmup", "warmup"), sets.filter { it.exerciseId == row.id }.map { it.setType })
        assertEquals(setOf(4), sets.mapNotNull { it.supersetGroup }.toSet())
        assertEquals(setOf(5), sets.filter { it.exerciseId == press.id }.mapNotNull { it.targetRepLow }.toSet())
        assertEquals(setOf(12), sets.filter { it.exerciseId == row.id }.mapNotNull { it.targetRepHigh }.toSet())
    }

    @Test
    fun savedFoodAndDailyNutritionUseLoggedMacros() = withRepository { repository ->
        repository.addFood(
            FoodEntryEntity(
                consumedOn = "2026-07-27",
                meal = "lunch",
                name = "Chicken",
                calories = 200.0,
                proteinG = 40.0,
                carbsG = 2.0,
                fatG = 5.0,
            ),
            saveForReuse = true,
        )
        repository.addFood(
            FoodEntryEntity(
                consumedOn = "2026-07-27",
                meal = "lunch",
                name = "Rice",
                calories = 300.0,
                proteinG = 6.0,
                carbsG = 65.0,
                fatG = 1.0,
            ),
        )

        val nutrition = repository.nutritionOn("2026-07-27").first()
        assertEquals(500.0, nutrition.calories, 0.001)
        assertEquals(46.0, nutrition.proteinG, 0.001)
        assertEquals(67.0, nutrition.carbsG, 0.001)
        assertEquals(6.0, nutrition.fatG, 0.001)
        assertEquals(listOf("Chicken"), repository.savedFoods.first().map { it.name })
    }

    @Test
    fun previousSetsIgnoreWorkoutsAfterCurrentDate() = withRepository { repository ->
        val exercise = repository.findOrCreateExercise("Previous-set lift")
        val olderId = repository.createWorkout(
            WorkoutEntity(performedOn = "2026-07-18"),
            listOf(WorkoutSetEntity(workoutId = 0, exerciseId = exercise.id, reps = 8, weightKg = 50.0)),
        )
        repository.createWorkout(
            WorkoutEntity(performedOn = "2026-07-22"),
            listOf(WorkoutSetEntity(workoutId = 0, exerciseId = exercise.id, reps = 5, weightKg = 70.0)),
        )
        val currentId = repository.addWorkout(WorkoutEntity(performedOn = "2026-07-20"))

        val previous = repository.previousSets(currentId, exercise.id).first()
        assertEquals(olderId, repository.workouts.first().first { it.performedOn == "2026-07-18" }.id)
        assertEquals(listOf("2026-07-18"), previous.map { it.performedOn }.distinct())
        assertEquals(50.0, previous.single().weightKg!!, 0.001)
    }

    @Test
    fun personalRecordRequiresBeatingPriorWorkingWeight() = withRepository { repository ->
        val exercise = repository.findOrCreateExercise("PR lift")
        val workoutId = repository.createWorkout(
            WorkoutEntity(performedOn = "2026-07-27"),
            listOf(
                WorkoutSetEntity(workoutId = 0, exerciseId = exercise.id, setNumber = 1, reps = 8, weightKg = 60.0),
                WorkoutSetEntity(workoutId = 0, exerciseId = exercise.id, setNumber = 2, reps = 6, weightKg = 62.5),
            ),
        )
        val sets = repository.workoutSets(workoutId).first()

        assertFalse(repository.isWeightPersonalRecord(exercise.id, sets[1].id, 60.0))
        assertTrue(repository.isWeightPersonalRecord(exercise.id, sets[1].id, 62.5))
    }

    private fun withRepository(block: suspend (JimvroRepository) -> Unit) = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, JimvroDatabase::class.java).build()
        try {
            block(JimvroRepository(database))
        } finally {
            database.close()
        }
    }
}
