package app.divyateja.jimvro.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.divyateja.jimvro.AppViewModel
import app.divyateja.jimvro.data.ExerciseEntity
import app.divyateja.jimvro.data.PreviousSet
import app.divyateja.jimvro.data.WorkoutSetDetail
import app.divyateja.jimvro.ui.theme.Clay
import app.divyateja.jimvro.ui.theme.ClayMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WorkoutTrackerScreen(viewModel: AppViewModel, workoutId: Long, settings: AppSettings, onBack: () -> Unit) {
    val workout by viewModel.workout(workoutId).collectAsStateWithLifecycle(initialValue = null)
    val sets by viewModel.workoutSets(workoutId).collectAsStateWithLifecycle(initialValue = emptyList())
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val groups = sets.groupBy { it.exerciseId }.values.toList()
    var index by remember { mutableIntStateOf(0) }
    var showAddExercise by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }
    var editingFinished by remember { mutableStateOf(false) }
    var pendingSetDelete by remember { mutableStateOf<Long?>(null) }
    var restRemaining by remember { mutableIntStateOf(0) }
    var restActive by remember { mutableStateOf(false) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val goToExercise: (Int) -> Unit = { target ->
        index = target.coerceIn(0, (groups.size - 1).coerceAtLeast(0))
        scope.launch { listState.scrollToItem(if (restRemaining > 0) 2 else 1) }
    }
    LaunchedEffect(groups.size) { index = index.coerceIn(0, (groups.size - 1).coerceAtLeast(0)) }
    LaunchedEffect(restRemaining, restActive) {
        if (restRemaining > 0) { delay(1_000); restRemaining-- }
        else if (restActive) { restActive = false; alertRestComplete(context) }
    }
    LaunchedEffect(workout?.finishedAt) {
        while (workout?.finishedAt == null) { now = System.currentTimeMillis(); delay(1_000) }
    }

    val totalVolume = sets.sumOf { (it.reps ?: 0) * (it.weightKg ?: 0.0) }
    val completed = sets.count { it.reps != null || it.weightKg != null }
    val finished = workout?.finishedAt != null
    val canEdit = !finished || editingFinished
    val elapsedSeconds = workout?.let { ((it.finishedAt ?: now) - it.createdAt).coerceAtLeast(0) / 1_000 } ?: 0

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = if (finished) 20.dp else 84.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 0.dp)) {
                        Icon(Icons.Outlined.ArrowBack, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Workouts")
                    }
                    Spacer(Modifier.weight(1f))
                    if (finished) {
                        TextButton(onClick = { editingFinished = !editingFinished }) {
                            Text(if (editingFinished) "Done" else "Edit")
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text(workout?.name ?: "Workout", style = MaterialTheme.typography.headlineMedium)
                        Text("${workout?.performedOn.orEmpty()} · ${elapsedSeconds.sessionDuration()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (totalVolume > 0) {
                            Text(totalVolume.prettyTracker(), fontSize = 21.sp)
                            Text("kg volume", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        if (!finished && restRemaining > 0) item {
            Row(
                Modifier.fillMaxWidth().background(ClayMuted.copy(alpha = 0.4f), RoundedCornerShape(10.dp)).padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Rest", Modifier.weight(1f), fontSize = 13.sp)
                Text("${restRemaining / 60}:${(restRemaining % 60).toString().padStart(2, '0')}", fontSize = 18.sp)
                TextButton(onClick = { restRemaining += 30 }) { Text("+30s") }
                TextButton(onClick = { restRemaining = 0 }) { Text("Skip") }
            }
        }

        if (groups.isEmpty()) {
            item {
                EmptyWorkoutCard { showAddExercise = true }
            }
        } else {
            val current = groups[index]
            item {
                ExerciseHeader(
                    name = current.first().exerciseName,
                    position = index + 1,
                    count = groups.size,
                    canGoBack = index > 0,
                    canGoForward = index < groups.lastIndex,
                    onBack = { goToExercise(index - 1) },
                    onForward = { goToExercise(index + 1) },
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
                        settings = settings,
                        showLabels = rowIndex == 0,
                        readOnly = !canEdit,
                        onUpdate = { reps, weight -> viewModel.updateSet(set.id, reps, weight, set.rpe) },
                        onSetType = { viewModel.updateSetType(set.id, it) },
                        onComplete = {
                            restRemaining = settings.restSeconds
                            restActive = true
                            if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        onDelete = { pendingSetDelete = set.id },
                    )
                }
            }
            if (canEdit) item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(
                        onClick = { viewModel.appendSet(workoutId, current.first().exerciseId) },
                        modifier = Modifier.weight(1f).height(44.dp),
                    ) {
                        Icon(Icons.Outlined.Add, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Blank set")
                    }
                    val last = current.last()
                    if (last.reps != null || last.weightKg != null) {
                        Button(
                            onClick = { viewModel.appendSet(workoutId, last.exerciseId, last.reps, last.weightKg) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface, contentColor = MaterialTheme.colorScheme.surface),
                        ) { Text("Repeat last", fontWeight = FontWeight.Normal) }
                    }
                }
            }
            item {
                HorizontalDivider()
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("$completed / ${sets.size} sets logged", Modifier.weight(1f), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (index < groups.lastIndex) {
                        Button(onClick = { goToExercise(index + 1) }, shape = RoundedCornerShape(9.dp)) {
                            Text("Next exercise")
                            Icon(Icons.Outlined.ChevronRight, null)
                        }
                    } else {
                        Text("Last exercise", color = Clay, fontSize = 14.sp)
                    }
                }
            }
            if (canEdit) item {
                if (index < groups.lastIndex) {
                    TextButton(
                        onClick = {
                            viewModel.setSuperset(
                                workoutId,
                                listOf(current.first().exerciseId, groups[index + 1].first().exerciseId),
                                index + 1,
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Pair with next as superset") }
                }
                TextButton(onClick = { showAddExercise = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Outlined.Add, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Add exercise")
                }
            }
        }
        }
        if (!finished) Row(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("$completed/${sets.size} sets", Modifier.weight(1f), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(
                onClick = { viewModel.finishWorkout(workoutId); showSummary = true },
                enabled = sets.isNotEmpty(),
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 24.dp),
            ) { Text("Finish workout") }
        }
    }

    if (canEdit && showAddExercise) {
        AddExerciseSheet(
            exercises = exercises,
            onToggleFavorite = viewModel::toggleFavorite,
            onCreateExercise = viewModel::findOrCreateExercise,
            onDismiss = { showAddExercise = false },
            onAdd = { exerciseId, reps, weight ->
                viewModel.appendSet(workoutId, exerciseId, reps, weight?.storageWeight(settings))
                showAddExercise = false
            },
        )
    }
    if (showSummary) {
        AlertDialog(
            onDismissRequest = { showSummary = false },
            title = { Text("Workout complete") },
            text = { Text("${elapsedSeconds.sessionDuration()} · $completed sets · ${totalVolume.prettyTracker()} kg volume · ${groups.size} exercises") },
            confirmButton = { TextButton(onClick = { showSummary = false; onBack() }) { Text("Done") } },
            dismissButton = { TextButton(onClick = { showSummary = false }) { Text("Stay") } },
        )
    }
    pendingSetDelete?.let { id -> ConfirmDeleteDialog("Delete set?", "This set will be removed from workout volume.", { pendingSetDelete = null }) { viewModel.deleteSet(id); pendingSetDelete = null } }
}

@Composable
private fun EmptyWorkoutCard(onAdd: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Log your first set", fontSize = 21.sp)
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
        Column(Modifier.weight(1f)) {
            Text("$position OF $count", fontSize = 10.sp, letterSpacing = 1.2.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(name, fontSize = 21.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal)
        }
        IconButton(onClick = onBack, enabled = canGoBack, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Outlined.ChevronLeft, "Previous exercise")
        }
        IconButton(onClick = onForward, enabled = canGoForward, modifier = Modifier.size(40.dp)) {
            Icon(Icons.Outlined.ChevronRight, "Next exercise")
        }
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
    settings: AppSettings,
    showLabels: Boolean,
    readOnly: Boolean,
    onUpdate: (Int?, Double?) -> Unit,
    onSetType: (String) -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    var reps by remember(set.id, set.reps) { mutableStateOf(set.reps?.toString().orEmpty()) }
    var weight by remember(set.id, set.weightKg, settings.weightUnit) { mutableStateOf(set.weightKg?.displayWeight(settings)?.prettyTracker().orEmpty()) }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
            if (showLabels) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Spacer(Modifier.width(88.dp))
                Text("Reps", Modifier.weight(1f), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("${settings.weightUnit}", Modifier.weight(1f), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!readOnly) Spacer(Modifier.width(32.dp))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(set.setNumber.toString().padStart(2, '0'), Modifier.width(24.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val typeLabel = when (set.setType) { "warmup" -> "Warm"; "drop" -> "Drop"; else -> "Work" }
                if (readOnly) {
                    Text(typeLabel, Modifier.width(56.dp), fontSize = 10.sp, color = Clay)
                    Text(reps.ifBlank { "—" }, Modifier.weight(1f).padding(vertical = 12.dp), fontSize = 16.sp)
                    Text(weight.ifBlank { "—" }, Modifier.weight(1f).padding(vertical = 12.dp), fontSize = 16.sp)
                } else {
                    TextButton(
                        onClick = {
                            val types = listOf("working", "warmup", "drop")
                            onSetType(types[(types.indexOf(set.setType) + 1).mod(types.size)])
                        },
                        modifier = Modifier.width(56.dp), contentPadding = PaddingValues(horizontal = 0.dp),
                    ) { Text(typeLabel, fontSize = 10.sp, color = Clay) }
                    TrackerNumberField(reps, "Reps", Modifier.weight(1f), onComplete) { value ->
                        reps = value
                        onUpdate(value.toIntOrNull(), weight.toDoubleOrNull()?.storageWeight(settings))
                    }
                    TrackerNumberField(weight, "Weight", Modifier.weight(1f), onComplete) { value ->
                        weight = value
                        onUpdate(reps.toIntOrNull(), value.toDoubleOrNull()?.storageWeight(settings))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Outlined.Delete, "Delete set", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    }
}

@Composable
private fun TrackerNumberField(value: String, label: String, modifier: Modifier, onDone: () -> Unit, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) onChange(it) },
        modifier = modifier.height(56.dp),
        placeholder = { Text("—") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { if (value.isNotBlank()) onDone() }),
        shape = RoundedCornerShape(7.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Clay,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
        ),
    )
}

@Composable
private fun AddExerciseSheet(
    exercises: List<ExerciseEntity>,
    onToggleFavorite: (Long) -> Unit,
    onCreateExercise: suspend (String) -> ExerciseEntity,
    onDismiss: () -> Unit,
    onAdd: (Long, Int?, Double?) -> Unit,
) {
    var selected by remember { mutableStateOf<ExerciseEntity?>(null) }
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
        ExerciseSearchField(exercises, selected, { selected = it }, onToggleFavorite, onCreateExercise)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(reps, { reps = it.filter(Char::isDigit) }, "Reps", Modifier.weight(1f))
            AppField(weight, { weight = it.filter { char -> char.isDigit() || char == '.' } }, "Weight", Modifier.weight(1f))
        }
    }
}

private fun Double.prettyTracker(): String = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)
private fun Long.sessionDuration(): String = "%d:%02d:%02d".format(this / 3600, (this % 3600) / 60, this % 60)

private fun alertRestComplete(context: Context) {
    val vibrator = if (Build.VERSION.SDK_INT >= 31) context.getSystemService(VibratorManager::class.java).defaultVibrator else @Suppress("DEPRECATION") (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator)
    vibrator.vibrate(VibrationEffect.createOneShot(450, VibrationEffect.DEFAULT_AMPLITUDE))
    val manager = context.getSystemService(NotificationManager::class.java)
    if (Build.VERSION.SDK_INT >= 26) manager.createNotificationChannel(NotificationChannel("rest_timer", "Rest timer", NotificationManager.IMPORTANCE_HIGH))
    if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
        manager.notify(90, NotificationCompat.Builder(context, "rest_timer").setSmallIcon(app.divyateja.jimvro.R.drawable.ic_launcher).setContentTitle("Rest complete").setContentText("Ready for your next set.").setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build())
    }
}
