package com.nexusystem.paguito.domain.usescases.perfil

import android.net.Uri
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.repository.local.room.bloodAndPresure.DeudoresRepository
import com.nexusystem.paguito.data.repository.remote.auth.StorageRepository
import com.nexusystem.paguito.domain.data.DeudoresSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PerfilUseCase @Inject constructor(
    private val storageRepository: StorageRepository
) {
    suspend fun guardarFoto(userId: String, imageUri: Uri): String =
        storageRepository.uploadProfileImage(userId,imageUri).toString()
}