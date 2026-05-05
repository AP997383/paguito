package com.nexusystem.paguito.utils

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.gson.Gson
import com.nexusystem.paguito.data.local.entity.UserProfileEntity

class SecureStorageManager(context: Context) {
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
    fun saveUserProfile(user: UserProfileEntity) {
        Log.e("SAVE_STORAGE","--<"+user)
        val json = gson.toJson(user)
        sharedPreferences.edit().putString("user_profile", json).apply()
    }

    // Obtener el perfil completo
    fun getUserProfile(): UserProfileEntity? {
        val json = sharedPreferences.getString("user_profile", null)
        return if (json != null) {
            gson.fromJson(json, UserProfileEntity::class.java)
        } else null
    }

    // Limpiar datos (útil para Logout)
    fun clearUserData() {
        sharedPreferences.edit().clear().apply()
    }
}