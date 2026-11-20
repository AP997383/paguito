package com.nexusystem.paguito.ui.screens.payments

import com.nexusystem.paguito.ui.components.*
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PagostoPreviewTiket
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.ui.screens.deudores.BorderColor
import com.nexusystem.paguito.ui.screens.deudores.CustomOutlinedTextField
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.deudores.FieldLabel
import com.nexusystem.paguito.ui.screens.productos.ChoseProductBottomSheet
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel
import com.nexusystem.paguito.utils.bottomsSheets.DeudoresBottomSheet
import com.nexusystem.paguito.utils.dialogs.SuccessAbonoDialog
import com.nexusystem.paguito.utils.getTodayDateString
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

// --- COLORES PRINCIPALES ---

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterSellScreen(deudorPrecarga:DeudoresEntity, onBackClick: () -> Unit = {}, openPreviewTicket:(PagostoPreviewTiket)->Unit, deudoresViewModel: DeudoresViewModel, pagosViewModel: PagosViewModel, productosViewModel: ProductosViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var amount by remember { mutableStateOf("0") }
    com.nexusystem.paguito.ui.components.PaymentOperationFeedback(pagosViewModel)
    var showSuccessPayment by remember { mutableStateOf(false) }
    var showListProducts by remember { mutableStateOf(false) }
    var showListDeudoresBottomSheeet by remember { mutableStateOf(false) }
    val listaBusquedaDeudores by deudoresViewModel.deudores.collectAsState()
    val listaBusquedaProductos by productosViewModel.produtosList.collectAsState()
    var selectedMethod by remember { mutableStateOf("Transfer") }
    var selectedMethodId by remember { mutableStateOf(1) }
    var nameInCard by remember { mutableStateOf(deudorPrecarga.nombre?:"Selecciona un deudor") }
    var ammountInCard by remember { mutableStateOf(deudorPrecarga.montoActualAdeudado.toString()?:"0.0") }
    var currentDeudorSelected by remember { mutableStateOf<DeudoresEntity?>(deudorPrecarga) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    var startDate: LocalDate? by remember {
        mutableStateOf(today)
    }
    val selectedProducts = remember { mutableStateListOf<PorductosEntity>() }
    var productSearch by remember { mutableStateOf("") }
    val startPickerState = rememberDatePickerState(
        initialSelectedDateMillis = (startDate ?: today).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    )
    val profile = pagosViewModel.profileState
    val contactText = listOfNotNull(profile?.email, profile?.phone).joinToString(" / ")
    var paymentData by remember{ mutableStateOf(PagosEntinty())}
    var paymentDatapreviewTiket by remember{ mutableStateOf(PagostoPreviewTiket())}
    if(showListProducts){
        ChoseProductBottomSheet({showListProducts = false},{
            showListProducts =false
            selectedProducts.add(it)
            Log.e("xxxxxx","->"+amount+"/"+it.precioConGanancia)
            if(amount.isNullOrEmpty())
                amount =  it.precioConGanancia.toString()
            else
                amount = ((amount.toFloatOrNull() ?: 0f) + it.precioConGanancia).toString()
                                                           },listaBusquedaProductos,productosViewModel)
    }
    if(showListDeudoresBottomSheeet){
        DeudoresBottomSheet({
            showListDeudoresBottomSheeet=false
        },{
            nameInCard = it.nombre
            ammountInCard = it.montoActualAdeudado.toString()
            currentDeudorSelected  = it
            showListDeudoresBottomSheeet=false
        },listaBusquedaDeudores)
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

    LaunchedEffect(Unit) {
        deudoresViewModel.obtenerDeudores()
        productosViewModel.obtenerProductos()
    }

    if(showSuccessPayment){
        SuccessAbonoDialog({
            showSuccessPayment = false
            onBackClick()
        },{
            showSuccessPayment =false
            openPreviewTicket(paymentDatapreviewTiket)
        },"Venta registrada","Se agrego la venta al deduor correctamente.\n ¿Deseas ver el  ticket?")
    }

    var notes by remember { mutableStateOf("") }
    Scaffold(modifier = Modifier.imePadding(),
        topBar = { ScreenHeader("Nueva venta", "Una venta más, todo bajo control", onBackClick) },
        bottomBar = {
            // Botón inferior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .paguitoBackground()
                    .navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                val isButtonEnabled =((amount.toFloatOrNull() ?: 0f)>0f) && (!nameInCard.equals("Selecciona un deudor") && !nameInCard.isNullOrEmpty())
                Button(
                    onClick = {
                        if(isButtonEnabled) {
                            val jsonCompleto: String = Gson().toJson(selectedProducts)
                            paymentDatapreviewTiket = PagostoPreviewTiket(
                                contactText,
                                currentDeudorSelected?.nombre.orEmpty(), "", (amount.toDoubleOrNull() ?: 0.0),
                                saldoAntesDeAbono = (currentDeudorSelected?.montoActualAdeudado ?: 0f).toDouble(),
                                startDate.toString(),
                                (currentDeudorSelected?.montoActualAdeudado ?: 0f).toDouble() + (amount.toDoubleOrNull() ?: 0.0),
                                1,
                                jsonCompleto,
                                isIngreso = false

                            )
                            paymentData = PagosEntinty(
                                null,
                                "",
                                currentDeudorSelected?.idRemoteDatabase.orEmpty(),
                                currentDeudorSelected?.id?.toString().orEmpty(),
                                (amount.toDoubleOrNull() ?: 0.0),
                                saldoAntesDeAbono = (currentDeudorSelected?.montoActualAdeudado ?: 0f).toDouble(),
                                startDate.toString(),
                                true,
                                selectedMethodId,
                                false,
                                notes,
                                jsonCompleto
                            )
                            pagosViewModel.guardarNuevaVenta(paymentData) { showSuccessPayment = true }
                        }else{
                            scope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = "Debes seleccionar un deudor y un monto mayor a 0",
                                    actionLabel = "Ok",
                                    duration = SnackbarDuration.Short
                                )

                                // 4. (Opcional) Detectar si el usuario hizo clic en la acción
                                when (result) {
                                    SnackbarResult.ActionPerformed -> {
                                        /* Lógica para deshacer o reaccionar */
                                    }
                                    SnackbarResult.Dismissed -> {
                                        /* Se cerró solo */
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    enabled = isButtonEnabled && !showSuccessPayment && !pagosViewModel.isSaving,
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        "Guardar venta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
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
                .paguitoBackground()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. DEUDOR SELECCIONADO
            SoftCard {
            Text("Cliente", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            DebtorCard({
                showListDeudoresBottomSheeet =true
            },nameInCard,ammountInCard)
            }

            Spacer(modifier = Modifier.height(24.dp))

            SoftCard {
            // 2. MONTO A REGISTRAR
            SectionLabel("Total de la venta")
            AmountInput(amount = amount, onAmountChange = { amount = it })

            Spacer(modifier = Modifier.height(24.dp))

            // 3. FECHA DE PAGO
            SectionLabel("Fecha de venta")
            DateSelectorCard({showStartDatePicker=true},startDate.toString())

            Spacer(modifier = Modifier.height(24.dp))
            FieldLabel(title = "Productos Relacionados", isOptional = true)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomOutlinedTextField(
                    value = productSearch,
                    onValueChange = { productSearch = it
                        showListProducts =true},
                    placeholder = "Buscar producto...",
                    icon = Icons.Outlined.Search,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { showListProducts =true },
                    modifier = Modifier.heightIn(min = 56.dp),
                    shape = MaterialTheme.shapes.large,
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
            }
            SoftCard {
            // 5. NOTAS ADICIONALES
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Description, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                SectionLabel("Notas adicionales", trailingText = "(Opcional)", modifier = Modifier.padding(bottom = 0.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            NotesInput(notes = notes, onNotesChange = { notes = it })
            }

            Spacer(modifier = Modifier.height(40.dp)) // Espacio extra para el scroll sobre el botón
        }
    }
}

@Composable
fun SelectedProductItem(
    producto: PorductosEntity,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f)) {
                Text(text = producto.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(10.dp)) // Espacio extra para el scroll sobre el botón
                Text(text = "$${producto.precioConGanancia}", color = BluePrimary, fontSize = 12.sp)
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = Color.Red, modifier = Modifier.size(20.dp))
            }
        }
    }
}
