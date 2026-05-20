package com.nexus.medi.data.repository.local.room.bloodAndPresure

import android.util.Log
import com.nexusystem.paguito.data.local.dao.ProductosDao
import com.nexus.medi.data.local.entity.PorductosEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PorductosRepository @Inject constructor(
    private val recipesDao: ProductosDao
) {

    fun obtenerTodos(): Flow<List<PorductosEntity>> {
        return recipesDao.obtenerTodosProductos( )
    }


    suspend fun add(recipe: PorductosEntity) :Long{
        Log.e("ADDDD","--<" + recipe)
       return  recipesDao.agregarProducto(recipe.toEntity())
    }

    suspend fun deleteProduct(recipe: PorductosEntity) {
        Log.e("ADDDD","--<" + recipe)
        recipesDao.deleteProduct(recipe.toEntity())
    }

    suspend fun addProductos(recipe: ArrayList<PorductosEntity>) {
        val existingIds = recipesDao.getAllRemoteIds()
        val nuevosProductos = recipe?.filter { it.idRemoteDatabase !in existingIds }
        if (!nuevosProductos.isNullOrEmpty()) {
            recipesDao.agregarProductos(nuevosProductos)
        }

    }

    suspend fun obtenerNumeroProductos(): Flow<Int> {
       return recipesDao.obtenerNumeroDeProductos()
    }
    suspend fun update(recipe: PorductosEntity) {
        val entity = recipe.toEntity()
       // recipesDao.updateBotiquin(entity.quiantity,entity.codigo )
    }


    private fun PorductosEntity.toEntity(): PorductosEntity {
        return PorductosEntity(
            id = this.id,
            nombre = this.nombre,
            idRemoteDatabase = this.idRemoteDatabase,
            urlFoto = this.urlFoto,
            precioOriginal = this.precioOriginal,
            precioConGanancia = this.precioConGanancia,
            factorMultiplicador = this.factorMultiplicador,
            signoMultiplicador = this.signoMultiplicador,
            inventario = this.inventario,
            notasAdicionales = this.notasAdicionales)
    }
}