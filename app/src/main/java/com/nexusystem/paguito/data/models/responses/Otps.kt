package com.nexusystem.paguito.data.models.responses

data class VerifyOtpResponse(
    val verified: Boolean
)

data class SendOtpResponse(
    val success: Boolean
)

data class RegisterUserResponse(
    val ok: Boolean,
    val error:String
)