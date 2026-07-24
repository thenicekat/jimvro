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
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

@Entity(tableName = "workout_templates")
data class WorkoutTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val notes: String? = null,
    val position: Int = 0,
)

@Entity(
    tableName = "template_exercises",
    foreignKeys = [
        ForeignKey(entity = WorkoutTemplateEntity::class, parentColumns = ["id"], childColumns = ["templateId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ExerciseEntity::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.RESTRICT),
    ],
    indices = [Index("templateId"), Index("exerciseId")],
)
data class TemplateExerciseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    val exerciseId: Long,
    val position: Int = 0,
    val targetSets: Int = 3,
    val repLow: Int? = null,
    val repHigh: Int? = null,
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

data class WorkoutSetDetail(
    val id: Long,
    val workoutId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val setNumber: Int,
    val reps: Int?,
    val weightKg: Double?,
    val rpe: Double?,
)

data class PreviousSet(
    val setNumber: Int,
    val reps: Int?,
    val weightKg: Double?,
    val performedOn: String,
)

data class PersonalRecord(
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val weightKg: Double,
    val reps: Int?,
    val performedOn: String,
    val workoutId: Long,
)

data class TemplateSummary(val id: Long, val name: String, val notes: String?, val exerciseCount: Int)

data class TemplateLine(
    val id: Long,
    val templateId: Long,
    val exerciseId: Long,
    val exerciseName: String,
    val muscleGroup: String,
    val position: Int,
    val targetSets: Int,
    val repLow: Int?,
    val repHigh: Int?,
)

data class TemplateTarget(
    val exerciseId: Long,
    val targetSets: Int,
    val repLow: Int?,
    val repHigh: Int?,
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

    @Query("SELECT * FROM workouts WHERE id = :id")
    fun observeWorkout(id: Long): Flow<WorkoutEntity?>

    @Query(
        """SELECT s.id, s.workoutId, s.exerciseId, e.name AS exerciseName,
            e.muscleGroup, s.setNumber, s.reps, s.weightKg, s.rpe
            FROM workout_sets s INNER JOIN exercises e ON e.id = s.exerciseId
            WHERE s.workoutId = :workoutId ORDER BY s.id""",
    )
    fun observeSetDetails(workoutId: Long): Flow<List<WorkoutSetDetail>>

    @Query(
        """SELECT s.setNumber, s.reps, s.weightKg, w.performedOn
        FROM workout_sets s INNER JOIN workouts w ON w.id = s.workoutId
        WHERE s.exerciseId = :exerciseId AND s.workoutId = (
          SELECT s2.workoutId FROM workout_sets s2 INNER JOIN workouts w2 ON w2.id = s2.workoutId
          WHERE s2.exerciseId = :exerciseId AND s2.workoutId != :workoutId
            AND w2.performedOn <= (SELECT performedOn FROM workouts WHERE id = :workoutId)
          ORDER BY w2.performedOn DESC, w2.id DESC LIMIT 1
        ) ORDER BY s.setNumber""",
    )
    fun observePreviousSets(workoutId: Long, exerciseId: Long): Flow<List<PreviousSet>>

    @Query(
        """SELECT s.exerciseId, e.name AS exerciseName, e.muscleGroup, s.weightKg,
        s.reps, w.performedOn, w.id AS workoutId
        FROM workout_sets s INNER JOIN workouts w ON w.id = s.workoutId
        INNER JOIN exercises e ON e.id = s.exerciseId
        WHERE s.weightKg IS NOT NULL AND s.id = (
          SELECT s2.id FROM workout_sets s2 INNER JOIN workouts w2 ON w2.id = s2.workoutId
          WHERE s2.exerciseId = s.exerciseId AND s2.weightKg IS NOT NULL
          ORDER BY s2.weightKg DESC, w2.performedOn DESC, COALESCE(s2.reps, 0) DESC LIMIT 1
        ) ORDER BY e.name""",
    )
    fun observePersonalRecords(): Flow<List<PersonalRecord>>

    @Insert suspend fun insertWorkout(value: WorkoutEntity): Long
    @Insert suspend fun insertSet(value: WorkoutSetEntity): Long
    @Query("UPDATE workout_sets SET reps = :reps, weightKg = :weightKg, rpe = :rpe WHERE id = :setId")
    suspend fun updateSet(setId: Long, reps: Int?, weightKg: Double?, rpe: Double?)

    @Query("DELETE FROM workout_sets WHERE id = :setId")
    suspend fun deleteSet(setId: Long)

    @Query("SELECT COALESCE(MAX(setNumber), 0) FROM workout_sets WHERE workoutId = :workoutId AND exerciseId = :exerciseId")
    suspend fun maxSetNumber(workoutId: Long, exerciseId: Long): Int
    @Query("DELETE FROM workouts WHERE id = :id") suspend fun deleteWorkout(id: Long)

    @Transaction
    suspend fun createWorkout(value: WorkoutEntity, sets: List<WorkoutSetEntity>): Long {
        val workoutId = insertWorkout(value)
        sets.forEach { insertSet(it.copy(workoutId = workoutId)) }
        return workoutId
    }

    @Transaction
    suspend fun appendSet(workoutId: Long, exerciseId: Long, reps: Int? = null, weightKg: Double? = null): Long =
        insertSet(
            WorkoutSetEntity(
                workoutId = workoutId,
                exerciseId = exerciseId,
                setNumber = maxSetNumber(workoutId, exerciseId) + 1,
                reps = reps,
                weightKg = weightKg,
            ),
        )
}

@Dao
interface TemplateDao {
    @Query(
        """SELECT t.id, t.name, t.notes, COUNT(l.id) AS exerciseCount
        FROM workout_templates t LEFT JOIN template_exercises l ON l.templateId = t.id
        GROUP BY t.id ORDER BY t.position, t.id""",
    )
    fun observeTemplates(): Flow<List<TemplateSummary>>

    @Query(
        """SELECT l.id, l.templateId, l.exerciseId, e.name AS exerciseName, e.muscleGroup,
        l.position, l.targetSets, l.repLow, l.repHigh
        FROM template_exercises l INNER JOIN exercises e ON e.id = l.exerciseId
        WHERE l.templateId = :templateId ORDER BY l.position, l.id""",
    )
    fun observeLines(templateId: Long): Flow<List<TemplateLine>>

    @Insert suspend fun insertTemplate(value: WorkoutTemplateEntity): Long
    @Insert suspend fun insertLine(value: TemplateExerciseEntity): Long
    @Transaction
    suspend fun createTemplate(name: String, targets: List<TemplateTarget>): Long {
        val templateId = insertTemplate(WorkoutTemplateEntity(name = name))
        targets.forEachIndexed { position, target ->
            insertLine(
                TemplateExerciseEntity(
                    templateId = templateId,
                    exerciseId = target.exerciseId,
                    position = position,
                    targetSets = target.targetSets,
                    repLow = target.repLow,
                    repHigh = target.repHigh,
                ),
            )
        }
        return templateId
    }
    @Query("DELETE FROM workout_templates WHERE id = :id") suspend fun deleteTemplate(id: Long)
    @Query("DELETE FROM template_exercises WHERE id = :id") suspend fun deleteLine(id: Long)
    @Query("SELECT * FROM workout_templates WHERE id = :id") suspend fun template(id: Long): WorkoutTemplateEntity?
    @Query("SELECT * FROM template_exercises WHERE templateId = :id ORDER BY position, id") suspend fun lines(id: Long): List<TemplateExerciseEntity>

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
        WorkoutTemplateEntity::class,
        TemplateExerciseEntity::class,
        FoodEntryEntity::class,
        BarcodeProductEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class JimvroDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun foodDao(): FoodDao
    abstract fun templateDao(): TemplateDao

    companion object {
        fun create(context: Context): JimvroDatabase =
            Room.databaseBuilder(context, JimvroDatabase::class.java, "jimvro.db")
                .addMigrations(MIGRATION_1_2)
                .build()

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS workout_templates (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, notes TEXT, position INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS template_exercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, templateId INTEGER NOT NULL, exerciseId INTEGER NOT NULL, position INTEGER NOT NULL, targetSets INTEGER NOT NULL, repLow INTEGER, repHigh INTEGER, FOREIGN KEY(templateId) REFERENCES workout_templates(id) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON UPDATE NO ACTION ON DELETE RESTRICT)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_template_exercises_templateId ON template_exercises(templateId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_template_exercises_exerciseId ON template_exercises(exerciseId)")
            }
        }
    }
}
