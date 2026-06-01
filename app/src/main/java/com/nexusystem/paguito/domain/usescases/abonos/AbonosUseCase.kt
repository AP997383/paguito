package com.nexusystem.paguito.domain.usescases.abonos

import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.data.repository.local.room.abonos.AbonosRepository
import com.nexusystem.paguito.data.local.entity.PagoConNombre

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AbonosUseCase @Inject constructor(
    private val localRepository: AbonosRepository
) {
    suspend fun obtenerAbonos(idDeudor:String,idRemote:String): Flow<List<PagosEntinty>> = localRepository.obtenerAbonoPorCliente(idDeudor,idRemote)
    suspend fun getLastTiketNumber(): Flow<Int> = localRepository.getLastTicketNumer()
    suspend fun obtenerUltimosAbonos(): Flow<List<PagoConNombre>> = localRepository.obtenerUltimosAbono()
    suspend fun obtenerUltimosAbonos5(): Flow<List<PagoConNombre>> = localRepository.obtenerUltimosAbono5()
    suspend fun obtenerPagosByMonth(fechaInicio: String,fechaFin: String): Flow<List<AbonosDelMes>> = localRepository.obtenerPagosByMonth(fechaInicio,fechaFin)
   suspend fun guardarAbono(data: PagosEntinty) = localRepository.add(data)
    suspend fun eliminarAbono(data: PagosEntinty) = localRepository.eliminar(data)
    suspend fun eliminarAbonos(idDeudor: String) = localRepository.eliminarAbonos(idDeudor)
    suspend fun guardarAbonos(data: ArrayList<PagosEntinty>) = localRepository.addPagosOrVentas(data)
    suspend fun guardarVenta(data: PagosEntinty) = localRepository.addVenta(data)
    suspend fun guardarprimeraVenta(data: PagosEntinty) = localRepository.addprimeraVenta(data)

    suspend fun updateVenta(data: PagosEntinty) = localRepository.updateVenta(data)
   // suspend fun borrarPago(data:PagosEntinty) = localRepository.b(data)
}