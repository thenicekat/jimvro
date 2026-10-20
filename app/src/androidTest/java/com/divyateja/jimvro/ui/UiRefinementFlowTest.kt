package com.divyateja.jimvro.ui

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.room.Room
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import com.divyateja.jimvro.AppViewModel
import com.divyateja.jimvro.data.FoodEntryEntity
import com.divyateja.jimvro.data.JimvroDatabase
import com.divyateja.jimvro.data.JimvroRepository
import com.divyateja.jimvro.data.WorkoutEntity
import com.divyateja.jimvro.data.WorkoutSetEntity
import com.divyateja.jimvro.ui.theme.JimvroTheme
import com.divyateja.jimvro.ui.theme.AccentColor
import com.divyateja.jimvro.ui.theme.accentPalette
import com.divyateja.jimvro.ui.theme.ThemeMode
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Isolated databases: these regressions never add or delete the user's diary data. */
class UiRefinementFlowTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var database: JimvroDatabase
    private lateinit var repository: JimvroRepository
    private val store = ViewModelStore()
    private val date = LocalDate.now().toString()
    private var displayedAccent: Color? = null

    @Before fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, JimvroDatabase::class.java).build()
        repository = JimvroRepository(database)
    }

    @After fun cleanup() {
        rule.runOnIdle { store.clear() }
        database.close()
    }

    private fun launch(theme: ThemeMode = ThemeMode.DARK) {
        val viewModel = ViewModelProvider(store, AppViewModel.Factory(repository))[AppViewModel::class.java]
        val workoutCount = runBlocking { repository.workouts.first().size }
        val foodCount = runBlocking { repository.foodEntries.first().size }
        rule.setContent {
            var settings by remember {
                mutableStateOf(AppSettings(
                    nutritionGoalsConfirmed = true,
                    proteinRemindersEnabled = false,
                    bodyRemindersEnabled = false,
                ))
            }
            var currentTheme by remember { mutableStateOf(theme) }
            JimvroTheme(currentTheme, settings.accentColor) {
                val accent = MaterialTheme.colorScheme.secondary
                SideEffect { displayedAccent = accent }
                JimvroApp(viewModel, currentTheme, { currentTheme = it }, settings, { settings = it })
            }
        }
        rule.waitUntil(5_000) { viewModel.workouts.value.size == workoutCount && viewModel.foodEntries.value.size == foodCount }
    }

    private fun screenshot(name: String) {
        Espresso.closeSoftKeyboard()
        rule.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        // Compose idle doesn't await Android window transitions or the compositor's last frame.
        instrumentation.waitForIdleSync()
        SystemClock.sleep(500)
        val config = instrumentation.targetContext.resources.configuration
        val directory = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")?.let(::File)
            ?: instrumentation.targetContext.getExternalFilesDir("design-qa")
        val output = File(directory, "$name-font${config.fontScale}-orientation${config.orientation}.png")
        output.parentFile?.mkdirs()
        instrumentation.uiAutomation.takeScreenshot()?.let { bitmap ->
            output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            bitmap.recycle()
        }
    }

    @Test fun darkAccentsUpdateGloballyAndFoodControlsAreIconOnly() = checkAppearance(ThemeMode.DARK, AccentColor.BLUE)

    @Test fun lightAccentsUpdateGloballyAndFoodControlsAreIconOnly() = checkAppearance(ThemeMode.LIGHT, AccentColor.ROSE)

    private fun checkAppearance(theme: ThemeMode, chosen: AccentColor) {
        launch(theme)
        rule.onAllNodesWithText("Home").assertCountEquals(1)
        rule.onNodeWithText("Today").assertDoesNotExist()
        val locale = InstrumentationRegistry.getInstrumentation().targetContext.resources.configuration.locales[0]
        val oldDateHeading = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)).uppercase(locale)
        rule.onNodeWithText(oldDateHeading).assertDoesNotExist()
        screenshot("home-${theme.name.lowercase()}")
        rule.onNodeWithContentDescription("Settings").performClick()
        rule.onNodeWithContentDescription("Olive accent").assertIsSelected()
        AccentColor.entries.forEach { accent ->
            rule.onNodeWithContentDescription("${accent.label} accent").performClick().assertIsSelected()
            rule.runOnIdle { assertEquals(accentPalette(accent, theme == ThemeMode.DARK).color, displayedAccent) }
        }
        rule.onNodeWithContentDescription("${chosen.label} accent").performClick().assertIsSelected()
        rule.onNodeWithContentDescription("Olive accent").assertIsNotSelected()
        screenshot("settings-${chosen.name.lowercase()}")
        rule.onNodeWithText("Back").performClick()
        rule.onNodeWithText("Food").performClick()
        rule.onAllNodesWithText("Food").assertCountEquals(1)
        listOf("Barcode", "Camera", "Gallery").forEach { name ->
            rule.onNodeWithText(name).assertDoesNotExist()
            rule.onNodeWithContentDescription(name).assertIsDisplayed().assertHasClickAction()
        }
        screenshot("food-${chosen.name.lowercase()}")
        rule.onNodeWithContentDescription("Camera").performClick()
        rule.onNodeWithText("Set up photo estimates").assertIsDisplayed()
        rule.onNodeWithContentDescription("Settings").performClick()
        rule.onNodeWithContentDescription("${chosen.label} accent").assertIsSelected()
        rule.onNodeWithText("Back").performClick()
        rule.onNodeWithText("Home").performClick()
        rule.onNodeWithText("Start workout").assertIsDisplayed()
    }

    @Test fun homeOffersStartBeforeMetricsAndDoesNotScoreUnconfiguredMission() {
        launch()
        rule.onNodeWithText("Start workout").assertIsDisplayed()
        rule.onNodeWithText("0/100").assertDoesNotExist()
        rule.onNodeWithText("Start workout").performClick()
        rule.onNodeWithText("Session name (optional)").assertIsDisplayed()
        screenshot("new-workout-dark")
    }

    @Test fun foodValidatesEnteredNumbersButAllowsUnknownAndZero() {
        launch()
        rule.onNodeWithText("Food").performClick()
        rule.onNodeWithText("Log food").performClick()
        rule.onNode(hasText("Food") and hasSetTextAction()).performTextInput("QA food")
        rule.onNodeWithText("Calories").performTextInput("oops")
        rule.onNodeWithText("Enter a valid number").assertIsDisplayed()
        rule.onNodeWithText("Add food").assertIsNotEnabled()
        screenshot("food-validation-dark")
        rule.onNodeWithText("Calories").performTextReplacement("0")
        rule.onNodeWithText("Add food").assertIsEnabled().performClick()
        rule.waitUntil(5_000) { runBlocking { repository.foodEntries.first().isNotEmpty() } }
        val saved = runBlocking { repository.foodEntries.first().single() }
        assertEquals(0.0, saved.calories!!, 0.001)
        assertNull(saved.proteinG)
        rule.onNodeWithText("P — g  ·  C — g  ·  F — g").performScrollTo().assertIsDisplayed()
    }

    @Test fun startCreatesOneSessionAndOpensTrackerImmediately() {
        launch()
        rule.onNodeWithText("Start workout").performClick()
        val actions = rule.onAllNodesWithText("Start workout")
        actions[actions.fetchSemanticsNodes().lastIndex].performClick()
        rule.waitUntil(5_000) { runBlocking { repository.workouts.first().size == 1 } }
        rule.onNodeWithText("New session").assertDoesNotExist()
        rule.onNodeWithText("Finish workout").assertIsNotEnabled()
    }

    @Test fun measurementRejectsInvalidNumbersAndAcceptsDecimalComma() {
        launch()
        rule.onNodeWithText("Measurement").performClick()
        rule.onNodeWithText("Weight (kg)").performTextInput("abc")
        rule.onNodeWithText("Enter a valid number").assertIsDisplayed()
        rule.onNodeWithText("Add measurement").assertIsNotEnabled()
        rule.onNodeWithText("Weight (kg)").performTextReplacement("72,5")
        rule.onNodeWithText("Add measurement").assertIsEnabled().performClick()
        rule.waitUntil(5_000) { runBlocking { repository.measurements.first().size == 1 } }
        assertEquals(72.5, runBlocking { repository.measurements.first().single().weightKg }!!, 0.001)
    }

    @Test fun quickAddUndoRemovesOnlyInsertedEntryAndKeepsSavedFood() {
        runBlocking { repository.addFood(FoodEntryEntity(consumedOn = LocalDate.now().minusDays(1).toString(), name = "QA oats", calories = 100.0, proteinG = 10.0), saveForReuse = true) }
        launch()
        rule.onNodeWithText("Food").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithContentDescription("Add QA oats on ${formatDateForDisplay(date)}").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription("Add QA oats on ${formatDateForDisplay(date)}").performClick()
        rule.onNodeWithText("Added QA oats to Today").assertIsDisplayed()
        screenshot("quick-add-undo-dark")
        rule.onNodeWithText("Undo").performClick()
        rule.waitUntil(5_000) { runBlocking { repository.foodEntries.first().size == 1 } }
        assertEquals(1, runBlocking { repository.savedFoods.first().size })
        assertEquals(LocalDate.now().minusDays(1).toString(), runBlocking { repository.foodEntries.first().single().consumedOn })
    }

    @Test fun incompleteSavedFoodOpensReviewInsteadOfSilentlyAdding() {
        runBlocking { repository.addFood(FoodEntryEntity(consumedOn = date, name = "QA unknown"), saveForReuse = true) }
        launch()
        rule.onNodeWithText("Food").performClick()
        rule.waitUntil(5_000) { rule.onAllNodesWithContentDescription("Add QA unknown on ${formatDateForDisplay(date)}").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription("Add QA unknown on ${formatDateForDisplay(date)}").performClick()
        rule.onNodeWithText("Review saved food").assertIsDisplayed()
        assertEquals(1, runBlocking { repository.foodEntries.first().size })
    }

    private fun workout(complete: Boolean): Long = runBlocking {
        val exercise = repository.findOrCreateExercise("QA press")
        repository.createWorkout(WorkoutEntity(performedOn = date, name = "QA workout"), listOf(
            WorkoutSetEntity(workoutId = 0, exerciseId = exercise.id, setNumber = 1, reps = 8, weightKg = 50.0, isCompleted = true),
            WorkoutSetEntity(workoutId = 0, exerciseId = exercise.id, setNumber = 2, reps = 8, weightKg = 50.0, isCompleted = complete),
        ))
    }

    @Test fun finishWithUnloggedSetsCanBeCancelledAndSummaryDoesNotPromiseResume() {
        val id = workout(complete = false)
        launch()
        rule.onNodeWithText("Resume workout").performClick()
        rule.onNodeWithText("Finish workout").performClick()
        rule.onNodeWithText("Finish with 1 set unlogged?").assertIsDisplayed()
        screenshot("finish-confirm-dark")
        rule.onNodeWithText("Keep training").performClick()
        assertNull(runBlocking { repository.workout(id).first() }?.finishedAt)
        rule.onNodeWithText("Finish workout").performClick()
        rule.onNode(hasText("Finish workout") and hasAnyAncestor(isDialog())).performClick()
        rule.onNodeWithText("Workout complete").assertIsDisplayed()
        rule.onNodeWithText("View workout").performClick()
        rule.waitUntil(5_000) { runBlocking { repository.workout(id).first()?.finishedAt != null } }
        rule.onNodeWithText("Finish workout").assertDoesNotExist()
    }

    @Test fun fullyLoggedWorkoutFinishesWithoutExtraConfirmation() {
        val id = workout(complete = true)
        launch()
        rule.onNodeWithText("Resume workout").performClick()
        rule.onNodeWithText("Finish workout").performClick()
        rule.onNodeWithText("Workout complete").assertIsDisplayed()
        rule.onNodeWithText("Keep training").assertDoesNotExist()
        rule.waitUntil(5_000) { runBlocking { repository.workout(id).first()?.finishedAt != null } }
        assertNotNull(runBlocking { repository.workout(id).first() }?.finishedAt)
    }

    @Test fun calendarExposesSelectedDateAndUsesReadableDarkThemePair() {
        launch()
        rule.onNodeWithText("Workouts").performClick()
        rule.onNodeWithText("Calendar").performScrollTo().performClick()
        rule.onNodeWithContentDescription("${formatDateForDisplay(date)}, 0 sessions logged").assertIsSelected()
        screenshot("calendar-dark")
    }

    @Test fun lightThemeKeepsPrimaryActionAndCalendarLegible() {
        launch(ThemeMode.LIGHT)
        rule.onNodeWithText("Start workout").assertIsDisplayed()
        screenshot("today-light")
        rule.onNodeWithText("Workouts").performClick()
        rule.onNodeWithText("Calendar").performScrollTo().performClick()
        rule.onNodeWithContentDescription("${formatDateForDisplay(date)}, 0 sessions logged").assertIsSelected()
        screenshot("calendar-light")
    }
}
