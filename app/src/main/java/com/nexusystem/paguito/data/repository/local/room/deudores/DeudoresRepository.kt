package com.nexus.medi.data.repository.local.room.bloodAndPresure

import android.util.Log
import com.nexusystem.paguito.data.local.dao.DeudoresDao
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.domain.data.DeudoresSummary
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeudoresRepository @Inject constructor(
    private val recipesDao: DeudoresDao
) {

    fun obtenerDeudores(): Flow<List<DeudoresEntity>> {
        return recipesDao.obtenerTodosDeudores()
    }

    fun obtenerDatosDeCards(): Flow<DeudoresSummary> {
        return recipesDao.getDeudoresSummary()
    }
    fun obtener5DatosDeCards(): Flow<DeudoresSummary> {
        return recipesDao.get5DeudoresSummary()
    }



    suspend fun add(recipe: DeudoresEntity) {
        Log.e("ADDDD","--<" + recipe)
        recipesDao.agregarNuevoDeudor(recipe.toEntity())
    }

    suspend fun addDeudores(recipe: ArrayList<DeudoresEntity>) {
        Log.e("ADDDD","--<" + recipe)
        recipesDao.agregarDeudores(recipe)
    }

    suspend fun updateSync(recipe: DeudoresEntity) {
        Log.e("ADDDD","--<" + recipe)
        recipesDao.updateSync(recipe.idRemoteDatabase!!)
    }

    suspend fun update(recipe: DeudoresEntity) {
        val entity = recipe.toEntity()
       // recipesDao.updateBotiquin(entity.quiantity,entity.codigo )
    }


    private fun DeudoresEntity.toEntity(): DeudoresEntity {
        return DeudoresEntity(
            id = this.id,
            idRemoteDatabase = this.idRemoteDatabase,
            nombre = this.nombre,
            telefono = this.telefono,
            correo = this.correo,
            periodicidad = this.periodicidad,
            fechaInicialDeuda = this.fechaInicialDeuda,
            domicilio = this.domicilio,
            montoActualAdeudado = this.montoActualAdeudado,
            montoAcomulado = this.montoAcomulado,
            pagosAtrasados = this.pagosaTiempo,
            pagosaTiempo=this.pagosaTiempo,
            idsPorductos = this.idsPorductos,
            notasSobreDeudor = this.notasSobreDeudor)
    }
}