package com.nexusystem.paguito.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.type.Date
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.nexusystem.paguito.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.apply
import kotlin.collections.filter
import kotlin.collections.first
import kotlin.collections.forEach
import kotlin.getOrDefault
import kotlin.io.writeBytes
import kotlin.jvm.java
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random
import kotlin.ranges.coerceAtLeast
import kotlin.ranges.contains
import kotlin.text.lowercase
import kotlin.text.orEmpty
import kotlin.text.padStart
import kotlin.text.replace
import kotlin.text.replaceFirstChar
import kotlin.text.uppercase
import kotlin.toString

val dashPath = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
@Composable
fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}
@RequiresApi(Build.VERSION_CODES.O)
fun getDelayMinutes(dateSaved: String): Long {
    val reminderDateTime = LocalDateTime.parse(dateSaved)
    val now = LocalDateTime.now()

    return ChronoUnit.MINUTES.between(reminderDateTime, now)
        .coerceAtLeast(0) // evita negativos
}


fun Modifier.dashedBorder(
    color: Color,
    cornerRadiusDp: Dp,
    strokeWidth: Dp = 2.dp,
    intervals: FloatArray = floatArrayOf(12f, 12f)
): Modifier {
    return this.drawWithContent {
        drawContent()

        drawRoundRect(
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(intervals)
            ),
            cornerRadius = CornerRadius(cornerRadiusDp.toPx())
        )
    }
}

fun getAppVersionName(context: Context): String {
    return try {
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            0
        )
        packageInfo.versionName ?: "N/A"
    } catch (e: Exception) {
        "N/A"
    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun DayOfWeek.toSundayIndex(): Int {
    // ISO: Monday=1 ... Sunday=7
    // Queremos: Sunday=0, Monday=1 ... Saturday=6
    return if (this == DayOfWeek.SUNDAY) 0 else this.value
}

fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
fun Activity.restartApp() {
    val intent = Intent(this, this::class.java)
    finish()
    startActivity(intent)
}


@RequiresApi(Build.VERSION_CODES.O)
fun YearMonth.formatMonthYearSpanish(): String {
    val monthName = this.month.getDisplayName(
        TextStyle.FULL,
        Locale("es", "ES")
    )
    return "${monthName.replaceFirstChar { it.uppercase() }} ${this.year}"
}

val Context.onboardingDataStore by preferencesDataStore("onboarding_prefs")

fun padZero(value: Int): String {
    return value.toString().padStart(2, '0')
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatToAmPm(time24: String): String {
    try {
        val time = LocalTime.parse(time24) // interpreta "08:00" o "20:00"
        val formatter = DateTimeFormatter.ofPattern("hh:mm a") // formato AM/PM
        return time.format(formatter)
    }catch (e: Exception){
        // 1. Creamos un parser flexible para la entrada (acepta 10:0 o 10:00)
        val inputFormatter = DateTimeFormatterBuilder()
            .appendPattern("H:m") // 'H' es hora 0-23, 'm' es minuto 0-59 (un solo dígito)
            .toFormatter()

        // 2. Parseamos el texto "10:0" a LocalTime
        val time = LocalTime.parse(time24, inputFormatter)

        // 3. Definimos el formato de salida (10:00 AM)
        // Usamos 'hh' para 01-12, 'mm' para dos dígitos en minutos, 'a' para AM/PM
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a")

        return time.format(outputFormatter)
    }
}
object OnboardingStore {
    private val KEY_SEEN = booleanPreferencesKey("onboarding_seen")

    suspend fun setOnboardingSeen(context: Context) {
        context.onboardingDataStore.edit { prefs ->
            prefs[KEY_SEEN] = true
        }
    }

    suspend fun hasSeenOnboarding(context: Context): Boolean {
        return context.onboardingDataStore.data
            .map { it[KEY_SEEN] ?: false }
            .first()
    }
}

fun turnFlashOn(context: Context) {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList.first() // cámara trasera
    cameraManager.setTorchMode(cameraId, true)
}

fun turnFlashOff(context: Context) {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList.first()
    cameraManager.setTorchMode(cameraId, false)
}

@RequiresApi(Build.VERSION_CODES.O)
fun parseExpireDate(date: String): LocalDate? {
    return runCatching {
        LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }.getOrNull()
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatFullDate(
    date: LocalDate,
    locale: Locale = Locale.getDefault()
): String {
    val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", locale)
    return date.format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun getExpirationText(
    expireDate: LocalDate,
    today: LocalDate = LocalDate.now()
): String {
    val days = ChronoUnit.DAYS.between(today, expireDate)

    return when {
        days > 1 -> "Faltan $days días para que caduque"
        days == 1L -> "Falta 1 día para que caduque"
        days == 0L -> "Caduca hoy"
        days == -1L -> "Caducó ayer"
        else -> "Caducó hace ${abs(days)} días"
    }
}

fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
    }
    context.startActivity(intent)
}

@Composable
fun NotificationPermissionDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🔔 Icono (opcional)
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 📝 TÍTULO
                Text(
                    text = "Permiso de notificaciones requerido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 📄 DESCRIPCIÓN
                Text(
                    text = "Necesitamos el permiso de notificaciones para recordarte tomar tus medicamentos a tiempo. Sin este permiso, no podremos enviarte recordatorios.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 🔘 BOTONES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onGoToSettings
                    ) {
                        Text("Ir a ajustes")
                    }
                }
            }
        }
    }
}

@Composable
fun ExactAlarmPermissionDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ⏰ Icono
                // 🎞️ LOTTIE




                Spacer(modifier = Modifier.height(-40.dp))

                // 📝 TÍTULO
                Text(
                    text = "Permiso de alarmas exactas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 📄 DESCRIPCIÓN
                Text(
                    text = "Para ofrecerte recordatorios precisos y confiables, necesitamos habilitar el permiso de alarmas exactas. Esto permite que la app te avise justo a tiempo, incluso con el teléfono en reposo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 🔘 BOTONES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onGoToSettings
                    ) {
                        Text("Configurar")
                    }
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun getMinutesFromNow(reminderDateTime: String): Long {
    val reminderTime = LocalDateTime.parse(reminderDateTime)
    val now = LocalDateTime.now()
    return ChronoUnit.MINUTES.between(reminderTime, now)
}

@Composable
fun MediaPermissionDialog(
    onDismiss: () -> Unit,
    onGoToSettings: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🖼️ Icono
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 📝 TÍTULO
                Text(
                    text = "Permiso de fotos y videos requerido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 📄 DESCRIPCIÓN
                Text(
                    text = "Necesitamos acceso a tus fotos y videos para que puedas adjuntar imágenes, guardar archivos o escanear documentos. Sin este permiso, algunas funciones no estarán disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 🔘 BOTONES
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = onDismiss
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = onGoToSettings
                    ) {
                        Text("Ir a ajustes")
                    }
                }
            }
        }
    }
}


@Composable
fun rememberHaptic() =
    LocalHapticFeedback.current

fun timeGradient(): Brush =
    Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1565C0), // azul
            Color(0xFF42A5F5),
            Color(0xFFFF7043), // rojo
            Color(0xFFD32F2F)
        )
    )

fun curveOffset(distance: Float): Float {
    return sin(distance) * 40f
}

fun getVersionCode(context: Context): Long {
    return try {
        val pInfo = context.packageManager.getPackageInfo(
            context.packageName,
            0
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pInfo.longVersionCode
        } else {
            @Suppress("DEPRECATION")
            pInfo.versionCode.toLong()
        }
    } catch (e: Exception) {
        0L
    }
}

@Composable
fun SuccessPopup(
    show: Boolean,
    onDismiss: () -> Unit
) {
    if (!show) return

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2ECC71),
                    modifier = Modifier.size(40.dp)
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text = "Actualizado correctamente",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun List<LocalDateTime>.onlyRemainingToday(): List<LocalDateTime> {
    val now = LocalDateTime.now()
    val endOfDay = LocalDate.now().atTime(23, 59, 59)

    return this.filter { dateTime ->
        dateTime.isAfter(now) && dateTime.isBefore(endOfDay)
    }
}

fun openAppSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

@Composable
fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )

    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )

    return this.background(brush)
}



suspend fun downloadMediPdf(
    contextDir: File, // context.cacheDir o context.filesDir
    template: String, // "glucose" | "bloodPressure" | "bloodPressurePremium"
    data: JSONObject, // { nombre, registros:[...] }
    lang: String = "es",
    filename: String = "Medi-Reporte.pdf"
): File {
    // 1. Configurar un cliente con timeouts más largos
    val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS) // Tiempo para conectar al servidor
        .writeTimeout(60, TimeUnit.SECONDS)   // Tiempo para enviar el JSON
        .readTimeout(60, TimeUnit.SECONDS)    // Tiempo para recibir el PDF generado
        .build()

    val url = "https://us-central1-medi-bbd18.cloudfunctions.net/httpGeneratePdf"

    val bodyJson = JSONObject().apply {
        put("template", template)
        put("lang", lang)
        put("filename", filename)
        put("data", data)
    }

    val request = Request.Builder()
        .url(url)
        .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
        .build()

    val response = client.newCall(request).execute()
    if (!response.isSuccessful) {
        Log.e("RESPONSEEEEE","-->"+response.toString())
        val err = response.body?.string().orEmpty()
        Log.e("RESPONSEEEEE","-->"+err)
        throw kotlin.RuntimeException("HTTP ${response.code}: $err")
    }else{
        Log.e("RESPONSEEEEE","si funcionoooooo-->")
    }

    val bytes = response.body?.bytes() ?: throw kotlin.RuntimeException("PDF vacío")

    val outFile = File(contextDir, filename)
    outFile.writeBytes(bytes)
    Log.e("RESPONSEEEEE","si funcionoooooo-->"+outFile)

    return outFile
}

@RequiresApi(Build.VERSION_CODES.O)
fun getFormattedDate(): String {
    // Definimos el formato: Mes abreviado (MMM), día (dd), año (yyyy)
    val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.US)
    return LocalDate.now().format(formatter).uppercase()
}

fun formatTicketNumber(number: Int): String {
    // Convierte el número a string y rellena con ceros hasta alcanzar 6 dígitos
    // '0' es el carácter de relleno
    return number.toString().padStart(6, '0')
}

// Función auxiliar para formatear el mes
@RequiresApi(Build.VERSION_CODES.O)
fun obtenerNombreMes(fecha: LocalDate): String {
    val nombre = fecha.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
    return nombre.lowercase().replaceFirstChar { it.uppercase() }
}



@RequiresApi(Build.VERSION_CODES.O)
fun formatDateToCharts(value: String): String{
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val outputFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("es", "ES"))
    val fechaFormateada = try {
        LocalDateTime.parse(value, inputFormatter).format(outputFormatter).replace(".", "")
    } catch (e: Exception) { "" }
    return fechaFormateada
}

@RequiresApi(Build.VERSION_CODES.O)
fun formatDateToChartsHours(value: String): String{
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale("es", "ES"))
    val fechaFormateada = try {
        LocalDateTime.parse(value, inputFormatter).format(outputFormatter).replace(".", "")
    } catch (e: Exception) { "" }
    return fechaFormateada
}



object PaguitoStore {
    private val KEY_SEEN = booleanPreferencesKey("onboarding_seen")
    private val IS_LOGGED = booleanPreferencesKey("is_logged")
    private val IS_INVITED= booleanPreferencesKey("is_invited")
    private val TYPE_USER = intPreferencesKey("type_user")
    private val MY_PROFILE = stringPreferencesKey("my_profile")

    suspend fun setOnboardingSeen(context: Context) {
        context.onboardingDataStore.edit { prefs ->
            prefs[KEY_SEEN] = true
        }
    }

    suspend fun hasSeenOnboarding(context: Context): Boolean {
        return context.onboardingDataStore.data
            .map { it[KEY_SEEN] ?: false }
            .first()
    }

    suspend fun setInvited(context: Context) {
        context.onboardingDataStore.edit { prefs ->
            prefs[IS_INVITED] = true
        }
    }

    suspend fun setInvitedNot(context: Context) {
        context.onboardingDataStore.edit { prefs ->
            prefs[IS_INVITED] = false
        }
    }

    suspend fun isInvited(context: Context): Boolean {
        return context.onboardingDataStore.data
            .map { it[IS_INVITED] ?: false }
            .first()
    }

    suspend fun setLoged(context: Context) {
        context.onboardingDataStore.edit { prefs ->
            prefs[IS_LOGGED] = true
        }
    }
    suspend fun setLogout(context: Context) {
        context.onboardingDataStore.edit { prefs ->
            prefs[IS_LOGGED] = false
        }
    }

    suspend fun isLoged(context: Context): Boolean {
        return context.onboardingDataStore.data
            .map { it[IS_LOGGED] ?: false }
            .first()
    }

    suspend fun setMyProfile(context: Context,type: String) {
        context.onboardingDataStore.edit { prefs ->
            prefs[MY_PROFILE] = type
        }
    }

    fun getMyProfile(context: Context): Flow<String> =
        context.onboardingDataStore.data
            .map { it[MY_PROFILE] ?: "" }

    suspend fun setTypeUser(context: Context,type:Int) {
        context.onboardingDataStore.edit { prefs ->
            prefs[TYPE_USER] = type
        }
    }

    fun getTypeUser(context: Context): Flow<Int> =
        context.onboardingDataStore.data
            .map { it[TYPE_USER] ?: 3 }


}

/**
 * Convierte un Number (Int, Float, Double) a un String formateado como moneda.
 * Ejemplo: 1500.5 -> $1,500.50
 */
fun getThumbnailUrl(originalUrl: String): String {
    // Si la URL ya tiene el sufijo, devolvemos la original como fallback
    if (originalUrl.contains("_200x200.jpg")) return originalUrl

    // Insertamos el sufijo antes de la extensión (o al final si no tiene)
    return originalUrl.replace(".jpg", "_200x200.jpg")
}

fun formatAsCurrency(amount: Any?): String {
    return try {
        // Convertimos a Double para que el formateador lo acepte sin importar si es Int o Float
        val value = when (amount) {
            is Number -> amount.toDouble()
            is String -> amount.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }

        // Usamos el Locale de México para asegurar el formato $X,XXX.XX
        val format = NumberFormat.getCurrencyInstance(Locale("es", "MX"))
        format.format(value)
    } catch (e: Exception) {
        "$0.00" // Fallback por si llega algo raro
    }
}

/**
 * Convierte de "2026-04-03 20:00" a "Viernes 3 de abril del 2026 a las 8:00 pm"
 */
@RequiresApi(Build.VERSION_CODES.O)
fun formatLongDateTime(inputDate: String?): String {
    if (inputDate.isNullOrBlank()) return ""

    return try {
        // 1. Definimos el formato de entrada
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateTime = LocalDateTime.parse(inputDate, inputFormatter)

        // 2. Definimos el formato de salida deseado
        // 'eeee' = Nombre del día completo, 'd' = día, 'MMMM' = mes completo, 'yyyy' = año
        // 'hh:mm a' = hora 12h con am/pm
        val outputFormatter = DateTimeFormatter.ofPattern(
            "eeee d 'de' MMMM 'del' yyyy",
            Locale("es", "MX")
        )

        // 3. Formateamos y ajustamos el am/pm a minúsculas para que se vea más natural
        dateTime.format(outputFormatter).lowercase(Locale("es", "MX"))
            .replaceFirstChar { it.uppercase() } // Capitalizar la primera letra (el día)

    } catch (e: Exception) {
        inputDate // Si falla, devolvemos el string original para no romper la UI
    }
}
@RequiresApi(Build.VERSION_CODES.O)
fun getTodayDateString(): String {
    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    return today.format(formatter)
}
// Modifier personalizado para dibujar bordes discontinuos (dashed)
fun Modifier.dashedBorder(
    width: Dp,
    radius: Dp,
    color: Color,
    dashLength: Dp = 8.dp,
    gapLength: Dp = 6.dp
) = drawWithContent {
    drawContent() // Dibuja el contenido normal primero

    // Creamos el Path Effect discontinuo
    val pathEffect = PathEffect.dashPathEffect(
        intervals = floatArrayOf(dashLength.toPx(), gapLength.toPx()),
        phase = 0f
    )

    // Dibujamos el rectángulo redondeado con el efecto
    drawPath(
        path = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = size.toRect(),
                    cornerRadius = CornerRadius(radius.toPx())
                )
            )
        },
        color = color,
        style = Stroke(
            width = width.toPx(),
            pathEffect = pathEffect
        )
    )
}

@RequiresApi(Build.VERSION_CODES.O)
fun getDaysUntilNextPayment(startDateStr: String, periodicity: String): Long {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val today = LocalDate.now()
    var nextPaymentDate = LocalDate.parse(startDateStr, formatter)

    // Si la fecha de inicio es hoy o en el pasado, calculamos la siguiente ocurrencia
    // hacia el futuro basándonos en la periodicidad.
    while (nextPaymentDate.isBefore(today) || nextPaymentDate.isEqual(today)) {
        nextPaymentDate = when (periodicity) {
            "S" -> nextPaymentDate.plusWeeks(1)
            "Q" -> nextPaymentDate.plusDays(15)
            "M" -> nextPaymentDate.plusMonths(1)
            "A" -> return 0 // Fecha abierta: no hay "próxima fecha" automática
            else -> nextPaymentDate // Evitar bucle infinito si la periodicidad es inválida
        }

        // Safety break por si la periodicidad no es válida
        if (periodicity !in listOf("S", "Q", "M")) break
    }

    // Calculamos la diferencia en días
    return ChronoUnit.DAYS.between(today, nextPaymentDate)
}

fun generarReferenciaUnica(prefijo: String = "PAG-"): String {
    // Formato: AñoMesDiaHoraMinutoSegundo (ej: 2603281305)
    val timestamp = SimpleDateFormat("yyMMddHHmm", Locale.getDefault()).format(java.util.Date())

    // Generar 3 caracteres aleatorios (letras mayúsculas y números)
    val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    val randomPart = (1..3)
        .map { caracteres[Random.nextInt(caracteres.length)] }
        .joinToString("")

    return "$prefijo$timestamp-$randomPart"
}

fun generarCodigoBarras(text: String, width: Int, height: Int): android.graphics.Bitmap? {
    return try {
        val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.CODE_128, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.TRANSPARENT)
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

val languajes  =  listOf(
    // ===== PRIORIDAD =====
    LanguageOption("English (United States)", "🇺🇸", "en", "US"),
    LanguageOption("Español (México)", "🇲🇽", "es", "MX"),
    LanguageOption("English (Canada)", "🇨🇦", "en", "CA"),

    // ===== RESTO (ORDENADO) =====
    LanguageOption("Español (España)", "🇪🇸", "es", "ES"),
    LanguageOption("Español (Argentina)", "🇦🇷", "es", "AR"),
    LanguageOption("Español (Chile)", "🇨🇱", "es", "CL"),
    LanguageOption("Español (Colombia)", "🇨🇴", "es", "CO"),
    LanguageOption("Español (Perú)", "🇵🇪", "es", "PE"),
    LanguageOption("Español (Ecuador)", "🇪🇨", "es", "EC"),
    LanguageOption("Español (Costa Rica)", "🇨🇷", "es", "CR"),
    LanguageOption("Español (Guatemala)", "🇬🇹", "es", "GT"),
    LanguageOption("Español (Honduras)", "🇭🇳", "es", "HN"),
    LanguageOption("Español (El Salvador)", "🇸🇻", "es", "SV"),
    LanguageOption("Español (Panamá)", "🇵🇦", "es", "PA"),
    LanguageOption("Español (Paraguay)", "🇵🇾", "es", "PY"),
    LanguageOption("Español (Uruguay)", "🇺🇾", "es", "UY"),
    LanguageOption("Español (Bolivia)", "🇧🇴", "es", "BO"),
    LanguageOption("Español (República Dominicana)", "🇩🇴", "es", "DO"),

    // Portugués
    LanguageOption("Português (Brasil)", "🇧🇷", "pt", "BR")
)
data class LanguageOption(
    val name: String,
    val flagEmoji: String,
    val prefij: String,     // "es" / "en" / "pt"
    val regionCode: String  // solo UI
)



@RequiresApi(Build.VERSION_CODES.O)
fun calcularSiguienteVencimiento(
    fechaInicio: LocalDate,       // 28-01-2026
    periodicidad: String,        // "Semanal", "Quincenal", "Mensual"
    historialPagos: List<LocalDate>
): LocalDate {
    val hoy = LocalDate.now()
    var fechaVencimientoActual = fechaInicio

    // Iteramos mientras la fecha de vencimiento sea anterior o igual a hoy
    // para encontrar cuál es el pago que toca "ahora" o el que está pendiente.
    while (fechaVencimientoActual.isBefore(hoy) || fechaVencimientoActual.isEqual(hoy)) {

        // Definimos el rango de búsqueda: desde el vencimiento anterior hasta el actual
        // Si es quincenal, buscamos si hubo un pago en los últimos 15 días antes de esta fecha.
        val diasAtras = cuandoRestar(periodicidad)
        val inicioPeriodo = fechaVencimientoActual.minusDays(diasAtras)

        // ¿Existe un pago realizado para cubrir este vencimiento específico?
        val pagoRealizado = historialPagos.any { it.isAfter(inicioPeriodo) && (it.isBefore(fechaVencimientoActual) || it.isEqual(fechaVencimientoActual)) }

        if (pagoRealizado) {
            // Si ya pagó, el siguiente vencimiento es el que sigue según la periodicidad
            fechaVencimientoActual = cuandoEsLaSiguiente(fechaVencimientoActual, periodicidad)
        } else {
            // Si NO hay pago en ese rango, este es el vencimiento que debe mostrarse (está pendiente)
            break
        }
    }

    return fechaVencimientoActual
}

fun cuandoRestar(periodicidad: String): Long {
    return when (periodicidad.lowercase()) {
        "S" -> 7L
        "Q" -> 15L
        "M" -> 30L // Aproximado para la ventana de búsqueda
        else -> 30L
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun cuandoEsLaSiguiente(fecha: LocalDate, periodicidad: String): LocalDate {
    return when (periodicidad.uppercase()) { // Usamos uppercase por consistencia
        "S" -> fecha.plusDays(7)
        "Q" -> fecha.plusDays(15)
        "M" -> fecha.plusMonths(1) // ¡Debe ser 1 para que avance al siguiente mes!
        else -> fecha.plusDays(15)  // Un fallback seguro que SIEMPRE avance
    }
}