package com.nexus.medi.data.repository.local.room.bloodAndPresure

import com.nexusystem.paguito.data.local.dao.DeudoresDao
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.data.local.session.LocalAccountSource
import com.nexusystem.paguito.domain.data.DeudoresSummary
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

/** Clientes y resúmenes limitados a la cuenta activa; conserva IDs locales al actualizar desde el servicio. */
@Singleton @OptIn(ExperimentalCoroutinesApi::class)
class DeudoresRepository @Inject constructor(private val recipesDao: DeudoresDao, private val accounts: LocalAccountSource) {
    fun obtenerDeudores(): Flow<List<DeudoresEntity>> = accounts.accountKeys.flatMapLatest(recipesDao::obtenerTodosDeudores)
    fun obtenerDatosDeCards(): Flow<DeudoresSummary> = accounts.accountKeys.flatMapLatest(recipesDao::getDeudoresSummary)
    fun obtener5DatosDeCards(): Flow<DeudoresSummary> = accounts.accountKeys.flatMapLatest(recipesDao::get5DeudoresSummary)

    /** Rechaza edición de una fila ajena incluso si una pantalla conserva un ID anterior. */
    suspend fun add(recipe: DeudoresEntity): Long {
        val owner = accounts.currentAccountKey()
        require(recipe.ownerAccount.isBlank() || recipe.ownerAccount == owner)
        recipe.id?.takeIf { it != 0 }?.let { id -> require(recipesDao.findById(id)?.ownerAccount.let { it == null || it == owner }) }
        return recipesDao.agregarNuevoDeudor(recipe.copy(ownerAccount = owner))
    }
    suspend fun deleteDeudor(id: String) = recipesDao.deleteDeudor(id, accounts.currentAccountKey())
    /** Esta entrada es de importación; la descarga completa se ejecuta atómicamente en AccountSyncCoordinator. */
    suspend fun addDeudores(records: ArrayList<DeudoresEntity>) {
        val owner = accounts.currentAccountKey()
        for (record in records) {
            val previous = recipesDao.findRemote(record.idRemoteDatabase, owner)
            recipesDao.agregarNuevoDeudor(record.copy(id = previous?.id, ownerAccount = owner, inRemote = true))
        }
    }
    suspend fun updateSync(recipe: DeudoresEntity) = recipesDao.updateSync(recipe.idRemoteDatabase, accounts.currentAccountKey())
}
