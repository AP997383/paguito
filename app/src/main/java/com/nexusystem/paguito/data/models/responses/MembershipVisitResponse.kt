package com.nexusystem.paguito.data.models.responses

/** Identificador aleatorio del servidor; Analytics nunca recibe el correo ni su hash. */
data class MembershipVisitResponse(val success: Boolean = false, val analyticsID: String = "")
