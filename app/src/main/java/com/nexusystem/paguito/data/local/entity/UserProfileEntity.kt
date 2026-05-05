package com.nexusystem.paguito.data.local.entity

data class UserProfileEntity(
   val email: String,
    val fotoUrl:String,
    val fullName: String,
    val bussinesName: String,
    val phone: String,
    val token: String,
    val userSuscription:UserSuscriptionData =UserSuscriptionData()
)

data class UserSuscriptionData(
  val nameSuscription:String="",
 val  nextExpiration:String="",
 val idSucription:String="",
 val price:String="",
 val isActive: Boolean =false
)