package com.nexusystem.paguito.data.models.responses

import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexus.medi.data.local.entity.PorductosResponse

data class GetAllMyDataResponse(
    val email:String="",
    val deudores: ArrayList<DeudoresEntity> = arrayListOf(),
    val productos: ArrayList<PorductosResponse> = arrayListOf()
)