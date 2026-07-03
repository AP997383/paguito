package com.nexusystem.paguito.ui.screens.website.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import com.nexusystem.paguito.ui.screens.website.components.WebsitePreviewProduct
import com.nexusystem.paguito.utils.SecureStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebsiteViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val secureStorage = SecureStorageManager(context)

    private val _uiState = MutableStateFlow(WebsiteUiState())
    val uiState: StateFlow<WebsiteUiState> = _uiState.asStateFlow()

    fun getUserProfile(): UserProfileEntity? {
        return secureStorage.getUserProfile()
    }

    fun isPremiumUser(): Boolean {
        return secureStorage.getUserProfile()
            ?.userSuscription
            ?.isActive == true
    }

    fun refresh() {
        val email = secureStorage.getUserProfile()?.email

        if (email.isNullOrBlank()) {
            clearWebsiteState()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val store = catalogRepository.getPublicStoreByBusinessId(email)

                _uiState.value = WebsiteUiState(
                    isLoading = false,
                    isWebsiteActive = true,
                    publishedWebsite = PublishedWebsite(
                        businessId = store.businessId,
                        businessName = store.businessName,
                        subdomain = store.subdomain,
                        whatsapp = store.whatsapp
                    ),
                    previewProducts = store.products.take(2).map {
                        WebsitePreviewProduct(
                            name = it.name,
                            price = it.finalPrice,
                            stock = it.stock,
                            sold = it.sales,
                            imageUrl = it.imageUrl
                        )
                    }
                )
            } catch (_: Exception) {
                clearWebsiteState()
            }
        }
    }

    fun deleteWebsite() {
        val website = _uiState.value.publishedWebsite ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                catalogRepository.deletePublicStore(website.subdomain)
                clearWebsiteState()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private fun clearWebsiteState() {
        _uiState.value = WebsiteUiState()
    }
}