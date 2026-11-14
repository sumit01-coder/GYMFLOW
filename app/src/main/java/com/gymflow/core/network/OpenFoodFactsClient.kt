package com.gymflow.core.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpenFoodFactsClient @Inject constructor() {

    private val httpClient by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 12000
                connectTimeoutMillis = 8000
                socketTimeoutMillis = 12000
            }
        }
    }

    suspend fun getProductByBarcode(barcode: String): Result<OpenFoodProduct> {
        return try {
            val trimmedBarcode = barcode.trim()
            val url = "https://world.openfoodfacts.org/api/v2/product/$trimmedBarcode.json"
            val response: OpenFoodFactsResponse = httpClient.get(url) {
                header(HttpHeaders.UserAgent, "GymFlow-Android/1.0.41 (support@gymflow.com)")
            }.body()

            if (response.status == 1 && response.product != null) {
                Result.success(response.product)
            } else {
                Result.failure(Exception(response.statusVerbose ?: "Product not found in Open Food database"))
            }
        } catch (e: Exception) {
            Log.e("OpenFoodFactsClient", "Error fetching barcode $barcode", e)
            Result.failure(e)
        }
    }
}
