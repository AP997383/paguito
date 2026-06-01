package com.nexusystem.paguito.ui.screens.productos

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.R
import com.nexusystem.paguito.utils.LoadingOverlay
import com.nexusystem.paguito.utils.bottomsSheets.BgBottomSheet
import com.nexusystem.paguito.utils.bottomsSheets.BlueLight
import com.nexusystem.paguito.utils.bottomsSheets.BluePrimary
import com.nexusystem.paguito.utils.dialogs.SuccessAlertDialog
import com.nexusystem.paguito.utils.emptyStates.DynamicEmptyState
import java.io.File
import java.util.Locale

// Blanco puro para tarjetas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductoFlashBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (PorductosEntity) -> Unit,
    viewmodel: ProductosViewModel,
    currentProduct: PorductosEntity = PorductosEntity()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BgBottomSheet,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        FlashBottomSheet(
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            viewmodel,
            currentProduct
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (PorductosEntity) -> Unit,
    viewmodel: ProductosViewModel,
    currentProduct: PorductosEntity = PorductosEntity()
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
    var stock by remember { mutableStateOf("1") }
    var notes by remember { mutableStateOf(currentProduct.notasAdicionales ?: "") }
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
                onDismiss()
            },
            onConfirmClick = {
                viewmodel.setUriLocal("")
                showSuccessPayment = false
                onDismiss()
            }
        )
    }
    Box{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .imePadding()
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Agregar Producto Rápido", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)

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

                CustomFieldLabel(stringResource(R.string.label_final_price))
                CustomOutlinedTextField(
                    value = costPrice,
                    onValueChange = {
                        costPrice = it },
                    placeholder = stringResource(R.string.placeholder_zero),
                    icon = Icons.Outlined.AttachMoney,
                    keyboardType = KeyboardType.Number
                )
                Spacer(modifier = Modifier.height(15.dp))
                Box(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background).padding( bottom = 60.dp)) {
                    Button(
                        onClick = {
                            val symbol ="x"
                            val finalStock = if (stock.isEmpty()) 1 else stock.toInt()
                            viewmodel.addNewCartilla(PorductosEntity(
                                id = null,
                                nombre = name,
                                idRemoteDatabase = "",
                                urlFoto = newUrlFromServer,
                                precioOriginal = costPrice.toFloat(),
                                precioConGanancia =  costPrice.toFloat(),
                                factorMultiplicador = 1F,
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

        LoadingOverlay(
            isLoading = isLoading,
            lottieRes = R.raw.loadings
        )
    }
}
// --- COMPONENTES SECUNDARIOS ---
