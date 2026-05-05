package com.nexusystem.paguito.ui.screens.perfil

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.data.local.entity.SuscriptionsItems
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.local.entity.UserSuscriptionData
import com.nexusystem.paguito.data.models.request.SuscripcionPurchaseRequest
import com.nexusystem.paguito.data.repository.remote.auth.AuthRepository
import com.nexusystem.paguito.data.repository.remote.auth.StorageRepository
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.domain.usescases.deudores.DeudoresUseCase
import com.nexusystem.paguito.domain.usescases.perfil.PerfilUseCase
import com.nexusystem.paguito.utils.SecureStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import kotlin.collections.List

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class PerfiViewModel @Inject constructor(
    private val useCase: PerfilUseCase,
    private val firestore: FirebaseFirestore,
    private val secureStorage: SecureStorageManager,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _deudor = MutableStateFlow<DeudoresEntity?>(null)
    val deudor = _deudor.asStateFlow()

    private val _newUrlServer = MutableStateFlow<String>("")
    val newUrlServer = _newUrlServer.asStateFlow()

    private val _updateSuccess = MutableStateFlow<Boolean>(false)
    val updateSuccess = _updateSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String>("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _deudores = MutableStateFlow<List<DeudoresEntity?>>(emptyList())
    val deudores = _deudores.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _suscriptions= MutableStateFlow<List<SuscriptionsItems>>(listOf<SuscriptionsItems>())
    val suscriptions = _suscriptions.asStateFlow()
    private val _sumary1 = MutableStateFlow<DeudoresSummary?>(null)
    val sumary1 = _sumary1.asStateFlow()

    private val _medicineNotFound = MutableStateFlow(false)
    val medicineNotFound = _medicineNotFound.asStateFlow()

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed
    private val _isSubscribedError = MutableStateFlow(false)
    val isSubscribedError: StateFlow<Boolean> = _isSubscribedError

    var profileState by mutableStateOf<UserProfileEntity?>(null)
        private set

    private val _currentProductSelected = MutableStateFlow<SuscriptionsItems?>(null)
    val currentProductSelected: StateFlow<SuscriptionsItems?> = _currentProductSelected

     fun setCurrentProduct(param:SuscriptionsItems){
        _currentProductSelected.value = param
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        loadUserProfile()
        startBillingConnection()
    }

    fun resetSuccess(){
        _updateSuccess.value =false
    }

    fun loadUserProfile() {
        // Recuperamos del Secure Storage
        val savedProfile = secureStorage.getUserProfile()
        _newUrlServer.value = savedProfile?.fotoUrl?:""
        profileState = savedProfile
    }


    fun resetAllStatusPurchase(){
        _isSubscribedError.value =false
        _isSubscribed.value=false
    }



    @RequiresApi(Build.VERSION_CODES.O)
    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProduct("premium_monthly_basic") // Tu ID de la consola
                }
            }
            override fun onBillingServiceDisconnected() {
                startBillingConnection()
            }
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun queryProduct(productId: String) {
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            ).build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { _, productDetailsList ->
            _productDetails.value = productDetailsList.firstOrNull()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun launchPurchaseFlow(activity: Activity) {
        val productDetails = _productDetails.value ?: return
        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken ?: ""

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(activity, billingFlowParams)
    }
    fun getAllSuscriptions() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value =""
            try {
                val result = authRepository.getAllSuscriptions()
                _suscriptions.value =result.suscripciones
            } catch (e: Exception) {
                _errorMessage.value = e.message?:""
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun comfirmarSuscripcion(purchaseToken: String, productId: String) {
        Log.e("PROCESS_PURCHASE", "-->comfirmarSuscripcion: Iniciando corrutina")
        viewModelScope.launch { // Esto ya es Main por defecto
            _isLoading.value = true

            // Cambiamos a IO para la operación de red
            val result = withContext(Dispatchers.IO) {
                try {
                    authRepository.confirmarSuscripcion(
                        SuscripcionPurchaseRequest(profileState!!.email, purchaseToken, productId,currentProductSelected.value?.price.toString())
                    )
                } catch (e: Exception) {
                    Log.e("PROCESS_PURCHASE", "-->Error en repositorio: ${e.message}")
                    null // Retornamos null para manejar el error después
                }
            }

            if (result?.success == true) {
                _isSubscribed.value = true
                // ... resto de tu lógica de éxito (esto corre en Main nuevamente gracias a withContext)
                Log.e("PROCESS_PURCHASE", "-->SUCESS")
                // ... guardar datos
            } else {
                _isSubscribedError.value = true
            }
            _isLoading.value = false
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun handlePurchase(purchase: Purchase) {
        Log.e("PROCESS_PURCHASE","-->handlePurchase")
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            Log.e("PROCESS_PURCHASE","-->handlePurchase:TRUE")
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) {
                Log.e("PROCESS_PURCHASE","-->handlePurchase:confirm billingxxx")
                if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.e("PROCESS_PURCHASE","-->handlePurchase:confirm billing")
                    comfirmarSuscripcion(purchase.purchaseToken,currentProductSelected.value!!.productId)
                }else{
                    Log.e("PROCESS_PURCHASE","-->handlePurchase:not confirm billing")
                    _isSubscribedError.value = true
                }
            }
        }else
                Log.e("PROCESS_PURCHASE","-->handlePurchase:FALSE")
    }

    fun updateProfile(userId: String, newUri: Uri?) {
        _isLoading.value =true
        viewModelScope.launch {
            newUri?.let {
                val url = useCase.guardarFoto(userId, it)
                if (url != null) {
                    _newUrlServer.value = url
                    // Actualizar el campo 'url_foto' en Firestore
                    firestore.collection("users").document(userId)
                        .update("fotoUrl", url)
                        .addOnCompleteListener {
                            _isLoading.value =false
                        }.addOnFailureListener {
                            _isLoading.value =false
                        }
                        .await()
                }
            }
        }
    }

    fun updateAllInfo(userId: String, updatedProfile: UserProfileEntity) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Actualizamos el estado LOCAL de inmediato para que la UI no parpadee
                profileState = updatedProfile

                // 2. Usamos solo .await() para manejar la asincronía limpiamente
                firestore.collection("users")
                    .document(userId)
                    .set(updatedProfile, SetOptions.merge())
                    .await()

                // 3. Si llega aquí, es que fue exitoso
                _updateSuccess.value = true
            } catch (e: Exception) {
                Log.e("FIRESTORE_ERROR", "Error al actualizar: ${e.message}")
                // Aquí podrías manejar un _errorState.value = true
            } finally {
                // Se ejecuta tanto si falla como si tiene éxito
                _isLoading.value = false
            }
        }
    }
}