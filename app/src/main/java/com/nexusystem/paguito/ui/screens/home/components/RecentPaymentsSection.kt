package com.nexusystem.paguito.ui.screens.home.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.ui.screens.deudores.IconBgGray
import com.nexusystem.paguito.ui.screens.deudores.TextDark2
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.formatFecha

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecentPaymentsSection(
    pagos: List<PagoConNombre?>,
    seeAllPayments: () -> Unit
) {
    Spacer(modifier = Modifier.height(20.dp))

    Text(
        "Pagos Recientes",
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )

    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            pagos.forEach { pago ->
                if (pago != null) {
                    val stringTypePayment = when (pago.tipoPago) {
                        1 -> "Efectivo"
                        2 -> "Transferencia"
                        3 -> "Tarjeta"
                        else -> "Efectivo"
                    }

                    RecentPaymentItem(
                        name = pago.nameDeudor ?: "",
                        details = "${formatFecha(pago.fechaAbono)} • $stringTypePayment",
                        amount = pago.montoAbonado.toString(),
                        isIngreso = pago.isIngreso
                    )

                    HorizontalDivider(
                        color = BorderLight,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { seeAllPayments() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Ver historial completo",
                    color = TextGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun RecentPaymentItem(
    name: String,
    details: String,
    amount: String,
    isIngreso: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(
                    1.dp,
                    com.nexusystem.paguito.ui.screens.deudores.BorderColor,
                    CircleShape
                )
                .background(IconBgGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isIngreso) Icons.Outlined.Download else Icons.Outlined.LocalMall,
                contentDescription = null,
                tint = TextDark2,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(details, fontSize = 12.sp, color = TextGray)
        }

        Text(
            formatAsCurrency(amount),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}