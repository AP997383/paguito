package com.nexusystem.paguito.data.sync

import com.google.gson.Gson
import com.google.firebase.firestore.util.CustomClassMapper
import com.nexusystem.paguito.data.models.responses.GetAllMyDataResponse
import com.nexus.medi.data.local.entity.PagosEntinty
import org.junit.Assert.*
import org.junit.Test

/** Contratos reales del servicio: no usa Firebase, cuentas ni datos de teléfonos personales. */
class AccountSnapshotValidationTest {
    private fun read(json: String) = Gson().fromJson(json, GetAllMyDataResponse::class.java)

    @Test fun sameMovementIdInPaymentAndSalePreservesBothDecimalsAndMethod() {
        val response = read("""{"productos":[],"deudores":[{"idRemoteDatabase":"client","montoActualAdeudado":-3330,"pagos":[{"idRemoteDatabase":"same","montoAbonado":"15.75","saldoAntesDeAbono":-20.5,"tipoPago":2}],"ventas":[{"idRemoteDatabase":"same","montoAbonado":9.5}]}]}""")
        AccountSnapshotValidation.validate(response)
        assertEquals(-3330f, response.deudores.single().montoActualAdeudado)
        val payment = response.deudores.single().pagos.single()
        assertEquals(15.75, payment.montoAbonado, 0.0)
        assertEquals(-20.5, payment.saldoAntesDeAbono, 0.0)
        assertEquals(2, payment.tipoPago)
    }
    @Test fun retiredClientsNeverBecomeActiveAndRetainTheirHistoricalFields() {
        val response = read("""{"productos":[],"deudores":[{"idRemoteDatabase":"active"},{"idRemoteDatabase":"retired","isDeleted":true,"montoActualAdeudado":-5,"saldoAFavor":68}]}""")
        AccountSnapshotValidation.validate(response)
        assertEquals(1, response.deudores.count { !it.isDeleted })
        assertEquals(-5f, response.deudores.last().montoActualAdeudado)
        assertEquals(68f, response.deudores.last().creditBalance)
    }
    @Test fun duplicateAndEmptyIdentitiesRejectTheEntireSnapshot() {
        for (json in listOf(
            """{"productos":[{"idRemoteDatabase":"p"},{"idRemoteDatabase":"p"}],"deudores":[]}""",
            """{"productos":[],"deudores":[{"idRemoteDatabase":"c"},{"idRemoteDatabase":"c"}]}""",
            """{"productos":[],"deudores":[{"idRemoteDatabase":"c","pagos":[{"idRemoteDatabase":""}]}]}""")) {
            assertThrows(IllegalArgumentException::class.java) { AccountSnapshotValidation.validate(read(json)) }
        }
    }
    @Test fun localAccountAndSyncFlagsAreExcludedFromFirestore() {
        val payment = PagosEntinty(ownerAccount = "private-local-key", isSynced = true, montoAbonado = 15.75)
        val encoded = CustomClassMapper.convertToPlainJavaTypes(payment) as Map<*, *>
        assertFalse(encoded.containsKey("ownerAccount"))
        assertFalse(encoded.containsKey("isSynced"))
        assertEquals(15.75, encoded["montoAbonado"])
    }
}
