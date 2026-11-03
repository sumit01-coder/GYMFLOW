package com.gymflow.server.database

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object UsersTable : Table("users") {
    val id = varchar("id", 36)
    val name = varchar("name", 100)
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 20) // "MEMBER", "TRAINER"
    val createdAt = long("created_at")

    override val primaryKey = PrimaryKey(id)
}

object MemberProfilesTable : Table("member_profiles") {
    val userId = varchar("user_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val height = float("height")
    val currentWeight = float("current_weight")
    val goal = varchar("goal", 255)
    val age = integer("age")
    val trainerId = varchar("trainer_id", 36).references(UsersTable.id, onDelete = ReferenceOption.SET_NULL).nullable()

    override val primaryKey = PrimaryKey(userId)
}

object TrainerProfilesTable : Table("trainer_profiles") {
    val userId = varchar("user_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val experience = integer("experience")
    val specialization = varchar("specialization", 255)

    override val primaryKey = PrimaryKey(userId)
}

object TrainerMembersTable : Table("trainer_members") {
    val id = varchar("id", 36)
    val trainerId = varchar("trainer_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val memberId = varchar("member_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val status = varchar("status", 20) // "ACTIVE", "PENDING", "INACTIVE"

    override val primaryKey = PrimaryKey(id)
}

object InvitationCodesTable : Table("invitation_codes") {
    val code = varchar("code", 20)
    val trainerId = varchar("trainer_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val expiresAt = long("expires_at")
    val isUsed = bool("is_used").default(false)

    override val primaryKey = PrimaryKey(code)
}

object ExercisesTable : Table("exercises") {
    val id = varchar("id", 36)
    val name = varchar("name", 150)
    val category = varchar("category", 50)
    val muscleGroup = varchar("muscle_group", 50)
    val equipment = varchar("equipment", 50)
    val instructions = text("instructions")
    val defaultRestSeconds = integer("default_rest_seconds")

    override val primaryKey = PrimaryKey(id)
}

object WorkoutPlansTable : Table("workout_plans") {
    val id = varchar("id", 36)
    val trainerId = varchar("trainer_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val memberId = varchar("member_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 150)
    val version = integer("version").default(1)
    val isActive = bool("is_active").default(true)

    override val primaryKey = PrimaryKey(id)
}

object WorkoutDaysTable : Table("workout_days") {
    val id = varchar("id", 36)
    val planId = varchar("plan_id", 36).references(WorkoutPlansTable.id, onDelete = ReferenceOption.CASCADE)
    val dayOfWeek = varchar("day_of_week", 20)
    val title = varchar("title", 150)
    val isRestDay = bool("is_rest_day").default(false)

    override val primaryKey = PrimaryKey(id)
}

object WorkoutExercisesTable : Table("workout_exercises") {
    val id = varchar("id", 36)
    val dayId = varchar("day_id", 36).references(WorkoutDaysTable.id, onDelete = ReferenceOption.CASCADE)
    val exerciseId = varchar("exercise_id", 36).references(ExercisesTable.id, onDelete = ReferenceOption.CASCADE)
    val position = integer("position")
    val targetSets = integer("target_sets")
    val targetReps = integer("target_reps")
    val targetWeight = float("target_weight")
    val restSeconds = integer("rest_seconds")
    val notes = text("notes")

    override val primaryKey = PrimaryKey(id)
}

object WorkoutSessionsTable : Table("workout_sessions") {
    val id = varchar("id", 36)
    val memberId = varchar("member_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val planId = varchar("plan_id", 36).references(WorkoutPlansTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val workoutTitle = varchar("workout_title", 150)
    val startTime = long("start_time")
    val endTime = long("end_time").nullable()
    val durationSeconds = long("duration_seconds").default(0L)
    val totalVolume = float("total_volume").default(0f)
    val caloriesBurned = integer("calories_burned").default(0)
    val isCompleted = bool("is_completed").default(false)

    override val primaryKey = PrimaryKey(id)
}

object SetLogsTable : Table("set_logs") {
    val id = varchar("id", 36)
    val sessionId = varchar("session_id", 36).references(WorkoutSessionsTable.id, onDelete = ReferenceOption.CASCADE)
    val exerciseId = varchar("exercise_id", 36).references(ExercisesTable.id, onDelete = ReferenceOption.CASCADE)
    val exerciseName = varchar("exercise_name", 150)
    val setNumber = integer("set_number")
    val targetReps = integer("target_reps")
    val targetWeight = float("target_weight")
    val actualReps = integer("actual_reps")
    val actualWeight = float("actual_weight")
    val completedAt = long("completed_at")

    override val primaryKey = PrimaryKey(id)
}

object WeightLogsTable : Table("weight_logs") {
    val id = varchar("id", 36)
    val memberId = varchar("member_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val preWeight = float("pre_weight")
    val postWeight = float("post_weight")
    val delta = float("delta")
    val recordedAt = long("recorded_at")
    val notes = text("notes")

    override val primaryKey = PrimaryKey(id)
}

object HydrationLogsTable : Table("hydration_logs") {
    val id = varchar("id", 36)
    val memberId = varchar("member_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val amountMl = integer("amount_ml")
    val recordedAt = long("recorded_at")

    override val primaryKey = PrimaryKey(id)
}

object BodyMeasurementsTable : Table("body_measurements") {
    val id = varchar("id", 36)
    val memberId = varchar("member_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val chest = float("chest")
    val waist = float("waist")
    val arms = float("arms")
    val thighs = float("thighs")
    val hips = float("hips")
    val recordedAt = long("recorded_at")

    override val primaryKey = PrimaryKey(id)
}

object SyncChangesTable : Table("sync_changes") {
    val id = varchar("id", 36)
    val userId = varchar("user_id", 36).references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val entityType = varchar("entity_type", 50)
    val entityId = varchar("entity_id", 36)
    val version = long("version")
    val payload = text("payload")
    val timestamp = long("timestamp")

    override val primaryKey = PrimaryKey(id)
}
