package com.nexusystem.paguito.data.models.firestore

import com.google.firebase.firestore.util.CustomClassMapper
import com.google.gson.Gson
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import org.junit.Assert.*
import org.junit.Test

/** Regresiones con el mismo convertidor de Firestore utilizado por DocumentSnapshot.toObject, sin conexiones remotas. */
class FirestoreUserProfileTest {
    /** Deserializa un mapa con los tipos reales de Firestore antes de normalizar el perfil local. */
    private fun read(subscription: Map<String, Any?>): UserProfileEntity =
        CustomClassMapper.convertToCustomClass(
            mapOf("email" to "profile@example.invalid", "userSuscription" to subscription),
            FirestoreUserProfile::class.java,
            null
        ).toEntity()

    /** El caso del cierre: Long se convierte a texto y el campo isActive conserva el premium del servidor. */
    @Test fun longPricePreservesPremium() {
        val profile = read(mapOf("price" to 79L, "isActive" to true, "idSuscription" to "premium_monthly_basic"))
        assertEquals("79", profile.userSuscription.price)
        assertTrue(profile.userSuscription.isActive)
        assertEquals("premium_monthly_basic", profile.userSuscription.idSuscription)
    }

    /** Los importes decimales de Firestore también se aceptan sin truncar su parte fraccionaria. */
    @Test fun decimalPriceIsSupported() {
        assertEquals("79.5", read(mapOf("price" to 79.5)).userSuscription.price)
    }

    /** Los precios ya formateados conservan su texto, sin intentar interpretarlos como un número. */
    @Test fun formattedTextPriceIsPreserved() {
        assertEquals("\$79.00 MXN", read(mapOf("price" to "\$79.00 MXN")).userSuscription.price)
    }

    /** Un documento sin precio usa el valor vacío del contrato local. */
    @Test fun missingPriceIsSupported() {
        assertEquals("", read(emptyMap()).userSuscription.price)
    }

    /** Un precio explícitamente nulo tampoco causa un cierre durante la conversión. */
    @Test fun nullPriceIsSupported() {
        assertEquals("", read(mapOf("price" to null)).userSuscription.price)
    }

    /** Cero sigue siendo un precio válido; no otorga premium a una suscripción inactiva. */
    @Test fun zeroPriceDoesNotActivateSubscription() {
        val profile = read(mapOf("price" to 0L, "isActive" to false))
        assertEquals("0", profile.userSuscription.price)
        assertFalse(profile.userSuscription.isActive)
    }

    /** La adaptación conserva datos del perfil y las mayúsculas históricas de RemindersActive. */
    @Test fun profileFieldsAndReminderFlagsArePreserved() {
        val profile = CustomClassMapper.convertToCustomClass(
            mapOf(
                "email" to "profile@example.invalid", "fotoUrl" to "photo", "fullName" to "Perfil",
                "bussinesName" to "Negocio", "phone" to "123", "token" to "test-token",
                "watsapSended" to 3L, "watsapsSendedTotal" to 8L, "verified" to false,
                "RemindersActive" to true, "reminderPreventivo" to true,
                "reminderAtraso" to false, "reminderAvisoPago" to true
            ), FirestoreUserProfile::class.java, null
        ).toEntity()
        assertEquals("profile@example.invalid", profile.email)
        assertEquals("photo", profile.fotoUrl)
        assertEquals("Perfil", profile.fullName)
        assertEquals("Negocio", profile.bussinesName)
        assertEquals("123", profile.phone)
        assertEquals("test-token", profile.token)
        assertEquals(3, profile.watsapSended)
        assertEquals(8, profile.watsapsSendedTotal)
        assertFalse(profile.verified)
        assertTrue(profile.RemindersActive)
        assertTrue(profile.reminderPreventivo)
        assertFalse(profile.reminderAtraso)
        assertTrue(profile.reminderAvisoPago)
    }

    /** Otros tipos se rechazan para que el callback conserve el perfil previo en lugar de guardar datos inválidos. */
    @Test fun unexpectedPriceTypeIsRejected() {
        try {
            read(mapOf("price" to true))
            fail("Expected unsupported price type")
        } catch (expected: IllegalArgumentException) {
            assertEquals("Tipo de precio de suscripción no compatible", expected.message)
        }
    }

    /** La sesión cifrada sigue utilizando el mismo JSON y el precio de tipo String. */
    @Test fun normalizedProfileKeepsLocalJsonContract() {
        val profile = read(mapOf("price" to 79L, "isActive" to true))
        val gson = Gson()
        val json = gson.toJson(profile)
        assertTrue(json.contains("\"price\":\"79\""))
        assertEquals(profile, gson.fromJson(json, UserProfileEntity::class.java))
    }

    /** Comprueba que el documento que provocaba la excepción es resuelto por el contrato nuevo. */
    @Test fun reproducesOldDeserializerFailure() {
        val document = mapOf("userSuscription" to mapOf("price" to 79L))
        try {
            CustomClassMapper.convertToCustomClass(document, UserProfileEntity::class.java, null)
            fail("Expected original Long-to-String failure")
        } catch (expected: RuntimeException) {
            assertTrue(expected.message.orEmpty().contains("Long"))
            assertTrue(expected.message.orEmpty().contains("String"))
        }
        assertEquals("79", CustomClassMapper.convertToCustomClass(document, FirestoreUserProfile::class.java, null).toEntity().userSuscription.price)
    }
}
