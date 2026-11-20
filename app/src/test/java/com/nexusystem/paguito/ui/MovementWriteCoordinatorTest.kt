package com.nexusystem.paguito.ui

import com.nexusystem.paguito.domain.usescases.abonos.persistMovementOnce
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class MovementWriteCoordinatorTest {
    @Test fun networkFailureStoresOnePendingRecord() = runBlocking {
        var writes = 0
        persistMovementOnce(remoteWrite = { throw IllegalStateException("offline") }, localWrite = { synced ->
            writes++; assertFalse(synced)
        })
        assertEquals(1, writes)
    }
    @Test fun localFailureNeverRetriesOrReportsSuccess() = runBlocking {
        var writes = 0; var success = false
        try {
            persistMovementOnce(remoteWrite = { true }, localWrite = { writes++; throw IllegalStateException("storage") })
            success = true
        } catch (_: IllegalStateException) { }
        assertFalse(success)
        assertEquals(1, writes)
    }
    @Test fun cancellationDoesNotFallBackToALocalWrite() = runBlocking {
        var writes = 0
        try {
            persistMovementOnce(remoteWrite = { throw CancellationException() }, localWrite = { writes++ })
            fail("Cancellation must propagate")
        } catch (_: CancellationException) { }
        assertEquals(0, writes)
    }
    @Test fun remoteConfirmationIsPreservedInTheLocalRecord() = runBlocking {
        var confirmed = false
        persistMovementOnce(remoteWrite = { true }, localWrite = { confirmed = it })
        assertTrue(confirmed)
    }
}
