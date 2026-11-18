package com.nexusystem.paguito.ui.screens.productos.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.components.SoftCard
import com.nexusystem.paguito.ui.components.paguitoBackground
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.getThumbnailUrl

enum class StockFilter(val label: String) { All("Todos"), Available("Disponibles"), Low("1–3 unidades"), Empty("Agotados") }
enum class ProductSort(val label: String) { BestSelling("Más vendidos"), Name("Nombre A–Z"), Price("Mayor precio") }

fun filterProducts(products: List<PorductosEntity>, query: String, filter: StockFilter, sort: ProductSort): List<PorductosEntity> {
    val term = query.trim()
    val matching = products.filter {
        (term.isEmpty() || it.nombre.contains(term, true) || it.notasAdicionales.contains(term, true)) && when (filter) {
            StockFilter.All -> true; StockFilter.Available -> it.inventario > 0; StockFilter.Low -> it.inventario in 1..3; StockFilter.Empty -> it.inventario <= 0
        }
    }
    return when (sort) { ProductSort.BestSelling -> matching.sortedByDescending { it.ventas }; ProductSort.Name -> matching.sortedBy { it.nombre.lowercase() }; ProductSort.Price -> matching.sortedByDescending { it.precioConGanancia } }
}

@Composable
fun ProductDirectoryContent(products: List<PorductosEntity>, profileImage: String = "", onProfile: () -> Unit,
    onAdd: () -> Unit, onOpen: (PorductosEntity) -> Unit, onDelete: (PorductosEntity) -> Unit, adContent: (@Composable () -> Unit)? = null) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(StockFilter.All) }
    var sort by rememberSaveable { mutableStateOf(ProductSort.BestSelling) }
    var sortMenu by remember { mutableStateOf(false) }
    val matching = remember(products, query, filter, sort) { filterProducts(products, query, filter, sort) }
    val colors = MaterialTheme.colorScheme
    Scaffold(containerColor = colors.background, floatingActionButton = {
        ExtendedFloatingActionButton(onClick = onAdd, icon = { Icon(Icons.Outlined.Add, null) }, text = { Text("Nuevo producto") },
            containerColor = colors.primary, contentColor = colors.onPrimary, modifier = Modifier.padding(bottom = 116.dp))
    }) { _ ->
        LazyColumn(Modifier.fillMaxSize().paguitoBackground().testTag("product-directory"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 200.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Mis productos", style = MaterialTheme.typography.headlineMedium)
                        Text("Tu catálogo, siempre a mano", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    FilledTonalIconButton(onClick = onProfile, modifier = Modifier.size(48.dp)) {
                        AsyncImage(profileImage.ifBlank { null } ?: R.drawable.avatar, "Abrir mi perfil", Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    }
                }
            }
            item {
                SoftCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Outlined.Inventory2, null, tint = colors.primary)
                        Text("Tu inventario, de un vistazo", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf(Triple(products.size.toString(), "productos", colors.onSurface), Triple(products.sumOf { it.inventario.coerceAtLeast(0).toLong() }.toString(), "unidades", colors.onSurface), Triple(products.count { it.inventario <= 0 }.toString(), "agotados", colors.error)).forEach { (value, label, color) ->
                            Column(Modifier.weight(1f)) { Text(value, style = MaterialTheme.typography.headlineMedium, color = color); Text(label, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant) }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(query, { query = it }, modifier = Modifier.fillMaxWidth().testTag("product-search"), singleLine = true,
                    placeholder = { Text("Nombre o nota del producto", style = MaterialTheme.typography.bodyMedium) }, leadingIcon = { Icon(Icons.Outlined.Search, null) },
                    trailingIcon = { if (query.isNotBlank()) IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, "Limpiar búsqueda") } },
                    shape = MaterialTheme.shapes.large, colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = colors.surface, unfocusedBorderColor = colors.outlineVariant))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StockFilter.entries.forEach { option -> FilterChip(selected = option == filter, onClick = { filter = option }, label = { Text(option.label) }) }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${matching.size} productos", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                    Box {
                        TextButton(onClick = { sortMenu = true }) { Icon(Icons.Outlined.Sort, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text(sort.label, style = MaterialTheme.typography.labelMedium) }
                        DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                            ProductSort.entries.forEach { option -> DropdownMenuItem(text = { Text(option.label) }, onClick = { sort = option; sortMenu = false }) }
                        }
                    }
                }
            }
            if (matching.isEmpty()) item {
                SoftCard {
                    Icon(Icons.Outlined.Inventory2, null, tint = colors.primary, modifier = Modifier.size(36.dp))
                    Spacer(Modifier.height(12.dp))
                    Text(if (products.isEmpty()) "Aquí empieza tu catálogo" else "Sin productos para este filtro", style = MaterialTheme.typography.titleMedium)
                    Text(if (products.isEmpty()) "Agrega tu primer producto y empieza a organizar tus ventas." else "Prueba otra búsqueda o consulta todo el inventario.", style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                    TextButton(onClick = { if (products.isEmpty()) onAdd() else { query = ""; filter = StockFilter.All } }) { Text(if (products.isEmpty()) "Agregar producto" else "Limpiar filtros") }
                }
            }
            itemsIndexed(matching, key = { index, product -> product.idRemoteDatabase.takeIf { it.isNotBlank() } ?: "${product.id}:$index" }) { index, product ->
                CatalogProductCard(product, { onOpen(product) }, { onDelete(product) })
                if (adContent != null && ((index + 1) % 5 == 0 || index == matching.lastIndex)) { Spacer(Modifier.height(12.dp)); adContent() }
            }
        }
    }
}

@Composable
fun CatalogProductCard(product: PorductosEntity, onOpen: () -> Unit, onDelete: (() -> Unit)? = null) {
    val colors = MaterialTheme.colorScheme
    var menu by remember { mutableStateOf(false) }
    var originalImage by remember(product.urlFoto) { mutableStateOf(false) }
    val soldOut = product.inventario <= 0
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth().testTag("product-${product.idRemoteDatabase.ifBlank { product.nombre }}"), shape = MaterialTheme.shapes.extraLarge,
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.4f)), colors = CardDefaults.cardColors(containerColor = colors.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(Modifier.size(74.dp).background(colors.primary.copy(alpha = 0.06f), MaterialTheme.shapes.large).clip(MaterialTheme.shapes.large), contentAlignment = Alignment.Center) {
                    if (product.urlFoto.isBlank()) Icon(Icons.Outlined.Inventory2, null, tint = colors.primary, modifier = Modifier.size(30.dp))
                    else AsyncImage(model = if (originalImage) product.urlFoto else getThumbnailUrl(product.urlFoto), contentDescription = "Foto de ${product.nombre}",
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, onError = { if (!originalImage) originalImage = true })
                }
                Column(Modifier.weight(1f)) {
                    Text(product.nombre.ifBlank { "Producto sin nombre" }, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.height(4.dp))
                    Text(formatAsCurrency(product.precioConGanancia), style = MaterialTheme.typography.titleLarge)
                    Text("Precio de venta", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                if (onDelete != null) Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreHoriz, "Opciones de ${product.nombre}") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        DropdownMenuItem(text = { Text("Editar producto") }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { menu = false; onOpen() })
                        DropdownMenuItem(text = { Text("Eliminar producto", color = colors.error) }, leadingIcon = { Icon(Icons.Outlined.DeleteOutline, null, tint = colors.error) }, onClick = { menu = false; onDelete() })
                    }
                }
            }
            HorizontalDivider(color = colors.outlineVariant.copy(alpha = 0.4f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = if (soldOut) colors.errorContainer else colors.primary.copy(alpha = 0.07f), shape = MaterialTheme.shapes.small) {
                    Text(if (soldOut) "Agotado" else "${product.inventario} disponibles", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall,
                        color = if (soldOut) colors.onErrorContainer else colors.primary)
                }
                Spacer(Modifier.weight(1f))
                Text("${product.ventas} ventas", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}
