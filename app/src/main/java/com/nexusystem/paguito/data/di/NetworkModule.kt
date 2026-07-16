// Path:
// app/src/main/java/com/nexusystem/paguito/data/di/NetworkModule.kt

package com.nexusystem.paguito.data.di

import com.nexusecosystem.nexuspayment.config.NexusPayHostConfig
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.data.remote.cloudFunctions.AuthApi
import com.nexusystem.paguito.data.remote.cloudFunctions.CatalogApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val FIREBASE_BASE_URL =
        "https://us-central1-paguito.cloudfunctions.net/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideNexusPayHostConfig(): NexusPayHostConfig {
        return object : NexusPayHostConfig {
            override val baseUrl: String
                get() = BuildConfig.BASE_URL
        }
    }

    @Provides
    @Singleton
    @Named("FirebaseRetrofit")
    fun provideFirebaseRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(FIREBASE_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    @Provides
    @Singleton
    @Named("AwsRetrofit")
    fun provideAwsRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(
                normalizeBaseUrl(
                    BuildConfig.BASE_URL
                )
            )
            .client(okHttpClient)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        @Named("FirebaseRetrofit")
        retrofit: Retrofit
    ): AuthApi {
        return retrofit.create(
            AuthApi::class.java
        )
    }

    @Provides
    @Singleton
    fun provideCatalogApi(
        @Named("AwsRetrofit")
        retrofit: Retrofit
    ): CatalogApi {
        return retrofit.create(
            CatalogApi::class.java
        )
    }

    private fun normalizeBaseUrl(
        baseUrl: String
    ): String {
        val cleanBaseUrl = baseUrl.trim()

        require(cleanBaseUrl.isNotEmpty()) {
            "BuildConfig.BASE_URL no puede estar vacío."
        }

        return if (cleanBaseUrl.endsWith("/")) {
            cleanBaseUrl
        } else {
            "$cleanBaseUrl/"
        }
    }
}