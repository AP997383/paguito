package com.nexusystem.paguito.data.models.request

data class ChangePasswordRequest(
    val email: String,
    val newPassword: String
)