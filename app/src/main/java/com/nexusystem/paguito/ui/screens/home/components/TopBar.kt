package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.nexusystem.paguito.R

@Composable
fun TopBar(imageUser: String?, goToMyProfile: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primaryContainer) {
            Image(painterResource(R.drawable.abonia_a), contentDescription = null,
                modifier = Modifier.padding(8.dp).size(32.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Paguito", style = MaterialTheme.typography.titleLarge)
            Text("Tu negocio, en orden", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        FilledTonalIconButton(onClick = goToMyProfile, modifier = Modifier.size(48.dp), shape = CircleShape) {
            AsyncImage(
                model = imageUser?.takeIf { it.isNotBlank() } ?: R.drawable.avatar,
                contentDescription = "Abrir mi perfil",
                modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop
            )
        }
    }
}
