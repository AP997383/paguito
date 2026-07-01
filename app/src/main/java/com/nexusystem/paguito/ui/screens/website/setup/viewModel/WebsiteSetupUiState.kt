package com.nexusystem.paguito.ui.screens.website.setup.viewModel

data class WebsiteSetupUiState(
    val businessName: String = "",
    val subdomain: String = "",
    val whatsapp: String = "",
    val isEditingWebsite: Boolean = false,
    val isCheckingSubdomain: Boolean = false,
    val subdomainAvailable: Boolean? = null,
    val subdomainMessage: String? = null,
    val hasProducts: Boolean = true,
    val productsMessage: String? = null,
    val isPublishing: Boolean = false,
    val showSuccessDialog: Boolean = false,
    val publishedWebsiteURL: String = "",
    val publishedBusinessName: String = "",
    val publishedProductsCount: Int = 0
)
