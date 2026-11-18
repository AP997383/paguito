package com.nexusystem.paguito.ui.components

import android.util.Patterns
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RecoveryContent(email: String, onEmail: (String) -> Unit, loading: Boolean, onBack: () -> Unit, onSend: () -> Unit) {
    Scaffold(modifier = Modifier.imePadding(), containerColor = MaterialTheme.colorScheme.background,
        topBar = { ScreenHeader("Recuperar acceso", "Volvamos a tu negocio", onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).paguitoBackground().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            SoftCard {
                Icon(Icons.Outlined.Key, null, modifier = Modifier.size(44.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(20.dp)); Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(8.dp)); Text("Escribe el correo de tu cuenta y te enviaremos un código para crear una nueva contraseña.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            SoftCard {
                Text("Tu correo registrado", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp)); OutlinedTextField(email, onEmail, label = { Text("Correo electrónico") }, placeholder = { Text("correo@ejemplo.com") },
                    leadingIcon = { Icon(Icons.Outlined.MailOutline, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("recovery-email"), shape = MaterialTheme.shapes.medium)
                Spacer(Modifier.height(12.dp)); Text("Revisa también tu carpeta de correo no deseado.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onSend, enabled = !loading && Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches(), modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("send-recovery"), shape = MaterialTheme.shapes.large) {
                if (loading) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp) else Icon(Icons.Outlined.Send, null)
                Spacer(Modifier.width(10.dp)); Text(if (loading) "Enviando código…" else "Enviar código")
            }
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Volver a iniciar sesión") }
            Spacer(Modifier.navigationBarsPadding().height(16.dp))
        }
    }
}
