package com.myra.ai.network.client

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

/**
 * Factory for creating configured OkHttpClient instances
 */
object HttpClientFactory {

    /**
     * Create OkHttpClient with production configuration
     */
    fun createProductionClient(enableLogging: Boolean = false): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }

    /**
     * Create OkHttpClient with custom configuration
     */
    fun createCustomClient(
        connectTimeoutSeconds: Long = 30,
        readTimeoutSeconds: Long = 30,
        writeTimeoutSeconds: Long = 30,
        enableLogging: Boolean = false
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
            .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
            .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        if (enableLogging) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        return builder.build()
    }
}
