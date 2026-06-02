package com.nexusystem.paguito.data.local.entity

data class UserProfileEntity(
   val email: String,
    val fotoUrl:String,
    val fullName: String,
    val bussinesName: String,
    val phone: String,
    val token: String,
    val userSuscription:UserSuscriptionData =UserSuscriptionData(),
   var verified: Boolean =true
)

data class UserSuscriptionData(
  var nameSuscription:String="",
 var  nextExpiration:String="",
 var idSuscription:String="",
 var price:String="",
 var isActive: Boolean =false
)