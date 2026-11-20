package com.nexusystem.paguito.domain.policy

import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.local.entity.UserSuscriptionData
import org.junit.Assert.*
import org.junit.Test

/** Una edición iniciada sin Premium no puede borrar beneficios recibidos mientras se guardaba. */
class ProfileEditPolicyTest {
    @Test fun membershipAndCountersSurviveAnOldProfileEdit() {
        val subscription = UserSuscriptionData(isActive = true, price = "79", nextExpiration = "2026-06-15")
        val current = UserProfileEntity(email = "buyer@example.com", userSuscription = subscription, watsapSended = 9)
        val edited = UserProfileEntity(email = "buyer@example.com", fullName = "Nuevo nombre")
        val result = mergeEditableProfile(current, edited)
        assertEquals("Nuevo nombre", result.fullName); assertEquals(subscription, result.userSuscription); assertEquals(9, result.watsapSended)
    }
    @Test(expected = IllegalArgumentException::class) fun anotherAccountCannotOverwriteTheCurrentProfile() {
        mergeEditableProfile(UserProfileEntity(email = "a@example.com"), UserProfileEntity(email = "b@example.com"))
    }
}
