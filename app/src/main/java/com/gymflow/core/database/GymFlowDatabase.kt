package com.gymflow.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UserEntity::class,
        WorkoutPlanEntity::class,
        WorkoutDayEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSessionEntity::class,
        SetLogEntity::class,
        WeightLogEntity::class,
        HydrationLogEntity::class,
        BodyMeasurementEntity::class,
        SyncQueueEntity::class,
        ExerciseEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GymFlowDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun planDao(): PlanDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    abstract fun weightDao(): WeightDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        const val DATABASE_NAME = "gymflow.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN isEmailVerified INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS exercises (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        category TEXT NOT NULL,
                        muscleGroup TEXT NOT NULL,
                        equipment TEXT NOT NULL,
                        machineName TEXT,
                        defaultSets INTEGER NOT NULL DEFAULT 3,
                        defaultReps INTEGER NOT NULL DEFAULT 12,
                        defaultWeightKg REAL NOT NULL DEFAULT 20.0,
                        defaultRestSeconds INTEGER NOT NULL DEFAULT 45,
                        mediaUrl TEXT,
                        instructionsJson TEXT NOT NULL DEFAULT '[]',
                        coachTip TEXT NOT NULL DEFAULT ''
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE exercises ADD COLUMN gifUrl TEXT")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_category ON exercises (category)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_equipment ON exercises (equipment)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_name ON exercises (name)")
            }
        }
    }
}
