package com.nexusystem.paguito.data.local.entity

data class SuscriptionsItems(
    val benefits: ArrayList<String> = arrayListOf(),
    val name: String ="",
    val price:Int = 0,
    val productId: String = ""
)
