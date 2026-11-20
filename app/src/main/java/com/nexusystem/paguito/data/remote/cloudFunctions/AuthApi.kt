package com.nexusystem.paguito.data.remote.cloudFunctions

import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.data.models.request.ChangePasswordRequest
import com.nexusystem.paguito.data.models.request.DeleteAccountRequest
import com.nexusystem.paguito.data.models.request.GetAllMyDataRequest
import com.nexusystem.paguito.data.models.request.LoginRequest
import com.nexusystem.paguito.data.models.request.ManualWatsapRequest
import com.nexusystem.paguito.data.models.request.SendOtpRequest
import com.nexusystem.paguito.data.models.request.SuscripcionPurchaseRequest
import com.nexusystem.paguito.data.models.request.VerifyOtpRequest
import com.nexusystem.paguito.data.models.request.recordatoriosRequest
import com.nexusystem.paguito.data.models.responses.ChangePasswordResponse
import com.nexusystem.paguito.data.models.responses.DeleteAccountResponse
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
import retrofit2.http.Url

interface AuthApi {

    // Función auxiliar interna para construir la URL basada en el hash de BuildConfig
    private fun getFunctionUrl(functionName: String): String {
        val hash = BuildConfig.CLOUD_RUN_HASH
        return "https://$functionName-$hash-uc.a.run.app"
    }

    @POST
    suspend fun sendOtp(
        @Url url: String = getFunctionUrl("sendotp"),
        @Body request: SendOtpRequest
    ): Response<SendOtpResponse>

    @POST
    suspend fun verifyOtp(
        @Url url: String = getFunctionUrl("verifyotp"),
        @Body request: VerifyOtpRequest
    ): Response<VerifyOtpResponse>

    @POST
    suspend fun getActualSuscriptions(
        @Url url: String = getFunctionUrl("getsuscriptions")
    ): Response<SuscriptionsResponse>

    @POST
    suspend fun deleteMyAccount(
        @Url url: String = getFunctionUrl("borrarusuarioporemail"),
        @Body request: DeleteAccountRequest
    ): Response<DeleteAccountResponse>

    /** Registra una visita sin efectos sobre compras ni beneficios. */
    @POST
    suspend fun membershipPackagesViewed(
        @Url url: String = getFunctionUrl("membershippackagesviewed"),
        @Body request: com.nexusystem.paguito.data.models.request.MembershipVisitRequest
    ): Response<com.nexusystem.paguito.data.models.responses.MembershipVisitResponse>

    /** Comprueba disponibilidad y membresía antes de abrir Google Play. */
    @POST
    suspend fun membershipPurchaseStatus(
        @Url url: String = getFunctionUrl("membershippurchasestatus"),
        @Body request: com.nexusystem.paguito.data.models.request.MembershipPurchaseStatusRequest
    ): Response<com.nexusystem.paguito.data.models.responses.MembershipPurchaseStatusResponse>

    @POST
    suspend fun confirmarSuscripcion(
        @Url url: String = getFunctionUrl("verifyandsyncpurchase"),
        @Body request: SuscripcionPurchaseRequest
    ): Response<SuscripcionPurchaseResponse>

    @POST
    suspend fun getAllMyData(
        @Url url: String = getFunctionUrl("obtenerdatoscompletos"),
        @Body request: GetAllMyDataRequest
    ): Response<GetAllMyDataResponse>

    @POST
    suspend fun changePassword(
        @Url url: String = getFunctionUrl("changepassword"),
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @POST
    suspend fun agendarRecordatorios(
        @Url url: String = getFunctionUrl("agendarrecordatoriosdeudor"),
        @Body request: recordatoriosRequest
    ): Response<ChangePasswordResponse>

    @POST
    suspend fun manualWatsap(
        @Url url: String = getFunctionUrl("manualreminder"),
        @Body request: ManualWatsapRequest
    ): Response<ChangePasswordResponse>

    @POST
    suspend fun register(
        @Url url: String = getFunctionUrl("register"),
        @Body request: UserDataModelAuth
    ): Response<RegisterUserResponse>

    @POST
    suspend fun loginUser(
        @Url url: String = getFunctionUrl("login"),
        @Body request: LoginRequest
    ): Response<LoginResponse>
}