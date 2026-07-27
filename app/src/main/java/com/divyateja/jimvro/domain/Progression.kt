package com.divyateja.jimvro.domain

import com.divyateja.jimvro.data.PreviousSet
import kotlin.math.round

data class ProgressionSuggestion(
    val weightKg: Double,
    val increase: Boolean,
)

fun progressionSuggestion(
    previous: List<PreviousSet>,
    targetRepLow: Int?,
    targetRepHigh: Int?,
    incrementKg: Double = 2.5,
): ProgressionSuggestion? {
    val low = targetRepLow ?: return null
    val high = targetRepHigh ?: return null
    if (low <= 0 || high < low || incrementKg <= 0) return null
    val logged = previous.filter { it.reps != null && it.weightKg != null }
    if (logged.isEmpty()) return null
    val workingWeight = logged.maxOf { it.weightKg!! }
    val setsAtWorkingWeight = logged.filter { it.weightKg == workingWeight }
    val reachedTop = setsAtWorkingWeight.all { it.reps!! >= high }
    val suggested = if (reachedTop) workingWeight + incrementKg else workingWeight
    return ProgressionSuggestion(round(suggested / incrementKg) * incrementKg, reachedTop)
}
