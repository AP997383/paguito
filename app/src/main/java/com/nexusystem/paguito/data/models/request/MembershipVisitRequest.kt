package com.nexusystem.paguito.data.models.request

/** Identificador único por apertura para no duplicar visitas al reintentar. */
data class MembershipVisitRequest(val email: String, val visitId: String, val platform: String = "google")
