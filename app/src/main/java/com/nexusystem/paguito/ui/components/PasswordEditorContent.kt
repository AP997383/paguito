package com.nexusystem.paguito.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp

fun passwordRules(password: String) = listOf(password.length >= 8, password.any { it.isUpperCase() }, password.any { it.isDigit() }, password.any { !it.isLetterOrDigit() })

@Composable
fun PasswordEditorContent(password: String, confirmation: String, onPassword: (String) -> Unit, onConfirmation: (String) -> Unit, loading: Boolean, onBack: (() -> Unit)?, onSave: () -> Unit) {
    val rules = passwordRules(password)
    Scaffold(modifier = Modifier.imePadding(), containerColor = MaterialTheme.colorScheme.background,
        topBar = { ScreenHeader("Cambiar contraseña", "Un nuevo comienzo, con más seguridad", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).paguitoBackground().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            SoftCard {
                Icon(Icons.Outlined.LockReset, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                Spacer(Modifier.height(12.dp)); Text("Protege tu cuenta", style = MaterialTheme.typography.titleLarge)
                Text("Elige una contraseña que solo tú conozcas. Al terminar podrás iniciar sesión con ella.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SoftCard {
                SecurePasswordInput("Nueva contraseña", password, onPassword, "new-password")
                Spacer(Modifier.height(18.dp)); Text("Seguridad de la contraseña · ${rules.count { it }}/4", style = MaterialTheme.typography.labelLarge)
                LinearProgressIndicator(progress = { rules.count { it } / 4f }, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp))
                listOf("8 o más caracteres", "Una mayúscula", "Un número", "Un carácter especial").forEachIndexed { i, text ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(if (rules[i]) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked, null, tint = if (rules[i]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp)); Text(text, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Spacer(Modifier.height(18.dp)); SecurePasswordInput("Confirmar contraseña", confirmation, onConfirmation, "confirm-password")
                if (confirmation.isNotEmpty() && confirmation != password) Text("Las contraseñas aún no coinciden", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onSave, enabled = rules.all { it } && password == confirmation && !loading,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("save-password"), shape = MaterialTheme.shapes.large) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Icon(Icons.Outlined.Check, null)
                Spacer(Modifier.width(10.dp)); Text(if (loading) "Actualizando…" else "Cambiar contraseña")
            }
            Spacer(Modifier.navigationBarsPadding().height(16.dp))
        }
    }
}

@Composable
fun SecurePasswordInput(label: String, value: String, onChange: (String) -> Unit, tag: String) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(value, onChange, label = { Text(label) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().testTag(tag), shape = MaterialTheme.shapes.medium,
        leadingIcon = { Icon(Icons.Outlined.Lock, null) }, trailingIcon = { IconButton(onClick = { visible = !visible }) { Icon(if (visible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, if (visible) "Ocultar $label" else "Mostrar $label") } })
}
