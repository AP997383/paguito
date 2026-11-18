package com.nexusystem.paguito.ui.screens.perfil

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nexusystem.paguito.ui.components.*

@Composable
fun EditProfileContent(business: String, name: String, phone: String, email: String, photo: String, loading: Boolean,
    onBusiness: (String) -> Unit, onName: (String) -> Unit, onPhone: (String) -> Unit, onBack: () -> Unit, onSave: () -> Unit, photoPicker: @Composable () -> Unit) {
    Scaffold(modifier = Modifier.imePadding(), containerColor = MaterialTheme.colorScheme.background,
        topBar = { ScreenHeader("Editar perfil", "Tu negocio empieza contigo", onBack) },
        bottomBar = { Surface(color = MaterialTheme.colorScheme.background, shadowElevation = 4.dp) {
            Button(onClick = onSave, enabled = !loading && name.isNotBlank() && phone.isNotBlank(), modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp, 12.dp).heightIn(min = 56.dp), shape = MaterialTheme.shapes.large) {
                Icon(Icons.Outlined.Check, null); Spacer(Modifier.width(10.dp)); Text(if (loading) "Guardando…" else "Guardar cambios")
            }
        } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).paguitoBackground().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                            if (photo.isNotBlank()) AsyncImage(photo, "Foto de perfil", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                            else Text(name.trim().take(1).uppercase().ifBlank { "P" }, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
                        }
                        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { photoPicker() }
                    }
                    Column(Modifier.weight(1f)) { Text("Dale tu toque", style = MaterialTheme.typography.titleMedium); Text("Una foto para reconocer tu cuenta", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            SoftCard {
                Text("Tu información", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(name, onName, label = { Text("Nombre completo *") }, leadingIcon = { Icon(Icons.Outlined.Person, null) }, modifier = Modifier.fillMaxWidth().testTag("edit-profile-name"), singleLine = true, shape = MaterialTheme.shapes.medium)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(phone, onPhone, label = { Text("Teléfono *") }, leadingIcon = { Icon(Icons.Outlined.Phone, null) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.medium)
            }
            SoftCard {
                Text("Tu negocio", style = MaterialTheme.typography.titleMedium); Spacer(Modifier.height(16.dp))
                OutlinedTextField(business, onBusiness, label = { Text("Nombre del negocio") }, leadingIcon = { Icon(Icons.Outlined.Storefront, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = MaterialTheme.shapes.medium)
            }
            SoftCard {
                Text("Correo de la cuenta", style = MaterialTheme.typography.labelLarge)
                Text(email, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(8.dp)); Text("Este correo identifica tu cuenta y no se puede cambiar desde aquí.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
