package app.jimvro.data

import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val databaseName = "migration-test"

    @Test fun migration3To4KeepsDatabaseValid() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(databaseName)
        val factory = FrameworkSQLiteOpenHelperFactory()
        factory.create(SupportSQLiteOpenHelper.Configuration.builder(context).name(databaseName).callback(object : SupportSQLiteOpenHelper.Callback(3) {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE exercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, muscleGroup TEXT NOT NULL, sourceId TEXT, bodyPart TEXT, equipment TEXT, target TEXT, secondaryMuscles TEXT, instructions TEXT)")
                db.execSQL("CREATE TABLE workouts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, performedOn TEXT NOT NULL, name TEXT, notes TEXT, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE workout_sets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, workoutId INTEGER NOT NULL, exerciseId INTEGER NOT NULL, setNumber INTEGER NOT NULL, reps INTEGER, weightKg REAL, rpe REAL)")
                db.execSQL("CREATE TABLE template_exercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, templateId INTEGER NOT NULL, exerciseId INTEGER NOT NULL, position INTEGER NOT NULL, targetSets INTEGER NOT NULL, repLow INTEGER, repHigh INTEGER)")
            }
            override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }).build()).writableDatabase.close()
        val migrated = factory.create(SupportSQLiteOpenHelper.Configuration.builder(context).name(databaseName).callback(object : SupportSQLiteOpenHelper.Callback(4) {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) = Unit
            override fun onUpgrade(db: androidx.sqlite.db.SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = JimvroDatabase.MIGRATION_3_4.migrate(db)
        }).build())
        val db = migrated.writableDatabase
        val columns = mutableSetOf<String>()
        db.query("PRAGMA table_info(workout_sets)").use { cursor -> while (cursor.moveToNext()) columns += cursor.getString(cursor.getColumnIndexOrThrow("name")) }
        assertTrue(columns.containsAll(listOf("setType", "supersetGroup")))
        db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='progress_photos'").use { assertTrue(it.moveToFirst()) }
        migrated.close()
    }
}
