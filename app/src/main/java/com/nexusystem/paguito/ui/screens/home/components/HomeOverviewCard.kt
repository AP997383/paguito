package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.ui.components.SoftCard
import com.nexusystem.paguito.utils.formatAsCurrency
import java.time.LocalDate

@Composable
fun HomeOverviewCard(
    payments: List<AbonosDelMes>, summary: DeudoresSummary, products: Int,
    showBalance: Boolean, onToggleBalance: () -> Unit, onAddPayment: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val today = LocalDate.now().toString()
    val sales = remember(payments, today) { payments.filter { !it.isIngreso && it.fechaAbono.take(10) == today } }
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.BarChart, null, tint = colors.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(10.dp))
            Text("Tu negocio hoy", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Surface(color = colors.primary.copy(alpha = 0.08f), shape = MaterialTheme.shapes.small) {
                Text("Hoy", color = colors.primary, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
            }
            IconButton(onClick = onToggleBalance) {
                Icon(if (showBalance) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    if (showBalance) "Ocultar importes" else "Mostrar importes", tint = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(if (showBalance) formatAsCurrency(sales.sumOf { it.montoAbonado.toDouble() }.toFloat()) else "$ ••••••",
            style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(4.dp))
        Text("Ventas registradas hoy", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.ShoppingCart, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp)); Text("${sales.size} ventas", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Inventory2, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp)); Text("$products productos", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        HorizontalDivider(Modifier.padding(vertical = 20.dp), color = colors.outlineVariant.copy(alpha = 0.45f))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Por cobrar · ${summary.totalDeudores} clientes", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text(if (showBalance) formatAsCurrency(summary.sumaTotalMontos ?: 0f) else "$ ••••",
                    style = MaterialTheme.typography.titleLarge)
            }
            FilledTonalIconButton(onClick = onAddPayment, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Outlined.Add, "Registrar pago")
            }
        }
    }
}
