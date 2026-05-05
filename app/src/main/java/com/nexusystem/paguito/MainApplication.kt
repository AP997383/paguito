package com.nexusystem.paguito

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }

   /* override fun attachBaseContext(base: Context) {
        Log.e("APP_LOCALE", "MainApplication attachBaseContext")
        val prefs = base.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_language", "es") ?: "es"

        val option = languajes.firstOrNull {
            it.regionCode == lang
        }

        val localeTag = option?.let {
            "${it.prefij}-${it.regionCode}"
        } ?: "es-MX"
        setAppLanguage(localeTag)
        super.attachBaseContext(base)
    }
*/
}