package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.getDaysUntilNextPayment

val TextGray: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val BorderLight: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
val RedUrgent = Color(0xFFEF4444)

@Composable
fun UpcomingDeadlinesSection(deudores: List<DeudoresEntity?>, seeAllDeudores: () -> Unit, openDetailDeudor: (DeudoresEntity) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Próximos cobros", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        TextButton(onClick = seeAllDeudores) { Text("Ver todos", style = MaterialTheme.typography.labelMedium) }
    }
    Spacer(Modifier.height(8.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(deudores.filterNotNull().filter { it.montoActualAdeudado > 0f }) { client ->
            UpcomingCard({ openDetailDeudor(client) }, client.nombre, client.montoActualAdeudado.toString(),
                client.fechaInicialDeuda, false, client.periodicidad)
        }
    }
}

@Composable
fun UpcomingCard(openDetailDeudor: () -> Unit, name: String, amount: String, date: String, isUrgent: Boolean, periodisity: String) {
    val colors = MaterialTheme.colorScheme
    val days = runCatching { getDaysUntilNextPayment(date, periodisity) }.getOrNull()
    Card(onClick = openDetailDeudor, modifier = Modifier.width(194.dp), shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(42.dp).background(colors.primary.copy(alpha = 0.08f), MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                Text(name.trim().take(1).uppercase().ifBlank { "?" }, style = MaterialTheme.typography.titleMedium, color = colors.primary)
            }
            Text(name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Column {
                Text("Saldo por cobrar", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text(formatAsCurrency(amount), style = MaterialTheme.typography.titleLarge)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.CalendarToday, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                Text(when { days == null || periodisity == "A" -> "Fecha por definir"; days == 0L -> "Hoy"; days == 1L -> "Mañana"; else -> "En $days días" },
                    style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}
