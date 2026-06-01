package com.nexusystem.paguito.ui.screens.registro

import android.util.Log
import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock

import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.domain.data.auth.UserDataModelAuth
import com.nexusystem.paguito.ui.screens.deudores.BorderColor
import com.nexusystem.paguito.utils.LoadingOverlay
import com.nexusystem.paguito.R
import com.nexusystem.paguito.utils.dialogs.ErrorAlertDialog

// --- COLORES ---
val GreenPrimary = Color( 0xFF15956F)
val BgColor = Color(0xFFFFFFFF)
val InputBgColor = Color(0xFFF9FAFB)
val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF6B7280)
val TextLightGray = Color(0xFF9CA3AF)

// Colores de fuerza de contraseña
val StrengthWeak = Color(0xFFEF4444)   // Rojo
val StrengthFair = Color(0xFFF59E0B)   // Amarillo/Naranja
val StrengthGood = Color(0xFF3B82F6)   // Azul
val StrengthStrong =  Color( 0xFF15956F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBackClick: () -> Unit = {},
    goToOtp: (UserDataModelAuth) -> Unit = {},
    onLoginClick: () -> Unit = {},
    registerwModel: RegisterViewModel
) {
    // Estados de los campos
    val isLoading by registerwModel.isLoading.collectAsState()
    var name by remember { mutableStateOf("") }
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    val successLogin by registerwModel.loginSuccessFull.collectAsState()
    var DATAUSER by remember { mutableStateOf(UserDataModelAuth()) }
    // Estados de visibilidad de contraseña
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // --- LÓGICA DE SEGURIDAD DE CONTRASEÑA ---
    val hasUppercase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }
    val hasMinLength = password.length >= 8

    val conditionsMetCount = listOf(hasUppercase, hasNumber, hasSpecial, hasMinLength).count { it }

    // Determinar color y progreso de la barra
    val strengthProgress by animateFloatAsState(targetValue = conditionsMetCount / 4f, label = "strength")
    val strengthColor = when (conditionsMetCount) {
        0 -> Color.Transparent
        1 -> StrengthWeak
        2 -> StrengthFair
        3 -> StrengthGood
        else -> StrengthStrong
    }


    val otpstatus by registerwModel.otpSuseccfull.collectAsState()
    if(otpstatus.equals("TRUE")){
        goToOtp(DATAUSER)
    }else if(otpstatus.equals("FAIL")){
        ErrorAlertDialog(
            "El OTP es incorrecto,intentalo de nuevo.",
            onDismissRequest = { registerwModel.resetOtpSucessfull() }, // Función para resetear el Flow a ""
            onConfirmClick = { registerwModel.resetOtpSucessfull() }
        )
    }
    // --- LÓGICA DE VALIDACIÓN DEL FORMULARIO ---
    var showError by remember { mutableStateOf(false) }
    val isIdentifierValid = Patterns.EMAIL_ADDRESS.matcher(identifier).matches() || (identifier.all { it.isDigit() } && identifier.length >= 10)
    val isFormValid = name.isNotBlank() && isIdentifierValid && conditionsMetCount == 4 && password == confirmPassword && termsAccepted
    Log.e("TERMS","->"+ name +"/"+isIdentifierValid+"/"+conditionsMetCount+"/"+(password == confirmPassword)+"/"+termsAccepted)
    Box(){
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Atrás", tint = TextDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgColor),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = BgColor
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            // Títulos
            Text("Crear Cuenta", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Únete en Paguito y comienza a tomar el control de tus finanzas hoy mismo.", fontSize = 14.sp, color = TextGray)

            Spacer(modifier = Modifier.height(32.dp))

            // --- 1. FORMULARIO BÁSICO ---
            CustomFieldLabel("Nombre Completo")
            CustomAuthTextField(value = name, onValueChange = { name = it }, placeholder = "Ej. Juan Pérez", icon = Icons.Default.PersonOutline)
            Spacer(modifier = Modifier.height(16.dp))

            CustomFieldLabel("Correo electrónico")
            CustomAuthTextField(
                value = identifier, onValueChange = { identifier = it },
                placeholder = "juan@ejemplo.com ", icon = Icons.Default.MailOutline,
                keyboardType = KeyboardType.Email
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- 2. CONTRASEÑA Y SEGURIDAD ---
            CustomFieldLabel("Crear Contraseña")
            CustomAuthTextField(
                value = password, onValueChange = { password = it },
                placeholder = "Mínimo 8 caracteres", icon = Icons.Default.Lock,
                isPassword = true, passwordVisible = passwordVisible,
                onPasswordVisibilityChange = { passwordVisible = it }
            )

            // Panel de Seguridad (Se despliega solo cuando empiezan a escribir)
            AnimatedVisibility(visible = password.isNotEmpty()) {
                PasswordStrengthPanel(
                    progress = strengthProgress,
                    color = strengthColor,
                    conditionsMetCount = conditionsMetCount,
                    hasUppercase = hasUppercase,
                    hasNumber = hasNumber,
                    hasSpecial = hasSpecial,
                    hasMinLength = hasMinLength
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirmar Contraseña (Solo aparece si la primera ya cumple los requisitos)
            AnimatedVisibility(visible = conditionsMetCount == 4) {
                Column {
                    CustomFieldLabel("Confirmar Contraseña")
                    CustomAuthTextField(
                        value = confirmPassword, onValueChange = { confirmPassword = it },
                        placeholder = "Repite tu contraseña", icon = Icons.Default.Lock,
                        isPassword = true, passwordVisible = confirmPasswordVisible,
                        onPasswordVisibilityChange = { confirmPasswordVisible = it }
                    )
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text("Las contraseñas no coinciden", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // --- 3. TÉRMINOS Y CONDICIONES ---
            TermsAndConditionsSection({
                termsAccepted=it
            })

            Log.e("TERMS","->"+ isFormValid +"/"+showError)
            if (showError && !isFormValid) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Por favor, completa correctamente todos los campos y acepta los términos.", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. BOTONES INFERIORES ---
            Button(
                onClick = {
                    if (isFormValid) {
                        DATAUSER = UserDataModelAuth(identifier, password, name,identifier )
                        registerwModel.sendOtp(identifier)
                    } else showError = true
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor =  MaterialTheme.colorScheme.primary, disabledContainerColor = GreenPrimary.copy(alpha = 0.5f)),
                enabled = true // Lo dejamos enabled siempre para mostrar el error si hacen clic incompleto, o ponlo en 'isFormValid' si prefieres que se bloquee.
            ) {
                Text("Registrarse", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("¿Ya tienes cuenta? ", color = TextGray, fontSize = 13.sp)
                Text(
                    "Inicia Sesión", color =  MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

    }
        LoadingOverlay(
            isLoading = isLoading,
            lottieRes = R.raw.loadings
        )
    }
}
@Composable
fun TermsAndConditionsSection(termsAccepted:(Boolean)->Unit) {
    // 1. Instanciamos el manejador de URIs de Compose
    val uriHandler = LocalUriHandler.current
    var termsAcceptedLocal by remember { mutableStateOf(false) }
    // URLs de tus documentos
    val urlTerminos = "https://www.nexusecosystem-mx.com/sections/terms-paguito.html"
    val urlPrivacidad = "https://www.nexusecosystem-mx.com/sections/terms-paguito.html"
// Guardamos la referencia del layout del texto para saber exactamente dónde se hace click
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    // Volvemos al esquema clásico con TAGS que funciona en todas las versiones
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = Color(0xFF64748B), fontSize = 12.sp)) {
            append("Al registrarme, acepto los ")
        }

        pushStringAnnotation(tag = "URL_TERMS", annotation = urlTerminos)
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )) {
            append("Términos de Servicio")
        }
        pop()

        withStyle(style = SpanStyle(color = Color(0xFF64748B), fontSize = 12.sp)) {
            append(" y la ")
        }

        pushStringAnnotation(tag = "URL_PRIVACY", annotation = urlPrivacidad)
        withStyle(style = SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )) {
            append("Política de Privacidad.")
        }
        pop()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = termsAcceptedLocal,
            onCheckedChange = {
                termsAcceptedLocal =it
                termsAccepted(it) },
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = annotatedString,
            onTextLayout = { textLayoutResult = it },
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures { offset ->
                    textLayoutResult?.let { layoutResult ->
                        // Calculamos qué caracter de la cadena recibió el toque
                        val position = layoutResult.getOffsetForPosition(offset)

                        // Buscamos si ese caracter pertenece a la anotación de Términos
                        annotatedString.getStringAnnotations(tag = "URL_TERMS", start = position, end = position)
                            .firstOrNull()?.let { annotation ->
                                uriHandler.openUri(annotation.item)
                            }

                        // Buscamos si ese caracter pertenece a la anotación de Privacidad
                        annotatedString.getStringAnnotations(tag = "URL_PRIVACY", start = position, end = position)
                            .firstOrNull()?.let { annotation ->
                                uriHandler.openUri(annotation.item)
                            }
                    }
                }
            }
        )
    }
}
// ================= COMPONENTES SECUNDARIOS =================

@Composable
fun PasswordStrengthPanel(
    progress: Float, color: Color, conditionsMetCount: Int,
    hasUppercase: Boolean, hasNumber: Boolean, hasSpecial: Boolean, hasMinLength: Boolean
) {
    Surface(
        color = InputBgColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SEGURIDAD DE LA CONTRASEÑA", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLightGray, letterSpacing = 1.sp)
                Text("$conditionsMetCount/4", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextLightGray)
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Barra de progreso animada
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = color,
                trackColor = BorderColor,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Requisitos
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    ConditionItem("1 Mayúscula", hasUppercase)
                    Spacer(modifier = Modifier.height(8.dp))
                    ConditionItem("1 Carácter especial", hasSpecial)
                }
                Column(modifier = Modifier.weight(1f)) {
                    ConditionItem("1 Número", hasNumber)
                    Spacer(modifier = Modifier.height(8.dp))
                    ConditionItem("8+ Caracteres", hasMinLength)
                }
            }
        }
    }
}

@Composable
fun ConditionItem(text: String, isMet: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (isMet) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isMet)  MaterialTheme.colorScheme.primary else TextLightGray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = if (isMet) TextDark else TextLightGray)
    }
}

@Composable
fun CustomFieldLabel(text: String) {
    Text(text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextDark, modifier = Modifier.padding(bottom = 6.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAuthTextField(
    value: String, onValueChange: (String) -> Unit, placeholder: String, icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false, passwordVisible: Boolean = false, onPasswordVisibilityChange: (Boolean) -> Unit = {},
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        placeholder = { Text(placeholder, color = TextLightGray, fontSize = 14.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = TextLightGray, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (isPassword) {
                IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                    Icon(if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, contentDescription = null, tint = TextLightGray, modifier = Modifier.size(20.dp))
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color(0xFFE5E7EB), focusedBorderColor =  MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = Color.White, focusedContainerColor = Color.White,
            focusedTextColor = TextDark, unfocusedTextColor = TextDark
        )
    )
}