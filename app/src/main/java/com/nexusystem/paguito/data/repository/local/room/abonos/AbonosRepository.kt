package com.nexusystem.paguito.data.repository.local.room.abonos

import com.nexusystem.paguito.data.local.dao.AbonosDao
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexusystem.paguito.data.local.dao.DeudoresDao
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.data.local.session.LocalAccountSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

/** Historial por cuenta; importar un movimiento no vuelve a modificar el saldo del cliente. */
@Singleton @OptIn(ExperimentalCoroutinesApi::class)
class AbonosRepository @Inject constructor(private val recipesDao: AbonosDao, private val deudoresDao: DeudoresDao, private val accounts: LocalAccountSource) {
    fun obtenerAbonoPorCliente(idCliente: String, idRemote: String): Flow<List<PagosEntinty>> = accounts.accountKeys.flatMapLatest { owner ->
        if (idRemote.isNotBlank()) recipesDao.obtenerAbonosPorClienteIdRemote(idRemote, owner) else recipesDao.obtenerAbonosPorCliente(idCliente, owner)
    }
    fun getLastTicketNumer(): Flow<Int> = accounts.accountKeys.flatMapLatest(recipesDao::getNetNumberTicket)
    fun obtenerUltimosAbono(): Flow<List<PagoConNombre>> = accounts.accountKeys.flatMapLatest(recipesDao::getPagosConNombre)
    fun obtenerUltimosAbono5(): Flow<List<PagoConNombre>> = accounts.accountKeys.flatMapLatest(recipesDao::getPagosConNombre5)
    fun obtenerPagosByMonth(fechaInicio: String, fechaFin: String): Flow<List<AbonosDelMes>> = accounts.accountKeys.flatMapLatest { recipesDao.obtenerPagosByMonth(fechaInicio, fechaFin, it) }

    /** Captura propietario y comprueba los IDs del movimiento y del cliente antes de cualquier transacción. */
    private suspend fun owned(record: PagosEntinty): PagosEntinty {
        val owner = accounts.currentAccountKey()
        require(record.ownerAccount.isBlank() || record.ownerAccount == owner)
        record.id?.takeIf { it != 0 }?.let { id -> require(recipesDao.findById(id)?.ownerAccount.let { it == null || it == owner }) }
        record.idDeudor.toIntOrNull()?.let { id -> require(deudoresDao.findById(id)?.ownerAccount == owner) }
        return record.copy(ownerAccount = owner, isSynced = record.isSynced)
    }
    suspend fun add(recipe: PagosEntinty) = recipesDao.registrarAbonoYActualizarDeudor(owned(recipe), deudoresDao)
    suspend fun eliminar(recipe: PagosEntinty) = recipesDao.eliminaAbonoyActualizaMonto(owned(recipe), deudoresDao)
    suspend fun eliminarAbonos(idDeudor: String) = recipesDao.borrarAbonosYventas(idDeudor, accounts.currentAccountKey())
    suspend fun addVenta(recipe: PagosEntinty) = recipesDao.registrarVentaYActualizarDeudor(owned(recipe), deudoresDao)
    suspend fun addprimeraVenta(recipe: PagosEntinty) = recipesDao.registrarPrimeraVentaYActualizarDeudor(owned(recipe), deudoresDao)
    suspend fun updateVenta(recipe: PagosEntinty) = recipesDao.actualizarAbono(owned(recipe))
    suspend fun addPagosOrVentas(records: ArrayList<PagosEntinty>) {
        val owner = accounts.currentAccountKey()
        for (record in records) {
            val previous = recipesDao.findRemote(record.idRemoteDatabase, record.idDeudorRemoteDatabase, record.isIngreso, owner)
            recipesDao.agregarAbono(record.copy(id = previous?.id, ownerAccount = owner))
        }
    }
}
