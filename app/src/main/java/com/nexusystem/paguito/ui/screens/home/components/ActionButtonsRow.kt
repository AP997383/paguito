package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.domain.data.DeudoresSummary

private val BluePrimary = Color(0xFF1AAF83)

@Composable
fun ActionButtonsRow(
    openWebsite: () -> Unit,
    registerPayment: () -> Unit,
    registerProduct: () -> Unit,
    registerDebtor: () -> Unit,
    registerCampaing: () -> Unit,
    registerNewSell: () -> Unit,
    sumary1: DeudoresSummary?,
    isSucriptionActive: Boolean,
    showDialogLimitFree: () -> Unit,
    numeroProductos: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            icon = Icons.Outlined.Language,
            text = "Sitio\nWeb",
            iconTint = Color(0xFF2196F3),
            onClick = openWebsite
        )

        ActionButton(
            icon = Icons.Outlined.AccountBalanceWallet,
            text = "Registrar\nPago",
            iconTint = MaterialTheme.colorScheme.onSurface,
            onClick = registerPayment
        )

        ActionButton(
            icon = Icons.Outlined.PersonAdd,
            text = "Nuevo\nDeudor",
            iconTint = BluePrimary,
            onClick = {
                if ((sumary1?.totalDeudores ?: 0) >= 10 && !isSucriptionActive) {
                    showDialogLimitFree()
                } else {
                    registerDebtor()
                }
            }
        )

        ActionButton(
            icon = Icons.Outlined.LocalGroceryStore,
            text = "Nuevo\nProducto",
            iconTint = Color(0xFF40C4FF),
            onClick = {
                if (numeroProductos >= 10 && !isSucriptionActive) {
                    showDialogLimitFree()
                } else {
                    registerProduct()
                }
            }
        )

        ActionButton(
            icon = Icons.Outlined.AttachMoney,
            text = "Agregar\nVenta",
            iconTint = Color(0xFF10B981),
            onClick = registerNewSell
        )
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(96.dp)
            .height(122.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(25.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}