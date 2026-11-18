package com.nexusystem.paguito.ui.screens.home.components

import com.nexusystem.paguito.ui.components.paguitoBackground
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.nexusystem.paguito.ui.screens.perfil.BorderColor
import com.nexusystem.paguito.utils.dashedBorder

private object PaguitoThemeColors {
    val iconTint = Color(0xFF9CA3AF)
    val textSecondary = Color(0xFF6B7280)
}

@Composable
fun PaymentHistoryVerticalEmptyState(dynamicDescription: String, modifier: Modifier = Modifier) {
            com.nexusystem.paguito.ui.components.SoftCard(modifier.padding(vertical = 12.dp)) {
                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(12.dp))
                Text("Tu actividad, aquí", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(dynamicDescription, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

@Composable
fun PaymentHistoryEmptyState(dynamicDescription: String, modifier: Modifier = Modifier) {
            com.nexusystem.paguito.ui.components.SoftCard(modifier.padding(vertical = 12.dp)) {
                Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(12.dp))
                Text("Tus próximos cobros", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))
                Text(dynamicDescription, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

@Composable
fun DummyPaymentItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .dashedBorder(
                width = 2.dp,
                radius = 20.dp,
                color = PaguitoThemeColors.textSecondary,
                dashLength = 10.dp,
                gapLength = 8.dp
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .width(200.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = PaguitoThemeColors.iconTint,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .paguitoBackground()
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "----- -----",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "$0.00 mxn",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "-- / -- / --",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "---- ---- ---- --",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DummyVerticalPaymentItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .dashedBorder(
                width = 2.dp,
                radius = 16.dp,
                color = PaguitoThemeColors.textSecondary,
                dashLength = 10.dp,
                gapLength = 8.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "--- ---",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "-- / -- / --",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "--- --- ---",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "$0.00",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}