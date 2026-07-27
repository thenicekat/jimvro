package com.divyateja.jimvro.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.divyateja.jimvro.MainActivity
import org.junit.Rule
import org.junit.Test

class WorkoutCalendarFlowTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun calendarOpensFromWorkoutLibraryAndReturns() {
        composeRule.onNodeWithText("Workouts").performClick()
        composeRule.onNodeWithText("Calendar").performClick()
        composeRule.onNodeWithText("TRAINING HISTORY").assertIsDisplayed()
        composeRule.onNodeWithText("Workouts").performClick()
        composeRule.onNodeWithText("RECENT SESSIONS").assertIsDisplayed()
    }
}
