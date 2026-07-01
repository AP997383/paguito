package com.nexusystem.paguito.ui.screens.productos

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.models.request.SyncCatalogProduct
import com.nexusystem.paguito.data.models.request.SyncCatalogRequest
import com.nexusystem.paguito.data.repository.remote.auth.CatalogRepository
import com.nexusystem.paguito.domain.usescases.productos.ProductosUseCase
import com.nexusystem.paguito.utils.SecureStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class ProductosViewModel @Inject constructor(
    private val productosUserCase: ProductosUseCase,
    private val firestore: FirebaseFirestore,
    private val secureStorage: SecureStorageManager,
    private val catalogRepository: CatalogRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _numProducts = MutableStateFlow(0)
    val numProducts: StateFlow<Int> = _numProducts

    private val _newUrlServer = MutableStateFlow("")
    val newUrlServer = _newUrlServer.asStateFlow()

    private val _medicine = MutableStateFlow<PorductosEntity?>(null)
    val medicine = _medicine.asStateFlow()

    private val _produtosList = MutableStateFlow<List<PorductosEntity?>>(emptyList())
    val produtosList = _produtosList.asStateFlow()

    private val _medicineNotFound = MutableStateFlow(false)
    val medicineNotFound = _medicineNotFound.asStateFlow()

    var profileState by mutableStateOf<UserProfileEntity?>(null)
        private set

    var mail by mutableStateOf<String?>("")
        private set

    init {
        loadUserProfile()
    }

    fun setUriLocal(url: String) {
        _newUrlServer.value = url
    }

    fun loadUserProfile() {
        val savedProfile = secureStorage.getUserProfile()
        mail = savedProfile?.email.orEmpty()
        profileState = savedProfile
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerProductos() {
        viewModelScope.launch {
            productosUserCase.obtenerProductos().collect { list ->
                _produtosList.value = list
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerNumeroProductos() {
        viewModelScope.launch {
            productosUserCase.obtenerNumeroProductos().collect { count ->
                _numProducts.value = count
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun daysUntilExpiration(dateStr: String): Int {
        val formatter = DateTimeFormatter.ofPattern("MM/yyyy")
        val yearMonth = YearMonth.parse(dateStr, formatter)
        val lastDayOfMonth = yearMonth.atEndOfMonth()
        val today = LocalDate.now()

        if (!today.isBefore(lastDayOfMonth)) return 0

        return ChronoUnit.DAYS.between(today, lastDayOfMonth).toInt()
    }

    fun addNewCartilla(
        producto: PorductosEntity,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        val userEmail = mail.orEmpty()

        if (userEmail.isBlank()) {
            onError(IllegalStateException("No se encontró el correo del usuario"))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            try {
                saveProductInFirebaseSuspend(userEmail, producto)
                syncWebsiteCatalogIfExists(userEmail)

                _newUrlServer.value = ""
                _isLoading.value = false
                onSuccess()
            } catch (e: Throwable) {
                _isLoading.value = false
                onError(e)
            }
        }
    }

    fun deleteProduct(
        producto: PorductosEntity,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        val userEmail = mail.orEmpty()

        if (userEmail.isBlank()) {
            onError(IllegalStateException("No se encontró el correo del usuario"))
            return
        }

        viewModelScope.launch {
            _isLoading.value = true

            try {
                productosUserCase.deleteProduct(producto)
                deleteProductInFirebaseSuspend(userEmail, producto)
                syncWebsiteCatalogIfExists(userEmail)

                _newUrlServer.value = ""
                _isLoading.value = false
                onSuccess()
            } catch (e: Throwable) {
                _isLoading.value = false
                onError(e)
            }
        }
    }

    fun guardarProductos(producto: ArrayList<PorductosEntity>) {
        viewModelScope.launch {
            // productosUserCase.guardarProductos(producto)
        }
    }

    fun savePhotoProduct(userId: String, newUri: Uri?) {
        _isLoading.value = true

        viewModelScope.launch {
            try {
                newUri?.let {
                    val url = productosUserCase.guardarFoto(userId, it)

                    if (url != null) {
                        _newUrlServer.value = url
                    }
                }
            } catch (e: Throwable) {
                Log.e("IMAGEN_PRODUCTO", "Error guardando imagen: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCartilla(recipe: PorductosEntity) {
        viewModelScope.launch {
            if (recipe.id == null) return@launch
            // productosUserCase.update(recipe)
        }
    }

    fun deleteCartilla(recipe: PorductosEntity) {
        viewModelScope.launch {
            if (recipe.id == null) return@launch
            // productosUserCase.delete(recipe)
        }
    }

    fun reset() {
        _medicineNotFound.value = false
    }

    private suspend fun saveProductInFirebaseSuspend(
        userId: String,
        producto: PorductosEntity
    ) {
        val newDocRef = firestore
            .collection("productos")
            .document(userId)
            .collection("productos")
            .document()

        val generatedId = newDocRef.id

        producto.idRemoteDatabase = generatedId

        productosUserCase.guardarProducto(producto)

        newDocRef
            .set(producto, SetOptions.merge())
            .await()
    }

    private suspend fun deleteProductInFirebaseSuspend(
        userId: String,
        producto: PorductosEntity
    ) {
        if (producto.idRemoteDatabase.isBlank()) return

        firestore
            .collection("productos")
            .document(userId)
            .collection("productos")
            .document(producto.idRemoteDatabase)
            .delete()
            .await()
    }

    private suspend fun syncWebsiteCatalogIfExists(userEmail: String) {
        try {
            val store = catalogRepository.getPublicStoreByBusinessId(userEmail)

            val products = productosUserCase
                .obtenerProductos()
                .first()
                .filterNotNull()

            val request = SyncCatalogRequest(
                businessId = userEmail,
                subdomain = store.subdomain,
                businessName = store.businessName,
                whatsapp = store.whatsapp,
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
        } catch (e: Throwable) {
            Log.e("WEBSITE_SYNC", "No se sincronizó sitio: ${e.message}")
        }
    }
}