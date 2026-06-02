package com.nexusystem.paguito.domain.data.auth

data class UserDataModelAuth(
    val email:String="",
    val password: String="",
    val fullName: String="",
    val phone: String="",
    var verified: Boolean=false
)
