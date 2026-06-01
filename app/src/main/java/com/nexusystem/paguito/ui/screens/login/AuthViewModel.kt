package com.nexusystem.paguito.ui.screens.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusystem.paguito.data.local.entity.SuscriptionsItems
import com.nexusystem.paguito.data.models.request.SendOtpRequest
import com.nexusystem.paguito.data.models.responses.LoginResponse
import com.nexusystem.paguito.data.models.responses.SuscriptionsResponse
import com.nexusystem.paguito.data.remote.cloudFunctions.AuthApi
import com.nexusystem.paguito.data.repository.remote.auth.AuthRepository
import com.nexusystem.paguito.domain.data.auth.UserDataModelAuth
import com.nexusystem.paguito.domain.usescases.abonos.AbonosUseCase
import com.nexusystem.paguito.domain.usescases.deudores.DeudoresUseCase
import com.nexusystem.paguito.domain.usescases.productos.ProductosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val authRepository: AuthRepository,
    private val productosUserCase: ProductosUseCase,
    private val pagosUserCase: AbonosUseCase,
    private val deudoresUserCase: DeudoresUseCase,
) : ViewModel() {
    private val _otpSuseccfull= MutableStateFlow<String>("")
    val otpSuseccfull = _otpSuseccfull.asStateFlow()

    private val _allDataSucessfull= MutableStateFlow<Boolean>(false)
    val allDataSucessfull = _allDataSucessfull.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isVerified = MutableStateFlow(0)
    val isVerified: StateFlow<Int> = _isVerified

    private val _changePasswordSucess = MutableStateFlow(false)
    val changePasswordSucess: StateFlow<Boolean> = _changePasswordSucess


    private val _isUserRegistered = MutableStateFlow(false)
    val isUserRegistered: StateFlow<Boolean> = _isUserRegistered

    private val _userAlreadyExist = MutableStateFlow("")
    val userAlreadyExist: StateFlow<String> = _userAlreadyExist

    private val _loginSuccessFull = MutableStateFlow<LoginResponse?>(null)
    val loginSuccessFull: StateFlow<LoginResponse?> = _loginSuccessFull



    private val _errorMessage = MutableStateFlow<String>("")
    val errorMessage: StateFlow<String> = _errorMessage

    fun sendOtp(email: String) = viewModelScope.launch {
        _isLoading.value =true
        val response = authApi.sendOtp(SendOtpRequest(email))

        if (response.isSuccessful && response.body()?.success == true) {
            _isLoading.value =false
            _otpSuseccfull.value = "TRUE"
        } else {
            _isLoading.value =false
            _otpSuseccfull.value = "FAIL"
        }
    }

    fun resetValuesRegister(){
        _isUserRegistered.value = false
    }

    fun resetLogin(){
        _userAlreadyExist.value =""
        _loginSuccessFull.value =null
    }

    fun resetVerified(){
        _isVerified.value  =0
    }
    fun resetChangePassword(){
        _changePasswordSucess.value  =false
    }
    fun clearError(){
        _userAlreadyExist.value=""
        _errorMessage.value = ""
    }
    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val result = authRepository.verifyOtp(email, otp)
                if(result){
                    _isVerified.value = 1
                }else{
                    _isVerified.value = 2
                }

            } catch (e: Exception) {
                _isVerified.value = 2
                _errorMessage.value = e.message?:""
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun changePassword(email: String, newPassword: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            try {
                val result = authRepository.changePassword(email, newPassword)
                _changePasswordSucess.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message?:""
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loginUser(email: String,password: String,token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value =""

            try {
                val result = authRepository.loginUser(email,password,token)
                _loginSuccessFull.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message?:""
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getAllMyData(email: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value =""

            try {
                val result = authRepository.getAllMyData(email)
                if(!result.productos.isNullOrEmpty()) {
                    productosUserCase.guardarProductos(result.productos)
                }
                if(!result.deudores.isNullOrEmpty()) {
                    deudoresUserCase.agregarDeudores(result.deudores)
                    result.deudores.forEach { deudor->
                        Log.e("NOMBRE_DEUDOR","-->"+deudor.nombre)
                        Log.e("NOMBRE_DEUDOR","pagos-->"+deudor.pagos)
                        Log.e("NOMBRE_DEUDOR","ventas-->"+deudor.ventas)
                        if(!deudor.pagos.isNullOrEmpty()){
                            pagosUserCase.guardarAbonos(deudor.pagos)
                        }
                        if(!deudor.ventas.isNullOrEmpty()){
                            pagosUserCase.guardarAbonos(deudor.ventas)
                        }
                    }
                }
                _allDataSucessfull.value =true
            } catch (e: Exception) {
                _errorMessage.value = e.message?:""
            } finally {
                _isLoading.value = false
            }
        }
    }



    fun completeRegisterUser(data: UserDataModelAuth) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            Log.e("ESTATUS REGISTER","SI")
            try {
                val result = authRepository.completeRegister(data)
                if(result.equals("SUCCESS"))
                   _isUserRegistered.value = true
                else{
                    _userAlreadyExist.value =result
                }
                Log.e("ESTATUS REGISTER","SI 2")
            } catch (e: Exception) {
                Log.e("ESTATUS REGISTER","SI 3")
                _userAlreadyExist.value ="Usuario ya existe"
                _errorMessage.value = e.message?:""
            } finally {
                _isLoading.value = false
            }
        }
    }
}