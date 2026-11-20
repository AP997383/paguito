package com.nexusystem.paguito.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.ui.components.*
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.utils.getDaysUntilNextPayment
import com.nexusystem.paguito.utils.formatAsCurrency

fun daysToClientPayment(client: DeudoresEntity): Long? = if (client.periodicidad !in listOf("S", "Q", "M")) null else runCatching { getDaysUntilNextPayment(client.fechaInicialDeuda, client.periodicidad) }.getOrNull()

@Composable
fun ListaClientesDeudaScreen(deudoresViewModel: DeudoresViewModel, onBackClick: () -> Unit, onClient: (DeudoresEntity) -> Unit = {}) {
    val clients by deudoresViewModel.deudores.collectAsState()
    LaunchedEffect(Unit) { deudoresViewModel.obtenerDeudores() }
    UpcomingClientsContent(clients.filterNotNull(), onBackClick, onClient)
}

@Composable
fun UpcomingClientsContent(clients: List<DeudoresEntity>, onBack: () -> Unit, onClient: (DeudoresEntity) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var onlyBalance by rememberSaveable { mutableStateOf(true) }
    val visible = clients.filter { (!onlyBalance || it.montoActualAdeudado > 0f) && (query.isBlank() || it.nombre.contains(query, true) || it.telefono.contains(query)) }.sortedBy { daysToClientPayment(it) ?: Long.MAX_VALUE }
    androidx.compose.material3.Scaffold(containerColor = MaterialTheme.colorScheme.background,
        topBar = { ScreenHeader("Próximos cobros", "Organiza tu día, cliente a cliente", onBack) }) { padding ->
    LazyColumn(Modifier.padding(padding).fillMaxSize().paguitoBackground(), contentPadding = PaddingValues(20.dp, 8.dp, 20.dp, 40.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SoftCard { Text("Tu agenda de cobranza", style = MaterialTheme.typography.titleLarge)
            Text("${clients.count { it.montoActualAdeudado > 0f }} clientes con saldo pendiente", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(14.dp)); Text(formatAsCurrency(clients.sumOf { it.montoActualAdeudado.coerceAtLeast(0f).toDouble() }), style = MaterialTheme.typography.headlineLarge) } }
        item { OutlinedTextField(query, { query = it }, label = { Text("Buscar cliente") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large)
            FilterChip(selected = onlyBalance, onClick = { onlyBalance = !onlyBalance }, label = { Text("Solo con saldo pendiente") }) }
        if (visible.isEmpty()) item { SoftCard { Text("No hay clientes para mostrar", style = MaterialTheme.typography.titleMedium); Text("Cambia la búsqueda o el filtro de saldo.") } }
        items(visible) { client -> ClientDueCard(client) { onClient(client) } }
    }
    }
}

@Composable
fun ClientDueCard(client: DeudoresEntity, onOpen: () -> Unit) {
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(client.nombre, style = MaterialTheme.typography.titleMedium)
            Text(formatAsCurrency(client.montoActualAdeudado), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            val days = daysToClientPayment(client)
            Text(when { days == null -> "Sin fecha programada"; days < 0 -> "Pago pendiente de regularizar"; days == 0L -> "Próximo pago: hoy"; else -> "Próximo pago en $days días" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable fun ClienteDeudaListItem(cliente: DeudoresEntity) { ClientDueCard(cliente) {} }
