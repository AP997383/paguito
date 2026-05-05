package com.nexusystem.paguito.data.models.request

data class SuscripcionPurchaseRequest(
    val email:String="",
    val purchaseToken:String="",
    val productId:String="",
    val price:String=""
)