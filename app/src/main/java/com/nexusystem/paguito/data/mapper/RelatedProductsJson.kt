package com.nexusystem.paguito.data.mapper

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexus.medi.data.local.entity.PorductosResponse
import com.nexus.medi.data.local.entity.toEntity

/** Lee el detalle de ventas de ambas plataformas sin modificar el JSON ni el importe del movimiento guardado. */
object RelatedProductsJson {
    private val gson = Gson()
    private const val MAX_DISPLAY_ROWS = 10_000

    /** Las cantidades anidadas de iOS equivalen a las líneas repetidas que guarda Android. */
    fun decode(json: String): List<PorductosEntity> {
        if (json.isBlank()) return emptyList()
        val result = mutableListOf<PorductosEntity>()
        for (element in JsonParser.parseString(json).asJsonArray) {
            val row = element.asJsonObject
            val nested = row.getAsJsonObject("product")
            val quantity = if (nested == null) 1 else row.get("quantity")?.asInt ?: 1
            require(quantity > 0 && quantity <= MAX_DISPLAY_ROWS - result.size) { "Detalle de productos fuera de rango" }
            val product = if (nested == null) gson.fromJson(row, PorductosEntity::class.java) else {
                val flat = JsonObject()
                for ((wire, local) in listOf("idRemoteDatabase" to "remoteId", "nombre" to "name", "urlFoto" to "imageUrl",
                    "precioOriginal" to "originalPrice", "precioConGanancia" to "finalPrice", "factorMultiplicador" to "profitFactor",
                    "signoMultiplicador" to "profitSymbol", "inventario" to "stock", "notasAdicionales" to "notes", "ventas" to "sales")) {
                    nested.get(local)?.let { flat.add(wire, it) }
                }
                gson.fromJson(flat, PorductosResponse::class.java).toEntity()
            }
            repeat(quantity) { result.add(product.copy()) }
        }
        return result
    }
}
