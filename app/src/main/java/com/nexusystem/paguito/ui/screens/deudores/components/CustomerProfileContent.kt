package com.nexusystem.paguito.ui.screens.deudores.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexusystem.paguito.ui.components.*
import com.nexusystem.paguito.utils.formatAsCurrency

@Composable
fun CustomerIdentityCard(client: DeudoresEntity, onCall: () -> Unit, onReminder: () -> Unit, onStatement: () -> Unit, onWhatsApp: () -> Unit = {}) {
    val colors = MaterialTheme.colorScheme
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(60.dp).background(colors.primary.copy(alpha = 0.1f), MaterialTheme.shapes.large), contentAlignment = Alignment.Center) {
                Text(client.nombre.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "?" },
                    style = MaterialTheme.typography.titleLarge, color = colors.primary)
            }
            Column(Modifier.weight(1f)) {
                Text(client.nombre.ifBlank { "Cliente" }, style = MaterialTheme.typography.titleLarge)
                Text(client.telefono.takeIf { it.isNotBlank() && it != "=" } ?: "Sin teléfono", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                Text(if (client.inRemote) "Sincronizado" else "Pendiente de sincronizar", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(Triple(Icons.Outlined.Phone, "Llamar", onCall), Triple(Icons.Outlined.ChatBubbleOutline, "WhatsApp", onWhatsApp), Triple(Icons.Outlined.NotificationsNone, "Recordar", onReminder), Triple(Icons.Outlined.ReceiptLong, "Estado de cuenta", onStatement)).forEach { (icon, label, action) ->
                FilledTonalButton(onClick = action, enabled = (label != "Llamar" && label != "WhatsApp") || client.telefono.any { it.isDigit() }, shape = MaterialTheme.shapes.medium, modifier = Modifier.weight(1f).heightIn(min = 80.dp), contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(icon, null, Modifier.size(20.dp)); Spacer(Modifier.height(4.dp)); Text(label, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                    }
                }
            }
        }
    }
}

@Composable
/** Estado de la cuenta separado del historial: un saldo a favor nunca se presenta como deuda negativa. */
fun CustomerBalanceCard(total: Float, paid: Float, pending: Float, period: String, nextDate: String, credit: Float = 0f, isLate: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.AccountBalanceWallet, null, tint = colors.primary)
            Text(when { pending > 0 && isLate -> "Pago vencido"; pending > 0 -> "Saldo pendiente"; credit > 0 || pending < 0 -> "Saldo a favor"; else -> "Sin adeudo" }, style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(14.dp))
        Text(formatAsCurrency(if (pending > 0) pending else maxOf(credit, -pending)), style = MaterialTheme.typography.displaySmall)
        Text(when { pending > 0 && isLate -> "Hay un pago pendiente de su fecha acordada"; pending > 0 -> "Importe que falta por pagar"; credit > 0 || pending < 0 -> "Disponible para próximas compras"; else -> "Este cliente no tiene saldo pendiente." }, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        if (pending > 0 && credit > 0) Text("Saldo a favor: ${formatAsCurrency(credit)}", style = MaterialTheme.typography.bodySmall, color = colors.primary)
        if (total > 0f) {
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) { Text("Abonado", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant); Text(formatAsCurrency(paid), style = MaterialTheme.typography.titleMedium) }
                Column(Modifier.weight(1f)) { Text("Compras acumuladas", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant); Text(formatAsCurrency(total), style = MaterialTheme.typography.titleMedium) }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(progress = { (paid / total).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth().height(6.dp), color = colors.primary, trackColor = colors.primary.copy(alpha = 0.08f))
        }
        if (pending > 0f) {
        HorizontalDivider(Modifier.padding(vertical = 18.dp), color = colors.outlineVariant.copy(alpha = 0.4f))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.CalendarToday, null, tint = colors.primary, modifier = Modifier.size(20.dp))
            Column(Modifier.weight(1f)) {
                Text("Próximo pago", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text(if (period == "A") "Sin fecha fija" else if (movementDate(nextDate) == null) "Por definir" else movementDayLabel(nextDate), style = MaterialTheme.typography.titleSmall)
            }
            Surface(color = colors.primary.copy(alpha = 0.07f), shape = MaterialTheme.shapes.small) {
                Text(when (period) { "S" -> "Semanal"; "Q" -> "Quincenal"; "M" -> "Mensual"; "A" -> "Abierto"; else -> "Sin definir" },
                    modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
        }
        }
    }
}

@Composable
fun CustomerProfileContent(
    client: DeudoresEntity, payments: List<PagosEntinty>, nextDate: String, isLate: Boolean,
    onBack: () -> Unit, onPayment: () -> Unit, onCall: () -> Unit, onReminder: () -> Unit, onStatement: () -> Unit,
    onOpenMovement: (PagosEntinty) -> Unit, onReceipt: (PagosEntinty) -> Unit, onDelete: (PagosEntinty) -> Unit, onWhatsApp: () -> Unit = {}
) {
    var filter by rememberSaveable { mutableStateOf(MovementFilter.All) }
    var query by rememberSaveable { mutableStateOf("") }
    val entries = remember(payments) { payments.map { it.toMovement() } }
    val original = remember(payments) { payments.associateBy { it.toMovement().key } }
    val filtered = remember(entries, filter, query) { filterMovements(entries, query, filter) }
    val colors = MaterialTheme.colorScheme
    Scaffold(containerColor = colors.background,
        topBar = { ScreenHeader("Perfil del cliente", "Su cuenta, en un solo lugar", onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onPayment, icon = { Icon(Icons.Outlined.Add, null) }, text = { Text("Registrar pago") },
                containerColor = colors.primary, contentColor = colors.onPrimary,
                modifier = Modifier.testTag("customer-register-payment"), shape = androidx.compose.foundation.shape.CircleShape)
        }
    ) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).paguitoBackground().testTag("customer-profile"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 112.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { CustomerIdentityCard(client, onCall, onReminder, onStatement, onWhatsApp) }
            item { CustomerBalanceCard(client.montoAcomulado, (client.montoAcomulado - client.montoActualAdeudado).coerceAtLeast(0f), client.montoActualAdeudado, client.periodicidad, nextDate, client.creditBalance, isLate) }
            if (client.correo.isNotBlank() || client.domicilio.isNotBlank() || client.notasSobreDeudor.isNotBlank()) item {
                SoftCard {
                    Text("Datos del cliente", style = MaterialTheme.typography.titleMedium)
                    if (client.correo.isNotBlank()) { Spacer(Modifier.height(10.dp)); Text(client.correo, style = MaterialTheme.typography.bodyMedium) }
                    if (client.domicilio.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(client.domicilio, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant) }
                    if (client.notasSobreDeudor.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(client.notasSobreDeudor, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant) }
                }
            }
            item {
                Text("Movimientos de su cuenta", style = MaterialTheme.typography.titleMedium)
                Text("${entries.size} registros", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text("Las ventas aumentan el saldo; los abonos lo reducen.", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MovementFilter.entries.forEach { option ->
                        val count = entries.count { option == MovementFilter.All || (option == MovementFilter.Payments) == it.isPayment }
                        FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text("${if (option == MovementFilter.Payments) "Abonos" else option.label} ($count)") })
                    }
                }
                if (filter != MovementFilter.All || query.isNotBlank()) TextButton(onClick = { filter = MovementFilter.All; query = "" }) { Text("Limpiar filtros") }
                Text("${filtered.size} de ${entries.size} movimientos", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().testTag("customer-movement-search"), singleLine = true,
                    placeholder = { Text("Importe, fecha, método o nota", style = MaterialTheme.typography.bodyMedium) }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, shape = MaterialTheme.shapes.large)
            }
            if (filtered.isNotEmpty()) item {
                SoftCard {
                    Text("Totales de los movimientos visibles", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f)) { Text("Ventas", style = MaterialTheme.typography.bodySmall); Text(formatAsCurrency(filtered.filterNot { it.isPayment }.sumOf { it.amount }), style = MaterialTheme.typography.titleMedium) }
                        Column(Modifier.weight(1f)) { Text("Abonos", style = MaterialTheme.typography.bodySmall); Text(formatAsCurrency(filtered.filter { it.isPayment }.sumOf { it.amount }), style = MaterialTheme.typography.titleMedium) }
                    }
                }
            }
            if (filtered.isEmpty()) item {
                SoftCard { Text("Sin movimientos para mostrar", style = MaterialTheme.typography.titleSmall); Text(if (entries.isEmpty()) "Los abonos y ventas de este cliente aparecerán aquí." else "Prueba otro filtro o borra la búsqueda.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant) }
            }
            itemsIndexed(filtered, key = { index, entry -> "${entry.key}:$index" }) { _, entry ->
                val transaction = original.getValue(entry.key)
                MovementCard(entry, onOpen = { onOpenMovement(transaction) }, onReceipt = { onReceipt(transaction) }, onDelete = { onDelete(transaction) })
            }
        }
    }
}
