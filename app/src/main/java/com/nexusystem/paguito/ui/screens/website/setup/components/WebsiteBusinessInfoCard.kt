package com.nexusystem.paguito.ui.screens.website.setup.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.nexusystem.paguito.ui.theme.WebsiteDanger
import com.nexusystem.paguito.ui.theme.WebsiteGreen

@Composable
fun WebsiteBusinessInfoCard(
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    subdomain: String,
    onSubdomainChange: (String) -> Unit,
    whatsapp: String,
    onWhatsappChange: (String) -> Unit,
    websiteURL: String,
    isCheckingSubdomain: Boolean,
    subdomainAvailable: Boolean?,
    subdomainMessage: String?,
    isEditingWebsite: Boolean,
    onCheckSubdomain: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Datos del sitio",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            WebsiteInputField(
                title = "Nombre del negocio",
                hint = "Ej. Zapatería Lupita",
                icon = WebsiteFieldIcons.Store,
                value = businessName,
                onValueChange = onBusinessNameChange
            )

            WebsiteInputField(
                title = "Nombre del enlace",
                hint = "Ej. zapateria-lupita",
                icon = WebsiteFieldIcons.Link,
                value = subdomain,
                onValueChange = { onSubdomainChange(it.lowercase().replace(" ", "-")) },
                enabled = !isEditingWebsite
            )

            if (isEditingWebsite) {
                Text(
                    text = "El enlace no se puede cambiar después de publicar el sitio.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                SubdomainAvailabilityStatus(
                    isChecking = isCheckingSubdomain,
                    isAvailable = subdomainAvailable,
                    message = subdomainMessage,
                    onCheck = onCheckSubdomain
                )
            }

            WebsiteInputField(
                title = "WhatsApp de contacto",
                hint = "Ej. 999 123 4567",
                icon = WebsiteFieldIcons.Phone,
                value = whatsapp,
                onValueChange = onWhatsappChange
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WebsiteGreen.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
                    .border(1.dp, WebsiteGreen.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "Tu enlace público",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = websiteURL,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = WebsiteGreen,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
private fun SubdomainAvailabilityStatus(
    isChecking: Boolean,
    isAvailable: Boolean?,
    message: String?,
    onCheck: () -> Unit
) {
    val color = when (isAvailable) {
        true -> WebsiteGreen
        false -> WebsiteDanger
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val title = when {
        isChecking -> "Validando enlace..."
        isAvailable == true -> "Enlace disponible"
        isAvailable == false -> "Enlace no disponible"
        else -> "Validar disponibilidad"
    }

    val background = when (isAvailable) {
        true -> WebsiteGreen.copy(alpha = 0.10f)
        false -> WebsiteDanger.copy(alpha = 0.10f)
        null -> MaterialTheme.colorScheme.background
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background, RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.20f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isChecking) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = WebsiteGreen,
                strokeWidth = 2.dp
            )
        } else {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .background(color.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (isAvailable) {
                        true -> Icons.Outlined.Check
                        false -> Icons.Outlined.Close
                        null -> Icons.Outlined.Link
                    },
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Black, color = color)

            if (!message.isNullOrBlank()) {
                Text(message, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
            }
        }

        Text(
            text = "Validar",
            modifier = Modifier
                .background(WebsiteGreen.copy(alpha = 0.12f), RoundedCornerShape(50))
                .clickable { onCheck() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            color = WebsiteGreen,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black
        )
    }
}