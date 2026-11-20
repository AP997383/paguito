package com.nexusystem.paguito.utils

import android.content.Context
import com.nexusystem.paguito.data.local.session.LocalAccountKey
import com.nexusystem.paguito.data.local.session.LocalAccountSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.nexusystem.paguito.data.local.entity.UserProfileEntity

/** Perfil cifrado y notificación compartida entre las instancias usadas por Compose e inyección. */
class SecureStorageManager(context: Context) : LocalAccountSource {
    private companion object { val accountChanges = MutableStateFlow(0L) }
    val profileChanges: Flow<UserProfileEntity?> = accountChanges.map { getUserProfile() }
    override val accountKeys: Flow<String> = accountChanges.map { currentAccountKey() }.distinctUntilChanged()
    override fun currentAccountKey(): String = LocalAccountKey.fromEmail(getUserProfile()?.email)

    /** Actualiza datos del perfil únicamente si todavía pertenece a la sesión activa. */
    fun updateCurrentUserProfile(user: UserProfileEntity): Boolean = synchronized(accountChanges) {
        if (LocalAccountKey.fromEmail(user.email) != currentAccountKey()) return false
        saveUserProfile(user)
        return true
    }

    private val gson = Gson()

    // Configuración de la llave maestra para el cifrado
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_user_prefs", // Nombre del archivo
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Guardar el perfil completo
    fun saveUserProfile(user: UserProfileEntity) = synchronized(accountChanges) {
        val json = gson.toJson(user)
        if (sharedPreferences.getString("user_profile", null) == json) return@synchronized
        sharedPreferences.edit().putString("user_profile", json).apply()
        accountChanges.update { it + 1 }
    }

    // Obtener el perfil completo
    fun getUserProfile(): UserProfileEntity? {
        val json = sharedPreferences.getString("user_profile", null)
        return if (json != null) {
            gson.fromJson(json, UserProfileEntity::class.java)
        } else null
    }

    // Limpiar datos (útil para Logout)
    fun clearUserData() = synchronized(accountChanges) {
        sharedPreferences.edit().clear().apply()
        accountChanges.update { it + 1 }
    }
}