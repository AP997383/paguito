package com.nexusystem.paguito.domain.usescases.deudores

import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.repository.local.room.bloodAndPresure.DeudoresRepository
import com.nexusystem.paguito.domain.data.DeudoresSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DeudoresUseCase @Inject constructor(
    private val localRepository: DeudoresRepository
) {
    suspend fun obtenerDeudores(): Flow<List<DeudoresEntity>> = localRepository.obtenerDeudores()

    suspend fun obtenerDatosDeCards(): Flow<DeudoresSummary> = localRepository.obtenerDatosDeCards()
    suspend fun obtener5DatosDeCards(): Flow<DeudoresSummary> = localRepository.obtener5DatosDeCards()

    suspend fun agregarDeudor(data:DeudoresEntity):Long = localRepository.add(data)
    suspend fun aliminarDeudorDeudor(id:String) = localRepository.deleteDeudor(id)
    suspend fun agregarDeudores(data: ArrayList<DeudoresEntity>) = localRepository.addDeudores(data)
    suspend fun updateSync(data:DeudoresEntity) = localRepository.updateSync(data)
    //suspend fun deleteMyRegister(data:MyBloodPresureAndGlucosaEntity) = localRepository.delete(data)
}