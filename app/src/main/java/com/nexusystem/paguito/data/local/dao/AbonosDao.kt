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
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import kotlinx.coroutines.flow.Flow

@Dao
interface  AbonosDao {

    @Query("SELECT * FROM PagosTable")
    fun obtenerTodosLosAbonos(): Flow<List<PagosEntinty>>

    @Query("""
        SELECT 
            p.id, 
            p.idDeudor, 
            d.nombre AS nameDeudor, 
            p.montoAbonado, 
            p.fechaAbono, 
            p.pagoATiempo, 
            p.tipoPago, 
            p.isIngreso, 
            p.notas, 
            p.jsonAbonoPorProducto
        FROM PagosTable AS p
        LEFT JOIN DeudoresTable AS d ON p.idDeudor = CAST(d.id AS TEXT)
    """)
    fun getPagosConNombre(): Flow<List<PagoConNombre>>

    @Query("""
        SELECT 
            p.id, 
            p.idDeudor, 
            d.nombre AS nameDeudor, 
            p.montoAbonado, 
            p.fechaAbono, 
            p.pagoATiempo, 
            p.tipoPago, 
            p.isIngreso, 
            p.notas, 
            p.jsonAbonoPorProducto
        FROM PagosTable AS p
        LEFT JOIN DeudoresTable AS d ON p.idDeudor = CAST(d.id AS TEXT) LIMIT 5
    """)
    fun getPagosConNombre5(): Flow<List<PagoConNombre>>

    @Query("SELECT * FROM PagosTable WHERE idDeudor=:idCiente")
    fun obtenerAbonosPorCliente(idCiente: String): Flow<List<PagosEntinty>>

    @Query("SELECT COUNT(*) FROM PagosTable")
    fun getNetNumberTicket(): Flow<Int>
    @Query("SELECT * FROM PagosTable")

    fun obtenerAbonosPorProducto(): Flow<List<PagosEntinty>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarAbono(mascota: PagosEntinty)

    @Transaction
    suspend fun registrarAbonoYActualizarDeudor(pago: PagosEntinty, deudoresDao: DeudoresDao) {
        // 1. Guardamos el abono
        agregarAbono(pago)
        // 2. Restamos el monto al deudor (convertimos el idDeudor String a Int)
        val idInt = pago.idDeudor.toIntOrNull() ?: 0
        deudoresDao.restarSaldo(idInt, pago.montoAbonado.toFloat())
    }

    @Transaction
    suspend fun registrarVentaYActualizarDeudor(pago: PagosEntinty, deudoresDao: DeudoresDao) {
        // 1. Guardamos el abono
        agregarAbono(pago)
        // 2. Restamos el monto al deudor (convertimos el idDeudor String a Int)
        val idInt = pago.idDeudor.toIntOrNull() ?: 0
        deudoresDao.sumarSaldo(idInt, pago.montoAbonado.toFloat())
    }

    @Transaction
    suspend fun registrarPrimeraVentaYActualizarDeudor(pago: PagosEntinty, deudoresDao: DeudoresDao) {
        agregarAbono(pago)

    }
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun actualizarAbono(pagos: PagosEntinty)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarAbonos(pagos: List<PagosEntinty>)

    @Query("SELECT idRemoteDatabase FROM PagosTable")
    suspend fun getAllRemoteIds(): List<String>
    @Query("UPDATE PagosTable SET montoAbonado=:recipe WHERE id = :id ")
    suspend fun actualizarAbono(recipe: Int,id: String)

    @Query("SELECT * FROM PagosTable WHERE id = :id LIMIT 1")
    suspend fun getAbonoPorId(id: String): PagosEntinty?
    @Delete
    suspend fun borrarAbono(mascota: PagosEntinty)
}