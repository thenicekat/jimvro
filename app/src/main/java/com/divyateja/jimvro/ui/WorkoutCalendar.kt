package com.divyateja.jimvro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.divyateja.jimvro.AppViewModel
import com.divyateja.jimvro.data.WorkoutSummary
import com.divyateja.jimvro.ui.theme.Clay
import com.divyateja.jimvro.ui.theme.ClayMuted
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun WorkoutCalendarScreen(viewModel: AppViewModel, onBack: () -> Unit, onOpenWorkout: (Long) -> Unit) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val locale = LocalConfiguration.current.locales[0]
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val workoutsByDate = remember(workouts) { workouts.groupBy { it.performedOn } }
    val leadingBlanks = month.atDay(1).dayOfWeek.value - 1
    val cells = List<LocalDate?>(leadingBlanks) { null } +
        (1..month.lengthOfMonth()).map(month::atDay)
    val paddedCells = cells + List((7 - cells.size % 7) % 7) { null }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        TextButton(onClick = onBack, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp)) {
            Icon(Icons.Outlined.ArrowBack, null, Modifier.size(18.dp))
            Spacer(Modifier.size(4.dp))
            Text("Workouts")
        }
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("TRAINING HISTORY", color = Clay, fontSize = 10.sp, letterSpacing = 1.5.sp)
            Text("Calendar", style = MaterialTheme.typography.headlineLarge)
            Text("See training frequency and open any logged session.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                month = month.minusMonths(1)
                selectedDate = month.atDay(1)
            }) { Icon(Icons.Outlined.ChevronLeft, "Previous month") }
            Text(
                month.format(DateTimeFormatter.ofPattern("MMMM yyyy", locale)),
                Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = {
                month = month.plusMonths(1)
                selectedDate = month.atDay(1)
            }) { Icon(Icons.Outlined.ChevronRight, "Next month") }
        }
        Row(Modifier.fillMaxWidth()) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            paddedCells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    week.forEach { date ->
                        CalendarDay(
                            date = date,
                            selected = date == selectedDate,
                            sessions = date?.let { workoutsByDate[it.toString()].orEmpty() }.orEmpty(),
                            modifier = Modifier.weight(1f),
                        ) { selectedDate = date ?: selectedDate }
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(formatDateForDisplay(selectedDate.toString()), style = MaterialTheme.typography.titleMedium)
            val selectedWorkouts = workoutsByDate[selectedDate.toString()].orEmpty()
            if (selectedWorkouts.isEmpty()) {
                Text("Rest day · no sessions logged", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                selectedWorkouts.forEach { workout ->
                    Column(
                        Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .clickable { onOpenWorkout(workout.id) }.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(workout.name ?: "Workout")
                        Text(
                            "${workout.setCount} sets · ${workout.volumeKg.prettyCalendar()} kg volume",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate?,
    selected: Boolean,
    sessions: List<WorkoutSummary>,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier.aspectRatio(1f)
            .background(if (selected) ClayMuted else MaterialTheme.colorScheme.surface, RoundedCornerShape(9.dp))
            .clickable(enabled = date != null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        date?.let {
            Text(it.dayOfMonth.toString(), fontSize = 13.sp)
            if (sessions.isNotEmpty()) {
                Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp).size(5.dp).background(Clay, CircleShape))
            }
        }
    }
}

private fun Double.prettyCalendar(): String = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)
