package app.jimvro.ui

import androidx.activity.compose.LocalActivity
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Scale
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.navArgument
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
import app.jimvro.ui.theme.Fraunces
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
    val current = Destination.entries.firstOrNull { it.route == currentRoute }
        ?: if (currentRoute.startsWith("workout/")) Destination.Workouts else Destination.Today

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(current.label, fontWeight = FontWeight.Medium, fontSize = 16.sp) },
                actions = {
                    Card(
                        modifier = Modifier.padding(end = 12.dp).size(48.dp),
                        shape = CircleShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                        elevation = CardDefaults.cardElevation(2.dp),
                    ) {
                        IconButton(onClick = {}, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Outlined.MoreHoriz, "More", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
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
            composable(Destination.Today.route) {
                TodayScreen(viewModel) { route -> navController.navigate(route) }
            }
            composable(Destination.Workouts.route) {
                WorkoutsScreen(viewModel) { id -> navController.navigate("workout/$id") }
            }
            composable(
                route = "workout/{id}",
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                WorkoutTrackerScreen(
                    viewModel = viewModel,
                    workoutId = entry.arguments?.getLong("id") ?: return@composable,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Destination.Body.route) { BodyScreen(viewModel) }
            composable(Destination.Food.route) { FoodScreen(viewModel) }
        }
    }
}

@Composable
private fun Page(
    eyebrow: String,
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(eyebrow.uppercase(), color = Clay, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(title, fontFamily = Fraunces, fontSize = 40.sp, fontWeight = FontWeight.Medium, letterSpacing = (-0.5).sp)
                    Spacer(Modifier.height(8.dp))
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp, lineHeight = 21.sp)
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
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onSurface,
            contentColor = MaterialTheme.colorScheme.surface,
        ),
        contentPadding = PaddingValues(horizontal = 14.dp),
    ) {
        Icon(Icons.Outlined.Add, null, Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun JournalCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun TodayScreen(viewModel: AppViewModel, onNavigate: (String) -> Unit) {
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
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Good morning."; hour < 18 -> "Good afternoon."; else -> "Good evening." }
    val dashedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 28.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    SimpleDateFormat("EEEE, d MMMM", locale).format(Date()).uppercase(locale),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(greeting, fontFamily = Fraunces, fontSize = 40.sp, fontWeight = FontWeight.Medium, lineHeight = 44.sp)
            }
        }
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (todayWorkouts.isEmpty()) MaterialTheme.colorScheme.surface else ClayMuted.copy(alpha = 0.55f),
                ),
                border = BorderStroke(1.dp, if (todayWorkouts.isEmpty()) MaterialTheme.colorScheme.outline.copy(alpha = 0.7f) else Clay.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(16.dp),
            ) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.FitnessCenter, null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Training", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(14.dp))
                            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(if (volume > 0) volume.pretty() else setCount.toString(), fontSize = 56.sp, fontWeight = FontWeight.SemiBold, lineHeight = 58.sp)
                                Text(if (volume > 0) "kg moved" else if (setCount == 0) "sets — rest day" else "sets", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 7.dp))
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
        }
        item {
            JournalCard {
                Text("Weekly volume", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("No tonnage logged yet", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(
                    Modifier.fillMaxWidth().height(112.dp).drawBehind {
                        drawRoundRect(
                            color = dashedBorderColor,
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(7f, 7f)),
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx()),
                        )
                    },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Log sets with reps and weight to see your weekly trend", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                    Text("—", fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
                    Text("Nothing logged yet", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        MetricInline(calories.pretty(), "kcal")
                        Box(Modifier.width(1.dp).height(34.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)))
                        MetricInline(protein.pretty(), "g protein")
                    }
                    Text("${todayFoods.size} item${if (todayFoods.size == 1) "" else "s"} logged", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                Text(measurements.firstOrNull()?.weightKg?.pretty() ?: "—", fontSize = 32.sp, fontWeight = FontWeight.SemiBold)
                Text(if (measurements.isEmpty()) "No measurements yet" else "kg · latest reading", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("QUICK LOG", fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickLogButton("Workout") { onNavigate(Destination.Workouts.route) }
                    QuickLogButton("Food") { onNavigate(Destination.Food.route) }
                    QuickLogButton("Measurement") { onNavigate(Destination.Body.route) }
                }
            }
        }
    }
}

@Composable
private fun MetricInline(value: String, unit: String) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(value, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
        Text(unit, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp))
    }
}

@Composable
private fun QuickLogButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface, contentColor = MaterialTheme.colorScheme.onSurface),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp),
    ) { Text("+", color = Clay); Spacer(Modifier.width(5.dp)); Text(label, fontSize = 13.sp) }
}

@Composable
private fun WorkoutsScreen(viewModel: AppViewModel, onOpenWorkout: (Long) -> Unit) {
    val workouts by viewModel.workouts.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    Page(
        "Training",
        "Workouts",
        "Each session holds the sets you logged that day.",
        action = { HeaderAddButton("New") { showAdd = true } },
    ) {
            if (workouts.isEmpty()) EmptyState("No workouts yet", "Log your first training session.")
            workouts.forEach { workout ->
                JournalCard(Modifier.clickable { onOpenWorkout(workout.id) }) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(workout.name ?: "Workout", fontWeight = FontWeight.Medium)
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
    if (showAdd) WorkoutDialog(onDismiss = { showAdd = false }) { workout ->
        viewModel.addWorkout(workout)
        showAdd = false
    }
}

@Composable
private fun BodyScreen(viewModel: AppViewModel) {
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()
    var showAdd by remember { mutableStateOf(false) }
    Page(
        "Measurements",
        "Body",
        "Weight, body fat, and tape measurements over time.",
        action = { HeaderAddButton("Add") { showAdd = true } },
    ) {
            if (measurements.isEmpty()) EmptyState("No measurements yet", "Add a weigh-in to start your trend.")
            measurements.forEach { measurement ->
                JournalCard {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(measurement.weightKg?.let { "${it.pretty()} kg" } ?: "Body measurement", fontSize = 24.sp, fontFamily = Fraunces)
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

    Page(
        "Nutrition",
        "Food",
        "Scan a barcode, or log it by hand.",
        action = { HeaderAddButton("Add") { showAdd = true } },
    ) {
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
            message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (foods.isEmpty()) EmptyState("No food logged", "Add manually or scan a packaged food.")
            foods.groupBy { it.consumedOn }.forEach { (date, entries) ->
                Text(date, fontWeight = FontWeight.Medium)
                entries.forEach { food ->
                    JournalCard {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(food.name, fontWeight = FontWeight.Medium)
                                Text(food.meal.replaceFirstChar(Char::uppercase), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            }
                            Text("${(food.calories ?: 0.0).pretty()} kcal", fontWeight = FontWeight.Medium)
                            IconButton(onClick = { viewModel.deleteFood(food) }) { Icon(Icons.Outlined.Delete, "Delete food") }
                        }
                        Text("P ${(food.proteinG ?: 0.0).pretty()}g  ·  C ${(food.carbsG ?: 0.0).pretty()}g  ·  F ${(food.fatG ?: 0.0).pretty()}g", fontSize = 13.sp)
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
        Text(title, fontFamily = Fraunces, fontSize = 20.sp)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Clay,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.75f),
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
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 0.dp,
        dragHandle = null,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, fontFamily = Fraunces, fontSize = 26.sp, fontWeight = FontWeight.Medium)
                Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            content()
            Spacer(Modifier.height(2.dp))
            Button(
                onClick = onPrimary,
                enabled = primaryEnabled,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f),
                    disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                ),
            ) { Text(primaryLabel, fontWeight = FontWeight.Medium) }
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Cancel") }
        }
    }
}

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
        AppField(date, { date = it }, "Date (YYYY-MM-DD)")
    }
}

@Composable
private fun MeasurementDialog(onDismiss: () -> Unit, onSave: (MeasurementEntity) -> Unit) {
    var weight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    FormSheet(
        title = "New measurement",
        description = "Log only what you measured today. Every field is optional.",
        primaryLabel = "Add measurement",
        primaryEnabled = date.isNotBlank() && listOf(weight, bodyFat, waist).any(String::isNotBlank),
        onPrimary = { onSave(MeasurementEntity(measuredOn = date, weightKg = weight.toDoubleOrNull(), bodyFatPct = bodyFat.toDoubleOrNull(), waistCm = waist.toDoubleOrNull())) },
        onDismiss = onDismiss,
    ) {
        AppField(date, { date = it }, "Date (YYYY-MM-DD)")
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppField(weight, { weight = it }, "Weight (kg)", Modifier.weight(1f))
            AppField(bodyFat, { bodyFat = it }, "Body fat (%)", Modifier.weight(1f))
        }
        AppField(waist, { waist = it }, "Waist (cm)")
    }
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
    FormSheet(
        title = if (product == null) "Log food" else "Review scanned food",
        description = if (product == null) "Add a meal and its macros." else "Check the serving and nutrition before saving.",
        primaryLabel = "Add food",
        primaryEnabled = name.isNotBlank(),
        onPrimary = { onSave(FoodEntryEntity(consumedOn = today(), name = name.trim(), calories = calories.toDoubleOrNull(), proteinG = protein.toDoubleOrNull(), carbsG = carbs.toDoubleOrNull(), fatG = fat.toDoubleOrNull(), barcode = product?.barcode)) },
        onDismiss = onDismiss,
    ) {
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
            Text("MACROS", fontSize = 11.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppField(calories, { calories = it }, "Calories", Modifier.weight(1f))
                AppField(protein, { protein = it }, "Protein (g)", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppField(carbs, { carbs = it }, "Carbs (g)", Modifier.weight(1f))
                AppField(fat, { fat = it }, "Fat (g)", Modifier.weight(1f))
            }
    }
}
