package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.testTag
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.ui.components.SoftCard
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.formatFecha

@Composable
fun RecentPaymentsSection(pagos: List<PagoConNombre?>, seeAllPayments: () -> Unit, onMovement: (PagoConNombre) -> Unit = {}) {
    Spacer(Modifier.height(20.dp))
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.History, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Text("Actividad reciente", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = seeAllPayments, contentPadding = PaddingValues(horizontal = 4.dp)) { Text("Ver todas", style = MaterialTheme.typography.labelMedium) }
        }
        Spacer(Modifier.height(8.dp))
        pagos.filterNotNull().forEach { payment ->
            RecentPaymentItem(
                name = (if (payment.isIngreso) "Pago recibido" else "Venta registrada") + " · " + payment.nameDeudor,
                details = runCatching { formatFecha(payment.fechaAbono) }.getOrDefault(payment.fechaAbono.take(10)),
                amount = payment.montoAbonado.toString(), isIngreso = payment.isIngreso, onClick = { onMovement(payment) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun RecentPaymentItem(name: String, details: String, amount: String, isIngreso: Boolean, onClick: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    Surface(shape = MaterialTheme.shapes.large, color = colors.background.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.25f)), modifier = Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = MaterialTheme.shapes.medium, color = colors.primary.copy(alpha = 0.07f)) {
                Icon(if (isIngreso) Icons.Outlined.Payments else Icons.Outlined.ShoppingCart, null,
                    tint = colors.primary, modifier = Modifier.padding(10.dp).size(20.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(name, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(details, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Text(formatAsCurrency(amount), style = MaterialTheme.typography.titleSmall)
        }
    }
}
