package com.nexusystem.paguito.integration.push

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.utils.SecureStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit

/** Registro por instalación: nunca utiliza correos, contraseñas ni identificadores de hardware. */
class PushDeviceRegistrar private constructor(context: Context) {
    private val app = context.applicationContext
    private val storage = SecureStorageManager(app)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Any()
    private var busy = false
    private var lastSignature = ""
    private var lastReported = 0L
    private val settings = app.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val preferenceListener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == "app_language") sync()
    }

    init { settings.registerOnSharedPreferenceChangeListener(preferenceListener) }

    /** Reutiliza la credencial cifrada; obtiene una prueba silenciosa de FCM solo al registrar por primera vez. */
    fun sync() {
        synchronized(lock) { if (busy) return; busy = true }
        scope.launch {
            try {
                val token = Tasks.await(FirebaseMessaging.getInstance().token, 15, TimeUnit.SECONDS)
                if (token.isNullOrBlank()) return@launch
                val installation = synchronized(lock) {
                    storage.pushString("installation").ifEmpty { UUID.randomUUID().toString().also { storage.savePushStrings(mapOf("installation" to it)) } }
                }
                val environment = BuildConfig.ENVIRONMENT.lowercase(java.util.Locale.ROOT)
                val locale = settings.getString("app_language", "es").orEmpty().substringBefore('-').substringBefore('_').let { if (it in listOf("es", "en", "pt")) it else "es" }
                val metadata = JSONObject().put("app", "paguito").put("environment", environment)
                    .put("installationId", installation).put("platform", "android").put("token", token)
                    .put("locale", locale).put("appVersion", BuildConfig.VERSION_NAME)
                    .put("deviceModel", "${Build.MANUFACTURER} ${Build.MODEL}")
                    .put("permissionGranted", NotificationManagerCompat.from(app).areNotificationsEnabled())
                val signature = MessageDigest.getInstance("SHA-256").digest(metadata.toString().toByteArray()).joinToString("") { "%02x".format(it) }
                val credential = storage.pushString("credential")
                if (credential.isNotEmpty() && signature == lastSignature && System.currentTimeMillis() - lastReported < 1_800_000) return@launch
                if (credential.isNotEmpty()) {
                    metadata.put("action", "update").put("deviceId", storage.pushString("deviceId")).put("credential", credential)
                    try { post(metadata) } catch (error: PushHttpError) {
                        if (error.status == 401) storage.savePushStrings(mapOf("credential" to "", "deviceId" to "", "pendingUntil" to "0"))
                        throw error
                    }
                    lastSignature = signature; lastReported = System.currentTimeMillis()
                } else {
                    val pendingUntil = storage.pushString("pendingUntil").toLongOrNull() ?: 0L
                    if (pendingUntil > System.currentTimeMillis()) return@launch
                    val request = UUID.randomUUID().toString()
                    storage.savePushStrings(mapOf("pendingRequest" to request, "pendingUntil" to (System.currentTimeMillis() + 65_000).toString()))
                    metadata.put("action", "begin").put("requestId", request)
                    post(metadata)
                }
            } catch (_: Exception) {
                if (BuildConfig.DEBUG) android.util.Log.d("PUSH_REGISTRATION", "Registro pendiente; se reintentará al volver a la app")
            } finally { synchronized(lock) { busy = false } }
        }
    }

    /** El desafío se consume sin crear una notificación visible y solo para la instalación que lo solicitó. */
    fun receiveChallenge(data: Map<String, String>) {
        if (data["app"] != "paguito" || data["environment"] != BuildConfig.ENVIRONMENT.lowercase(java.util.Locale.ROOT)) return
        if (data["installationId"] != storage.pushString("installation") || data["requestId"] != storage.pushString("pendingRequest")) return
        scope.launch {
            try {
                val request = JSONObject().put("action", "complete").put("app", "paguito")
                    .put("environment", BuildConfig.ENVIRONMENT.lowercase(java.util.Locale.ROOT))
                    .put("installationId", storage.pushString("installation")).put("requestId", data["requestId"])
                    .put("challengeId", data["challengeId"]).put("nonce", data["nonce"])
                val response = post(request)
                val credential = response.optString("credential")
                val id = response.optString("deviceId")
                if (credential.isNotEmpty() && id.isNotEmpty()) {
                    storage.savePushStrings(mapOf("credential" to credential, "deviceId" to id, "pendingRequest" to "", "pendingUntil" to "0"))
                    lastSignature = ""; sync()
                }
            } catch (_: Exception) {
                if (BuildConfig.DEBUG) android.util.Log.d("PUSH_REGISTRATION", "Verificación silenciosa pendiente")
            }
        }
    }

    /** Transporte propio sin interceptores que puedan registrar la credencial del dispositivo. */
    private fun post(body: JSONObject): JSONObject {
        val connection = URL("https://pushdeviceregistration-${BuildConfig.CLOUD_RUN_HASH}-uc.a.run.app").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"; connection.connectTimeout = 15_000; connection.readTimeout = 15_000
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8"); connection.doOutput = true
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            if (status !in 200..299) throw PushHttpError(status)
            return JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
        } finally { connection.disconnect() }
    }

    private class PushHttpError(val status: Int) : Exception("push_http_$status")

    companion object {
        @Volatile private var instance: PushDeviceRegistrar? = null
        /** Una sola cola por proceso evita registros simultáneos de Activity y Firebase Messaging. */
        fun get(context: Context): PushDeviceRegistrar = instance ?: synchronized(this) {
            instance ?: PushDeviceRegistrar(context).also { instance = it }
        }
    }
}
