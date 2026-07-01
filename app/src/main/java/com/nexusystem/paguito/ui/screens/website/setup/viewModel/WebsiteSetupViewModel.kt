package com.nexusystem.paguito.ui.screens.website.setup.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.data.models.request.SyncCatalogProduct
import com.nexusystem.paguito.data.models.request.SyncCatalogRequest
import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import com.nexusystem.paguito.utils.SecureStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WebsiteSetupViewModel @Inject constructor(
    private val catalogRepository: CatalogRepository,
    @ApplicationContext context: Context
) : ViewModel() {

    private val secureStorage = SecureStorageManager(context)

    private val _uiState = MutableStateFlow(WebsiteSetupUiState())
    val uiState = _uiState.asStateFlow()

    fun setupInitialData(
        businessName: String,
        subdomain: String,
        whatsapp: String,
        isEditingWebsite: Boolean
    ) {
        _uiState.value = _uiState.value.copy(
            businessName = businessName,
            subdomain = subdomain,
            whatsapp = whatsapp,
            isEditingWebsite = isEditingWebsite,
            subdomainAvailable = if (isEditingWebsite && subdomain.isNotBlank()) true else null,
            subdomainMessage = if (isEditingWebsite && subdomain.isNotBlank()) {
                "Este enlace ya está publicado"
            } else {
                null
            }
        )
    }

    fun onBusinessNameChange(value: String) {
        _uiState.value = _uiState.value.copy(businessName = value)
    }

    fun onSubdomainChange(value: String) {
        val clean = value
            .lowercase()
            .trim()
            .replace(" ", "-")

        _uiState.value = _uiState.value.copy(
            subdomain = clean,
            subdomainAvailable = null,
            subdomainMessage = null
        )
    }

    fun onWhatsappChange(value: String) {
        _uiState.value = _uiState.value.copy(whatsapp = value)
    }

    fun checkSubdomain() {
        val state = _uiState.value
        if (state.isEditingWebsite) return

        val cleanSubdomain = state.subdomain.trim()

        if (cleanSubdomain.length < 3) {
            _uiState.value = state.copy(
                subdomainAvailable = false,
                subdomainMessage = "Escribe al menos 3 caracteres"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingSubdomain = true)

            try {
                catalogRepository.checkSubdomainAvailability(cleanSubdomain)

                _uiState.value = _uiState.value.copy(
                    isCheckingSubdomain = false,
                    subdomainAvailable = true,
                    subdomainMessage = "Este enlace está disponible"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCheckingSubdomain = false,
                    subdomainAvailable = false,
                    subdomainMessage = e.message ?: "El enlace no está disponible"
                )
            }
        }
    }

    fun publishWebsite(
        products: List<PorductosEntity>
    ) {
        val state = _uiState.value

        val email = secureStorage.getUserProfile()?.email
            ?.trim()
            ?.lowercase()
            .orEmpty()

        val cleanSubdomain = state.subdomain.trim()
        val cleanWhatsapp = state.whatsapp.filter { it.isDigit() }

        if (email.isBlank()) {
            _uiState.value = state.copy(subdomainMessage = "No pudimos identificar tu cuenta")
            return
        }

        if (state.businessName.trim().isBlank()) {
            _uiState.value = state.copy(subdomainMessage = "Agrega el nombre del negocio")
            return
        }

        if (cleanSubdomain.length < 3) {
            _uiState.value = state.copy(subdomainMessage = "Agrega un enlace válido")
            return
        }

        if (cleanWhatsapp.length < 10) {
            _uiState.value = state.copy(subdomainMessage = "Agrega un WhatsApp válido")
            return
        }

        if (products.isEmpty()) {
            _uiState.value = state.copy(
                hasProducts = false,
                productsMessage = "No puedes crear o editar tu sitio web si no tienes productos asociados."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPublishing = true)

            try {
                val request = SyncCatalogRequest(
                    businessId = email,
                    subdomain = cleanSubdomain,
                    businessName = state.businessName.trim(),
                    whatsapp = cleanWhatsapp,
                    products = products.map { product ->
                        SyncCatalogProduct(
                            id = product.idRemoteDatabase.ifBlank {
                                product.id?.toString().orEmpty()
                            },
                            name = product.nombre,
                            imageUrl = product.urlFoto,
                            originalPrice = product.precioOriginal.toDouble(),
                            finalPrice = product.precioConGanancia.toDouble(),
                            stock = product.inventario,
                            notes = product.notasAdicionales,
                            sales = product.ventas,
                            buyers = emptyList()
                        )
                    }
                )

                catalogRepository.syncCatalog(request)

                val store = catalogRepository.getPublicStore(cleanSubdomain)

                _uiState.value = _uiState.value.copy(
                    isPublishing = false,
                    showSuccessDialog = true,
                    publishedWebsiteURL = "${BuildConfig.PUBLIC_STORE_URL}/${store.subdomain}",
                    publishedBusinessName = store.businessName,
                    publishedProductsCount = store.products.size
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isPublishing = false,
                    subdomainAvailable = false,
                    subdomainMessage = e.message ?: "No pudimos publicar el sitio"
                )
            }
        }
    }

    fun closeSuccessDialog() {
        _uiState.value = _uiState.value.copy(showSuccessDialog = false)
    }

    fun websiteUrl(): String {
        val subdomain = _uiState.value.subdomain.ifBlank { "mi-negocio" }
        return "${BuildConfig.PUBLIC_STORE_URL}/$subdomain"
    }
}