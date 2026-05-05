package com.nexusystem.paguito.data.models.request

data class SendOtpRequest(
    val email: String
)

data class VerifyOtpRequest(
    val email: String,
    val otp: String
)

