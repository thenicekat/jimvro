package com.divyateja.jimvro.ui

import android.Manifest
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.ComponentActivity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.content.res.Configuration
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.MimeTypeMap
import android.view.WindowManager
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.RotateLeft
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Schedule
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.divyateja.jimvro.AppViewModel
import com.divyateja.jimvro.JimvroApplication
import com.divyateja.jimvro.HealthConnectSync
import com.divyateja.jimvro.data.BarcodeProductEntity
import com.divyateja.jimvro.data.ExerciseEntity
import com.divyateja.jimvro.data.FoodPhotoEstimate
import com.divyateja.jimvro.data.LEGACY_MUSCLE_GROUPS
import com.divyateja.jimvro.data.MUSCLE_GROUPS
import com.divyateja.jimvro.data.FoodEntryEntity
import com.divyateja.jimvro.data.MeasurementEntity
import com.divyateja.jimvro.data.ProgressPhotoEntity
import com.divyateja.jimvro.data.SavedFoodEntity
import com.divyateja.jimvro.data.WorkoutEntity
import com.divyateja.jimvro.data.WorkoutSummary
import com.divyateja.jimvro.data.WorkoutSetEntity
import com.divyateja.jimvro.domain.Macros
import com.divyateja.jimvro.domain.NutritionGuidance
import com.divyateja.jimvro.domain.NutritionGoal
import com.divyateja.jimvro.domain.nutritionGuidance
import com.divyateja.jimvro.domain.scaleMacros
import com.divyateja.jimvro.domain.weeklyScore
import com.divyateja.jimvro.ui.theme.Clay
import com.divyateja.jimvro.ui.theme.ThemeMode
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

private enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Outlined.Home),
    Workouts("workouts", "Workouts", Icons.Outlined.FitnessCenter),
    Body("body", "Body", Icons.Outlined.Scale),
    Food("food", "Food", Icons.Outlined.Restaurant),
}

private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
private fun Double.pretty(): String = if (this % 1.0 == 0.0) "%.0f".format(this) else "%.1f".format(this)
private fun Long.compactDuration(): String = if (this >= 3600) "%dh %02dm".format(this / 3600, (this % 3600) / 60) else "%dm".format(this / 60)
private val progressPhotoPoses = listOf("Front", "Side", "Back", "Other")

internal fun ProgressPhotoEntity.poseLabel(): String? =
    notes?.substringBefore(" · ")?.takeIf { it in progressPhotoPoses }

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
    val isStandaloneScreen = currentRoute.startsWith("workout/") || currentRoute in setOf("settings", "calendar")
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
    var nutritionPlanOpen by remember { mutableStateOf(false) }
    var geminiKeyOpen by remember { mutableStateOf(false) }
    var healthMessage by remember { mutableStateOf<String?>(null) }
    var restoreMessage by remember { mutableStateOf<String?>(null) }
    var enableAutoHealthSyncAfterPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val healthWorkouts by viewModel.workouts.collectAsStateWithLifecycle()
    val healthMeasurements by viewModel.measurements.collectAsStateWithLifecycle()
    val healthFoods by viewModel.foodEntries.collectAsStateWithLifecycle()
    val catalogExercises by viewModel.exercises.collectAsStateWithLifecycle()
    var deferredLegacyExerciseId by remember { mutableStateOf<Long?>(null) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    LaunchedEffect(Unit) {
        if (
            settings.bodyRemindersEnabled && Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    val runHealthSync = {
        scope.launch {
            healthMessage = runCatching {
                val count = HealthConnectSync.sync(
                    context,
                    healthWorkouts,
                    healthMeasurements,
                    healthFoods,
                )
                if (count == 0) "Nothing ready to sync yet." else "$count records synced to Health Connect."
            }.getOrElse { error -> "Health Connect sync failed: ${error.message ?: "Unknown error"}" }
        }
    }
    val healthPermissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
    ) { granted ->
        if (granted.containsAll(HealthConnectSync.permissions)) {
            if (enableAutoHealthSyncAfterPermission) {
                enableAutoHealthSyncAfterPermission = false
                onSettingsChange(settings.copy(healthConnectAutoSync = true))
            } else runHealthSync()
        }
        else {
            enableAutoHealthSyncAfterPermission = false
            healthMessage = "Health Connect permission was not granted."
        }
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
    val setAutoHealthSync: (Boolean) -> Unit = { enabled ->
        if (!enabled) onSettingsChange(settings.copy(healthConnectAutoSync = false))
        else when (HealthConnectSync.availability(context)) {
            HealthConnectClient.SDK_AVAILABLE -> scope.launch {
                val granted = HealthConnectClient.getOrCreate(context)
                    .permissionController.getGrantedPermissions()
                if (granted.containsAll(HealthConnectSync.permissions)) onSettingsChange(settings.copy(healthConnectAutoSync = true))
                else {
                    enableAutoHealthSyncAfterPermission = true
                    healthPermissionLauncher.launch(HealthConnectSync.permissions)
                }
            }
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                healthMessage = "Install or update Health Connect, then try again."
            else -> healthMessage = "Health Connect is unavailable on this device."
        }
    }
    LaunchedEffect(settings.healthConnectAutoSync, healthWorkouts, healthMeasurements, healthFoods) {
        if (settings.healthConnectAutoSync && HealthConnectSync.availability(context) == HealthConnectClient.SDK_AVAILABLE) {
            val client = HealthConnectClient.getOrCreate(context)
            if (client.permissionController.getGrantedPermissions().containsAll(HealthConnectSync.permissions)) {
                runCatching { HealthConnectSync.sync(context, healthWorkouts, healthMeasurements, healthFoods) }
            }
        }
    }
    val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        uri?.let { scope.launch { context.contentResolver.openOutputStream(it)?.use { output -> viewModel.backupDatabase(output) } } }
    }
    val activity = LocalActivity.current
    val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { scope.launch {
            runCatching {
                context.contentResolver.openInputStream(it)?.use { input -> viewModel.restoreDatabase(input) }
                    ?: error("Could not open selected backup")
            }.onSuccess {
                (context.applicationContext as JimvroApplication).reloadAfterRestore()
                (activity as? ComponentActivity)?.viewModelStore?.clear()
                activity?.recreate()
            }.onFailure { error ->
                restoreMessage = error.message ?: "Restore failed"
            }
        } }
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
                        selected = current == destination,
                        onClick = {
                            if (current == destination) navController.popBackStack(destination.route, inclusive = false)
                            else navigateRoot(destination.route)
                        },
                        icon = { Icon(destination.icon, null) },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
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
                TodayScreen(viewModel, navigateRoot, settings, onOpenWorkout = { id -> navController.navigate("workout/$id") })
            }
            composable(Destination.Workouts.route) {
                WorkoutsScreen(
                    viewModel,
                    onOpenWorkout = { id -> navController.navigate("workout/$id") },
                    onTemplates = { navController.navigate("templates") },
                    onRecords = { navController.navigate("records") },
                    onCalendar = { navController.navigate("calendar") },
                )
            }
            composable("calendar") {
                WorkoutCalendarScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenWorkout = { id -> navController.navigate("workout/$id") },
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
                    onFoodPlan = { nutritionPlanOpen = true },
                    foodPlanSummary = when {
                        settings.nutritionGoal.isNotBlank() -> "${settings.nutritionGoal.replaceFirstChar(Char::uppercase)} · ${settings.calorieTarget} kcal · ${settings.proteinTarget} g protein"
                        settings.nutritionGoalsConfirmed -> "Review your daily calorie and protein goals"
                        else -> "Choose cut, recomp, or bulk"
                    },
                    onGeminiKey = { geminiKeyOpen = true },
                    geminiKeyConfigured = settings.geminiApiKey.isNotBlank(),
                    onHealthSync = requestHealthSync,
                    healthConnectAutoSync = settings.healthConnectAutoSync,
                    onHealthConnectAutoSync = setAutoHealthSync,
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
            composable(Destination.Food.route) {
                FoodScreen(viewModel, settings, onOpenNutritionPlan = { nutritionPlanOpen = true }, onOpenPhotoSettings = { geminiKeyOpen = true })
            }
        }
    }
    if (settingsOpen) SettingsSheet(settings, { settingsOpen = false }) {
        onSettingsChange(it)
        if (
            (it.proteinRemindersEnabled || it.bodyRemindersEnabled) && Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        settingsOpen = false
    }
    if (nutritionPlanOpen) NutritionPlanSheet(settings, healthMeasurements, { nutritionPlanOpen = false }) { plan ->
        onSettingsChange(plan)
        if (plan.proteinRemindersEnabled && Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        nutritionPlanOpen = false
    }
    if (geminiKeyOpen) GeminiKeySheet(settings, { geminiKeyOpen = false }) {
        onSettingsChange(it)
        geminiKeyOpen = false
    }
    healthMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { healthMessage = null },
            title = { Text("Health Connect") },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { healthMessage = null }) { Text("Done") } },
        )
    }
    catalogExercises.firstOrNull { it.sourceId == null && it.muscleGroup in LEGACY_MUSCLE_GROUPS && it.id != deferredLegacyExerciseId }?.let { exercise ->
        AlertDialog(
            onDismissRequest = { deferredLegacyExerciseId = exercise.id },
            title = { Text("Categorize ${exercise.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("This existing exercise uses the old ${exercise.muscleGroup} category. Choose its specific group; workouts and templates will update automatically.")
                    MUSCLE_GROUPS.filter { it != "other" }.chunked(3).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { group ->
                                FilterChip(selected = false, onClick = {
                                    viewModel.updateMuscleGroup(exercise.id, group)
                                    deferredLegacyExerciseId = null
                                }, label = { Text(group.replaceFirstChar(Char::uppercase)) })
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { deferredLegacyExerciseId = exercise.id }) { Text("Later") } },
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


@Composable
internal fun Page(
    eyebrow: String,
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null,
    showHeader: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        if (showHeader) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f)) {
                        Text(eyebrow.uppercase(), color = MaterialTheme.colorScheme.secondary, fontSize = 10.sp, letterSpacing = 1.5.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(title, style = MaterialTheme.typography.headlineLarge)
                        if (subtitle.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    action?.invoke()
                }
            }
        } else if (action != null) item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) { action() }
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
private fun TodayScreen(viewModel: AppViewModel, onNavigate: (String) -> Unit, settings: AppSettings, onOpenWorkout: (Long) -> Unit) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val foods by viewModel.foodEntries.collectAsStateWithLifecycle()
    var date by remember { mutableStateOf(today()) }
    LaunchedEffect(Unit) { while (isActive) { delay(60_000); date = today() } }
    val weeklyMuscleSets by remember(date) { viewModel.weeklyMuscleSets(date) }.collectAsStateWithLifecycle(emptyList())
    val todayFoods = foods.filter { it.consumedOn == date }
    val nutrition = foodDayMetrics(foods, date)
    val calories = nutritionTotal(nutrition.calories, nutrition.entryCount, nutrition.missingCalories)
    val protein = nutritionTotal(nutrition.proteinG, nutrition.entryCount, nutrition.missingProtein)
    val locale = LocalConfiguration.current.primaryLocale()
    val todayWorkouts = workouts.filter { it.performedOn == date }
    val volume = todayWorkouts.sumOf { it.volumeKg }
    val weeklyVolume = remember(workouts) { weeklyVolumePoints(workouts) }
    val weeklyScore = remember(date, workouts, foods, measurements, settings) {
        weeklyScore(LocalDate.parse(date), workouts, foods, measurements, settings.weeklyTrainingTarget, settings.proteinTarget, settings.calorieTarget)
    }
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Good morning."; hour < 18 -> "Good afternoon."; else -> "Good evening." }
    var showMeasurement by remember { mutableStateOf(false) }
    var showWorkout by rememberSaveable { mutableStateOf(false) }
    val activeWorkout = workouts.filter { it.finishedAt == null }.maxByOrNull { it.createdAt }
    val openTraining: () -> Unit = {
        if (activeWorkout != null) onOpenWorkout(activeWorkout.id) else showWorkout = true
    }

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
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = openTraining, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp), shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Outlined.FitnessCenter, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (activeWorkout == null) "Start workout" else "Resume workout")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickLogButton("Log food") { onNavigate(Destination.Food.route) }
                    QuickLogButton("Measurement") { showMeasurement = true }
                }
            }
        }
        item {
            val start = runCatching { LocalDate.parse(settings.missionStartDate) }.getOrNull()
            val end = runCatching { LocalDate.parse(settings.missionEndDate) }.getOrNull()
            JournalCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("4 MONTH MISSION", fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(
                            if (start != null && end != null) "Week ${(ChronoUnit.WEEKS.between(start, LocalDate.parse(date)) + 1).coerceAtLeast(1)} / ${(ChronoUnit.WEEKS.between(start, end) + 1).coerceAtLeast(1)}" else "Set your goal and dates",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = { onNavigate("settings") }) { Text(if (start == null || end == null) "Set up" else "Edit") }
                }
                if (start != null && end != null) {
                    val currentWeight = measurements.firstOrNull { it.weightKg != null }?.weightKg
                    Text(
                        listOfNotNull(
                            currentWeight?.displayWeight(settings)?.let { "${it.pretty()} ${settings.weightUnit}" },
                            settings.goalWeightKg?.displayWeight(settings)?.let { "→ ${it.pretty()} ${settings.weightUnit}" },
                        ).joinToString("  ").ifBlank { "Add a weigh-in to start the trend" },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                    )
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("This week", Modifier.weight(1f), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${weeklyScore.score}/100", fontSize = 20.sp)
                    }
                    LinearProgressIndicator(progress = { weeklyScore.score / 100f }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondary)
                    Text(
                        "Training ${weeklyScore.trainingDone}/${weeklyScore.trainingTarget}  ·  Protein ${weeklyScore.proteinDays}/${weeklyScore.daysElapsed}  ·  Calories ${weeklyScore.calorieDays}/${weeklyScore.daysElapsed}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    weeklyScore.weeklyWeightChangeKg?.let { change ->
                        val status = if (change in -0.35..-0.15) "Ideal cut rate" else if (change < -0.35) "Losing quickly" else "Weight trend ${if (change >= 0) "+" else ""}${change.pretty()} kg/week"
                        Text(if (status.startsWith("Weight trend")) "Weight trend ${if (change >= 0) "+" else ""}${change.displayWeight(settings).pretty()} ${settings.weightUnit}/week" else status, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
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
                                Text(volume.displayWeight(settings).pretty(), fontSize = 44.sp, fontWeight = FontWeight.Light, lineHeight = 48.sp)
                                Text("${settings.weightUnit} moved", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 7.dp))
                            }
                            if (todayWorkouts.isNotEmpty()) Text("${todayWorkouts.size} session${if (todayWorkouts.size == 1) "" else "s"} today", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(onClick = { onNavigate(Destination.Workouts.route) }) { Text("History") }
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
        if (weeklyMuscleSets.isNotEmpty()) item {
            JournalCard {
                Text("Muscles this week", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val maxVolume = weeklyMuscleSets.maxOf { it.volumeKg }.coerceAtLeast(1.0)
                weeklyMuscleSets.forEach { entry ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            entry.muscleGroup.replaceFirstChar(Char::uppercase),
                            Modifier.width(90.dp),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Box(
                            Modifier
                                .weight(1f)
                                .height(6.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(3.dp))
                        ) {
                            Box(
                                Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth((entry.volumeKg / maxVolume).toFloat().coerceIn(0f, 1f))
                                    .background(Clay, RoundedCornerShape(3.dp))
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${entry.volumeKg.pretty()} kg", fontSize = 12.sp)
                            Text("${entry.setCount} sets", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
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
                        MetricInline(calories, "kcal")
                        Box(Modifier.width(1.dp).height(34.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)))
                        MetricInline(protein, "g protein")
                    }
                    Text(
                        "$calories${if (settings.nutritionGoalsConfirmed && settings.calorieTarget > 0) " / ${settings.calorieTarget}" else ""} kcal · " +
                            "$protein${if (settings.nutritionGoalsConfirmed && settings.proteinTarget > 0) " / ${settings.proteinTarget}" else ""}g protein",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                val latestWeight = measurements.firstOrNull { it.weightKg != null }?.weightKg
                Text(latestWeight?.displayWeight(settings)?.pretty() ?: "—", fontSize = 30.sp)
                Text(if (latestWeight == null) "No weigh-ins yet" else "${settings.weightUnit} · latest reading", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    if (showWorkout) WorkoutDialog(onDismiss = { showWorkout = false }) { value ->
        showWorkout = false
        viewModel.addWorkout(value, onCreated = onOpenWorkout)
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
    ) { Icon(Icons.Outlined.Add, null, Modifier.size(15.dp), tint = MaterialTheme.colorScheme.secondary); Spacer(Modifier.width(5.dp)); Text(label, fontSize = 13.sp) }
}

@Composable
private fun WorkoutsScreen(
    viewModel: AppViewModel,
    onOpenWorkout: (Long) -> Unit,
    onTemplates: () -> Unit,
    onRecords: () -> Unit,
    onCalendar: () -> Unit,
) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Long?>(null) }
    Page(
        "Training",
        "Workouts",
        "Each session holds the sets you logged that day.",
        action = { HeaderAddButton("Start workout") { showAdd = true } },
        showHeader = false,
    ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("LIBRARY", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                WorkoutMenuRow("Templates", "Reusable training plans", onTemplates)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
                WorkoutMenuRow("Personal records", "Best weight by exercise", onRecords)
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
                WorkoutMenuRow("Calendar", "Monthly training history", onCalendar)
            }
            Text("RECENT SESSIONS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (workouts.isEmpty()) EmptyState("No workouts yet", "Log your first training session.")
            workouts.forEachIndexed { index, workout ->
                Column(Modifier.fillMaxWidth().clickable { onOpenWorkout(workout.id) }.padding(vertical = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(workout.name ?: "Workout")
                            Text(formatDateForDisplay(workout.performedOn), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            if (workout.finishedAt == null) Text("In progress · Resume", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelMedium)
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
        viewModel.addWorkout(workout, onCreated = onOpenWorkout)
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
    var importMessage by remember { mutableStateOf<String?>(null) }
    var selectedTrend by remember { mutableStateOf("Weight") }
    var pendingMeasurement by remember { mutableStateOf<MeasurementEntity?>(null) }
    var photoError by remember { mutableStateOf<String?>(null) }
    var pendingPhotoPath by remember { mutableStateOf<String?>(null) }
    var selectedPhoto by remember { mutableStateOf<ProgressPhotoEntity?>(null) }
    var comparePhotos by remember { mutableStateOf(false) }
    var comparisonPose by remember { mutableStateOf<String?>(null) }
    var photoRevision by remember { mutableStateOf(0) }
    var photosUnlocked by remember { mutableStateOf(false) }
    var photoAuthError by remember { mutableStateOf<String?>(null) }
    var showFullHistory by remember { mutableStateOf(false) }
    val photoPickerInFlight = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = LocalActivity.current as? FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val inBodyImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            importMessage = runCatching {
                context.contentResolver.openInputStream(uri)?.use { viewModel.importInBodyCsv(it) }
                    ?: error("Could not open the selected file")
            }.fold(
                onSuccess = { if (it == 0) "No new InBody readings found." else "$it InBody readings imported." },
                onFailure = { it.message ?: "Could not import InBody CSV" },
            )
        }
    }
    val photoAuthenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK or
        BiometricManager.Authenticators.DEVICE_CREDENTIAL
    val biometricPrompt = remember(activity) {
        activity?.let { host ->
            BiometricPrompt(
                host,
                ContextCompat.getMainExecutor(context),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        photosUnlocked = true
                        photoAuthError = null
                    }

                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_CANCELED) {
                            photoAuthError = errString.toString()
                        }
                    }
                },
            )
        }
    }
    val unlockPhotos = {
        if (activity == null || biometricPrompt == null) {
            photoAuthError = "Device authentication is unavailable"
        } else {
            when (BiometricManager.from(context).canAuthenticate(photoAuthenticators)) {
                BiometricManager.BIOMETRIC_SUCCESS -> biometricPrompt.authenticate(
                    BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Unlock progress photos")
                        .setSubtitle("Use your device screen lock")
                        .setAllowedAuthenticators(photoAuthenticators)
                        .build(),
                )
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> photoAuthError = "Set up a device PIN, pattern, password, or biometric first."
                else -> photoAuthError = "Secure device authentication is unavailable."
            }
        }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (shouldRelockPhotos(event, photoPickerInFlight.value)) {
                photosUnlocked = false
                selectedPhoto = null
                comparePhotos = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    DisposableEffect(photosUnlocked, activity) {
        if (photosUnlocked) activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose {
            if (photosUnlocked) activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        photoPickerInFlight.value = false
        uri ?: return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                withContext(Dispatchers.IO) { importProgressPhoto(context, uri) }
            }.onSuccess { path ->
                pendingPhotoPath = path
                photoError = null
            }.onFailure { error ->
                photoError = error.message ?: "Could not import photo"
            }
        }
    }
    val launchPhotoPicker = {
        photoError = null
        photoPickerInFlight.value = true
        runCatching {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }.onFailure { error ->
            photoPickerInFlight.value = false
            photoError = error.message ?: "Photo picker is unavailable"
        }
        Unit
    }
    Page(
        "Measurements",
        "Body",
        "Weight, body fat, and tape measurements over time.",
        action = {
            TextButton(onClick = { inBodyImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values")) }) { Text("Import InBody") }
            HeaderAddButton("Add") { showAdd = true }
        },
        showHeader = false,
    ) {
        importMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        if (!photosUnlocked) {
            Row(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.secondary)
                Column(Modifier.weight(1f).padding(start = 10.dp)) {
                    Text("Progress photos", style = MaterialTheme.typography.titleMedium)
                    Text("Locked", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = unlockPhotos) { Text("Unlock") }
                photoAuthError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            }
        } else {
            Text("PROGRESS PHOTOS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            photoError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
            if (progressPhotos.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("Build a visual timeline", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Use the same pose, distance, and lighting every few weeks. Photos stay private on this device.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = launchPhotoPicker) { Icon(Icons.Outlined.Add, null); Text("Add first photo") }
                }
            } else {
                val latestPhoto = progressPhotos.first()
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Latest", style = MaterialTheme.typography.titleMedium)
                        Text(formatDateForDisplay(latestPhoto.capturedOn), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = launchPhotoPicker) { Icon(Icons.Outlined.Add, null); Text("Add") }
                }
                StoredProgressPhoto(
                    latestPhoto,
                    Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(12.dp)).clickable { selectedPhoto = latestPhoto },
                    maxWidth = 1200,
                    maxHeight = 1280,
                    revision = photoRevision,
                )
                latestPhoto.notes?.takeIf(String::isNotBlank)?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val comparablePoses = progressPhotoPoses.filter { pose -> progressPhotos.count { it.poseLabel() == pose } >= 2 }
                if (comparablePoses.isNotEmpty()) {
                    val selectedComparisonPose = comparisonPose?.takeIf { it in comparablePoses } ?: comparablePoses.first()
                    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        comparablePoses.forEach { pose ->
                            FilterChip(selected = selectedComparisonPose == pose, onClick = { comparisonPose = pose }, label = { Text(pose) })
                        }
                    }
                    Button(onClick = { comparisonPose = selectedComparisonPose; comparePhotos = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Compare ${selectedComparisonPose.lowercase()} photos")
                    }
                } else {
                    Text("Add two photos with the same pose to compare them.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("TIMELINE", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    progressPhotos.forEach { photo ->
                        ProgressPhotoCard(photo, photoRevision) { selectedPhoto = photo }
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
                listOf("Weight", "Body fat", "Muscle", "Fat mass", "Waist", "Arms", "Thighs").forEach { metric ->
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
                        Text("LATEST", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.secondary)
                        Text(formatDateForDisplay(latest.measuredOn), fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                if (listOf(latest.skeletalMuscleKg, latest.bodyFatMassKg, latest.basalMetabolicRateKcal, latest.visceralFatLevel).any { it != null }) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Text(
                        listOfNotNull(
                            latest.skeletalMuscleKg?.let { "${it.pretty()} kg muscle" },
                            latest.bodyFatMassKg?.let { "${it.pretty()} kg fat mass" },
                            latest.basalMetabolicRateKcal?.let { "${it.pretty()} kcal BMR" },
                            latest.visceralFatLevel?.let { "Visceral fat ${it.pretty()}" },
                            latest.inBodyScore?.let { "InBody score ${it.pretty()}" },
                        ).joinToString("  ·  "),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
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
                    val history = measurements.drop(1).let { if (showFullHistory) it else it.take(4) }
                    history.forEachIndexed { index, measurement ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(formatDateForDisplay(measurement.measuredOn), fontSize = 13.sp)
                                Text(
                                    listOfNotNull(
                                        measurement.bodyFatPct?.let { "${it.pretty()}% fat" },
                                        measurement.skeletalMuscleKg?.let { "${it.pretty()} kg muscle" },
                                        measurement.bodyFatMassKg?.let { "${it.pretty()} kg fat mass" },
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
                        if (index < history.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    }
                }
                if (measurements.size > 5) {
                    TextButton(onClick = { showFullHistory = !showFullHistory }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (showFullHistory) "Show less" else "Show all ${measurements.size - 1} readings")
                    }
                }
            }
        }
    }
    if (showAdd) MeasurementDialog(settings, onDismiss = { showAdd = false }) {
        viewModel.addMeasurement(it)
        showAdd = false
    }
    if (photosUnlocked) {
        pendingPhotoPath?.let { path ->
            ProgressPhotoDetailsDialog(
                path = path,
                onDismiss = {
                    File(path).delete()
                    pendingPhotoPath = null
                },
            ) { date, notes ->
                viewModel.addProgressPhoto(ProgressPhotoEntity(capturedOn = date, uri = path, notes = notes))
                pendingPhotoPath = null
            }
        }
        selectedPhoto?.let { photo ->
            ProgressPhotoViewer(
                photo = photo,
                onDismiss = { selectedPhoto = null },
            onDelete = {
                    viewModel.deleteProgressPhoto(photo)
                    File(photo.uri).delete()
                selectedPhoto = null
            },
            onRotated = { photoRevision++ },
            )
        }
        val matchingPhotos = comparisonPose?.let { pose -> progressPhotos.filter { it.poseLabel() == pose } }.orEmpty()
        if (comparePhotos && matchingPhotos.size >= 2) {
            ProgressPhotoComparison(matchingPhotos.last(), matchingPhotos.first(), photoRevision) { comparePhotos = false }
        }
    }
    pendingMeasurement?.let { value -> ConfirmDeleteDialog("Delete measurement?", "This reading will be removed from trends.", { pendingMeasurement = null }) { viewModel.deleteMeasurement(value); pendingMeasurement = null } }
}

internal fun shouldRelockPhotos(event: Lifecycle.Event, photoPickerInFlight: Boolean): Boolean =
    event == Lifecycle.Event.ON_STOP && !photoPickerInFlight

private fun importProgressPhoto(context: Context, source: android.net.Uri): String {
    val directory = File(context.filesDir, "progress_photos")
    check(directory.exists() || directory.mkdirs()) { "Could not create photo storage" }
    val extension = context.contentResolver.getType(source)
        ?.let(MimeTypeMap.getSingleton()::getExtensionFromMimeType)
        ?.takeIf(String::isNotBlank)
        ?: "img"
    val destination = File(directory, "${UUID.randomUUID()}.$extension")
    try {
        val input = requireNotNull(context.contentResolver.openInputStream(source)) { "Could not open selected photo" }
        input.use { sourceStream -> destination.outputStream().use(sourceStream::copyTo) }
        check(destination.length() > 0) { "Selected photo was empty" }
        check(decodeSampledBitmap(destination.absolutePath, 32, 32) != null) { "Selected file is not a supported image" }
        return destination.absolutePath
    } catch (error: Throwable) {
        destination.delete()
        throw error
    }
}

@Composable
private fun StoredProgressPhoto(
    photo: ProgressPhotoEntity,
    modifier: Modifier,
    maxWidth: Int = 768,
    maxHeight: Int = 984,
    revision: Int = 0,
) {
    var bitmap by remember(photo.uri, revision) { mutableStateOf<ImageBitmap?>(null) }
    var loaded by remember(photo.uri, revision) { mutableStateOf(false) }
    LaunchedEffect(photo.uri, revision) {
        bitmap = withContext(Dispatchers.IO) {
            decodeSampledBitmap(photo.uri, maxWidth, maxHeight)?.asImageBitmap()
        }
        loaded = true
    }
    Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant)) {
        bitmap?.let { Image(it, "Progress photo ${photo.capturedOn}", Modifier.fillMaxSize(), contentScale = ContentScale.Crop) }
        if (loaded && bitmap == null) {
            Text(
                "Photo unavailable",
                Modifier.align(Alignment.Center).padding(12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ProgressPhotoCard(photo: ProgressPhotoEntity, revision: Int, onClick: () -> Unit) {
    Column(Modifier.width(112.dp).clickable(onClick = onClick), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        StoredProgressPhoto(photo, Modifier.fillMaxWidth().height(142.dp).clip(RoundedCornerShape(8.dp)), revision = revision)
        Text(formatDateShort(photo.capturedOn), fontSize = 11.sp)
        photo.notes?.takeIf(String::isNotBlank)?.let {
            Text(it.substringBefore(" · "), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}

@Composable
private fun ProgressPhotoDetailsDialog(
    path: String,
    onDismiss: () -> Unit,
    onSave: (String, String?) -> Unit,
) {
    var date by remember { mutableStateOf(today()) }
    var pose by remember { mutableStateOf("Front") }
    var note by remember { mutableStateOf("") }
    var revision by remember { mutableStateOf(0) }
    var rotateError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val rotate: (Float) -> Unit = { degrees ->
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { rotateProgressPhoto(path, degrees) } }
                .onSuccess { revision++; rotateError = null }
                .onFailure { rotateError = it.message ?: "Could not rotate photo" }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add progress photo") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                StoredProgressPhoto(
                    ProgressPhotoEntity(capturedOn = date, uri = path),
                    Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(10.dp)),
                    revision = revision,
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    TextButton(onClick = { rotate(-90f) }) { Icon(Icons.Outlined.RotateLeft, null); Text("Left") }
                    TextButton(onClick = { rotate(90f) }) { Icon(Icons.Outlined.RotateRight, null); Text("Right") }
                }
                rotateError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                DateField(date) { date = it }
                Text("Pose", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    progressPhotoPoses.forEach { option ->
                        FilterChip(selected = pose == option, onClick = { pose = option }, label = { Text(option) })
                    }
                }
                AppField(note, { note = it }, "Optional note")
                Text("Tip: keep pose, distance, and lighting consistent.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(date, listOf(pose, note.trim()).filter(String::isNotBlank).joinToString(" · ").ifBlank { null })
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun ProgressPhotoViewer(photo: ProgressPhotoEntity, onDismiss: () -> Unit, onDelete: () -> Unit, onRotated: () -> Unit) {
    var revision by remember(photo.uri) { mutableStateOf(0) }
    var rotateError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val rotate: (Float) -> Unit = { degrees ->
        scope.launch {
            runCatching { withContext(Dispatchers.IO) { rotateProgressPhoto(photo.uri, degrees) } }
                .onSuccess { revision++; onRotated(); rotateError = null }
                .onFailure { rotateError = it.message ?: "Could not rotate photo" }
        }
    }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().fillMaxHeight(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(formatDateForDisplay(photo.capturedOn), style = MaterialTheme.typography.titleMedium)
                        photo.notes?.takeIf(String::isNotBlank)?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                StoredProgressPhoto(
                    photo,
                    Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)),
                    maxWidth = 1600,
                    maxHeight = 2200,
                    revision = revision,
                )
                rotateError?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { rotate(-90f) }) { Icon(Icons.Outlined.RotateLeft, null); Text("Left") }
                    TextButton(onClick = { rotate(90f) }) { Icon(Icons.Outlined.RotateRight, null); Text("Right") }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, null)
                        Text("Delete")
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressPhotoComparison(first: ProgressPhotoEntity, latest: ProgressPhotoEntity, revision: Int, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxWidth().fillMaxHeight(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Progress comparison", style = MaterialTheme.typography.headlineMedium)
                        Text("First and latest", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onDismiss) { Text("Close") }
                }
                Row(Modifier.fillMaxWidth().weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ComparisonPhoto("FIRST", first, revision, Modifier.weight(1f))
                    ComparisonPhoto("LATEST", latest, revision, Modifier.weight(1f))
                }
                Text("Best compared with matching pose, distance, and lighting.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ComparisonPhoto(label: String, photo: ProgressPhotoEntity, revision: Int, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(label, fontSize = 10.sp, letterSpacing = 1.3.sp, color = MaterialTheme.colorScheme.secondary)
        StoredProgressPhoto(
            photo,
            Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(10.dp)),
            maxWidth = 900,
            maxHeight = 1600,
            revision = revision,
        )
        Text(formatDateShort(photo.capturedOn), fontSize = 12.sp)
        photo.notes?.takeIf(String::isNotBlank)?.let { Text(it, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2) }
    }
}

private fun decodeSampledBitmap(path: String, maxWidth: Int, maxHeight: Int): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
    var sampleSize = 1
    while (bounds.outWidth / sampleSize > maxWidth * 2 || bounds.outHeight / sampleSize > maxHeight * 2) {
        sampleSize *= 2
    }
    return BitmapFactory.decodeFile(path, BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.RGB_565
    })
}

internal fun rotateProgressPhoto(path: String, degrees: Float) {
    val sourceFile = File(path)
    check(sourceFile.isFile) { "Photo file is unavailable" }
    val source = checkNotNull(decodeSampledBitmap(path, 2400, 2400)) { "Photo could not be decoded" }
    val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, Matrix().apply { postRotate(degrees) }, true)
    val temporary = File(sourceFile.parentFile, "${sourceFile.name}.rotating")
    try {
        val format = if (sourceFile.extension.equals("png", ignoreCase = true)) Bitmap.CompressFormat.PNG else Bitmap.CompressFormat.JPEG
        temporary.outputStream().use { output ->
            check(rotated.compress(format, 95, output)) { "Photo could not be saved" }
        }
        runCatching {
            Files.move(
                temporary.toPath(),
                sourceFile.toPath(),
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE,
            )
        }.getOrElse {
            Files.move(temporary.toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    } finally {
        temporary.delete()
        if (rotated !== source) rotated.recycle()
        source.recycle()
    }
}

private fun bodyTrendPoints(measurements: List<MeasurementEntity>, metric: String, settings: AppSettings): List<ChartPoint> =
    measurements.asReversed().mapNotNull { measurement ->
        val value = when (metric) {
            "Weight" -> measurement.weightKg
            "Body fat" -> measurement.bodyFatPct
            "Muscle" -> measurement.skeletalMuscleKg
            "Fat mass" -> measurement.bodyFatMassKg
            "Waist" -> measurement.waistCm
            "Arms" -> listOfNotNull(measurement.leftArmCm, measurement.rightArmCm).takeIf { it.isNotEmpty() }?.average()
            "Thighs" -> listOfNotNull(measurement.leftThighCm, measurement.rightThighCm).takeIf { it.isNotEmpty() }?.average()
            else -> null
        }
        value?.let { raw -> ChartPoint(formatDateShort(measurement.measuredOn), if (metric == "Weight") raw.displayWeight(settings) else if (metric in listOf("Waist", "Arms", "Thighs")) raw.displayLength(settings) else raw) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodDaySelector(date: String, currentDate: String, onChange: (String) -> Unit) {
    var pickerOpen by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange(LocalDate.parse(date).minusDays(1).toString()) }) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Previous day")
            }
            TextButton(onClick = { pickerOpen = true }, modifier = Modifier.weight(1f)) {
                Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(17.dp))
                Spacer(Modifier.width(6.dp))
                Text(formatDateForDisplay(date), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(
                onClick = { onChange(LocalDate.parse(date).plusDays(1).toString()) },
                enabled = date < currentDate,
            ) { Icon(Icons.AutoMirrored.Outlined.ArrowForward, "Next day") }
        }
        if (date != currentDate) {
            TextButton(onClick = { onChange(currentDate) }, modifier = Modifier.align(Alignment.End)) {
                Text("Back to today")
            }
        }
    }
    if (pickerOpen) {
        val selectedMillis = LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
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

@Composable
private fun FoodCaptureButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.heightIn(min = 80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

@Composable
private fun FoodAdvice(guide: NutritionGuidance, goalName: String) {
    val mealAmounts = listOfNotNull(
        guide.nextMealCalories?.let { "$it kcal" },
        guide.nextMealProteinG?.let { "$it g protein" },
    ).joinToString(" · ")
    if (mealAmounts.isEmpty() && guide.mealsLeft > 0 && !guide.incompleteLog &&
        guide.caloriesLeft != 0 && guide.proteinLeftG != 0) return

    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        val goal = NutritionGoal.entries.firstOrNull { it.name.equals(goalName, ignoreCase = true) }
        Text(goal?.let { "${it.label} food guide" } ?: "Food guide", style = MaterialTheme.typography.titleSmall)
        when {
            mealAmounts.isNotEmpty() && !guide.hasLoggedFood -> Text(
                "Starting point from your daily goals: about $mealAmounts per meal. Log food to tailor this.",
                style = MaterialTheme.typography.bodySmall,
            )
            mealAmounts.isNotEmpty() -> Text(
                "About $mealAmounts for your next meal, based on what you've logged.",
                style = MaterialTheme.typography.bodySmall,
            )
            guide.mealsLeft == 0 -> Text(
                "No next-meal suggestion after your meal times. You can still log food you ate.",
                style = MaterialTheme.typography.bodySmall,
            )
            guide.caloriesLeft == 0 && (guide.proteinLeftG ?: 0) > 0 -> Text(
                "Calorie goal already logged; no extra meal suggested to catch up on protein.",
                style = MaterialTheme.typography.bodySmall,
            )
            !guide.incompleteLog -> Text("Today's logged goals are met.", style = MaterialTheme.typography.bodySmall)
        }
        guide.foodIdea?.let { idea ->
            val portions = idea.portions.pretty()
            Text(
                "Saved idea: $portions × ${idea.name} (${idea.calories} kcal · ${idea.proteinG} g protein).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (guide.incompleteLog) Text(
            "Some entries are missing nutrition values; fill them in for a fuller guide.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FoodScreen(viewModel: AppViewModel, settings: AppSettings, onOpenNutritionPlan: () -> Unit, onOpenPhotoSettings: () -> Unit) {
    val foods by viewModel.foodEntries.collectAsStateWithLifecycle()
    val savedFoods by viewModel.savedFoods.collectAsStateWithLifecycle()
    val currentDate = today()
    var selectedDate by rememberSaveable { mutableStateOf(currentDate) }
    var nowMinutes by remember { mutableStateOf(LocalTime.now().toSecondOfDay() / 60) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            nowMinutes = LocalTime.now().toSecondOfDay() / 60
        }
    }
    val dayEntries = foodEntriesOn(foods, selectedDate)
    val dayMetrics = foodDayMetrics(foods, selectedDate)
    val calorieLabel = nutritionTotal(dayMetrics.calories, dayMetrics.entryCount, dayMetrics.missingCalories)
    val proteinLabel = nutritionTotal(dayMetrics.proteinG, dayMetrics.entryCount, dayMetrics.missingProtein)
    val guide = if (selectedDate == currentDate && settings.nutritionGoalsConfirmed) nutritionGuidance(
        dayEntries, savedFoods, settings.calorieTarget, settings.proteinTarget,
        listOf(settings.breakfastReminderMinutes, settings.lunchReminderMinutes, settings.dinnerReminderMinutes),
        nowMinutes,
    ) else null
    var showAdd by remember { mutableStateOf(false) }
    var scannedProduct by remember { mutableStateOf<BarcodeProductEntity?>(null) }
    var photoEstimate by remember { mutableStateOf<FoodPhotoEstimate?>(null) }
    var photoPreview by remember { mutableStateOf<ImageBitmap?>(null) }
    var estimatingPhoto by remember { mutableStateOf(false) }
    var lookingUpBarcode by remember { mutableStateOf(false) }
    var photoSetupNeeded by remember { mutableStateOf(false) }
    var quickAdding by remember { mutableStateOf(emptySet<Long>()) }
    var reviewingSavedFood by remember { mutableStateOf<SavedFoodEntity?>(null) }
    val snackbar = remember { SnackbarHostState() }
    var cameraCaptureUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingFood by remember { mutableStateOf<FoodEntryEntity?>(null) }
    var pendingSavedFood by remember { mutableStateOf<SavedFoodEntity?>(null) }
    var editingFood by remember { mutableStateOf<FoodEntryEntity?>(null) }
    var editingSavedFood by remember { mutableStateOf<SavedFoodEntity?>(null) }
    val activity = LocalActivity.current ?: return
    val context = LocalContext.current
    val preferences = remember(context) { context.getSharedPreferences("jimvro_settings", Context.MODE_PRIVATE) }
    var onboardingSeen by remember { mutableStateOf(preferences.getBoolean("nutrition_plan_onboarding_seen", false)) }
    LaunchedEffect(settings.nutritionGoalsConfirmed) {
        if (!settings.nutritionGoalsConfirmed && !onboardingSeen) {
            onboardingSeen = true
            preferences.edit().putBoolean("nutrition_plan_onboarding_seen", true).apply()
            onOpenNutritionPlan()
        }
    }
    val scope = rememberCoroutineScope()
    val scanner = remember {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(activity, options)
    }
    val estimateFromUri: (android.net.Uri) -> Unit = { uri ->
        scope.launch {
            estimatingPhoto = true
            message = null
            photoPreview = null
            runCatching {
                val bytes = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                        ?: error("Could not read photo")
                }
                photoPreview = withContext(Dispatchers.IO) {
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                }
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                viewModel.estimateFoodFromPhoto(settings.geminiApiKey, bytes, mime).getOrThrow()
            }.onSuccess {
                photoEstimate = it
            }.onFailure {
                photoPreview = null
                message = it.message ?: "Photo estimate failed"
            }
            estimatingPhoto = false
        }
    }
    val foodPhotoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let(estimateFromUri)
    }
    val takeFoodPhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) cameraCaptureUri?.let(estimateFromUri) else message = "Camera capture cancelled"
    }
    val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            runCatching {
                val file = File(context.cacheDir, "food_capture_${System.currentTimeMillis()}.jpg")
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                cameraCaptureUri = uri
                takeFoodPhoto.launch(uri)
            }.onFailure { message = it.message ?: "Camera unavailable" }
        } else {
            message = "Camera permission is required to take a meal photo"
        }
    }
    val requireGeminiKey: () -> Boolean = {
        if (settings.geminiApiKey.isBlank()) {
            message = "Add a Gemini API key to use photo estimates."
            photoSetupNeeded = true
            false
        } else true
    }
    val launchFoodCamera = {
        if (requireGeminiKey()) {
            when {
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    PackageManager.PERMISSION_GRANTED -> {
                    runCatching {
                        val file = File(context.cacheDir, "food_capture_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                        cameraCaptureUri = uri
                        takeFoodPhoto.launch(uri)
                    }.onFailure { message = it.message ?: "Camera unavailable" }
                }
                else -> cameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
    Page(
        "Nutrition",
        "Food",
        "Tap a regular food once, or add something new.",
        action = { HeaderAddButton("Log food") { showAdd = true } },
        showHeader = false,
    ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                FoodDaySelector(selectedDate, currentDate) { selectedDate = it }
                Row(Modifier.fillMaxWidth()) {
                    Text(if (selectedDate == currentDate) "Today" else "Daily totals", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (settings.nutritionGoalsConfirmed && settings.calorieTarget > 0) "$calorieLabel / ${settings.calorieTarget} kcal"
                        else "$calorieLabel kcal logged",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (settings.nutritionGoalsConfirmed && settings.calorieTarget > 0) LinearProgressIndicator(progress = { (dayMetrics.calories / settings.calorieTarget).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondary)
                Row(Modifier.fillMaxWidth()) {
                    Text("Protein", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                    Text(
                        if (settings.nutritionGoalsConfirmed && settings.proteinTarget > 0) "$proteinLabel / ${settings.proteinTarget} g"
                        else "$proteinLabel g logged",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (settings.nutritionGoalsConfirmed && settings.proteinTarget > 0) LinearProgressIndicator(progress = { (dayMetrics.proteinG / settings.proteinTarget).toFloat().coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Text("Carbs ${nutritionTotal(dayMetrics.carbsG, dayMetrics.entryCount, dayMetrics.missingCarbs)} g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Fat ${nutritionTotal(dayMetrics.fatG, dayMetrics.entryCount, dayMetrics.missingFat)} g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (listOf(dayMetrics.missingCalories, dayMetrics.missingProtein, dayMetrics.missingCarbs, dayMetrics.missingFat).any { it > 0 }) {
                    Text("Some values aren't entered. — means unknown; ≥ totals include entered values only.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (selectedDate != currentDate && settings.nutritionGoalsConfirmed) {
                    Text("Compared with your current goals", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            guide?.let { FoodAdvice(it, settings.nutritionGoal) }
            if (selectedDate == currentDate && settings.nutritionGoal.isBlank()) {
                Column {
                    Text(
                        if (settings.nutritionGoalsConfirmed) "Your saved goals are in use. Choose cut, recomp, or bulk for a guided starting point."
                        else "Pick cut, recomp, or bulk to get a starting food plan.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    TextButton(onClick = onOpenNutritionPlan) { Text("Build a food plan") }
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FoodCaptureButton(
                    label = if (lookingUpBarcode) "Looking up…" else "Barcode",
                    icon = Icons.Outlined.QrCodeScanner,
                    onClick = {
                        message = null
                        photoSetupNeeded = false
                        scanner.startScan()
                            .addOnSuccessListener { barcode ->
                                barcode.rawValue?.let { code ->
                                    scope.launch {
                                        lookingUpBarcode = true
                                        try { viewModel.lookupBarcode(code)
                                            .onSuccess { scannedProduct = it }
                                            .onFailure { message = it.message ?: "Product lookup failed" }
                                        } finally { lookingUpBarcode = false }
                                    }
                                }
                            }
                            .addOnFailureListener { message = it.message ?: "Scanner unavailable" }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !lookingUpBarcode,
                )
                FoodCaptureButton(
                    label = "Camera",
                    icon = Icons.Outlined.PhotoCamera,
                    onClick = launchFoodCamera,
                    enabled = !estimatingPhoto,
                    modifier = Modifier.weight(1f),
                )
                FoodCaptureButton(
                    label = "Gallery",
                    icon = Icons.Outlined.Image,
                    onClick = {
                        if (requireGeminiKey()) {
                            foodPhotoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    },
                    enabled = !estimatingPhoto,
                    modifier = Modifier.weight(1f),
                )
            }
            if (estimatingPhoto) {
                Text("Estimating macros…", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (photoSetupNeeded && settings.geminiApiKey.isBlank()) TextButton(onClick = {
                message = null
                photoSetupNeeded = false
                onOpenPhotoSettings()
            }) { Text("Set up photo estimates") }
            if (savedFoods.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Quick add", style = MaterialTheme.typography.titleMedium)
                    savedFoods.forEach { saved ->
                        var optionsExpanded by remember(saved.id) { mutableStateOf(false) }
                        val summary = listOfNotNull(
                            saved.calories?.let { "${it.pretty()} kcal" },
                            saved.proteinG?.let { "${it.pretty()}g protein" },
                            saved.carbsG?.let { "${it.pretty()}g carbs" },
                            saved.fatG?.let { "${it.pretty()}g fat" },
                        ).take(2).joinToString(" · ").ifBlank { "Macros not set" }
                        Row(
                            Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(saved.name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(summary, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            IconButton(
                                onClick = {
                                    if (!saved.readyForQuickAdd()) reviewingSavedFood = saved
                                    else scope.launch {
                                        quickAdding = quickAdding + saved.id
                                        val added = try {
                                            viewModel.quickAddFood(FoodEntryEntity(consumedOn = selectedDate, name = saved.name, calories = saved.calories, proteinG = saved.proteinG, carbsG = saved.carbsG, fatG = saved.fatG))
                                        } catch (cancel: CancellationException) {
                                            throw cancel
                                        } catch (_: Exception) {
                                            snackbar.showSnackbar("Couldn't add food. Please try again.")
                                            return@launch
                                        } finally { quickAdding = quickAdding - saved.id }
                                        snackbar.currentSnackbarData?.dismiss()
                                        val day = if (added.consumedOn == currentDate) "Today" else formatDateShort(added.consumedOn)
                                        if (snackbar.showSnackbar("Added ${saved.name} to $day", actionLabel = "Undo", withDismissAction = true) == SnackbarResult.ActionPerformed) {
                                            viewModel.deleteFood(added)
                                        }
                                    }
                                },
                                modifier = Modifier.size(48.dp),
                                enabled = saved.id !in quickAdding,
                            ) {
                                Icon(
                                    Icons.Outlined.Add,
                                    "Add ${saved.name} on ${formatDateForDisplay(selectedDate)}",
                                    Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            Box {
                                IconButton(onClick = { optionsExpanded = true }, modifier = Modifier.size(48.dp)) {
                                    Icon(Icons.Outlined.MoreHoriz, "More options for ${saved.name}", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                DropdownMenu(expanded = optionsExpanded, onDismissRequest = { optionsExpanded = false }) {
                                    DropdownMenuItem(text = { Text("Edit saved food") }, onClick = {
                                        optionsExpanded = false
                                        editingSavedFood = saved
                                    })
                                    DropdownMenuItem(text = { Text("Remove saved food", color = MaterialTheme.colorScheme.error) }, onClick = {
                                        optionsExpanded = false
                                        pendingSavedFood = saved
                                    })
                                }
                            }
                        }
                    }
                }
            }
            if (dayEntries.isEmpty()) {
                EmptyState(
                    if (selectedDate == currentDate) "No food logged today" else "No food logged for this day",
                    "Add manually, scan a barcode, or estimate from a photo.",
                )
            } else {
                Text(if (selectedDate == currentDate) "TODAY'S FOOD" else "FOOD ON THIS DAY", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                dayEntries.forEachIndexed { index, food ->
                    Column(Modifier.fillMaxWidth().clickable { editingFood = food }.padding(vertical = 8.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(food.name)
                            }
                            Text(food.calories.knownNutrition()?.let { "${nutritionAmount(it)} kcal" } ?: "Calories not entered", style = MaterialTheme.typography.bodySmall)
                            IconButton(onClick = { pendingFood = food }) { Icon(Icons.Outlined.Delete, "Delete food") }
                        }
                        Text("P ${nutritionAmount(food.proteinG)} g  ·  C ${nutritionAmount(food.carbsG)} g  ·  F ${nutritionAmount(food.fatG)} g", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (index < dayEntries.lastIndex) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f))
                }
            }
    }
    SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            actionColor = MaterialTheme.colorScheme.onSecondaryContainer,
            dismissActionContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
    }
    reviewingSavedFood?.let { saved ->
        FoodDialog(product = null, initialDate = selectedDate, savedFood = saved, onDismiss = { reviewingSavedFood = null }) { food, save ->
            viewModel.addFood(food, save)
            selectedDate = food.consumedOn
            reviewingSavedFood = null
        }
    }
    if (showAdd) FoodDialog(product = null, initialDate = selectedDate, onDismiss = { showAdd = false }) { food, save ->
        viewModel.addFood(food, save); selectedDate = food.consumedOn; showAdd = false
    }
    scannedProduct?.let { product ->
        FoodDialog(product = product, initialDate = selectedDate, onDismiss = { scannedProduct = null }) { food, save ->
            viewModel.addFood(food, save); selectedDate = food.consumedOn; scannedProduct = null
        }
    }
    photoEstimate?.let { estimate ->
        FoodDialog(
            product = null,
            initialDate = selectedDate,
            estimate = estimate,
            photoPreview = photoPreview,
            onDismiss = { photoEstimate = null; photoPreview = null },
        ) { food, save ->
            viewModel.addFood(food, save)
            selectedDate = food.consumedOn
            photoEstimate = null
            photoPreview = null
        }
    }
    editingFood?.let { value ->
        FoodDialog(product = null, initialDate = selectedDate, entry = value, onDismiss = { editingFood = null }) { food, _ ->
            viewModel.updateFood(food); selectedDate = food.consumedOn; editingFood = null
        }
    }
    editingSavedFood?.let { value ->
        SavedFoodDialog(value, onDismiss = { editingSavedFood = null }) { saved ->
            viewModel.updateSavedFood(saved); editingSavedFood = null
        }
    }
    pendingFood?.let { value -> ConfirmDeleteDialog("Delete food?", "This entry will be removed from the totals for ${formatDateForDisplay(value.consumedOn)}.", { pendingFood = null }) { viewModel.deleteFood(value); pendingFood = null } }
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
internal fun AppField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = error != null,
        supportingText = if (error != null) { { Text(error) } } else null,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    allowZero: Boolean = true,
    maximum: Double? = null,
) {
    AppField(value, onValueChange, label, modifier, KeyboardType.Decimal, numberInputError(value, allowZero, maximum))
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
    primaryHeight: Dp = 50.dp,
    dismissLabel: String = "Cancel",
    scrollKey: Int = 0,
    compact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    LaunchedEffect(scrollKey) { scrollState.scrollTo(0) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp),
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .then(if (compact) Modifier.heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.92f) else Modifier.fillMaxHeight(0.92f))
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
                Modifier.weight(1f, fill = !compact).verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                content = content,
            )
            Button(
                onClick = onPrimary,
                enabled = primaryEnabled,
                modifier = Modifier.fillMaxWidth().height(primaryHeight),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                ),
            ) { Text(primaryLabel) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(40.dp)) { Text(dismissLabel) }
        }
    }
}

@Composable
private fun SettingsScreen(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    onBack: () -> Unit,
    onGoals: () -> Unit,
    onFoodPlan: () -> Unit,
    foodPlanSummary: String,
    onGeminiKey: () -> Unit,
    geminiKeyConfigured: Boolean,
    onHealthSync: () -> Unit,
    healthConnectAutoSync: Boolean,
    onHealthConnectAutoSync: (Boolean) -> Unit,
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
            Text("PREFERENCES", fontSize = 10.sp, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.secondary)
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
                SettingsActionRow("Food plan", foodPlanSummary, onFoodPlan)
            }
        }
        item {
            SettingsSection("FOOD PHOTO") {
                SettingsActionRow(
                    "Gemini API key",
                    if (geminiKeyConfigured) "Configured · used only for photo estimates" else "Required to estimate macros from a meal photo",
                    onGeminiKey,
                )
            }
        }
        item {
            SettingsSection("HEALTH") {
                Row(Modifier.fillMaxWidth().padding(vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Auto-sync changes", fontSize = 16.sp)
                        Text("Export new workouts, measurements, and food while Jimvro is open", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = healthConnectAutoSync, onCheckedChange = onHealthConnectAutoSync)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                SettingsActionRow("Sync Health Connect", "Export finished workouts, weight, body fat, and nutrition", onHealthSync)
            }
        }
        item {
            SettingsSection("YOUR DATA") {
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
private fun GeminiKeySheet(settings: AppSettings, onDismiss: () -> Unit, onSave: (AppSettings) -> Unit) {
    var apiKey by remember(settings) { mutableStateOf(settings.geminiApiKey) }
    var reveal by remember { mutableStateOf(false) }
    FormSheet(
        title = "Gemini API key",
        description = "Your key stays on this device. Meal photos are sent to Google Gemini only when you tap Estimate from photo.",
        primaryLabel = "Save key",
        primaryEnabled = true,
        onPrimary = { onSave(settings.copy(geminiApiKey = apiKey.trim())) },
        onDismiss = onDismiss,
    ) {
        OutlinedTextField(
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text("API key") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = if (reveal) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.secondary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
            ),
            trailingIcon = {
                TextButton(onClick = { reveal = !reveal }) { Text(if (reveal) "Hide" else "Show") }
            },
        )
        TextButton(onClick = { apiKey = "" }, enabled = apiKey.isNotBlank()) {
            Text("Clear key")
        }
        Text(
            "Create a key at aistudio.google.com. Prefer a key with low quota and no billing surprises.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SettingsSheet(settings: AppSettings, onDismiss: () -> Unit, onSave: (AppSettings) -> Unit) {
    var calories by remember(settings) { mutableStateOf(if (settings.nutritionGoalsConfirmed) settings.calorieTarget.toString() else "") }
    var protein by remember(settings) { mutableStateOf(if (settings.nutritionGoalsConfirmed) settings.proteinTarget.toString() else "") }
    var rest by remember(settings) { mutableStateOf(settings.restSeconds.toString()) }
    var weightUnit by remember(settings) { mutableStateOf(settings.weightUnit) }
    var lengthUnit by remember(settings) { mutableStateOf(settings.lengthUnit) }
    var remindersEnabled by remember(settings) { mutableStateOf(settings.proteinRemindersEnabled) }
    var bodyRemindersEnabled by remember(settings) { mutableStateOf(settings.bodyRemindersEnabled) }
    var breakfastMinutes by remember(settings) { mutableStateOf(settings.breakfastReminderMinutes) }
    var lunchMinutes by remember(settings) { mutableStateOf(settings.lunchReminderMinutes) }
    var dinnerMinutes by remember(settings) { mutableStateOf(settings.dinnerReminderMinutes) }
    var missionStart by remember(settings) { mutableStateOf(settings.missionStartDate) }
    var missionEnd by remember(settings) { mutableStateOf(settings.missionEndDate) }
    var goalWeight by remember(settings) { mutableStateOf(settings.goalWeightKg?.displayWeight(settings)?.pretty().orEmpty()) }
    var weeklyTraining by remember(settings) { mutableStateOf(settings.weeklyTrainingTarget.toString()) }
    val missionDatesValid = remember(missionStart, missionEnd) {
        if (missionStart.isBlank() && missionEnd.isBlank()) true
        else runCatching { LocalDate.parse(missionEnd) >= LocalDate.parse(missionStart) }.getOrDefault(false)
    }
    FormSheet("Goals & units", "Used across nutrition, body measurements, and workouts.", "Save settings", missionDatesValid, {
        onSave(
            settings.copy(
                weightUnit = weightUnit,
                lengthUnit = lengthUnit,
                calorieTarget = calories.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                proteinTarget = protein.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                nutritionGoalsConfirmed = (calories.toIntOrNull() ?: 0) > 0 || (protein.toIntOrNull() ?: 0) > 0,
                nutritionGoal = if ((calories.toIntOrNull() ?: 0) > 0 || (protein.toIntOrNull() ?: 0) > 0) settings.nutritionGoal else "",
                restSeconds = rest.toIntOrNull()?.coerceIn(15, 600) ?: 90,
                proteinRemindersEnabled = remindersEnabled,
                bodyRemindersEnabled = bodyRemindersEnabled,
                breakfastReminderMinutes = breakfastMinutes,
                lunchReminderMinutes = lunchMinutes,
                dinnerReminderMinutes = dinnerMinutes,
                missionStartDate = missionStart,
                missionEndDate = missionEnd,
                goalWeightKg = goalWeight.toDoubleOrNull()?.takeIf { it > 0 }?.storageWeight(settings.copy(weightUnit = weightUnit)),
                weeklyTrainingTarget = weeklyTraining.toIntOrNull()?.coerceIn(1, 7) ?: 4,
            ),
        )
    }, onDismiss) {
        Text("UNITS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("kg", "lb").forEach { unit ->
                Button(onClick = {
                    val storedGoal = goalWeight.toDoubleOrNull()?.storageWeight(settings.copy(weightUnit = weightUnit))
                    weightUnit = unit
                    goalWeight = storedGoal?.displayWeight(settings.copy(weightUnit = unit))?.pretty().orEmpty()
                }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (weightUnit == unit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface, contentColor = if (weightUnit == unit) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)) { Text(unit) }
            }
            listOf("cm", "in").forEach { unit ->
                Button(onClick = { lengthUnit = unit }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = if (lengthUnit == unit) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface, contentColor = if (lengthUnit == unit) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurface)) { Text(unit) }
            }
        }
        Text("DAILY GOALS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (!settings.nutritionGoalsConfirmed) Text(
            "Build a food plan for a guided starting point, or enter goals you choose here.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(calories, { calories = it.filter(Char::isDigit) }, "Calories", Modifier.weight(1f))
            AppField(protein, { protein = it.filter(Char::isDigit) }, "Protein (g)", Modifier.weight(1f))
        }
        AppField(rest, { rest = it.filter(Char::isDigit) }, "Rest timer (seconds)")
        Text("PROTEIN REMINDERS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            Modifier.fillMaxWidth().clickable { remindersEnabled = !remindersEnabled }.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Meal-time reminders", fontSize = 15.sp)
                Text("Only nudge when logged protein is behind your meal-time pace", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = remindersEnabled, onCheckedChange = { remindersEnabled = it })
        }
        if (remindersEnabled) {
            ReminderTimeField("Breakfast", breakfastMinutes) { breakfastMinutes = it }
            ReminderTimeField("Lunch", lunchMinutes) { lunchMinutes = it }
            ReminderTimeField("Dinner", dinnerMinutes) { dinnerMinutes = it }
        }
        Text("BODY REMINDERS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(
            Modifier.fillMaxWidth().clickable { bodyRemindersEnabled = !bodyRemindersEnabled }.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Weight & body fat", fontSize = 15.sp)
                Text("Daily weight at 8 AM and body fat every Sunday at 8 AM", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = bodyRemindersEnabled, onCheckedChange = { bodyRemindersEnabled = it })
        }
        Text("4 MONTH MISSION", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        DateField(missionStart, "Start date") { missionStart = it }
        DateField(missionEnd, "Target date") { missionEnd = it }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(goalWeight, { goalWeight = it }, "Goal weight ($weightUnit)", Modifier.weight(1f))
            AppField(weeklyTraining, { weeklyTraining = it.filter(Char::isDigit) }, "Sessions/week", Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeField(label: String, minutesFromMidnight: Int, onChange: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
            .clickable { open = true }.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${(minutesFromMidnight / 60).toString().padStart(2, '0')}:${(minutesFromMidnight % 60).toString().padStart(2, '0')}", fontSize = 16.sp)
        }
        Icon(Icons.Outlined.Schedule, "Choose $label reminder time", tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (open) {
        val state = rememberTimePickerState(
            initialHour = minutesFromMidnight / 60,
            initialMinute = minutesFromMidnight % 60,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { open = false },
            title = { Text("$label reminder") },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    onChange(state.hour * 60 + state.minute)
                    open = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel") } },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(value: String, label: String = "Date", onChange: (String) -> Unit) {
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
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

internal fun formatDateForDisplay(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault()))
}.getOrDefault(value)

internal fun formatDateShort(value: String): String = runCatching {
    LocalDate.parse(value).format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
}.getOrDefault(value)

@Composable
private fun WorkoutDialog(
    onDismiss: () -> Unit,
    onSave: (WorkoutEntity) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(today()) }
    FormSheet(
        title = "New session",
        description = "Start with today's date, or choose another day.",
        primaryLabel = "Start workout",
        primaryEnabled = date.isNotBlank(),
        onPrimary = { onSave(WorkoutEntity(performedOn = date, name = name.ifBlank { null })) },
        onDismiss = onDismiss,
        compact = true,
    ) {
        AppField(name, { name = it }, "Session name (optional)")
        DateField(date) { date = it }
    }
}

@Composable
private fun MeasurementDialog(settings: AppSettings, onDismiss: () -> Unit, onSave: (MeasurementEntity) -> Unit) {
    var weight by rememberSaveable { mutableStateOf("") }
    var bodyFat by rememberSaveable { mutableStateOf("") }
    var waist by rememberSaveable { mutableStateOf("") }
    var leftArm by rememberSaveable { mutableStateOf("") }
    var rightArm by rememberSaveable { mutableStateOf("") }
    var leftThigh by rememberSaveable { mutableStateOf("") }
    var rightThigh by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf(today()) }
    val values = listOf(weight, bodyFat, waist, leftArm, rightArm, leftThigh, rightThigh)
    val validNumbers = values.all { numberInputError(it, allowZero = false) == null } &&
        numberInputError(bodyFat, allowZero = false, maximum = 100.0) == null
    FormSheet(
        title = "New measurement",
        description = "Log only what you measured today. Every field is optional.",
        primaryLabel = "Add measurement",
        primaryEnabled = date.isNotBlank() && values.any(String::isNotBlank) && validNumbers,
        onPrimary = {
            onSave(
                MeasurementEntity(
                    measuredOn = date,
                    weightKg = decimalValue(weight)?.storageWeight(settings),
                    bodyFatPct = decimalValue(bodyFat),
                    waistCm = decimalValue(waist)?.storageLength(settings),
                    leftArmCm = decimalValue(leftArm)?.storageLength(settings),
                    rightArmCm = decimalValue(rightArm)?.storageLength(settings),
                    leftThighCm = decimalValue(leftThigh)?.storageLength(settings),
                    rightThighCm = decimalValue(rightThigh)?.storageLength(settings),
                ),
            )
        },
        onDismiss = onDismiss,
    ) {
        DateField(date) { date = it }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField(weight, { weight = it }, "Weight (${settings.weightUnit})", Modifier.weight(1f), allowZero = false)
            NumberField(bodyFat, { bodyFat = it }, "Body fat (%)", Modifier.weight(1f), allowZero = false, maximum = 100.0)
        }
        NumberField(waist, { waist = it }, "Waist (${settings.lengthUnit})", allowZero = false)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField(leftArm, { leftArm = it }, "Left arm (${settings.lengthUnit})", Modifier.weight(1f), allowZero = false)
            NumberField(rightArm, { rightArm = it }, "Right arm (${settings.lengthUnit})", Modifier.weight(1f), allowZero = false)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField(leftThigh, { leftThigh = it }, "Left thigh (${settings.lengthUnit})", Modifier.weight(1f), allowZero = false)
            NumberField(rightThigh, { rightThigh = it }, "Right thigh (${settings.lengthUnit})", Modifier.weight(1f), allowZero = false)
        }
    }
}

@Composable
private fun FoodDialog(
    product: BarcodeProductEntity?,
    initialDate: String,
    entry: FoodEntryEntity? = null,
    savedFood: SavedFoodEntity? = null,
    estimate: FoodPhotoEstimate? = null,
    photoPreview: ImageBitmap? = null,
    onDismiss: () -> Unit,
    onSave: (FoodEntryEntity, Boolean) -> Unit,
) {
    val defaultServing = product?.servingG ?: 100.0
    var name by rememberSaveable(product, entry, estimate, savedFood) {
        mutableStateOf(entry?.name ?: savedFood?.name ?: estimate?.name ?: product?.name.orEmpty())
    }
    var serving by rememberSaveable(product) { mutableStateOf(defaultServing.pretty()) }
    var calories by rememberSaveable(product, entry, estimate, savedFood) {
        mutableStateOf(
            entry?.calories?.pretty()
                ?: savedFood?.calories?.pretty()
                ?: estimate?.calories?.pretty()
                ?: product?.caloriesPer100g?.times(defaultServing / 100)?.pretty().orEmpty(),
        )
    }
    var protein by rememberSaveable(product, entry, estimate, savedFood) {
        mutableStateOf(
            entry?.proteinG?.pretty()
                ?: savedFood?.proteinG?.pretty()
                ?: estimate?.proteinG?.pretty()
                ?: product?.proteinPer100g?.times(defaultServing / 100)?.pretty().orEmpty(),
        )
    }
    var carbs by rememberSaveable(product, entry, estimate, savedFood) {
        mutableStateOf(
            entry?.carbsG?.pretty()
                ?: savedFood?.carbsG?.pretty()
                ?: estimate?.carbsG?.pretty()
                ?: product?.carbsPer100g?.times(defaultServing / 100)?.pretty().orEmpty(),
        )
    }
    var fat by rememberSaveable(product, entry, estimate, savedFood) {
        mutableStateOf(
            entry?.fatG?.pretty()
                ?: savedFood?.fatG?.pretty()
                ?: estimate?.fatG?.pretty()
                ?: product?.fatPer100g?.times(defaultServing / 100)?.pretty().orEmpty(),
        )
    }
    var date by rememberSaveable(entry, initialDate) { mutableStateOf(entry?.consumedOn ?: initialDate) }
    var saveForReuse by rememberSaveable(entry, savedFood) { mutableStateOf(entry == null && savedFood == null) }
    var showMoreMacros by rememberSaveable(estimate, entry, savedFood) {
        mutableStateOf(
            estimate?.carbsG != null || estimate?.fatG != null ||
                entry?.carbsG != null || entry?.fatG != null || savedFood?.carbsG != null || savedFood?.fatG != null,
        )
    }
    var showMultiplier by rememberSaveable { mutableStateOf(false) }
    val validMacros = listOf(calories, protein, carbs, fat).all { numberInputError(it) == null }
    val validServing = product == null || (numberInputError(serving, allowZero = false) == null && serving.isNotBlank())
    val macros = { Macros(decimalValue(calories), decimalValue(protein), decimalValue(carbs), decimalValue(fat)) }
    val setMacros: (Macros) -> Unit = { scaled ->
        calories = scaled.calories?.pretty().orEmpty()
        protein = scaled.proteinG?.pretty().orEmpty()
        carbs = scaled.carbsG?.pretty().orEmpty()
        fat = scaled.fatG?.pretty().orEmpty()
    }
    FormSheet(
        title = when {
            entry != null -> "Edit food"
            savedFood != null -> "Review saved food"
            estimate != null -> "Review photo estimate"
            product == null -> "Log food"
            else -> "Review scanned food"
        },
        description = when {
            estimate != null -> "Gemini estimates are rough. Check serving size and macros before saving."
            savedFood != null -> "Some nutrition values aren't entered. Add them if known; leave unknown values blank."
            product == null -> "Add food and any nutrition values you know. Blank values stay unknown."
            else -> "Check the serving and nutrition before saving."
        },
        primaryLabel = if (entry == null) "Add food" else "Save changes",
        primaryEnabled = name.isNotBlank() && validMacros && validServing,
        onPrimary = {
            onSave(entry?.copy(consumedOn = date, name = name.trim(), calories = decimalValue(calories), proteinG = decimalValue(protein), carbsG = decimalValue(carbs), fatG = decimalValue(fat))
                ?: FoodEntryEntity(consumedOn = date, name = name.trim(), calories = decimalValue(calories), proteinG = decimalValue(protein), carbsG = decimalValue(carbs), fatG = decimalValue(fat), barcode = product?.barcode), entry == null && saveForReuse)
        },
        onDismiss = onDismiss,
    ) {
            photoPreview?.let { preview ->
                Image(
                    bitmap = preview,
                    contentDescription = "Meal photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
            AppField(name, { name = it }, "Food")
            DateField(date) { date = it }
            if (product != null) NumberField(serving, { value ->
                serving = value
                decimalValue(value)?.takeIf { it > 0 }?.let { grams ->
                    setMacros(scaleMacros(
                        Macros(product.caloriesPer100g, product.proteinPer100g, product.carbsPer100g, product.fatPer100g),
                        grams,
                    ))
                }
            }, "Serving (g)", allowZero = false)
            Text("MACROS", fontSize = 10.sp, letterSpacing = 1.4.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                NumberField(calories, { calories = it }, "Calories", Modifier.weight(1f))
                NumberField(protein, { protein = it }, "Protein (g)", Modifier.weight(1f))
            }
            TextButton(onClick = { showMoreMacros = !showMoreMacros }) {
                Text(if (showMoreMacros) "Hide carbs and fat" else "Add carbs and fat")
            }
            if (showMoreMacros || numberInputError(carbs) != null || numberInputError(fat) != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NumberField(carbs, { carbs = it }, "Carbs (g)", Modifier.weight(1f))
                    NumberField(fat, { fat = it }, "Fat (g)", Modifier.weight(1f))
                }
            }
            TextButton(onClick = { showMultiplier = !showMultiplier }, enabled = validMacros && listOf(calories, protein, carbs, fat).any(String::isNotBlank)) {
                Text(if (showMultiplier) "Hide serving adjustment" else "Adjust serving")
            }
            if (showMultiplier) MacroMultiplier(macros(), setMacros)
            if (entry == null) Row(
                Modifier.fillMaxWidth().clickable { saveForReuse = !saveForReuse }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Save for reuse", fontSize = 15.sp)
                    Text("Keep name and macros in quick add", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = saveForReuse, onCheckedChange = { saveForReuse = it })
            }
    }
}

@Composable
private fun MacroMultiplier(macros: Macros, onChange: (Macros) -> Unit) {
    var multiplier by rememberSaveable { mutableStateOf("1") }
    val factor = decimalValue(multiplier)?.takeIf { it > 0 }
    val scaled = factor?.let { scaleMacros(macros, it * 100) }
    val valid = scaled != null && listOfNotNull(scaled.calories, scaled.proteinG, scaled.carbsG, scaled.fatG).all { it.isFinite() && it >= 0 }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        NumberField(multiplier, { multiplier = it }, "Multiply by", Modifier.weight(1f), allowZero = false)
        TextButton(onClick = { scaled?.let(onChange) }, enabled = valid) { Text("Apply") }
    }
}

@Composable
private fun SavedFoodDialog(value: SavedFoodEntity, onDismiss: () -> Unit, onSave: (SavedFoodEntity) -> Unit) {
    var name by rememberSaveable(value) { mutableStateOf(value.name) }
    var calories by rememberSaveable(value) { mutableStateOf(value.calories?.pretty().orEmpty()) }
    var protein by rememberSaveable(value) { mutableStateOf(value.proteinG?.pretty().orEmpty()) }
    var carbs by rememberSaveable(value) { mutableStateOf(value.carbsG?.pretty().orEmpty()) }
    var fat by rememberSaveable(value) { mutableStateOf(value.fatG?.pretty().orEmpty()) }
    val valid = listOf(calories, protein, carbs, fat).all { numberInputError(it) == null }
    FormSheet("Edit saved food", "Used by Quick add. Blank nutrition values stay unknown.", "Save changes", name.isNotBlank() && valid, {
        onSave(value.copy(name = name.trim(), calories = decimalValue(calories), proteinG = decimalValue(protein), carbsG = decimalValue(carbs), fatG = decimalValue(fat)))
    }, onDismiss) {
        AppField(name, { name = it }, "Food")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField(calories, { calories = it }, "Calories", Modifier.weight(1f))
            NumberField(protein, { protein = it }, "Protein (g)", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            NumberField(carbs, { carbs = it }, "Carbs (g)", Modifier.weight(1f))
            NumberField(fat, { fat = it }, "Fat (g)", Modifier.weight(1f))
        }
    }
}
