package com.nexusystem.paguito.data.models.responses

import com.nexusystem.paguito.data.local.entity.SuscriptionsItems

data class SuscriptionsResponse(
    val suscripciones: ArrayList<SuscriptionsItems> = arrayListOf()
)
