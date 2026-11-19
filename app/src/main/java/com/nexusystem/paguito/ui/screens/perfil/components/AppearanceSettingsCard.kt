package com.nexusystem.paguito.ui.screens.perfil.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexusystem.paguito.ui.components.SoftCard
import com.nexusystem.paguito.ui.theme.*

@Composable
fun AppearanceSettingsCard() {
    val appearance = LocalAppearance.current
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = MaterialTheme.shapes.medium, color = colors.primary.copy(alpha = 0.09f)) {
                Icon(Icons.Outlined.Palette, null, tint = colors.primary, modifier = Modifier.padding(12.dp))
            }
            Column {
                Text("Hazlo tuyo", style = MaterialTheme.typography.titleMedium)
                Text("Tu estilo, en todo Paguito", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Tema", style = MaterialTheme.typography.labelLarge)
        Row(modifier = Modifier.fillMaxWidth().selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppearanceMode.entries.forEach { mode ->
                FilterChip(
                    selected = appearance.mode == mode,
                    onClick = { AppearancePreferences.setMode(context, mode) },
                    label = { Text(mode.label, style = MaterialTheme.typography.labelMedium) },
                    modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                )
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = colors.outlineVariant.copy(alpha = 0.5f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.AccountBalanceWallet, null, tint = colors.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Fondo financiero", style = MaterialTheme.typography.titleSmall)
                Text("Billeteras y monedas con movimiento suave", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Switch(checked = appearance.showWaves,
                onCheckedChange = { AppearancePreferences.setWaves(context, it) },
                modifier = Modifier.semantics { contentDescription = "Fondo financiero" })
        }
        HorizontalDivider(Modifier.padding(vertical = 16.dp), color = colors.outlineVariant.copy(alpha = 0.5f))
        Text("Color de Paguito", style = MaterialTheme.typography.titleSmall)
        Text("Se guarda y se aplica al instante", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(12.dp))
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            AccentColor.entries.chunked(4).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    row.forEach { accent ->
                        val selected = appearance.accent == accent
                        Column(
                            modifier = Modifier.weight(1f).selectable(
                                selected = selected, role = Role.RadioButton,
                                onClick = { AppearancePreferences.setAccent(context, accent) }
                            ).background(if (selected) colors.primary.copy(alpha = 0.08f) else Color.Transparent, MaterialTheme.shapes.medium)
                                .padding(vertical = 10.dp, horizontal = 2.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(modifier = Modifier.size(36.dp).background(
                                Brush.verticalGradient(listOf(accent.light.copy(alpha = 0.7f), accent.light)), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center) {
                                if (selected) Icon(Icons.Outlined.Check, "Color seleccionado", tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                            Text(accent.label, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center,
                                color = if (selected) colors.onSurface else colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
