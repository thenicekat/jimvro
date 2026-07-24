package app.jimvro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jimvro.AppViewModel
import app.jimvro.data.ExerciseEntity
import app.jimvro.data.TemplateSummary
import app.jimvro.data.TemplateTarget
import app.jimvro.data.TemplateLine
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
        onToggleFavorite = viewModel::toggleFavorite,
        onCreateExercise = viewModel::findOrCreateExercise,
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
    var editing by remember { mutableStateOf<TemplateLine?>(null) }
    var confirmTemplateDelete by remember { mutableStateOf(false) }
    JournalCard {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(template.name, fontSize = 17.sp)
                template.notes?.let { Text(it, fontSize = 11.sp, color = Clay) }
                Text("${lines.size} exercises", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { confirmTemplateDelete = true }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Outlined.Delete, "Delete template", Modifier.size(18.dp))
            }
        }
        lines.forEachIndexed { index, line ->
            Row(Modifier.fillMaxWidth().clickable { editing = line }, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(line.exerciseName, fontSize = 14.sp)
                    Text(
                        "${line.targetSets} sets${if (line.repLow != null) " · ${line.repLow}${line.repHigh?.let { "–$it" } ?: ""} reps" else ""}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(
                    onClick = { if (index > 0) viewModel.reorderTemplateLines(lines.toMutableList().apply { add(index - 1, removeAt(index)) }) },
                    enabled = index > 0,
                    modifier = Modifier.size(32.dp),
                ) { Icon(Icons.Outlined.KeyboardArrowUp, "Move up", Modifier.size(17.dp)) }
                IconButton(
                    onClick = { if (index < lines.lastIndex) viewModel.reorderTemplateLines(lines.toMutableList().apply { add(index + 1, removeAt(index)) }) },
                    enabled = index < lines.lastIndex,
                    modifier = Modifier.size(32.dp),
                ) { Icon(Icons.Outlined.KeyboardArrowDown, "Move down", Modifier.size(17.dp)) }
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
    if (addLine) AddTemplateLineSheet(exercises, viewModel::toggleFavorite, viewModel::findOrCreateExercise, onDismiss = { addLine = false }) { exerciseId, sets, low, high ->
        viewModel.addTemplateLine(template.id, exerciseId, sets, low, high)
        addLine = false
    }
    editing?.let { line ->
        EditTemplateLineSheet(line, onDismiss = { editing = null }) { sets, low, high ->
            viewModel.updateTemplateLine(line.id, sets, low, high)
            editing = null
        }
    }
    if (confirmTemplateDelete) ConfirmDeleteDialog("Delete template?", "Logged workouts will stay, but this reusable plan will be removed.", { confirmTemplateDelete = false }) { viewModel.deleteTemplate(template.id); confirmTemplateDelete = false }
}

@Composable
private fun EditTemplateLineSheet(line: TemplateLine, onDismiss: () -> Unit, onSave: (Int, Int?, Int?) -> Unit) {
    var sets by remember(line.id) { mutableStateOf(line.targetSets.toString()) }
    var low by remember(line.id) { mutableStateOf(line.repLow?.toString().orEmpty()) }
    var high by remember(line.id) { mutableStateOf(line.repHigh?.toString().orEmpty()) }
    FormSheet("Edit ${line.exerciseName}", "Adjust working targets.", "Save changes", (sets.toIntOrNull() ?: 0) > 0, {
        onSave(sets.toIntOrNull() ?: 1, low.toIntOrNull(), high.toIntOrNull())
    }, onDismiss) {
        AppField(sets, { sets = it.filter(Char::isDigit) }, "Target sets")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(low, { low = it.filter(Char::isDigit) }, "Min reps", Modifier.weight(1f))
            AppField(high, { high = it.filter(Char::isDigit) }, "Max reps", Modifier.weight(1f))
        }
    }
}

@Composable
fun RecordsScreen(viewModel: AppViewModel, onBack: () -> Unit, onExercise: (Long) -> Unit) {
    val records by viewModel.personalRecords.collectAsStateWithLifecycle()
    Page("Training", "Personal records", "Your heaviest logged set for each exercise.") {
        BackLink(onBack)
        if (records.isEmpty()) Text("No records yet. Log weighted sets to build this list.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        records.groupBy { it.muscleGroup }.forEach { (group, items) ->
            Text(group.uppercase(), fontSize = 11.sp, letterSpacing = 1.5.sp, color = Clay)
            JournalCard {
                items.forEach { record ->
                    Row(Modifier.fillMaxWidth().clickable { onExercise(record.exerciseId) }.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(record.exerciseName, fontSize = 14.sp)
                            Text(record.performedOn, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("${record.weightKg.clean()} kg", fontSize = 18.sp)
                        record.reps?.let { Text(" × $it", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}

@Composable
fun ExerciseProgressScreen(viewModel: AppViewModel, exerciseId: Long, onBack: () -> Unit) {
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val progress by viewModel.exerciseProgress(exerciseId).collectAsStateWithLifecycle(initialValue = emptyList())
    val exercise = exercises.firstOrNull { it.id == exerciseId }
    Page(
        eyebrow = exercise?.muscleGroup ?: "Exercise",
        title = exercise?.name ?: "Progress",
        subtitle = "Session volume and top weight over time.",
    ) {
        BackLink(onBack)
        if (progress.size >= 2) {
            JournalCard {
                Text("Volume trend", fontSize = 14.sp)
                LineTrendChart(progress.map { ChartPoint(it.performedOn.takeLast(5), it.volumeKg) })
            }
            val weighted = progress.filter { it.maxWeightKg != null }
            if (weighted.size >= 2) {
                JournalCard {
                    Text("Top weight", fontSize = 14.sp)
                    LineTrendChart(weighted.map { ChartPoint(it.performedOn.takeLast(5), it.maxWeightKg!!) }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Text("Log this exercise in two sessions to unlock its trend.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("SESSIONS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        progress.asReversed().forEachIndexed { index, point ->
            Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(point.performedOn, Modifier.weight(1f), fontSize = 13.sp)
                Text("${point.volumeKg.clean()} kg vol", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                point.maxWeightKg?.let { Text("  ·  ${it.clean()} kg", fontSize = 12.sp) }
            }
            if (index < progress.lastIndex) androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        }
    }
}

@Composable private fun BackLink(onBack: () -> Unit) {
    TextButton(onClick = onBack, contentPadding = PaddingValues(0.dp)) {
        Icon(Icons.Outlined.ArrowBack, null, Modifier.size(17.dp)); Spacer(Modifier.width(4.dp)); Text("Workouts")
    }
}

@Composable private fun LibraryAddButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.height(40.dp), shape = RoundedCornerShape(9.dp), contentPadding = PaddingValues(horizontal = 13.dp)) {
        Icon(Icons.Outlined.Add, null, Modifier.size(17.dp)); Spacer(Modifier.width(5.dp)); Text(label)
    }
}

@Composable private fun CreateTemplateSheet(
    exercises: List<ExerciseEntity>,
    onToggleFavorite: (Long) -> Unit,
    onCreateExercise: suspend (String) -> ExerciseEntity,
    onDismiss: () -> Unit,
    onCreate: (String, List<TemplateTarget>) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf<ExerciseEntity?>(null) }
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
        ExerciseSearchField(
            exercises = exercises.filterNot { exercise -> targets.any { it.exerciseId == exercise.id } },
            selected = selected,
            onSelected = { selected = it },
            onToggleFavorite = onToggleFavorite,
            onCreateExercise = onCreateExercise,
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TargetStepper("Sets", sets, 1..8, Modifier.weight(1f)) { sets = it }
            TargetStepper("Reps", repLow, 1..30, Modifier.weight(1f)) { repLow = it; if (repHigh < it) repHigh = it }
            TargetStepper("to", repHigh, repLow..30, Modifier.weight(1f)) { repHigh = it }
        }
        TextButton(
            onClick = {
                selected?.let { exercise ->
                    targets += TemplateTarget(exercise.id, sets, repLow, repHigh)
                    selected = null
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
    Column(
        modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f), RoundedCornerShape(10.dp)).padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(label.uppercase(), fontSize = 9.sp, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { onChange((value - 1).coerceAtLeast(range.first)) }, enabled = value > range.first, modifier = Modifier.size(34.dp), contentPadding = PaddingValues(0.dp)) { Text("−") }
            Text(value.toString(), Modifier.weight(1f), fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            TextButton(onClick = { onChange((value + 1).coerceAtMost(range.last)) }, enabled = value < range.last, modifier = Modifier.size(34.dp), contentPadding = PaddingValues(0.dp)) { Text("+") }
        }
    }
}

@Composable
internal fun ExerciseSearchField(
    exercises: List<ExerciseEntity>,
    selected: ExerciseEntity?,
    onSelected: (ExerciseEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit = {},
    onCreateExercise: suspend (String) -> ExerciseEntity,
) {
    var pickerOpen by remember { mutableStateOf(false) }
    Surface(
        onClick = { pickerOpen = true },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    ) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Search, null, Modifier.size(19.dp), tint = Clay)
            Spacer(Modifier.width(10.dp))
            Text(
                selected?.name ?: "Choose exercise",
                Modifier.weight(1f),
                fontSize = 14.sp,
                color = if (selected == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
    if (pickerOpen) {
        ExercisePickerDialog(
            exercises = exercises,
            onDismiss = { pickerOpen = false },
            onSelected = { onSelected(it); pickerOpen = false },
            onToggleFavorite = onToggleFavorite,
            onCreateExercise = onCreateExercise,
        )
    }
}

@Composable
private fun ExercisePickerDialog(
    exercises: List<ExerciseEntity>,
    onDismiss: () -> Unit,
    onSelected: (ExerciseEntity) -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onCreateExercise: suspend (String) -> ExerciseEntity,
) {
    var query by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val matches = remember(query, exercises) {
        exercises.asSequence()
            .filter { exercise ->
                query.isBlank() || exercise.name.contains(query, ignoreCase = true)
            }
            .sortedWith(compareByDescending<ExerciseEntity> { it.favorite }.thenBy { it.name })
            .take(100)
            .toList()
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            shape = RectangleShape,
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(Modifier.padding(top = 12.dp)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Exercise name", style = MaterialTheme.typography.headlineMedium)
                        Text("Type a new name or reuse one you logged before", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
                    placeholder = { Text("e.g. Incline dumbbell press") },
                    leadingIcon = { Icon(Icons.Outlined.Search, null, Modifier.size(19.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Clay),
                )
                if (query.isNotBlank() && matches.none { it.name.equals(query.trim(), ignoreCase = true) }) {
                    TextButton(
                        onClick = { scope.launch { onSelected(onCreateExercise(query)) } },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
                    ) { Icon(Icons.Outlined.Add, null); Spacer(Modifier.width(6.dp)); Text("Use “${query.trim()}”") }
                }
                if (matches.isNotEmpty()) Text("Previously used", Modifier.padding(horizontal = 20.dp, vertical = 10.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LazyColumn(Modifier.weight(1f)) {
                    items(matches, key = { it.id }) { exercise ->
                        Row(
                            Modifier.fillMaxWidth().clickable { onSelected(exercise) }.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    listOfNotNull(exercise.target, exercise.equipment).joinToString(" · "),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = { onToggleFavorite(exercise.id) }) {
                                Icon(
                                    if (exercise.favorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                                    if (exercise.favorite) "Remove favorite" else "Add favorite",
                                    tint = if (exercise.favorite) Clay else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
                    }
                }
            }
        }
    }
}

@Composable private fun AddTemplateLineSheet(
    exercises: List<ExerciseEntity>,
    onToggleFavorite: (Long) -> Unit,
    onCreateExercise: suspend (String) -> ExerciseEntity,
    onDismiss: () -> Unit,
    onAdd: (Long, Int, Int?, Int?) -> Unit,
) {
    var selected by remember { mutableStateOf<ExerciseEntity?>(null) }
    var sets by remember { mutableStateOf("3") }
    var low by remember { mutableStateOf("") }
    var high by remember { mutableStateOf("") }
    FormSheet("Add exercise", "Set a target; weight is logged during training.", "Add exercise", selected != null && (sets.toIntOrNull() ?: 0) > 0, {
        selected?.let { onAdd(it.id, sets.toIntOrNull() ?: 3, low.toIntOrNull(), high.toIntOrNull()) }
    }, onDismiss) {
        ExerciseSearchField(exercises, selected, { selected = it }, onToggleFavorite, onCreateExercise)
        AppField(sets, { sets = it.filter(Char::isDigit) }, "Target sets")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(low, { low = it.filter(Char::isDigit) }, "Min reps", Modifier.weight(1f))
            AppField(high, { high = it.filter(Char::isDigit) }, "Max reps", Modifier.weight(1f))
        }
    }
}

private fun Double.clean(): String = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)
