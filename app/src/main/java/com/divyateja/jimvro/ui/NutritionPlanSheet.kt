package com.divyateja.jimvro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.divyateja.jimvro.data.MeasurementEntity
import com.divyateja.jimvro.domain.ActivityLevel
import com.divyateja.jimvro.domain.EstimateSex
import com.divyateja.jimvro.domain.MaintenanceMethod
import com.divyateja.jimvro.domain.NutritionGoal
import com.divyateja.jimvro.domain.NutritionPlanInput
import com.divyateja.jimvro.domain.estimateNutritionPlan
import java.time.LocalDate
import java.util.Locale

@Composable
internal fun NutritionPlanSheet(
    settings: AppSettings,
    measurements: List<MeasurementEntity>,
    onDismiss: () -> Unit,
    onSave: (AppSettings) -> Unit,
) {
    val today = LocalDate.now()
    val latestWeight = measurements.filter { it.measuredOn <= today.toString() && it.weightKg?.isFinite() == true && it.weightKg > 0 }
        .maxByOrNull { it.measuredOn }
    val recentBmr = measurements.filter {
        it.measuredOn in today.minusDays(90).toString()..today.toString() &&
            it.basalMetabolicRateKcal?.isFinite() == true && it.basalMetabolicRateKcal > 0
    }.maxByOrNull { it.measuredOn }?.basalMetabolicRateKcal
    var step by rememberSaveable { mutableIntStateOf(0) }
    var goalName by rememberSaveable { mutableStateOf(settings.nutritionGoal.uppercase(Locale.ROOT)) }
    var weight by rememberSaveable {
        mutableStateOf((settings.nutritionPlanWeightKg ?: latestWeight?.weightKg)?.displayWeight(settings)?.let { "%.1f".format(Locale.US, it) }.orEmpty())
    }
    var height by rememberSaveable { mutableStateOf("") }
    var methodName by rememberSaveable { mutableStateOf(if (recentBmr != null) MaintenanceMethod.BODY_SCAN.name else MaintenanceMethod.PROFILE.name) }
    var activityName by rememberSaveable { mutableStateOf(ActivityLevel.LIGHT.name) }
    var age by rememberSaveable { mutableStateOf("") }
    var sexName by rememberSaveable { mutableStateOf("") }
    var maintenance by rememberSaveable { mutableStateOf("") }
    var calories by rememberSaveable { mutableStateOf("") }
    var protein by rememberSaveable { mutableStateOf("") }
    var remindersEnabled by rememberSaveable { mutableStateOf(settings.proteinRemindersEnabled) }

    val goal = NutritionGoal.entries.firstOrNull { it.name == goalName }
    val method = MaintenanceMethod.entries.firstOrNull { it.name == methodName } ?: MaintenanceMethod.PROFILE
    val activity = ActivityLevel.entries.firstOrNull { it.name == activityName } ?: ActivityLevel.LIGHT
    val weightKg = weight.toDoubleOrNull()?.storageWeight(settings)
    val estimate = if (goal != null && weightKg != null) estimateNutritionPlan(
        NutritionPlanInput(
            goal = goal,
            weightKg = weightKg,
            heightCm = height.toDoubleOrNull()?.storageLength(settings) ?: Double.NaN,
            method = method,
            activity = activity,
            age = age.toIntOrNull(),
            estimateSex = EstimateSex.entries.firstOrNull { it.name == sexName },
            measuredBmrKcal = recentBmr,
            knownMaintenanceKcal = maintenance.toDoubleOrNull(),
        ),
    ) else null
    val enteredTargets = (calories.toIntOrNull() ?: 0) in 1_200..5_500 && (protein.toIntOrNull() ?: 0) in 20..300

    FormSheet(
        title = when (step) { 0 -> "Your food plan"; 1 -> "Build your estimate"; else -> "Review your plan" },
        description = when (step) {
            0 -> "Choose a direction. You'll see and edit the numbers before saving."
            1 -> "A few details help estimate a starting point. Nothing is sent to a server."
            else -> "These are starting targets, not a medical prescription. Adjust them to fit your plan."
        },
        primaryLabel = when (step) { 0 -> "Continue"; 1 -> "See starting targets"; else -> "Use this plan" },
        primaryEnabled = when (step) { 0 -> goal != null; 1 -> estimate != null; else -> enteredTargets },
        onPrimary = {
            when (step) {
                0 -> step = 1
                1 -> if (estimate != null) {
                    calories = estimate.calorieTarget.toString()
                    protein = estimate.proteinTargetG.toString()
                    step = 2
                }
                else -> if (goal != null && weightKg != null && enteredTargets) onSave(
                    settings.copy(
                        calorieTarget = calories.toInt(),
                        proteinTarget = protein.toInt(),
                        nutritionGoalsConfirmed = true,
                        nutritionGoal = goal.name.lowercase(Locale.ROOT),
                        nutritionPlanWeightKg = weightKg,
                        proteinRemindersEnabled = remindersEnabled,
                    ),
                )
            }
        },
        onDismiss = onDismiss,
        dismissLabel = "Not now",
        scrollKey = step,
    ) {
        if (step > 0) TextButton(onClick = { step -= 1 }) { Text("Back") }
        when (step) {
            0 -> {
                listOf(NutritionGoal.RECOMP, NutritionGoal.CUT, NutritionGoal.BULK).forEach { choice ->
                    PlanChoiceRow(choice.label, choice.description, goal == choice) { goalName = choice.name }
                }
                Text(
                    "Food guidance uses your goal and what you log, then adjusts meal suggestions through the day.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            1 -> {
                Text("Your starting details", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AppField(weight, { weight = it }, "Weight (${settings.weightUnit})", Modifier.weight(1f))
                    AppField(height, { height = it }, "Height (${settings.lengthUnit})", Modifier.weight(1f))
                }
                Text("How should we estimate maintenance?", style = MaterialTheme.typography.titleSmall)
                if (recentBmr != null) PlanChoiceRow(
                    "Use body scan", "Recent BMR: ${recentBmr.toInt()} kcal/day", method == MaintenanceMethod.BODY_SCAN,
                ) { methodName = MaintenanceMethod.BODY_SCAN.name }
                PlanChoiceRow("Estimate from profile", "Age, height, and activity", method == MaintenanceMethod.PROFILE) {
                    methodName = MaintenanceMethod.PROFILE.name
                }
                PlanChoiceRow("I know my maintenance", "Enter your usual maintenance calories", method == MaintenanceMethod.KNOWN) {
                    methodName = MaintenanceMethod.KNOWN.name
                }
                when (method) {
                    MaintenanceMethod.KNOWN -> AppField(maintenance, { maintenance = it.filter(Char::isDigit) }, "Maintenance (kcal/day)")
                    MaintenanceMethod.PROFILE -> {
                        AppField(age, { age = it.filter(Char::isDigit) }, "Age (18 or older)")
                        Text("Choose the equation used for this estimate", style = MaterialTheme.typography.titleSmall)
                        EstimateSex.entries.forEach { choice ->
                            PlanChoiceRow(choice.label, "Used only for calculation; not saved", sexName == choice.name) {
                                sexName = choice.name
                            }
                        }
                    }
                    MaintenanceMethod.BODY_SCAN -> Unit
                }
                if (method != MaintenanceMethod.KNOWN) {
                    Text("Typical activity", style = MaterialTheme.typography.titleSmall)
                    ActivityLevel.entries.forEach { choice ->
                        PlanChoiceRow(choice.label, "", activity == choice) { activityName = choice.name }
                    }
                }
                Text(
                    if (estimate == null) "Enter valid adult details to see a starting estimate. For medical diets or unsupported values, set goals manually in Settings."
                    else "Estimate ready. Review the targets before using them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                if (estimate != null && goal != null) {
                    Text("${goal.label} starting point", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Estimated maintenance: about ${estimate.maintenanceKcal} kcal/day. " +
                            when (goal) {
                                NutritionGoal.CUT -> "A modest deficit supports a gradual cut."
                                NutritionGoal.RECOMP -> "Start near maintenance while training."
                                NutritionGoal.BULK -> "A modest surplus supports a gradual bulk."
                            },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AppField(calories, { calories = it.filter(Char::isDigit) }, "Calories/day", Modifier.weight(1f))
                        AppField(protein, { protein = it.filter(Char::isDigit) }, "Protein (g/day)", Modifier.weight(1f))
                    }
                    Text(
                        "Based on your inputs; check weight and training trends over the next few weeks. Only goal, weight, and chosen targets are saved locally.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Meal-time protein nudges", style = MaterialTheme.typography.titleSmall)
                            Text(
                                "Only when your logged protein is behind pace. Set times in Settings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(checked = remindersEnabled, onCheckedChange = { remindersEnabled = it })
                    }
                }
            }
        }
    }
}

@Composable
private fun PlanChoiceRow(title: String, subtitle: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier.fillMaxWidth().clip(shape)
            .background(if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (selected) {
            Spacer(Modifier.size(8.dp))
            Icon(Icons.Outlined.Check, contentDescription = null, modifier = Modifier.size(20.dp))
        }
    }
}
