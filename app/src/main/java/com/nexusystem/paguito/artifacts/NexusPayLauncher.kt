// Path:
// app/src/main/java/com/nexusystem/paguito/artifacts/NexusPayLauncher.kt

package com.nexusystem.paguito.artifacts

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.nexusecosystem.nexuspayment.NexusPayActivity

object NexusPayLauncher {

    fun open(
        context: Context,
        email: String,
        fcmToken: String,
        isPremium: Boolean
    ) {
        val cleanEmail = email.trim()
        val cleanFcmToken = fcmToken.trim()

        if (cleanEmail.isBlank()) {
            Toast.makeText(
                context,
                "No encontramos el correo electrónico.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val intent = Intent(
            context,
            NexusPayActivity::class.java
        ).apply {

            putExtra(
                NexusPayActivity.EXTRA_APP_ID,
                "paguito"
            )

            // Temporal mientras se elimina la dependencia del businessId
            putExtra(
                NexusPayActivity.EXTRA_BUSINESS_ID,
                "temp_business_id"
            )

            putExtra(
                NexusPayActivity.EXTRA_EMAIL,
                cleanEmail
            )

            putExtra(
                NexusPayActivity.EXTRA_FCM_TOKEN,
                cleanFcmToken
            )

            putExtra(
                NexusPayActivity.EXTRA_CALLBACK_SCHEME,
                "nexuspay"
            )

            putExtra(
                NexusPayActivity.EXTRA_IS_PREMIUM,
                isPremium
            )

            putExtra(
                NexusPayActivity.EXTRA_NEXUS_COMMISSION_DESCRIPTION,
                if (isPremium) {
                    "Tu plan Premium no agrega comisión adicional de Nexus."
                } else {
                    "Nexus puede aplicar una comisión adicional según tu plan."
                }
            )
        }

        context.startActivity(intent)
    }
}