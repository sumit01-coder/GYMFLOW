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
        ExerciseEntity::class,
        DailyStepsEntity::class,
        OutdoorWorkoutEntity::class,
        OutdoorGpsPointEntity::class,
        FoodLogEntity::class
    ],
    version = 11,
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
    abstract fun dailyStepsDao(): DailyStepsDao
    abstract fun outdoorWorkoutDao(): OutdoorWorkoutDao
    abstract fun foodDao(): FoodDao

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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS weight_logs_new (
                        id TEXT NOT NULL PRIMARY KEY,
                        memberId TEXT NOT NULL,
                        preWeightKg REAL NOT NULL,
                        postWeightKg REAL NOT NULL,
                        deltaKg REAL NOT NULL,
                        recordedAtEpochMs INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        isSynced INTEGER NOT NULL,
                        postRecordedAtEpochMs INTEGER
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO weight_logs_new (id, memberId, preWeightKg, postWeightKg, deltaKg, recordedAtEpochMs, notes, isSynced, postRecordedAtEpochMs)
                    SELECT id, memberId, preWeightKg, COALESCE(postWeightKg, 0.0), deltaKg, recordedAtEpochMs, notes, isSynced, recordedAtEpochMs FROM weight_logs
                """.trimIndent())
                db.execSQL("DROP TABLE weight_logs")
                db.execSQL("ALTER TABLE weight_logs_new RENAME TO weight_logs")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN createdAtEpochMs INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS weight_logs_v7 (
                        id TEXT NOT NULL PRIMARY KEY,
                        memberId TEXT NOT NULL,
                        preWeightKg REAL NOT NULL,
                        postWeightKg REAL NOT NULL,
                        deltaKg REAL NOT NULL,
                        recordedAtEpochMs INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        isSynced INTEGER NOT NULL,
                        postRecordedAtEpochMs INTEGER
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO weight_logs_v7 (id, memberId, preWeightKg, postWeightKg, deltaKg, recordedAtEpochMs, notes, isSynced, postRecordedAtEpochMs)
                    SELECT id, memberId, preWeightKg, COALESCE(postWeightKg, 0.0), deltaKg, recordedAtEpochMs, notes, isSynced, postRecordedAtEpochMs FROM weight_logs
                """.trimIndent())
                db.execSQL("DROP TABLE weight_logs")
                db.execSQL("ALTER TABLE weight_logs_v7 RENAME TO weight_logs")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS weight_logs_v8 (
                        id TEXT NOT NULL PRIMARY KEY,
                        memberId TEXT NOT NULL,
                        preWeightKg REAL NOT NULL,
                        postWeightKg REAL NOT NULL,
                        deltaKg REAL NOT NULL,
                        recordedAtEpochMs INTEGER NOT NULL,
                        notes TEXT NOT NULL,
                        isSynced INTEGER NOT NULL,
                        postRecordedAtEpochMs INTEGER
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO weight_logs_v8 (id, memberId, preWeightKg, postWeightKg, deltaKg, recordedAtEpochMs, notes, isSynced, postRecordedAtEpochMs)
                    SELECT id, memberId, preWeightKg, COALESCE(postWeightKg, 0.0), deltaKg, recordedAtEpochMs, notes, isSynced, postRecordedAtEpochMs FROM weight_logs
                """.trimIndent())
                db.execSQL("DROP TABLE weight_logs")
                db.execSQL("ALTER TABLE weight_logs_v8 RENAME TO weight_logs")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `daily_steps`")
                db.execSQL("DROP TABLE IF EXISTS `outdoor_workouts`")
                db.execSQL("DROP TABLE IF EXISTS `outdoor_gps_points`")
                db.execSQL("DROP INDEX IF EXISTS `index_daily_steps_memberId_date`")
                db.execSQL("DROP INDEX IF EXISTS `index_outdoor_workouts_memberId`")
                db.execSQL("DROP INDEX IF EXISTS `index_outdoor_gps_points_workoutId`")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `daily_steps` (
                        `id` TEXT NOT NULL,
                        `memberId` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `stepCount` INTEGER NOT NULL,
                        `targetGoal` INTEGER NOT NULL,
                        `distanceMeters` REAL NOT NULL,
                        `caloriesKcal` INTEGER NOT NULL,
                        `activeMinutes` INTEGER NOT NULL,
                        `lastSensorStepCount` INTEGER NOT NULL,
                        `lastUpdatedEpochMs` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `outdoor_workouts` (
                        `id` TEXT NOT NULL,
                        `memberId` TEXT NOT NULL,
                        `workoutType` TEXT NOT NULL,
                        `startTimeEpochMs` INTEGER NOT NULL,
                        `endTimeEpochMs` INTEGER,
                        `durationSeconds` INTEGER NOT NULL,
                        `totalSteps` INTEGER NOT NULL,
                        `totalDistanceMeters` REAL NOT NULL,
                        `avgPaceSecondsPerKm` REAL NOT NULL,
                        `maxSpeedMps` REAL NOT NULL,
                        `caloriesBurned` INTEGER NOT NULL,
                        `polylineEncoded` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `outdoor_gps_points` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `workoutId` TEXT NOT NULL,
                        `timestampEpochMs` INTEGER NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `altitude` REAL NOT NULL,
                        `accuracy` REAL NOT NULL,
                        `speed` REAL NOT NULL,
                        `cumulativeSteps` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_outdoor_gps_points_workoutId` ON `outdoor_gps_points` (`workoutId`)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS `daily_steps`")
                db.execSQL("DROP TABLE IF EXISTS `outdoor_workouts`")
                db.execSQL("DROP TABLE IF EXISTS `outdoor_gps_points`")
                db.execSQL("DROP INDEX IF EXISTS `index_daily_steps_memberId_date`")
                db.execSQL("DROP INDEX IF EXISTS `index_outdoor_workouts_memberId`")
                db.execSQL("DROP INDEX IF EXISTS `index_outdoor_gps_points_workoutId`")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `daily_steps` (
                        `id` TEXT NOT NULL,
                        `memberId` TEXT NOT NULL,
                        `date` TEXT NOT NULL,
                        `stepCount` INTEGER NOT NULL,
                        `targetGoal` INTEGER NOT NULL,
                        `distanceMeters` REAL NOT NULL,
                        `caloriesKcal` INTEGER NOT NULL,
                        `activeMinutes` INTEGER NOT NULL,
                        `lastSensorStepCount` INTEGER NOT NULL,
                        `lastUpdatedEpochMs` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `outdoor_workouts` (
                        `id` TEXT NOT NULL,
                        `memberId` TEXT NOT NULL,
                        `workoutType` TEXT NOT NULL,
                        `startTimeEpochMs` INTEGER NOT NULL,
                        `endTimeEpochMs` INTEGER,
                        `durationSeconds` INTEGER NOT NULL,
                        `totalSteps` INTEGER NOT NULL,
                        `totalDistanceMeters` REAL NOT NULL,
                        `avgPaceSecondsPerKm` REAL NOT NULL,
                        `maxSpeedMps` REAL NOT NULL,
                        `caloriesBurned` INTEGER NOT NULL,
                        `polylineEncoded` TEXT NOT NULL,
                        `isCompleted` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `outdoor_gps_points` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `workoutId` TEXT NOT NULL,
                        `timestampEpochMs` INTEGER NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `altitude` REAL NOT NULL,
                        `accuracy` REAL NOT NULL,
                        `speed` REAL NOT NULL,
                        `cumulativeSteps` INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_outdoor_gps_points_workoutId` ON `outdoor_gps_points` (`workoutId`)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `food_logs` (
                        `id` TEXT NOT NULL,
                        `memberId` TEXT NOT NULL,
                        `mealType` TEXT NOT NULL,
                        `foodName` TEXT NOT NULL,
                        `brand` TEXT,
                        `barcode` TEXT,
                        `servingSizeGrams` REAL NOT NULL,
                        `caloriesKcal` INTEGER NOT NULL,
                        `proteinGrams` REAL NOT NULL,
                        `carbsGrams` REAL NOT NULL,
                        `fatGrams` REAL NOT NULL,
                        `fiberGrams` REAL NOT NULL,
                        `sugarsGrams` REAL NOT NULL,
                        `sodiumMg` REAL NOT NULL,
                        `imageUrl` TEXT,
                        `source` TEXT NOT NULL,
                        `loggedAtEpochMs` INTEGER NOT NULL,
                        `isSynced` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_food_logs_memberId_loggedAtEpochMs` ON `food_logs` (`memberId`, `loggedAtEpochMs`)")
            }
        }
    }
}
