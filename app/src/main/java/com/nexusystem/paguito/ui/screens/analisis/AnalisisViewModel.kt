package com.nexusystem.paguito.ui.screens.analisis

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
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.domain.data.ProductosSummary1
import com.nexusystem.paguito.domain.usescases.abonos.AbonosUseCase
import com.nexusystem.paguito.domain.usescases.deudores.DeudoresUseCase
import com.nexusystem.paguito.domain.usescases.productos.ProductosUseCase
import com.nexusystem.paguito.utils.SecureStorageManager
import com.nexusystem.paguito.utils.getTodayDateString
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
class AnalisisViewModel @Inject constructor(
    private val productos: ProductosUseCase,
    private val pagosAbonos: AbonosUseCase,
    private val firestore: FirebaseFirestore,
    private val secureStorage: SecureStorageManager
) : ViewModel() {

    private val _deudor = MutableStateFlow<DeudoresEntity?>(null)
    val deudor = _deudor.asStateFlow()

    private val _deudores = MutableStateFlow<List<DeudoresEntity?>>(emptyList())
    val deudores = _deudores.asStateFlow()



    private val _sumaryInvestment = MutableStateFlow<ProductosSummary1>(ProductosSummary1())
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
    fun obtenerInversionTotal() {
        viewModelScope.launch {
            productos.inversionEnProductos().collect { summary ->
                _sumaryInvestment.value = summary
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

}