package com.nexusystem.paguito.ui.screens.login

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.R
import com.nexusystem.paguito.domain.data.auth.UserDataModelAuth
import com.nexusystem.paguito.ui.screens.login.AuthViewModel
import com.nexusystem.paguito.ui.screens.registro.BgColor
import com.nexusystem.paguito.ui.screens.registro.GreenPrimary
import com.nexusystem.paguito.ui.screens.registro.TextDark
import com.nexusystem.paguito.ui.screens.registro.TextGray
import com.nexusystem.paguito.ui.screens.registro.TextLightGray
import com.nexusystem.paguito.utils.LoadingOverlay
import kotlinx.coroutines.delay

// --- COLORES ---



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpRecoveryScreen(
    email: String,
    goToHome: (String) -> Unit,
    onResend: () -> Unit,
    onBack: () -> Unit,
    authViewModel: AuthViewModel
) {
    // Estado del código OTP
    var otpValue by remember { mutableStateOf("") }
    val otpLength = 6

    // Estado del temporizador
    var timeLeft by remember { mutableStateOf(30) }

    // Focus requester para abrir el teclado automáticamente
    val focusRequester = remember { FocusRequester() }
    val isLoading by authViewModel.isLoading.collectAsState()
    val isVerified by authViewModel.isVerified.collectAsState()
    // Temporizador
    LaunchedEffect(key1 = timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    if (isVerified) {
        goToHome(email)
        authViewModel.resetVerified()
    }


    // Abrir teclado al iniciar
    LaunchedEffect(Unit) {
        delay(300) // Pequeño delay para que la pantalla termine de cargar
        focusRequester.requestFocus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Atrás", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = BgColor
    ) { paddingValues ->
        Box{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // 1. Icono de Escudo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(BgLightGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Shield, contentDescription = null, tint =  MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Textos
            Text("Recupera tu contraseña", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ingresa el código de 6 dígitos\nenviado a tu correo.",
                fontSize = 14.sp,
                color = TextGray,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // 3. Cajas del OTP (Implementación Profesional)
            BasicTextField(
                value = otpValue,
                onValueChange = { newValue ->
                    // Solo permitimos números y un máximo de 4 caracteres
                    if (newValue.length <= otpLength && newValue.all { it.isDigit() }) {
                        otpValue = newValue
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                modifier = Modifier.focusRequester(focusRequester),
                decorationBox = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Dibujamos 4 casillas visuales
                        repeat(otpLength) { index ->
                            val char = otpValue.getOrNull(index)?.toString() ?: ""
                            val isFocused = otpValue.length == index

                            OtpCharBox(
                                char = char,
                                isFocused = isFocused
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            // 4. Botón Verificar
            val isButtonEnabled = otpValue.length == otpLength
            Button(
                onClick = { authViewModel.verifyOtp(email.replace("\"", "").trim(),otpValue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isButtonEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor =  MaterialTheme.colorScheme.primary,
                    disabledContainerColor =  MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) // Botón atenuado si no está lleno
                )
            ) {
                Text("Verificar", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Temporizador / Reenviar
            Row(
                modifier = Modifier.padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("¿No recibiste el código? ", color = TextLightGray, fontSize = 13.sp)
                if (timeLeft > 0) {
                    Text(
                        text = "Reenviar en 00:${timeLeft.toString().padStart(2, '0')}",
                        color =  MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Text(
                        text = "Reenviar ahora",
                        color =  MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            authViewModel.sendOtp(email.replace("\"", "").trim())
                            // Acción para reenviar código
                            timeLeft = 30 // Reiniciar temporizador
                            otpValue = "" // Limpiar cajas
                        }
                    )
                }
            }
        }
        LoadingOverlay(
            isLoading = isLoading,
            lottieRes = R.raw.loadings
        )
    }
    }
}

// Sub-componente que dibuja cada casillita individual
@Composable
fun RowScope.OtpCharBox(char: String, isFocused: Boolean) {
    val isFilled = char.isNotEmpty()

    // Si tiene texto, borde verde y fondo verdecito. Si está enfocada, borde verde. Si no, borde gris.
    val borderColor = if (isFilled || isFocused)  MaterialTheme.colorScheme.primary else BorderColor
    val backgroundColor = if (isFilled) BgLightGreen else Color.White
    val textColor = if (isFilled)  MaterialTheme.colorScheme.primary else TextDark

    Box(
        modifier = Modifier
            .weight(1f) // Esto hace que se distribuyan uniformemente
            .aspectRatio(0.85f) // Mantiene proporción rectangular (más alto que ancho)
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(BorderStroke(if (isFocused) 2.dp else 1.dp, borderColor), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (isFilled) {
            Text(
                text = char,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        } else if (isFocused) {
            // Simulamos un cursor parpadeante (opcional, aquí solo estático)
            Box(
                modifier = Modifier
                    .height(24.dp)
                    .width(2.dp)
                    .background( MaterialTheme.colorScheme.primary)
            )
        }
    }
}