package com.nexusystem.paguito.data.remote.cloudFunctions

import com.nexusystem.paguito.data.models.request.ChangePasswordRequest
import com.nexusystem.paguito.data.models.request.GetAllMyDataRequest
import com.nexusystem.paguito.data.models.request.LoginRequest
import com.nexusystem.paguito.data.models.request.SendOtpRequest
import com.nexusystem.paguito.data.models.request.SuscripcionPurchaseRequest
import com.nexusystem.paguito.data.models.request.VerifyOtpRequest
import com.nexusystem.paguito.data.models.responses.ChangePasswordResponse
import com.nexusystem.paguito.data.models.responses.GetAllMyDataResponse
import com.nexusystem.paguito.data.models.responses.LoginResponse
import com.nexusystem.paguito.data.models.responses.RegisterUserResponse
import com.nexusystem.paguito.data.models.responses.SendOtpResponse
import com.nexusystem.paguito.data.models.responses.SuscripcionPurchaseResponse
import com.nexusystem.paguito.data.models.responses.SuscriptionsResponse
import com.nexusystem.paguito.data.models.responses.VerifyOtpResponse
import com.nexusystem.paguito.domain.data.auth.UserDataModelAuth
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthApi {

    @POST("https://sendotp-2a2qk4zvlq-uc.a.run.app")
    suspend fun sendOtp(
        @Body request: SendOtpRequest
    ): Response<SendOtpResponse>

    @POST("https://verifyotp-2a2qk4zvlq-uc.a.run.app")
    suspend fun verifyOtp(
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>

    @POST("https://getsuscriptions-2a2qk4zvlq-uc.a.run.app")
    suspend fun getActualSuscriptions(): Response<SuscriptionsResponse>

    @POST("https://verifyandsyncpurchase-2a2qk4zvlq-uc.a.run.app")
    suspend fun confirmarSuscripcion(   @Body request: SuscripcionPurchaseRequest): Response<SuscripcionPurchaseResponse>

    @POST("https://obtenerdatoscompletos-2a2qk4zvlq-uc.a.run.app")
    suspend fun getAllMyData(
        @Body request: GetAllMyDataRequest
    ): Response<GetAllMyDataResponse>

    @POST("https://changepassword-2a2qk4zvlq-uc.a.run.app")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @POST("https://register-2a2qk4zvlq-uc.a.run.app")
    suspend fun register(
        @Body request: UserDataModelAuth
    ): Response<RegisterUserResponse>

    @POST("https://login-2a2qk4zvlq-uc.a.run.app")
    suspend fun loginUser(
        @Body request: LoginRequest
    ): Response<LoginResponse>


}