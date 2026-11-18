package com.nexusystem.paguito.ui.screens.analisis.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.ui.components.SoftCard
import com.nexusystem.paguito.utils.formatAsCurrency
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class DailyPaymentPoint(val day: Int, val collections: Float, val sales: Float)

fun paymentMonths(payments: List<AbonosDelMes>, current: YearMonth = YearMonth.now()): List<String> =
    (listOf(current.toString()) + payments.mapNotNull {
        runCatching { YearMonth.from(LocalDate.parse(it.fechaAbono.take(10))).toString() }.getOrNull()
    }).distinct().sortedDescending()

/** Aggregate only the selected month; blank and malformed dates never become invented points. */
fun dailyPaymentPoints(payments: List<AbonosDelMes>, month: String): List<DailyPaymentPoint> {
    val period = runCatching { YearMonth.parse(month) }.getOrElse { YearMonth.now() }
    val totals = payments.mapNotNull { payment ->
        val date = runCatching { LocalDate.parse(payment.fechaAbono.take(10)) }.getOrNull()
        if (date != null && YearMonth.from(date) == period) date.dayOfMonth to payment else null
    }.groupBy({ it.first }, { it.second })
    return (1..period.lengthOfMonth()).map { day ->
        val entries = totals[day].orEmpty()
        DailyPaymentPoint(day,
            entries.filter { it.isIngreso }.sumOf { it.montoAbonado.toDouble() }.toFloat(),
            entries.filterNot { it.isIngreso }.sumOf { it.montoAbonado.toDouble() }.toFloat())
    }
}

@Composable
fun PaymentTrendCard(
    payments: List<AbonosDelMes>,
    selectedMonth: String,
    availableMonths: List<String>,
    onMonthSelected: (String) -> Unit,
    enabled: Boolean = true
) {
    val colors = MaterialTheme.colorScheme
    val points = remember(payments, selectedMonth, enabled) { dailyPaymentPoints(if (enabled) payments else emptyList(), selectedMonth) }
    var showSales by rememberSaveable { mutableStateOf(false) }
    var selectedDay by remember(selectedMonth, points, showSales) { mutableStateOf<Int?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    val values = remember(selectedMonth, points, showSales) { points.map { if (showSales) it.sales else it.collections } }
    val total = values.sum()
    val amount = selectedDay?.let { values[it] } ?: total
    val monthLabel = runCatching { YearMonth.parse(selectedMonth).format(DateTimeFormatter.ofPattern("MMM yyyy", Locale("es", "MX"))) }.getOrDefault(selectedMonth)
    SoftCard {
        Box(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.13f)
                .then(if (!enabled) Modifier.clearAndSetSemantics {} else Modifier)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Waves, null, tint = colors.primary)
                    Spacer(Modifier.width(10.dp))
                    Text("Tu negocio en movimiento", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(6.dp))
                Text("Así se comportan tus cobros y ventas", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = !showSales, enabled = enabled, onClick = { showSales = false }, label = { Text("Cobros") })
                    FilterChip(selected = showSales, enabled = enabled, onClick = { showSales = true }, label = { Text("Ventas") })
                    Spacer(Modifier.weight(1f))
                    Box {
                        TextButton(onClick = { menuExpanded = true }, enabled = enabled, contentPadding = PaddingValues(4.dp)) {
                            Text(monthLabel, style = MaterialTheme.typography.labelSmall)
                            Icon(Icons.Outlined.ExpandMore, null, Modifier.size(16.dp))
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            availableMonths.forEach { month ->
                                DropdownMenuItem(text = { Text(month) }, onClick = { onMonthSelected(month); menuExpanded = false })
                            }
                        }
                    }
                }
                Text(formatAsCurrency(amount), style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.testTag("trend-amount"))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (selectedDay == null) "${if (showSales) "Ventas" else "Cobros"} del mes" else "Día ${selectedDay!! + 1} · $monthLabel",
                        style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant,
                        modifier = Modifier.weight(1f).testTag("trend-selection"))
                    if (selectedDay != null) TextButton(onClick = { selectedDay = null }, enabled = enabled) { Text("Ver total") }
                }
                Spacer(Modifier.height(12.dp))
                SmoothPaymentChart(values, selectedDay, enabled, onSelect = { selectedDay = it },
                    modifier = Modifier.fillMaxWidth().height(230.dp))
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Outlined.TouchApp, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Text(if (total == 0f) "Aún no hay ${if (showSales) "ventas" else "cobros"} en este mes" else "Toca la curva para consultar un día",
                        style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
            }
            if (!enabled) Column(Modifier.matchParentSize().padding(16.dp).clearAndSetSemantics {
                contentDescription = "Gráficas disponibles con Premium"
            }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Surface(shape = MaterialTheme.shapes.large, color = colors.primaryContainer) {
                    Icon(Icons.Outlined.Lock, null, tint = colors.onPrimaryContainer, modifier = Modifier.padding(16.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("Descubre tus tendencias", style = MaterialTheme.typography.titleMedium)
                Text("Las gráficas están incluidas en Premium", style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun SmoothPaymentChart(values: List<Float>, selected: Int?, enabled: Boolean = true, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val density = LocalDensity.current
    val left = with(density) { 52.dp.toPx() }
    val paint = remember(colors.onSurfaceVariant, density) {
        Paint(Paint.ANTI_ALIAS_FLAG).apply { color = colors.onSurfaceVariant.toArgb(); textSize = with(density) { 10.sp.toPx() } }
    }
    val safeValues = values.map { if (it.isFinite()) it.coerceAtLeast(0f) else 0f }
    val maximum = (safeValues.maxOrNull() ?: 0f).coerceAtLeast(1f)
    val chartDescription = if (maximum <= 1f && safeValues.all { it == 0f }) "Gráfica sin movimientos" else "Gráfica diaria, ${values.size} días"
    Canvas(modifier = modifier.testTag("payment-trend-chart").semantics {
        contentDescription = chartDescription
        if (enabled && values.isNotEmpty()) customActions = listOf(
            CustomAccessibilityAction("Consultar día siguiente") { onSelect(((selected ?: -1) + 1).coerceAtMost(values.lastIndex)); true },
            CustomAccessibilityAction("Consultar día anterior") { onSelect(((selected ?: 1) - 1).coerceAtLeast(0)); true }
        )
    }.pointerInput(values, enabled) {
        if (enabled) detectTapGestures { offset ->
            if (values.isNotEmpty()) onSelect((((offset.x - left) / (size.width - left - 12.dp.toPx()).coerceAtLeast(1f)) * (values.size - 1)).toInt().coerceIn(0, values.lastIndex))
        }
    }) {
        val top = 12.dp.toPx()
        val bottom = size.height - 28.dp.toPx()
        val right = size.width - 12.dp.toPx()
        val height = (bottom - top).coerceAtLeast(1f)
        val width = (right - left).coerceAtLeast(1f)
        fun point(index: Int) = Offset(left + width * index / (safeValues.size - 1).coerceAtLeast(1), bottom - height * safeValues[index] / maximum)
        for (tick in 0..4) {
            val y = bottom - height * tick / 4f
            drawLine(colors.outlineVariant.copy(alpha = 0.35f), Offset(left, y), Offset(right, y), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 5.dp.toPx())))
            val value = maximum * tick / 4f
            val label = when {
                value >= 1000f -> "$" + String.format(Locale.US, "%.1fk", value / 1000f)
                maximum < 4f && value > 0f -> "$" + String.format(Locale.US, "%.2f", value)
                else -> "$${value.toInt()}"
            }
            drawContext.canvas.nativeCanvas.drawText(label, 0f, y + paint.textSize / 3, paint)
        }
        if (safeValues.isEmpty()) return@Canvas
        fun curve(yOffset: Float = 0f): Path = Path().apply {
            val first = point(0)
            moveTo(first.x, first.y + yOffset)
            for (index in 1..safeValues.lastIndex) {
                val previous = point(index - 1)
                val next = point(index)
                val middle = (previous.x + next.x) / 2f
                cubicTo(middle, previous.y + yOffset, middle, next.y + yOffset, next.x, next.y + yOffset)
            }
        }
        val area = curve().apply { lineTo(point(safeValues.lastIndex).x, bottom); lineTo(left, bottom); close() }
        drawPath(area, Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.15f), colors.primary.copy(alpha = 0.01f)), top, bottom))
        listOf(-6f, -3f, 3f, 6f).forEach { offset ->
            drawPath(curve(offset.dp.toPx()), colors.primary.copy(alpha = 0.10f), style = Stroke(1.dp.toPx()))
        }
        drawPath(curve(), colors.primary, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
        if (selected != null && selected in safeValues.indices) {
            val p = point(selected)
            drawLine(colors.primary.copy(alpha = 0.25f), Offset(p.x, top), Offset(p.x, bottom), strokeWidth = 1.dp.toPx())
            drawCircle(colors.primary.copy(alpha = 0.13f), 10.dp.toPx(), p)
            drawCircle(colors.surface, 5.dp.toPx(), p)
            drawCircle(colors.primary, 5.dp.toPx(), p, style = Stroke(2.dp.toPx()))
        }
        listOf(0, safeValues.lastIndex / 4, safeValues.lastIndex / 2, safeValues.lastIndex * 3 / 4, safeValues.lastIndex).distinct().forEach { index ->
            val label = (index + 1).toString()
            drawContext.canvas.nativeCanvas.drawText(label, point(index).x - paint.measureText(label) / 2, size.height - 4.dp.toPx(), paint)
        }
    }
}
