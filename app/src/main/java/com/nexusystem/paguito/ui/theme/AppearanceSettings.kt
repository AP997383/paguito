package com.nexusystem.paguito.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class AccentColor(val label: String, val light: Color, val dark: Color) {
    Paguito("Paguito", PrimaryLight, PrimaryDark),
    Ocean("Océano", Color(0xFF2563EB), Color(0xFF9BBEFF)),
    Sky("Cielo", Color(0xFF087EA4), Color(0xFF82D7F5)),
    Mint("Menta", Color(0xFF087F75), Color(0xFF79DCCE)),
    Emerald("Esmeralda", Color(0xFF187347), Color(0xFF88D8AB)),
    Lime("Lima", Color(0xFF52751B), Color(0xFFB7D882)),
    Amber("Ámbar", Color(0xFF965600), Color(0xFFFFC36B)),
    Coral("Coral", Color(0xFFB84828), Color(0xFFFFB59C)),
    Sakura("Sakura", Color(0xFFB13578), Color(0xFFFFADCF)),
    Ruby("Rubí", Color(0xFFBA354A), Color(0xFFFFB2BF)),
    Violet("Violeta", Color(0xFF7952B3), Color(0xFFD2B7FF)),
    Indigo("Índigo", Color(0xFF5546B3), Color(0xFFC6BEFF))
}

enum class AppearanceMode(val label: String) { System("Automático"), Light("Claro"), Dark("Oscuro") }

data class AppearanceSettings(
    val accent: AccentColor = AccentColor.Paguito,
    val mode: AppearanceMode = AppearanceMode.System,
    val showWaves: Boolean = true
)

val LocalAppearance = staticCompositionLocalOf { AppearanceSettings() }

object AppearancePreferences {
    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun read(preferences: SharedPreferences): AppearanceSettings {
        val mode = preferences.getString("appearance_mode", null)?.let { saved ->
            AppearanceMode.entries.find { it.name == saved }
        } ?: if (preferences.contains("dark_mode")) {
            if (preferences.getBoolean("dark_mode", false)) AppearanceMode.Dark else AppearanceMode.Light
        } else AppearanceMode.System
        val accent = AccentColor.entries.find { it.name == preferences.getString("appearance_accent", null) } ?: AccentColor.Paguito
        return AppearanceSettings(accent, mode, preferences.getBoolean("appearance_waves", true))
    }

    fun setAccent(context: Context, accent: AccentColor) = prefs(context).edit().putString("appearance_accent", accent.name).apply()
    fun setMode(context: Context, mode: AppearanceMode) = prefs(context).edit().putString("appearance_mode", mode.name).apply()
    fun setWaves(context: Context, enabled: Boolean) = prefs(context).edit().putBoolean("appearance_waves", enabled).apply()
}

@Composable
fun rememberAppearanceSettings(): AppearanceSettings {
    val context = LocalContext.current.applicationContext
    val preferences = remember(context) { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    var settings by remember(preferences) { mutableStateOf(AppearancePreferences.read(preferences)) }
    DisposableEffect(preferences) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in setOf("appearance_accent", "appearance_mode", "appearance_waves", "dark_mode")) {
                settings = AppearancePreferences.read(preferences)
            }
        }
        preferences.registerOnSharedPreferenceChangeListener(listener)
        // Re-read after registration so a change between composition and effect is not missed.
        settings = AppearancePreferences.read(preferences)
        onDispose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
    }
    return settings
}
