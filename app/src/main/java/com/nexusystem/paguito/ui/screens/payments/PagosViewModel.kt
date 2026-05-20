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
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.domain.usescases.abonos.AbonosUseCase
import com.nexusystem.paguito.domain.usescases.deudores.DeudoresUseCase
import com.nexusystem.paguito.utils.SecureStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
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

    var profileState by mutableStateOf<UserProfileEntity?>(null)
        private set

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        // Recuperamos del Secure Storage
        val savedProfile = secureStorage.getUserProfile()
        profileState = savedProfile
        mail = savedProfile?.email?:""
    }



    @RequiresApi(Build.VERSION_CODES.O)
    fun obtenerAbonos(idDeudor: String) {
        viewModelScope.launch {
            abonosUseCase.obtenerAbonos(idDeudor).collect { list ->
                _pagos.value = list
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
    fun daysUntilExpiration(dateStr: String): Int {
        val formatter = DateTimeFormatter.ofPattern("MM/yyyy")
        val yearMonth = YearMonth.parse(dateStr, formatter)
        val lastDayOfMonth = yearMonth.atEndOfMonth()
        val today = LocalDate.now()
        if (!today.isBefore(lastDayOfMonth)) return 0
        return ChronoUnit.DAYS.between(today, lastDayOfMonth).toInt()
    }

    fun guardarDeudor(recipe: PagosEntinty) {
        viewModelScope.launch {
            registrarPagoRemoteFirebase(mail!!,recipe)
        }
    }

    fun deletePayment(recipe: PagosEntinty) {
        viewModelScope.launch {
            registrarPagoRemoteFirebase(mail!!,recipe)
        }
    }

    fun guardarNuevaVenta(recipe: PagosEntinty) {
        viewModelScope.launch {
            registrarVentaRemoteFirebase(mail!!,recipe)
        }
    }

    fun updateventa(pago: PagosEntinty) {
        viewModelScope.launch {
            abonosUseCase.updateVenta(pago)
        }
    }

    fun resetdeleteSuccess() {
        _deleteSuccess.value = false
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
                        abonosUseCase.guardarVenta(pago)
                        // Opcional: Log o aviso de éxito
                    }

                } catch (e: Exception) {
                    abonosUseCase.guardarVenta(pago)
                    // Importante: No dejes el catch vacío para poder debuguear
                    e.printStackTrace()
                }
            }
        }
    }

    fun deletePagoRemoteFirebase( pago: PagosEntinty?) {
        Log.e("PAGOOOS","-->")
        viewModelScope.launch {
            pago?.let { pago ->
                try {
                    if(profileState?.email.isNullOrEmpty()){
                        Log.e("PAGOOOS","-->1")
                        abonosUseCase.eliminarAbono(pago)
                        _deleteSuccess.value =true
                    }else{
                         firestore.collection("deudores")
                            .document(profileState?.email.toString())
                            .collection("deudores")
                            .document(pago.idDeudorRemoteDatabase)
                            .collection("pagos")
                            .document(pago.idRemoteDatabase)
                             .delete()
                        abonosUseCase.eliminarAbono(pago)
                        _deleteSuccess.value =true
                        Log.e("PAGOOOS","-->2")
                    }

                } catch (e: Exception) {
                    Log.e("PAGOOOS","-->3"+e.toString())
                    abonosUseCase.eliminarAbono(pago)
                    _deleteSuccess.value =true
                    // Importante: No dejes el catch vacío para poder debuguear
                    e.printStackTrace()
                }
            }
        }
    }

    fun registrarPagoRemoteFirebase(userId: String, pago: PagosEntinty?) {
        Log.e("PAGOOOS","-->")
        viewModelScope.launch {
            pago?.let { pago ->
                try {
                    if(userId.isNullOrEmpty()){
                        Log.e("PAGOOOS","-->1")
                        abonosUseCase.guardarAbono(pago)
                    }else{
                        // 1. Creamos una referencia a un nuevo documento (genera el ID localmente)
                        val newDocRef = firestore.collection("deudores")
                            .document(userId)
                            .collection("deudores")
                            .document(pago.idDeudorRemoteDatabase)
                            .collection("pagos")
                            .document()

                        // 2. Obtenemos ese ID generado y lo asignamos a tu entidad
                        val generatedId = newDocRef.id
                        pago.idRemoteDatabase = generatedId
                        // 3. Subimos el objeto que ya incluye su propio ID
                        newDocRef.set(pago, SetOptions.merge()).await()
                        // 4. Guardamos en la base de datos local (Room) con el ID actualizado
                        abonosUseCase.guardarAbono(pago)
                        Log.e("PAGOOOS","-->2")
                    }

                } catch (e: Exception) {
                    Log.e("PAGOOOS","-->3"+e.toString())
                    abonosUseCase.guardarAbono(pago)
                    // Importante: No dejes el catch vacío para poder debuguear
                    e.printStackTrace()
                }
            }
        }
    }
}