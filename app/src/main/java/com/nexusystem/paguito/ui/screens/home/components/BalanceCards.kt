package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.utils.formatAsCurrency

@Composable
fun BalanceCarousel(
    pagosList: List<AbonosDelMes>, sumary1: DeudoresSummary, showBalance: Boolean,
    onToggleBalance: () -> Unit, onAddPaymentClick: () -> Unit,
    overdueCount: Int = 0, productCount: Int = 0
) {
    HomeOverviewCard(pagosList, sumary1, productCount, showBalance, onToggleBalance, onAddPaymentClick)
}

@Composable
fun MainBalanceCard(
    title: String, amount: String, decimals: String = "", count1: String, label1: String,
    count2: String, label2: String, backgroundColor: Color, icon: ImageVector,
    showBalance: Boolean, onToggleBalance: () -> Unit, onAddPaymentClick: () -> Unit
) {
    Card(shape = MaterialTheme.shapes.extraLarge, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))))
                .padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = onToggleBalance) {
                    Icon(if (showBalance) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (showBalance) "Ocultar saldo" else "Mostrar saldo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(if (showBalance) amount + decimals else "$ ••••••",
                style = MaterialTheme.typography.displaySmall, color = MaterialTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp), modifier = Modifier.fillMaxWidth()) {
                InfoItem(Icons.Outlined.AccountBalanceWallet, count1, label1)
            }
            Button(
                onClick = onAddPaymentClick, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Registrar pago", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, count: String, label: String) {
    Column {
        Text(count, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
