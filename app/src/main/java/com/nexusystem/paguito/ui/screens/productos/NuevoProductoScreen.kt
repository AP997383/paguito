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
val BgColor: Color
    @Composable get() = MaterialTheme.colorScheme.background
val CardWhite = Color(0xFFFFFFFF)
val TextDark: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
val TextGray: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val TextLightGray: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val BorderColor: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant

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
    val newUrlFromServer by viewmodel.newUrlServer.collectAsState()

    val isEditingProduct = currentProduct.idRemoteDatabase.isNotBlank()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    var name by remember { mutableStateOf(currentProduct.nombre) }
    var costPrice by remember {
        mutableStateOf(
            if (currentProduct.precioOriginal > 0f) currentProduct.precioOriginal.toString() else ""
        )
    }
    var stock by remember {
        mutableStateOf(
            if (currentProduct.inventario > 0) currentProduct.inventario.toString() else ""
        )
    }
    var notes by remember { mutableStateOf(currentProduct.notasAdicionales) }

    var isMultiplierMode by remember {
        mutableStateOf(currentProduct.signoMultiplicador != "%")
    }
    var sliderMultiplier by remember {
        mutableStateOf(if (currentProduct.factorMultiplicador > 0f) currentProduct.factorMultiplicador else 2.0f)
    }
    var sliderPercentage by remember {
        mutableStateOf(if (currentProduct.factorMultiplicador > 0f) currentProduct.factorMultiplicador else 100f)
    }

    var finalPrice by remember { mutableStateOf("") }
    var profitAmount by remember { mutableStateOf("") }
    var showSuccessPayment by remember { mutableStateOf(false) }

    LaunchedEffect(currentProduct.urlFoto) {
        viewmodel.setUriLocal(currentProduct.urlFoto)
    }

    fun Context.createImageUri(): Uri {
        val authority = "${packageName}.fileprovider"
        val file = File(cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(this, authority, file)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            if (!email.isNullOrEmpty()) viewmodel.savePhotoProduct(email, uri)
            else viewmodel.setUriLocal(uri.toString())

            imageUri = uri
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempUri != null) {
            if (!email.isNullOrEmpty()) viewmodel.savePhotoProduct(email, tempUri)
            else viewmodel.setUriLocal(tempUri.toString())

            imageUri = tempUri
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val uri = context.createImageUri()
            tempUri = uri
            cameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(costPrice, isMultiplierMode, sliderMultiplier, sliderPercentage) {
        val cost = costPrice.toDoubleOrNull() ?: 0.0
        val calculatedPrice = if (isMultiplierMode) {
            cost * sliderMultiplier
        } else {
            cost * (1 + (sliderPercentage / 100))
        }

        finalPrice = String.format(Locale.US, "%.2f", calculatedPrice)
        profitAmount = String.format(Locale.US, "%.2f", calculatedPrice - cost)
    }

    fun isFormValid(): Boolean {
        return name.isNotBlank() &&
                costPrice.isNotBlank() &&
                (costPrice.toFloatOrNull() ?: 0f) > 0f
    }

    fun saveProduct() {
        val symbol = if (isMultiplierMode) "x" else "%"
        val finalStock = stock.toIntOrNull() ?: 0

        val productToSave = PorductosEntity(
            id = if (isEditingProduct) currentProduct.id else null,
            nombre = name,
            idRemoteDatabase = if (isEditingProduct) currentProduct.idRemoteDatabase else "",
            urlFoto = newUrlFromServer,
            precioOriginal = costPrice.toFloatOrNull() ?: 0f,
            precioConGanancia = finalPrice.toFloatOrNull() ?: 0f,
            factorMultiplicador = if (isMultiplierMode) sliderMultiplier else sliderPercentage,
            signoMultiplicador = symbol,
            inventario = finalStock,
            notasAdicionales = notes,
            ventas = if (isEditingProduct) currentProduct.ventas else 0,
            ownerAccount = if (isEditingProduct) currentProduct.ownerAccount else ""
        )

        viewmodel.addNewCartilla(
            producto = productToSave,
            onSuccess = {
                showSuccessPayment = true
            },
            onError = {
                Log.e("PRODUCT", "Error guardando producto", it)
            }
        )
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
                showSuccessPayment = false
                viewmodel.setUriLocal("")
                onBackClick()
            }
        )
    }

    Box {
        com.nexusystem.paguito.ui.screens.productos.components.ProductEditorContent(
            state = com.nexusystem.paguito.ui.screens.productos.components.ProductEditorState(
                name = name, cost = costPrice, stock = stock, notes = notes, photo = newUrlFromServer,
                finalPrice = finalPrice, profit = profitAmount, multiplierMode = isMultiplierMode,
                multiplier = sliderMultiplier, percentage = sliderPercentage, editing = isEditingProduct,
                saving = isLoading, canSave = isFormValid()
            ),
            actions = com.nexusystem.paguito.ui.screens.productos.components.ProductEditorActions(
                onName = { name = it }, onCost = { costPrice = it }, onStock = { stock = it }, onNotes = { notes = it },
                onPrice = {
                    finalPrice = it
                    profitAmount = String.format(Locale.US, "%.2f", (it.toDoubleOrNull() ?: 0.0) - (costPrice.toDoubleOrNull() ?: 0.0))
                }, onMode = { isMultiplierMode = it }, onMultiplier = { sliderMultiplier = it }, onPercentage = { sliderPercentage = it },
                onGallery = { galleryLauncher.launch("image/*") }, onCamera = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                onSave = { saveProduct() }, onBack = onBackClick
            )
        )
        LoadingOverlay(
            isLoading = isLoading,
            lottieRes = R.raw.loadings
        )
    }
}

// ================= COMPONENTES SECUNDARIOS =================

@Composable
fun ImageUploadSection(imageBitmap: String?, onGalleryClick: () -> Unit, onCameraClick: () -> Unit) {
        com.nexusystem.paguito.ui.screens.productos.components.ProductPhotoCard(imageBitmap, onGalleryClick, onCameraClick)
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
        costPrice: String, finalPrice: String, profitAmount: String, isMultiplierMode: Boolean,
        sliderMultiplier: Float, sliderPercentage: Float, onModeChange: (Boolean) -> Unit,
        onSliderMultiplierChange: (Float) -> Unit, onSliderPercentageChange: (Float) -> Unit, onFinalPriceManualChange: (String) -> Unit
    ) {
        com.nexusystem.paguito.ui.screens.productos.components.ProductPricingCard(costPrice, finalPrice, profitAmount,
            isMultiplierMode, sliderMultiplier, sliderPercentage, onFinalPriceManualChange, onModeChange, onSliderMultiplierChange, onSliderPercentageChange)
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