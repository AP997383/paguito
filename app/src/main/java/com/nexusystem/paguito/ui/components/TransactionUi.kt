package com.nexusystem.paguito.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.utils.formatAsCurrency
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class MovementFilter(val label: String) { All("Todos"), Payments("Pagos"), Sales("Ventas") }

data class MovementEntry(val key: String, val client: String, val amount: Double, val rawDate: String,
    val isPayment: Boolean, val method: Int, val notes: String = "", val products: String = "",
    val previousBalance: Double? = null)

fun PagoConNombre.toMovement() = MovementEntry("$idRemoteDatabase:$idDeudorRemoteDatabase:$isIngreso:$id", nameDeudor, montoAbonado, fechaAbono, isIngreso, tipoPago, notas, jsonAbonoPorProducto)
fun PagosEntinty.toMovement(client: String = "") = MovementEntry("$id:$idRemoteDatabase:$fechaAbono", client, montoAbonado, fechaAbono, isIngreso, tipoPago, notas, jsonAbonoPorProducto, saldoAntesDeAbono)
fun movementDate(raw: String): LocalDate? = runCatching { LocalDate.parse(raw.take(10)) }.getOrNull()
fun movementDayLabel(raw: String): String = movementDate(raw)?.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "MX"))) ?: "Fecha no disponible"
fun movementMethod(value: Int) = when (value) { 2 -> "Transferencia"; 3 -> "Tarjeta"; else -> "Efectivo" }

fun filterMovements(entries: List<MovementEntry>, query: String, filter: MovementFilter): List<MovementEntry> {
    val term = query.trim()
    return entries.filter {
        (filter == MovementFilter.All || (filter == MovementFilter.Payments && it.isPayment) || (filter == MovementFilter.Sales && !it.isPayment)) &&
            (term.isEmpty() || listOf(it.client, it.notes, it.amount.toString(), it.rawDate, movementDayLabel(it.rawDate), movementMethod(it.method), it.products).any { text -> text.contains(term, true) })
    }.sortedWith(compareByDescending<MovementEntry> { movementDate(it.rawDate) ?: LocalDate.MIN }.thenByDescending { it.rawDate.replace(' ', 'T') }.thenByDescending { it.key })
}

@Composable
fun MovementFilters(selected: MovementFilter, onSelect: (MovementFilter) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MovementFilter.entries.forEach { filter -> FilterChip(selected = selected == filter, onClick = { onSelect(filter) }, label = { Text(filter.label) }) }
    }
}

@Composable
fun MovementSummary(entries: List<MovementEntry>) {
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
            Text("Tu actividad, en orden", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(8.dp))
        Text("${entries.size} movimientos registrados", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Cobrado", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatAsCurrency(entries.filter { it.isPayment }.sumOf { it.amount.toDouble() }), style = MaterialTheme.typography.titleLarge)
            }
            Column(Modifier.weight(1f)) {
                Text("Ventas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatAsCurrency(entries.filterNot { it.isPayment }.sumOf { it.amount.toDouble() }), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
fun MovementCard(entry: MovementEntry, onOpen: (() -> Unit)? = null, onReceipt: (() -> Unit)? = null, onDelete: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember(entry.key) { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    val productNames = remember(entry.products) {
        runCatching { com.nexusystem.paguito.data.mapper.RelatedProductsJson.decode(entry.products).map { it.nombre }.filter { it.isNotBlank() } }.getOrDefault(emptyList())
    }
    Card(onClick = { if (onOpen != null) onOpen() else expanded = !expanded },
        modifier = Modifier.fillMaxWidth().testTag("movement-${entry.key}"), shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.surface), border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f))) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = (if (entry.isPayment) colors.primary else colors.tertiary).copy(alpha = 0.08f), shape = MaterialTheme.shapes.medium) {
                    Icon(if (entry.isPayment) Icons.Outlined.Payments else Icons.Outlined.ShoppingBag, null,
                        tint = if (entry.isPayment) colors.primary else colors.tertiary, modifier = Modifier.padding(12.dp).size(22.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(if (entry.isPayment) "Pago recibido" else "Venta registrada", style = MaterialTheme.typography.titleSmall)
                    Text(entry.client.ifBlank { movementMethod(entry.method) }, style = MaterialTheme.typography.bodySmall,
                        color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (onReceipt != null || onDelete != null) Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreHoriz, "Opciones del movimiento") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        if (onReceipt != null) DropdownMenuItem(text = { Text("Ver comprobante") }, leadingIcon = { Icon(Icons.Outlined.ReceiptLong, null) }, onClick = { menu = false; onReceipt() })
                        if (onDelete != null) DropdownMenuItem(text = { Text("Eliminar movimiento", color = colors.error) }, leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = colors.error) }, onClick = { menu = false; onDelete() })
                    }
                } else Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, "Ver detalles", tint = colors.onSurfaceVariant)
            }
            Text(formatAsCurrency(entry.amount.toFloat()), style = MaterialTheme.typography.headlineSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(14.dp), tint = colors.onSurfaceVariant)
                Text(movementDayLabel(entry.rawDate), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text("· ${movementMethod(entry.method)}", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            if (productNames.isNotEmpty()) Text(productNames.take(2).joinToString(" · ") + if (productNames.size > 2) " +${productNames.size - 2}" else "",
                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (entry.previousBalance != null) {
                val balance = if (entry.isPayment) entry.previousBalance - entry.amount else entry.previousBalance + entry.amount
                Text(if (balance < 0) "Saldo a favor después: ${formatAsCurrency(-balance)}" else "Saldo pendiente después: ${formatAsCurrency(balance)}",
                    style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Text(if (entry.isPayment) "Reduce el saldo pendiente" else "Aumenta el saldo pendiente", style = MaterialTheme.typography.labelSmall,
                color = if (entry.isPayment) colors.primary else colors.tertiary)
            if (entry.notes.isNotBlank() && !expanded) Text(entry.notes, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (expanded) {
                HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.4f))
                Text("Notas", style = MaterialTheme.typography.labelMedium)
                Text(entry.notes.ifBlank { "Sin notas adicionales" }, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}
