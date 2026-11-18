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

    com.nexusystem.paguito.ui.components.PasswordEditorContent(
        password, confirmPassword, { password = it }, { confirmPassword = it }, isLoading,
        onBack = onBackClick,
        onSave = { viewModel.changePassword(mail.replace("\"", "").replace("\\", "").trim(), password) })
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