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
import com.google.gson.Gson
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.domain.usescases.productos.ProductosUseCase
import com.nexusystem.paguito.utils.SecureStorageManager
import com.nexusystem.paguito.utils.getTodayDateString
import dagger.hilt.android.lifecycle.HiltViewModel
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
class ProductosViewModel @Inject constructor(
    private val productosUserCase: ProductosUseCase,
    private val firestore: FirebaseFirestore,
    private val secureStorage: SecureStorageManager
) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _numProducts = MutableStateFlow(0)
    val numProducts: StateFlow<Int> = _numProducts

    private val _newUrlServer = MutableStateFlow<String>("")
    val newUrlServer = _newUrlServer.asStateFlow()
    private val _medicine = MutableStateFlow<PorductosEntity?>(null)
    val medicine = _medicine.asStateFlow()

    private val _produtosList = MutableStateFlow<List<PorductosEntity?>>(emptyList())
    val produtosList = _produtosList.asStateFlow()
    var profileState by mutableStateOf<UserProfileEntity?>(null)
        private set

    private val _medicineNotFound = MutableStateFlow(false)
    val medicineNotFound = _medicineNotFound.asStateFlow()

    var mail by mutableStateOf<String?>("")
        private set

    init {
        loadUserProfile()
    }
    fun setUriLocal(url: String){
        _newUrlServer.value = url
    }


    fun loadUserProfile() {
        // Recuperamos del Secure Storage
        val savedProfile = secureStorage.getUserProfile()
        mail = savedProfile?.email?:""
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
            productosUserCase.obtenerNumeroProductos().collect { list ->
                _numProducts.value = list
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

    fun addNewCartilla(producto: PorductosEntity) {

        if(!mail.isNullOrEmpty()) {
            viewModelScope.launch {
                saveProductInFirebase(mail!!, producto)
                _newUrlServer.value =""
            }
        }
    }

    fun deleteProduct(producto: PorductosEntity) {
        viewModelScope.launch {
            productosUserCase.deleteProduct(producto)
        }
        if(!mail.isNullOrEmpty()) {
            Log.e("DELETE_PRODUCT","SI")
            viewModelScope.launch {
                deleteProduct(mail!!, producto)
                _newUrlServer.value =""
            }
        }
    }


    fun guardarProductos(producto: ArrayList<PorductosEntity>) {
        viewModelScope.launch {
           // productosUserCase.guardarProductos(producto)
        }
    }

    fun savePhotoProduct(userId: String, newUri: Uri?) {
        _isLoading.value =true
        viewModelScope.launch {
            newUri?.let {
                Log.e("IMAGEN_PRODUCTO","SI 2")
                val url = productosUserCase.guardarFoto(userId, it)
                Log.e("IMAGEN_PRODUCTO","SI 3"+url)
                if (url != null) {
                    _isLoading.value =false
                    _newUrlServer.value = url
                }else{
                    _isLoading.value =false
                }
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
            //productosUserCase.delete(recipe)
        }
    }

    fun reset() {
        _medicineNotFound.value = false
    }

    fun saveProductInFirebase(userId: String, producto: PorductosEntity?) {
        viewModelScope.launch {

            // 1. Preparamos los datos
            val newDocRef =  firestore.collection("productos").document(userId)
                .collection("productos")
                .document()
            val generatedId = newDocRef.id
            producto!!.idRemoteDatabase = generatedId
            val idLocal =  productosUserCase.guardarProducto(producto!!)
            newDocRef.set(producto, SetOptions.merge())
                .addOnSuccessListener {}
                .addOnFailureListener { e ->
                    Log.e("SAVEDEUDOE", "Error en cola de sincronización: ${e.message}")
                }
        }
    }

    fun deleteProduct(userId: String, producto: PorductosEntity?) {
        Log.e("DELETE_PRODUCT","SI"+userId+"/"+producto!!.idRemoteDatabase)
        viewModelScope.launch {
            producto?.let { product ->
                try {
                    firestore.collection("productos").document(userId)
                        .collection("productos")
                        .document(producto.idRemoteDatabase)
                        .delete()
                        .addOnCompleteListener {
                            Log.e("DELETE_PRODUCT","addOnCompleteListener")
                        }
                        .addOnFailureListener {
                            Log.e("DELETE_PRODUCT","addOnFailureListener"+it.toString())
                        }
                        .await()
                } catch (e: Exception) {}
            }
        }
    }
}