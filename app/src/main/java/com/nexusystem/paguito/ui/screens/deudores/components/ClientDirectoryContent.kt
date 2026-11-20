package com.nexusystem.paguito.ui.screens.deudores.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.components.SoftCard
import com.nexusystem.paguito.ui.components.paguitoBackground
import com.nexusystem.paguito.utils.formatAsCurrency

enum class ClientFilter(val label: String) { All("Todos"), Balance("Con saldo"), Settled("Al día"), Sync("Por sincronizar") }

fun filterClients(clients: List<DeudoresEntity>, query: String, filter: ClientFilter, largestBalanceFirst: Boolean): List<DeudoresEntity> {
    val term = query.trim()
    val matching = clients.filter { client ->
        (term.isEmpty() || listOf(client.nombre, client.telefono, client.correo).any { it.contains(term, ignoreCase = true) }) &&
            when (filter) {
                ClientFilter.All -> true
                ClientFilter.Balance -> client.montoActualAdeudado > 0f
                ClientFilter.Settled -> client.montoActualAdeudado <= 0f
                ClientFilter.Sync -> !client.inRemote
            }
    }
    return if (largestBalanceFirst) matching.sortedByDescending { it.montoActualAdeudado } else matching.sortedBy { it.nombre.lowercase() }
}

@Composable
fun ClientDirectoryContent(
    clients: List<DeudoresEntity>, imageUser: String = "",
    onProfile: () -> Unit, onAddClient: () -> Unit,
    onOpenClient: (DeudoresEntity) -> Unit, onPayment: (DeudoresEntity) -> Unit,
    onSale: (DeudoresEntity) -> Unit, onEdit: (DeudoresEntity) -> Unit,
    onDelete: (DeudoresEntity) -> Unit, adContent: (@Composable () -> Unit)? = null
) {
    var search by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(ClientFilter.All) }
    var compact by rememberSaveable { mutableStateOf(true) }
    var largestFirst by rememberSaveable { mutableStateOf(false) }
    val filtered = remember(clients, search, filter, largestFirst) { filterClients(clients, search, filter, largestFirst) }
    val pending = clients.count { it.montoActualAdeudado > 0f }
    val colors = MaterialTheme.colorScheme
    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddClient, icon = { Icon(Icons.Outlined.PersonAdd, null) },
                text = { Text("Nuevo cliente") }, containerColor = colors.primary, contentColor = colors.onPrimary,
                modifier = Modifier.padding(bottom = 116.dp))
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize().testTag("client-directory").paguitoBackground(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 200.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Tus clientes", style = MaterialTheme.typography.headlineMedium)
                        Text("Conecta, cobra y sigue creciendo", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    FilledTonalIconButton(onClick = onProfile, modifier = Modifier.size(48.dp)) {
                        AsyncImage(model = imageUser.ifBlank { null } ?: R.drawable.avatar, contentDescription = "Abrir mi perfil",
                            modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    }
                }
            }
            item {
                SoftCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.PeopleAlt, null, tint = colors.primary)
                        Text("Tu cartera, de un vistazo", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(formatAsCurrency(clients.sumOf { it.montoActualAdeudado.coerceAtLeast(0f).toDouble() }.toFloat()), style = MaterialTheme.typography.headlineLarge)
                    Text("Saldo pendiente por cobrar", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Column { Text(clients.size.toString(), style = MaterialTheme.typography.titleMedium); Text("clientes", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
                        Column { Text(pending.toString(), style = MaterialTheme.typography.titleMedium); Text("con saldo", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
                        Column { Text((clients.size - pending).toString(), style = MaterialTheme.typography.titleMedium); Text("al día", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
                    }
                }
            }
            item {
                OutlinedTextField(value = search, onValueChange = { search = it }, singleLine = true,
                    placeholder = { Text("Nombre, teléfono o correo", style = MaterialTheme.typography.bodyMedium) },
                    leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    trailingIcon = { if (search.isNotEmpty()) IconButton(onClick = { search = "" }) { Icon(Icons.Outlined.Close, "Limpiar búsqueda") } },
                    modifier = Modifier.fillMaxWidth().testTag("client-search"), shape = MaterialTheme.shapes.large,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = colors.outlineVariant.copy(alpha = 0.6f), unfocusedContainerColor = colors.surface))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ClientFilter.entries.forEach { option ->
                        val count = when (option) { ClientFilter.All -> clients.size; ClientFilter.Balance -> pending; ClientFilter.Settled -> clients.size - pending; ClientFilter.Sync -> clients.count { !it.inRemote } }
                        FilterChip(selected = filter == option, onClick = { filter = option }, label = { Text("${option.label} · $count") })
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${filtered.size} ${if (filtered.size == 1) "cliente" else "clientes"}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    IconToggleButton(checked = !compact, onCheckedChange = { compact = !it }) {
                        Icon(if (compact) Icons.Outlined.ViewAgenda else Icons.Outlined.ViewList, if (compact) "Vista amplia" else "Vista compacta")
                    }
                    TextButton(onClick = { largestFirst = !largestFirst }) {
                        Icon(Icons.Outlined.Sort, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (largestFirst) "Mayor saldo" else "Nombre A–Z", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
            if (filtered.isEmpty()) item {
                SoftCard {
                    Icon(if (clients.isEmpty()) Icons.Outlined.PeopleAlt else Icons.Outlined.SearchOff, null, tint = colors.primary, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(if (clients.isEmpty()) "Aquí empieza tu cartera" else "No encontramos coincidencias", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(if (clients.isEmpty()) "Agrega tu primer cliente y lleva sus pagos en un solo lugar." else "Prueba con otro nombre o cambia el filtro.",
                        style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    TextButton(onClick = if (clients.isEmpty()) onAddClient else { { search = ""; filter = ClientFilter.All } }) {
                        Text(if (clients.isEmpty()) "Agregar cliente" else "Limpiar filtros")
                    }
                }
            }
            itemsIndexed(filtered, key = { index, client -> client.idRemoteDatabase.takeIf { it.isNotBlank() } ?: "local:${client.id}:${client.nombre}:$index" }) { index, client ->
                ClientCard(client, { onOpenClient(client) }, { onPayment(client) }, { onSale(client) }, { onEdit(client) }, { onDelete(client) }, compact = compact)
                if (adContent != null && ((index + 1) % 5 == 0 || index == filtered.lastIndex)) {
                    Spacer(Modifier.height(12.dp))
                    adContent()
                }
            }
        }
    }
}

@Composable
fun ClientCard(client: DeudoresEntity, onOpen: () -> Unit, onPayment: () -> Unit, onSale: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, compact: Boolean = false) {
    val colors = MaterialTheme.colorScheme
    var menu by remember { mutableStateOf(false) }
    val initials = client.nombre.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.take(2).joinToString("") { it.take(1).uppercase() }.ifBlank { "?" }
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth().testTag("client-${client.idRemoteDatabase.ifBlank { client.nombre }}"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.surface.copy(alpha = 0.98f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.45f))) {
        Column(Modifier.fillMaxWidth().padding(if (compact) 14.dp else 18.dp), verticalArrangement = Arrangement.spacedBy(if (compact) 10.dp else 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(if (compact) 40.dp else 48.dp).background(colors.primary.copy(alpha = 0.09f), MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                    Text(initials, style = MaterialTheme.typography.titleMedium, color = colors.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(client.nombre.ifBlank { "Sin nombre" }, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(client.telefono.takeIf { it.isNotBlank() && it != "=" } ?: client.correo.ifBlank { "Sin contacto" },
                        style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreHoriz, "Opciones de ${client.nombre}") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Editar contacto") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { menu = false; onEdit() })
                        DropdownMenuItem(text = { Text("Eliminar cliente", color = colors.error) }, leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = colors.error) }, onClick = { menu = false; onDelete() })
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(if (client.montoActualAdeudado > 0f) "Saldo pendiente" else "Sin saldo pendiente", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    Text(formatAsCurrency(client.montoActualAdeudado), style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                        color = if (client.montoActualAdeudado > 0f) colors.onSurface else colors.primary)
                }
                Icon(if (client.inRemote) Icons.Outlined.CloudDone else Icons.Outlined.CloudUpload,
                    contentDescription = if (client.inRemote) "Sincronizado" else "Pendiente de sincronizar", tint = colors.onSurfaceVariant, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = colors.primary, modifier = Modifier.size(18.dp))
            }
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.4f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilledTonalButton(onClick = onPayment, modifier = Modifier.weight(1f).heightIn(min = 48.dp).semantics { contentDescription = "Registrar pago de ${client.nombre}" }, shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Outlined.Payments, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Pago")
                }
                OutlinedButton(onClick = onSale, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = MaterialTheme.shapes.medium) {
                    Icon(Icons.Outlined.ShoppingBag, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Venta")
                }
            }
        }
    }
}
