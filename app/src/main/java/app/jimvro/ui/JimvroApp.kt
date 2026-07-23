package app.jimvro.ui

import androidx.activity.compose.LocalActivity
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.jimvro.AppViewModel
import app.jimvro.data.BarcodeProductEntity
import app.jimvro.data.ExerciseEntity
import app.jimvro.data.FoodEntryEntity
import app.jimvro.data.MeasurementEntity
import app.jimvro.data.WorkoutEntity
import app.jimvro.data.WorkoutSetEntity
import app.jimvro.domain.Macros
import app.jimvro.domain.scaleMacros
import app.jimvro.ui.theme.Clay
import app.jimvro.ui.theme.ClayMuted
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

private enum class Destination(val route: String, val label: String, val icon: ImageVector) {
    Today("today", "Today", Icons.Outlined.Home),
    Workouts("workouts", "Workouts", Icons.Outlined.FitnessCenter),
    Body("body", "Body", Icons.Outlined.Scale),
    Food("food", "Food", Icons.Outlined.Restaurant),
}

private fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
private fun Double.pretty(): String = if (this % 1.0 == 0.0) toInt().toString() else "%.1f".format(this)

@Suppress("DEPRECATION")
private fun Configuration.primaryLocale(): Locale =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) locales[0] else locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JimvroApp(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: Destination.Today.route
    val current = Destination.entries.firstOrNull { it.route == currentRoute } ?: Destination.Today

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current.label, fontWeight = FontWeight.SemiBold, fontSize = 16.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                Destination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, null) },
                        label = { Text(destination.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Clay,
                            indicatorColor = ClayMuted,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Destination.Today.route,
            modifier = Modifier.padding(padding),
        ) {
            composable(Destination.Today.route) { TodayScreen(viewModel) }
            composable(Destination.Workouts.route) { WorkoutsScreen(viewModel) }
            composable(Destination.Body.route) { BodyScreen(viewModel) }
            composable(Destination.Food.route) { FoodScreen(viewModel) }
        }
    }
}

@Composable
private fun Page(title: String, subtitle: String, content: @Composable ColumnScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Column {
                Text(title, fontFamily = FontFamily.Serif, fontSize = 32.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
            }
        }
        item { Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content) }
    }
}

@Composable
private fun JournalCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun TodayScreen(viewModel: AppViewModel) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    val foods by viewModel.foodEntries.collectAsStateWithLifecycle()
    val date = today()
    val todayFoods = foods.filter { it.consumedOn == date }
    val calories = todayFoods.sumOf { it.calories ?: 0.0 }
    val protein = todayFoods.sumOf { it.proteinG ?: 0.0 }
    val locale = LocalConfiguration.current.primaryLocale()

    Page("Today", SimpleDateFormat("EEEE, d MMMM", locale).format(Date())) {
        JournalCard {
            Text("TRAINING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Clay)
            Text("${workouts.count { it.performedOn == date }} sessions", fontSize = 26.sp, fontFamily = FontFamily.Serif)
            Text("Small entries add up to a strong record.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        JournalCard {
            Text("NUTRITION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Clay)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric("Calories", calories.pretty(), "kcal")
                Metric("Protein", protein.pretty(), "g")
                Metric("Entries", todayFoods.size.toString(), "today")
            }
        }
        JournalCard {
            Text("LATEST WEIGH-IN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Clay)
            val latest = measurements.firstOrNull()
            Text(latest?.weightKg?.let { "${it.pretty()} kg" } ?: "No measurement yet", fontSize = 26.sp, fontFamily = FontFamily.Serif)
            latest?.let { Text(it.measuredOn, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun Metric(label: String, value: String, unit: String) {
    Column {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Text(unit, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun WorkoutsScreen(viewModel: AppViewModel) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        Page("Workouts", "Sessions, sets, and training volume") {
            if (workouts.isEmpty()) EmptyState("No workouts yet", "Log your first training session.")
            workouts.forEach { workout ->
                JournalCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(workout.name ?: "Workout", fontWeight = FontWeight.SemiBold)
                            Text(workout.performedOn, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                        }
                        IconButton(onClick = { viewModel.deleteWorkout(workout.id) }) {
                            Icon(Icons.Outlined.Delete, "Delete workout")
                        }
                    }
                    Text("${workout.setCount} sets  ·  ${workout.volumeKg.pretty()} kg volume", fontSize = 13.sp)
                }
            }
        }
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = Clay,
        ) { Icon(Icons.Outlined.Add, "Add workout") }
    }
    if (showAdd) WorkoutDialog(exercises = exercises, onDismiss = { showAdd = false }) { workout, sets ->
        viewModel.createWorkout(workout, sets)
        showAdd = false
    }
}

@Composable
private fun BodyScreen(viewModel: AppViewModel) {
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize()) {
        Page("Body", "Measurements and progress over time") {
            if (measurements.isEmpty()) EmptyState("No measurements yet", "Add a weigh-in to start your trend.")
            measurements.forEach { measurement ->
                JournalCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(measurement.weightKg?.let { "${it.pretty()} kg" } ?: "Body measurement", fontSize = 24.sp, fontFamily = FontFamily.Serif)
                            Text(measurement.measuredOn, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.deleteMeasurement(measurement) }) {
                            Icon(Icons.Outlined.Delete, "Delete measurement")
                        }
                    }
                    measurement.bodyFatPct?.let { Text("Body fat  ${it.pretty()}%", fontSize = 13.sp) }
                    measurement.waistCm?.let { Text("Waist  ${it.pretty()} cm", fontSize = 13.sp) }
                    measurement.notes?.takeIf(String::isNotBlank)?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
        }
        FloatingActionButton(
            onClick = { showAdd = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            containerColor = Clay,
        ) { Icon(Icons.Outlined.Add, "Add measurement") }
    }
    if (showAdd) MeasurementDialog(onDismiss = { showAdd = false }) {
        viewModel.addMeasurement(it)
        showAdd = false
    }
}

@Composable
private fun FoodScreen(viewModel: AppViewModel) {
    val foods by viewModel.foodEntries.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    var scannedProduct by remember { mutableStateOf<BarcodeProductEntity?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    val activity = LocalActivity.current ?: return
    val scope = rememberCoroutineScope()
    val scanner = remember {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A, Barcode.FORMAT_UPC_E)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(activity, options)
    }

    Box(Modifier.fillMaxSize()) {
        Page("Food", "Meals and macros, stored on this device") {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = { showAdd = true }) { Icon(Icons.Outlined.Add, null); Text(" Manual") }
                Button(onClick = {
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
                }) { Icon(Icons.Outlined.QrCodeScanner, null); Text(" Scan") }
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (foods.isEmpty()) EmptyState("No food logged", "Add manually or scan a packaged food.")
            foods.groupBy { it.consumedOn }.forEach { (date, entries) ->
                Text(date, fontWeight = FontWeight.SemiBold)
                entries.forEach { food ->
                    JournalCard {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(food.name, fontWeight = FontWeight.SemiBold)
                                Text(food.meal.replaceFirstChar(Char::uppercase), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                            Text("${(food.calories ?: 0.0).pretty()} kcal", fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = { viewModel.deleteFood(food) }) { Icon(Icons.Outlined.Delete, "Delete food") }
                        }
                        Text("P ${(food.proteinG ?: 0.0).pretty()}g  ·  C ${(food.carbsG ?: 0.0).pretty()}g  ·  F ${(food.fatG ?: 0.0).pretty()}g", fontSize = 13.sp)
                    }
                }
            }
        }
    }
    if (showAdd) FoodDialog(product = null, onDismiss = { showAdd = false }) {
        viewModel.addFood(it); showAdd = false
    }
    scannedProduct?.let { product ->
        FoodDialog(product = product, onDismiss = { scannedProduct = null }) {
            viewModel.addFood(it); scannedProduct = null
        }
    }
}

@Composable
private fun EmptyState(title: String, subtitle: String) {
    JournalCard {
        Text(title, fontFamily = FontFamily.Serif, fontSize = 20.sp)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun AppField(value: String, onValueChange: (String) -> Unit, label: String, modifier: Modifier = Modifier) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
}

private data class DraftSet(
    val exerciseId: Long,
    val exerciseName: String,
    val reps: Int?,
    val weightKg: Double?,
    val rpe: Double?,
)

@Composable
private fun WorkoutDialog(
    exercises: List<ExerciseEntity>,
    onDismiss: () -> Unit,
    onSave: (WorkoutEntity, List<WorkoutSetEntity>) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    var selected by remember(exercises) { mutableStateOf(exercises.firstOrNull()) }
    var exerciseMenu by remember { mutableStateOf(false) }
    var reps by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var rpe by remember { mutableStateOf("") }
    var sets by remember { mutableStateOf(emptyList<DraftSet>()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New workout", fontFamily = FontFamily.Serif) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(name, { name = it }, "Workout name")
            AppField(date, { date = it }, "Date (YYYY-MM-DD)")
            Box {
                Button(onClick = { exerciseMenu = true }, enabled = exercises.isNotEmpty()) {
                    Text(selected?.name ?: "Loading exercises…")
                }
                DropdownMenu(expanded = exerciseMenu, onDismissRequest = { exerciseMenu = false }) {
                    exercises.forEach { exercise ->
                        DropdownMenuItem(
                            text = { Text(exercise.name) },
                            onClick = { selected = exercise; exerciseMenu = false },
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppField(reps, { reps = it }, "Reps", Modifier.weight(1f))
                AppField(weight, { weight = it }, "kg", Modifier.weight(1f))
                AppField(rpe, { rpe = it }, "RPE", Modifier.weight(1f))
            }
            TextButton(
                onClick = {
                    selected?.let {
                        sets = sets + DraftSet(it.id, it.name, reps.toIntOrNull(), weight.toDoubleOrNull(), rpe.toDoubleOrNull())
                        reps = ""; weight = ""; rpe = ""
                    }
                },
                enabled = selected != null && (reps.isNotBlank() || weight.isNotBlank()),
            ) { Text("+ Add set") }
            if (sets.isNotEmpty()) {
                Text("${sets.size} sets added", fontWeight = FontWeight.SemiBold)
                sets.takeLast(3).forEach { set ->
                    Text("${set.exerciseName}  ·  ${set.reps ?: "–"} × ${set.weightKg?.pretty() ?: "–"} kg", fontSize = 12.sp)
                }
            }
        } },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        WorkoutEntity(performedOn = date, name = name.ifBlank { null }),
                        sets.mapIndexed { index, set ->
                            WorkoutSetEntity(
                                workoutId = 0,
                                exerciseId = set.exerciseId,
                                setNumber = index + 1,
                                reps = set.reps,
                                weightKg = set.weightKg,
                                rpe = set.rpe,
                            )
                        },
                    )
                },
                enabled = date.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun MeasurementDialog(onDismiss: () -> Unit, onSave: (MeasurementEntity) -> Unit) {
    var weight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add measurement", fontFamily = FontFamily.Serif) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(weight, { weight = it }, "Weight (kg)")
            AppField(bodyFat, { bodyFat = it }, "Body fat (%)")
            AppField(waist, { waist = it }, "Waist (cm)")
            AppField(date, { date = it }, "Date (YYYY-MM-DD)")
        } },
        confirmButton = { Button(onClick = { onSave(MeasurementEntity(measuredOn = date, weightKg = weight.toDoubleOrNull(), bodyFatPct = bodyFat.toDoubleOrNull(), waistCm = waist.toDoubleOrNull())) }, enabled = date.isNotBlank() && listOf(weight, bodyFat, waist).any(String::isNotBlank)) { Text("Save") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun FoodDialog(product: BarcodeProductEntity?, onDismiss: () -> Unit, onSave: (FoodEntryEntity) -> Unit) {
    val defaultServing = product?.servingG ?: 100.0
    var name by remember(product) { mutableStateOf(product?.name.orEmpty()) }
    var serving by remember(product) { mutableStateOf(defaultServing.pretty()) }
    var calories by remember(product) { mutableStateOf(product?.caloriesPer100g?.times(defaultServing / 100)?.pretty().orEmpty()) }
    var protein by remember(product) { mutableStateOf(product?.proteinPer100g?.times(defaultServing / 100)?.pretty().orEmpty()) }
    var carbs by remember(product) { mutableStateOf(product?.carbsPer100g?.times(defaultServing / 100)?.pretty().orEmpty()) }
    var fat by remember(product) { mutableStateOf(product?.fatPer100g?.times(defaultServing / 100)?.pretty().orEmpty()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Log food" else "Review scanned food", fontFamily = FontFamily.Serif) },
        text = { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AppField(name, { name = it }, "Food")
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
            AppField(calories, { calories = it }, "Calories")
            AppField(protein, { protein = it }, "Protein (g)")
            AppField(carbs, { carbs = it }, "Carbs (g)")
            AppField(fat, { fat = it }, "Fat (g)")
        } },
        confirmButton = { Button(onClick = { onSave(FoodEntryEntity(consumedOn = today(), name = name.trim(), calories = calories.toDoubleOrNull(), proteinG = protein.toDoubleOrNull(), carbsG = carbs.toDoubleOrNull(), fatG = fat.toDoubleOrNull(), barcode = product?.barcode)) }, enabled = name.isNotBlank()) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
