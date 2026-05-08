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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
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
fun ProductListScreen(onBackClick: () -> Unit = {},openProduct:(PorductosEntity)->Unit,addNewProduct:()->Unit,viewmodel:ProductosViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Más vendidos") }
    val listaProductos by viewmodel.produtosList.collectAsState()
    var isSucriptionActive by remember { mutableStateOf(false) }
    var showAlertFreeLimited by remember { mutableStateOf(false) }
    val filteredDebtors = remember(searchQuery, listaProductos) {
        if (searchQuery.isEmpty()) {
            listaProductos
        } else {
            listaProductos.filter { deudor ->
                // Filtramos por nombre, ignorando mayúsculas/minúsculas y espacios extras
                deudor?.nombre?.contains(searchQuery.trim(), ignoreCase = true) == true
            }
        }
    }
    val profile = viewmodel.profileState
    if(showAlertFreeLimited)
    {
        PremiumLimitReachedDialog({
            showAlertFreeLimited =false
        },{
            showAlertFreeLimited =false
        })
    }


    // val filters = listOf("Más vendidos", "Mayor Ganancia (%)", "Mayor Precio", "Menor Precio", "A-Z")

    LaunchedEffect(Unit) {
        viewmodel.loadUserProfile()
        viewmodel.obtenerProductos()
    }
    LaunchedEffect(profile) {
        if (profile != null) {
            isSucriptionActive = profile.userSuscription.isActive
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Productos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },

                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            if(filteredDebtors.size>0) {
                    FloatingActionButton(
                        onClick = {
                            if(listaProductos.size>=5 && !isSucriptionActive){
                                showAlertFreeLimited =true
                            }else{
                                addNewProduct()
                            }},
                        containerColor =  MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(end = 5.dp, bottom = 90.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Agregar",
                            modifier = Modifier.size(28.dp)
                        )
                    }


            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Buscador
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Buscar por nombre de producto...", color = TextLightGray, fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextLightGray) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = GreenPrimary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Filtros (Chips Deslizables)
           /* LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    FilterChipItem(
                        text = filter,
                        isSelected = selectedFilter == filter,
                        onClick = { selectedFilter = filter }
                    )
                }
            }*/

            Spacer(modifier = Modifier.height(8.dp))

            if(filteredDebtors.size>0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(filteredDebtors) { index, product ->
                        ProductCardItem(product = product!!, openProduct)
                        if(isSucriptionActive==false) {
                            val isLast = index == filteredDebtors.lastIndex
                            val isEveryFive = (index + 1) % 5 == 0

                            if (isEveryFive || (isLast && (index + 1) % 5 != 0)) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    NativeAdBanner(
                                        adUnitId = if (BuildConfig.DEBUG)
                                            "ca-app-pub-3940256099942544/2247696110"
                                        else
                                            "ca-app-pub-1155673544372892/4903935361"
                                    )
                                }
                            }
                        }
                    }
                  item{
                      Spacer(modifier = Modifier.height(90.dp))
                  }
                }
            }else{
                DynamicEmptyState(
                    imagePainter = painterResource(id = R.drawable.empty_state_products),
                    title = "Aún no tienes productos",
                    description = "Comienza a agregar tu inventario para gestionar tus ventas...",
                    buttonText = "Agregar Primer Producto",
                    buttonIcon = Icons.Default.Add,
                    onButtonClick = { addNewProduct() }
                )
            }
        }
    }
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
fun ProductCardItem(product: PorductosEntity,  onSelect: (PorductosEntity) -> Unit) {
    val isSoldOut = product.inventario == 0
    // Si está agotado, atenuamos un poco el texto
    val cardAlpha = if (isSoldOut) 0.6f else 1f
    val context = LocalContext.current

    // Estado para saber si debemos intentar cargar la original porque el thumb falló
    var useOriginalUrl by remember { mutableStateOf(false) }

    // Elegimos la URL basándonos en el estado
    val currentUrl = if (useOriginalUrl) {
        product.urlFoto
    } else {
        getThumbnailUrl(product.urlFoto)
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp).clickable{
                    onSelect(product)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del producto (Placeholder)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE5E7EB)),
                contentAlignment = Alignment.Center
            ) {
                if(product.urlFoto.isNullOrEmpty())
                    Icon(Icons.Outlined.Image, contentDescription = null, tint = TextLightGray, modifier = Modifier.size(32.dp))
                 else {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen de producto",
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        // Si la carga falla, actualizamos el estado para que se recargue con la original
                        onError = {
                            if (!useOriginalUrl) {
                                useOriginalUrl = true
                            }
                        }
                    )
                }


                // Etiqueta "DESTACADO"
                if (false) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("★ DESTACADO", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Etiqueta "AGOTADO" (Inclinada y roja)
                if (isSoldOut) {
                    Box(
                        modifier = Modifier
                            .background(RedAlert, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .graphicsLayer { rotationZ = -10f } // Rotación para darle ese efecto de sello
                    ) {
                        Text("AGOTADO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del Producto
            Column(
                modifier = Modifier
                    .weight(1f)
                    .alpha(cardAlpha) // Difumina si está agotado
            ) {
                // Nombre
                Text(
                    text = product.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fila de Precios (Costo / Venta)
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("COSTO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(formatAsCurrency( product.precioOriginal), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("VENTA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = TextLightGray)
                        Text(formatAsCurrency( product.precioConGanancia), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Stock
                Text(
                    text = if (isSoldOut) "Stock: 0 unidades" else "Stock: ${product.inventario} unidades",
                    fontSize = 11.sp,
                    color = if (isSoldOut) RedAlert else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSoldOut) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fila de Compras
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.PeopleAlt,
                        contentDescription = null,
                        tint = PurpleIcon.copy(alpha = 0.7f),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Comprado por ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                                append(""+product.ventas)
                            }
                            append(" clientes")
                        },
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
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