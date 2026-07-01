package com.nexusystem.paguito.ui.screens.website.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.NorthEast
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nexusystem.paguito.ui.theme.WebsiteGreen
import com.nexusystem.paguito.ui.theme.WebsitePrimary

@Composable
fun WebsitePublishedPreviewCard(
    businessName: String,
    url: String,
    products: List<WebsitePreviewProduct>,
    onOpen: () -> Unit
) {
    val visibleProducts = products.take(2)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Vista publicada",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Mini vista de tu sitio activo.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Outlined.NorthEast,
                    contentDescription = null,
                    tint = WebsiteGreen
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                        RoundedCornerShape(22.dp)
                    )
                    .clickable { onOpen() }
                    .padding(10.dp)
            ) {
                HeroMini(
                    businessName = businessName
                )

                Spacer(modifier = Modifier.height(12.dp))

                CatalogMini(
                    products = visibleProducts
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = url,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun HeroMini(
    businessName: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(20.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            WebsitePrimary,
                            MaterialTheme.colorScheme.onSurface
                        )
                    ),
                    RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Storefront,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "TIENDA DIGITAL · BY PAGUITO",
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                color = WebsitePrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = businessName,
                fontSize = 23.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Productos disponibles, precios actualizados y atención directa.",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniBadge(
                    icon = Icons.Outlined.VerifiedUser,
                    text = "Compra segura"
                )

                MiniBadge(
                    icon = Icons.Outlined.Message,
                    text = "WhatsApp"
                )
            }
        }
    }
}

@Composable
private fun CatalogMini(
    products: List<WebsitePreviewProduct>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(20.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "CATÁLOGO",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = WebsitePrimary
                )

                Text(
                    text = "Productos en venta",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Revisa disponibilidad y compra rápido.",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = WebsiteGreen,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .height(28.dp)
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Message,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )

                    Spacer(modifier = Modifier.width(5.dp))

                    Text(
                        text = "WhatsApp",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (products.isEmpty()) {
                MiniProductCard(
                    product = WebsitePreviewProduct(
                        name = "Sin productos",
                        price = 0.0,
                        stock = 0,
                        sold = 0,
                        imageUrl = null
                    ),
                    modifier = Modifier.weight(1f)
                )
            } else {
                products.forEach { product ->
                    MiniProductCard(
                        product = product,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniProductCard(
    product: WebsitePreviewProduct,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.20f),
                RoundedCornerShape(16.dp)
            )
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(86.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(
                    MaterialTheme.colorScheme.background.copy(alpha = 0.65f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (!product.imageUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )

                    Text(
                        text = "Sin imagen",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(7.dp))

        Text(
            text = product.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(5.dp))

        Text(
            text = "PRECIO",
            fontSize = 7.sp,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$${product.price.toInt()}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = WebsitePrimary,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Comprar",
                fontSize = 7.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier
                    .background(
                        WebsiteGreen,
                        RoundedCornerShape(7.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun MiniBadge(
    icon: ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(10.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = text,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}