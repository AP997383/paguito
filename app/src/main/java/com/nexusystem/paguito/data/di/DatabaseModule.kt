package com.nexus.composeaprendizaje.data.di

import android.content.Context
import androidx.room.Room
import com.nexusystem.paguito.data.local.dao.ProductosDao
import com.nexusystem.paguito.data.local.dao.AbonosDao
import com.nexusystem.paguito.data.local.dao.DeudoresDao
import com.nexusystem.paguito.data.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    fun providesDeudoresDao(database: AppDatabase): DeudoresDao{
        return database.deudoresDao()
    }

    @Provides
    fun providesAbonosDao(database: AppDatabase): AbonosDao{
        return database.abonosDao()
    }

    @Provides
    fun providesproductosDao(database: AppDatabase): ProductosDao{
        return database.productosDao()
    }


    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext appContext: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "paguito_db"
        )
            .build()
    }

}