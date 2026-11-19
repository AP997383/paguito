package com.nexusystem.paguito.ui.screens.productos

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PeopleAlt
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.utils.emptyStates.DynamicEmptyState
import com.nexusystem.paguito.R
import com.nexusystem.paguito.utils.dialogs.DeleteConfirmationDialog
import com.nexusystem.paguito.utils.dialogs.PremiumLimitReachedDialog
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.getThumbnailUrl
import com.nexusystem.paguito.utils.shimmerEffect

// --- COLORES PRINCIPALES ---
val RedAlert = Color(0xFFEF4444)
val PurpleIcon = Color(0xFF8B5CF6) // Para el ícono de clientes

// --- MODELO DE DATOS ---
data class ProductItem(
    val name: String,
    val costPrice: String,
    val salePrice: String,
    val stock: Int,
    val clientsCount: Int,
    val isFeatured: Boolean = false
)



// --- PANTALLA PRINCIPAL ---
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(onBackClick: () -> Unit = {}, openProduct: (PorductosEntity) -> Unit, addNewProduct: () -> Unit,
        goToMyProfile: () -> Unit, viewmodel: ProductosViewModel) {
        val rows by viewmodel.produtosList.collectAsState()
        val products = rows.filterNotNull()
        val profile = viewmodel.profileState
        val premium = profile?.userSuscription?.isActive == true
        var showLimit by remember { mutableStateOf(false) }
        var deleting by remember { mutableStateOf<PorductosEntity?>(null) }
        LaunchedEffect(Unit) { viewmodel.loadUserProfile(); viewmodel.obtenerProductos() }
        if (showLimit) PremiumLimitReachedDialog({ showLimit = false }, { showLimit = false })
        deleting?.let { product -> DeleteConfirmationDialog(product.nombre, {
            viewmodel.deleteProduct(product); deleting = null
        }, { deleting = null }) }
        com.nexusystem.paguito.ui.screens.productos.components.ProductDirectoryContent(
            products, profile?.fotoUrl.orEmpty(), goToMyProfile,
            onAdd = { if (products.size >= 10 && !premium) showLimit = true else addNewProduct() },
            onOpen = openProduct, onDelete = { deleting = it },
            adContent = if (premium) null else {
                { NativeAdBanner(adUnitId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110" else "ca-app-pub-1155673544372892/4903935361") }
            }
        )
    }

// --- COMPONENTES SECUNDARIOS ---

@Composable
fun FilterChipItem(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected)  MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else TextGray
    val borderColor = if (isSelected)  MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = contentColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun ProductCardItem(product: PorductosEntity, onSelect: (PorductosEntity) -> Unit, onDelete: (PorductosEntity) -> Unit) {
        var confirming by remember { mutableStateOf(false) }
        if (confirming) DeleteConfirmationDialog(product.nombre, { onDelete(product); confirming = false }, { confirming = false })
        com.nexusystem.paguito.ui.screens.productos.components.CatalogProductCard(product, { onSelect(product) }, { confirming = true })
    }

@Composable
fun NativeAdBanner(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }

    // Definimos un alto fijo para el banner pequeño (típicamente entre 90dp y 120dp)
    val adHeight = 360.dp

    DisposableEffect(adUnitId) {
        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAd = ad
            }
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
        }
    }

    Column (
        modifier = modifier
            .fillMaxWidth()
            .height(adHeight) // 🔥 Reservamos el espacio desde el inicio
            .clip(RoundedCornerShape(5.dp))
    ) {
        // --- ETIQUETA DE PUBLICIDAD ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(all = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PUBLICIDAD",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.LightGray,
                letterSpacing = 1.sp
            )
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = Color.LightGray
            )
        }
        if (nativeAd == null) {

            // 🔥 Mientras carga, mostramos el Skeleton con Shimmer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .shimmerEffect() // Usamos el modificador que definimos antes
            )
        } else {
            // 🔥 Cuando carga, mostramos el anuncio
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val adView = LayoutInflater.from(ctx)
                        .inflate(R.layout.native_ad_small, null) as NativeAdView

                    // (Tu lógica de binding de vistas se mantiene igual...)
                    adView.headlineView = adView.findViewById(R.id.ad_headline)
                    adView.bodyView = adView.findViewById(R.id.ad_body)
                    adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
                    adView.iconView = adView.findViewById(R.id.ad_app_icon)
                    adView.mediaView = adView.findViewById(R.id.ad_media)

                    nativeAd?.let { ad ->
                        (adView.headlineView as TextView).text = ad.headline
                        (adView.bodyView as TextView).text = ad.body
                        (adView.callToActionView as Button).text = ad.callToAction
                        ad.icon?.let { (adView.iconView as ImageView).setImageDrawable(it.drawable) }
                        adView.setNativeAd(ad)
                    }

                    adView
                }
            )
        }
    }
}