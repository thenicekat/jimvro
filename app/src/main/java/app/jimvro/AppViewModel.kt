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

class AppViewModel(private val repository: JimvroRepository) : ViewModel() {
    val measurements = repository.measurements.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val workouts = repository.workouts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val foodEntries = repository.foodEntries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val exercises = repository.exercises.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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
    fun deleteSet(setId: Long) = viewModelScope.launch { repository.deleteSet(setId) }
    fun addFood(value: FoodEntryEntity) = viewModelScope.launch { repository.addFood(value) }
    fun deleteFood(value: FoodEntryEntity) = viewModelScope.launch { repository.deleteFood(value) }
    suspend fun lookupBarcode(code: String) = repository.lookupBarcode(code)

    class Factory(private val repository: JimvroRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AppViewModel(repository) as T
    }
}
