package com.nexusystem.paguito.integration.push

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/** Contenido externo de campañas: acepta HTTPS público, sin credenciales ni destinos locales. */
object CampaignNotificationContent {
    fun publicUrl(value: String?): String? = runCatching {
        val uri = URI(value ?: return null)
        val host = uri.host?.lowercase() ?: return null
        if (uri.scheme != "https" || uri.rawUserInfo != null || !host.contains('.') || host.contains(':') ||
            host.endsWith(".local") || host.endsWith(".internal") ||
            host.matches(Regex("[0-9.:]+")) || value.length > 2048
        ) return null
        uri.toASCIIString()
    }.getOrNull()

    /** Descarga acotada en el hilo del servicio FCM; la notificación funciona también sin imagen. */
    fun image(value: String?): Bitmap? = runCatching {
        var location = publicUrl(value) ?: return null
        repeat(3) {
            val connection = URL(location).openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = 4000
                connection.readTimeout = 4000
                connection.instanceFollowRedirects = false
                if (connection.responseCode in 300..399) {
                    location = publicUrl(URI(location).resolve(connection.getHeaderField("Location")).toString()) ?: return null
                } else {
                    if (connection.responseCode != 200 || !connection.contentType.orEmpty().startsWith("image/")) return null
                    val maximum = 5 * 1024 * 1024
                    if (connection.contentLengthLong > maximum) return null
                    val bytes = connection.inputStream.use { input ->
                        val output = java.io.ByteArrayOutputStream()
                        val buffer = ByteArray(8192)
                        while (true) {
                            val count = input.read(buffer)
                            if (count < 0) break
                            if (output.size() + count > maximum) return null
                            output.write(buffer, 0, count)
                        }
                        output.toByteArray()
                    }
                    if (bytes.size > maximum) return null
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)
                    if (bounds.outWidth <= 0 || bounds.outHeight <= 0 || bounds.outWidth.toLong() * bounds.outHeight > 16_000_000) return null
                    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            } finally { connection.disconnect() }
        }
        null
    }.getOrNull()
}
