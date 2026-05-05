package com.nexus.medi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ProductosTble")
data class PorductosEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = 0,
    var nombre: String = "",
    val idRemoteDatabase: String="",
    var urlFoto: String = "",
    var precioOriginal: Float = 0.0F,
    var precioConGanancia: Float = 0.0F,
    var factorMultiplicador: Float = 0.0F,
    var signoMultiplicador: String ="%",
    var inventario:Int = 0,
    var notasAdicionales: String="",
    var ventas: Int=0

)