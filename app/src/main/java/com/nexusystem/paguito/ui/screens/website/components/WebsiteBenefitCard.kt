package com.nexusystem.paguito.ui.screens.website.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.ui.theme.WebsiteGreen

@Composable
fun WebsiteBenefitCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            BenefitRow(
                icon = Icons.Outlined.Schedule,
                title = "Disponible las 24 horas"
            )

            HorizontalDivider(modifier = Modifier.padding(start = 42.dp))

            BenefitRow(
                icon = Icons.Outlined.Sync,
                title = "Productos sincronizados automáticamente"
            )

            HorizontalDivider(modifier = Modifier.padding(start = 42.dp))

            BenefitRow(
                icon = Icons.Outlined.Message,
                title = "Compra directa por WhatsApp"
            )

            HorizontalDivider(modifier = Modifier.padding(start = 42.dp))

            BenefitRow(
                icon = Icons.Outlined.Link,
                title = "Comparte un solo enlace"
            )

            HorizontalDivider(modifier = Modifier.padding(start = 42.dp))

            BenefitRow(
                icon = Icons.Outlined.AutoAwesome,
                title = "Sin conocimientos técnicos"
            )
        }
    }
}

@Composable
private fun BenefitRow(
    icon: ImageVector,
    title: String
) {
    Row(
        modifier = Modifier.padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(30.dp)
                .background(
                    WebsiteGreen.copy(alpha = 0.12f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = WebsiteGreen,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}