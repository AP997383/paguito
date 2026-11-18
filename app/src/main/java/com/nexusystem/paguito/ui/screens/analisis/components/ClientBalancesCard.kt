package com.nexusystem.paguito.ui.screens.analisis.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.ui.components.SoftCard
import com.nexusystem.paguito.utils.formatAsCurrency

@Composable
fun ClientBalancesCard(clients: List<DeudoresEntity?>) {
    val ranked = clients.filterNotNull().filter { it.montoActualAdeudado > 0f }.sortedByDescending { it.montoActualAdeudado }.take(10)
    val maximum = ranked.maxOfOrNull { it.montoActualAdeudado } ?: 1f
    val colors = MaterialTheme.colorScheme
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.PeopleAlt, null, tint = colors.primary)
            Text("Saldos por recuperar", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(4.dp))
        Text("Tus 10 clientes con mayor saldo pendiente", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(20.dp))
        if (ranked.isEmpty()) Text("Sin saldos pendientes por ahora.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        ranked.forEachIndexed { index, client ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(30.dp).background(colors.primary.copy(alpha = 0.08f), MaterialTheme.shapes.small), contentAlignment = Alignment.Center) {
                    Text("${index + 1}", style = MaterialTheme.typography.labelMedium, color = colors.primary)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(client.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Spacer(Modifier.width(8.dp))
                        Text(formatAsCurrency(client.montoActualAdeudado), style = MaterialTheme.typography.labelLarge)
                    }
                    Box(Modifier.fillMaxWidth().height(6.dp).background(colors.primary.copy(alpha = 0.06f), CircleShape)) {
                        Box(Modifier.fillMaxWidth((client.montoActualAdeudado / maximum).coerceIn(0f, 1f)).fillMaxHeight().background(colors.primary.copy(alpha = 1f - index * 0.055f), CircleShape))
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
        HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.4f))
        Spacer(Modifier.height(16.dp))
        Text("Total de estos clientes", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        Text(formatAsCurrency(ranked.sumOf { it.montoActualAdeudado.toDouble() }.toFloat()), style = MaterialTheme.typography.titleLarge)
    }
}
