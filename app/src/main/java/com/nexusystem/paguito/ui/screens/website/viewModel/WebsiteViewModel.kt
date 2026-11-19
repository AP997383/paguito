package com.nexusystem.paguito.ui.screens.website.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusystem.paguito.data.local.websiteBusinessId
import com.nexusystem.paguito.data.models.responses.PublicStoreData
import com.nexusystem.paguito.data.repository.remote.auth.CatalogException
import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import com.nexusystem.paguito.ui.screens.website.components.WebsitePreviewProduct
import com.nexusystem.paguito.utils.SecureStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebsiteViewModel @Inject constructor(private val catalogRepository: CatalogRepository, @ApplicationContext context: Context) : ViewModel() {
    private val secureStorage = SecureStorageManager(context)
    private val _uiState = MutableStateFlow(WebsiteUiState())
    val uiState = _uiState.asStateFlow()
    private var refreshJob: Job? = null
    private var deleteJob: Job? = null

    fun refresh() {
        if (deleteJob?.isActive == true) return
        refreshJob?.cancel()
        val owner = websiteBusinessId(secureStorage.getUserProfile()?.email.orEmpty())
        if (owner.isBlank()) {
            _uiState.value = WebsiteUiState(isLoading = false, errorMessage = "Inicia sesión para consultar el sitio de tu cuenta.")
            return
        }
        val cached = catalogRepository.cachedPublicStore(owner)
        _uiState.value = cached?.toUiState()?.copy(isRefreshing = true) ?: WebsiteUiState()
        refreshJob = viewModelScope.launch {
            try {
                _uiState.value = catalogRepository.getPublicStoreByBusinessId(owner).toUiState()
            } catch (e: CancellationException) { throw e
            } catch (_: CatalogException.StoreNotFound) {
                if (cached == null) {
                    _uiState.value = missingWebsite()
                } else {
                    // A broken business lookup does not mean that a known public URL disappeared.
                    try {
                        val store = catalogRepository.getPublicStore(cached.subdomain)
                        if (websiteBusinessId(store.businessId) != owner) {
                            catalogRepository.forgetPublicStore(owner)
                            _uiState.value = missingWebsite()
                        } else {
                            catalogRepository.rememberPublicStore(store)
                            _uiState.value = store.toUiState()
                        }
                    } catch (e: CancellationException) { throw e
                    } catch (_: CatalogException.StoreNotFound) {
                        // Publication/index propagation can temporarily return 404 after a successful write.
                        showLookupError(cached)
                    } catch (_: Exception) { showLookupError(cached) }
                }
            } catch (_: Exception) { showLookupError(cached) }
        }
    }

    fun deleteWebsite() {
        if (deleteJob?.isActive == true) return
        val website = _uiState.value.publishedWebsite ?: return
        refreshJob?.cancel()
        deleteJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isRefreshing = false, errorMessage = null)
            try {
                catalogRepository.deletePublicStore(website.subdomain)
                catalogRepository.forgetPublicStore(website.businessId)
                _uiState.value = missingWebsite()
            } catch (e: CancellationException) { throw e
            } catch (_: CatalogException.StoreNotFound) {
                catalogRepository.forgetPublicStore(website.businessId)
                _uiState.value = missingWebsite()
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "No pudimos eliminar el sitio. Tu enlace se conserva; puedes reintentarlo.")
            }
        }
    }

    private fun showLookupError(cached: PublicStoreData?) {
        val message = if (cached != null) "No pudimos actualizar tu sitio. Conservamos el enlace guardado para que puedas abrirlo o volver a intentarlo."
            else "No pudimos comprobar si ya tienes un sitio. Reintenta la consulta antes de crear uno nuevo."
        _uiState.value = (cached?.toUiState() ?: WebsiteUiState(isLoading = false)).copy(errorMessage = message)
    }

    private fun missingWebsite() = WebsiteUiState(isLoading = false, canCreateWebsite = true)
    private fun PublicStoreData.toUiState() = WebsiteUiState(isLoading = false, isWebsiteActive = true,
        publishedWebsite = PublishedWebsite(businessId, businessName, subdomain, whatsapp),
        previewProducts = products.take(2).map { WebsitePreviewProduct(it.name, it.finalPrice, it.stock, it.sales, it.imageUrl) })
}
