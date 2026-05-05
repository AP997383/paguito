package com.nexusystem.paguito.data.models.responses

import com.nexusystem.paguito.data.local.entity.UserSuscriptionData

data class LoginResponse(
    val accessToken: String = "",
    val expiresIn: Int = 0,
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val fotoUrl: String,
    val bussinesName: String,
val userSuscription:UserSuscriptionData = UserSuscriptionData()
)

