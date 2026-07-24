package app.jimvro.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.jimvro.AppViewModel
import app.jimvro.data.ExerciseEntity
import app.jimvro.data.PreviousSet
import app.jimvro.data.WorkoutSetDetail
import app.jimvro.ui.theme.Clay
import app.jimvro.ui.theme.ClayMuted
import app.jimvro.ui.theme.Fraunces

@Composable
fun WorkoutTrackerScreen(viewModel: AppViewModel, workoutId: Long, onBack: () -> Unit) {
    val workout by viewModel.workout(workoutId).collectAsStateWithLifecycle(initialValue = null)
    val sets by viewModel.workoutSets(workoutId).collectAsStateWithLifecycle(initialValue = emptyList())
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val groups = sets.groupBy { it.exerciseId }.values.toList()
    var index by remember { mutableIntStateOf(0) }
    var showAddExercise by remember { mutableStateOf(false) }
    LaunchedEffect(groups.size) { index = index.coerceIn(0, (groups.size - 1).coerceAtLeast(0)) }

    val totalVolume = sets.sumOf { (it.reps ?: 0) * (it.weightKg ?: 0.0) }
    val completed = sets.count { it.reps != null || it.weightKg != null }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 0.dp)) {
                    Icon(Icons.Outlined.ArrowBack, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("All sessions")
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text(workout?.name ?: "Workout", fontFamily = Fraunces, fontSize = 32.sp, fontWeight = FontWeight.Normal)
                        Text(workout?.performedOn.orEmpty(), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (totalVolume > 0) Column(horizontalAlignment = Alignment.End) {
                        Text(totalVolume.prettyTracker(), fontSize = 22.sp, fontWeight = FontWeight.Medium)
                        Text("kg volume", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (groups.isEmpty()) {
            item {
                EmptyWorkoutCard { showAddExercise = true }
            }
        } else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    groups.forEachIndexed { dotIndex, group ->
                        Box(
                            Modifier
                                .padding(horizontal = 3.dp)
                                .width(if (dotIndex == index) 24.dp else 7.dp)
                                .height(7.dp)
                                .background(
                                    if (dotIndex == index) Clay else if (group.all { it.reps != null || it.weightKg != null }) Clay.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outline,
                                    CircleShape,
                                )
                                .clickable { index = dotIndex },
                        )
                    }
                }
            }

            val current = groups[index]
            item {
                ExerciseHeader(
                    name = current.first().exerciseName,
                    position = index + 1,
                    count = groups.size,
                    canGoBack = index > 0,
                    canGoForward = index < groups.lastIndex,
                    onBack = { index-- },
                    onForward = { index++ },
                )
            }
            item {
                PreviousPerformance(
                    viewModel = viewModel,
                    workoutId = workoutId,
                    exerciseId = current.first().exerciseId,
                )
            }
            current.forEachIndexed { rowIndex, set ->
                item(key = set.id) {
                    TrackerSetRow(
                        set = set,
                        showLabels = rowIndex == 0,
                        onUpdate = { reps, weight -> viewModel.updateSet(set.id, reps, weight, set.rpe) },
                        onDelete = { viewModel.deleteSet(set.id) },
                    )
                }
            }
            item {
                TextButton(
                    onClick = { viewModel.appendSet(workoutId, current.first().exerciseId) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Icon(Icons.Outlined.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add set")
                }
            }
            item {
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("$completed / ${sets.size} sets logged", Modifier.weight(1f), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (index < groups.lastIndex) {
                        Button(onClick = { index++ }, shape = CircleShape) {
                            Text("Next exercise")
                            Icon(Icons.Outlined.ChevronRight, null)
                        }
                    } else {
                        Text("Last exercise", color = Clay, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            item {
                TextButton(onClick = { showAddExercise = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add exercise")
                }
            }
        }
    }

    if (showAddExercise) {
        AddExerciseSheet(
            exercises = exercises,
            onDismiss = { showAddExercise = false },
            onAdd = { exerciseId, reps, weight ->
                viewModel.appendSet(workoutId, exerciseId, reps, weight)
                showAddExercise = false
            },
        )
    }
}

@Composable
private fun EmptyWorkoutCard(onAdd: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Log your first set", fontFamily = Fraunces, fontSize = 24.sp)
            Text("Choose an exercise, then record reps and weight as you train.", color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 20.sp)
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface),
            ) {
                Icon(Icons.Outlined.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("Add exercise")
            }
        }
    }
}

@Composable
private fun ExerciseHeader(
    name: String,
    position: Int,
    count: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, enabled = canGoBack) { Icon(Icons.Outlined.ChevronLeft, "Previous exercise") }
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$position / $count", fontSize = 11.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(name, fontFamily = Fraunces, fontSize = 25.sp, fontWeight = FontWeight.Normal)
        }
        IconButton(onClick = onForward, enabled = canGoForward) { Icon(Icons.Outlined.ChevronRight, "Next exercise") }
    }
}

@Composable
private fun PreviousPerformance(
    viewModel: AppViewModel,
    workoutId: Long,
    exerciseId: Long,
) {
    val previous by viewModel.previousSets(workoutId, exerciseId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    if (previous.isEmpty()) return

    Column(
        Modifier
            .fillMaxWidth()
            .background(ClayMuted.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text("LAST TIME", Modifier.weight(1f), fontSize = 10.sp, letterSpacing = 1.3.sp, color = Clay)
            Text(previous.first().performedOn, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            previous.joinToString("  ·  ") { it.summary() },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun PreviousSet.summary(): String = when {
    reps != null && weightKg != null -> "$reps × ${weightKg.prettyTracker()} kg"
    reps != null -> "$reps reps"
    weightKg != null -> "${weightKg.prettyTracker()} kg"
    else -> "—"
}

@Composable
private fun TrackerSetRow(
    set: WorkoutSetDetail,
    showLabels: Boolean,
    onUpdate: (Int?, Double?) -> Unit,
    onDelete: () -> Unit,
) {
    var reps by remember(set.id, set.reps) { mutableStateOf(set.reps?.toString().orEmpty()) }
    var weight by remember(set.id, set.weightKg) { mutableStateOf(set.weightKg?.prettyTracker().orEmpty()) }
    val done = reps.isNotBlank() || weight.isNotBlank()
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (done) ClayMuted.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, if (done) Clay.copy(alpha = 0.28f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("SET ${set.setNumber}", Modifier.weight(1f), fontSize = 11.sp, letterSpacing = 1.2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) { Icon(Icons.Outlined.Delete, "Delete set", Modifier.size(18.dp)) }
            }
            if (showLabels) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Reps", Modifier.weight(1f), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Weight (kg)", Modifier.weight(1f), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(48.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                TrackerNumberField(reps, "Reps", Modifier.weight(1f)) { value ->
                    reps = value
                    onUpdate(value.toIntOrNull(), weight.toDoubleOrNull())
                }
                TrackerNumberField(weight, "Weight", Modifier.weight(1f)) { value ->
                    weight = value
                    onUpdate(reps.toIntOrNull(), value.toDoubleOrNull())
                }
                Box(
                    Modifier.size(48.dp).background(if (done) Clay else ClayMuted, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Outlined.Check, null, tint = if (done) MaterialTheme.colorScheme.surface else Clay) }
            }
        }
    }
}

@Composable
private fun TrackerNumberField(value: String, label: String, modifier: Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) onChange(it) },
        modifier = modifier,
        placeholder = { Text("—") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Clay,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
        ),
    )
}

@Composable
private fun AddExerciseSheet(
    exercises: List<ExerciseEntity>,
    onDismiss: () -> Unit,
    onAdd: (Long, Int?, Double?) -> Unit,
) {
    var selected by remember(exercises) { mutableStateOf(exercises.firstOrNull()) }
    var menuOpen by remember { mutableStateOf(false) }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    FormSheet(
        title = "Add an exercise",
        description = "Choose a movement and log the first set.",
        primaryLabel = "Add to workout",
        primaryEnabled = selected != null,
        onPrimary = { selected?.let { onAdd(it.id, reps.toIntOrNull(), weight.toDoubleOrNull()) } },
        onDismiss = onDismiss,
    ) {
        Box {
            Button(
                onClick = { menuOpen = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.75f)),
            ) { Text(selected?.name ?: "Choose exercise") }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                exercises.forEach { exercise ->
                    DropdownMenuItem(text = { Text(exercise.name) }, onClick = { selected = exercise; menuOpen = false })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(reps, { reps = it.filter(Char::isDigit) }, "Reps", Modifier.weight(1f))
            AppField(weight, { weight = it.filter { char -> char.isDigit() || char == '.' } }, "Weight (kg)", Modifier.weight(1f))
        }
    }
}

private fun Double.prettyTracker(): String = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)
