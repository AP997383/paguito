package com.nexusystem.paguito.data.sync

import androidx.room.withTransaction
import com.nexusystem.paguito.data.AppDatabase
import com.nexusystem.paguito.data.local.session.LocalAccountSource
import com.nexusystem.paguito.data.local.session.LocalAccountKey
import com.nexusystem.paguito.data.repository.remote.auth.AuthRepository
import com.nexus.medi.data.local.entity.toEntity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/** El servicio es la fuente compartida; una lectura actualiza la caché completa sin volver a subirla. */
@Singleton
class AccountSyncCoordinator @Inject constructor(private val remote: AuthRepository, private val database: AppDatabase, private val accounts: LocalAccountSource) {
    private val mutex = Mutex()
    private var lastOwner = ""
    private var lastSuccessNanos = 0L

    /** Agrupa consultas simultáneas y descarta respuestas pertenecientes a una sesión anterior. */
    suspend fun refresh(email: String, force: Boolean = false) = mutex.withLock {
        val owner = LocalAccountKey.fromEmail(email)
        if (owner == LocalAccountKey.GUEST || owner != accounts.currentAccountKey()) return@withLock
        if (!force && lastOwner == owner && System.nanoTime() - lastSuccessNanos < 30_000_000_000L) return@withLock
        val response = remote.getAllMyData(email.trim())
        check(owner == accounts.currentAccountKey()) { "La sesión cambió durante la consulta" }
        check(response.email.isBlank() || LocalAccountKey.fromEmail(response.email) == owner)
        AccountSnapshotValidation.validate(response)
        database.withTransaction {
            check(owner == accounts.currentAccountKey())
            val products = database.productosDao()
            val clients = database.deudoresDao()
            val payments = database.abonosDao()
            val productIds = response.productos.map { it.idRemoteDatabase }.filter(String::isNotBlank)
            productIds.chunked(900).forEach { products.claimVerifiedLegacyProducts(owner, it) }
            val currentProducts = products.snapshot(owner).associateBy { it.idRemoteDatabase }
            for (product in response.productos) {
                products.agregarProducto(product.toEntity().copy(id = currentProducts[product.idRemoteDatabase]?.id, ownerAccount = owner, isSynced = true))
            }
            val activeClients = response.deudores.filter { !it.isDeleted }
            val clientIds = activeClients.map { it.idRemoteDatabase }.toSet()
            val movementKeys = mutableSetOf<String>()
            for (client in activeClients) {
                require(client.idRemoteDatabase.isNotBlank()) { "Cliente sin identidad remota" }
                val existing = clients.findRemote(client.idRemoteDatabase, owner)
                val localId = clients.agregarNuevoDeudor(client.copy(id = existing?.id, inRemote = true, ownerAccount = owner))
                for ((income, records) in listOf(true to client.pagos, false to client.ventas)) {
                    for (movement in records) {
                        require(movement.idRemoteDatabase.isNotBlank()) { "Movimiento sin identidad remota" }
                        require(movementKeys.add("${client.idRemoteDatabase}|$income|${movement.idRemoteDatabase}")) { "Movimiento duplicado en la respuesta" }
                        val previous = payments.findRemote(movement.idRemoteDatabase, client.idRemoteDatabase, income, owner)
                        payments.agregarAbono(movement.copy(id = previous?.id, idDeudor = localId.toString(), idDeudorRemoteDatabase = client.idRemoteDatabase, isIngreso = income, ownerAccount = owner, isSynced = true))
                    }
                }
            }
            // Solo se retiran copias confirmadas ausentes de una lectura completa; los pendientes se conservan.
            for (row in products.snapshot(owner)) if (row.isSynced && row.idRemoteDatabase.isNotBlank() && row.idRemoteDatabase !in productIds) row.id?.let { products.deleteProduct(it, owner) }
            for (row in clients.snapshot(owner)) if (row.inRemote && row.idRemoteDatabase.isNotBlank() && row.idRemoteDatabase !in clientIds) row.id?.let { clients.elominarDeudor(it, owner) }
            for (row in payments.snapshot(owner)) if (row.isSynced && row.idRemoteDatabase.isNotBlank() && "${row.idDeudorRemoteDatabase}|${row.isIngreso}|${row.idRemoteDatabase}" !in movementKeys) row.id?.let { payments.deleteOwned(it, owner) }
        }
        lastOwner = owner
        lastSuccessNanos = System.nanoTime()
    }
}
