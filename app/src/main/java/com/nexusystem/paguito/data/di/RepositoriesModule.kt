package com.nexusystem.paguito.data.di

import android.content.Context
import com.nexus.medi.data.repository.local.room.bloodAndPresure.AbonosRepository
import com.nexus.medi.data.repository.local.room.bloodAndPresure.DeudoresRepository
import com.nexus.medi.data.repository.local.room.bloodAndPresure.PorductosRepository
import com.nexusystem.paguito.data.repository.remote.auth.StorageRepository
import com.nexusystem.paguito.domain.usescases.abonos.AbonosUseCase
import com.nexusystem.paguito.domain.usescases.deudores.DeudoresUseCase
import com.nexusystem.paguito.domain.usescases.perfil.PerfilUseCase
import com.nexusystem.paguito.domain.usescases.productos.ProductosUseCase
import com.nexusystem.paguito.utils.SecureStorageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton


@Module
@InstallIn(ViewModelComponent::class)
object RepositoriesModule {



    @Provides
    fun provideDeudoresUseCase(
        repository: DeudoresRepository
    ): DeudoresUseCase = DeudoresUseCase(repository)

    @Provides
    fun providePerfilUseCase(
        repository: StorageRepository
    ): PerfilUseCase = PerfilUseCase(repository)

    @Provides
    fun provideAbonosUseCase(
        repository: AbonosRepository
    ): AbonosUseCase = AbonosUseCase(repository)

    @Provides
    fun provideProductosUsesCase(
        repository: PorductosRepository,
        storage: StorageRepository
    ): ProductosUseCase = ProductosUseCase(repository,storage)

}