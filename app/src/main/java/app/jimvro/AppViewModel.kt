package app.jimvro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import app.jimvro.data.FoodEntryEntity
import app.jimvro.data.JimvroRepository
import app.jimvro.data.MeasurementEntity
import app.jimvro.data.WorkoutEntity
import app.jimvro.data.WorkoutSetEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import app.jimvro.data.WorkoutSetDetail
import app.jimvro.data.PreviousSet
import app.jimvro.data.TemplateLine
import app.jimvro.data.TemplateTarget
import app.jimvro.data.ExerciseProgressPoint

class AppViewModel(private val repository: JimvroRepository) : ViewModel() {
    val measurements = repository.measurements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val workouts = repository.workouts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val foodEntries = repository.foodEntries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val exercises = repository.exercises.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val templates = repository.templates.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val personalRecords = repository.personalRecords.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val recentExercises = repository.recentExercises.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val progressPhotos = repository.progressPhotos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addMeasurement(value: MeasurementEntity) = viewModelScope.launch { repository.addMeasurement(value) }
    fun deleteMeasurement(value: MeasurementEntity) = viewModelScope.launch { repository.deleteMeasurement(value) }
    fun addWorkout(value: WorkoutEntity) = viewModelScope.launch { repository.addWorkout(value) }
    fun createWorkout(value: WorkoutEntity, sets: List<WorkoutSetEntity>) = viewModelScope.launch {
        repository.createWorkout(value, sets)
    }
    fun deleteWorkout(id: Long) = viewModelScope.launch { repository.deleteWorkout(id) }
    fun workout(id: Long): Flow<WorkoutEntity?> = repository.workout(id)
    fun workoutSets(id: Long): Flow<List<WorkoutSetDetail>> = repository.workoutSets(id)
    fun appendSet(workoutId: Long, exerciseId: Long, reps: Int? = null, weightKg: Double? = null) =
        viewModelScope.launch { repository.appendSet(workoutId, exerciseId, reps, weightKg) }
    fun updateSet(setId: Long, reps: Int?, weightKg: Double?, rpe: Double? = null) =
        viewModelScope.launch { repository.updateSet(setId, reps, weightKg, rpe) }
    fun updateSetType(setId: Long, setType: String) = viewModelScope.launch { repository.updateSetType(setId, setType) }
    fun finishWorkout(workoutId: Long) = viewModelScope.launch { repository.finishWorkout(workoutId) }
    fun setSuperset(workoutId: Long, exerciseIds: List<Long>, groupId: Int?) = viewModelScope.launch { repository.setSuperset(workoutId, exerciseIds, groupId) }
    fun toggleFavorite(exerciseId: Long) = viewModelScope.launch { repository.toggleFavorite(exerciseId) }
    suspend fun findOrCreateExercise(name: String) = repository.findOrCreateExercise(name)
    fun deleteSet(setId: Long) = viewModelScope.launch { repository.deleteSet(setId) }
    fun previousSets(workoutId: Long, exerciseId: Long): Flow<List<PreviousSet>> = repository.previousSets(workoutId, exerciseId)
    fun exerciseProgress(exerciseId: Long): Flow<List<ExerciseProgressPoint>> = repository.exerciseProgress(exerciseId)
    fun templateLines(id: Long): Flow<List<TemplateLine>> = repository.templateLines(id)
    fun createTemplate(name: String, notes: String? = null) = viewModelScope.launch { repository.createTemplate(name, notes) }
    suspend fun createTemplate(name: String, targets: List<TemplateTarget>) = repository.createTemplate(name, targets)
    fun deleteTemplate(id: Long) = viewModelScope.launch { repository.deleteTemplate(id) }
    fun addTemplateLine(templateId: Long, exerciseId: Long, targetSets: Int, repLow: Int?, repHigh: Int?) = viewModelScope.launch {
        repository.addTemplateLine(templateId, exerciseId, targetSets, repLow, repHigh)
    }
    fun deleteTemplateLine(id: Long) = viewModelScope.launch { repository.deleteTemplateLine(id) }
    fun updateTemplateLine(id: Long, sets: Int, low: Int?, high: Int?) = viewModelScope.launch { repository.updateTemplateLine(id, sets, low, high) }
    fun reorderTemplateLines(lines: List<TemplateLine>) = viewModelScope.launch { repository.reorderTemplateLines(lines) }
    suspend fun startFromTemplate(templateId: Long, date: String): Long = repository.startFromTemplate(templateId, date)
    fun addFood(value: FoodEntryEntity) = viewModelScope.launch { repository.addFood(value) }
    fun deleteFood(value: FoodEntryEntity) = viewModelScope.launch { repository.deleteFood(value) }
    suspend fun lookupBarcode(code: String) = repository.lookupBarcode(code)

    class Factory(private val repository: JimvroRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(repository) as T
    }
}
