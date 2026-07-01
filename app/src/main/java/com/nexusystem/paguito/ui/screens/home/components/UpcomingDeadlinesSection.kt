package com.nexusystem.paguito.ui.screens.home.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.ui.screens.analisis.BluePrimary
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.getDaysUntilNextPayment

val TextGray = Color(0xFF6B7280)
val BorderLight = Color(0xFFF3F4F6)
val RedUrgent = Color(0xFFEF4444)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpcomingDeadlinesSection(
    deudores: List<DeudoresEntity?>,
    seeAllDeudores: () -> Unit,
    openDetailDeudor: (DeudoresEntity) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "Próximos Vencimientos",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            "Ver todos",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary,
            modifier = Modifier.clickable { seeAllDeudores() }
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        deudores.forEach { deudor ->
            item {
                if (deudor != null) {
                    UpcomingCard(
                        openDetailDeudor = { openDetailDeudor(deudor) },
                        name = deudor.nombre,
                        amount = deudor.montoActualAdeudado.toString(),
                        date = deudor.fechaInicialDeuda,
                        isUrgent = true,
                        periodisity = deudor.periodicidad
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpcomingCard(
    openDetailDeudor: () -> Unit,
    name: String,
    amount: String,
    date: String,
    isUrgent: Boolean,
    periodisity: String
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable { openDetailDeudor() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BorderLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, null, tint = TextGray)
                }

                if (isUrgent) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(RedUrgent)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                "Saldo restante: ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                formatAsCurrency(amount),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isUrgent) RedUrgent else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderLight)
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                "Siguiente pago en: ",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    null,
                    tint = if (isUrgent) RedUrgent else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(14.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    "${getDaysUntilNextPayment(date, periodisity)} días",
                    fontSize = 12.sp,
                    color = if (isUrgent) RedUrgent else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}