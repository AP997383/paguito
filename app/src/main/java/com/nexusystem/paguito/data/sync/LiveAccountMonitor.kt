package com.nexusystem.paguito.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.nexusystem.paguito.data.local.session.LocalAccountSource
import com.nexusystem.paguito.data.local.session.LocalAccountKey
import com.nexusystem.paguito.utils.SecureStorageManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import javax.inject.Singleton

/** Mantiene la caché de la cuenta visible al día sin sondeos ni escrituras de retorno al servidor. */
@Singleton
class LiveAccountMonitor @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val accounts: LocalAccountSource,
    private val storage: SecureStorageManager,
    private val sync: AccountSyncCoordinator,
) {
    private var session: Job? = null
    private var fingerprintOwner = ""
    private val fingerprints = mutableMapOf<String, Map<String, Map<String, Any>>>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    /** Cada cambio de sesión retira las escuchas anteriores antes de abrir las nuevas. */
    fun start() {
        if (session?.isActive == true) return
        session = scope.launch {
            accounts.accountKeys.collectLatest { owner ->
                val email = storage.getUserProfile()?.email.orEmpty().trim().lowercase(java.util.Locale.ROOT)
                if (owner == LocalAccountKey.GUEST || owner != LocalAccountKey.fromEmail(email)) return@collectLatest
                val listeners = mutableListOf<ListenerRegistration>()
                if (fingerprintOwner != owner) { fingerprints.clear(); fingerprintOwner = owner }
                var refresh: Job? = null
                var forcePending = false
                try {
                    for (collection in listOf("deudores", "productos")) {
                        listeners += firestore.collection(collection).document(email).collection(collection)
                            .addSnapshotListener { snapshot, error ->
                                if (error != null || snapshot == null || snapshot.metadata.hasPendingWrites() || accounts.currentAccountKey() != owner) return@addSnapshotListener
                                val fingerprint = snapshot.documents.associate { it.id to it.data.orEmpty() }
                                val previous = fingerprints.put(collection, fingerprint)
                                if (previous == fingerprint) return@addSnapshotListener
                                forcePending = forcePending || previous != null
                                refresh?.cancel()
                                refresh = launch {
                                    delay(650)
                                    val force = forcePending
                                    forcePending = false
                                    try { sync.refresh(email, force) }
                                    catch (cancelled: CancellationException) { throw cancelled }
                                    catch (failure: Exception) { if (com.nexusystem.paguito.BuildConfig.DEBUG) android.util.Log.w("ACCOUNT_SYNC", "No se pudo recibir la actualización: ${failure.javaClass.simpleName}") }
                                }
                            }
                    }
                    listeners += firestore.collection("users").document(email).addSnapshotListener { snapshot, error ->
                        if (error == null && snapshot != null && !snapshot.metadata.hasPendingWrites() && accounts.currentAccountKey() == owner) {
                            runCatching { snapshot.toObject(com.nexusystem.paguito.data.models.firestore.FirestoreUserProfile::class.java)?.toEntity() }
                                .getOrNull()?.let { profile -> if (LocalAccountKey.fromEmail(profile.email) == owner) storage.updateCurrentUserProfile(profile) }
                        }
                    }
                    awaitCancellation()
                } finally { refresh?.cancel(); listeners.forEach { it.remove() } }
            }
        }
    }

    /** Detiene la recepción en segundo plano sin borrar los datos guardados. */
    fun stop() { session?.cancel(); session = null }
}
