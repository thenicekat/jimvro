package com.divyateja.jimvro.data

internal val MUSCLE_GROUPS = listOf(
    "chest", "back", "shoulders", "biceps", "triceps", "forearms",
    "quads", "hamstrings", "glutes", "calves", "adductors", "abductors",
    "abs", "cardio", "other",
)
internal val LEGACY_MUSCLE_GROUPS = setOf("arms", "legs", "core")

internal fun canonicalMuscleGroup(target: String?): String = when (target) {
    "pectorals" -> "chest"
    "lats", "traps", "upper back", "levator scapulae", "spine" -> "back"
    "delts" -> "shoulders"
    "biceps", "triceps", "forearms", "quads", "hamstrings", "glutes", "calves", "adductors", "abductors", "abs" -> target
    "cardiovascular system" -> "cardio"
    else -> "other"
}

internal val stockExerciseGroups = mapOf(
    "Bench Press (Smith/Barbell)" to "chest", "Iso-Lateral Row" to "back", "Overhead Press" to "shoulders",
    "Lat Pulldown" to "back", "Preacher Curl" to "biceps", "Rope Pushdown" to "triceps", "Machine Lateral Raise" to "shoulders",
    "Leg Press" to "quads", "Romanian Deadlift" to "hamstrings", "Assisted Pull-ups" to "back", "Seated Leg Curl" to "hamstrings",
    "Seated Calf Raise" to "calves", "Hanging Leg Raise" to "abs", "Cable Crunch" to "abs", "Incline Smith Press" to "chest",
    "Seated Cable Row (V-Grip)" to "back", "Chest Fly (Machine/Cable)" to "chest", "Rear Delt Fly" to "shoulders",
    "Cross-Body Hammer Curl" to "biceps", "Overhead Rope Triceps Extension" to "triceps", "Front Squat" to "quads",
    "Bulgarian Split Squat" to "quads", "Leg Curl" to "hamstrings", "Leg Extension" to "quads", "Decline Sit-ups" to "abs", "Ab Wheel Rollouts" to "abs",
)
