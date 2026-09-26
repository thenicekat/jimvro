package com.divyateja.jimvro.data

import org.json.JSONObject

data class FoodPhotoEstimate(
    val name: String,
    val calories: Double? = null,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
)

internal fun parseFoodPhotoEstimate(raw: String): FoodPhotoEstimate {
    val json = JSONObject(stripJsonFence(raw.trim()))
    val name = json.optString("name").trim().ifBlank { "Food from photo" }
    fun number(vararg keys: String): Double? = keys.firstNotNullOfOrNull { key ->
        if (!json.has(key) || json.isNull(key)) null
        else json.optDouble(key, Double.NaN).takeUnless(Double::isNaN)
    }
    return FoodPhotoEstimate(
        name = name,
        calories = number("calories", "kcal", "energyKcal"),
        proteinG = number("proteinG", "protein", "protein_g"),
        carbsG = number("carbsG", "carbs", "carbohydrates", "carbs_g"),
        fatG = number("fatG", "fat", "fat_g"),
    )
}

internal fun stripJsonFence(raw: String): String {
    val fenced = Regex("""^```(?:json)?\s*([\s\S]*?)\s*```$""", RegexOption.IGNORE_CASE)
        .matchEntire(raw)
    return fenced?.groupValues?.get(1)?.trim() ?: raw
}
