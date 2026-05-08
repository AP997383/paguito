package com.nexusystem.paguito.ui.screens.deudores

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.ui.screens.payments.DateSelectorCard
import com.nexusystem.paguito.ui.screens.payments.PeriodicitySelectorRow
import com.nexusystem.paguito.ui.screens.payments.SectionLabel
import com.nexusystem.paguito.ui.screens.payments.SelectedProductItem
import com.nexusystem.paguito.ui.screens.productos.ChoseProductBottomSheet
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

// --- COLORES PRINCIPALES ---
val GreenPrimary =Color( 0xFF15956F)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtorScreen(deudoresViewModel:DeudoresViewModel,onBackClick: () -> Unit = {},productosViewModel: ProductosViewModel) {
    // Estados de los campos
    val listaBusquedaProductos by productosViewModel.produtosList.collectAsState()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var selectedPeriod by remember { mutableStateOf("M") } // Nuevo
    var amount by remember { mutableStateOf("") }
    var productSearch by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    var startDate: LocalDate? by remember {
        mutableStateOf(today)
    }
    val isFormValid by remember {
        derivedStateOf {
            name.isNotBlank() && phone.isNotBlank() && amount.isNotBlank()
        }
    }
    var showListProducts by remember { mutableStateOf(false) }
    val startPickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDate!!.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
    val selectedProducts = remember { mutableStateListOf<PorductosEntity>() }

    LaunchedEffect(Unit) {
        productosViewModel.obtenerProductos()
    }
    if(showListProducts){
        ChoseProductBottomSheet({showListProducts = false},{
            showListProducts =false
            selectedProducts.add(it)
            Log.e("xxxxxx","->"+amount+"/"+it.precioConGanancia)
            if(amount.isNullOrEmpty())
                amount =  it.precioConGanancia.toString()
            else
                amount = (amount.toFloat() + it.precioConGanancia).toString()
        },listaBusquedaProductos)
    }

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = startPickerState.selectedDateMillis
                        if (millis != null) {
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            startDate = LocalDate.of(date.year, date.monthValue, date.dayOfMonth)
                        }
                        showStartDatePicker = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancelar") } }
        ) { DatePicker(state = startPickerState) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Nuevo Cliente", fontSize = 18.sp, fontWeight = FontWeight.Bold, color =  MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Atrás", tint =  MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            // Botón inferior fijo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 50.dp, end = 16.dp, start = 16.dp)
            ) {
                Button(
                    onClick = {
                        val jsonCompleto: String = Gson().toJson(selectedProducts)
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
                                jsonCompleto,
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
                    enabled = isFormValid, // <--- APLICAMOS LA VALIDACIÓN AQUÍ
                ) {
                    Text(
                        "Guardar y Crear Deuda",
                        fontSize = 16.sp,
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
                .imePadding() // Añade padding cuando el teclado está abierto
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // --- 1. INFORMACIÓN PERSONAL ---
            Text("Información Personal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))

            // Campo: Nombre
            LabelledTextField(
                label = "Nombre *",
                value = name,
                onValueChange = { name = it },
                placeholder = "Ej. Juan Pérez",
                icon = Icons.Outlined.Person
            )

            // Campo: Teléfono
            LabelledTextField(
                label = "Teléfono *",
                value = phone,
                onValueChange = { phone = it },
                placeholder = "Ej. 55 1234 5678",
                icon = Icons.Outlined.Phone,
                keyboardType = KeyboardType.Phone,
                maxChars = 10
            )

            // Campo: Domicilio
            LabelledTextField(
                label = "Domicilio",
                isOptional = true,
                value = address,
                onValueChange = { address = it },
                placeholder = "Calle, número, colonia...",
                icon = Icons.Outlined.LocationOn
            )

            Spacer(modifier = Modifier.height(8.dp))
            Spacer(modifier = Modifier.height(24.dp))

            // --- 2. DETALLES DEL CRÉDITO INICIAL ---
            Text("Detalles del Crédito Inicial", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta: Monto de Deuda
            AmountCard(amount = amount, onAmountChange = { amount = it })

            Spacer(modifier = Modifier.height(24.dp))
            SectionLabel("Fecha de pago")
            DateSelectorCard({
                 showStartDatePicker=true
            },startDate.toString())
            Spacer(modifier = Modifier.height(24.dp))
// 3. PERIODICIDAD DE PAGO (NUEVO)
            SectionLabel("Periodicidad de pago")
            PeriodicitySelectorRow(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = { selectedPeriod = it }
            )
            Spacer(modifier = Modifier.height(24.dp))
            // Fila: Productos Relacionados
            FieldLabel(title = "Productos Relacionados", isOptional = true)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomOutlinedTextField(
                    value = productSearch,
                    onValueChange = { productSearch = it },
                    placeholder = "Buscar producto...",
                    icon = Icons.Outlined.Search,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { showListProducts =true },
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, BorderColor),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor =  MaterialTheme.colorScheme.onSurface)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp), tint =  MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Añadir", fontWeight = FontWeight.SemiBold, color =  MaterialTheme.colorScheme.onSurface)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedProducts.isNotEmpty()) {
                SectionLabel("Productos seleccionados")
                selectedProducts.forEach { producto ->
                    SelectedProductItem(
                        producto = producto,
                        onRemove = {
                            // Al eliminar:
                            // 1. Restamos del total
                            val currentAmount = amount.toFloatOrNull() ?: 0f
                            val newAmount = (currentAmount - producto.precioConGanancia).coerceAtLeast(0f)
                            amount = if (newAmount == 0f) "" else newAmount.toString()

                            // 2. Quitamos de la lista
                            selectedProducts.remove(producto)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Campo: Notas / Observaciones
            FieldLabel(title = "Notas / Observaciones")
            CustomOutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Acuerdos de pago, referencias, detalles del producto, etc.",
                singleLine = false,
                modifier = Modifier.height(100.dp)
            )

            Spacer(modifier = Modifier.height(40.dp)) // Espacio final
        }
    }
}

// --- COMPONENTES REUTILIZABLES ---

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
    maxChars: Int? = null // Nuevo parámetro opcional
) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        FieldLabel(title = label, isOptional = isOptional)
        CustomOutlinedTextField(
            value = value,
            // Aquí filtramos el cambio
            onValueChange = { newValue ->
                if (maxChars == null || newValue.length <= maxChars) {
                    onValueChange(newValue)
                }
            },
            placeholder = placeholder,
            icon = icon,
            keyboardType = keyboardType
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
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
        placeholder = { Text(placeholder, color = TextLightGray, fontSize = 15.sp) },
        leadingIcon = icon?.let {
            { Icon(it, contentDescription = null, tint = TextLightGray, modifier = Modifier.size(20.dp)) }
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor =  MaterialTheme.colorScheme.surface,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor =  MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor =  MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun AmountCard(amount: String, onAmountChange: (String) -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color(0x0A000000)
            )
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Text(
                "MONTO DE DEUDA INICIAL *",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextLightGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Símbolo de moneda
                Text("$", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = if (amount.isEmpty()) TextLightGray else  MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(4.dp))

                // Campo de texto básico para permitir centrado y tamaño grande sin bordes
                BasicTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    textStyle = TextStyle(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color =  MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.width(IntrinsicSize.Min),
                    decorationBox = { innerTextField ->
                        if (amount.isEmpty()) {
                            Text("0.00", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = TextLightGray)
                        }
                        innerTextField()
                    }
                )
            }
        }
    }
}