package com.nexusystem.paguito.integration.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.nexusystem.paguito.BuildConfig
import java.security.MessageDigest

/** Embudo de membresías sin correos, recibos ni tokens de compra en Analytics. */
object MembershipAnalytics {
    /** El mismo identificador seudónimo permite medir usuarios entre Android e iOS. */
    fun accountKey(email: String): String = MessageDigest.getInstance("SHA-256")
        .digest(email.trim().lowercase(java.util.Locale.ROOT).toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    /** Almacena únicamente UUID aleatorios emitidos por el servidor; nunca convierte el correo en User-ID. */
    fun cacheIdentity(context: Context, email: String, identifier: String) {
        if (runCatching { java.util.UUID.fromString(identifier) }.isFailure) return
        context.getSharedPreferences("membership_analytics", Context.MODE_PRIVATE).edit().putString(accountKey(email), identifier).apply()
        FirebaseAnalytics.getInstance(context).setUserId(identifier)
    }

    /** Cada apertura cuenta una visita; las compras reales se contabilizan en el registro del servidor. */
    fun event(context: Context, name: String, email: String?, productId: String? = null, result: String? = null) {
        val analytics = FirebaseAnalytics.getInstance(context)
        // No utiliza correos ni hashes de correos como identificadores de Analytics.
        val identifier = email?.let { context.getSharedPreferences("membership_analytics", Context.MODE_PRIVATE).getString(accountKey(it), null) }
        analytics.setUserId(identifier)
        analytics.logEvent(name, Bundle().apply {
            putString("platform", "android"); putString("environment", BuildConfig.FLAVOR)
            productId?.let { putString("product_id", it) }; result?.let { putString("result", it) }
        })
    }
}
