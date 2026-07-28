package com.divyateja.jimvro.ui

data class AppSettings(
    val weightUnit: String = "kg",
    val lengthUnit: String = "cm",
    val calorieTarget: Int = 2_000,
    val proteinTarget: Int = 150,
    val restSeconds: Int = 90,
    val proteinRemindersEnabled: Boolean = true,
    val breakfastReminderMinutes: Int = 8 * 60,
    val lunchReminderMinutes: Int = 13 * 60,
    val dinnerReminderMinutes: Int = 20 * 60,
)

internal fun Double.displayWeight(settings: AppSettings): Double =
    if (settings.weightUnit == "lb") this * 2.2046226218 else this

internal fun Double.storageWeight(settings: AppSettings): Double =
    if (settings.weightUnit == "lb") this / 2.2046226218 else this

internal fun Double.displayLength(settings: AppSettings): Double =
    if (settings.lengthUnit == "in") this / 2.54 else this

internal fun Double.storageLength(settings: AppSettings): Double =
    if (settings.lengthUnit == "in") this * 2.54 else this
