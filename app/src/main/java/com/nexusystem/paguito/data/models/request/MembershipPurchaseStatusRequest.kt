package com.nexusystem.paguito.data.models.request

/** Consulta sin efectos de cobro; nunca envía tokens ficticios al verificador de compras. */
data class MembershipPurchaseStatusRequest(val email: String, val platform: String = "google", val productId: String)
