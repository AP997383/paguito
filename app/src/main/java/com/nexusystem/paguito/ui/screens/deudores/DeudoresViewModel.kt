package com.nexusystem.paguito.ui.screens.deudores

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
import com.google.firebase.firestore.firestoreSettings
import com.google.gson.Gson
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.models.request.ManualWatsapRequest
import com.nexusystem.paguito.data.remote.cloudFunctions.AuthApi
import com.nexusystem.paguito.data.repository.remote.auth.AuthRepository
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.domain.usescases.abonos.AbonosUseCase
import com.nexusystem.paguito.domain.usescases.deudores.DeudoresUseCase
import com.nexusystem.paguito.utils.SecureStorageManager
import com.nexusystem.paguito.utils.getTodayDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class DeudoresViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val accountSync: com.nexusystem.paguito.data.sync.AccountSyncCoordinator,
    private val deudoresUseCase: DeudoresUseCase,
    private val abonosUseCase: AbonosUseCase,
    private val firestore: FirebaseFirestore,
    private val secureStorage: SecureStorageManager
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _reminderSuccess= MutableStateFlow(false)
    val reminderSuccess: StateFlow<Boolean> = _reminderSuccess
    private val _deudor = MutableStateFlow<DeudoresEntity?>(null)
    val deudor = _deudor.asStateFlow()

    private val _deudores = MutableStateFlow<List<DeudoresEntity?>>(emptyList())
    val deudores = _deudores.asStateFlow()

    private val _sumaryFive = MutableStateFlow<DeudoresSummary?>(null)
    val sumaryFive = _sumaryFive.asStateFlow()

    private val _sumaryInvestment = MutableStateFlow<DeudoresSummary?>(null)
    val sumaryInvestment = _sumaryInvestment.asStateFlow()

    private val _sumaryAll = MutableStateFlow<DeudoresSummary?>(null)
    val sumaryAll = _sumaryAll.asStateFlow()
    var profileState by mutableStateOf<UserProfileEntity?>(null)
        private set

    private val _deleteClientSuccess = MutableStateFlow(false)
    val deleteClientSuccess = _deleteClientSuccess.asStateFlow()

    val settings = firestoreSettings {
        isPersistenceEnabled = true
    }
    fun resetreminderSuccess(){
        _reminderSuccess.value=false
    }
    fun manualWatsap(typeReminder:Int,nombreCliente:String,telefonoCliente:String,saldoRestante:String,proximoPago:String) {

        viewModelScope.launch {
            _isLoading.value = true
            _reminderSuccess.value = true
            try {
                val result = authRepository.manualWatsap(ManualWatsapRequest(telefonoCliente,profileState?.fullName?:"",nombreCliente,profileState?.email?:"",saldoRestante,typeReminder,profileState?.phone?:"",proximoPago))
                _reminderSuccess.value = true
            } catch (e: Exception) {
                _reminderSuccess.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }


    var mail by mutableStateOf<String?>("")
        private set

    init {
        viewModelScope.launch { secureStorage.profileChanges.collect { loadUserProfile() } }
        firestore.firestoreSettings = settings
        loadUserProfile()
        viewModelScope.launch {
            secureStorage.accountKeys.collectLatest {
                _deudores.value = emptyList(); _sumaryFive.value = null; _sumaryAll.value = null
                loadUserProfile()
                refreshCloudData()
            }
        }
    }

    /** Refresca al volver a Inicio, conservando la caché durante errores de conexión. */
    fun refreshCloudData(force: Boolean = false) {
        val email = secureStorage.getUserProfile()?.email ?: return
        viewModelScope.launch {
            try { accountSync.refresh(email, force = force) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (failure: Exception) { if (com.nexusystem.paguito.BuildConfig.DEBUG) Log.w("ACCOUNT_SYNC", "No se pudo actualizar la caché: ${failure.javaClass.simpleName}") }
        }
    }

    fun loadUserProfile() {
        // Recuperamos del Secure Storage
        val savedProfile = secureStorage.getUserProfile()
        mail = savedProfile?.email?:""
        profileState = savedProfile
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerDeudores() {
        viewModelScope.launch {
            deudoresUseCase.obtenerDeudores().collect { list ->
                _deudores.value = list
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun eliminarDeudorYpagos(idDeudor:String) {
        val client = _deudores.value.filterNotNull().firstOrNull { it.id?.toString() == idDeudor } ?: return
        val email = secureStorage.getUserProfile()?.email.orEmpty()
        viewModelScope.launch {
            try {
                if (email.isNotBlank() && client.idRemoteDatabase.isNotBlank()) {
                    firestore.collection("deudores").document(email).collection("deudores")
                        .document(client.idRemoteDatabase).update(mapOf("isDeleted" to true, "deletedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp())).await()
                }
                if (secureStorage.getUserProfile()?.email.orEmpty() != email) return@launch
                deudoresUseCase.aliminarDeudorDeudor(idDeudor)
                abonosUseCase.eliminarAbonos(idDeudor)
            } catch (_: Exception) {
                if (com.nexusystem.paguito.BuildConfig.DEBUG) Log.w("Paguito", "No se pudo confirmar la baja del cliente")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtener5DatosCards() {
        viewModelScope.launch {
            deudoresUseCase.obtener5DatosDeCards().collect { list ->
                _sumaryFive.value = list
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerInversionTotal() {
        viewModelScope.launch {
            deudoresUseCase.obtener5DatosDeCards().collect { list ->
                _sumaryFive.value = list
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerTodosDatosCards() {
        viewModelScope.launch {
            deudoresUseCase.obtenerDatosDeCards().collect { list ->
                _sumaryAll.value = list
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun guardarDeudor(recipe: DeudoresEntity) {
        viewModelScope.launch {
            saveDeudorInFirebase(mail!!,recipe)
        }
    }

    fun guardarDeudores(recipe: ArrayList<DeudoresEntity>) {
        viewModelScope.launch {
            deudoresUseCase.agregarDeudores(recipe)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    /** Editar el contacto nunca vuelve a publicar un saldo guardado antes de un movimiento en otro teléfono. */
    fun updateDeudor(producto: DeudoresEntity?) {
        val client = producto ?: return
        val email = secureStorage.getUserProfile()?.email.orEmpty()
        viewModelScope.launch {
            try {
                if (email.isBlank() || client.idRemoteDatabase.isBlank()) { deudoresUseCase.agregarDeudor(client); return@launch }
                require(client.ownerAccount == secureStorage.currentAccountKey())
                firestore.collection("deudores").document(email).collection("deudores").document(client.idRemoteDatabase)
                    .update(mapOf("nombre" to client.nombre, "telefono" to client.telefono, "correo" to client.correo, "domicilio" to client.domicilio,
                        "periodicidad" to client.periodicidad, "notasSobreDeudor" to client.notasSobreDeudor)).await()
                accountSync.refresh(email, force = true)
            } catch (_: Exception) {
                if (com.nexusystem.paguito.BuildConfig.DEBUG) Log.w("Paguito", "No se pudo confirmar la edición del contacto")
            }
        }
    }

    /** Crea cliente y venta inicial en un lote; el servicio aplica el saldo una sola vez. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun saveDeudorInFirebase(userId: String, producto: DeudoresEntity?) {
        val product = producto ?: return
        viewModelScope.launch {
            if (userId.isBlank()) { deudoresUseCase.agregarDeudor(product); return@launch }
            val owner = com.nexusystem.paguito.data.local.session.LocalAccountKey.fromEmail(userId)
            check(owner == secureStorage.currentAccountKey())
            val document = firestore.collection("deudores").document(userId).collection("deudores").document()
            product.idRemoteDatabase = document.id
            val localId = deudoresUseCase.agregarDeudor(product.copy(inRemote = false))
            try {
                val batch = firestore.batch()
                val initialAmount = product.montoActualAdeudado.toDouble()
                val payload = if (initialAmount > 0) product.copy(montoActualAdeudado = 0f, montoAcomulado = 0f, inRemote = true) else product.copy(inRemote = true)
                batch.set(document, payload, SetOptions.merge())
                val sale = if (initialAmount > 0) PagosEntinty(id = null, idRemoteDatabase = "initial-sale", idDeudorRemoteDatabase = document.id,
                    idDeudor = localId.toString(), montoAbonado = initialAmount, saldoAntesDeAbono = 0.0, fechaAbono = getTodayDateString(),
                    pagoATiempo = true, tipoPago = 1, isIngreso = false, notas = product.notasSobreDeudor.ifBlank { "Deuda inicial" }, jsonAbonoPorProducto = product.idsPorductos) else null
                if (sale != null) batch.set(document.collection("ventas").document("initial-sale"), sale)
                batch.commit().await()
                check(owner == secureStorage.currentAccountKey())
                deudoresUseCase.updateSync(product)
                if (sale != null) abonosUseCase.guardarprimeraVenta(sale.copy(isSynced = true))
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { if (com.nexusystem.paguito.BuildConfig.DEBUG) Log.w("CLIENT_SAVE", "El alta permanece pendiente de confirmación") }
        }
    }

    fun registrarVentaRemoteFirebase(userId: String, pago: PagosEntinty?) {
        viewModelScope.launch {
            pago?.let { pago ->
                try {
                    if(userId.isNullOrEmpty()){
                        abonosUseCase.guardarVenta(pago)
                    }else{
                        // 1. Creamos una referencia a un nuevo documento (genera el ID localmente)
                        val newDocRef = firestore.collection("deudores")
                            .document(userId)
                            .collection("deudores")
                            .document(pago.idDeudorRemoteDatabase)
                            .collection("ventas")
                            .document()
                        // 2. Obtenemos ese ID generado y lo asignamos a tu entidad
                        val generatedId = newDocRef.id
                        pago.idRemoteDatabase = generatedId
                        // 3. Subimos el objeto que ya incluye su propio ID
                        newDocRef.set(pago, SetOptions.merge()).await()
                        // 4. Guardamos en la base de datos local (Room) con el ID actualizado
                        abonosUseCase.guardarprimeraVenta(pago)
                        // Opcional: Log o aviso de éxito
                    }

                } catch (e: Exception) {
                    abonosUseCase.guardarprimeraVenta(pago)
                    // Importante: No dejes el catch vacío para poder debuguear
                    e.printStackTrace()
                }
            }
        }
    }



}