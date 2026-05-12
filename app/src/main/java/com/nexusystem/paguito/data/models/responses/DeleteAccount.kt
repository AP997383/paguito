package com.nexusystem.paguito.data.models.responses

data class DeleteAccountResponse(
    val message: String,
    val error: String,
    val success: Boolean
)