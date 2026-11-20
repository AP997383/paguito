package com.nexusystem.paguito.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import kotlinx.coroutines.flow.Flow

@Dao
interface  AbonosDao {

    @Query("SELECT * FROM PagosTable WHERE ownerAccount = :owner")
    fun obtenerTodosLosAbonos(owner: String): Flow<List<PagosEntinty>>

    @Query("""
        SELECT
            p.id, p.idRemoteDatabase, p.saldoAntesDeAbono,
            p.idDeudor,
            p.idDeudorRemoteDatabase,
            COALESCE(d.nombre, 'Cliente no disponible') AS nameDeudor,
            p.montoAbonado,
            p.fechaAbono,
            p.pagoATiempo,
            p.tipoPago,
            p.isIngreso,
            p.notas,
            p.jsonAbonoPorProducto
        FROM PagosTable AS p
        LEFT JOIN DeudoresTable AS d ON d.ownerAccount = p.ownerAccount AND ((p.idDeudorRemoteDatabase != '' AND p.idDeudorRemoteDatabase = CAST(d.idRemoteDatabase AS TEXT)) OR (p.idDeudorRemoteDatabase = '' AND p.idDeudor = CAST(d.id AS TEXT)))
    WHERE p.ownerAccount = :owner
           ORDER BY substr(p.fechaAbono,1,10) DESC, REPLACE(p.fechaAbono, ' ', 'T') DESC, p.idRemoteDatabase DESC, p.idDeudorRemoteDatabase DESC, p.isIngreso DESC, p.id DESC
    """)
    fun getPagosConNombre(owner: String): Flow<List<PagoConNombre>>

    @Query("""
    SELECT
        p.id, p.idRemoteDatabase, p.saldoAntesDeAbono,
        p.idDeudor,
            p.idDeudorRemoteDatabase,
        COALESCE(d.nombre, 'Cliente no disponible') AS nameDeudor,
        p.montoAbonado,
        p.fechaAbono,
        p.pagoATiempo,
        p.tipoPago,
        p.isIngreso,
        p.notas,
        p.jsonAbonoPorProducto
    FROM PagosTable AS p
    LEFT JOIN DeudoresTable AS d ON d.ownerAccount = p.ownerAccount AND ((p.idDeudorRemoteDatabase != '' AND p.idDeudorRemoteDatabase = CAST(d.idRemoteDatabase AS TEXT)) OR (p.idDeudorRemoteDatabase = '' AND p.idDeudor = CAST(d.id AS TEXT)))
    WHERE p.ownerAccount = :owner
    ORDER BY substr(p.fechaAbono,1,10) DESC, REPLACE(p.fechaAbono, ' ', 'T') DESC, p.idRemoteDatabase DESC, p.idDeudorRemoteDatabase DESC, p.isIngreso DESC, p.id DESC
    LIMIT 5
""")
    fun getPagosConNombre5(owner: String): Flow<List<PagoConNombre>>

    @Query("""
        SELECT
            p.montoAbonado,
            p.fechaAbono,
            p.isIngreso
        FROM PagosTable AS p WHERE p.ownerAccount = :owner AND substr(fechaAbono,1,10) BETWEEN :fechaInicio AND :fechaFin
    """)
    fun obtenerPagosByMonth(fechaInicio: String,fechaFin: String, owner: String): Flow<List<AbonosDelMes>>

    @Query("SELECT * FROM PagosTable WHERE idDeudor=:idCiente AND ownerAccount = :owner")
    fun obtenerAbonosPorCliente(idCiente: String, owner: String): Flow<List<PagosEntinty>>
    @Query("SELECT * FROM PagosTable WHERE idDeudorRemoteDatabase=:idCiente AND ownerAccount = :owner")
    fun obtenerAbonosPorClienteIdRemote(idCiente: String, owner: String): Flow<List<PagosEntinty>>

    @Query("SELECT COUNT(*) FROM PagosTable WHERE ownerAccount = :owner")
    fun getNetNumberTicket(owner: String): Flow<Int>
    @Query("SELECT * FROM PagosTable WHERE ownerAccount = :owner")

    fun obtenerAbonosPorProducto(owner: String): Flow<List<PagosEntinty>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarAbono(mascota: PagosEntinty)

    @Transaction
    suspend fun registrarAbonoYActualizarDeudor(pago: PagosEntinty, deudoresDao: DeudoresDao) {
        // Una recepción en tiempo real puede haber importado ya este movimiento confirmado.
        if (pago.idRemoteDatabase.isNotBlank() && findRemote(pago.idRemoteDatabase, pago.idDeudorRemoteDatabase, pago.isIngreso, pago.ownerAccount)?.ownerAccount == pago.ownerAccount) return
        // 1. Guardamos el abono
        agregarAbono(pago)
        // 2. Restamos el monto al deudor (convertimos el idDeudor String a Int)
        val idInt = pago.idDeudor.toIntOrNull() ?: 0
        deudoresDao.restarSaldo(idInt, pago.montoAbonado.toFloat(), pago.ownerAccount)
    }

    @Transaction
    suspend fun eliminaAbonoyActualizaMonto(pago: PagosEntinty, deudoresDao: DeudoresDao) {
        // 1. Guardamos el abono
        pago.id?.let { deleteOwned(it, pago.ownerAccount) }
        val idInt = pago.idDeudor.toIntOrNull() ?: 0
        deudoresDao.sumarSaldo(idInt, pago.montoAbonado.toFloat(), pago.ownerAccount)
    }
    @Query("DELETE  FROM PagosTable WHERE idDeudorRemoteDatabase=:idCiente AND ownerAccount = :owner")
    suspend fun borrarAbonosYventas(idCiente: String, owner: String)



    @Transaction
    suspend fun registrarVentaYActualizarDeudor(pago: PagosEntinty, deudoresDao: DeudoresDao) {
        // Una recepción en tiempo real puede haber importado ya este movimiento confirmado.
        if (pago.idRemoteDatabase.isNotBlank() && findRemote(pago.idRemoteDatabase, pago.idDeudorRemoteDatabase, pago.isIngreso, pago.ownerAccount)?.ownerAccount == pago.ownerAccount) return
        // 1. Guardamos el abono
        agregarAbono(pago)
        // 2. Restamos el monto al deudor (convertimos el idDeudor String a Int)
        val idInt = pago.idDeudor.toIntOrNull() ?: 0
        deudoresDao.sumarSaldo(idInt, pago.montoAbonado.toFloat(), pago.ownerAccount)
    }

    @Transaction
    suspend fun registrarPrimeraVentaYActualizarDeudor(pago: PagosEntinty, deudoresDao: DeudoresDao) {
        // Una recepción en tiempo real puede haber importado ya este movimiento confirmado.
        if (pago.idRemoteDatabase.isNotBlank() && findRemote(pago.idRemoteDatabase, pago.idDeudorRemoteDatabase, pago.isIngreso, pago.ownerAccount)?.ownerAccount == pago.ownerAccount) return
        agregarAbono(pago)

    }
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun actualizarAbono(pagos: PagosEntinty)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarAbonos(pagos: List<PagosEntinty>)


    @Query("SELECT idRemoteDatabase FROM PagosTable WHERE ownerAccount = :owner")
    suspend fun getAllRemoteIds(owner: String): List<String>
    @Query("UPDATE PagosTable SET montoAbonado=:recipe WHERE id = :id AND ownerAccount = :owner")
    suspend fun actualizarAbono(recipe: Int,id: String, owner: String)

    @Query("SELECT * FROM PagosTable WHERE id = :id AND ownerAccount = :owner LIMIT 1")
    suspend fun getAbonoPorId(id: String, owner: String): PagosEntinty?
    @Query("DELETE FROM PagosTable WHERE id = :id AND ownerAccount = :owner")
    suspend fun deleteOwned(id: Int, owner: String)
    @Query("SELECT * FROM PagosTable WHERE ownerAccount = :owner")
    suspend fun snapshot(owner: String): List<PagosEntinty>
    @Query("SELECT * FROM PagosTable WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): PagosEntinty?
    @Query("SELECT * FROM PagosTable WHERE idRemoteDatabase = :remoteId AND idDeudorRemoteDatabase = :debtorRemoteId AND isIngreso = :income AND (ownerAccount = :owner OR ownerAccount = '') ORDER BY CASE WHEN ownerAccount = :owner THEN 0 ELSE 1 END LIMIT 1")
    suspend fun findRemote(remoteId: String, debtorRemoteId: String, income: Boolean, owner: String): PagosEntinty?
}
