package com.nexusystem.paguito.data.local.entity

import com.google.gson.annotations.SerializedName

data class UserProfileEntity(
   val email: String="",
    val fotoUrl:String="",
    val fullName: String="",
    val bussinesName: String="",
    val phone: String="",
    val token: String="",
    val watsapSended: Int=0,
    val watsapsSendedTotal: Int=0,
    val userSuscription:UserSuscriptionData =UserSuscriptionData(),
    var verified: Boolean =true,
    @SerializedName("RemindersActive")
    var RemindersActive: Boolean=false,
    var reminderPreventivo: Boolean=false,
    var reminderAtraso: Boolean=false,
    var reminderAvisoPago:Boolean=false)

data class UserSuscriptionData(
  var nameSuscription:String="",
 var  nextExpiration:String="",
 var idSuscription:String="",
 var price:String="",
 var isActive: Boolean =false
)