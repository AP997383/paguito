package com.nexus.medi.data.local.entity

import androidx.room.ColumnInfo
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

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
    @SerializedName(value = "idsPorductos", alternate = ["idsProductos"])
    val idsPorductos: String="",
    val notasSobreDeudor: String="",
    @SerializedName("saldoAFavor") @get:PropertyName("saldoAFavor") @set:PropertyName("saldoAFavor") @ColumnInfo(defaultValue = "0.0")
    var creditBalance: Float = 0f,
    /** Identidad local; no se transmite al documento de Firestore. */
    @get:Exclude @ColumnInfo(defaultValue = "''") val ownerAccount: String = "",


){
    @Ignore @get:Exclude
    val isDeleted: Boolean = false
    @Ignore
    val pagos: ArrayList<PagosEntinty> = arrayListOf()
    @Ignore
    val ventas: ArrayList<PagosEntinty>  =arrayListOf()
    @Ignore
    val diasRemaining:Int=0
}