package com.nexusystem.paguito.ui.screens.payments

import androidx.compose.foundation.layout.*
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
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.ui.components.*
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.utils.formatAsCurrency

fun salesByDate(payments: List<PagoConNombre>) = payments.filterNot { it.isIngreso }
    .sortedWith(compareByDescending<PagoConNombre> { movementDate(it.fechaAbono) ?: java.time.LocalDate.MIN }.thenByDescending { it.fechaAbono })

fun saleClient(sale: PagoConNombre, clients: List<DeudoresEntity>): DeudoresEntity? =
    if (sale.idDeudorRemoteDatabase.isNotBlank()) clients.firstOrNull { it.idRemoteDatabase == sale.idDeudorRemoteDatabase }
    else clients.firstOrNull { it.id?.toString() == sale.idDeudor }

@Composable
fun SalesScreen(payments: PagosViewModel, debtors: DeudoresViewModel, onNewSale: () -> Unit, onClient: (DeudoresEntity) -> Unit, onAnalysis: () -> Unit = {}) {
    val entries by payments.pagosConNombre.collectAsState()
    val clients by debtors.deudores.collectAsState()
    LaunchedEffect(Unit) { payments.obtenerUltimosAbonos(); debtors.obtenerDeudores() }
    SalesContent(entries.filterNotNull(), clients.filterNotNull(), onNewSale, onClient, onAnalysis)
}

@Composable
fun SalesContent(payments: List<PagoConNombre>, clients: List<DeudoresEntity>, onNewSale: () -> Unit, onClient: (DeudoresEntity) -> Unit, onAnalysis: () -> Unit = {}) {
    var query by rememberSaveable { mutableStateOf("") }
    var unavailable by remember { mutableStateOf(false) }
    val sales = remember(payments) { salesByDate(payments) }
    val visible = sales.filter { query.isBlank() || it.nameDeudor.contains(query, true) || it.notas.contains(query, true) || it.montoAbonado.toString().contains(query) }
    if (unavailable) AlertDialog(onDismissRequest = { unavailable = false }, title = { Text("Cliente no disponible") },
        text = { Text("Esta venta pertenece a un cliente que ya no está en tu lista.") }, confirmButton = { TextButton(onClick = { unavailable = false }) { Text("Entendido") } })
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0), containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = { ExtendedFloatingActionButton(onClick = onNewSale, icon = { Icon(Icons.Outlined.Add, null) }, text = { Text("Nueva venta") },
            containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.padding(bottom = 112.dp).testTag("new-sale")) }) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize().paguitoBackground().testTag("sales-list"), contentPadding = PaddingValues(20.dp, 16.dp, 20.dp, 220.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { Text("Ventas", style = MaterialTheme.typography.headlineMedium); Text("Cada venta, una historia que crece", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            item { SoftCard {
                Icon(Icons.Outlined.ReceiptLong, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp)); Text("Tu actividad comercial", style = MaterialTheme.typography.titleMedium)
                Text(formatAsCurrency(sales.sumOf { it.montoAbonado.toDouble() }), style = MaterialTheme.typography.headlineLarge)
                Text("${sales.size} ventas registradas · todos tus clientes", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            } }
            item { AnalysisShortcutCard(onAnalysis) }
            item { OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().testTag("sales-search"), singleLine = true,
                label = { Text("Buscar venta") }, placeholder = { Text("Cliente, importe o nota") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, shape = MaterialTheme.shapes.large) }
            if (visible.isEmpty()) item { SoftCard { Text(if (sales.isEmpty()) "Tu próxima venta empieza aquí" else "Sin coincidencias", style = MaterialTheme.typography.titleMedium)
                Text(if (sales.isEmpty()) "Registra una venta para ver aquí su importe, fecha y cliente." else "Prueba con otro nombre o importe.") } }
            itemsIndexed(visible, key = { i, sale -> "${sale.id}:$i" }) { i, sale ->
                if (i == 0 || movementDate(visible[i - 1].fechaAbono) != movementDate(sale.fechaAbono))
                    Text(movementDayLabel(sale.fechaAbono), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 10.dp))
                MovementCard(sale.toMovement(), onOpen = { val client = saleClient(sale, clients); if (client == null) unavailable = true else onClient(client) })
            }
        }
    }
}
