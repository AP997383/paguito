package com.nexusystem.paguito.data.di

import android.content.Context
import com.nexusystem.paguito.data.local.session.LocalAccountSource
import com.nexusystem.paguito.utils.SecureStorageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // <--- CAMBIO CLAVE
object AppModule {
    /** Comparte la misma identidad con repositorios y cambios de sesión desde las pantallas. */
    @Provides
    fun provideLocalAccountSource(storage: SecureStorageManager): LocalAccountSource = storage


    @Provides
    @Singleton // Ahora sí es compatible porque está en SingletonComponent
    fun provideSecureStorageManager(
        @ApplicationContext context: Context
    ): SecureStorageManager {
        return SecureStorageManager(context)
    }
}