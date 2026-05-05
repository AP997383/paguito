package com.nexusystem.paguito.data.models.responses

import android.R

data class ChangePasswordResponse(
    val message: String,
    val error: String,
    val ok: Boolean
)