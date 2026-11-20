package com.nexusystem.paguito.data.models.responses

/** Un estado desconocido o un servicio no disponible bloquean compras nuevas, sin revocar Premium existente. */
data class MembershipPurchaseStatusResponse(val success: Boolean = false, val isPremium: Boolean = false, val canPurchase: Boolean = false)
