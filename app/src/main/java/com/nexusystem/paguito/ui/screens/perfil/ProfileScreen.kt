package com.nexusystem.paguito.ui.screens.perfil

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- COLORES PRINCIPALES ---
val BgColor = Color(0xFFF3F4F6)       // Gris clarito del fondo
val CardBgColor = Color(0xFFFFFFFF)   // Blanco de las tarjetas
val TextPrimary = Color(0xFF1F2937)   // Texto oscuro principal
val TextSecondary = Color(0xFF9CA3AF) // Texto gris claro (subtítulos)
val BorderColor = Color(0xFFE5E7EB)   // Bordes sutiles
val BluePrimary = Color(0xFF3B82F6)   // Azul de los switches
val RedLogout = Color(0xFFEF4444)     // Rojo salir

@Composable
fun ProfileScreen() {
    // Estados para los switches
    var isDarkMode by remember { mutableStateOf(true) }
    var isBiometricEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgColor)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Título de la pantalla
        Text(
            text = "Perfil y Ajustes",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 1. TARJETA DE PERFIL (Header)
        ProfileHeaderCard()

        Spacer(modifier = Modifier.height(24.dp))

        // 2. SECCIÓN: MI NEGOCIO
        SectionTitle(title = "Mi Negocio")
        SettingsCard {
            SettingsItem(
                icon = Icons.Outlined.LocalOffer,
                title = "Catálogo de Productos",
                onClick = { }
            )
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(start = 56.dp))
            SettingsItem(
                icon = Icons.Outlined.Download,
                title = "Exportar mi Libreta (Excel/PDF)",
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 3. SECCIÓN: AUTOMATIZACIÓN Y COBRO
        SectionTitle(title = "Automatización y Cobro")
        SettingsCard {
            SettingsItem(
                icon = Icons.AutoMirrored.Outlined.Chat,
                title = "Configuración de SMS Automáticos",
                onClick = { }
            )
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(start = 56.dp))
            SettingsItem(
                icon = Icons.Outlined.AccountBalance,
                title = "Cuentas Bancarias",
                subtitle = "Donde me depositan",
                onClick = { }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. SECCIÓN: PREFERENCIAS DE LA APP
        SectionTitle(title = "Preferencias de la App")
        SettingsCard {
            // Idioma
            SettingsItem(
                icon = Icons.Outlined.Language,
                title = "Idioma",
                onClick = { },
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Español", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            )
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(start = 56.dp))

            // Modo Oscuro (Switch)
            SettingsItem(
                icon = Icons.Outlined.DarkMode,
                title = "Modo Oscuro",
                onClick = { isDarkMode = !isDarkMode },
                trailingContent = {
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { isDarkMode = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = BluePrimary)
                    )
                }
            )
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(start = 56.dp))

            // Bloqueo con Huella (Switch)
            SettingsItem(
                icon = Icons.Outlined.Lock,
                title = "Bloqueo con Huella/FaceID",
                onClick = { isBiometricEnabled = !isBiometricEnabled },
                trailingContent = {
                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { isBiometricEnabled = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = BluePrimary)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // 5. BOTÓN CERRAR SESIÓN Y VERSIÓN
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* Acción cerrar sesión */ }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = "Salir", tint = RedLogout, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Cerrar Sesión", color = RedLogout, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "v1.0.2",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- COMPONENTES REUTILIZABLES ---

@Composable
fun ProfileHeaderCard() {
    Surface(
        color = CardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar (Placeholder con fondo de color)
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFCE7F3)), // Rosa clarito de fondo
                contentAlignment = Alignment.Center
            ) {
                // Usa AsyncImage de Coil en producción para cargar la foto real
                Icon(Icons.Outlined.Person, contentDescription = "Avatar", tint = Color(0xFFEC4899), modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Ana Sofía", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(text = "Novedades Sofía", fontSize = 14.sp, color = TextSecondary)
            }

            // Botón Editar Perfil
            Surface(
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderColor),
                color = Color.Transparent,
                modifier = Modifier.clickable { }
            ) {
                Text(
                    text = "Editar perfil",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = CardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            content()
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit = {
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = title, tint = TextSecondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = TextPrimary)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
            }
        }

        trailingContent()
    }
}