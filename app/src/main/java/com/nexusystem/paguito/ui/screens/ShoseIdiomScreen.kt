// Path: com/beastspinning/medi/ui/screens/SelectLanguageScreen.kt
package com.nexusystem.paguito.ui.screens
import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.nexusystem.paguito.utils.findActivity
import com.nexusystem.paguito.utils.languajes
import  com.nexusystem.paguito.R
import com.nexusystem.paguito.utils.LanguageOption
import kotlin.collections.get
import kotlin.compareTo
import kotlin.text.equals

@Composable
fun SelectLanguageScreen(
    onContinueClick: () -> Unit = {},
    isOnboarding: Boolean,
    onBackClick: (() -> Unit)? = null,
) {
    val languages = remember {
        languajes
    }
    val activity = LocalContext.current.findActivity()
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    // ✅ guardas solo "es/en/pt" — dejamos tu lógica igual
    val lang = prefs.getString("app_language", "es") ?: "es"

    var selectedLanguage by remember {
        mutableStateOf(languages.firstOrNull { it.regionCode == lang } ?: languages.first())
    }

    val scrollState = rememberScrollState()

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val defaultBack: () -> Unit = {
        // Back normal (si estás en NavHost/Activity)
        backDispatcher?.onBackPressed()
            ?: (context as? Activity)?.onBackPressed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ✅ Top bar SOLO si viene desde Settings
            if (!isOnboarding) {

                val bg = MaterialTheme.colorScheme.background
                val onSurface = MaterialTheme.colorScheme.onSurface
                val circleBg = MaterialTheme.colorScheme.surface

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(circleBg)
                            .clickable { (onBackClick ?: defaultBack).invoke() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = "Back",
                            tint = onSurface,
                            modifier = Modifier.size(22.dp) // ArrowBackIosNew se ve mejor ~18-20dp
                        )
                    }

                    // Título centrado real ✅
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.shose_idiom),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = onSurface,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.width(44.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ✅ Solo descripción (sin repetir “Elige tu idioma” otra vez)
            Text(
                text = stringResource(id = R.string.shose_idiom_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Lista
            languages.forEach { option ->
                LanguageCard(
                    language = option,
                    isSelected = option == selectedLanguage,
                    onClick = {
                        selectedLanguage = option
                        onLanguageSelected(
                            activity = activity!!,
                            langCode = option.prefij,
                            countryCode = option.regionCode
                        ) // ✅ tu lógica igual
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(50.dp))
            // Botón fijo (solo onboarding)
            if (isOnboarding) {
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .height(55.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4DB5FF)
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.continue_label),
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height( 80.dp))
            }
        }


    }
}

fun onLanguageSelected(activity: Activity, langCode: String,countryCode: String) {
    val prefs = activity.getSharedPreferences("settings", Context.MODE_PRIVATE)
    Log.e("CODE_SAVED","3->"+countryCode)
    prefs.edit().putString("app_language", countryCode).apply()
    val lang = prefs.getString("app_language", "es") ?: "es"
    Log.e("CODE_SAVED","->"+lang)
    var code_languaje = languajes.filter { it.regionCode.equals(lang) }
    Log.e("CODE_SAVED","2->"+code_languaje)
    if(code_languaje.size>0) {
        val localeTag = "${code_languaje[0].prefij}-${code_languaje[0].regionCode}"
        setAppLanguage(localeTag)
        val lang = prefs.getString("app_language", "es") ?: "es"
        val mytoken = prefs.getString("MyToken", "")
        Log.e("CODE_SAVED","SEND TO FIREBASE->"+"$langCode-$countryCode")
    }
}

fun setAppLanguage(languageTag: String) {
    val localeList = LocaleListCompat.forLanguageTags(languageTag)
    AppCompatDelegate.setApplicationLocales(localeList)
}


@Composable
fun LanguageCard(
    language: LanguageOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF4DB5FF) else Color.LightGray

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = language.flagEmoji,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = language.name,
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

data class LanguageOption(
    val name: String,
    val flagEmoji: String,
    val prefij: String,     // "es" / "en" / "pt"
    val regionCode: String  // solo UI
)