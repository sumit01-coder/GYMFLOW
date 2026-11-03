package com.gymflow.core.di

import com.gymflow.data.repository.AuthRepositoryImpl
import com.gymflow.data.repository.ExerciseRepositoryImpl
import com.gymflow.data.repository.TrainerRepositoryImpl
import com.gymflow.data.repository.WeightRepositoryImpl
import com.gymflow.data.repository.WorkoutRepositoryImpl
import com.gymflow.domain.repository.AuthRepository
import com.gymflow.domain.repository.ExerciseRepository
import com.gymflow.domain.repository.TrainerRepository
import com.gymflow.domain.repository.WeightRepository
import com.gymflow.domain.repository.WorkoutRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindWorkoutRepository(
        workoutRepositoryImpl: WorkoutRepositoryImpl
    ): WorkoutRepository

    @Binds
    @Singleton
    abstract fun bindTrainerRepository(
        trainerRepositoryImpl: TrainerRepositoryImpl
    ): TrainerRepository

    @Binds
    @Singleton
    abstract fun bindWeightRepository(
        weightRepositoryImpl: WeightRepositoryImpl
    ): WeightRepository

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(
        exerciseRepositoryImpl: ExerciseRepositoryImpl
    ): ExerciseRepository
}
