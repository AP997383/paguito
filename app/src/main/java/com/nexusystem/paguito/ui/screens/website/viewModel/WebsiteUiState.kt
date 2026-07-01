package com.nexusystem.paguito.ui.screens.website.viewModel

import com.nexusystem.paguito.ui.screens.website.components.WebsitePreviewProduct

data class WebsiteUiState(
    val isLoading: Boolean = false,
    val isWebsiteActive: Boolean = false,
    val publishedWebsite: PublishedWebsite? = null,
    val previewProducts: List<WebsitePreviewProduct> = emptyList()
)
