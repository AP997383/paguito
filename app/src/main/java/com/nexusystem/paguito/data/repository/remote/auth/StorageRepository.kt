package com.nexusystem.paguito.data.repository.remote.auth

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class StorageRepository @Inject constructor() {
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    suspend fun uploadProfileImage(userId: String, imageUri: Uri): String? {
        return try {
            Log.e("IMAGEN_PRODUCTO","SI 3")
            // Referencia: perfil_usuarios/user123.jpg
            val fileRef = storageRef.child("perfil_usuarios/$userId.jpg")

            // Subir archivo
            fileRef.putFile(imageUri).await()

            // Obtener la URL de descarga para guardarla en Firestore
            val downloadUrl = fileRef.downloadUrl.await()
            Log.e("IMAGEN_PRODUCTO","SI"+downloadUrl.toString())
            downloadUrl.toString()

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun uploadProductImage(userId: String, imageUri: Uri): String? {
        return try {
            // 1. Generar el nombre basado en fecha, minuto y segundo
            // Formato: 20260327_232504 (AñoMesDía_HoraMinutoSegundo)
            Log.e("IMAGEN_PRODUCTO","SI 4"+userId +"/"+imageUri)
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "prod_$timestamp.jpg"

            // 2. Referencia: productos/user123/prod_20260327_232504.jpg
            val fileRef = storageRef.child("productos/$userId/$fileName")

            // Subir archivo
            fileRef.putFile(imageUri).await()

            // Obtener la URL de descarga
            val downloadUrl = fileRef.downloadUrl.await()
            downloadUrl.toString()
        } catch (e: Exception) {
            Log.e("IMAGEN_PRODUCTO","SI 4"+e.toString())
            e.printStackTrace()
            null
        }
    }
}