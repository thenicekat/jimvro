package com.divyateja.jimvro

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.divyateja.jimvro.ui.JimvroApp
import com.divyateja.jimvro.ui.AppSettings
import com.divyateja.jimvro.ui.theme.JimvroTheme
import com.divyateja.jimvro.ui.theme.ThemeMode

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as JimvroApplication).repository
        val preferences = getSharedPreferences("jimvro_settings", MODE_PRIVATE)
        val initialSettings = AppSettings(
            weightUnit = preferences.getString("weight_unit", "kg") ?: "kg",
            lengthUnit = preferences.getString("length_unit", "cm") ?: "cm",
            calorieTarget = preferences.getInt("calorie_target", 2_000),
            proteinTarget = preferences.getInt("protein_target", 150),
            restSeconds = preferences.getInt("rest_seconds", 90),
            proteinRemindersEnabled = preferences.getBoolean("protein_reminders_enabled", true),
            breakfastReminderMinutes = preferences.getInt("breakfast_reminder_minutes", 8 * 60),
            lunchReminderMinutes = preferences.getInt("lunch_reminder_minutes", 13 * 60),
            dinnerReminderMinutes = preferences.getInt("dinner_reminder_minutes", 20 * 60),
        )
        ProteinReminderScheduler.sync(this, initialSettings)
        setContent {
            var themeMode by remember {
                mutableStateOf(
                    runCatching { ThemeMode.valueOf(preferences.getString("theme_mode", ThemeMode.SYSTEM.name)!!) }
                        .getOrDefault(ThemeMode.SYSTEM),
                )
            }
            var appSettings by remember { mutableStateOf(initialSettings) }
            JimvroTheme(themeMode) {
                val appViewModel: AppViewModel = viewModel(factory = AppViewModel.Factory(repository))
                JimvroApp(appViewModel, themeMode, { mode ->
                    themeMode = mode
                    preferences.edit().putString("theme_mode", mode.name).apply()
                }, appSettings) { value ->
                    appSettings = value
                    preferences.edit()
                        .putString("weight_unit", value.weightUnit)
                        .putString("length_unit", value.lengthUnit)
                        .putInt("calorie_target", value.calorieTarget)
                        .putInt("protein_target", value.proteinTarget)
                        .putInt("rest_seconds", value.restSeconds)
                        .putBoolean("protein_reminders_enabled", value.proteinRemindersEnabled)
                        .putInt("breakfast_reminder_minutes", value.breakfastReminderMinutes)
                        .putInt("lunch_reminder_minutes", value.lunchReminderMinutes)
                        .putInt("dinner_reminder_minutes", value.dinnerReminderMinutes)
                        .apply()
                    ProteinReminderScheduler.sync(this, value)
                }
            }
        }
    }
}
