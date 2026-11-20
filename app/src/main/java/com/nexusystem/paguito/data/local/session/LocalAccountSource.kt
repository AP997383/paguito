package com.nexusystem.paguito.data.local.session

import java.security.MessageDigest
import java.util.Locale
import kotlinx.coroutines.flow.Flow

/** Identidad local observable; cambia al iniciar/cerrar sesión sin compartir datos entre perfiles. */
interface LocalAccountSource {
    val accountKeys: Flow<String>
    fun currentAccountKey(): String
}

/** Claves locales estables sin guardar correos en claro; un invitado nunca equivale a una cuenta. */
object LocalAccountKey {
    const val GUEST = "guest"

    /** Normaliza solo la identidad, nunca nombres, precios ni el contenido de los productos. */
    fun fromEmail(email: String?): String {
        val normalized = email.orEmpty().trim().lowercase(Locale.ROOT)
        if (normalized.isEmpty()) return GUEST
        return "account:" + MessageDigest.getInstance("SHA-256")
            .digest(normalized.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
    }
}
