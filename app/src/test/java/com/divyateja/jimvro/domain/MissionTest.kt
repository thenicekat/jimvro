package com.divyateja.jimvro.domain

import com.divyateja.jimvro.data.FoodEntryEntity
import com.divyateja.jimvro.data.MeasurementEntity
import com.divyateja.jimvro.data.WorkoutSummary
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class MissionTest {
    @Test
    fun scoresElapsedDaysAndUsesSevenDayWeightAverages() {
        val today = LocalDate.parse("2026-09-09")
        val workouts = listOf("2026-09-07", "2026-09-09").mapIndexed { index, date ->
            WorkoutSummary(index.toLong(), date, null, 3, 1_000.0, 0, 1)
        }
        val foods = listOf("2026-09-07", "2026-09-08").flatMap { date ->
            listOf(FoodEntryEntity(consumedOn = date, name = "Food", calories = 1_800.0, proteinG = 150.0))
        }
        val measurements = listOf(
            MeasurementEntity(measuredOn = "2026-09-08", weightKg = 71.8),
            MeasurementEntity(measuredOn = "2026-09-01", weightKg = 72.1),
        )

        val result = weeklyScore(today, workouts, foods, measurements, 4, 150, 2_000)

        assertEquals(63, result.score)
        assertEquals(2, result.trainingDone)
        assertEquals(2, result.proteinDays)
        assertEquals(-0.3, result.weeklyWeightChangeKg!!, 0.001)
    }
}
