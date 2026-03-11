package com.nexusystem.paguito.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.hardware.camera2.CameraManager
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.google.type.Date
import com.nexusystem.paguito.R
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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