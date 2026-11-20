package com.nexusystem.paguito.data.sync

import com.nexusystem.paguito.data.models.responses.GetAllMyDataResponse

/** Una respuesta incompleta o ambigua no puede sustituir una caché válida ni retirar filas confirmadas. */
object AccountSnapshotValidation {
    /** La identidad de un movimiento incluye cliente, colección y documento, nunca solamente el ID local. */
    fun validate(response: GetAllMyDataResponse) {
        val products = response.productos.map { it.idRemoteDatabase }
        require(products.all(String::isNotBlank) && products.distinct().size == products.size)
        val clients = response.deudores.filterNot { it.isDeleted }
        val clientIds = clients.map { it.idRemoteDatabase }
        require(clientIds.all(String::isNotBlank) && clientIds.distinct().size == clientIds.size)
        val movementKeys = mutableSetOf<String>()
        for (client in clients) for ((income, movements) in listOf(true to client.pagos, false to client.ventas)) {
            for (movement in movements) {
                require(movement.idRemoteDatabase.isNotBlank())
                require(movementKeys.add("${client.idRemoteDatabase}|$income|${movement.idRemoteDatabase}"))
            }
        }
    }
}
