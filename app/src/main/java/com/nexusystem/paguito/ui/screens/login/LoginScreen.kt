package com.nexusystem.paguito.ui.screens.login

import android.content.Context
import android.util.Log
import android.util.Patterns
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.R
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.data.local.entity.UserSuscriptionData
import com.nexusystem.paguito.data.models.responses.LoginResponse
import com.nexusystem.paguito.utils.LoadingOverlay
import com.nexusystem.paguito.utils.SecureStorageManager
import com.nexusystem.paguito.utils.dialogs.ErrorAlertDialog


// --- COLORES ---
val GreenPrimary =Color( 0xFF15956F)


val TextDark: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
val TextGray: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val TextLightGray: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant

@Composable
fun LoginScreen(
    onLoginSuccess: (LoginResponse) -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onGuestClick: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    viewModel: AuthViewModel
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val secureStorage = SecureStorageManager(context)
    var identifier by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val successLogin by viewModel.loginSuccessFull.collectAsState()
    // Estados de errores
    var identifierError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    if (successLogin != null) {
        val profileToSave = UserProfileEntity(
            verified = successLogin!!.verified,
            email =successLogin!!.email?:"" ,
            fotoUrl = successLogin!!.fotoUrl ?: "",
            fullName = successLogin!!.fullName?:"",
            bussinesName = successLogin!!.bussinesName?:"",
            phone = successLogin!!.phone?:"",
            watsapSended = successLogin!!.watsapSended?:0,
            watsapsSendedTotal = successLogin!!.watsapsSendedTotal?:0,
            token =prefs.getString("MyToken","").toString(),
            userSuscription = successLogin!!.userSuscription?: UserSuscriptionData()
        )
        secureStorage.saveUserProfile(profileToSave)
        onLoginSuccess(successLogin!!)
        viewModel.resetLogin()
    }
    val errorMessage by viewModel.errorMessage.collectAsState()

// Ejemplo de uso con el diálogo que creamos:
    if (errorMessage.isNotEmpty()) {
        ErrorAlertDialog(
            "Correo o contraseña no son correctos intentalo de nuevo.",
            onDismissRequest = { viewModel.clearError() }, // Función para resetear el Flow a ""
            onConfirmClick = { viewModel.clearError() }
        )
    }
    // Lógica de Validación
    fun validateForm(): Boolean {
        var isValid = true

        // Validar Correo o Teléfono
        if (identifier.isBlank()) {
            identifierError = "Este campo es requerido"
            isValid = false
        } else {
            val isEmail = Patterns.EMAIL_ADDRESS.matcher(identifier).matches()
            val isPhone = identifier.all { it.isDigit() } && identifier.length >= 10

            if (!isEmail && !isPhone) {
                identifierError = "Ingresa un correo válido o un teléfono de 10 dígitos"
                isValid = false
            } else {
                identifierError = null
            }
        }

        // Validar Contraseña
        if (password.isBlank()) {
            passwordError = "La contraseña es requerida"
            isValid = false
        } else if (password.length < 6) {
            passwordError = "La contraseña debe tener al menos 6 caracteres"
            isValid = false
        } else {
            passwordError = null
        }

        return isValid
    }
    Box(modifier = Modifier.fillMaxSize()) {
    com.nexusystem.paguito.ui.components.AuthSheetScaffold(registering = false) {
        Text("Inicia sesión", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text("Usa tu cuenta Paguito para continuar.", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(28.dp))
        Text("Correo electrónico", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        // --- 2. FORMULARIO ---
        // Campo: Correo o Teléfono
        CustomLoginTextField(
            value = identifier,
            onValueChange = {
                identifier = it
                if (identifierError != null) identifierError = null // Limpiar error al escribir
            },
            placeholder = "Correo registrado",
            icon = Icons.Default.MailOutline,
            errorMessage = identifierError,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Contraseña", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(8.dp))
        // Campo: Contraseña
        CustomLoginTextField(
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) passwordError = null // Limpiar error al escribir
            },
            placeholder = "Contraseña",
            icon = Icons.Default.Lock,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordVisibilityChange = { passwordVisible = it },
            errorMessage = passwordError
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ¿Olvidé mi contraseña?
        Text(
            text = "¿Olvidé mi contraseña?",
            color =  MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.End)
                .clickable { onForgotPasswordClick() }
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- 3. BOTONES ---
        // Botón Iniciar Sesión
        Button(
            onClick = {
                if (validateForm()) {
                    viewModel.loginUser(identifier, password,  token =prefs.getString("MyToken","").toString())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = MaterialTheme.shapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                "Iniciar Sesión",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Continuar sin registro
      /*  Text(
            text = "Continuar sin registro (Modo Invitado)",
            color = TextLightGray,
            fontSize = 13.sp,
            modifier = Modifier
                .clickable { onGuestClick() }
                .padding(8.dp)
        )*/

        Spacer(modifier = Modifier.height(24.dp)) // Empuja el texto inferior hasta abajo

        // --- 4. TEXTO INFERIOR (Registro) ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "¿No tienes cuenta? ", color = TextLightGray, fontSize = 14.sp)
            Text(
                text = "Regístrate",
                color =  MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onRegisterClick() }
            )
        }
        Text("Tu negocio siempre contigo", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(modifier = Modifier.height(16.dp)) // Empuja el texto inferior hasta abajo
    }

    LoadingOverlay(
        isLoading = isLoading,
        lottieRes = R.raw.loadings
    )
}
}

// ================= COMPONENTE REUTILIZABLE DE TEXTFIELD =================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomLoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (Boolean) -> Unit = {},
    errorMessage: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else keyboardType),
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            placeholder = { Text(placeholder, color = TextLightGray, fontSize = 14.sp) },
            leadingIcon = { Icon(icon, contentDescription = null, tint = TextLightGray, modifier = Modifier.size(20.dp)) },
            trailingIcon = {
                if (isPassword) {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { onPasswordVisibilityChange(!passwordVisible) }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña", tint = TextLightGray, modifier = Modifier.size(20.dp))
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                errorContainerColor = MaterialTheme.colorScheme.background,
                focusedBorderColor = MaterialTheme.colorScheme.primary, 
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                errorBorderColor = MaterialTheme.colorScheme.error,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor =  MaterialTheme.colorScheme.onSurface
            ),
            isError = errorMessage != null
        )

        // Mensaje de Error
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}