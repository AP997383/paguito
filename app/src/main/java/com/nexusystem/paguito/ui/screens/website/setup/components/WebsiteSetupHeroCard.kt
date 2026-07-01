package com.nexusystem.paguito.ui.screens.website.setup.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.nexusystem.paguito.ui.theme.WebsiteGreen
import com.nexusystem.paguito.ui.theme.WebsitePrimary

@Composable
fun WebsiteSetupHeroCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .background(
                Brush.linearGradient(
                    listOf(
                        WebsitePrimary.copy(alpha = 0.18f),
                        WebsiteGreen.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.surface
                    )
                ),
                RoundedCornerShape(22.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .width(220.dp)
                .height(128.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                WebsitePrimary.copy(alpha = 0.15f),
                                androidx.compose.foundation.shape.CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.Storefront,
                            null,
                            tint = WebsitePrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Box(
                            modifier = Modifier
                                .width(92.dp)
                                .height(8.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                    RoundedCornerShape(4.dp)
                                )
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Box(
                            modifier = Modifier
                                .width(70.dp)
                                .height(6.dp)
                                .background(
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroMiniProduct(modifier = Modifier.weight(1f))
                    HeroMiniProduct(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .background(WebsiteGreen, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Message, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMiniProduct(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
            .padding(7.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(34.dp)
                .background(
                    MaterialTheme.colorScheme.surface,
                    RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Inventory2, null, tint = WebsitePrimary, modifier = Modifier.size(14.dp))
        }

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .width(54.dp)
                .height(5.dp)
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                    RoundedCornerShape(3.dp)
                )
        )

        Spacer(modifier = Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .width(36.dp)
                .height(5.dp)
                .background(
                    WebsitePrimary.copy(alpha = 0.55f),
                    RoundedCornerShape(3.dp)
                )
        )
    }
}