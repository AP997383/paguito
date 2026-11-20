package com.nexusystem.paguito.domain.usescases.productos

import android.net.Uri
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexus.medi.data.local.entity.PorductosResponse
import com.nexus.medi.data.repository.local.room.bloodAndPresure.PorductosRepository
import com.nexusystem.paguito.data.repository.remote.auth.StorageRepository
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.domain.data.ProductosSummary1
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProductosUseCase @Inject constructor(
    private val localRepository: PorductosRepository,
    private val storageRepository: StorageRepository
) {
    suspend fun obtenerProductos(): Flow<List<PorductosEntity>> = localRepository.obtenerTodos()
    suspend fun obtenerNumeroProductos(): Flow<Int> = localRepository.obtenerNumeroProductos()
    suspend fun inversionEnProductos(): Flow<ProductosSummary1> = localRepository.inversionEnProductos()

     suspend fun guardarProducto(data:PorductosEntity, email: String):Long = localRepository.add(data, email)
    suspend fun deleteProduct(data:PorductosEntity) = localRepository.deleteProduct(data)
    suspend fun guardarProductos(data: ArrayList<PorductosResponse>, email: String) = localRepository.addProductos(data, email)
    /** Restaura propiedad histórica únicamente con IDs verificados en la colección de la cuenta. */
    suspend fun claimVerifiedLegacyProducts(email: String, remoteIds: List<String>) =
        localRepository.claimVerifiedLegacyProducts(email, remoteIds)

    /** Confirmación del producto remoto sin volver a crear el registro local. */
    suspend fun markSynced(email: String, remoteId: String) = localRepository.markSynced(email, remoteId)

    /** Consulta exclusivamente el catálogo de la cuenta que inició la publicación. */
    suspend fun snapshot(email: String): List<PorductosEntity> = localRepository.snapshot(email)
 //   suspend fun deleteMyRegister(data:MyBloodPresureAndGlucosaEntity) = localRepository.delete(data)

    suspend fun guardarFoto(userId: String, imageUri: Uri): String =
        storageRepository.uploadProductImage(userId,imageUri).toString()
}