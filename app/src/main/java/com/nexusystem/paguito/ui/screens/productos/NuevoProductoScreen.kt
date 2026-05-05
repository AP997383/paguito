package com.nexusystem.paguito.ui.screens.productos

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import coil.compose.AsyncImage
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.R
import com.nexusystem.paguito.utils.LoadingOverlay
import com.nexusystem.paguito.utils.dialogs.SuccessAlertDialog
import java.io.File
import java.util.Locale

// --- COLORES ---
val GreenPrimary = Color(0xFF15956F)
val GreenLight = Color(0xFFE6F9F3)
val BgColor = Color(0xFFF9FAFB)
val CardWhite = Color(0xFFFFFFFF)
val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF6B7280)
val TextLightGray = Color(0xFF9CA3AF)
val BorderColor = Color(0xFFE5E7EB)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onBackClick: () -> Unit = {},
    viewmodel: ProductosViewModel,
    currentProduct: PorductosEntity
) {
    val context = LocalContext.current
    val email = viewmodel.mail
    val isLoading by viewmodel.isLoading.collectAsState()
    // --- ESTADOS: FOTOS ---
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    fun Context.createImageUri(): Uri {
        val authority = "${context.packageName}.fileprovider"


        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        return  FileProvider.getUriForFile(
            context,
            authority, // Esto resolverá a "com.nexusystem.paguito.fileprovider"
            file
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            if (!email.isNullOrEmpty()) viewmodel.savePhotoProduct(email, uri)
            else viewmodel.setUriLocal(uri.toFile().absolutePath)
            imageUri = uri
            imageBitmap = null
        }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success && tempUri != null) {
            // La foto se guardó exitosamente en tempUri
            if (!email.isNullOrEmpty()) {
                viewmodel.savePhotoProduct(email, tempUri!!)
            } else {
                viewmodel.setUriLocal(tempUri!!.toString())
            }
            imageUri = tempUri
            imageBitmap = null
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val uri = context.createImageUri()
            tempUri = uri // Guardamos la referencia para usarla en el callback
            cameraLauncher.launch(uri)
        }
    }

    // --- ESTADOS: FORMULARIO ---
    var name by remember { mutableStateOf(currentProduct.nombre ?: "") }
    var costPrice by remember { mutableStateOf(currentProduct.precioOriginal.toString() ?: "0") }
    var stock by remember { mutableStateOf(currentProduct.inventario.toString() ?: "") }
    var notes by remember { mutableStateOf(currentProduct.notasAdicionales ?: "") }

    // --- ESTADOS: CALCULADORA DE GANANCIA ---
    var isMultiplierMode by remember { mutableStateOf(true) }
    var sliderMultiplier by remember { mutableStateOf(currentProduct.factorMultiplicador ?: 2.0f) }
    var sliderPercentage by remember { mutableStateOf(currentProduct.factorMultiplicador ?: 100f) }
    var finalPrice by remember { mutableStateOf("") }
    var profitAmount by remember { mutableStateOf("") }

    var showSuccessPayment by remember { mutableStateOf(false) }
    val newUrlFromServer by viewmodel.newUrlServer.collectAsState()
     if(!currentProduct.urlFoto.isNullOrEmpty()){
         viewmodel.setUriLocal(currentProduct.urlFoto)
     }
    if (showSuccessPayment) {
        SuccessAlertDialog(
            mesage = stringResource(R.string.product_registered_success),
            onDismissRequest = {
                showSuccessPayment = false
                viewmodel.setUriLocal("")
                onBackClick()
            },
            onConfirmClick = {
                viewmodel.setUriLocal("")
                showSuccessPayment = false
                onBackClick()
            }
        )
    }

    LaunchedEffect(costPrice, isMultiplierMode, sliderMultiplier, sliderPercentage) {
        val cost = costPrice.toDoubleOrNull() ?: 0.0
        val calculatedPrice = if (isMultiplierMode) {
            cost * sliderMultiplier
        } else {
            cost * (1 + (sliderPercentage / 100))
        }
        val profit = calculatedPrice - cost

        finalPrice = String.format(Locale.US, "%.2f", calculatedPrice)
        profitAmount = String.format(Locale.US, "%.2f", profit)
    }
Box{
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.title_new_product), fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, stringResource(R.string.content_desc_back))
                    }

                },
                actions = {
                    TextButton(onClick = {
                        val symbol = if (isMultiplierMode) "x" else "%"
                        val finalStock = if (stock.isEmpty()) 0 else stock.toInt()
                        viewmodel.addNewCartilla(PorductosEntity(
                            id = null,
                            nombre = name,
                            idRemoteDatabase = "",
                            urlFoto = newUrlFromServer,
                            precioOriginal = costPrice.toFloat(),
                            precioConGanancia = finalPrice.toFloat(),
                            factorMultiplicador = 0.5F,
                            signoMultiplicador = symbol,
                            inventario = finalStock,
                            notasAdicionales = "",
                            ventas = 0
                        ))
                        showSuccessPayment = true
                    }, enabled = !name.isEmpty()&& !costPrice.isEmpty()&& costPrice.toDouble()>0) {
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.Bold,
                            color = if (!name.isEmpty()&& !costPrice.isEmpty()) GreenPrimary else TextLightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0.dp)
            )
        },

        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .imePadding()
        ) {
            Spacer(modifier = Modifier.height(16.dp))
                ImageUploadSection(
                    imageBitmap = newUrlFromServer,
                    onGalleryClick = { galleryLauncher.launch("image/*") },
                    onCameraClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                )


            Spacer(modifier = Modifier.height(24.dp))

            CustomFieldLabel(stringResource(R.string.label_product_name))
            CustomOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = stringResource(R.string.placeholder_product_name)
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomFieldLabel(stringResource(R.string.label_cost_price))
            CustomOutlinedTextField(
                value = costPrice,
                onValueChange = { costPrice = it },
                placeholder = stringResource(R.string.placeholder_zero),
                icon = Icons.Outlined.AttachMoney,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(32.dp))

            ProfitCalculatorCard(
                costPrice = costPrice,
                finalPrice = finalPrice,
                profitAmount = profitAmount,
                isMultiplierMode = isMultiplierMode,
                sliderMultiplier = sliderMultiplier,
                sliderPercentage = sliderPercentage,
                onModeChange = { isMultiplierMode = it },
                onSliderMultiplierChange = { sliderMultiplier = it },
                onSliderPercentageChange = { sliderPercentage = it },
                onFinalPriceManualChange = {
                    finalPrice = it
                    val cost = costPrice.toDoubleOrNull() ?: 0.0
                    val finalP = it.toDoubleOrNull() ?: 0.0
                    profitAmount = String.format(Locale.US, "%.2f", finalP - cost)
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(stringResource(R.string.label_optionals), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            CustomFieldLabel(stringResource(R.string.label_stock))
            CustomOutlinedTextField(
                value = stock,
                onValueChange = { stock = it },
                placeholder = stringResource(R.string.placeholder_stock),
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomFieldLabel(stringResource(R.string.label_additional_notes))
            CustomOutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = stringResource(R.string.placeholder_notes),
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )
            Spacer(modifier = Modifier.height(15.dp))
            Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding( bottom = 60.dp)) {
                Button(
                    onClick = {
                        val symbol = if (isMultiplierMode) "x" else "%"
                        val finalStock = if (stock.isEmpty()) 0 else stock.toInt()
                        viewmodel.addNewCartilla(PorductosEntity(
                            id = null,
                            nombre = name,
                            idRemoteDatabase = "",
                            urlFoto = newUrlFromServer,
                            precioOriginal = costPrice.toFloat(),
                            precioConGanancia = finalPrice.toFloat(),
                            factorMultiplicador = 0.5F,
                            signoMultiplicador = symbol,
                            inventario = finalStock,
                            notasAdicionales = "",
                            ventas = 0
                        ))
                        showSuccessPayment = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    enabled =  !name.isEmpty()&& !costPrice.isEmpty() && costPrice.toDouble()>0
                ) {
                    Text(stringResource(R.string.btn_save_product), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
    LoadingOverlay(
        isLoading = isLoading,
        lottieRes = R.raw.loadings
    )
}

}

// ================= COMPONENTES SECUNDARIOS =================

@Composable
fun ImageUploadSection(
    imageBitmap: String?,
    onGalleryClick: () -> Unit,
    onCameraClick: () -> Unit
) {
    Log.e("UURLPAINT","-->"+imageBitmap)
    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .drawBehind {
                drawRoundRect(
                    color = GreenPrimary.copy(alpha = 0.4f),
                    style = Stroke(width = 4f, pathEffect = dashPathEffect),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx())
                )
            }
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!imageBitmap.isNullOrEmpty()) {
            AsyncImage(
                model = imageBitmap,
                contentDescription = stringResource(R.string.content_desc_profile_photo),
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.upload_product_image), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(stringResource(R.string.optional_label), color = TextLightGray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    IconButtonBox(icon = Icons.Outlined.PhotoCamera, onClick = onCameraClick)
                    IconButtonBox(icon = Icons.Outlined.PhotoLibrary, onClick = onGalleryClick)
                }
            }
        }
    }
}

@Composable
fun IconButtonBox(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Surface(
        shape = CircleShape,
        border = BorderStroke(1.dp, BorderColor),
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.size(40.dp).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) }
    }
}

@Composable
fun ProfitCalculatorCard(
    costPrice: String, finalPrice: String, profitAmount: String,
    isMultiplierMode: Boolean, sliderMultiplier: Float, sliderPercentage: Float,
    onModeChange: (Boolean) -> Unit, onSliderMultiplierChange: (Float) -> Unit,
    onSliderPercentageChange: (Float) -> Unit, onFinalPriceManualChange: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, BorderColor),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.label_define_profit), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.Info, contentDescription = null, tint = TextLightGray, modifier = Modifier.size(16.dp))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderColor)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PriceBox(
                    label = stringResource(R.string.label_original_cost),
                    price = "$${if(costPrice.isEmpty()) "0" else costPrice}",
                    borderColor = BorderColor,
                    modifier = Modifier.weight(1f)
                )

                Box(modifier = Modifier.weight(1f)) {
                    PriceBox(
                        label = stringResource(R.string.label_final_price),
                        price = finalPrice,
                        isEditable = true,
                        onPriceChange = onFinalPriceManualChange,
                        borderColor = GreenPrimary,
                        textColor = GreenPrimary,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    Surface(
                        color = GreenPrimary,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.align(Alignment.TopCenter).offset(y = (-2).dp)
                    ) {
                        Text(
                            text = stringResource(R.string.profit_badge_format, profitAmount),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            SegmentedControl(isMultiplierMode = isMultiplierMode, onModeChange = onModeChange)
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if (isMultiplierMode) stringResource(R.string.adjust_multiplier) else stringResource(R.string.adjust_percentage),
                    fontSize = 13.sp
                )
                Surface(color = GreenLight, shape = RoundedCornerShape(6.dp), border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.3f))) {
                    Text(
                        text = if (isMultiplierMode) String.format(Locale.US, "%.2fx", sliderMultiplier) else "${sliderPercentage.toInt()}%",
                        color = GreenPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (isMultiplierMode) {
                Slider(
                    value = sliderMultiplier,
                    onValueChange = onSliderMultiplierChange,
                    valueRange = 0.25f..3.0f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = GreenPrimary)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("0.25x", "1.0x", "2.0x", "3.0x").forEach { Text(it, fontSize = 10.sp, color = TextLightGray) }
                }
            } else {
                Slider(
                    value = sliderPercentage,
                    onValueChange = onSliderPercentageChange,
                    valueRange = 10f..200f,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = GreenPrimary)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("10%", "100%", "200%").forEach { Text(it, fontSize = 10.sp, color = TextLightGray) }
                }
            }
        }
    }
}

@Composable
fun SegmentedControl(isMultiplierMode: Boolean, onModeChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().height(40.dp).background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)).border(1.dp, BorderColor, RoundedCornerShape(8.dp))) {
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight().background(if (!isMultiplierMode) GreenPrimary else Color.Transparent, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp))
                .clickable { onModeChange(false) },
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.tab_percentage), color = if (!isMultiplierMode) Color.White else TextGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight().background(if (isMultiplierMode) GreenPrimary else Color.Transparent, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp))
                .clickable { onModeChange(true) },
            contentAlignment = Alignment.Center
        ) {
            Text(stringResource(R.string.tab_multiplier), color = if (isMultiplierMode) Color.White else TextGray, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PriceBox(label: String, price: String, isEditable: Boolean = false, onPriceChange: (String) -> Unit = {}, borderColor: Color, textColor: Color = MaterialTheme.colorScheme.onSurface, modifier: Modifier = Modifier) {
    Surface(shape = RoundedCornerShape(12.dp), border = BorderStroke(1.dp, borderColor), color = MaterialTheme.colorScheme.background, modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                if (!isEditable) {
                    Text(price, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                } else {
                    Text("$", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
                    BasicTextField(
                        value = price,
                        onValueChange = onPriceChange,
                        textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = textColor, textAlign = TextAlign.Center),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        cursorBrush = SolidColor(GreenPrimary),
                        modifier = Modifier.width(IntrinsicSize.Min)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Outlined.Edit, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@Composable
fun CustomFieldLabel(text: String) { Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp)) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, keyboardType: KeyboardType = KeyboardType.Text, singleLine: Boolean = true, modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        modifier = modifier, shape = RoundedCornerShape(12.dp), singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        placeholder = { Text(placeholder, fontSize = 14.sp) },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = TextLightGray) } },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = BorderColor, focusedBorderColor = GreenPrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}