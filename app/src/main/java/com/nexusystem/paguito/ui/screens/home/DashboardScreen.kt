package com.nexusystem.paguito.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- COLORES ---
val BluePrimary = Color(0xFF3884FF)
val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF6B7280)
val BorderLight = Color(0xFFF3F4F6)
val RedUrgent = Color(0xFFEF4444)

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. App Bar
        TopBar()
        Divider(color = BorderLight, thickness = 1.dp)

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Tarjeta Principal
            MainBalanceCard()
            Spacer(modifier = Modifier.height(20.dp))

            // 3. Tarjetas de Acción (3 botones)
            ActionButtonsRow()
            Spacer(modifier = Modifier.height(24.dp))

            // 4. Carrusel de Próximos Vencimientos
            UpcomingDeadlinesSection()
            Spacer(modifier = Modifier.height(24.dp))

            // 5. Lista de Pagos Recientes
            RecentPaymentsSection()
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

// --- 1. TOP BAR ---
@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(BluePrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.AccountBalance, contentDescription = "Logo", tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "DeudaPro",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}

// --- 2. TARJETA PRINCIPAL ---
@Composable
fun MainBalanceCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BluePrimary)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Fondo simulado de marca de agua
            Icon(
                imageVector = Icons.Outlined.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .size(140.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = (-20).dp)
            )

            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Saldo Pendiente Total", color = Color.White, fontSize = 14.sp)
                    // Badge "Actualizado"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Actualizado", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Monto con decimales más pequeños
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)) {
                            append("$24,500")
                        }
                        withStyle(style = SpanStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) {
                            append(".00")
                        }
                    },
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.2f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Footer de la tarjeta
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = "Deudores", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("45") }
                            append(" Deudores")
                        },
                        color = Color.White, fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.width(16.dp))
                    Divider(color = Color.White.copy(alpha = 0.4f), modifier = Modifier.height(14.dp).width(1.dp))
                    Spacer(modifier = Modifier.width(16.dp))

                    Icon(Icons.Default.DateRange, contentDescription = "Vencidos", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = buildAnnotatedString {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("12") }
                            append(" Vencidos")
                        },
                        color = Color.White, fontSize = 13.sp
                    )
                }
            }
        }
    }
}

// --- 3. BOTONES DE ACCIÓN ---
@Composable
fun ActionButtonsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(icon = Icons.Outlined.AccountBalanceWallet, text = "Registrar\nPago", modifier = Modifier.weight(1f))
        ActionButton(icon = Icons.Outlined.PersonAdd, text = "Nuevo\nDeudor", iconTint = BluePrimary, modifier = Modifier.weight(1f))
        ActionButton(icon = Icons.Outlined.Campaign, text = "Campaña\nSMS", iconTint = Color(0xFF10B981), modifier = Modifier.weight(1f))
    }
}

@Composable
fun ActionButton(icon: ImageVector, text: String, iconTint: Color = TextDark, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextDark, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 16.sp)
        }
    }
}

// --- 4. CARRUSEL PRÓXIMOS VENCIMIENTOS ---
@Composable
fun UpcomingDeadlinesSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Próximos Vencimientos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
        Text("Ver todos >", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = BluePrimary, modifier = Modifier.clickable { })
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        item { UpcomingCard(name = "Carlos Mendoza", amount = "$1250.00", date = "Hoy", isUrgent = true) }
        item { UpcomingCard(name = "Ana Sofia Rios", amount = "$450.50", date = "Mañana", isUrgent = false) }
        item { UpcomingCard(name = "Empresa XYZ", amount = "$890.00", date = "En 3 días", isUrgent = false) }
    }
}

@Composable
fun UpcomingCard(name: String, amount: String, date: String, isUrgent: Boolean) {
    Card(
        modifier = Modifier.width(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Avatar Placeholder
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BorderLight), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextGray)
                }
                if (isUrgent) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(RedUrgent))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(amount, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (isUrgent) RedUrgent else TextDark)

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = if (isUrgent) RedUrgent else TextGray, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(date, fontSize = 12.sp, color = if (isUrgent) RedUrgent else TextGray, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// --- 5. PAGOS RECIENTES ---
@Composable
fun RecentPaymentsSection() {
    Text("Pagos Recientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark)
    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BorderLight),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            RecentPaymentItem("Luis Fernando Gomez", "12 Oct, 14:30 • Transferencia", "+$500.00")
            Divider(color = BorderLight, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            RecentPaymentItem("Valeria Torres", "11 Oct, 09:15 • Efectivo", "+$150.00")
            Divider(color = BorderLight, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            RecentPaymentItem("Taller Mecánico Ruiz", "10 Oct, 16:45 • Depósito", "+$1200.00")

            Divider(color = BorderLight, thickness = 1.dp)
            // Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Ver historial completo", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun RecentPaymentItem(name: String, details: String, amount: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Placeholder
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BorderLight), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, contentDescription = null, tint = TextGray)
        }
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Spacer(modifier = Modifier.height(2.dp))
            Text(details, fontSize = 12.sp, color = TextGray)
        }

        Text(amount, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextDark)
    }
}