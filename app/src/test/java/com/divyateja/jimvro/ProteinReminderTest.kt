package com.divyateja.jimvro

import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class ProteinReminderTest {
    private val zone = ZoneId.of("Asia/Kolkata")

    @Test fun laterMealUsesToday() {
        val now = ZonedDateTime.of(2026, 7, 27, 7, 30, 0, 0, zone)

        assertEquals(
            ZonedDateTime.of(2026, 7, 27, 8, 0, 0, 0, zone),
            nextReminderAt(now, 8 * 60),
        )
    }

    @Test fun passedMealUsesTomorrow() {
        val now = ZonedDateTime.of(2026, 7, 27, 13, 0, 0, 0, zone)

        assertEquals(
            ZonedDateTime.of(2026, 7, 28, 13, 0, 0, 0, zone),
            nextReminderAt(now, 13 * 60),
        )
    }

    @Test fun invalidTimeIsClamped() {
        val now = ZonedDateTime.of(2026, 7, 27, 20, 0, 0, 0, zone)

        assertEquals(
            ZonedDateTime.of(2026, 7, 27, 23, 59, 0, 0, zone),
            nextReminderAt(now, Int.MAX_VALUE),
        )
    }
}
