package com.nexusystem.paguito.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.domain.data.DeudoresSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface  DeudoresDao {

    @Query("SELECT * FROM DeudoresTable WHERE ownerAccount = :owner")
    fun obtenerTodosDeudores(owner: String): Flow<List<DeudoresEntity>>


        @Query("""
        SELECT
            COUNT(*) as totalDeudores,
            COALESCE(SUM(CASE WHEN montoActualAdeudado < 0 THEN montoActualAdeudado ELSE montoActualAdeudado - MAX(creditBalance, 0) END), 0.0) as sumaTotalMontos
        FROM DeudoresTable WHERE ownerAccount = :owner
    """)
        fun getDeudoresSummary(owner: String): Flow<DeudoresSummary>

    @Query("""
        SELECT
            COUNT(*) as totalDeudores,
            COALESCE(SUM(CASE WHEN montoActualAdeudado < 0 THEN montoActualAdeudado ELSE montoActualAdeudado - MAX(creditBalance, 0) END), 0.0) as sumaTotalMontos
        FROM DeudoresTable WHERE ownerAccount = :owner
    """)
    fun get5DeudoresSummary(owner: String): Flow<DeudoresSummary>
        // O puedes usar 'suspend fun' si no necesitas observar cambios en tiempo real
        @Query("UPDATE DeudoresTable SET creditBalance = MAX(0, -((CASE WHEN montoActualAdeudado < 0 THEN montoActualAdeudado ELSE montoActualAdeudado - MAX(creditBalance, 0) END) - :monto)), montoActualAdeudado = MAX(0, (CASE WHEN montoActualAdeudado < 0 THEN montoActualAdeudado ELSE montoActualAdeudado - MAX(creditBalance, 0) END) - :monto) WHERE id = :deudorId AND ownerAccount = :owner")
        suspend fun restarSaldo(deudorId: Int, monto: Float, owner: String)

    @Query("UPDATE DeudoresTable SET creditBalance = MAX(0, -((CASE WHEN montoActualAdeudado < 0 THEN montoActualAdeudado ELSE montoActualAdeudado - MAX(creditBalance, 0) END) + :monto)), montoActualAdeudado = MAX(0, (CASE WHEN montoActualAdeudado < 0 THEN montoActualAdeudado ELSE montoActualAdeudado - MAX(creditBalance, 0) END) + :monto), montoAcomulado = montoAcomulado + :monto WHERE id = :deudorId AND ownerAccount = :owner")
    suspend fun sumarSaldo(deudorId: Int, monto: Float, owner: String)

    @Query("SELECT * FROM DeudoresTable WHERE ownerAccount = :owner")
    fun obtenerDeudoresPorFecha(owner: String): Flow<List<DeudoresEntity>>

    @Query("SELECT * FROM DeudoresTable WHERE ownerAccount = :owner")
    fun obtenerMorosos(owner: String): Flow<List<DeudoresEntity>>

    @Query("SELECT * FROM DeudoresTable WHERE ownerAccount = :owner")
    fun obtenerBuenosPagadores(owner: String): Flow<List<DeudoresEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarNuevoDeudor(mascota: DeudoresEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarDeudores(productos: List<DeudoresEntity>)

    @Query("SELECT idRemoteDatabase FROM DeudoresTable WHERE ownerAccount = :owner")
    suspend fun getAllRemoteIds(owner: String): List<String>

    @Query("UPDATE DeudoresTable SET inRemote=true WHERE idRemoteDatabase=:id AND ownerAccount = :owner")
    suspend fun updateSync(id: String, owner: String)

    @Query("DELETE FROM DeudoresTable  WHERE idRemoteDatabase=:id AND ownerAccount = :owner")
    suspend fun deleteDeudor(id: String, owner: String)

    @Query("DELETE FROM DeudoresTable WHERE id = :id AND ownerAccount = :owner")
    suspend fun elominarDeudor(id: Int, owner: String)
    @Query("SELECT * FROM DeudoresTable WHERE ownerAccount = :owner")
    suspend fun snapshot(owner: String): List<DeudoresEntity>
    @Query("SELECT * FROM DeudoresTable WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): DeudoresEntity?
    @Query("SELECT * FROM DeudoresTable WHERE idRemoteDatabase = :remoteId AND (ownerAccount = :owner OR ownerAccount = '') ORDER BY CASE WHEN ownerAccount = :owner THEN 0 ELSE 1 END LIMIT 1")
    suspend fun findRemote(remoteId: String, owner: String): DeudoresEntity?
}
