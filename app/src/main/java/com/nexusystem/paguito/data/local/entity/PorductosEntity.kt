package com.nexus.medi.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "ProductosTble")
data class PorductosEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = 0,
    var nombre: String = "",
    var idRemoteDatabase: String="",
    var urlFoto: String = "",
    var precioOriginal: Float = 0.0F,
    var precioConGanancia: Float = 0.0F,
    var factorMultiplicador: Float = 0.0F,
    var signoMultiplicador: String ="%",
    var inventario:Int = 0,
    var notasAdicionales: String="",
    var ventas: Int=0

)

data class PorductosResponse(
    val id: String? = "",
    var nombre: String = "",
    var idRemoteDatabase: String="",
    var urlFoto: String = "",
    var precioOriginal: Float = 0.0F,
    var precioConGanancia: Float = 0.0F,
    var factorMultiplicador: Float = 0.0F,
    var signoMultiplicador: String ="%",
    var inventario:Int = 0,
    var notasAdicionales: String="",
    var ventas: Int=0

)


fun PorductosResponse.toEntity(): PorductosEntity {
    return PorductosEntity(
        id = null, // 👈 Ignoramos el String 'id' y le pasamos 0 para que Room genere su propio Int autoincremental
        nombre = this.nombre,
        idRemoteDatabase = this.idRemoteDatabase,
        urlFoto = this.urlFoto ?: "",
        precioOriginal = this.precioOriginal,
        precioConGanancia = this.precioConGanancia,
        factorMultiplicador = this.factorMultiplicador,
        signoMultiplicador = this.signoMultiplicador,
        inventario = this.inventario,
        notasAdicionales = this.notasAdicionales ?: "",
        ventas = this.ventas
    )
}