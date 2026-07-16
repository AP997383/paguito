package com.nexusystem.paguito

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Aquí debes enviar el nuevo token a tu servidor Node.js
        // para actualizarlo en Firestore
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Obtenemos los datos de la notificación
        val title = remoteMessage.notification?.title ?: "Nueva notificación"
        val body = remoteMessage.notification?.body ?: ""

        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val channelId = "default_notification_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Crear el canal para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones generales",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Intent para abrir la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Construir la notificación
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification) // Usa tu icono (.png) aquí
            .setColor(0xFF15956F.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        notificationManager.notify(Random.nextInt(), builder.build())
    }
}