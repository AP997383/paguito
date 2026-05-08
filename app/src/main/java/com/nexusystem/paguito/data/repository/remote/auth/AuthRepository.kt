package com.nexusystem.paguito.data.repository.remote.auth

import com.nexusystem.paguito.data.models.request.ChangePasswordRequest
import com.nexusystem.paguito.data.models.request.GetAllMyDataRequest
import com.nexusystem.paguito.data.models.request.LoginRequest
import com.nexusystem.paguito.data.models.request.SuscripcionPurchaseRequest
import com.nexusystem.paguito.data.models.request.VerifyOtpRequest
import com.nexusystem.paguito.data.models.responses.GetAllMyDataResponse
import com.nexusystem.paguito.data.models.responses.LoginResponse
import com.nexusystem.paguito.data.models.responses.SuscripcionPurchaseResponse
import com.nexusystem.paguito.data.models.responses.SuscriptionsResponse
import com.nexusystem.paguito.data.remote.cloudFunctions.AuthApi
import com.nexusystem.paguito.domain.data.auth.UserDataModelAuth
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi
) {

    suspend fun verifyOtp(email: String, otp: String): Boolean {
        val response = authApi.verifyOtp(
            VerifyOtpRequest(email, otp)
        )

        if (response.isSuccessful) {
            return response.body()?.verified == true
        } else {
            throw kotlin.Exception(
                response.errorBody()?.string() ?: "OTP inválido"
            )
        }
    }

    suspend fun changePassword(email: String, newPassword: String): Boolean {
        val response = authApi.changePassword(
            ChangePasswordRequest(email, newPassword)
        )

        if (response.isSuccessful) {
            return response.body()?.ok == true
        } else {
            throw kotlin.Exception(
                response.errorBody()?.string() ?: "Error al cambiar password"
            )
        }
    }



    suspend fun completeRegister(dataUser : UserDataModelAuth): String {
        val response = authApi.register(dataUser)
        if (response.isSuccessful) {
            if(!response.body()?.error.isNullOrEmpty()){
                return "El usuario ya existe, intenta iniciar session con tus accesos."
            }else{
                return "SUCCESS"
            }
        } else {
            return "El usuario ya existe, intenta iniciar session con tus accesos."
        }
    }

    suspend fun loginUser(email:String,password: String,token:String): LoginResponse {
        val response = authApi.loginUser(LoginRequest(email, password,token))
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw kotlin.Exception(
                response.errorBody()?.string() ?: "OTP inválido"
            )
        }
    }

    suspend fun getAllMyData(email:String): GetAllMyDataResponse {
        val response = authApi.getAllMyData(GetAllMyDataRequest(email))
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw kotlin.Exception(
                response.errorBody()?.string() ?: "OTP inválido"
            )
        }
    }

    suspend fun getAllSuscriptions(): SuscriptionsResponse {
        val response = authApi.getActualSuscriptions()
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw kotlin.Exception(
                response.errorBody()?.string() ?: "OTP inválido"
            )
        }
    }

    suspend fun confirmarSuscripcion(request:SuscripcionPurchaseRequest): SuscripcionPurchaseResponse {
        val response = authApi.confirmarSuscripcion(request)
        if (response.isSuccessful) {
            return response.body()!!
        } else {
            throw kotlin.Exception(
                response.errorBody()?.string() ?: "OTP inválido"
            )
        }
    }

}