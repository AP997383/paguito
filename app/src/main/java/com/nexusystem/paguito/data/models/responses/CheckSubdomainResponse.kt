package com.nexusystem.paguito.data.models.responses

data class CheckSubdomainResponse(
    val success: Boolean,
    val code: String? = null,
    val message: String? = null,
    val data: CheckSubdomainData? = null
)

data class CheckSubdomainData(
    val subdomain: String,
    val url: String
)