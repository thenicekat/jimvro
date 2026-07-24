package app.jimvro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jimvro.AppViewModel
import app.jimvro.data.ExerciseEntity
import app.jimvro.data.TemplateSummary
import app.jimvro.data.TemplateTarget
import app.jimvro.ui.theme.Clay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
fun TemplatesScreen(viewModel: AppViewModel, onBack: () -> Unit, onWorkoutStarted: (Long) -> Unit) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    var createOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Page(
        "Training plans",
        "Templates",
        "Reusable sessions for the workouts you repeat.",
        action = { LibraryAddButton("New") { createOpen = true } },
    ) {
        BackLink(onBack)
        if (templates.isEmpty()) {
            Text("No templates yet. Create one for your regular training split.", color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 21.sp)
        }
        templates.forEach { template -> TemplateCard(template, exercises, viewModel, onWorkoutStarted) }
    }
    if (createOpen) CreateTemplateSheet(
        exercises = exercises,
        onDismiss = { createOpen = false },
        onCreate = { name, targets ->
            scope.launch {
                viewModel.createTemplate(name, targets)
                createOpen = false
            }
        },
    )
}

@Composable
private fun TemplateCard(
    template: TemplateSummary,
    exercises: List<ExerciseEntity>,
    viewModel: AppViewModel,
    onWorkoutStarted: (Long) -> Unit,
) {
    val lines by viewModel.templateLines(template.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    var addLine by remember { mutableStateOf(false) }
    JournalCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(template.name, fontSize = 17.sp, fontWeight = FontWeight.Medium)
                Text("${lines.size} exercises", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { viewModel.deleteTemplate(template.id) }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Outlined.Delete, "Delete template", Modifier.size(18.dp))
            }
        }
        lines.forEach { line ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(line.exerciseName, fontSize = 14.sp)
                    Text(
                        "${line.targetSets} sets${if (line.repLow != null) " · ${line.repLow}${line.repHigh?.let { "–$it" } ?: ""} reps" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { viewModel.deleteTemplateLine(line.id) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Delete, "Remove exercise", Modifier.size(16.dp))
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = { addLine = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.Add, null, Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("Exercise")
            }
            Button(
                onClick = {
                    scope.launch {
                        val id = viewModel.startFromTemplate(template.id, SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
                        onWorkoutStarted(id)
                    }
                },
                enabled = lines.isNotEmpty(),
                modifier = Modifier.weight(1f).height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface),
            ) { Icon(Icons.Outlined.PlayArrow, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("Start") }
        }
    }
    if (addLine) AddTemplateLineSheet(exercises, onDismiss = { addLine = false }) { exerciseId, sets, low, high ->
        viewModel.addTemplateLine(template.id, exerciseId, sets, low, high)
        addLine = false
    }
}

@Composable
fun RecordsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    val records by viewModel.personalRecords.collectAsStateWithLifecycle()
    Page("Training", "Personal records", "Your heaviest logged set for each exercise.") {
        BackLink(onBack)
        if (records.isEmpty()) Text("No records yet. Log weighted sets to build this list.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        records.groupBy { it.muscleGroup }.forEach { (group, items) ->
            Text(group.uppercase(), fontSize = 11.sp, letterSpacing = 1.5.sp, color = Clay)
            JournalCard {
                items.forEach { record ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(record.exerciseName, fontSize = 14.sp)
                            Text(record.performedOn, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${record.weightKg.clean()} kg", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        record.reps?.let { Text(" × $it", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

@Composable private fun BackLink(onBack: () -> Unit) {
    TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
        Icon(Icons.Outlined.ArrowBack, null, Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("Workouts")
    }
}

@Composable private fun LibraryAddButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.height(40.dp), shape = CircleShape, contentPadding = PaddingValues(horizontal = 13.dp)) {
        Icon(Icons.Outlined.Add, null, Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text(label)
    }
}

@Composable private fun CreateTemplateSheet(
    exercises: List<ExerciseEntity>,
    onDismiss: () -> Unit,
    onCreate: (String, List<TemplateTarget>) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selected by remember(exercises) { mutableStateOf(exercises.firstOrNull()) }
    var menu by remember { mutableStateOf(false) }
    var sets by remember { mutableStateOf(3) }
    var repLow by remember { mutableStateOf(8) }
    var repHigh by remember { mutableStateOf(12) }
    val targets = remember { mutableStateListOf<TemplateTarget>() }

    FormSheet(
        "Build template",
        "Set up the whole session now. You can edit it later.",
        "Save template",
        name.isNotBlank() && targets.isNotEmpty(),
        { onCreate(name.trim(), targets.toList()) },
        onDismiss,
    ) {
        AppField(name, { name = it }, "Template name")
        Text("EXERCISES", fontSize = 10.sp, letterSpacing = 1.4.sp, color = Clay)
        targets.forEachIndexed { index, target ->
            val exercise = exercises.firstOrNull { it.id == target.exerciseId }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("${index + 1}", Modifier.width(24.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(Modifier.weight(1f)) {
                    Text(exercise?.name.orEmpty(), fontSize = 14.sp)
                    Text("${target.targetSets} × ${target.repLow}–${target.repHigh}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { targets.removeAt(index) }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Outlined.Delete, "Remove exercise", Modifier.size(16.dp))
                }
            }
        }
        androidx.compose.foundation.layout.Box {
            Button(
                onClick = { menu = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
            ) { Text(selected?.name ?: "Choose exercise", fontWeight = FontWeight.Normal) }
            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                exercises.filterNot { exercise -> targets.any { it.exerciseId == exercise.id } }.forEach { exercise ->
                    DropdownMenuItem(text = { Text(exercise.name) }, onClick = { selected = exercise; menu = false })
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TargetStepper("Sets", sets, 1..8, Modifier.weight(1f)) { sets = it }
            TargetStepper("Reps", repLow, 1..30, Modifier.weight(1f)) { repLow = it; if (repHigh < it) repHigh = it }
            TargetStepper("to", repHigh, repLow..30, Modifier.weight(1f)) { repHigh = it }
        }
        TextButton(
            onClick = {
                selected?.let { exercise ->
                    targets += TemplateTarget(exercise.id, sets, repLow, repHigh)
                    selected = exercises.firstOrNull { candidate -> targets.none { it.exerciseId == candidate.id } }
                }
            },
            enabled = selected != null && targets.none { it.exerciseId == selected?.id },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Add, null, Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text("Add to template")
        }
    }
}

@Composable
private fun TargetStepper(label: String, value: Int, range: IntRange, modifier: Modifier, onChange: (Int) -> Unit) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onChange((value - 1).coerceAtLeast(range.first)) }, enabled = value > range.first, contentPadding = PaddingValues(0.dp)) { Text("−") }
            Text(value.toString(), fontSize = 15.sp)
            TextButton(onClick = { onChange((value + 1).coerceAtMost(range.last)) }, enabled = value < range.last, contentPadding = PaddingValues(0.dp)) { Text("+") }
        }
    }
}

@Composable private fun AddTemplateLineSheet(
    exercises: List<ExerciseEntity>,
    onDismiss: () -> Unit,
    onAdd: (Long, Int, Int?, Int?) -> Unit,
) {
    var selected by remember(exercises) { mutableStateOf(exercises.firstOrNull()) }
    var menu by remember { mutableStateOf(false) }
    var sets by remember { mutableStateOf("3") }
    var low by remember { mutableStateOf("") }
    var high by remember { mutableStateOf("") }
    FormSheet("Add exercise", "Set a target; weight is logged during training.", "Add exercise", selected != null && (sets.toIntOrNull() ?: 0) > 0, {
        selected?.let { onAdd(it.id, sets.toIntOrNull() ?: 3, low.toIntOrNull(), high.toIntOrNull()) }
    }, onDismiss) {
        androidx.compose.foundation.layout.Box {
            Button(
                onClick = { menu = true }, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            ) { Text(selected?.name ?: "Choose exercise") }
            DropdownMenu(menu, { menu = false }) {
                exercises.forEach { exercise -> DropdownMenuItem({ Text(exercise.name) }, { selected = exercise; menu = false }) }
            }
        }
        AppField(sets, { sets = it.filter(Char::isDigit) }, "Target sets")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(low, { low = it.filter(Char::isDigit) }, "Min reps", Modifier.weight(1f))
            AppField(high, { high = it.filter(Char::isDigit) }, "Max reps", Modifier.weight(1f))
        }
    }
}

private fun Double.clean(): String = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)
