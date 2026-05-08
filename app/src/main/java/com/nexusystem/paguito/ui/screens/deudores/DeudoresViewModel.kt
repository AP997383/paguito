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
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.domain.data.DeudoresSummary
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
class DeudoresViewModel @Inject constructor(
    private val deudoresUseCase: DeudoresUseCase,
    private val firestore: FirebaseFirestore,
    private val secureStorage: SecureStorageManager
) : ViewModel() {

    private val _deudor = MutableStateFlow<DeudoresEntity?>(null)
    val deudor = _deudor.asStateFlow()

    private val _deudores = MutableStateFlow<List<DeudoresEntity?>>(emptyList())
    val deudores = _deudores.asStateFlow()

    private val _sumaryFive = MutableStateFlow<DeudoresSummary?>(null)
    val sumaryFive = _sumaryFive.asStateFlow()

    private val _sumaryAll = MutableStateFlow<DeudoresSummary?>(null)
    val sumaryAll = _sumaryAll.asStateFlow()
    var profileState by mutableStateOf<UserProfileEntity?>(null)
        private set

    private val _medicineNotFound = MutableStateFlow(false)
    val medicineNotFound = _medicineNotFound.asStateFlow()

    val settings = firestoreSettings {
        isPersistenceEnabled = true
    }


    var mail by mutableStateOf<String?>("")
        private set

    init {
        firestore.firestoreSettings = settings
        loadUserProfile()
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
    fun obtener5DatosCards() {
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

    fun saveDeudorInFirebase(userId: String, producto: DeudoresEntity?) {
        viewModelScope.launch {
            producto?.let { product ->
                try {
                    if(userId.isNullOrEmpty()){
                        deudoresUseCase.agregarDeudor(product)
                    }else{
                        // 1. Preparamos los datos
                        val newDocRef = firestore.collection("deudores")
                            .document(userId)
                            .collection("deudores")
                            .document()
                        val generatedId = newDocRef.id
                        product.idRemoteDatabase = generatedId
                        deudoresUseCase.agregarDeudor(product)
                        product.inRemote =true
                        newDocRef.set(product, SetOptions.merge())
                            .addOnSuccessListener {
                                viewModelScope.launch {
                                    deudoresUseCase.updateSync(product)
                                    Log.d("SAVEDEUDOE", "Sincronizado con la nube exitosamente")
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("SAVEDEUDOE", "Error en cola de sincronización: ${e.message}")
                            }

                        Log.e("SAVEDEUDOE", "--> Se guardó localmente: " + generatedId)
                    }

                } catch (e: Exception) {
                    deudoresUseCase.agregarDeudor(product)
                    e.printStackTrace()
                }
            }
        }
    }

    fun reset() {
        _medicineNotFound.value = false
    }
}