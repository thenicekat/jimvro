package app.divyateja.jimvro.ui

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.res.Configuration
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.divyateja.jimvro.AppViewModel
import app.divyateja.jimvro.HealthConnectSync
import app.divyateja.jimvro.data.BarcodeProductEntity
import app.divyateja.jimvro.data.ExerciseEntity
import app.divyateja.jimvro.data.FoodEntryEntity
import app.divyateja.jimvro.data.MeasurementEntity
import app.divyateja.jimvro.data.ProgressPhotoEntity
import app.divyateja.jimvro.data.SavedFoodEntity
import app.divyateja.jimvro.data.WorkoutEntity
import app.divyateja.jimvro.data.WorkoutSummary
import app.divyateja.jimvro.data.WorkoutSetEntity
import app.divyateja.jimvro.domain.Macros
import app.divyateja.jimvro.domain.scaleMacros
import app.divyateja.jimvro.ui.theme.Clay
import app.divyateja.jimvro.ui.theme.ClayMuted
import app.divyateja.jimvro.ui.theme.ThemeMode
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

private enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Outlined.Home),
    Workouts("workouts", "Workouts", Icons.Outlined.FitnessCenter),
    Body("body", "Body", Icons.Outlined.Scale),
    Food("food", "Food", Icons.Outlined.Restaurant),
}

private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
private fun Double.pretty(): String = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)
private fun Long.compactDuration(): String = if (this >= 3600) "%dh %02dm".format(this / 3600, (this % 3600) / 60) else "%dm".format(this / 60)

@Suppress("DEPRECATION")
private fun Configuration.primaryLocale(): Locale =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) locales[0] else locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JimvroApp(
    viewModel: AppViewModel,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Destination.Today.route
    val isStandaloneScreen = currentRoute.startsWith("workout/") || currentRoute == "settings"
    val current = Destination.entries.firstOrNull { it.route == currentRoute }
        ?: if (currentRoute.startsWith("workout/") || currentRoute.startsWith("exercise/") || currentRoute == "templates" || currentRoute == "records") Destination.Workouts else Destination.Today
    val navigateRoot: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    var settingsOpen by remember { mutableStateOf(false) }
    var healthMessage by remember { mutableStateOf<String?>(null) }
    var restoreMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val runHealthSync = {
        scope.launch {
            healthMessage = runCatching {
                val count = HealthConnectSync.sync(
                    context,
                    viewModel.workouts.value,
                    viewModel.measurements.value,
                    viewModel.foodEntries.value,
                )
                if (count == 0) "Nothing ready to sync yet." else "$count records synced to Health Connect."
            }.getOrElse { error -> "Health Connect sync failed: ${error.message ?: "Unknown error"}" }
        }
    }
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        if (granted.containsAll(HealthConnectSync.permissions)) runHealthSync()
        else healthMessage = "Health Connect permission was not granted."
    }
    val requestHealthSync: () -> Unit = {
        when (HealthConnectSync.availability(context)) {
            HealthConnectClient.SDK_AVAILABLE -> scope.launch {
                val granted = HealthConnectClient.getOrCreate(context)
                    .permissionController.getGrantedPermissions()
                if (granted.containsAll(HealthConnectSync.permissions)) runHealthSync()
                else healthPermissionLauncher.launch(HealthConnectSync.permissions)
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                healthMessage = "Install or update Health Connect, then try again."
            else -> healthMessage = "Health Connect is unavailable on this device."
        }
        Unit
    }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let { scope.launch { context.contentResolver.openOutputStream(it)?.use { output -> viewModel.backupDatabase(output) } } }
    }
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { scope.launch {
            runCatching {
                context.contentResolver.openInputStream(it)?.use { input -> viewModel.restoreDatabase(input) }
                    ?: error("Could not open selected backup")
            }.onSuccess {
                scheduleAppRestart(context)
            }.onFailure { error ->
                restoreMessage = error.message ?: "Restore failed"
            }
        } }
    }
    val csvLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let {
            val csv = buildString {
                appendLine("type,date,name,value,details")
                viewModel.workouts.value.forEach { row -> appendLine("workout,${row.performedOn},\"${row.name.orEmpty().replace("\"", "\"\"")}\",${row.volumeKg},${row.setCount} sets") }
                viewModel.measurements.value.forEach { row -> appendLine("measurement,${row.measuredOn},weight,${row.weightKg ?: ""},body fat ${row.bodyFatPct ?: ""}") }
                viewModel.foodEntries.value.forEach { row -> appendLine("food,${row.consumedOn},\"${row.name.replace("\"", "\"\"")}\",${row.calories ?: ""},protein ${row.proteinG ?: ""}") }
            }
            context.contentResolver.openOutputStream(it)?.bufferedWriter()?.use { writer -> writer.write(csv) }
        }
    }
    val jsonLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            val root = JSONObject().put("exportedAt", System.currentTimeMillis())
                .put("workouts", JSONArray(viewModel.workouts.value.map { row -> JSONObject().put("date", row.performedOn).put("name", row.name).put("sets", row.setCount).put("volumeKg", row.volumeKg) }))
                .put("measurements", JSONArray(viewModel.measurements.value.map { row -> JSONObject().put("date", row.measuredOn).put("weightKg", row.weightKg).put("bodyFatPct", row.bodyFatPct).put("waistCm", row.waistCm) }))
                .put("foods", JSONArray(viewModel.foodEntries.value.map { row -> JSONObject().put("date", row.consumedOn).put("name", row.name).put("calories", row.calories).put("proteinG", row.proteinG).put("carbsG", row.carbsG).put("fatG", row.fatG) }))
            context.contentResolver.openOutputStream(it)?.bufferedWriter()?.use { writer -> writer.write(root.toString(2)) }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            if (!isStandaloneScreen) {
            TopAppBar(
                title = { Text(current.label, fontSize = 16.sp) },
                actions = {
                    IconButton(
                        onClick = { navController.navigate("settings") },
                        modifier = Modifier.padding(end = 12.dp).size(44.dp),
                    ) {
                        Icon(Icons.Outlined.Settings, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
            }
        },
        bottomBar = {
            if (!isStandaloneScreen) {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                Destination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = { if (currentRoute != destination.route) navigateRoot(destination.route) },
                        icon = { Icon(destination.icon, null) },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Clay,
                            indicatorColor = ClayMuted,
                        ),
                    )
                }
            }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Today.route,
            modifier = Modifier.padding(padding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None },
        ) {
            composable(Destination.Today.route) {
                TodayScreen(viewModel, navigateRoot, settings)
            }
            composable(Destination.Workouts.route) {
                WorkoutsScreen(
                    viewModel,
                    onOpenWorkout = { id -> navController.navigate("workout/$id") },
                    onTemplates = { navController.navigate("templates") },
                    onRecords = { navController.navigate("records") },
                )
            }
            composable("templates") {
                TemplatesScreen(viewModel, onBack = { navController.popBackStack() }) { id -> navController.navigate("workout/$id") }
            }
            composable("records") {
                RecordsScreen(viewModel, onBack = { navController.popBackStack() }) { id -> navController.navigate("exercise/$id") }
            }
            composable("settings") {
                SettingsScreen(
                    themeMode = themeMode,
                    onThemeModeChange = onThemeModeChange,
                    onBack = { navController.popBackStack() },
                    onGoals = { settingsOpen = true },
                    onHealthSync = requestHealthSync,
                    onExportCsv = { csvLauncher.launch("jimvro-export.csv") },
                    onExportJson = { jsonLauncher.launch("jimvro-export.json") },
                    onBackup = { backupLauncher.launch("jimvro-backup.db") },
                    onRestore = { restoreLauncher.launch(arrayOf("application/octet-stream", "application/x-sqlite3", "*/*")) },
                )
            }
            composable(
                route = "exercise/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                ExerciseProgressScreen(
                    viewModel = viewModel,
                    exerciseId = entry.arguments?.getLong("id") ?: return@composable,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = "workout/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                WorkoutTrackerScreen(
                    viewModel = viewModel,
                    workoutId = entry.arguments?.getLong("id") ?: return@composable,
                    settings = settings,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Destination.Body.route) { BodyScreen(viewModel, settings) }
            composable(Destination.Food.route) { FoodScreen(viewModel, settings) }
        }
    }
    if (settingsOpen) SettingsSheet(settings, { settingsOpen = false }) {
        onSettingsChange(it)
        settingsOpen = false
    }
    healthMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { healthMessage = null },
            title = { Text("Health Connect") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { healthMessage = null }) { Text("Done") } },
        )
    }
    restoreMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { restoreMessage = null },
            title = { Text("Restore failed") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { restoreMessage = null }) { Text("Done") } },
        )
    }
}

private fun scheduleAppRestart(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        ?: return
    val pendingIntent = PendingIntent.getActivity(
        context,
        701,
        launchIntent,
        PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
    val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarm.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 350, pendingIntent)
    android.os.Process.killProcess(android.os.Process.myPid())
}

@Composable
internal fun Page(
    eyebrow: String,
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(eyebrow.uppercase(), color = Clay, fontSize = 10.sp, letterSpacing = 1.5.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(title, style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(6.dp))
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
                action?.invoke()
            }
        }
        item { Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content) }
    }
}

@Composable
private fun HeaderAddButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(42.dp),
        shape = RoundedCornerShape(9.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface,
        ),
        contentPadding = PaddingValues(horizontal = 14.dp),
    ) {
        Icon(Icons.Outlined.Add, null, Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp)
    }
}

@Composable
internal fun JournalCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(9.dp), content = content)
    }
}

@Composable
private fun TodayScreen(viewModel: AppViewModel, onNavigate: (String) -> Unit, settings: AppSettings) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val foods by viewModel.foodEntries.collectAsStateWithLifecycle()
    val date = today()
    val todayFoods = foods.filter { it.consumedOn == date }
    val calories = todayFoods.sumOf { it.calories ?: 0.0 }
    val protein = todayFoods.sumOf { it.proteinG ?: 0.0 }
    val locale = LocalConfiguration.current.primaryLocale()
    val todayWorkouts = workouts.filter { it.performedOn == date }
    val setCount = todayWorkouts.sumOf { it.setCount }
    val volume = todayWorkouts.sumOf { it.volumeKg }
    val weeklyVolume = remember(workouts) { weeklyVolumePoints(workouts) }
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Good morning."; hour < 18 -> "Good afternoon."; else -> "Good evening." }
    val dashedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)
    var showMeasurement by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    SimpleDateFormat("EEEE, d MMMM", locale).format(Date()).uppercase(locale),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 2.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(greeting, fontSize = 38.sp, fontWeight = FontWeight.Light, lineHeight = 42.sp, letterSpacing = (-0.6).sp)
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.FitnessCenter, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Training", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(if (volume > 0) volume.pretty() else setCount.toString(), fontSize = 44.sp, fontWeight = FontWeight.Light, lineHeight = 48.sp)
                                Text(if (volume > 0) "kg moved" else if (setCount == 0) "sets — rest day" else "sets", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 7.dp))
                            }
                            if (todayWorkouts.isNotEmpty()) Text("${todayWorkouts.size} session${if (todayWorkouts.size == 1) "" else "s"} today", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(
                            onClick = { onNavigate(Destination.Workouts.route) },
                            modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.onSurface, CircleShape),
                        ) {
                            Icon(Icons.Outlined.ArrowOutward, "Open workouts", tint = MaterialTheme.colorScheme.surface)
                        }
                    }
            }
        }
        item {
            JournalCard {
                Text("Weekly volume", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (weeklyVolume.any { it.value > 0 }) {
                    Text("${weeklyVolume.last().value.pretty()} kg this week", fontSize = 13.sp)
                    VolumeBarChart(weeklyVolume, Modifier.padding(top = 6.dp))
                } else {
                    Text("No tonnage logged yet", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Log weighted sets to build your weekly trend.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            JournalCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Restaurant, null, Modifier.size(17.dp))
                        Text("Nutrition", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onNavigate(Destination.Food.route) }) { Icon(Icons.Outlined.ArrowOutward, "Open food") }
                }
                if (todayFoods.isEmpty()) {
                    Text("—", fontSize = 30.sp)
                    Text("Nothing logged yet", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MetricInline(calories.pretty(), "kcal")
                        Box(Modifier.width(1.dp).height(34.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)))
                        MetricInline(protein.pretty(), "g protein")
                    }
                    Text("${calories.pretty()} / ${settings.calorieTarget} kcal · ${protein.pretty()} / ${settings.proteinTarget}g protein", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        item {
            JournalCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Scale, null, Modifier.size(17.dp))
                        Text("Body weight", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { onNavigate(Destination.Body.route) }) { Icon(Icons.Outlined.ArrowOutward, "Open body") }
                }
                Text(measurements.firstOrNull()?.weightKg?.pretty() ?: "—", fontSize = 30.sp)
                Text(if (measurements.isEmpty()) "No measurements yet" else "kg · latest reading", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("QUICK LOG", fontSize = 11.sp, letterSpacing = 1.6.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickLogButton("Workout") { onNavigate(Destination.Workouts.route) }
                    QuickLogButton("Food") { onNavigate(Destination.Food.route) }
                    QuickLogButton("Measurement") { showMeasurement = true }
                }
            }
        }
    }
    if (showMeasurement) {
        MeasurementDialog(settings, onDismiss = { showMeasurement = false }) {
            viewModel.addMeasurement(it)
            showMeasurement = false
        }
    }
}

private fun weeklyVolumePoints(workouts: List<WorkoutSummary>): List<ChartPoint> {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val labeler = SimpleDateFormat("d MMM", Locale.US)
    val current = java.util.Calendar.getInstance().apply {
        firstDayOfWeek = java.util.Calendar.MONDAY
        set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY)
        set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        add(java.util.Calendar.WEEK_OF_YEAR, -5)
    }
    return (0 until 6).map { offset ->
        val start = current.clone() as java.util.Calendar
        start.add(java.util.Calendar.WEEK_OF_YEAR, offset)
        val end = start.clone() as java.util.Calendar
        end.add(java.util.Calendar.DAY_OF_YEAR, 7)
        ChartPoint(
            label = labeler.format(start.time),
            value = workouts.filter { workout -> parser.parse(workout.performedOn)?.let { !it.before(start.time) && it.before(end.time) } == true }.sumOf { it.volumeKg },
        )
    }
}

@Composable
private fun MetricInline(value: String, unit: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(value, fontSize = 28.sp)
        Text(unit, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
    }
}

@Composable
private fun QuickLogButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
    ) { Icon(Icons.Outlined.Add, null, Modifier.size(15.dp), tint = Clay); Spacer(Modifier.width(5.dp)); Text(label, fontSize = 13.sp) }
}

@Composable
private fun WorkoutsScreen(
    viewModel: AppViewModel,
    onOpenWorkout: (Long) -> Unit,
    onTemplates: () -> Unit,
    onRecords: () -> Unit,
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Long?>(null) }
    Page(
        "Training",
        "Workouts",
        "Each session holds the sets you logged that day.",
        action = { HeaderAddButton("New") { showAdd = true } },
    ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("LIBRARY", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                WorkoutMenuRow("Templates", "Reusable training plans", onTemplates)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
                WorkoutMenuRow("Personal records", "Best weight by exercise", onRecords)
            }
            Text("RECENT SESSIONS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (workouts.isEmpty()) EmptyState("No workouts yet", "Log your first training session.")
            workouts.forEachIndexed { index, workout ->
                Column(Modifier.fillMaxWidth().clickable { onOpenWorkout(workout.id) }.padding(vertical = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(workout.name ?: "Workout")
                            Text(workout.performedOn, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                        IconButton(onClick = { pendingDelete = workout.id }) {
                            Icon(Icons.Outlined.Delete, "Delete workout")
                        }
                    }
                    Text(
                        buildString {
                            append("${workout.setCount} sets  ·  ${workout.volumeKg.pretty()} kg volume")
                            workout.finishedAt?.let { append("  ·  ${((it - workout.createdAt).coerceAtLeast(0) / 1_000).compactDuration()}") }
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (index < workouts.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
            }
    }
    if (showAdd) WorkoutDialog(onDismiss = { showAdd = false }) { workout ->
        viewModel.addWorkout(workout)
        showAdd = false
    }
    pendingDelete?.let { id -> ConfirmDeleteDialog("Delete workout?", "Sets in this workout will also be removed.", { pendingDelete = null }) { viewModel.deleteWorkout(id); pendingDelete = null } }
}

@Composable
private fun WorkoutMenuRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Outlined.ArrowOutward, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun BodyScreen(viewModel: AppViewModel, settings: AppSettings) {
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val progressPhotos by viewModel.progressPhotos.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var selectedTrend by remember { mutableStateOf("Weight") }
    var pendingMeasurement by remember { mutableStateOf<MeasurementEntity?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            val stored = withContext(Dispatchers.IO) {
                val directory = File(context.filesDir, "progress_photos").apply { mkdirs() }
                val file = File(directory, "${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input -> file.outputStream().use(input::copyTo) }
                file.takeIf(File::exists)?.absolutePath
            }
            stored?.let { viewModel.addProgressPhoto(ProgressPhotoEntity(capturedOn = today(), uri = it)) }
        }
    }
    Page(
        "Measurements",
        "Body",
        "Weight, body fat, and tape measurements over time.",
        action = { HeaderAddButton("Add") { showAdd = true } },
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Progress photos", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { photoPicker.launch("image/*") }) { Icon(Icons.Outlined.Add, null); Text("Photo") }
        }
        if (progressPhotos.isEmpty()) {
            Text("Private photos stay on this device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                progressPhotos.forEach { photo ->
                    val bitmap = remember(photo.uri) { android.graphics.BitmapFactory.decodeFile(photo.uri)?.asImageBitmap() }
                    Box(Modifier.width(128.dp).height(164.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        bitmap?.let { Image(it, "Progress photo ${photo.capturedOn}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
                        IconButton(onClick = { viewModel.deleteProgressPhoto(photo); File(photo.uri).delete() }, modifier = Modifier.align(Alignment.TopEnd)) {
                            Icon(Icons.Outlined.Delete, "Delete photo", tint = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(photo.capturedOn, Modifier.align(Alignment.BottomStart).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)).padding(6.dp), fontSize = 10.sp)
                    }
                }
            }
        }
        if (measurements.isEmpty()) {
            EmptyState("No measurements yet", "Add a weigh-in to start your trend.")
        } else {
            val latest = measurements.first()
            val trendPoints = remember(measurements, selectedTrend, settings) { bodyTrendPoints(measurements, selectedTrend, settings) }
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                listOf("Weight", "Body fat", "Waist", "Arms", "Thighs").forEach { metric ->
                    FilterChip(selected = selectedTrend == metric, onClick = { selectedTrend = metric }, label = { Text(metric) })
                }
            }
            if (trendPoints.size >= 2) {
                Column(
                    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text("$selectedTrend trend", fontSize = 13.sp)
                    LineTrendChart(trendPoints)
                    val delta = trendPoints.last().value - trendPoints.first().value
                    Text("${if (delta > 0) "+" else ""}${delta.pretty()} across ${trendPoints.size} readings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("LATEST", fontSize = 10.sp, letterSpacing = 1.4.sp, color = Clay)
                        Text(latest.measuredOn, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { pendingMeasurement = latest }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Outlined.Delete, "Delete latest measurement", Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    BodyStat(latest.weightKg?.displayWeight(settings)?.pretty() ?: "—", settings.weightUnit, "Weight", Modifier.weight(1f))
                    BodyStat(latest.bodyFatPct?.pretty() ?: "—", "%", "Body fat", Modifier.weight(1f))
                    BodyStat(latest.waistCm?.displayLength(settings)?.pretty() ?: "—", settings.lengthUnit, "Waist", Modifier.weight(1f))
                }
                if (listOf(latest.leftArmCm, latest.rightArmCm, latest.leftThighCm, latest.rightThighCm).any { it != null }) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        MeasurementPair("Arms", latest.leftArmCm, latest.rightArmCm, settings)
                        MeasurementPair("Thighs", latest.leftThighCm, latest.rightThighCm, settings)
                    }
                }
                latest.notes?.takeIf(String::isNotBlank)?.let {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Text(it, fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (measurements.size > 1) {
                Text("HISTORY", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column {
                    measurements.drop(1).forEachIndexed { index, measurement ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(measurement.measuredOn, fontSize = 13.sp)
                                Text(
                                    listOfNotNull(
                                        measurement.bodyFatPct?.let { "${it.pretty()}% fat" },
                                        measurement.waistCm?.let { "${it.pretty()} cm waist" },
                                        measurement.leftArmCm?.let { "L arm ${it.pretty()}" },
                                        measurement.rightArmCm?.let { "R arm ${it.pretty()}" },
                                        measurement.leftThighCm?.let { "L thigh ${it.pretty()}" },
                                        measurement.rightThighCm?.let { "R thigh ${it.pretty()}" },
                                    ).joinToString("  ·  ").ifBlank { "General measurement" },
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(measurement.weightKg?.let { "${it.displayWeight(settings).pretty()} ${settings.weightUnit}" } ?: "—", fontSize = 16.sp)
                            IconButton(onClick = { pendingMeasurement = measurement }, modifier = Modifier.size(38.dp)) {
                                Icon(Icons.Outlined.Delete, "Delete measurement", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        if (index < measurements.size - 2) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    }
                }
            }
        }
    }
    if (showAdd) MeasurementDialog(settings, onDismiss = { showAdd = false }) {
        viewModel.addMeasurement(it)
        showAdd = false
    }
    pendingMeasurement?.let { value -> ConfirmDeleteDialog("Delete measurement?", "This reading will be removed from trends.", { pendingMeasurement = null }) { viewModel.deleteMeasurement(value); pendingMeasurement = null } }
}

private fun bodyTrendPoints(measurements: List<MeasurementEntity>, metric: String, settings: AppSettings): List<ChartPoint> =
    measurements.asReversed().mapNotNull { measurement ->
        val value = when (metric) {
            "Weight" -> measurement.weightKg
            "Body fat" -> measurement.bodyFatPct
            "Waist" -> measurement.waistCm
            "Arms" -> listOfNotNull(measurement.leftArmCm, measurement.rightArmCm).takeIf { it.isNotEmpty() }?.average()
            "Thighs" -> listOfNotNull(measurement.leftThighCm, measurement.rightThighCm).takeIf { it.isNotEmpty() }?.average()
            else -> null
        }
        value?.let { raw -> ChartPoint(measurement.measuredOn.takeLast(5), if (metric == "Weight") raw.displayWeight(settings) else if (metric != "Body fat") raw.displayLength(settings) else raw) }
    }

@Composable
private fun BodyStat(value: String, unit: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(value, fontSize = 27.sp, fontWeight = FontWeight.Light)
            if (value != "—") Text(unit, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
        }
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun MeasurementPair(label: String, left: Double?, right: Double?, settings: AppSettings) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("L  ${left?.displayLength(settings)?.pretty() ?: "—"} ${settings.lengthUnit}", fontSize = 12.sp)
        Spacer(Modifier.width(18.dp))
        Text("R  ${right?.displayLength(settings)?.pretty() ?: "—"} ${settings.lengthUnit}", fontSize = 12.sp)
    }
}

@Composable
private fun FoodScreen(viewModel: AppViewModel, settings: AppSettings) {
    val foods by viewModel.foodEntries.collectAsStateWithLifecycle()
    val savedFoods by viewModel.savedFoods.collectAsStateWithLifecycle()
    val todayEntries = foods.filter { it.consumedOn == today() }
    val todayCalories = todayEntries.sumOf { it.calories ?: 0.0 }
    val todayProtein = todayEntries.sumOf { it.proteinG ?: 0.0 }
    val nutritionDays = remember(foods) {
        foods.groupBy { it.consumedOn }.toSortedMap().entries.toList().takeLast(7).map { (date, entries) ->
            date to (entries.sumOf { it.calories ?: 0.0 } to entries.sumOf { it.proteinG ?: 0.0 })
        }
    }
    var showAdd by remember { mutableStateOf(false) }
    var scannedProduct by remember { mutableStateOf<BarcodeProductEntity?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingFood by remember { mutableStateOf<FoodEntryEntity?>(null) }
    var reuseFood by remember { mutableStateOf<SavedFoodEntity?>(null) }
    var pendingSavedFood by remember { mutableStateOf<SavedFoodEntity?>(null) }
    val activity = LocalActivity.current ?: return
    val scope = rememberCoroutineScope()
    val scanner = remember {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(activity, options)
    }

    Page(
        "Nutrition",
        "Food",
        "Scan a barcode, or log it by hand.",
        action = { HeaderAddButton("Add") { showAdd = true } },
    ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth()) {
                    Text("Today", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Text("${todayCalories.pretty()} / ${settings.calorieTarget} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(progress = { if (settings.calorieTarget > 0) (todayCalories / settings.calorieTarget).toFloat().coerceIn(0f, 1f) else 0f }, modifier = Modifier.fillMaxWidth(), color = Clay)
                Row(Modifier.fillMaxWidth()) {
                    Text("Protein", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text("${todayProtein.pretty()} / ${settings.proteinTarget} g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                LinearProgressIndicator(progress = { if (settings.proteinTarget > 0) (todayProtein / settings.proteinTarget).toFloat().coerceIn(0f, 1f) else 0f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (nutritionDays.size >= 2) {
                JournalCard {
                    Text("7-day nutrition", fontSize = 14.sp)
                    Text("Calories", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LineTrendChart(nutritionDays.map { ChartPoint(it.first.takeLast(5), it.second.first) })
                    Text("Protein", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    LineTrendChart(nutritionDays.map { ChartPoint(it.first.takeLast(5), it.second.second) }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Button(
                onClick = {
                    scanner.startScan()
                        .addOnSuccessListener { barcode ->
                            barcode.rawValue?.let { code ->
                                scope.launch {
                                    viewModel.lookupBarcode(code)
                                        .onSuccess { scannedProduct = it }
                                        .onFailure { message = it.message ?: "Product lookup failed" }
                                }
                            }
                        }
                        .addOnFailureListener { message = it.message ?: "Scanner unavailable" }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
            ) { Icon(Icons.Outlined.QrCodeScanner, null); Spacer(Modifier.width(8.dp)); Text("Scan barcode") }
            if (savedFoods.isNotEmpty()) {
                Text("SAVED FOODS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                savedFoods.forEach { saved ->
                    Row(
                        Modifier.fillMaxWidth().clickable { reuseFood = saved }.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(saved.name, style = MaterialTheme.typography.bodyMedium)
                            Text("${(saved.calories ?: 0.0).pretty()} kcal · ${(saved.proteinG ?: 0.0).pretty()}g protein", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(Icons.Outlined.Add, "Log again", Modifier.size(18.dp), tint = Clay)
                        IconButton(onClick = { pendingSavedFood = saved }, modifier = Modifier.size(38.dp)) {
                            Icon(Icons.Outlined.Delete, "Remove saved food", Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (foods.isEmpty()) EmptyState("No food logged", "Add manually or scan a packaged food.")
            foods.groupBy { it.consumedOn }.forEach { (date, entries) ->
                Text(date)
                entries.forEachIndexed { index, food ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(food.name)
                                Text(food.meal.replaceFirstChar(Char::uppercase), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                            Text("${(food.calories ?: 0.0).pretty()} kcal")
                            IconButton(onClick = { pendingFood = food }) { Icon(Icons.Outlined.Delete, "Delete food") }
                        }
                        Text("P ${(food.proteinG ?: 0.0).pretty()}g  ·  C ${(food.carbsG ?: 0.0).pretty()}g  ·  F ${(food.fatG ?: 0.0).pretty()}g", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (index < entries.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
                }
            }
    }
    if (showAdd) FoodDialog(product = null, savedFood = null, onDismiss = { showAdd = false }) { food, save ->
        viewModel.addFood(food, save); showAdd = false
    }
    scannedProduct?.let { product ->
        FoodDialog(product = product, savedFood = null, onDismiss = { scannedProduct = null }) { food, save ->
            viewModel.addFood(food, save); scannedProduct = null
        }
    }
    reuseFood?.let { saved ->
        FoodDialog(product = null, savedFood = saved, onDismiss = { reuseFood = null }) { food, _ ->
            viewModel.addFood(food); reuseFood = null
        }
    }
    pendingFood?.let { value -> ConfirmDeleteDialog("Delete food?", "This entry will be removed from today’s totals.", { pendingFood = null }) { viewModel.deleteFood(value); pendingFood = null } }
    pendingSavedFood?.let { value -> ConfirmDeleteDialog("Remove saved food?", "Logged food entries will stay unchanged.", { pendingSavedFood = null }) { viewModel.deleteSavedFood(value); pendingSavedFood = null } }
}

@Composable
internal fun ConfirmDeleteDialog(title: String, message: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    JournalCard {
        Text(title, fontSize = 16.sp)
        Text(subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
internal fun AppField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Clay,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FormSheet(
    title: String,
    description: String,
    primaryLabel: String,
    primaryEnabled: Boolean,
    onPrimary: () -> Unit,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, style = MaterialTheme.typography.headlineMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content,
            )
            Button(
                onClick = onPrimary,
                enabled = primaryEnabled,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                ),
            ) { Text(primaryLabel) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(40.dp)) { Text("Cancel") }
        }
    }
}

@Composable
private fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    onGoals: () -> Unit,
    onHealthSync: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onBackup: () -> Unit,
    onRestore: () -> Unit,
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        item {
            TextButton(onClick = onBack, contentPadding = PaddingValues(horizontal = 0.dp)) {
                Icon(Icons.Outlined.ArrowBack, null, Modifier.size(18.dp))
                Spacer(Modifier.width(5.dp))
                Text("Back")
            }
            Spacer(Modifier.height(18.dp))
            Text("PREFERENCES", fontSize = 10.sp, letterSpacing = 1.5.sp, color = Clay)
            Text("Settings", style = MaterialTheme.typography.headlineLarge)
        }
        item {
            SettingsSection("APPEARANCE") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.entries.forEach { mode ->
                        Button(
                            onClick = { onThemeModeChange(mode) },
                            modifier = Modifier.weight(1f).height(42.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (themeMode == mode) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface,
                                contentColor = if (themeMode == mode) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface,
                            ),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                        ) { Text(mode.name.lowercase().replaceFirstChar(Char::uppercase), fontSize = 12.sp) }
                    }
                }
                SettingsActionRow("Goals & units", "Weight, measurements, nutrition, and rest timer", onGoals)
            }
        }
        item {
            SettingsSection("HEALTH") {
                SettingsActionRow("Sync Health Connect", "Export finished workouts, weight, body fat, and nutrition", onHealthSync)
            }
        }
        item {
            SettingsSection("YOUR DATA") {
                SettingsActionRow("Export CSV", "Readable spreadsheet export", onExportCsv)
                SettingsActionRow("Export JSON", "Portable structured export", onExportJson)
                SettingsActionRow("Backup database", "Complete local backup", onBackup)
                SettingsActionRow("Restore database", "Replace local data from backup", onRestore)
            }
        }
    }
}

@Composable
private fun SettingsSection(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        content()
    }
}

@Composable
private fun SettingsActionRow(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, fontSize = 16.sp)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Outlined.ArrowOutward, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
}

@Composable
private fun SettingsSheet(settings: AppSettings, onDismiss: () -> Unit, onSave: (AppSettings) -> Unit) {
    var calories by remember(settings) { mutableStateOf(settings.calorieTarget.toString()) }
    var protein by remember(settings) { mutableStateOf(settings.proteinTarget.toString()) }
    var rest by remember(settings) { mutableStateOf(settings.restSeconds.toString()) }
    var weightUnit by remember(settings) { mutableStateOf(settings.weightUnit) }
    var lengthUnit by remember(settings) { mutableStateOf(settings.lengthUnit) }
    FormSheet("Goals & units", "Used across nutrition, body measurements, and workouts.", "Save settings", true, {
        onSave(AppSettings(weightUnit, lengthUnit, calories.toIntOrNull()?.coerceAtLeast(0) ?: 0, protein.toIntOrNull()?.coerceAtLeast(0) ?: 0, rest.toIntOrNull()?.coerceIn(15, 600) ?: 90))
    }, onDismiss) {
        Text("UNITS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("kg", "lb").forEach { unit ->
                Button(onClick = { weightUnit = unit }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (weightUnit == unit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface, contentColor = if (weightUnit == unit) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)) { Text(unit) }
            }
            listOf("cm", "in").forEach { unit ->
                Button(onClick = { lengthUnit = unit }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (lengthUnit == unit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface, contentColor = if (lengthUnit == unit) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)) { Text(unit) }
            }
        }
        Text("DAILY GOALS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(calories, { calories = it.filter(Char::isDigit) }, "Calories", Modifier.weight(1f))
            AppField(protein, { protein = it.filter(Char::isDigit) }, "Protein (g)", Modifier.weight(1f))
        }
        AppField(rest, { rest = it.filter(Char::isDigit) }, "Rest timer (seconds)")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(value: String, onChange: (String) -> Unit) {
    var pickerOpen by remember { mutableStateOf(false) }
    val selectedMillis = remember(value) {
        runCatching {
            LocalDate.parse(value).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull()
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .clickable { pickerOpen = true }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatDateForDisplay(value), fontSize = 16.sp)
        }
        Icon(Icons.Outlined.CalendarMonth, "Choose date", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (pickerOpen) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = selectedMillis)
        DatePickerDialog(
            onDismissRequest = { pickerOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onChange(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString())
                    }
                    pickerOpen = false
                }) { Text("Choose") }
            },
            dismissButton = { TextButton(onClick = { pickerOpen = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }
}

private fun formatDateForDisplay(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()))
}.getOrDefault(value)

@Composable
private fun WorkoutDialog(
    onDismiss: () -> Unit,
    onSave: (WorkoutEntity) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    FormSheet(
        title = "New session",
        description = "Name it, set the date, then log your sets inside.",
        primaryLabel = "Add session",
        primaryEnabled = date.isNotBlank(),
        onPrimary = { onSave(WorkoutEntity(performedOn = date, name = name.ifBlank { null })) },
        onDismiss = onDismiss,
    ) {
        AppField(name, { name = it }, "Session name")
        DateField(date) { date = it }
    }
}

@Composable
private fun MeasurementDialog(settings: AppSettings, onDismiss: () -> Unit, onSave: (MeasurementEntity) -> Unit) {
    var weight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var leftArm by remember { mutableStateOf("") }
    var rightArm by remember { mutableStateOf("") }
    var leftThigh by remember { mutableStateOf("") }
    var rightThigh by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    FormSheet(
        title = "New measurement",
        description = "Log only what you measured today. Every field is optional.",
        primaryLabel = "Add measurement",
        primaryEnabled = date.isNotBlank() && listOf(weight, bodyFat, waist, leftArm, rightArm, leftThigh, rightThigh).any(String::isNotBlank),
        onPrimary = {
            onSave(
                MeasurementEntity(
                    measuredOn = date,
                    weightKg = weight.toDoubleOrNull()?.storageWeight(settings),
                    bodyFatPct = bodyFat.toDoubleOrNull(),
                    waistCm = waist.toDoubleOrNull()?.storageLength(settings),
                    leftArmCm = leftArm.toDoubleOrNull()?.storageLength(settings),
                    rightArmCm = rightArm.toDoubleOrNull()?.storageLength(settings),
                    leftThighCm = leftThigh.toDoubleOrNull()?.storageLength(settings),
                    rightThighCm = rightThigh.toDoubleOrNull()?.storageLength(settings),
                ),
            )
        },
        onDismiss = onDismiss,
    ) {
        DateField(date) { date = it }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(weight, { weight = it }, "Weight (${settings.weightUnit})", Modifier.weight(1f))
            AppField(bodyFat, { bodyFat = it }, "Body fat (%)", Modifier.weight(1f))
        }
        AppField(waist, { waist = it }, "Waist (${settings.lengthUnit})")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(leftArm, { leftArm = it }, "Left arm (${settings.lengthUnit})", Modifier.weight(1f))
            AppField(rightArm, { rightArm = it }, "Right arm (${settings.lengthUnit})", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(leftThigh, { leftThigh = it }, "Left thigh (${settings.lengthUnit})", Modifier.weight(1f))
            AppField(rightThigh, { rightThigh = it }, "Right thigh (${settings.lengthUnit})", Modifier.weight(1f))
        }
    }
}

@Composable
private fun FoodDialog(product: BarcodeProductEntity?, savedFood: SavedFoodEntity?, onDismiss: () -> Unit, onSave: (FoodEntryEntity, Boolean) -> Unit) {
    val defaultServing = product?.servingG ?: 100.0
    var name by remember(product, savedFood) { mutableStateOf(savedFood?.name ?: product?.name.orEmpty()) }
    var serving by remember(product) { mutableStateOf(defaultServing.pretty()) }
    var calories by remember(product, savedFood) { mutableStateOf(savedFood?.calories?.pretty() ?: product?.caloriesPer100g?.times(defaultServing / 100)?.pretty().orEmpty()) }
    var protein by remember(product, savedFood) { mutableStateOf(savedFood?.proteinG?.pretty() ?: product?.proteinPer100g?.times(defaultServing / 100)?.pretty().orEmpty()) }
    var carbs by remember(product, savedFood) { mutableStateOf(savedFood?.carbsG?.pretty() ?: product?.carbsPer100g?.times(defaultServing / 100)?.pretty().orEmpty()) }
    var fat by remember(product, savedFood) { mutableStateOf(savedFood?.fatG?.pretty() ?: product?.fatPer100g?.times(defaultServing / 100)?.pretty().orEmpty()) }
    var date by remember { mutableStateOf(today()) }
    var saveForReuse by remember { mutableStateOf(false) }
    FormSheet(
        title = when { savedFood != null -> "Log saved food"; product == null -> "Log food"; else -> "Review scanned food" },
        description = if (savedFood != null) "Adjust anything you need, then add it to your day." else if (product == null) "Add a meal and its macros." else "Check the serving and nutrition before saving.",
        primaryLabel = "Add food",
        primaryEnabled = name.isNotBlank(),
        onPrimary = { onSave(FoodEntryEntity(consumedOn = date, name = name.trim(), calories = calories.toDoubleOrNull(), proteinG = protein.toDoubleOrNull(), carbsG = carbs.toDoubleOrNull(), fatG = fat.toDoubleOrNull(), barcode = product?.barcode), saveForReuse) },
        onDismiss = onDismiss,
    ) {
            AppField(name, { name = it }, "Food")
            DateField(date) { date = it }
            if (product != null) AppField(serving, { value ->
                serving = value
                value.toDoubleOrNull()?.let { grams ->
                    val scaled = scaleMacros(
                        Macros(product.caloriesPer100g, product.proteinPer100g, product.carbsPer100g, product.fatPer100g),
                        grams,
                    )
                    calories = scaled.calories?.pretty().orEmpty()
                    protein = scaled.proteinG?.pretty().orEmpty()
                    carbs = scaled.carbsG?.pretty().orEmpty()
                    fat = scaled.fatG?.pretty().orEmpty()
                }
            }, "Serving (g)")
            Text("MACROS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppField(calories, { calories = it }, "Calories", Modifier.weight(1f))
                AppField(protein, { protein = it }, "Protein (g)", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppField(carbs, { carbs = it }, "Carbs (g)", Modifier.weight(1f))
                AppField(fat, { fat = it }, "Fat (g)", Modifier.weight(1f))
            }
            if (savedFood == null) {
                Row(
                    Modifier.fillMaxWidth().clickable { saveForReuse = !saveForReuse }.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Save for reuse", fontSize = 15.sp)
                        Text("Keep name and macros in saved foods", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = saveForReuse, onCheckedChange = { saveForReuse = it })
                }
            }
    }
}
