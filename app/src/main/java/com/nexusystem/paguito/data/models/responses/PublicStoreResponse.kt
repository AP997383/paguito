package com.nexusystem.paguito.data.models.responses

data class PublicStoreResponse(
    val success: Boolean,
    val data: PublicStoreData? = null,
    val message: String? = null
)

data class PublicStoreData(
    val businessId: String,
    val subdomain: String,
    val businessName: String,
    val whatsapp: String,
    val products: List<PublicStoreProduct>
)

data class PublicStoreProduct(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val originalPrice: Double,
    val finalPrice: Double,
    val stock: Int,
    val notes: String? = null,
    val sales: Int
)