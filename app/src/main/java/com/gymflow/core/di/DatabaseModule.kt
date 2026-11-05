package com.gymflow.core.di

import android.content.Context
import androidx.room.Room
import com.gymflow.core.database.ExerciseDao
import com.gymflow.core.database.GymFlowDatabase
import com.gymflow.core.database.PlanDao
import com.gymflow.core.database.SyncQueueDao
import com.gymflow.core.database.UserDao
import com.gymflow.core.database.WeightDao
import com.gymflow.core.database.WorkoutSessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideGymFlowDatabase(
        @ApplicationContext context: Context
    ): GymFlowDatabase {
        return Room.databaseBuilder(
            context,
            GymFlowDatabase::class.java,
            GymFlowDatabase.DATABASE_NAME
        )
            .addMigrations(
                GymFlowDatabase.MIGRATION_1_2,
                GymFlowDatabase.MIGRATION_2_3,
                GymFlowDatabase.MIGRATION_3_4,
                GymFlowDatabase.MIGRATION_4_5
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideUserDao(database: GymFlowDatabase): UserDao = database.userDao()

    @Provides
    fun providePlanDao(database: GymFlowDatabase): PlanDao = database.planDao()

    @Provides
    fun provideWorkoutSessionDao(database: GymFlowDatabase): WorkoutSessionDao =
        database.workoutSessionDao()

    @Provides
    fun provideWeightDao(database: GymFlowDatabase): WeightDao = database.weightDao()

    @Provides
    fun provideSyncQueueDao(database: GymFlowDatabase): SyncQueueDao = database.syncQueueDao()

    @Provides
    fun provideExerciseDao(database: GymFlowDatabase): ExerciseDao = database.exerciseDao()
}
