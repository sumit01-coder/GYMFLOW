package com.gymflow.server.database

import com.gymflow.server.config.ServerConfig
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.util.UUID

object DatabaseFactory {
    private val logger = LoggerFactory.getLogger(DatabaseFactory::class.java)

    fun init() {
        logger.info("Initializing database connection with pool...")
        val dataSource = createHikariDataSource()
        Database.connect(dataSource)

        transaction {
            logger.info("Running schema migration / creating missing tables...")
            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                MemberProfilesTable,
                TrainerProfilesTable,
                TrainerMembersTable,
                InvitationCodesTable,
                ExercisesTable,
                WorkoutPlansTable,
                WorkoutDaysTable,
                WorkoutExercisesTable,
                WorkoutSessionsTable,
                SetLogsTable,
                WeightLogsTable,
                HydrationLogsTable,
                BodyMeasurementsTable,
                SyncChangesTable
            )
            seedDefaultExercises()
        }
        logger.info("Database initialized successfully.")
    }

    private fun createHikariDataSource(): HikariDataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = ServerConfig.databaseUrl
            username = ServerConfig.databaseUser
            password = ServerConfig.databasePassword
            maximumPoolSize = ServerConfig.databaseMaxConnections
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            poolName = "GymFlowHikariPool"
            connectionTimeout = 15000
            maxLifetime = 1800000
            idleTimeout = 600000
            validate()
        }
        return HikariDataSource(config)
    }

    private fun seedDefaultExercises() {
        val existingCount = ExercisesTable.selectAll().count()
        if (existingCount > 0) {
            logger.info("Exercises table already populated with $existingCount exercises.")
            return
        }

        logger.info("Seeding default standard exercises...")

        data class SeedExercise(
            val id: String,
            val name: String,
            val category: String,
            val muscleGroup: String,
            val equipment: String,
            val instructions: String,
            val defaultRestSeconds: Int
        )

        val defaultExercises = listOf(
            SeedExercise(
                id = "ex-bench-press",
                name = "Bench Press",
                category = "Chest",
                muscleGroup = "Chest",
                equipment = "Barbell",
                instructions = "Lie flat on the bench, grip the barbell slightly wider than shoulder-width, lower the bar smoothly to your mid-chest, and press back up explosively.",
                defaultRestSeconds = 60
            ),
            SeedExercise(
                id = "ex-incline-db-press",
                name = "Incline Dumbbell Press",
                category = "Chest",
                muscleGroup = "Upper Chest",
                equipment = "Dumbbell",
                instructions = "Set bench to a 30-45 degree incline. Press dumbbells upward over chest with elbows angled at 45 degrees, maintaining controlled descent.",
                defaultRestSeconds = 60
            ),
            SeedExercise(
                id = "ex-cable-fly",
                name = "Cable Fly",
                category = "Chest",
                muscleGroup = "Chest",
                equipment = "Cable",
                instructions = "Position pulleys at chest height. Step forward slightly and bring hands together in a smooth hugging arc, squeezing pectorals at peak contraction.",
                defaultRestSeconds = 45
            ),
            SeedExercise(
                id = "ex-squat",
                name = "Squat",
                category = "Legs",
                muscleGroup = "Quadriceps & Glutes",
                equipment = "Barbell",
                instructions = "Place barbell comfortably across upper traps. Squat down until thighs are parallel to ground, keep chest upright, and drive through heels to return.",
                defaultRestSeconds = 90
            ),
            SeedExercise(
                id = "ex-leg-press",
                name = "Leg Press",
                category = "Legs",
                muscleGroup = "Quadriceps",
                equipment = "Machine",
                instructions = "Position feet shoulder-width on the sled platform. Lower weight smoothly until knees bend to 90 degrees, then push through mid-foot without locking knees.",
                defaultRestSeconds = 60
            ),
            SeedExercise(
                id = "ex-deadlift",
                name = "Deadlift",
                category = "Back",
                muscleGroup = "Posterior Chain",
                equipment = "Barbell",
                instructions = "Stand with feet hip-width under barbell. Hinge at hips, grasp bar, brace core, and drive through the floor to stand fully erect with neutral spine.",
                defaultRestSeconds = 120
            ),
            SeedExercise(
                id = "ex-triceps-pushdown",
                name = "Triceps Pushdown",
                category = "Arms",
                muscleGroup = "Triceps",
                equipment = "Cable",
                instructions = "Attach straight or rope bar to high pulley. Keep elbows tightly pinned at sides, press downward until arms are fully locked out.",
                defaultRestSeconds = 45
            ),
            SeedExercise(
                id = "ex-barbell-curl",
                name = "Barbell Curl",
                category = "Arms",
                muscleGroup = "Biceps",
                equipment = "Barbell",
                instructions = "Hold barbell with shoulder-width underhand grip. Curl bar upward towards upper chest while keeping upper arms strictly stationary.",
                defaultRestSeconds = 45
            ),
            SeedExercise(
                id = "ex-shoulder-press",
                name = "Shoulder Press",
                category = "Shoulders",
                muscleGroup = "Deltoids",
                equipment = "Dumbbell",
                instructions = "Hold dumbbells at shoulder height with palms facing forward. Press vertically overhead until arms are extended, then lower under steady control.",
                defaultRestSeconds = 60
            )
        )

        for (ex in defaultExercises) {
            ExercisesTable.insertIgnore {
                it[id] = ex.id
                it[name] = ex.name
                it[category] = ex.category
                it[muscleGroup] = ex.muscleGroup
                it[equipment] = ex.equipment
                it[instructions] = ex.instructions
                it[defaultRestSeconds] = ex.defaultRestSeconds
            }
        }
        logger.info("Seeded ${defaultExercises.size} default standard exercises.")
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
