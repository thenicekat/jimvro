package com.divyateja.jimvro.ui

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.ClipData
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.divyateja.jimvro.AppViewModel
import com.divyateja.jimvro.data.ExerciseEntity
import com.divyateja.jimvro.data.PreviousSet
import com.divyateja.jimvro.data.WorkoutSetDetail
import com.divyateja.jimvro.domain.progressionSuggestion
import com.divyateja.jimvro.ui.theme.Clay
import com.divyateja.jimvro.ui.theme.ClayMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun WorkoutTrackerScreen(viewModel: AppViewModel, workoutId: Long, settings: AppSettings, onBack: () -> Unit) {
    val workout by viewModel.workout(workoutId).collectAsStateWithLifecycle(initialValue = null)
    val sets by viewModel.workoutSets(workoutId).collectAsStateWithLifecycle(initialValue = emptyList())
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val groups = sets.groupBy { it.exerciseId }.values.toList()
    val pagerState = rememberPagerState(pageCount = { groups.size.coerceAtLeast(1) })
    var showAddExercise by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }
    var showSaveTemplate by remember { mutableStateOf(false) }
    var templateName by remember { mutableStateOf("") }
    var editingFinished by remember { mutableStateOf(false) }
    var showDurationEdit by remember { mutableStateOf(false) }
    var pendingSetDelete by remember { mutableStateOf<Long?>(null) }
    var editingMuscleGroup by remember { mutableStateOf<Pair<Long, String>?>(null) }
    var restRemaining by remember { mutableIntStateOf(0) }
    var restActive by remember { mutableStateOf(false) }
    var prMessage by remember { mutableStateOf<String?>(null) }
    var announcedPrSets by remember { mutableStateOf(emptySet<Long>()) }
    var pendingExerciseId by remember { mutableStateOf<Long?>(null) }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val context = LocalContext.current
    val notificationPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    val scope = rememberCoroutineScope()
    val goToExercise: (Int) -> Unit = { target ->
        scope.launch { pagerState.animateScrollToPage(target.coerceIn(0, (groups.size - 1).coerceAtLeast(0))) }
    }
    LaunchedEffect(groups, pendingExerciseId) {
        val target = groups.indexOfFirst { it.first().exerciseId == pendingExerciseId }
        if (target >= 0) {
            pagerState.scrollToPage(target)
            pendingExerciseId = null
        }
    }
    LaunchedEffect(groups.size) {
        if (groups.isNotEmpty() && pagerState.currentPage > groups.lastIndex) {
            pagerState.scrollToPage(groups.lastIndex)
        }
    }
    LaunchedEffect(restRemaining, restActive) {
        if (restRemaining > 0) { delay(1_000); restRemaining-- }
        else if (restActive) { restActive = false; alertRestComplete(context) }
    }
    LaunchedEffect(workout?.finishedAt) {
        while (workout?.finishedAt == null) { now = System.currentTimeMillis(); delay(1_000) }
    }
    LaunchedEffect(prMessage) {
        if (prMessage != null) {
            delay(4_000)
            prMessage = null
        }
    }

    val totalVolume = sets.filter { it.isCompleted }.sumOf { (it.reps ?: 0) * (it.weightKg ?: 0.0) }
    val completed = sets.count { it.isCompleted }
    val finished = workout?.finishedAt != null
    val canEdit = !finished || editingFinished
    val elapsedSeconds = workout?.let { ((it.finishedAt ?: now) - it.createdAt).coerceAtLeast(0) / 1_000 } ?: 0
    val shareWorkout = { shareWorkoutImage(context, workout?.name ?: "Workout", formatDateForDisplay(workout?.performedOn.orEmpty()), elapsedSeconds, completed, totalVolume, groups) }

    Box(Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { page -> groups.getOrNull(page)?.firstOrNull()?.exerciseId ?: -1L },
        ) { page ->
        val current = groups.getOrNull(page)
        LazyColumn(
            state = rememberLazyListState(),
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
                        TextButton(onClick = {
                            templateName = workout?.name ?: "Workout"
                            showSaveTemplate = true
                        }) { Text("Save template") }
                        TextButton(onClick = shareWorkout) { Text("Share") }
                        TextButton(onClick = { editingFinished = !editingFinished }) {
                            Text(if (editingFinished) "Done" else "Edit")
                        }
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text(workout?.name ?: "Workout", style = MaterialTheme.typography.headlineMedium)
                        Text(
                            "${formatDateForDisplay(workout?.performedOn.orEmpty())} · ${elapsedSeconds.sessionDuration()}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = if (editingFinished) Modifier.clickable { showDurationEdit = true } else Modifier,
                        )
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

        prMessage?.let { message ->
            item {
                Row(
                    Modifier.fillMaxWidth().background(Clay.copy(alpha = 0.14f), RoundedCornerShape(10.dp)).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("NEW PR", fontSize = 10.sp, letterSpacing = 1.2.sp, color = Clay)
                    Spacer(Modifier.width(12.dp))
                    Text(message, fontSize = 13.sp)
                }
            }
        }

        if (current == null) {
            item {
                EmptyWorkoutCard { showAddExercise = true }
            }
        } else {
            item {
                ExerciseHeader(
                    name = current.first().exerciseName,
                    muscleGroup = current.first().muscleGroup,
                    position = page + 1,
                    count = groups.size,
                    canGoBack = page > 0,
                    canGoForward = page < groups.lastIndex,
                    onBack = { goToExercise(page - 1) },
                    onForward = { goToExercise(page + 1) },
                    onEditMuscleGroup = { editingMuscleGroup = current.first().exerciseId to current.first().muscleGroup },
                )
            }
            item {
                PreviousPerformance(
                    viewModel = viewModel,
                    workoutId = workoutId,
                    exerciseId = current.first().exerciseId,
                    targetRepLow = current.first().targetRepLow,
                    targetRepHigh = current.first().targetRepHigh,
                )
            }
            val nextSetId = current.firstOrNull { !it.isCompleted }?.id
            current.forEachIndexed { rowIndex, set ->
                item(key = set.id) {
                    TrackerSetRow(
                        set = set,
                        settings = settings,
                        showLabels = rowIndex == 0,
                        readOnly = !canEdit,
                        isNext = canEdit && set.id == nextSetId,
                        onUpdate = { reps, weight -> viewModel.updateSet(set.id, reps, weight, set.rpe) },
                        onSetType = { viewModel.updateSetType(set.id, it) },
                        onComplete = { reps, weightKg ->
                            viewModel.completeSet(set.id, reps, weightKg)
                            if (!finished) {
                                restRemaining = settings.restSeconds
                                restActive = true
                            }
                            if (
                                set.setType == "working" && weightKg != null &&
                                set.id !in announcedPrSets
                            ) {
                                scope.launch {
                                    if (viewModel.isWeightPersonalRecord(set.exerciseId, set.id, weightKg)) {
                                        announcedPrSets = announcedPrSets + set.id
                                        prMessage = "${set.exerciseName} · ${weightKg.prettyTracker()} kg × $reps"
                                    }
                                }
                            }
                            if (!finished && Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        onUncomplete = { viewModel.uncompleteSet(set.id) },
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
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
                        ) { Text("Repeat last", fontWeight = FontWeight.Normal) }
                    }
                }
            }
            if (canEdit) item {
                if (page < groups.lastIndex) {
                    TextButton(
                        onClick = {
                            viewModel.setSuperset(
                                workoutId,
                                listOf(current.first().exerciseId, groups[page + 1].first().exerciseId),
                                page + 1,
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
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text("$completed/${sets.size} sets", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                LinearProgressIndicator(
                    progress = { if (sets.isEmpty()) 0f else completed.toFloat() / sets.size },
                    modifier = Modifier.width(96.dp).height(3.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
            }
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
            onDeleteExercise = viewModel::deleteExercise,
            onDismiss = { showAddExercise = false },
            onAdd = { exerciseId, reps, weight ->
                pendingExerciseId = exerciseId
                viewModel.appendSet(workoutId, exerciseId, reps, weight?.storageWeight(settings), prefillFromHistory = true)
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
    if (showSaveTemplate) {
        AlertDialog(
            onDismissRequest = { showSaveTemplate = false },
            title = { Text("Save as template") },
            text = { OutlinedTextField(templateName, { templateName = it }, label = { Text("Template name") }) },
            confirmButton = { TextButton(onClick = {
                viewModel.createTemplateFromWorkout(workoutId, templateName.trim())
                showSaveTemplate = false
            }, enabled = templateName.isNotBlank()) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showSaveTemplate = false }) { Text("Cancel") } },
        )
    }
    if (showDurationEdit) {
        workout?.let { w ->
            DurationEditDialog(
                currentSeconds = elapsedSeconds,
                onDismiss = { showDurationEdit = false },
                onConfirm = { newSeconds ->
                    viewModel.updateWorkoutDuration(workoutId, w.createdAt, newSeconds)
                    showDurationEdit = false
                },
            )
        }
    }
    pendingSetDelete?.let { id -> ConfirmDeleteDialog("Delete set?", "This set will be removed from the workout.", { pendingSetDelete = null }) { viewModel.deleteSet(id); pendingSetDelete = null } }
    editingMuscleGroup?.let { (exerciseId, current) ->
        MuscleGroupPickerSheet(
            current = current,
            onDismiss = { editingMuscleGroup = null },
            onSelect = { group ->
                viewModel.updateMuscleGroup(exerciseId, group)
                editingMuscleGroup = null
            },
        )
    }
}

private fun shareWorkoutImage(context: Context, name: String, date: String, seconds: Long, completed: Int, volumeKg: Double, groups: List<List<WorkoutSetDetail>>) {
    val logged = groups.mapNotNull { exerciseSets ->
        exerciseSets.filter { it.isCompleted }.takeIf { it.isNotEmpty() }?.let { sets ->
            exerciseSets.first().exerciseName to sets.joinToString("  ·  ") { set ->
                listOfNotNull(set.reps?.let { "$it reps" }, set.weightKg?.let { "$it kg" }).joinToString(" × ")
            }
        }
    }
    val bitmap = Bitmap.createBitmap(1080, 430 + logged.size * 140, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    canvas.drawColor(Color.rgb(13, 16, 14))
    paint.color = Color.rgb(151, 202, 70); paint.textSize = 30f; paint.letterSpacing = 0.12f
    canvas.drawText("JIMVRO · WORKOUT", 72f, 88f, paint)
    paint.color = Color.rgb(246, 247, 244); paint.textSize = 58f; paint.letterSpacing = 0f
    canvas.drawText(name.take(30), 72f, 168f, paint)
    paint.color = Color.rgb(190, 195, 187); paint.textSize = 28f
    canvas.drawText("$date · ${seconds.sessionDuration()}", 72f, 218f, paint)
    paint.color = Color.rgb(246, 247, 244); paint.textSize = 36f
    canvas.drawText("$completed sets  ·  ${volumeKg.prettyTracker()} kg volume", 72f, 298f, paint)
    var y = 390f
    logged.forEach { (exercise, sets) ->
        paint.color = Color.rgb(246, 247, 244); paint.textSize = 32f
        canvas.drawText(exercise.take(42), 72f, y, paint)
        paint.color = Color.rgb(190, 195, 187); paint.textSize = 25f
        canvas.drawText(sets.take(70), 72f, y + 40f, paint)
        y += 140f
    }
    val output = java.io.File(context.cacheDir, "jimvro-workout.png")
    output.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", output)
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        clipData = ClipData.newRawUri("workout", uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }, "Share workout"))
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
    muscleGroup: String,
    position: Int,
    count: Int,
    canGoBack: Boolean,
    canGoForward: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit,
    onEditMuscleGroup: () -> Unit,
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.clickable(onClick = onEditMuscleGroup).height(40.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(muscleGroup.replaceFirstChar(Char::uppercase), fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Icon(Icons.Outlined.Edit, "Edit muscle group", Modifier.size(12.dp), tint = MaterialTheme.colorScheme.secondary)
                }
                Spacer(Modifier.weight(1f))
                Text("$position of $count", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
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
    targetRepLow: Int?,
    targetRepHigh: Int?,
) {
    val previous by viewModel.previousSets(workoutId, exerciseId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    if (previous.isEmpty()) return
    val suggestion = progressionSuggestion(previous, targetRepLow, targetRepHigh)

    Column(
        Modifier
            .fillMaxWidth()
            .background(ClayMuted.copy(alpha = 0.22f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text("LAST TIME", Modifier.weight(1f), fontSize = 10.sp, letterSpacing = 1.3.sp, color = Clay)
            Text(formatDateForDisplay(previous.first().performedOn), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            previous.joinToString("  ·  ") { it.summary() },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        suggestion?.let {
            Text(
                if (it.increase) "Next: ${it.weightKg.prettyTracker()} kg · top of range reached"
                else "Next: ${it.weightKg.prettyTracker()} kg · build toward ${targetRepHigh} reps",
                fontSize = 12.sp,
                color = Clay,
            )
        }
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
    isNext: Boolean,
    onUpdate: (Int?, Double?) -> Unit,
    onSetType: (String) -> Unit,
    onComplete: (Int, Double?) -> Unit,
    onUncomplete: () -> Unit,
    onDelete: () -> Unit,
) {
    var reps by remember(set.id, set.reps) { mutableStateOf(set.reps?.toString().orEmpty()) }
    var weight by remember(set.id, set.weightKg, settings.weightUnit) { mutableStateOf(set.weightKg?.displayWeight(settings)?.prettyTracker().orEmpty()) }
    var showSetMenu by remember(set.id) { mutableStateOf(false) }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
            if (showLabels) Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Spacer(Modifier.width(88.dp))
                Text("Reps", Modifier.weight(1f).padding(start = 8.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(settings.weightUnit, Modifier.weight(1f).padding(start = 8.dp), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Done", Modifier.width(48.dp), fontSize = 10.sp, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isNext) 0.64f else 0f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(set.setNumber.toString().padStart(2, '0'), Modifier.width(24.dp), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val typeLabel = when (set.setType) { "warmup" -> "Warm"; "drop" -> "Drop"; else -> "Work" }
                if (readOnly) {
                    Text(typeLabel, Modifier.width(56.dp), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(reps.ifBlank { "—" }, Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 12.dp), fontSize = 16.sp)
                    Text(weight.ifBlank { "—" }, Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 12.dp), fontSize = 16.sp)
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        if (set.isCompleted) Icon(Icons.Outlined.Check, "Set done", tint = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    Box(Modifier.width(56.dp)) {
                        TextButton(
                            onClick = { showSetMenu = true },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Text(typeLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Icon(Icons.Outlined.KeyboardArrowDown, null, Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(expanded = showSetMenu, onDismissRequest = { showSetMenu = false }) {
                            listOf("working" to "Working", "warmup" to "Warm-up", "drop" to "Drop").forEach { (type, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = { showSetMenu = false; onSetType(type) },
                                    trailingIcon = { if (set.setType == type) Icon(Icons.Outlined.Check, null) },
                                )
                            }
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete set", color = MaterialTheme.colorScheme.error) },
                                onClick = { showSetMenu = false; onDelete() },
                                leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            )
                        }
                    }
                    TrackerNumberField(reps, "Reps", Modifier.weight(1f)) { value ->
                        reps = value
                        onUpdate(value.toIntOrNull(), weight.toDoubleOrNull()?.storageWeight(settings))
                    }
                    TrackerNumberField(weight, "Weight", Modifier.weight(1f)) { value ->
                        weight = value
                        onUpdate(reps.toIntOrNull(), value.toDoubleOrNull()?.storageWeight(settings))
                    }
                    Checkbox(
                        checked = set.isCompleted,
                        onCheckedChange = { done ->
                            if (done) reps.toIntOrNull()?.takeIf { it > 0 }?.let { onComplete(it, weight.toDoubleOrNull()?.storageWeight(settings)) }
                            else onUncomplete()
                        },
                        enabled = set.isCompleted || (reps.toIntOrNull() ?: 0) > 0,
                        modifier = Modifier.size(48.dp).semantics {
                            contentDescription = if (set.isCompleted) "Undo set ${set.setNumber}" else "Mark set ${set.setNumber} done"
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.secondary,
                            checkmarkColor = MaterialTheme.colorScheme.onSecondary,
                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                }
            }
    }
}

@Composable
private fun TrackerNumberField(value: String, label: String, modifier: Modifier, onChange: (String) -> Unit) {
    var focused by remember { mutableStateOf(false) }
    BasicTextField(
        value = value,
        onValueChange = { if (it.matches(Regex("^\\d*\\.?\\d*$"))) onChange(it) },
        modifier = modifier
            .height(48.dp)
            .onFocusChanged { focused = it.isFocused }
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = if (focused) 0.12f else 0f), RoundedCornerShape(8.dp))
            .semantics { contentDescription = label },
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.secondary),
        decorationBox = { innerTextField ->
            Box(Modifier.fillMaxSize().padding(horizontal = 8.dp), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) Text("—", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                innerTextField()
            }
        },
    )
}

@Composable
private fun AddExerciseSheet(
    exercises: List<ExerciseEntity>,
    onToggleFavorite: (Long) -> Unit,
    onCreateExercise: suspend (String) -> ExerciseEntity,
    onDeleteExercise: suspend (Long) -> Boolean,
    onDismiss: () -> Unit,
    onAdd: (Long, Int?, Double?) -> Unit,
) {
    var selected by remember { mutableStateOf<ExerciseEntity?>(null) }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    FormSheet(
        title = "Add an exercise",
        description = "Choose a movement; set values from your last session fill in when available.",
        primaryLabel = "Add to workout",
        primaryEnabled = selected != null,
        onPrimary = { selected?.let { onAdd(it.id, reps.toIntOrNull(), weight.toDoubleOrNull()) } },
        onDismiss = onDismiss,
    ) {
        ExerciseSearchField(exercises, selected, { selected = it }, onToggleFavorite, onCreateExercise, onDeleteExercise, { selected = null })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(reps, { reps = it.filter(Char::isDigit) }, "Reps", Modifier.weight(1f))
            AppField(weight, { weight = it.filter { char -> char.isDigit() || char == '.' } }, "Weight", Modifier.weight(1f))
        }
    }
}

@Composable
private fun DurationEditDialog(currentSeconds: Long, onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var hours by remember { mutableStateOf((currentSeconds / 3600).toString()) }
    var minutes by remember { mutableStateOf(((currentSeconds % 3600) / 60).toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit duration") },
        text = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it.filter(Char::isDigit).take(2) },
                    label = { Text("Hours") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                Text(":", fontSize = 20.sp)
                OutlinedTextField(
                    value = minutes,
                    onValueChange = { minutes = it.filter(Char::isDigit).take(2) },
                    label = { Text("Minutes") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val totalSeconds = (hours.toLongOrNull() ?: 0L) * 3600 + (minutes.toLongOrNull() ?: 0L) * 60
                if (totalSeconds > 0) onConfirm(totalSeconds)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

val MUSCLE_GROUPS = com.divyateja.jimvro.data.MUSCLE_GROUPS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MuscleGroupPickerSheet(
    current: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
    ) {
        Column(
            Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("Muscle group", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            MUSCLE_GROUPS.forEach { group ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(group) }
                        .padding(vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(group.replaceFirstChar(Char::uppercase), Modifier.weight(1f), fontSize = 16.sp)
                    if (group == current) Icon(Icons.Outlined.Check, null, tint = Clay)
                }
                if (group != MUSCLE_GROUPS.last()) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
            }
            Spacer(Modifier.height(16.dp))
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
        manager.notify(90, NotificationCompat.Builder(context, "rest_timer").setSmallIcon(com.divyateja.jimvro.R.drawable.ic_launcher).setContentTitle("Rest complete").setContentText("Ready for your next set.").setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true).build())
    }
}
