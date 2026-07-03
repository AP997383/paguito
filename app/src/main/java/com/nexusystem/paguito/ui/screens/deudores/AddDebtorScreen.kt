package com.nexusystem.paguito.ui.screens.deudores

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.ContactsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.components.navigation.view.AppHeader
import com.nexusystem.paguito.ui.screens.payments.DateSelectorCard
import com.nexusystem.paguito.ui.screens.payments.PeriodicitySelectorRow
import com.nexusystem.paguito.ui.screens.payments.SectionLabel
import com.nexusystem.paguito.ui.screens.payments.SelectedProductItem
import com.nexusystem.paguito.ui.screens.productos.ChoseProductBottomSheet
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel
import com.nexusystem.paguito.utils.getTodayDateString
import com.nexusystem.paguito.utils.openAppSettings
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

// --- COLORES ---
val GreenPrimary = Color(0xFF15956F)



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtorScreen(
    deudoresViewModel: DeudoresViewModel,
    onBackClick: () -> Unit = {},
    productosViewModel: ProductosViewModel
) {
    val context = LocalContext.current
    var showSettingsDialog by remember { mutableStateOf(false) }
    // --- ESTADOS ---
    val listaBusquedaProductos by productosViewModel.produtosList.collectAsState()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("M") }
    var amount by remember { mutableStateOf("") }
    var productSearch by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showListProducts by remember { mutableStateOf(false) }

    val today = LocalDate.now()
    var startDate: LocalDate? by remember { mutableStateOf(today) }
    val selectedProducts = remember { mutableStateListOf<PorductosEntity>() }

    val sharedPrefs = remember { context.getSharedPreferences("paguito_settings", Context.MODE_PRIVATE) }
    var showWizard by remember {
        mutableStateOf(sharedPrefs.getBoolean("has_seen_contacts_tip", false).not())
    }
    val isFormValid by remember {
        derivedStateOf { name.isNotBlank() && phone.isNotBlank() && amount.isNotBlank() }
    }
    var paymentData by remember{ mutableStateOf(PagosEntinty())}
    val startPickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDate!!.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )


    // --- LÓGICA DE CONTACTOS ---
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { contactUri ->
            context.contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val hasPhone = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                    val nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                    if (nameIndex != -1) name = cursor.getString(nameIndex)

                    if (hasPhone > 0) {
                        context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )?.use { pCursor ->
                            if (pCursor.moveToFirst()) {
                                val numIndex = pCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numIndex != -1) {
                                    val rawNumber = pCursor.getString(numIndex).replace(Regex("[^0-9]"), "")
                                    phone = rawNumber.takeLast(10)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            // Verificar si se denegó permanentemente
            val activity = context as? Activity
            val showRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.READ_CONTACTS)
            } ?: false

            if (!showRationale) {
                showSettingsDialog = true
            }
        }
    }

    // --- DIÁLOGO DE ADVERTENCIA PARA SETTINGS ---
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text("Permiso necesario") },
            text = { Text("Has desactivado el acceso a contactos. Para usar esta función, por favor actívalo en los ajustes de la aplicación.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSettingsDialog = false
                        openAppSettings(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Ir a Ajustes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSettingsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // --- EFECTOS ---
    LaunchedEffect(Unit) {
        productosViewModel.obtenerProductos()
    }

    // --- DIÁLOGOS Y BOTTOM SHEETS ---
    if(showListProducts){
        ChoseProductBottomSheet({showListProducts = false},{
            showListProducts =false
            selectedProducts.add(it)
            Log.e("xxxxxx","->"+amount+"/"+it.precioConGanancia)
            if(amount.isNullOrEmpty())
                amount =  it.precioConGanancia.toString()
            else
                amount = (amount.toFloat() + it.precioConGanancia).toString()
        },listaBusquedaProductos,productosViewModel)
    }


    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val millis = startPickerState.selectedDateMillis
                    if (millis != null) {
                        startDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showStartDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = startPickerState) }
    }

    // --- UI ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Scaffold(
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp)
                ) {
                    Button(
                        onClick = {
                            val jsonProducts = Gson().toJson(
                                if (selectedProducts.size > 0) selectedProducts else ""
                            )

                            deudoresViewModel.guardarDeudor(
                                DeudoresEntity(
                                    null,
                                    "",
                                    inRemote = false,
                                    name,
                                    phone,
                                    "",
                                    address,
                                    amount.toFloat(),
                                    montoAcomulado = amount.toFloat(),
                                    fechaInicialDeuda = startDate.toString(),
                                    periodicidad = selectedPeriod,
                                    0,
                                    0,
                                    jsonProducts,
                                    ""
                                )
                            )

                            onBackClick()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        enabled = isFormValid
                    ) {
                        Text(
                            "Guardar y Crear Deuda",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 104.dp)
                    .imePadding()
            ) {
                Text("Información Personal", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                LabelledTextField(
                    label = "Nombre *",
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Ej. Juan Pérez",
                    icon = Icons.Outlined.Person
                )

                LabelledTextField(
                    label = "Teléfono *",
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "10 dígitos",
                    icon = Icons.Outlined.Phone,
                    keyboardType = KeyboardType.Phone,
                    maxChars = 10
                )

                LabelledTextField(
                    label = "Domicilio",
                    isOptional = true,
                    value = address,
                    onValueChange = { address = it },
                    placeholder = "Calle, número...",
                    icon = Icons.Outlined.LocationOn
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Detalles del Crédito", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                AmountCard(amount = amount, onAmountChange = { amount = it })

                Spacer(modifier = Modifier.height(24.dp))

                SectionLabel("Fecha de inicio")
                DateSelectorCard({ showStartDatePicker = true }, startDate.toString())

                Spacer(modifier = Modifier.height(24.dp))

                SectionLabel("Periodicidad de pago")
                PeriodicitySelectorRow(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { selectedPeriod = it }
                )

                Spacer(modifier = Modifier.height(24.dp))

                FieldLabel(title = "Productos Relacionados", isOptional = true)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CustomOutlinedTextField(
                        value = productSearch,
                        onValueChange = { productSearch = it },
                        placeholder = "Buscar...",
                        icon = Icons.Outlined.Search,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    OutlinedButton(
                        onClick = { showListProducts = true },
                        modifier = Modifier.height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Añadir")
                    }
                }

                if (selectedProducts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))

                    selectedProducts.forEach { producto ->
                        SelectedProductItem(
                            producto = producto,
                            onRemove = {
                                val current = amount.toFloatOrNull() ?: 0f
                                amount = (current - producto.precioConGanancia)
                                    .coerceAtLeast(0f)
                                    .toString()

                                selectedProducts.remove(producto)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                FieldLabel(title = "Notas / Observaciones")

                CustomOutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    placeholder = "Detalles adicionales...",
                    singleLine = false,
                    modifier = Modifier.height(100.dp)
                )

                Spacer(modifier = Modifier.height(120.dp))
            }
        }

        AppHeader(
            onBack = onBackClick,
            title = "Nuevo Cliente",
            rightIcon = Icons.Outlined.ContactPage,
            rightIconTint = GreenPrimary,
            onRightClick = {
                if (
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.READ_CONTACTS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    contactPickerLauncher.launch(null)
                } else {
                    permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            }
        )
    }
    if (showWizard) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)) // Fondo oscurecido para enfocar el mensaje
                .clickable(enabled = false) { } // Bloquea clics hacia los campos de abajo
        ) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFBC02D),
                        modifier = Modifier.size(48.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        "Consejo rápido",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Puedes evitar escribir manualmente: usa el icono de contactos en la esquina superior para importar nombres y teléfonos al instante. 📁⚡",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            // Guardamos que ya lo vio y cerramos
                            sharedPrefs.edit().putBoolean("has_seen_contacts_tip", true).apply()
                            showWizard = false
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                    ) {
                        Text("¡Entendido!", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@Composable
fun FieldLabel(title: String, isOptional: Boolean = false) {
    Row(modifier = Modifier.padding(bottom = 8.dp)) {
        Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextGray)
        if (isOptional) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "(Opcional)", fontSize = 13.sp, color = TextLightGray)
        }
    }
}

@Composable
fun LabelledTextField(
    label: String,
    isOptional: Boolean = false,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    maxChars: Int? = null
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        FieldLabel(title = label, isOptional = isOptional)
        CustomOutlinedTextField(
            value = value,
            onValueChange = { if (maxChars == null || it.length <= maxChars) onValueChange(it) },
            placeholder = placeholder,
            icon = icon,
            keyboardType = keyboardType
        )
    }
}

@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.defaultMinSize(minHeight = 52.dp),
        shape = RoundedCornerShape(12.dp),
        singleLine = singleLine,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        placeholder = { Text(placeholder, color = TextLightGray) },
        leadingIcon = icon?.let { { Icon(it, contentDescription = null, tint = TextLightGray) } },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = BorderColor
        )
    )
}

@Composable
fun AmountCard(amount: String, onAmountChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().border(1.dp, BorderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            Text("MONTO DE DEUDA INICIAL *", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLightGray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = if (amount.isEmpty()) TextLightGray else Color.Black)
                BasicTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    textStyle = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Start),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(IntrinsicSize.Min),
                    decorationBox = { inner ->
                        if (amount.isEmpty()) Text("0.00", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = TextLightGray)
                        inner()
                    }
                )
            }
        }
    }
}