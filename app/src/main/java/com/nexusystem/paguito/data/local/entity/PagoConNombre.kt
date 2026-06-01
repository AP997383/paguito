package com.nexusystem.paguito.data.local.entity

data class PagoConNombre(
    val id: Int?,
    val idDeudor: String="",
    val nameDeudor: String="", // Aquí caerá el 'nombre' de DeudoresTable
    val montoAbonado: Int=0,
    val fechaAbono: String="",
    val pagoATiempo: Boolean=false,
    val tipoPago: Int=0,
    val isIngreso: Boolean=false,
    val notas: String="",
    val jsonAbonoPorProducto: String=""
)

data class AbonosDelMes(
    val montoAbonado: Int=0,
    val fechaAbono: String="",
    val isIngreso: Boolean
)