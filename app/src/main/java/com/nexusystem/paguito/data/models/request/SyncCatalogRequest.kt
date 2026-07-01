package com.nexusystem.paguito.data.models.request

data class SyncCatalogRequest(
    val businessId: String,
    val subdomain: String,
    val businessName: String,
    val whatsapp: String,
    val products: List<SyncCatalogProduct>
)

data class SyncCatalogProduct(
    val id: String,
    val name: String,
    val imageUrl: String,
    val originalPrice: Double,
    val finalPrice: Double,
    val stock: Int,
    val notes: String,
    val sales: Int,
    val buyers: List<String>
)