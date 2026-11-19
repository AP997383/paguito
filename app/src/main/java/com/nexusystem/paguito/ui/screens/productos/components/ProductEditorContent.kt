package com.nexusystem.paguito.ui.screens.productos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nexusystem.paguito.ui.components.*
import com.nexusystem.paguito.utils.formatAsCurrency

data class ProductEditorState(
    val name: String = "", val cost: String = "", val stock: String = "", val notes: String = "", val photo: String = "",
    val finalPrice: String = "0.00", val profit: String = "0.00", val multiplierMode: Boolean = true,
    val multiplier: Float = 2f, val percentage: Float = 100f, val editing: Boolean = false, val saving: Boolean = false,
    val canSave: Boolean = false
)

data class ProductEditorActions(
    val onName: (String) -> Unit, val onCost: (String) -> Unit, val onStock: (String) -> Unit, val onNotes: (String) -> Unit,
    val onPrice: (String) -> Unit, val onMode: (Boolean) -> Unit, val onMultiplier: (Float) -> Unit, val onPercentage: (Float) -> Unit,
    val onGallery: () -> Unit, val onCamera: () -> Unit, val onSave: () -> Unit, val onBack: () -> Unit
)

fun isValidProductInput(name: String, cost: String) = name.isNotBlank() && cost.isNotBlank() && (cost.toFloatOrNull() ?: 0f) > 0f

@Composable
fun ProductPhotoCard(photo: String?, onGallery: () -> Unit, onCamera: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var failed by remember(photo) { mutableStateOf(false) }
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(Modifier.size(88.dp).background(colors.primary.copy(alpha = 0.06f), MaterialTheme.shapes.large).clip(MaterialTheme.shapes.large), contentAlignment = Alignment.Center) {
                if (!photo.isNullOrBlank() && !failed) AsyncImage(photo, "Foto del producto", Modifier.fillMaxSize(), contentScale = ContentScale.Crop, onError = { failed = true })
                else Icon(Icons.Outlined.AddPhotoAlternate, null, tint = colors.primary, modifier = Modifier.size(36.dp))
            }
            Column(Modifier.weight(1f)) {
                Text("Foto del producto", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Haz que sea fácil reconocerlo", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text("Opcional", style = MaterialTheme.typography.labelSmall, color = colors.primary)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onCamera, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = MaterialTheme.shapes.medium) {
                Icon(Icons.Outlined.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Cámara")
            }
            OutlinedButton(onClick = onGallery, modifier = Modifier.weight(1f).heightIn(min = 48.dp), shape = MaterialTheme.shapes.medium) {
                Icon(Icons.Outlined.PhotoLibrary, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Galería")
            }
        }
    }
}

@Composable
fun ProductPricingCard(cost: String, price: String, profit: String, multiplierMode: Boolean, multiplier: Float, percentage: Float,
    onPrice: (String) -> Unit, onMode: (Boolean) -> Unit, onMultiplier: (Float) -> Unit, onPercentage: (Float) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val costValue = cost.toDoubleOrNull() ?: 0.0
    val priceValue = price.toDoubleOrNull() ?: 0.0
    val suggested = if (multiplierMode) costValue * multiplier else costValue * (1 + percentage / 100.0)
    val manualPrice = priceValue > 0 && kotlin.math.abs(priceValue - suggested) > 0.01
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Outlined.Sell, null, tint = colors.primary)
            Text("Precio y ganancia", style = MaterialTheme.typography.titleMedium)
        }
        Spacer(Modifier.height(6.dp))
        Text("Ajusta tu margen o escribe el precio de venta", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Costo de compra", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text(formatAsCurrency(cost), style = MaterialTheme.typography.titleLarge)
            }
            Column(Modifier.weight(1f)) {
                Text("Ganancia por unidad", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Text(formatAsCurrency(profit), style = MaterialTheme.typography.titleLarge,
                    color = if ((profit.toFloatOrNull() ?: 0f) < 0f) colors.error else colors.primary)
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(price, onPrice, label = { Text("Precio de venta") }, prefix = { Text("$") },
            singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().testTag("product-final-price"), shape = MaterialTheme.shapes.medium)
        if (manualPrice) {
            Spacer(Modifier.height(8.dp))
            Text("Precio personalizado", style = MaterialTheme.typography.labelMedium, color = colors.primary)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(selected = !multiplierMode, onClick = { onMode(false) }, label = { Text("Porcentaje") }, modifier = Modifier.weight(1f).heightIn(min = 48.dp))
            FilterChip(selected = multiplierMode, onClick = { onMode(true) }, label = { Text("Multiplicador") }, modifier = Modifier.weight(1f).heightIn(min = 48.dp))
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if (manualPrice) "Ajusta para recalcular el precio" else if (multiplierMode) "Multiplica tu costo" else "Ganancia sobre costo", style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(if (multiplierMode) "${String.format(java.util.Locale.US, "%.2f", multiplier)}×" else "${percentage.toInt()}%",
                style = MaterialTheme.typography.labelLarge, color = colors.primary)
        }
        if (multiplierMode) Slider(value = multiplier.coerceIn(1.5f, 3f), onValueChange = onMultiplier, valueRange = 1.5f..3f, steps = 2,
            modifier = Modifier.semantics { contentDescription = "Multiplicador de precio" })
        else Slider(value = percentage.coerceIn(50f, 250f), onValueChange = onPercentage, valueRange = 50f..250f, steps = 3,
            modifier = Modifier.semantics { contentDescription = "Porcentaje de ganancia" })
    }
}

@Composable
fun ProductEditorContent(state: ProductEditorState, actions: ProductEditorActions) {
    val colors = MaterialTheme.colorScheme
    Scaffold(modifier = Modifier.imePadding(), containerColor = colors.background,
        topBar = { ScreenHeader(if (state.editing) "Editar producto" else "Nuevo producto", "Dale forma a tu catálogo", actions.onBack) },
        bottomBar = {
            Surface(color = colors.background, shadowElevation = 4.dp) {
                Button(onClick = actions.onSave, enabled = state.canSave && !state.saving,
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp).heightIn(min = 56.dp).testTag("save-product"), shape = MaterialTheme.shapes.large) {
                    if (state.saving) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Icon(Icons.Outlined.Check, null, Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp)); Text(if (state.saving) "Guardando…" else "Guardar producto")
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding).paguitoBackground()
            .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            ProductPhotoCard(state.photo, actions.onGallery, actions.onCamera)
            SoftCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Inventory2, null, tint = colors.primary); Text("Datos del producto", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(state.name, actions.onName, label = { Text("Nombre del producto *") }, placeholder = { Text("Ej. Tenis deportivos") },
                    modifier = Modifier.fillMaxWidth().testTag("product-name"), singleLine = true, shape = MaterialTheme.shapes.medium)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(state.cost, actions.onCost, label = { Text("Costo de compra *") }, prefix = { Text("$") }, placeholder = { Text("0.00") },
                    modifier = Modifier.fillMaxWidth().testTag("product-cost"), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), shape = MaterialTheme.shapes.medium)
            }
            ProductPricingCard(state.cost, state.finalPrice, state.profit, state.multiplierMode, state.multiplier, state.percentage,
                actions.onPrice, actions.onMode, actions.onMultiplier, actions.onPercentage)
            SoftCard {
                Text("Inventario y notas", style = MaterialTheme.typography.titleMedium)
                Text("Completa estos datos si los necesitas", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(state.stock, actions.onStock, label = { Text("Existencias") }, placeholder = { Text("0") }, suffix = { Text("unidades") },
                    modifier = Modifier.fillMaxWidth().testTag("product-stock"), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), shape = MaterialTheme.shapes.medium)
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(state.notes, actions.onNotes, label = { Text("Notas adicionales") }, placeholder = { Text("Detalles que quieras recordar") },
                    modifier = Modifier.fillMaxWidth().testTag("product-notes"), minLines = 3, maxLines = 6, shape = MaterialTheme.shapes.medium)
            }
            Text("* Nombre y costo son obligatorios", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))
        }
    }
}
