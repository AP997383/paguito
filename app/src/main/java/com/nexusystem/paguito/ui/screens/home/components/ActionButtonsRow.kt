package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.LocalGroceryStore
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Event
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.domain.data.DeudoresSummary

/** Datos de presentación de un acceso; su callback nunca se reutiliza para otro destino. */
private data class QuickActionSpec(val icon: ImageVector, val title: String, val color: Color, val onClick: () -> Unit)

/** Siete accesos en filas de cuatro y tres; el texto ampliado utiliza filas de dos. */
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
    numeroProductos: Int,
    seeAllClients: (() -> Unit)? = null,
    seeUpcoming: () -> Unit = {}
) {
    // Estos dos accesos conservan el azul aunque el usuario personalice el acento principal.
    val shortcutBlue = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFFA9CCFF) else Color(0xFF2866AD)
    val actions = listOf(
        QuickActionSpec(Icons.Outlined.Language, "Sitio\nWeb", shortcutBlue, openWebsite),
        QuickActionSpec(Icons.Outlined.AccountBalanceWallet, "Registrar\nPago", MaterialTheme.colorScheme.primary, registerPayment),
        QuickActionSpec(Icons.Outlined.PersonAdd, "Nuevo\nDeudor", MaterialTheme.colorScheme.primary) {
            if ((sumary1?.totalDeudores ?: 0) >= 10 && !isSucriptionActive) showDialogLimitFree() else registerDebtor()
        },
        QuickActionSpec(Icons.Outlined.LocalGroceryStore, "Nuevo\nProducto", shortcutBlue) {
            if (numeroProductos >= 10 && !isSucriptionActive) showDialogLimitFree() else registerProduct()
        },
        QuickActionSpec(Icons.Outlined.AttachMoney, "Agregar\nVenta", MaterialTheme.colorScheme.primary, registerNewSell),
        QuickActionSpec(Icons.Outlined.PeopleAlt, "Mis\nClientes", MaterialTheme.colorScheme.primary, seeAllClients ?: {}),
        QuickActionSpec(Icons.Outlined.Event, "Próximos\ncobros", MaterialTheme.colorScheme.primary, seeUpcoming)
    )
    val rows = if (LocalDensity.current.fontScale > 1.3f) actions.chunked(2) else listOf(actions.take(4), actions.drop(4))
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { item ->
                    ActionButton(modifier = Modifier.weight(1f), icon = item.icon, text = item.title, iconTint = item.color, onClick = item.onClick)
                }
            }
        }
    }
}

@Composable
fun ActionButton(
    icon: ImageVector,
    text: String,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.heightIn(min = 100.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(iconTint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
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