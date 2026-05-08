package com.nexusystem.paguito.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.medi.data.local.entity.PorductosEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface  ProductosDao {

    @Query("SELECT * FROM ProductosTble")
    fun obtenerTodosProductos(): Flow<List<PorductosEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarProducto(mascota: PorductosEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun agregarProductos(productos: List<PorductosEntity>)

    @Query("SELECT idRemoteDatabase FROM ProductosTble")
    suspend fun getAllRemoteIds(): List<String>
    @Query("SELECT * FROM ProductosTble  WHERE nombre=:nombre ")
    fun obtenerProductoPorNombre(nombre: String):  Flow<List<PorductosEntity>>

    @Delete
    suspend fun borrarProducto(mascota: PorductosEntity)
}