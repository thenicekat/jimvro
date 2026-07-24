package app.divyateja.jimvro.domain

data class Macros(
    val calories: Double? = null,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
)

fun scaleMacros(per100g: Macros, servingG: Double): Macros {
    val factor = servingG.coerceAtLeast(0.0) / 100.0
    return Macros(
        calories = per100g.calories?.times(factor),
        proteinG = per100g.proteinG?.times(factor),
        carbsG = per100g.carbsG?.times(factor),
        fatG = per100g.fatG?.times(factor),
    )
}
