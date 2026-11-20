package com.nexusystem.paguito.data.mapper

import com.google.gson.Gson
import com.nexus.medi.data.local.entity.DeudoresEntity
import org.junit.Assert.*
import org.junit.Test

/** Protege la compatibilidad de detalles de ventas y clientes guardados desde la otra plataforma. */
class RelatedProductsJsonTest {
    @Test fun readsIOSNestedProductsAndPreservesUnitPriceAndQuantity() {
        val rows = RelatedProductsJson.decode("""[{"id":"ios-local-uuid","quantity":2,"product":{"id":"ios-local-uuid","remoteId":"p1","name":"Perfume","originalPrice":10.25,"finalPrice":15.75,"stock":9}}]""")
        assertEquals(2, rows.size)
        assertTrue(rows.all { it.nombre == "Perfume" && it.idRemoteDatabase == "p1" })
        assertEquals(31.5, rows.sumOf { it.precioConGanancia.toDouble() }, 0.0)
    }
    @Test fun historicalAndroidFlatProductsKeepTheirFields() {
        val row = RelatedProductsJson.decode("""[{"id":7,"idRemoteDatabase":"p","nombre":"Android","precioConGanancia":15.75}]""").single()
        assertEquals(7, row.id)
        assertEquals("Android", row.nombre)
        assertEquals(15.75f, row.precioConGanancia)
    }
    @Test fun canonicalIOSCustomerProductKeyAndAndroidLegacyKeyAreBothRead() {
        for (key in listOf("idsProductos", "idsPorductos")) {
            val client = Gson().fromJson("""{"$key":"detail"}""", DeudoresEntity::class.java)
            assertEquals("detail", client.idsPorductos)
        }
    }
}
