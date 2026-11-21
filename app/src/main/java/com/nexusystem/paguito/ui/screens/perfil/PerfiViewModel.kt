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
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.google.firebase.firestore.Source
import com.nexusystem.paguito.integration.analytics.MembershipAnalytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.data.local.entity.SuscriptionsItems
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.local.entity.UserSuscriptionData
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.data.models.firestore.FirestoreUserProfile
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
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
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

    private val _accountDelete = MutableStateFlow(0)
    val accountDelete: StateFlow<Int> = _accountDelete
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
    private val usersCollection = firestore.collection("users")
    private val _currentProductSelected = MutableStateFlow<SuscriptionsItems?>(null)
    val currentProductSelected: StateFlow<SuscriptionsItems?> = _currentProductSelected

     fun setCurrentProduct(param:SuscriptionsItems){
        _currentProductSelected.value = param
    }

    private val _billingBusy = MutableStateFlow(false)
    val billingBusy = _billingBusy.asStateFlow()
    private val _billingError = MutableStateFlow("")
    val billingError = _billingError.asStateFlow()
    private val _storePrices = MutableStateFlow<Map<String, String>>(emptyMap())
    val storePrices = _storePrices.asStateFlow()
    private val storeProducts = mutableMapOf<String, ProductDetails>()
    private val processingPurchases = mutableSetOf<String>()
    private var profileRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    private var launchedPurchase = false
    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        _billingBusy.value = false
        val userInitiated = launchedPurchase
        launchedPurchase = false
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach { handlePurchase(it, showSuccess = userInitiated) }
            BillingClient.BillingResponseCode.USER_CANCELED -> MembershipAnalytics.event(context, "membership_purchase_result", profileState?.email, result = "cancelled")
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> restorePurchases(showSuccess = true)
            else -> { _billingError.value = "No se pudo completar la compra con Google Play. Intenta nuevamente." }
        }
    }
    private val billingClient = BillingClient.newBuilder(context).setListener(purchasesUpdatedListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection().build()

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
        updateMyLocalDataProfile()
    }


    fun resetAllStatusPurchase(){
        _isSubscribedError.value =false
        _isSubscribed.value=false
    }

    fun resetDelete(){
        _accountDelete.value =0
    }

    /** Actualiza la sesión desde Firestore normalizando precios numéricos y conservando la caché si el documento falla. */
    fun updateMyLocalDataProfile() {
        val email = profileState?.email?.takeIf { it.isNotBlank() } ?: return
        profileRegistration?.remove()
        profileRegistration = usersCollection.document(email).addSnapshotListener { snapshot, error ->
            if (error != null) {
                if (BuildConfig.DEBUG) Log.w("PROFILE_SYNC", "No se pudo consultar el perfil")
                return@addSnapshotListener
            }
            if (snapshot == null || !snapshot.exists()) return@addSnapshotListener

            try {
                val profile = snapshot.toObject(FirestoreUserProfile::class.java)?.toEntity()
                    ?: return@addSnapshotListener
                if (!secureStorage.updateCurrentUserProfile(profile)) return@addSnapshotListener
                profileState = profile
            } catch (exception: RuntimeException) {
                // Una respuesta incompatible no debe cerrar la app ni reemplazar el perfil guardado.
                _errorMessage.value = "No se pudo actualizar el perfil. Intenta nuevamente."
                if (BuildConfig.DEBUG) Log.w("PROFILE_SYNC", "Formato de perfil no compatible")
            }
        }
    }

    /** Un evento Analytics y un registro idempotente de visita, separados del historial de pagos. */
    fun trackPackagesVisit() {
        val email = secureStorage.getUserProfile()?.email ?: return
        val visitId = java.util.UUID.randomUUID().toString()
        MembershipAnalytics.event(context, "view_subscription_packages", email)
        viewModelScope.launch {
            val result = runCatching { authRepository.trackMembershipPackages(email, visitId, MembershipAnalytics.installationID(context)) }.getOrNull() ?: return@launch
            if (result.success && secureStorage.getUserProfile()?.email == email) MembershipAnalytics.cacheIdentity(context, email, result.analyticsID)
        }
    }

    /** Consulta derechos existentes al conectar, también los ya reconocidos por Google. */
    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts(setOf("premium_monthly_basic")); restorePurchases()
                }
            }
            override fun onBillingServiceDisconnected() { /* La reconexión automática evita clientes y callbacks duplicados. */ }
        })
    }

    /** El precio mostrado y el producto comprado se obtienen del mismo catálogo de Google Play. */
    private fun queryProducts(ids: Set<String>) {
        if (ids.isEmpty()) return
        val params = QueryProductDetailsParams.newBuilder().setProductList(ids.map { id ->
            QueryProductDetailsParams.Product.newBuilder().setProductId(id).setProductType(BillingClient.ProductType.SUBS).build()
        }).build()
        billingClient.queryProductDetailsAsync(params) { result, details ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                details.productDetailsList.forEach { product -> storeProducts[product.productId] = product }
                _productDetails.value = storeProducts["premium_monthly_basic"]
                _storePrices.value = storeProducts.mapNotNull { (id, product) ->
                    regularOffer(product)?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice?.let { id to it }
                }.toMap()
            }
        }
    }

    /** No selecciona automáticamente pruebas o promociones con condiciones distintas del plan mostrado. */
    private fun regularOffer(product: ProductDetails) = product.subscriptionOfferDetails?.filter {
        it.offerId == null && it.pricingPhases.pricingPhaseList.lastOrNull()?.billingPeriod == "P1M"
    }?.singleOrNull()

    /** Antes de cobrar comprueba el perfil actual y bloquea clics repetidos o sesiones reemplazadas. */
    fun launchPurchaseFlow(activity: Activity) {
        if (_billingBusy.value || processingPurchases.isNotEmpty()) return
        val owner = secureStorage.getUserProfile()?.email?.trim()?.lowercase().orEmpty()
        val productId = currentProductSelected.value?.productId ?: return
        val product = storeProducts[productId] ?: run { _billingError.value = "No pudimos cargar el precio de Google Play."; return }
        val offer = regularOffer(product) ?: return
        _billingBusy.value = true; _billingError.value = ""
        viewModelScope.launch {
            try {
                val profile = refreshBillingProfile(owner)
                if (profile.userSuscription.isActive) return@launch
                val existing = kotlinx.coroutines.withTimeout(15_000) { queryOwnedPurchases() }
                if (existing.isNotEmpty()) {
                    existing.forEach { handlePurchase(it, showSuccess = true) }
                    if (_billingError.value.isBlank()) _billingError.value = "Ya existe una compra en Google Play. Se está recuperando; no necesitas volver a pagar."
                    _billingBusy.value = processingPurchases.isNotEmpty()
                    return@launch
                }
                val status = authRepository.prepareMembershipPurchase(owner, productId)
                check(secureStorage.getUserProfile()?.email?.trim()?.lowercase() == owner)
                if (status.isPremium) { refreshBillingProfile(owner); _billingBusy.value = false; return@launch }
                check(status.success && status.canPurchase)
                val params = BillingFlowParams.newBuilder().setObfuscatedAccountId(MembershipAnalytics.accountKey(owner))
                    .setProductDetailsParamsList(listOf(BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).setOfferToken(offer.offerToken).build())).build()
                MembershipAnalytics.event(context, "membership_purchase_started", owner, productId)
                launchedPurchase = true
                val result = billingClient.launchBillingFlow(activity, params)
                if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                    launchedPurchase = false; _billingBusy.value = false
                    _billingError.value = "Google Play no pudo abrir la compra. Intenta nuevamente."
                }
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) { _billingError.value = "Google Play no respondió. No se inició una compra."; _billingBusy.value = false }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { _billingError.value = "No pudimos comprobar tu membresía. No se inició una compra."; _billingBusy.value = false }
            finally { if (secureStorage.getUserProfile()?.userSuscription?.isActive == true) _billingBusy.value = false }
        }
    }

    /** Consulta suspendible previa al cobro, incluyendo compras pendientes o ya reconocidas. */
    private suspend fun queryOwnedPurchases(): List<Purchase> = kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { result, purchases ->
            if (continuation.isActive) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) continuation.resumeWith(Result.success(purchases))
                else continuation.resumeWith(Result.failure(IllegalStateException("Google Play no disponible")))
            }
        }
    }

    /** Recuperar vuelve a validar derechos existentes; nunca abre el diálogo para volver a pagar. */
    fun restorePurchases(showSuccess: Boolean = false) {
        billingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) purchases.forEach { handlePurchase(it, showSuccess) }
            else if (showSuccess) _billingError.value = "No pudimos conectar con Google Play. Intenta restaurar nuevamente."
        }
    }

    /** Fuente autorizada de beneficios: no desactiva por fecha ni por ausencia de compras de Google. */
    private suspend fun refreshBillingProfile(owner: String): UserProfileEntity {
        check(owner.isNotBlank() && secureStorage.getUserProfile()?.email?.trim()?.lowercase() == owner)
        val snapshot = usersCollection.document(owner).get(Source.SERVER).await()
        val profile = snapshot.toObject(FirestoreUserProfile::class.java)?.toEntity() ?: error("Perfil no disponible")
        check(secureStorage.updateCurrentUserProfile(profile))
        profileState = profile
        return profile
    }

    /** Libera las conexiones del ViewModel sin modificar compras ni perfiles del servidor. */
    override fun onCleared() {
        profileRegistration?.remove(); billingClient.endConnection(); super.onCleared()
    }

    fun getAllSuscriptions() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value =""
            try {
                val result = authRepository.getAllSuscriptions()
                _suscriptions.value = result.suscripciones
                queryProducts(result.suscripciones.map { it.productId }.filter { it.isNotBlank() }.toSet())
            } catch (e: Exception) {
                _errorMessage.value = e.message?:""
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMyAccount() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value =""
            try {
                val result = authRepository.deleteMyAccount(profileState!!.email)
                if(result){
                    _accountDelete.value =1
                }else{
                    _accountDelete.value =2
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message?:""
                _accountDelete.value =2
            } finally {
                _isLoading.value = false
            }
        }
    }
    /** Valida cada token una sola vez en vuelo y reconoce en Google únicamente después de confirmar beneficios. */
    private fun handlePurchase(purchase: Purchase, showSuccess: Boolean = false) {
        if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
            _billingError.value = "La compra está pendiente de aprobación. No necesitas volver a pagar."
            return
        }
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        val owner = secureStorage.getUserProfile()?.email?.trim()?.lowercase().orEmpty()
        if (owner.isBlank()) return
        val accountId = purchase.accountIdentifiers?.obfuscatedAccountId
        if (!accountId.isNullOrBlank() && accountId != MembershipAnalytics.accountKey(owner)) return
        val productId = purchase.products.singleOrNull() ?: return
        val key = MembershipAnalytics.accountKey(purchase.purchaseToken)
        if (!processingPurchases.add(key)) return
        _billingBusy.value = true; _billingError.value = ""
        viewModelScope.launch {
            try {
                com.nexusystem.paguito.domain.usescases.billing.confirmMembershipPurchase(
                    owner = owner,
                    currentOwner = { secureStorage.getUserProfile()?.email?.trim()?.lowercase().orEmpty() },
                    validate = { authRepository.confirmarSuscripcion(SuscripcionPurchaseRequest(owner, purchase.purchaseToken, productId, storePrices.value[productId].orEmpty())).success },
                    refresh = { refreshBillingProfile(owner).userSuscription.isActive },
                    acknowledge = {
                        if (purchase.isAcknowledged) true else {
                            val params = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                            val result = kotlinx.coroutines.withTimeout(15_000) {
                                kotlinx.coroutines.suspendCancellableCoroutine<BillingResult> { continuation ->
                                    billingClient.acknowledgePurchase(params) { if (continuation.isActive) continuation.resumeWith(Result.success(it)) }
                                }
                            }
                            result.responseCode == BillingClient.BillingResponseCode.OK
                        }
                    }
                )
                MembershipAnalytics.event(context, "membership_validation", owner, productId, "accepted")
                if (showSuccess) _isSubscribed.value = true
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                if (secureStorage.getUserProfile()?.email?.trim()?.lowercase() == owner) _billingError.value = "La compra sigue pendiente de confirmar. Usa Restaurar compras; no necesitas volver a pagar."
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) {
                if (secureStorage.getUserProfile()?.email?.trim()?.lowercase() == owner) {
                    _billingError.value = "La compra está pendiente de validar. Usa Restaurar compras; no necesitas volver a pagar."
                    MembershipAnalytics.event(context, "membership_validation", owner, productId, "pending")
                    if (showSuccess) _isSubscribedError.value = true
                }
            } finally { processingPurchases.remove(key); _billingBusy.value = processingPurchases.isNotEmpty() }
        }
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

    /** El perfil personal no vuelve a publicar una copia antigua de Premium o contadores del servidor. */
    fun updateAllInfo(userId: String, updatedProfile: UserProfileEntity) {
        val owner = secureStorage.getUserProfile()?.email ?: return
        if (!owner.equals(userId, true) || !owner.equals(updatedProfile.email, true)) return
        _isLoading.value = true
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId).update(mapOf(
                    "fullName" to updatedProfile.fullName, "phone" to updatedProfile.phone,
                    "bussinesName" to updatedProfile.bussinesName, "fotoUrl" to updatedProfile.fotoUrl
                )).await()
                val current = secureStorage.getUserProfile() ?: return@launch
                val merged = com.nexusystem.paguito.domain.policy.mergeEditableProfile(current, updatedProfile)
                if (secureStorage.updateCurrentUserProfile(merged)) { profileState = merged; _updateSuccess.value = true }
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { _errorMessage.value = "No pudimos guardar el perfil. Intenta nuevamente." }
            finally { _isLoading.value = false }
        }
    }
}
