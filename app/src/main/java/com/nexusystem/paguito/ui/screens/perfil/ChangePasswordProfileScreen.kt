package com.nexusystem.paguito.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.screens.login.AuthViewModel
import com.nexusystem.paguito.utils.LoadingOverlay
import com.nexusystem.paguito.utils.PaguitoStore
import com.nexusystem.paguito.utils.dialogs.SuccessAlertDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordProfileScreen(mail:String, viewModel: AuthViewModel, goToLogin:()->Unit,onBackClick:()->Unit) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Validaciones
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    val validationsCount = listOf(hasMinLength, hasUppercase, hasNumber, hasSpecialChar).count { it }
    val passwordsMatch = password.isNotEmpty() && password == confirmPassword
    val isButtonEnabled = validationsCount == 4 && passwordsMatch
    val isLoading by viewModel.isLoading.collectAsState()
    val changePasswordSucess by viewModel.changePasswordSucess.collectAsState()
    val primaryGreen = Color(0xFF24D193)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    if(changePasswordSucess){
        SuccessAlertDialog("Contraseña, actualizada correctamente, !Logueate nuevamente !",{
            scope.launch {
                PaguitoStore.setLogout(context)
                viewModel.resetChangePassword()
                goToLogin()
            }
        },{
            scope.launch {
                PaguitoStore.setLogout(context)
                viewModel.resetChangePassword()
                goToLogin()
            }
        })

    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Cambiar contraseña",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor =MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        containerColor = MaterialTheme.colorScheme.background // Fondo gris muy claro
    ) { paddingValues ->
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                // Campo: Crear Contraseña
                Text("Crear Contraseña", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                PasswordTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "........",
                    isVisible = passwordVisible,
                    onVisibilityToggle = { passwordVisible = !passwordVisible },
                    borderColor = if (validationsCount == 4) primaryGreen else Color.LightGray
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Card de Validación
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "SEGURIDAD DE LA CONTRASEÑA",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                            Text("$validationsCount/4", fontSize = 11.sp, color = Color.Gray)
                        }

                        LinearProgressIndicator(
                            progress = { validationsCount / 4f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .height(6.dp),
                            color = primaryGreen,
                            trackColor = Color.LightGray,
                            strokeCap = StrokeCap.Round
                        )

                        Row(modifier = Modifier.fillMaxWidth()) {
                            ValidationItem("1 Mayúscula", hasUppercase, Modifier.weight(1f))
                            ValidationItem("1 Número", hasNumber, Modifier.weight(1f))
                        }
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                            ValidationItem(
                                "1 Carácter especial",
                                hasSpecialChar,
                                Modifier.weight(1f)
                            )
                            ValidationItem("8+ Caracteres", hasMinLength, Modifier.weight(1f))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Campo: Confirmar Contraseña
                Text("Confirmar Contraseña", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                PasswordTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Repite tu contraseña",
                    isVisible = confirmPasswordVisible,
                    onVisibilityToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                    borderColor = if (passwordsMatch) primaryGreen else Color.LightGray
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Botón Final
                Button(
                    onClick = {
                        viewModel.changePassword(
                            mail.replace("\"", "").replace("\\", "").trim(), password
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = isButtonEnabled,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryGreen,
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cambiar contraseña", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            LoadingOverlay(
                isLoading = isLoading,
                lottieRes = R.raw.loadings
            )
        }
    }
}

@Composable
fun ValidationItem(text: String, isValid: Boolean, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isValid) Color(0xFF24D193) else Color.LightGray,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, fontSize = 12.sp, color = if (isValid) Color.Black else Color.Gray)
    }
}

@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isVisible: Boolean,
    onVisibilityToggle: () -> Unit,
    borderColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        placeholder = { Text(placeholder, color = Color.LightGray) },
        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color.LightGray) },
        trailingIcon = {
            IconButton(onClick = onVisibilityToggle) {
                Icon(
                    imageVector = if (isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = null,
                    tint = Color.LightGray
                )
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = borderColor,
            unfocusedBorderColor = Color.LightGray
        )
    )
}