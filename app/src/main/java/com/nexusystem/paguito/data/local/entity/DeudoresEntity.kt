package com.nexus.medi.data.local.entity

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "DeudoresTable")
data class DeudoresEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Int?=0,
    var idRemoteDatabase: String="",
    var inRemote: Boolean=false,
    val nombre: String="",
    val telefono: String="=",
    val correo: String="",
    val domicilio: String="",
    var montoActualAdeudado: Float=0.0f,
    val montoAcomulado: Float=0.0f,
    val fechaInicialDeuda: String="",
    val periodicidad: String="",
    val pagosAtrasados:Int=0,
    val pagosaTiempo:Int=0,
    val idsPorductos: String="",
    val notasSobreDeudor: String="",


){
    @Ignore
    val pagos: ArrayList<PagosEntinty> = arrayListOf()
    @Ignore
    val ventas: ArrayList<PagosEntinty>  =arrayListOf()
    @Ignore
    val diasRemaining:Int=0
}