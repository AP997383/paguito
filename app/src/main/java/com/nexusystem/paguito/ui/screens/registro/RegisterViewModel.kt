package com.nexusystem.paguito.ui.screens.registro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexusystem.paguito.data.models.request.SendOtpRequest
import com.nexusystem.paguito.data.models.responses.LoginResponse
import com.nexusystem.paguito.data.remote.cloudFunctions.AuthApi
import com.nexusystem.paguito.data.repository.remote.auth.AuthRepository
import com.nexusystem.paguito.domain.data.auth.UserDataModelAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authApi: AuthApi,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _otpSuseccfull= MutableStateFlow<String>("")
    val otpSuseccfull = _otpSuseccfull.asStateFlow()


    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isVerified = MutableStateFlow(false)
    val isVerified: StateFlow<Boolean> = _isVerified


    private val _isUserRegistered = MutableStateFlow(false)
    val isUserRegistered: StateFlow<Boolean> = _isUserRegistered

    private val _loginSuccessFull = MutableStateFlow<LoginResponse?>(null)
    val loginSuccessFull: StateFlow<LoginResponse?> = _loginSuccessFull

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun resetOtpSucessfull(){
        _otpSuseccfull.value =""
    }

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


    fun verifyOtp(email: String, otp: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.verifyOtp(email, otp)
                _isVerified.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun loginUser(email: String,password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = authRepository.loginUser(email,password)
                _loginSuccessFull.value = result
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }


}