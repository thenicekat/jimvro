package app.jimvro.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "measurements", indices = [Index("measuredOn")])
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val measuredOn: String,
    val weightKg: Double? = null,
    val bodyFatPct: Double? = null,
    val chestCm: Double? = null,
    val waistCm: Double? = null,
    val hipsCm: Double? = null,
    val leftArmCm: Double? = null,
    val rightArmCm: Double? = null,
    val leftThighCm: Double? = null,
    val rightThighCm: Double? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "exercises", indices = [Index(value = ["name"], unique = true)])
data class ExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val muscleGroup: String = "other",
)

@Entity(tableName = "workouts", indices = [Index("performedOn")])
data class WorkoutEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val performedOn: String,
    val name: String? = null,
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [Index("workoutId"), Index("exerciseId")],
)
data class WorkoutSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val workoutId: Long,
    val exerciseId: Long,
    val setNumber: Int = 1,
    val reps: Int? = null,
    val weightKg: Double? = null,
    val rpe: Double? = null,
)

@Entity(tableName = "saved_foods", indices = [Index("name")])
data class SavedFoodEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val calories: Double? = null,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
)

@Entity(tableName = "food_entries", indices = [Index("consumedOn")])
data class FoodEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val consumedOn: String,
    val meal: String = "snack",
    val name: String,
    val calories: Double? = null,
    val proteinG: Double? = null,
    val carbsG: Double? = null,
    val fatG: Double? = null,
    val barcode: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "barcode_products")
data class BarcodeProductEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val caloriesPer100g: Double? = null,
    val proteinPer100g: Double? = null,
    val carbsPer100g: Double? = null,
    val fatPer100g: Double? = null,
    val servingG: Double? = null,
    val cachedAt: Long = System.currentTimeMillis(),
)

data class WorkoutSummary(
    val id: Long,
    val performedOn: String,
    val name: String?,
    val setCount: Int,
    val volumeKg: Double,
)

data class DailyNutrition(
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
)

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY measuredOn DESC, createdAt DESC")
    fun observeAll(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements ORDER BY measuredOn DESC, createdAt DESC LIMIT 1")
    fun observeLatest(): Flow<MeasurementEntity?>

    @Insert suspend fun insert(value: MeasurementEntity): Long
    @Delete suspend fun delete(value: MeasurementEntity)
}

@Dao
interface WorkoutDao {
    @Query(
        """SELECT w.id, w.performedOn, w.name, COUNT(s.id) AS setCount,
            COALESCE(SUM(COALESCE(s.reps, 0) * COALESCE(s.weightKg, 0)), 0.0) AS volumeKg
            FROM workouts w LEFT JOIN workout_sets s ON s.workoutId = w.id
            GROUP BY w.id ORDER BY w.performedOn DESC, w.createdAt DESC""",
    )
    fun observeSummaries(): Flow<List<WorkoutSummary>>

    @Query("SELECT COUNT(*) FROM workouts WHERE performedOn = :date")
    fun observeCountOn(date: String): Flow<Int>

    @Insert suspend fun insertWorkout(value: WorkoutEntity): Long
    @Insert suspend fun insertSet(value: WorkoutSetEntity): Long
    @Query("DELETE FROM workouts WHERE id = :id") suspend fun deleteWorkout(id: Long)

    @Transaction
    suspend fun createWorkout(value: WorkoutEntity, sets: List<WorkoutSetEntity>): Long {
        val workoutId = insertWorkout(value)
        sets.forEach { insertSet(it.copy(workoutId = workoutId)) }
        return workoutId
    }
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises ORDER BY name")
    fun observeAll(): Flow<List<ExerciseEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(values: List<ExerciseEntity>)
}

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_entries ORDER BY consumedOn DESC, createdAt DESC")
    fun observeAll(): Flow<List<FoodEntryEntity>>

    @Query("SELECT * FROM food_entries WHERE consumedOn = :date ORDER BY createdAt DESC")
    fun observeOn(date: String): Flow<List<FoodEntryEntity>>

    @Query(
        """SELECT COALESCE(SUM(calories), 0.0) AS calories,
            COALESCE(SUM(proteinG), 0.0) AS proteinG,
            COALESCE(SUM(carbsG), 0.0) AS carbsG,
            COALESCE(SUM(fatG), 0.0) AS fatG
            FROM food_entries WHERE consumedOn = :date""",
    )
    fun observeNutritionOn(date: String): Flow<DailyNutrition>

    @Insert suspend fun insert(value: FoodEntryEntity): Long
    @Delete suspend fun delete(value: FoodEntryEntity)

    @Query("SELECT * FROM barcode_products WHERE barcode = :barcode")
    suspend fun getBarcodeProduct(barcode: String): BarcodeProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cacheBarcodeProduct(value: BarcodeProductEntity)
}

@Database(
    entities = [
        MeasurementEntity::class,
        ExerciseEntity::class,
        WorkoutEntity::class,
        WorkoutSetEntity::class,
        SavedFoodEntity::class,
        FoodEntryEntity::class,
        BarcodeProductEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class JimvroDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun foodDao(): FoodDao

    companion object {
        fun create(context: Context): JimvroDatabase =
            Room.databaseBuilder(context, JimvroDatabase::class.java, "jimvro.db")
                .build()
    }
}
