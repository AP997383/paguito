package com.nexusystem.paguito.ui.screens.deudores

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- COLORES ---
val BluePrimary = Color(0xFF3B82F6)
val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF6B7280)
val BorderLight = Color(0xFFE5E7EB)
val RedUrgent = Color(0xFFEF4444)
val RedBackground = Color(0xFFFEE2E2)
val GrayBackground = Color(0xFFF3F4F6)

// --- MODELO DE DATOS ---
enum class DebtorStatus { OVERDUE, UPCOMING, UP_TO_DATE }

data class Debtor(
    val name: String,
    val phone: String,
    val amount: Double,
    val status: DebtorStatus,
    val dateText: String
)

// Datos de prueba basados en tu imagen
val sampleDebtors = listOf(
    Debtor("Juan Pérez", "+52 55 1234 5678", 1500.00, DebtorStatus.OVERDUE, "Venció: 12 Oct 2023"),
    Debtor("María Gómez", "+52 55 9876 5432", 450.00, DebtorStatus.UPCOMING, "Vence: 25 Oct 2023"),
    Debtor("Carlos López", "+52 55 4567 8901", 2100.00, DebtorStatus.UP_TO_DATE, "Vence: 05 Nov 2023"),
    Debtor("Ana Martínez", "+52 55 3456 7890", 800.00, DebtorStatus.OVERDUE, "Venció: 01 Oct 2023"),
    Debtor("Roberto Sánchez", "+52 55 2345 6789", 320.00, DebtorStatus.UPCOMING, "Vence: 28 Oct 2023")
)

// --- PANTALLA PRINCIPAL ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtorsScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Todos") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                modifier = Modifier.padding(end = 20.dp, bottom = 100.dp),
                onClick = { /* Acción nuevo deudor */ },
                containerColor = BluePrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Deudor")
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Título
            Text(
                text = "Deudores",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
            )

            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar deudor por nombre...", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary,
                    unfocusedBorderColor = BorderLight,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtros (Chips)
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { FilterChipItem("Todos", 5, selectedFilter == "Todos") { selectedFilter = "Todos" } }
                item { FilterChipItem("Vencidos", 2, selectedFilter == "Vencidos") { selectedFilter = "Vencidos" } }
                item { FilterChipItem("Próximos", 2, selectedFilter == "Próximos") { selectedFilter = "Próximos" } }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = BorderLight, thickness = 1.dp)

            // Lista de Deudores
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(sampleDebtors) { debtor ->
                    DebtorListItem(debtor = debtor)
                    Divider(color = BorderLight, thickness = 1.dp)
                }
                item { Spacer(modifier = Modifier.height(80.dp)) } // Espacio para que el FAB no tape el último item
            }
        }
    }
}

// --- COMPONENTES SECUNDARIOS ---

@Composable
fun FilterChipItem(text: String, count: Int, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) BluePrimary else Color.White
    val contentColor = if (isSelected) Color.White else TextGray
    val borderColor = if (isSelected) BluePrimary else BorderLight
    val badgeBgColor = if (isSelected) Color.White.copy(alpha = 0.2f) else GrayBackground

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBgColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = count.toString(), color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DebtorListItem(debtor: Debtor) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // --- Fila Superior: Info + Monto ---
        Row(modifier = Modifier.fillMaxWidth()) {
            // Avatar Placeholder (En una app real usa AsyncImage de Coil)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BorderLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextGray)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Datos del usuario
            Column(modifier = Modifier.weight(1f)) {
                Text(text = debtor.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextDark)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = debtor.phone, fontSize = 13.sp, color = TextGray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Badges de Estado y Fecha
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusBadge(debtor.status)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = TextGray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = debtor.dateText, fontSize = 12.sp, color = TextGray)
                }
            }

            // Monto Total
            Column(horizontalAlignment = Alignment.End) {
                val amountColor = if (debtor.status == DebtorStatus.OVERDUE) RedUrgent else TextDark
                Text(
                    text = "$${String.format("%.2f", debtor.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = amountColor
                )
                Text(text = "Total adeudado", fontSize = 11.sp, color = TextGray)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Fila Inferior: Botones de Acción ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ver detalles",
                fontSize = 14.sp,
                color = TextGray,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { /* Navegar a detalles */ }.padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { /* Registrar pago */ },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Registrar pago", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StatusBadge(status: DebtorStatus) {
    val (bgColor, textColor, icon, text) = when (status) {
        DebtorStatus.OVERDUE -> listOf(RedBackground, RedUrgent, Icons.Outlined.ErrorOutline, "Vencido")
        DebtorStatus.UPCOMING -> listOf(Color.Transparent, TextDark, Icons.Outlined.Schedule, "Próximo")
        DebtorStatus.UP_TO_DATE -> listOf(Color.Transparent, TextGray, null, "Al día")
    }

    Surface(
        color = bgColor as Color,
        shape = RoundedCornerShape(12.dp),
        border = if (status != DebtorStatus.OVERDUE) BorderStroke(1.dp, BorderLight) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon as ImageVector, contentDescription = null, tint = textColor as Color, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = text as String, color = textColor as Color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}