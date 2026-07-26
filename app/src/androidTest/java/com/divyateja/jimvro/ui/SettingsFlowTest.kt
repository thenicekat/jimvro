package com.divyateja.jimvro.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.divyateja.jimvro.MainActivity
import org.junit.Rule
import org.junit.Test

class SettingsFlowTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun opensGoalsAndUnitsFromSettingsMenu() {
        rule.onNodeWithContentDescription("Settings").performClick()
        rule.onNodeWithText("Goals & units").performClick()
        rule.onNodeWithText("Save settings").assertIsDisplayed()
    }
}
