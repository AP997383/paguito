package com.nexusystem.paguito.ui.screens.website.viewModel

import com.nexusystem.paguito.ui.screens.website.components.WebsitePreviewProduct

data class WebsiteUiState(
    val isLoading: Boolean = true,
    val isWebsiteActive: Boolean = false,
    val publishedWebsite: PublishedWebsite? = null,
    val previewProducts: List<WebsitePreviewProduct> = emptyList(),
    val canCreateWebsite: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)
