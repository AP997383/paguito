package com.nexusystem.paguito.ui.screens.login

import android.content.Context
import android.util.Log
import android.util.Patterns
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


val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF6B7280)
val TextLightGray = Color(0xFF9CA3AF)

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
            token =prefs.getString("MyToken","").toString(),
            userSuscription = successLogin!!.userSuscription?: UserSuscriptionData()
        )
        Log.e("RESPONSE_LOGIN","-->"+profileToSave)
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
    Box{

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = com.nexusystem.paguito.R.drawable.abonia_a),
                    contentDescription = null,
                    modifier = Modifier.size(90.dp)
                )
            }


        Spacer(modifier = Modifier.height(48.dp))

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
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                "Iniciar Sesión",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
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

        Spacer(modifier = Modifier.weight(1f)) // Empuja el texto inferior hasta abajo

        // --- 4. TEXTO INFERIOR (Registro) ---
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
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
        Spacer(modifier = Modifier.height(50.dp)) // Empuja el texto inferior hasta abajo
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
        TextField(
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
                        Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = TextLightGray, modifier = Modifier.size(20.dp))
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                errorContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent, // Quita la línea de abajo
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
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