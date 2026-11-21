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
import com.nexusystem.paguito.integration.push.CampaignNotificationContent

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putString("MyToken", token).apply()
        com.nexusystem.paguito.integration.push.PushDeviceRegistrar.get(applicationContext).sync()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data["type"] == "NEXUS_PUSH_DEVICE_CHALLENGE") {
            com.nexusystem.paguito.integration.push.PushDeviceRegistrar.get(applicationContext).receiveChallenge(remoteMessage.data)
            return
        }
        // Los desafíos de registro nunca se convierten en alertas visibles.
        val title = remoteMessage.notification?.title ?: "Nueva notificación"
        val body = remoteMessage.notification?.body ?: ""

        showNotification(title, body, remoteMessage.data["linkURL"], remoteMessage.notification?.imageUrl?.toString() ?: remoteMessage.data["imageURL"])
    }

    private fun showNotification(title: String, body: String, link: String?, imageURL: String?) {
        val notificationId = Random.nextInt()
        val channelId = "DEFAULT_CHANNEL"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 1. Crear el canal para Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Notificaciones de Paguito",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Intent para abrir la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("type", "NEXUS_CAMPAIGN")
            CampaignNotificationContent.publicUrl(link)?.let { putExtra("linkURL", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Construir la notificación
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.notification) // Usa tu icono (.png) aquí
            .setColor(0xFF15956F.toInt())
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        CampaignNotificationContent.image(imageURL)?.let { image ->
            builder.setLargeIcon(image).setStyle(NotificationCompat.BigPictureStyle().bigPicture(image))
        }
        notificationManager.notify(notificationId, builder.build())
    }
}