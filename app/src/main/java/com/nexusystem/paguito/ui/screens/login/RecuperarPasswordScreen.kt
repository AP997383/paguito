package com.nexusystem.paguito.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.R
import com.nexusystem.paguito.utils.LoadingOverlay

// --- COLORES ---
val BgLightGreen = Color(0xFFE8F5E9) // Fondo verde muy claro para el ícono
val BorderColor: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit = {},
    goToOtp: (String) -> Unit = {},
    viewModel:AuthViewModel
) {
    // Estado del campo de texto
    var identifier by remember { mutableStateOf("") }
    val otpSucess by viewModel.otpSuseccfull.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    LaunchedEffect(otpSucess) { if (otpSucess == "TRUE") goToOtp(identifier) }
    com.nexusystem.paguito.ui.components.RecoveryContent(identifier, { identifier = it }, isLoading,
        onBackClick, { viewModel.sendOtp(identifier.trim()) })
}
