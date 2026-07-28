package com.divyateja.jimvro

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.divyateja.jimvro.ui.AppSettings
import java.time.LocalDate
import java.time.ZonedDateTime
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

internal data class ProteinMeal(val key: String, val label: String, val requestCode: Int)

internal val proteinMeals = listOf(
    ProteinMeal("breakfast", "Breakfast", 201),
    ProteinMeal("lunch", "Lunch", 202),
    ProteinMeal("dinner", "Dinner", 203),
)

internal fun nextReminderAt(now: ZonedDateTime, minutesFromMidnight: Int): ZonedDateTime {
    val safeMinutes = minutesFromMidnight.coerceIn(0, 23 * 60 + 59)
    val candidate = now.withHour(safeMinutes / 60).withMinute(safeMinutes % 60).withSecond(0).withNano(0)
    return if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
}

object ProteinReminderScheduler {
    const val ACTION_REMIND = "com.divyateja.jimvro.PROTEIN_REMINDER"
    const val EXTRA_MEAL = "meal"

    fun sync(context: Context, settings: AppSettings) {
        proteinMeals.forEach { meal ->
            if (settings.proteinRemindersEnabled) schedule(context, meal, settings.minutesFor(meal))
            else cancel(context, meal)
        }
    }

    internal fun schedule(context: Context, meal: ProteinMeal, minutesFromMidnight: Int) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAt = nextReminderAt(ZonedDateTime.now(), minutesFromMidnight).toInstant().toEpochMilli()
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            pendingIntent(context, meal),
        )
    }

    private fun cancel(context: Context, meal: ProteinMeal) {
        context.getSystemService(AlarmManager::class.java).cancel(pendingIntent(context, meal))
    }

    private fun pendingIntent(context: Context, meal: ProteinMeal): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            meal.requestCode,
            Intent(context, ProteinReminderReceiver::class.java)
                .setAction(ACTION_REMIND)
                .putExtra(EXTRA_MEAL, meal.key),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}

class ProteinReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            ProteinReminderScheduler.sync(context, context.loadReminderSettings())
            return
        }
        if (intent.action != ProteinReminderScheduler.ACTION_REMIND) return
        val meal = proteinMeals.firstOrNull { it.key == intent.getStringExtra(ProteinReminderScheduler.EXTRA_MEAL) } ?: return
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val settings = context.loadReminderSettings()
            try {
                if (!settings.proteinRemindersEnabled) return@launch
                val app = context.applicationContext as JimvroApplication
                val protein = app.repository.nutritionOn(LocalDate.now().toString()).first().proteinG.roundToInt()
                showNotification(context, meal, protein, settings.proteinTarget)
            } finally {
                if (settings.proteinRemindersEnabled) {
                    ProteinReminderScheduler.schedule(context, meal, settings.minutesFor(meal))
                }
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, meal: ProteinMeal, protein: Int, target: Int) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            manager.createNotificationChannel(NotificationChannel("protein_reminders", "Protein reminders", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val openApp = PendingIntent.getActivity(
            context,
            210,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val progress = if (target > 0) "$protein / $target g logged today" else "$protein g logged today"
        manager.notify(
            meal.requestCode,
            NotificationCompat.Builder(context, "protein_reminders")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("${meal.label} protein check")
                .setContentText("$progress · add a protein source with this meal")
                .setContentIntent(openApp)
                .setAutoCancel(true)
                .build(),
        )
    }
}

internal fun AppSettings.minutesFor(meal: ProteinMeal): Int = when (meal.key) {
    "breakfast" -> breakfastReminderMinutes
    "lunch" -> lunchReminderMinutes
    else -> dinnerReminderMinutes
}

private fun Context.loadReminderSettings(): AppSettings {
    val preferences = getSharedPreferences("jimvro_settings", Context.MODE_PRIVATE)
    return AppSettings(
        proteinTarget = preferences.getInt("protein_target", 150),
        proteinRemindersEnabled = preferences.getBoolean("protein_reminders_enabled", true),
        breakfastReminderMinutes = preferences.getInt("breakfast_reminder_minutes", 8 * 60),
        lunchReminderMinutes = preferences.getInt("lunch_reminder_minutes", 13 * 60),
        dinnerReminderMinutes = preferences.getInt("dinner_reminder_minutes", 20 * 60),
    )
}
