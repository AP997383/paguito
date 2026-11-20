package com.nexusystem.paguito.ui.screens.payments

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
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.domain.usescases.abonos.AbonosUseCase
import com.nexusystem.paguito.domain.usescases.deudores.DeudoresUseCase
import com.nexusystem.paguito.utils.SecureStorageManager
import com.nexusystem.paguito.utils.getTodayDateString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class PagosViewModel @Inject constructor(
    private val abonosUseCase: AbonosUseCase,
    private val firestore: FirebaseFirestore,
    private val secureStorage: SecureStorageManager
) : ViewModel() {

    private val _deudor = MutableStateFlow<PagosEntinty?>(null)
    val deudor = _deudor.asStateFlow()

    private val _pagos = MutableStateFlow<List<PagosEntinty?>>(emptyList())
    val pagos = _pagos.asStateFlow()

    private val _nextTikectNumber= MutableStateFlow<Int>(0)
    val nextTikectNumber = _nextTikectNumber.asStateFlow()

    private val _pagosConNombre = MutableStateFlow<List<PagoConNombre?>>(emptyList())
    val pagosConNombre  = _pagosConNombre .asStateFlow()

    private val _pagosConNombre5 = MutableStateFlow<List<PagoConNombre?>>(emptyList())
    val pagosConNombre5  = _pagosConNombre5 .asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess = _deleteSuccess.asStateFlow()
    var mail by mutableStateOf<String?>("")
        private set

    var isSaving by mutableStateOf(false)
        private set
    var operationLabel by mutableStateOf("Guardando…")
        private set
    var saveError by mutableStateOf<String?>(null)
        private set

    /** Cierra el error sin volver a enviar una operación ni borrar su formulario. */
    fun clearSaveError() { saveError = null }

    var profileState by mutableStateOf<UserProfileEntity?>(null)
        private set

    private val _pagosByMonth = MutableStateFlow<List<AbonosDelMes>>(emptyList())
    val pagosByMonth = _pagosByMonth.asStateFlow()

    init {
        loadUserProfile()
    }

    val totalPagos: StateFlow<Double> = _pagos
        .map { lista ->
            lista.filterNotNull().filter { it.isIngreso }.sumOf { it.montoAbonado }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun loadUserProfile() {
        // Recuperamos del Secure Storage
        val savedProfile = secureStorage.getUserProfile()
        profileState = savedProfile
        mail = savedProfile?.email?:""
    }



    private var clientPaymentsObservation: kotlinx.coroutines.Job? = null

    /** Al cambiar de cliente descarta el historial anterior y cancela su colección de Room. */
    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerAbonos(idDeudor: String,idRemote:String) {
        clientPaymentsObservation?.cancel()
        _pagos.value = emptyList()
        clientPaymentsObservation = viewModelScope.launch {
            abonosUseCase.obtenerAbonos(idDeudor,idRemote).collect { list ->
                _pagos.value = list.sortedBy { it.fechaAbono }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun nextNumberTiket() {
        viewModelScope.launch {
            abonosUseCase.getLastTiketNumber().collect { next ->
                _nextTikectNumber.value = next
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerUltimosAbonos() {
        viewModelScope.launch {
            abonosUseCase.obtenerUltimosAbonos().collect { list ->
                _pagosConNombre.value = list
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerUltimos5Abonos() {
        viewModelScope.launch {
            abonosUseCase.obtenerUltimosAbonos5().collect { list ->
                _pagosConNombre5.value = list
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerAbonosdelMes() {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        // 1. La fecha límite final es el día de hoy
        val hoy = LocalDate.now()

        // 2. La fecha límite inicial es hoy menos 30 días hacia el pasado
        val hace30Dias = hoy.minusDays(30)

        viewModelScope.launch {
            // Pasamos 'hace30Dias' como la fecha de inicio y 'hoy' como la fecha de fin
            abonosUseCase.obtenerPagosByMonth(
                hace30Dias.format(formatter),
                hoy.format(formatter)
            ).collect { list ->
                _pagosByMonth.value = list
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

    /** Confirma el abono solamente cuando la transacción local se completó. */
    fun guardarDeudor(recipe: PagosEntinty, onSuccess: () -> Unit = {}) {
        persistMovement(secureStorage.getUserProfile()?.email.orEmpty(), recipe, onSuccess)
    }

    fun deletePayment(recipe: PagosEntinty) { deletePagoRemoteFirebase(recipe) }

    /** La venta usa un ID remoto único y no permite dobles envíos desde el formulario. */
    fun guardarNuevaVenta(recipe: PagosEntinty, onSuccess: () -> Unit = {}) {
        persistMovement(secureStorage.getUserProfile()?.email.orEmpty(), recipe, onSuccess)
    }

    /** Conserva la pantalla y sus cambios cuando falla el guardado de productos relacionados. */
    fun updateventa(pago: PagosEntinty, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val email = secureStorage.getUserProfile()?.email.orEmpty()
                val owner = com.nexusystem.paguito.data.local.session.LocalAccountKey.fromEmail(email)
                require(pago.ownerAccount.isBlank() || pago.ownerAccount == owner)
                if (email.isNotBlank() && pago.idDeudorRemoteDatabase.isNotBlank() && pago.idRemoteDatabase.isNotBlank()) {
                    val client = firestore.collection("deudores").document(email).collection("deudores").document(pago.idDeudorRemoteDatabase)
                    val movement = client.collection(if (pago.isIngreso) "pagos" else "ventas").document(pago.idRemoteDatabase)
                    val batch = firestore.batch()
                    // Relacionar productos no vuelve a publicar el importe ni modifica el saldo de la venta.
                    batch.update(movement, "jsonAbonoPorProducto", pago.jsonAbonoPorProducto)
                    batch.update(client, "syncUpdatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp())
                    kotlinx.coroutines.withTimeout(8_000) { batch.commit().await() }
                }
                abonosUseCase.updateVenta(pago)
                onSuccess()
            } catch (timeout: kotlinx.coroutines.TimeoutCancellationException) {
                onError(timeout)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                onError(error)
            }
        }
    }

    fun resetdeleteSuccess() {
        _deleteSuccess.value = false
    }
    /** Conserva el punto de entrada existente delegando al mismo flujo de persistencia. */
    fun registrarVentaRemoteFirebase(userId: String, pago: PagosEntinty?) {
        pago?.let { persistMovement(userId, it) }
    }

    /** Conserva el punto de entrada existente para abonos, sin lanzar corrutinas anidadas. */
    fun registrarPagoRemoteFirebase(userId: String, pago: PagosEntinty?) {
        pago?.let { persistMovement(userId, it) }
    }

    /**
     * Captura la cuenta, asigna un ID único y realiza una sola escritura local.
     * Sin red conserva el movimiento pendiente de sincronizar; un fallo local no muestra éxito.
     * Una operación iniciada en otra cuenta nunca se guarda en la sesión actual.
     */
    private fun persistMovement(userId: String, payment: PagosEntinty, onSuccess: () -> Unit = {}) {
        if (isSaving) return
        if (!payment.montoAbonado.isFinite() || payment.montoAbonado <= 0 || payment.idDeudor.isBlank()) {
            saveError = "Selecciona un cliente e ingresa un monto válido."
            return
        }
        operationLabel = "Guardando…"
        isSaving = true
        saveError = null
        viewModelScope.launch {
            try {
                val owner = com.nexusystem.paguito.data.local.session.LocalAccountKey.fromEmail(userId)
                require(payment.ownerAccount.isBlank() || payment.ownerAccount == owner)
                val currentOwner = com.nexusystem.paguito.data.local.session.LocalAccountKey.fromEmail(secureStorage.getUserProfile()?.email)
                require(currentOwner == owner)
                var record = payment.copy(ownerAccount = owner, isSynced = false)
                val reference = if (userId.isNotBlank() && record.idDeudorRemoteDatabase.isNotBlank())
                    firestore.collection("deudores").document(userId).collection("deudores")
                        .document(record.idDeudorRemoteDatabase).collection(if (record.isIngreso) "pagos" else "ventas").document()
                    else null
                if (reference != null) record.idRemoteDatabase = reference.id
                com.nexusystem.paguito.domain.usescases.abonos.persistMovementOnce(remoteWrite = {
                    if (reference == null) false else kotlinx.coroutines.withTimeoutOrNull(8_000) {
                        reference.set(record, SetOptions.merge()).await()
                        true
                    } ?: false
                }, localWrite = { synced ->
                    val saved = record.copy(isSynced = synced)
                    if (saved.isIngreso) abonosUseCase.guardarAbono(saved) else abonosUseCase.guardarVenta(saved)
                })
                onSuccess()
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { saveError = "No se pudo guardar. Tus datos siguen en el formulario; vuelve a intentarlo." }
            finally { isSaving = false }
        }
    }

    /** Borra en la colección correcta y espera la respuesta antes de reajustar el saldo local. */
    fun deletePagoRemoteFirebase(payment: PagosEntinty?) {
        if (payment == null || isSaving) return
        operationLabel = "Eliminando…"
        isSaving = true
        saveError = null
        _deleteSuccess.value = false
        val email = secureStorage.getUserProfile()?.email.orEmpty()
        viewModelScope.launch {
            try {
                if (email.isNotBlank() && payment.idRemoteDatabase.isNotBlank() && payment.idDeudorRemoteDatabase.isNotBlank()) {
                    val owner = com.nexusystem.paguito.data.local.session.LocalAccountKey.fromEmail(email)
                    require(payment.ownerAccount.isBlank() || payment.ownerAccount == owner)
                    kotlinx.coroutines.withTimeout(8_000) {
                        firestore.collection("deudores").document(email).collection("deudores").document(payment.idDeudorRemoteDatabase)
                            .collection(if (payment.isIngreso) "pagos" else "ventas").document(payment.idRemoteDatabase).delete().await()
                    }
                }
                abonosUseCase.eliminarAbono(payment)
                _deleteSuccess.value = true
            } catch (_: kotlinx.coroutines.TimeoutCancellationException) {
                saveError = "No se pudo confirmar la eliminación. Actualiza la cuenta antes de volver a intentarlo."
            } catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { saveError = "No se pudo eliminar el movimiento. Revisa tu conexión y vuelve a intentarlo." }
            finally { isSaving = false }
        }
    }
}
