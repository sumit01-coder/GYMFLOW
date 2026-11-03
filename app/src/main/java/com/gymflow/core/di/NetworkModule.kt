package com.gymflow.core.di

import com.gymflow.core.network.GymFlowApiClient
import com.gymflow.core.network.GymFlowApiClientImpl
import com.gymflow.core.network.KtorClientFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(clientFactory: KtorClientFactory): HttpClient {
        return clientFactory.create()
    }

    @Provides
    @Singleton
    fun provideGymFlowApiClient(httpClient: HttpClient): GymFlowApiClient {
        return GymFlowApiClientImpl(httpClient)
    }
}
