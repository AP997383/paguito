package com.nexusystem.paguito.data.repository

import com.google.firebase.firestore.util.CustomClassMapper
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexus.medi.data.local.entity.PorductosResponse
import com.nexus.medi.data.repository.local.room.bloodAndPresure.PorductosRepository
import com.nexusystem.paguito.data.local.dao.ProductosDao
import com.nexusystem.paguito.data.local.session.LocalAccountKey
import com.nexusystem.paguito.data.local.session.LocalAccountSource
import com.nexusystem.paguito.domain.data.ProductosSummary1
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test

/** Regresiones con las clases reales del repositorio; sin borrar datos ni usar dispositivos o servicios. */
class ProductAccountIsolationTest {
    private val first = "first@example.invalid"
    private val second = "second@example.invalid"
    private val accounts = Accounts(first)
    private val dao = Products()
    private val repository = PorductosRepository(dao, accounts)

    @Test fun changingAccountSeparatesProductsCountsAndInvestmentAndPreservesPreviousData() = runBlocking {
        repository.add(PorductosEntity(id = null, nombre = "Solo A", precioOriginal = 50f), first)
        assertEquals("Solo A", repository.obtenerTodos().first().single().nombre)
        accounts.select(second)
        assertTrue(repository.obtenerTodos().first().isEmpty())
        assertEquals(0, repository.obtenerNumeroProductos().first())
        assertEquals(0f, repository.inversionEnProductos().first().totalCostoProductos)
        repository.add(PorductosEntity(id = null, nombre = "Solo B", precioOriginal = 90f), second)
        assertEquals("Solo B", repository.snapshot(second).single().nombre)
        accounts.select(first)
        assertEquals("Solo A", repository.obtenerTodos().first().single().nombre)
        assertEquals(50f, repository.inversionEnProductos().first().totalCostoProductos)
        assertEquals(2, dao.rows.value.size)
    }

    @Test fun existingCollectorSwitchesToTheNewOwnerWithoutRestartingTheScreen() = runBlocking {
        repository.add(PorductosEntity(id = null, nombre = "A"), first)
        val emissions = Channel<List<PorductosEntity>>(Channel.UNLIMITED)
        val job = launch(start = CoroutineStart.UNDISPATCHED) { repository.obtenerTodos().collect { emissions.send(it) } }
        withTimeout(5000) { assertEquals("A", emissions.receive().single().nombre) }
        accounts.select(second)
        withTimeout(5000) { assertTrue(emissions.receive().isEmpty()) }
        job.cancel()
    }

    @Test fun unknownLegacyAndGuestProductsAreNeverAdoptedByAnotherAccount() = runBlocking {
        dao.rows.value = listOf(
            PorductosEntity(id = 1, nombre = "Sin dueño"),
            PorductosEntity(id = 2, nombre = "Invitado", ownerAccount = LocalAccountKey.GUEST))
        assertTrue(repository.obtenerTodos().first().isEmpty())
        accounts.select(null)
        assertEquals("Invitado", repository.obtenerTodos().first().single().nombre)
        accounts.select(second)
        assertTrue(repository.obtenerTodos().first().isEmpty())
        assertEquals(2, dao.rows.value.size)
    }

    @Test fun accountCannotEditDeleteOrPublishAnotherAccountsProducts() = runBlocking {
        repository.add(PorductosEntity(id = null, nombre = "Privado"), first)
        val product = repository.snapshot(first).single()
        accounts.select(second)
        expectRejected { repository.deleteProduct(product) }
        expectRejected { repository.add(product.copy(ownerAccount = ""), second) }
        expectRejected { repository.snapshot(first) }
        assertEquals("Privado", dao.rows.value.single().nombre)
    }

    @Test fun delayedDownloadNeverBecomesNewAccountsCatalog() = runBlocking {
        dao.onReadIds = { accounts.select(second) }
        expectRejected { repository.addProductos(arrayListOf(PorductosResponse(nombre = "A", idRemoteDatabase = "remote-a")), first) }
        assertTrue(dao.rows.value.isEmpty())
    }

    @Test fun remoteIdsAreDeduplicatedWithinEachAccount() = runBlocking {
        val remote = arrayListOf(PorductosResponse(nombre = "Cuenta", idRemoteDatabase = "same-id"))
        repository.addProductos(remote, first)
        repository.addProductos(remote, first)
        accounts.select(second)
        repository.addProductos(remote, second)
        assertEquals(2, dao.rows.value.size)
        assertEquals(1, repository.obtenerNumeroProductos().first())
    }

    @Test fun verifiedLegacyRecoveryPreservesLocalIdsAndLeavesUnknownRowsHidden() = runBlocking {
        dao.rows.value = listOf(PorductosEntity(id = 17, idRemoteDatabase = "verified"), PorductosEntity(id = 18, idRemoteDatabase = "unknown"))
        repository.claimVerifiedLegacyProducts(first, listOf("verified"))
        assertEquals(17, repository.obtenerTodos().first().single().id)
        assertEquals("", dao.rows.value.single { it.id == 18 }.ownerAccount)
        accounts.select(second)
        assertTrue(repository.obtenerTodos().first().isEmpty())
    }

    @Test fun ownerKeyIsStablePrivateAndNeverSerializedToFirestore() {
        assertEquals(LocalAccountKey.fromEmail(first), LocalAccountKey.fromEmail(" FIRST@example.invalid "))
        assertNotEquals(LocalAccountKey.fromEmail(first), LocalAccountKey.fromEmail(second))
        assertFalse(LocalAccountKey.fromEmail(first).contains(first))
        val wire = CustomClassMapper.convertToPlainJavaTypes(PorductosEntity(ownerAccount = LocalAccountKey.fromEmail(first))) as Map<*, *>
        assertFalse(wire.containsKey("ownerAccount"))
    }

    private suspend fun expectRejected(action: suspend () -> Unit) {
        try { action(); fail("Una acción de otra cuenta debe rechazarse") }
        catch (_: IllegalStateException) {} catch (_: IllegalArgumentException) {}
    }

    private class Accounts(email: String?) : LocalAccountSource {
        private val key = MutableStateFlow(LocalAccountKey.fromEmail(email))
        override val accountKeys: Flow<String> = key
        override fun currentAccountKey() = key.value
        fun select(email: String?) { key.value = LocalAccountKey.fromEmail(email) }
    }
    private class Products : ProductosDao {
        val rows = MutableStateFlow<List<PorductosEntity>>(emptyList())
        var onReadIds: (() -> Unit)? = null
        override fun obtenerTodosProductos(owner: String) = rows.map { it.filter { p -> p.ownerAccount == owner } }
        override suspend fun snapshot(owner: String) = rows.value.filter { it.ownerAccount == owner }
        override suspend fun markSynced(owner: String, remoteId: String) { rows.value = rows.value.map { if (it.ownerAccount == owner && it.idRemoteDatabase == remoteId) it.copy(isSynced = true) else it } }
        override suspend fun findById(id: Int) = rows.value.find { it.id == id }
        override suspend fun agregarProducto(producto: PorductosEntity): Long {
            val id = producto.id ?: ((rows.value.maxOfOrNull { it.id ?: 0 } ?: 0) + 1)
            rows.value = rows.value.filterNot { it.id == id } + producto.copy(id = id)
            return id.toLong()
        }
        override suspend fun deleteProduct(id: Int, owner: String) { rows.value = rows.value.filterNot { it.id == id && it.ownerAccount == owner } }
        override suspend fun agregarProductos(productos: List<PorductosEntity>) { productos.forEach { agregarProducto(it) } }
        override suspend fun getAllRemoteIds(owner: String): List<String> { onReadIds?.invoke(); return snapshot(owner).map { it.idRemoteDatabase } }
        override fun obtenerNumeroDeProductos(owner: String) = obtenerTodosProductos(owner).map { it.size }
        override fun obtenerInversionProdcutos(owner: String) = obtenerTodosProductos(owner).map { ProductosSummary1(it.size, it.sumOf { p -> p.precioOriginal.toDouble() }.toFloat()) }
        override fun obtenerProductoPorNombre(nombre: String, owner: String) = obtenerTodosProductos(owner).map { it.filter { p -> p.nombre == nombre } }
        override suspend fun claimVerifiedLegacyProducts(owner: String, remoteIds: List<String>) {
            rows.value = rows.value.map { if (it.ownerAccount.isEmpty() && it.idRemoteDatabase in remoteIds) it.copy(ownerAccount = owner) else it }
        }
    }
}
