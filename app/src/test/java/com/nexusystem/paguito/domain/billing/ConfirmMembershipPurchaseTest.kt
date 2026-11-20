package com.nexusystem.paguito.domain.billing

import com.nexusystem.paguito.domain.usescases.billing.confirmMembershipPurchase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Compra simulada: nunca se conecta a Play Billing ni realiza cargos. */
class ConfirmMembershipPurchaseTest {
    @Test fun validatesAndGrantsBeforeAcknowledging() = runBlocking {
        val calls = mutableListOf<String>()
        confirmMembershipPurchase("a", { "a" }, { calls.add("validate"); true }, { calls.add("benefits"); true }, { calls.add("acknowledge"); true })
        assertEquals(listOf("validate", "benefits", "acknowledge"), calls)
    }
    @Test fun serverFailureNeverAcknowledgesTheChargeAsDelivered() = runBlocking {
        var acknowledged = false
        runCatching { confirmMembershipPurchase("a", {"a"}, {false}, {true}, {acknowledged = true; true}) }
        assertFalse(acknowledged)
    }
    @Test fun switchingAccountsDuringValidationNeverRefreshesOrAcknowledges() = runBlocking {
        var owner = "a"; var refreshed = false; var acknowledged = false
        runCatching { confirmMembershipPurchase("a", {owner}, {owner="b"; true}, {refreshed=true; true}, {acknowledged=true; true}) }
        assertFalse(refreshed); assertFalse(acknowledged)
    }
    @Test fun switchingAccountsWhileRefreshingNeverAcknowledges() = runBlocking {
        var owner = "a"; var acknowledged = false
        runCatching { confirmMembershipPurchase("a", {owner}, {true}, {owner="b"; true}, {acknowledged=true; true}) }
        assertFalse(acknowledged)
    }
    @Test fun missingBenefitsNeverAcknowledges() = runBlocking {
        var acknowledged=false
        runCatching { confirmMembershipPurchase("a", {"a"}, {true}, {false}, {acknowledged=true;true}) }
        assertFalse(acknowledged)
    }
}
