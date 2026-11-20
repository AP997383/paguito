package com.nexusystem.paguito.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.domain.data.ProductosSummary1
import kotlinx.coroutines.flow.Flow

/** Todas las lecturas y bajas exigen propietario; nunca se consulta un catálogo compartido. */
@Dao
interface ProductosDao {
    @Query("SELECT * FROM ProductosTble WHERE ownerAccount = :owner")
    fun obtenerTodosProductos(owner: String): Flow<List<PorductosEntity>>

    @Query("SELECT * FROM ProductosTble WHERE ownerAccount = :owner")
    suspend fun snapshot(owner: String): List<PorductosEntity>

    /** Solo recupera filas antiguas con IDs comprobados en la colección remota de esta cuenta. */
    @Query("UPDATE ProductosTble SET ownerAccount = :owner WHERE ownerAccount = '' AND idRemoteDatabase IN (:remoteIds)")
    suspend fun claimVerifiedLegacyProducts(owner: String, remoteIds: List<String>)

    @Query("UPDATE ProductosTble SET isSynced = 1 WHERE ownerAccount = :owner AND idRemoteDatabase = :remoteId")
    suspend fun markSynced(owner: String, remoteId: String)
    @Query("SELECT * FROM ProductosTble WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): PorductosEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarProducto(producto: PorductosEntity): Long

    @Query("DELETE FROM ProductosTble WHERE id = :id AND ownerAccount = :owner")
    suspend fun deleteProduct(id: Int, owner: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarProductos(productos: List<PorductosEntity>)

    @Query("SELECT idRemoteDatabase FROM ProductosTble WHERE ownerAccount = :owner")
    suspend fun getAllRemoteIds(owner: String): List<String>

    @Query("SELECT COUNT(*) FROM ProductosTble WHERE ownerAccount = :owner")
    fun obtenerNumeroDeProductos(owner: String): Flow<Int>

    @Query("SELECT COUNT(*) AS numeroProductos, COALESCE(SUM(precioOriginal), 0.0) AS totalCostoProductos FROM ProductosTble WHERE ownerAccount = :owner")
    fun obtenerInversionProdcutos(owner: String): Flow<ProductosSummary1>

    @Query("SELECT * FROM ProductosTble WHERE nombre = :nombre AND ownerAccount = :owner")
    fun obtenerProductoPorNombre(nombre: String, owner: String): Flow<List<PorductosEntity>>
}
