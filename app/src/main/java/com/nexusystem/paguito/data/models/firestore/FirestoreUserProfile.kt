package com.nexusystem.paguito.data.models.firestore

import com.google.firebase.firestore.PropertyName
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.local.entity.UserSuscriptionData

/** Contrato de lectura Firestore: admite precios históricos numéricos sin cambiar el perfil local serializado. */
data class FirestoreUserProfile(
    var email: String = "",
    var fotoUrl: String = "",
    var fullName: String = "",
    var bussinesName: String = "",
    var phone: String = "",
    var token: String = "",
    var watsapSended: Int = 0,
    var watsapsSendedTotal: Int = 0,
    var userSuscription: FirestoreUserSubscription = FirestoreUserSubscription(),
    var verified: Boolean = true,
    @get:PropertyName("RemindersActive")
    @set:PropertyName("RemindersActive")
    var RemindersActive: Boolean = false,
    var reminderPreventivo: Boolean = false,
    var reminderAtraso: Boolean = false,
    var reminderAvisoPago: Boolean = false
) {
    /** Convierte a la estructura de sesión existente y conserva los campos del perfil y sus recordatorios. */
    fun toEntity(): UserProfileEntity = UserProfileEntity(
        email = email,
        fotoUrl = fotoUrl,
        fullName = fullName,
        bussinesName = bussinesName,
        phone = phone,
        token = token,
        watsapSended = watsapSended,
        watsapsSendedTotal = watsapsSendedTotal,
        userSuscription = userSuscription.toEntity(),
        verified = verified,
        RemindersActive = RemindersActive,
        reminderPreventivo = reminderPreventivo,
        reminderAtraso = reminderAtraso,
        reminderAvisoPago = reminderAvisoPago
    )
}

/** Mantiene el valor original del precio hasta convertirlo al texto esperado por el modelo local. */
data class FirestoreUserSubscription(
    var nameSuscription: String = "",
    var nextExpiration: String = "",
    var idSuscription: String = "",
    var price: Any? = null,
    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = false
) {
    /** Acepta texto, números y ausencia del precio; rechaza otros tipos para conservar la sesión previa. */
    fun toEntity(): UserSuscriptionData = UserSuscriptionData(
        nameSuscription = nameSuscription,
        nextExpiration = nextExpiration,
        idSuscription = idSuscription,
        price = when (val value = price) {
            null -> ""
            is String -> value
            is Number -> value.toString()
            else -> throw IllegalArgumentException("Tipo de precio de suscripción no compatible")
        },
        isActive = isActive
    )
}
