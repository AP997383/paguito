package com.nexusystem.paguito.domain.data

data class DeudoresSummary(
    val totalDeudores: Int,
    val sumaTotalMontos: Float
)

data class ProductosSummary1(
    val numeroProductos: Int=0,
    val totalCostoProductos: Float=0.0f
)