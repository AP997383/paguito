package com.nexus.medi.data.local.entity

import androidx.room.ColumnInfo
import com.google.firebase.firestore.Exclude
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "PagosTable")
data class PagosEntinty(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = 0,
    var idRemoteDatabase: String="",
    var idDeudorRemoteDatabase: String="",
    val idDeudor: String="",
    val montoAbonado: Double=0.0,
    val saldoAntesDeAbono: Double=0.0,
    val fechaAbono: String="",
    val pagoATiempo: Boolean=false,
    val tipoPago:Int=1,
    @SerializedName("ingreso")
    val isIngreso: Boolean =false,
    val notas:String="",
    var jsonAbonoPorProducto: String="",
    /** Identidad local; la cuenta se asigna desde la sesión, no desde la respuesta remota. */
    @get:Exclude @ColumnInfo(defaultValue = "''") val ownerAccount: String = "",
    @get:Exclude @ColumnInfo(defaultValue = "0") val isSynced: Boolean = false
)


data class PagostoPreviewTiket(
    var correoAndPhone:String="",
    var nameClient: String="",
    var nameBussines: String="",
    val montoAbonado: Double=0.0,
    val saldoAntesDeAbono: Double=0.0,
    val fechaAbono: String="",
    val ammountTotal: Double=0.0,
    val tipoPago:Int=1,
    val jsonAbonoPorProducto: String="",
    val isIngreso: Boolean=false
)
