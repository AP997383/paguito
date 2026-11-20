package com.nexusystem.paguito.domain.usescases.abonos

import kotlinx.coroutines.CancellationException

/**
 * Confirma una operación solo después de una escritura local, sin reintentarla en el catch de red.
 * Un fallo remoto conserva el registro pendiente; un fallo local se propaga al formulario.
 * La cancelación nunca se convierte en un guardado exitoso.
 */
internal suspend fun persistMovementOnce(remoteWrite: suspend () -> Boolean, localWrite: suspend (Boolean) -> Unit) {
    val remoteConfirmed = try { remoteWrite() }
    catch (cancelled: CancellationException) { throw cancelled }
    catch (_: Exception) { false }
    localWrite(remoteConfirmed)
}
