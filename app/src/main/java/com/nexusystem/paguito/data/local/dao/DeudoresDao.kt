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

    @Query("SELECT * FROM DeudoresTable")
    fun obtenerTodosDeudores(): Flow<List<DeudoresEntity>>


        @Query("""
        SELECT 
            COUNT(*) as totalDeudores, 
            COALESCE(SUM(montoActualAdeudado), 0.0) as sumaTotalMontos 
        FROM DeudoresTable
    """)
        fun getDeudoresSummary(): Flow<DeudoresSummary>

    @Query("""
        SELECT 
            COUNT(*) as totalDeudores, 
            COALESCE(SUM(montoActualAdeudado), 0.0) as sumaTotalMontos 
        FROM DeudoresTable
    """)
    fun get5DeudoresSummary(): Flow<DeudoresSummary>
        // O puedes usar 'suspend fun' si no necesitas observar cambios en tiempo real
        @Query("UPDATE DeudoresTable SET montoActualAdeudado = montoActualAdeudado - :monto WHERE id = :deudorId")
        suspend fun restarSaldo(deudorId: Int, monto: Float)

    @Query("UPDATE DeudoresTable SET montoActualAdeudado = montoActualAdeudado + :monto, montoAcomulado = montoAcomulado + :monto WHERE id = :deudorId")
    suspend fun sumarSaldo(deudorId: Int, monto: Float)

    @Query("SELECT * FROM DeudoresTable")
    fun obtenerDeudoresPorFecha(): Flow<List<DeudoresEntity>>

    @Query("SELECT * FROM DeudoresTable")
    fun obtenerMorosos(): Flow<List<DeudoresEntity>>

    @Query("SELECT * FROM DeudoresTable")
    fun obtenerBuenosPagadores(): Flow<List<DeudoresEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarNuevoDeudor(mascota: DeudoresEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarDeudores(productos: List<DeudoresEntity>)

    @Query("SELECT idRemoteDatabase FROM DeudoresTable")
    suspend fun getAllRemoteIds(): List<String>

    @Query("UPDATE DeudoresTable SET inRemote=true WHERE idRemoteDatabase=:id")
    suspend fun updateSync(id: String)

    @Delete
    suspend fun elominarDeudor(mascota: DeudoresEntity)
}