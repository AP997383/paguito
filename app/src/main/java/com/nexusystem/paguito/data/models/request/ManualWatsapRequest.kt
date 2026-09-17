package com.nexusystem.paguito.data.models.request

data class ManualWatsapRequest(
    val telefono: String,
    val nombreUsuario: String,
    val nombreCliente: String,
    val correo: String,
    val saldoRestante: String, // O Double/Int si prefieres manejarlo como número
    val tipoReminder: Int,
    val fromPhone: String,
    val proximopago:String
)