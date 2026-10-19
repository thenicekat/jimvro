package com.divyateja.jimvro.domain

import com.divyateja.jimvro.data.FoodEntryEntity
import com.divyateja.jimvro.data.SavedFoodEntity
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

data class SavedFoodIdea(
    val name: String,
    val portions: Double,
    val calories: Int,
    val proteinG: Int,
)

data class NutritionGuidance(
    val caloriesLeft: Int?,
    val proteinLeftG: Int?,
    val nextMealCalories: Int?,
    val nextMealProteinG: Int?,
    val mealsLeft: Int,
    val hasLoggedFood: Boolean,
    val incompleteLog: Boolean,
    val proteinBehindPace: Boolean,
    val foodIdea: SavedFoodIdea?,
)

/** A guide based on user goals and logged portions, not an estimate of energy needs. */
fun nutritionGuidance(
    entries: List<FoodEntryEntity>,
    savedFoods: List<SavedFoodEntity>,
    calorieTarget: Int,
    proteinTarget: Int,
    mealMinutes: List<Int>,
    nowMinutes: Int,
): NutritionGuidance? {
    if (calorieTarget <= 0 && proteinTarget <= 0) return null

    val times = mealMinutes.map { it.coerceIn(0, 1439) }.sorted()
    val upcoming = times.count { it >= nowMinutes }
    val hasLoggedFood = entries.isNotEmpty()
    val incompleteCalories = calorieTarget > 0 && entries.any { !it.calories.isValidMacro() }
    val incompleteProtein = proteinTarget > 0 && entries.any { !it.proteinG.isValidMacro() }
    val caloriesLogged = entries.sumOf { it.calories?.takeIf { value -> value.isFinite() && value >= 0.0 } ?: 0.0 }
    val proteinLogged = entries.sumOf { it.proteinG?.takeIf { value -> value.isFinite() && value >= 0.0 } ?: 0.0 }
    val caloriesLeft = if (calorieTarget > 0 && !incompleteCalories) (calorieTarget - caloriesLogged).coerceAtLeast(0.0).roundToInt() else null
    val proteinLeft = if (proteinTarget > 0 && !incompleteProtein) (proteinTarget - proteinLogged).coerceAtLeast(0.0).roundToInt() else null

    // Spread what's left over upcoming meals, without prescribing a huge catch-up meal.
    val canSuggestMeal = upcoming > 0 && !incompleteCalories && !incompleteProtein &&
        (calorieTarget <= 0 || (caloriesLeft ?: 0) > 0)
    val splitMeals = if (hasLoggedFood) upcoming else times.size
    val mealCalories = caloriesLeft?.takeIf { it > 0 && canSuggestMeal && times.isNotEmpty() }
        ?.let { min(ceil(it.toDouble() / splitMeals).toInt(), ceil(calorieTarget.toDouble() / times.size * 1.2).toInt()) }
    val mealProtein = proteinLeft?.takeIf { it > 0 && canSuggestMeal && times.isNotEmpty() }
        ?.let { min(ceil(it.toDouble() / splitMeals).toInt(), ceil(proteinTarget.toDouble() / times.size * 1.2).toInt()) }
        // Protein itself supplies ~4 kcal/g; don't propose an impossible pairing.
        ?.let { amount -> min(amount, mealCalories?.div(4) ?: amount).takeIf { it > 0 } }

    val passedIncludingNow = times.count { it <= nowMinutes }
    val tolerance = max(5.0, proteinTarget * 0.05)
    val behindPace = proteinTarget > 0 && mealProtein != null && canSuggestMeal && times.isNotEmpty() &&
        (hasLoggedFood || nowMinutes == times.first()) &&
        proteinLeft > tolerance &&
        proteinLogged + tolerance < proteinTarget.toDouble() * passedIncludingNow / times.size

    val idea = if (canSuggestMeal) {
        matchingSavedFood(savedFoods, mealCalories, mealProtein)
    } else null
    return NutritionGuidance(caloriesLeft, proteinLeft, mealCalories, mealProtein, upcoming, hasLoggedFood,
        incompleteCalories || incompleteProtein, behindPace, idea)
}

private fun Double?.isValidMacro(): Boolean = this != null && isFinite() && this >= 0.0

private fun matchingSavedFood(
    savedFoods: List<SavedFoodEntity>, calories: Int?, protein: Int?,
): SavedFoodIdea? {
    if (calories == null && protein == null) return null
    val portions = listOf(0.5, 1.0, 1.5, 2.0)
    val ideas = savedFoods.flatMap { food ->
        val foodCalories = food.calories?.takeIf { it.isFinite() && it > 0.0 } ?: return@flatMap emptyList()
        val foodProtein = food.proteinG?.takeIf { it.isFinite() && it >= 0.0 } ?: return@flatMap emptyList()
        portions.map { amount ->
            val calorieDifference = calories?.let { abs(foodCalories * amount - it) / max(it, 1) } ?: 0.0
            val proteinDifference = protein?.let { abs(foodProtein * amount - it) / max(it, 1) } ?: 0.0
            val score = (calorieDifference + proteinDifference) / (listOfNotNull(calories, protein).size)
            score to SavedFoodIdea(food.name, amount, (foodCalories * amount).roundToInt(), (foodProtein * amount).roundToInt())
        }.filter { (score, idea) ->
            score <= 0.5 &&
                (protein == null || idea.proteinG >= protein * 0.5) &&
                (calories == null || idea.calories <= calories * 1.35)
        }
    }
    return ideas.minByOrNull { it.first }?.second
}
