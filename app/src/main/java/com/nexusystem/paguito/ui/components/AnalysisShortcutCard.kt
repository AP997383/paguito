package com.nexusystem.paguito.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun AnalysisShortcutCard(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().testTag("open-analysis"), shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.primaryContainer), border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.15f))) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(shape = MaterialTheme.shapes.medium, color = colors.primary.copy(alpha = 0.1f)) {
                Icon(Icons.Outlined.Insights, null, Modifier.padding(12.dp), tint = colors.primary)
            }
            Column(Modifier.weight(1f)) {
                Text("Análisis de tu negocio", style = MaterialTheme.typography.titleMedium, color = colors.onPrimaryContainer)
                Spacer(Modifier.height(4.dp))
                Text("Explora tus ventas, cobros y tendencias", style = MaterialTheme.typography.bodySmall, color = colors.onPrimaryContainer.copy(alpha = 0.85f))
            }
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, null, tint = colors.primary, modifier = Modifier.size(20.dp))
        }
    }
}
