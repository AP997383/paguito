package com.nexusystem.paguito.data.repository.local.room.abonos

import android.util.Log
import com.nexusystem.paguito.data.local.dao.AbonosDao
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexusystem.paguito.data.local.dao.DeudoresDao
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AbonosRepository @Inject constructor(
    private val recipesDao: AbonosDao,
    private val deudoresDao: DeudoresDao
) {

    fun obtenerAbonoPorCliente(idCliente: String): Flow<List<PagosEntinty>> {
        return recipesDao.obtenerAbonosPorCliente(idCliente)
    }

    fun getLastTicketNumer(): Flow<Int> {
        return recipesDao.getNetNumberTicket()
    }

    fun obtenerUltimosAbono(): Flow<List<PagoConNombre>> {
        return recipesDao.getPagosConNombre()
    }
    fun obtenerUltimosAbono5(): Flow<List<PagoConNombre>> {
        return recipesDao.getPagosConNombre5()
    }



    suspend fun add(recipe: PagosEntinty) {
        Log.e("ADDDD","--<" + recipe)
        recipesDao.registrarAbonoYActualizarDeudor(recipe.toEntity(),deudoresDao)
      //  recipesDao.agregarAbono(recipe.toEntity())
    }

    suspend fun eliminar(recipe: PagosEntinty) {
        Log.e("ADDDD","--<" + recipe)
        recipesDao.eliminaAbonoyActualizaMonto(recipe.toEntity(),deudoresDao)
        //  recipesDao.agregarAbono(recipe.toEntity())
    }
    suspend fun addPagosOrVentas(recipe: ArrayList<PagosEntinty>) {
        Log.e("ADDDD","--<" + recipe)
        val existingIds = recipesDao.getAllRemoteIds()
        val nuevosAbonos = recipe?.filter { it.idRemoteDatabase !in existingIds }
        if (!nuevosAbonos.isNullOrEmpty()) {
            recipesDao.agregarAbonos(nuevosAbonos)
        }
    }

    suspend fun addVenta(recipe: PagosEntinty) {
        Log.e("ADDDD","--<" + recipe)
        recipesDao.registrarVentaYActualizarDeudor(recipe.toEntity(),deudoresDao)
        //  recipesDao.agregarAbono(recipe.toEntity())
    }
    suspend fun addprimeraVenta(recipe: PagosEntinty) {
        Log.e("ADDDD","--<" + recipe)
        recipesDao.registrarPrimeraVentaYActualizarDeudor(recipe.toEntity(),deudoresDao)
        //  recipesDao.agregarAbono(recipe.toEntity())
    }

    suspend fun updateVenta(recipe: PagosEntinty) {
        Log.e("ADDDD","--<" + recipe)
        recipesDao.actualizarAbono(recipe.toEntity())
        //  recipesDao.agregarAbono(recipe.toEntity())
    }


    private fun PagosEntinty.toEntity(): PagosEntinty {
        return PagosEntinty(
            id = this.id,
            idDeudor = this.idDeudor,
            idRemoteDatabase = this.idRemoteDatabase,
            idDeudorRemoteDatabase = this.idDeudorRemoteDatabase,
            montoAbonado = this.montoAbonado,
            saldoAntesDeAbono = this.saldoAntesDeAbono,
            fechaAbono = this.fechaAbono,
            pagoATiempo = this.pagoATiempo,
            tipoPago = this.tipoPago,
            isIngreso=this.isIngreso,
            notas = this.notas,
            jsonAbonoPorProducto = this.jsonAbonoPorProducto)
    }
}