package com.nexus.medi.data.repository.local.room.bloodAndPresure

import com.nexusystem.paguito.data.local.dao.ProductosDao
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexus.medi.data.local.entity.PorductosResponse
import com.nexus.medi.data.local.entity.toEntity
import com.nexusystem.paguito.data.local.session.LocalAccountKey
import com.nexusystem.paguito.data.local.session.LocalAccountSource
import com.nexusystem.paguito.domain.data.ProductosSummary1
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject
import javax.inject.Singleton

/** Catálogo local separado por cuenta, incluidas listas, selectores, conteos e inversión. */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class PorductosRepository @Inject constructor(
    private val recipesDao: ProductosDao,
    private val accounts: LocalAccountSource
) {
    /** Cancela la lectura anterior cuando cambia el perfil, sin borrar sus productos. */
    fun obtenerTodos(): Flow<List<PorductosEntity>> = accounts.accountKeys.flatMapLatest(recipesDao::obtenerTodosProductos)

    fun obtenerNumeroProductos(): Flow<Int> = accounts.accountKeys.flatMapLatest(recipesDao::obtenerNumeroDeProductos)
    fun inversionEnProductos(): Flow<ProductosSummary1> = accounts.accountKeys.flatMapLatest(recipesDao::obtenerInversionProdcutos)

    /** Captura la identidad antes de suspender; una operación antigua nunca se reasigna a la nueva cuenta. */
    suspend fun add(recipe: PorductosEntity, email: String): Long {
        val owner = requireCurrentAccount(email)
        require(recipe.ownerAccount.isEmpty() || recipe.ownerAccount == owner) { "El producto no pertenece a la cuenta activa" }
        recipe.id?.takeIf { it != 0 }?.let { id ->
            val existing = recipesDao.findById(id)
            require(existing == null || existing.ownerAccount == owner) { "El producto no pertenece a la cuenta activa" }
        }
        requireCurrentAccount(email)
        return recipesDao.agregarProducto(recipe.copy(ownerAccount = owner, isSynced = false))
    }

    /** Solo permite eliminar filas cuya identidad fue comprobada al leerlas. */
    suspend fun deleteProduct(recipe: PorductosEntity) {
        val owner = accounts.currentAccountKey()
        require(recipe.ownerAccount == owner) { "El producto no pertenece a la cuenta activa" }
        recipe.id?.let { recipesDao.deleteProduct(it, owner) }
    }

    /** Los IDs remotos se deduplican dentro de la cuenta, nunca contra productos de otro perfil. */
    suspend fun addProductos(recipe: ArrayList<PorductosResponse>, email: String) {
        val owner = requireCurrentAccount(email)
        claimVerifiedLegacyProducts(email, recipe.map { it.idRemoteDatabase })
        val existingIds = recipesDao.getAllRemoteIds(owner).toSet()
        val products = recipe.distinctBy { it.idRemoteDatabase }.filter { it.idRemoteDatabase !in existingIds }
            .map { it.toEntity().copy(ownerAccount = owner, isSynced = true) }
        requireCurrentAccount(email)
        if (products.isNotEmpty()) recipesDao.agregarProductos(products)
    }

    /** Conserva IDs locales y datos anteriores solo cuando el servidor confirma su pertenencia. */
    suspend fun claimVerifiedLegacyProducts(email: String, remoteIds: List<String>) {
        val owner = requireCurrentAccount(email)
        val verified = remoteIds.filter(String::isNotBlank).distinct()
        verified.chunked(900).forEach { recipesDao.claimVerifiedLegacyProducts(owner, it) }
    }

    /** Marca confirmación solo después de que Firestore aceptó la escritura. */
    suspend fun markSynced(email: String, remoteId: String) { recipesDao.markSynced(requireCurrentAccount(email), remoteId) }

    /** Lectura puntual real para publicar el catálogo; no usa emisiones vacías de transición de la interfaz. */
    suspend fun snapshot(email: String): List<PorductosEntity> {
        val owner = requireCurrentAccount(email)
        val products = recipesDao.snapshot(owner)
        requireCurrentAccount(email)
        return products
    }

    /** Rechaza respuestas o acciones pendientes de otra sesión antes de tocar almacenamiento local. */
    private fun requireCurrentAccount(email: String): String {
        val owner = LocalAccountKey.fromEmail(email)
        check(owner != LocalAccountKey.GUEST && owner == accounts.currentAccountKey()) { "La sesión cambió. Vuelve a intentarlo." }
        return owner
    }
}
