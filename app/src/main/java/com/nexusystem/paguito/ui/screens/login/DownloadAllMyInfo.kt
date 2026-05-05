package com.nexusystem.paguito.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.nexusystem.paguito.R
import kotlinx.coroutines.delay

// Definición de colores de la interfaz (según la imagen)

val BackgroundColor = Color(0xFFF9FBFB)
val TextTitleColor = Color(0xFF111827)
val TextDescriptionColor = Color(0xFF6B7280)

@Composable
fun ProcessingScreen(
    email:String,
    viewModel: AuthViewModel,
    onContinueClick: () -> Unit,
    onBackClick: () -> Unit
) {
    // Estado para rastrear el paso actual (del 1 al 3)
    var currentStep by remember { mutableStateOf(1) }
    val allDataDownloaded by viewModel.allDataSucessfull.collectAsState()
    // Estado para controlar cuándo mostrar el botón "Continuar"
    var showContinueButton by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        viewModel.getAllMyData(email)
    }

    // Simulación de carga: Incrementa el paso cada 2 segundos
    LaunchedEffect(key1 = currentStep) {
        if (currentStep < 3) {
            delay(3000L) // Retraso de 2 segundos (2000ms) por paso
            currentStep += 1
        } else {
            // Cuando llega al paso 3, espera otros 2 segundos antes de mostrar el botón
            delay(3000L)
            if(allDataDownloaded)
                showContinueButton = true
        }
    }

    // Configuración de textos basados en el paso actual
    val (titleText, descriptionText) = when (currentStep) {
        1 -> "Descargando deudores" to "Validando información de contactos"
        2 -> "Descargando pagos" to "Conciliando transacciones recibidas"
        else -> "Descargando productos" to "Actualizando inventario y catálogo"
    }

    // Configuración del porcentaje de progreso
    val progressPercent = when (currentStep) {
        1 -> 0.33f
        2 -> 0.66f
        else -> 1f // 100%
    }
    val progressLabel = when (currentStep) {
        1 -> "33%"
        2 -> "66%"
        else -> "100%"
    }

    // Comienzo del layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Downloading,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Título de la Pantalla ---
        Text(
            text = "Actualizando tu Información",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextTitleColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // --- Descripción de la Pantalla ---
        Text(
            text = "Estamos preparando todo. Por favor,\nno cierres esta pantalla ni la\naplicación.",
            style = MaterialTheme.typography.bodyLarge,
            color = TextDescriptionColor,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // --- Sección de Animación Lottie ---
        // IMPORTANTE: Reemplaza "lottie_loading_documents" con tu archivo .json real en res/raw
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(
            R.raw.money // Placeholder temporal
        ))
        val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)

        Box(
            modifier = Modifier
                .size(200.dp)
                .background(Color.White.copy(alpha = 0.5f), shape = RoundedCornerShape(20.dp)), // Fondo borroso suave
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { progress },
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Tarjeta de Progreso (Progress Card) ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Título y Descripción del Paso Actual
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_input_get),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = titleText, fontWeight = FontWeight.Bold, color = TextTitleColor)
                        Text(text = descriptionText, color = TextDescriptionColor)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Barra de Progreso y Etiqueta
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { progressPercent },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = progressLabel,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.width(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Etiqueta del paso
                Text(text = "Paso $currentStep de 3", color = TextDescriptionColor, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(26.dp))

        // --- Botón de Continuar (Sólo visible en el paso 3) ---
        if (showContinueButton) {
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 26.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Continuar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

