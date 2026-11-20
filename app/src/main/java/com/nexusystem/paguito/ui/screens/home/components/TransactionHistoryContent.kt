package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.ui.components.*

@Composable
fun TransactionHistoryContent(payments: List<PagoConNombre>, onBack: () -> Unit, onMovement: (PagoConNombre) -> Unit = {}) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(MovementFilter.All) }
    var recentDays by rememberSaveable { mutableStateOf(0) }
    val entries = remember(payments) { payments.map { it.toMovement() } }
    val filtered = remember(entries, query, filter, recentDays) { filterMovements(entries, query, filter).filter { recentDays == 0 || movementDate(it.rawDate)?.let { date -> !date.isBefore(java.time.LocalDate.now().minusDays((recentDays - 1).toLong())) && !date.isAfter(java.time.LocalDate.now()) } == true } }
    LazyColumn(Modifier.fillMaxSize().paguitoBackground().testTag("movement-list"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 156.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { ScreenHeader("Transacciones", "Todos tus pagos y ventas", onBack, horizontalPadding = 0.dp) }
        item { MovementSummary(entries) }
        item {
            OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().testTag("movement-search"), singleLine = true,
                shape = MaterialTheme.shapes.large, placeholder = { Text("Cliente, importe o método", style = MaterialTheme.typography.bodyMedium) },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, "Limpiar búsqueda") } },
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant, unfocusedContainerColor = MaterialTheme.colorScheme.surface))
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0 to "Todo el historial", 7 to "7 días", 30 to "30 días").forEach { (days, label) ->
                    FilterChip(selected = recentDays == days, onClick = { recentDays = days }, label = { Text(label) })
                }
            }
            MovementFilters(filter) { filter = it }
        }
        if (filtered.isEmpty()) item {
            SoftCard {
                Icon(Icons.Outlined.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(12.dp))
                Text(if (entries.isEmpty()) "Tu historial empieza aquí" else "Sin coincidencias", style = MaterialTheme.typography.titleMedium)
                Text(if (entries.isEmpty()) "Tus pagos y ventas aparecerán en esta sección." else "Prueba otra búsqueda o cambia el filtro.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        itemsIndexed(filtered, key = { index, entry -> "${entry.key}:$index" }) { index, entry ->
            if (index == 0 || movementDate(filtered[index - 1].rawDate) != movementDate(entry.rawDate)) {
                Text(movementDayLabel(entry.rawDate), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp, bottom = 10.dp))
            }
            val original = payments.firstOrNull { it.toMovement().key == entry.key }
            MovementCard(entry, onOpen = { original?.let(onMovement) }, onReceipt = { original?.let(onMovement) })
        }
    }
}
