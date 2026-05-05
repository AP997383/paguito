package com.nexusystem.paguito.ui.screens.payments

import android.os.Build
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PagostoPreviewTiket
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.utils.bottomsSheets.DeudoresBottomSheet
import com.nexusystem.paguito.utils.dialogs.AbonoMayorSaldoDialog
import com.nexusystem.paguito.utils.dialogs.PremiumLimitReachedDialog
import com.nexusystem.paguito.utils.dialogs.SuccessAbonoDialog
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.getTodayDateString
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

// --- COLORES PRINCIPALES ---
val BluePrimary = Color(0xFF3B82F6)
val BlueLightBg = Color(0xFFEFF6FF)
val BlueDisabled = Color(0xFF93C5FD)
val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF6B7280)
val TextLightGray = Color(0xFF9CA3AF)
val BorderColor = Color(0xFFE5E7EB)
val RedDebt = Color(0xFFEF4444)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPaymentScreen(deudorPrecarga:DeudoresEntity, onBackClick: () -> Unit = {}, openPreviewTicket:(PagostoPreviewTiket)->Unit, deudoresViewModel: DeudoresViewModel, pagosViewModel: PagosViewModel) {
    // Estados de la pantalla
    var amount by remember { mutableStateOf("") }
    var showSuccessPayment by remember { mutableStateOf(false) }
    var showListDeudoresBottomSheeet by remember { mutableStateOf(false) }
    val listaBusquedaDeudores by deudoresViewModel.deudores.collectAsState()
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
    var notes by remember { mutableStateOf("") }
    val profile = pagosViewModel.profileState
    var paymentData by remember{ mutableStateOf(PagosEntinty())}
    var showAlertFreeLimited by remember { mutableStateOf(false) }
    if(showAlertFreeLimited)
    {
        AbonoMayorSaldoDialog(formatAsCurrency(deudorPrecarga.montoActualAdeudado.toString()),formatAsCurrency(amount),{
            showAlertFreeLimited =false
        },{
            showAlertFreeLimited =false
        },{
            showAlertFreeLimited =false
            paymentData = PagosEntinty(
                null,
                "",
                currentDeudorSelected!!.idRemoteDatabase,
                currentDeudorSelected!!.id.toString(),
                amount.toInt(),
                saldoAntesDeAbono =  currentDeudorSelected!!.montoActualAdeudado.toInt(),
                getTodayDateString(),
                true,
                selectedMethodId,
                true,
                notes,
                ""

            )
            pagosViewModel.guardarDeudor(paymentData)
            showSuccessPayment = true
        })
    }

    if(amount.isNullOrEmpty()){
        amount  ="0"
    }
    val contactText = listOfNotNull(profile?.email, profile?.phone).joinToString(" / ")
    var paymentDatapreviewTiket by remember{ mutableStateOf(PagostoPreviewTiket(
        contactText,

        nameInCard,"",amount.toInt(),
        saldoAntesDeAbono = currentDeudorSelected!!.montoActualAdeudado.toInt(),
        getTodayDateString(),
        deudorPrecarga.montoActualAdeudado.toInt(),
        selectedMethodId,
        ""

    ))}
    val startPickerState = rememberDatePickerState(
        initialSelectedDateMillis = startDate!!.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    )
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
    }

    if(showSuccessPayment){
        SuccessAbonoDialog({
            showSuccessPayment = false
            onBackClick()
        },{
            showSuccessPayment =false
            openPreviewTicket(paymentDatapreviewTiket)
        },"Pago registrado","El abono se registro correctamente.\n ¿Deseas ver el  ticket?")
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Pago", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Atrás", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            // Botón inferior
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(bottom = 50.dp, end = 16.dp, start = 16.dp)
            ) {
                val isButtonEnabled =(amount.isNotEmpty() && amount.toInt()>0) && (!nameInCard.equals("Selecciona un deudor") && !nameInCard.isNullOrEmpty())

                Button(
                    onClick = {
                        if(isButtonEnabled) {

                            if(deudorPrecarga.montoActualAdeudado < amount.toInt() ){
                                showAlertFreeLimited = true
                            }else{
                                paymentData = PagosEntinty(
                                    null,
                                    "",
                                    currentDeudorSelected!!.idRemoteDatabase,
                                    currentDeudorSelected!!.id.toString(),
                                    amount.toInt(),
                                    saldoAntesDeAbono =  currentDeudorSelected!!.montoActualAdeudado.toInt(),
                                    getTodayDateString(),
                                    true,
                                    selectedMethodId,
                                    true,
                                    notes,
                                    ""

                                )
                                pagosViewModel.guardarDeudor(paymentData)
                                showSuccessPayment = true
                            }


                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isButtonEnabled) BluePrimary else BlueDisabled
                    )
                ) {
                    Text(
                        "Guardar pago",
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
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // 1. DEUDOR SELECCIONADO
            SectionLabel("Deudor seleccionado")
            DebtorCard({
                showListDeudoresBottomSheeet =true
            },nameInCard,ammountInCard)

            Spacer(modifier = Modifier.height(24.dp))

            // 2. MONTO A REGISTRAR
            SectionLabel("Monto a registrar")
            AmountInput(amount = amount, onAmountChange = { amount = it })

            Spacer(modifier = Modifier.height(24.dp))

            // 3. FECHA DE PAGO
            SectionLabel("Fecha de pago")
            DateSelectorCard({showStartDatePicker=true},startDate.toString())

            Spacer(modifier = Modifier.height(24.dp))

            Spacer(modifier = Modifier.height(24.dp))
            // 4. MÉTODO DE PAGO
            SectionLabel("Método de pago")
            PaymentMethodsRow(
                selectedMethod = selectedMethod,
                onMethodSelected = {metodoLabel,metodoId ->
                    selectedMethodId = metodoId
                    selectedMethod = metodoLabel
                 }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. NOTAS ADICIONALES
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Description, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                SectionLabel("Notas adicionales", trailingText = "(Opcional)", modifier = Modifier.padding(bottom = 0.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            NotesInput(notes = notes, onNotesChange = { notes = it })

            Spacer(modifier = Modifier.height(40.dp)) // Espacio extra para el scroll sobre el botón
        }
    }
}

// --- COMPONENTES SECUNDARIOS ---

@Composable
fun SectionLabel(text: String, trailingText: String? = null, modifier: Modifier = Modifier.padding(bottom = 8.dp)) {
    Row(modifier = modifier) {
        Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        if (trailingText != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = trailingText, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun DebtorCard(onnclcikCard:()-> Unit,nameInCard: String,ammountInCard: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BorderColor),
                contentAlignment = Alignment.Center
            ) {
                // Placeholder de imagen
                Icon(Icons.Default.Person, contentDescription = null, tint = TextGray)
            }
            Spacer(modifier = Modifier.width(12.dp))

            // Textos
            Column(modifier = Modifier.weight(1f).clickable{
                onnclcikCard()
            }) {
                Text(text = if (nameInCard.isNullOrBlank()) "Seleccionar un deudor" else nameInCard, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Deuda: $$ammountInCard", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = RedDebt)
            }

            // Flecha circular
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountInput(amount: String, onAmountChange: (String) -> Unit) {
    OutlinedTextField(
        value = amount,
        onValueChange = { newValue ->
            // Filtro: Solo permite caracteres que sean dígitos (0-9)
            if (newValue.all { it.isDigit() }) {
                onAmountChange(newValue)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        // Cambiamos a NumberPassword o solo Number para evitar puntos/comas en algunos teclados
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        textStyle = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface),
        placeholder = {
            Text("0", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        },
        leadingIcon = {
            Text("$", fontSize = 32.sp, fontWeight = FontWeight.Medium, color = BluePrimary, modifier = Modifier.padding(start = 12.dp))
        },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = BluePrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun DateSelectorCard(openCalendar:()->Unit,startDate: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { openCalendar() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de calendario con fondo azul clarito
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(BlueLightBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))

            Text(startDate, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))

            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun PaymentMethodsRow(selectedMethod: String, onMethodSelected: (String,Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment =Alignment.CenterVertically
    ) {
        MethodItem("Efectivo", Icons.Outlined.Money, selectedMethod == "Efectivo", Modifier.weight(1f)) { onMethodSelected("Efectivo",1) }
        MethodItem("Transfer", Icons.Outlined.AccountBalance, selectedMethod == "Transfer", Modifier.weight(1f)) { onMethodSelected("Transfer",2) }
        MethodItem("Tarjeta", Icons.Outlined.CreditCard, selectedMethod == "Tarjeta", Modifier.weight(1f)) { onMethodSelected("Tarjeta",3) }
    }
}

@Composable
fun MethodItem(text: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val borderColor = if (isSelected) BluePrimary else MaterialTheme.colorScheme.surface
    val bgColor = if (isSelected) BlueLightBg else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) BluePrimary else TextGray

    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        color = bgColor,
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(icon, contentDescription = text, tint = contentColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = contentColor)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesInput(notes: String, onNotesChange: (String) -> Unit) {
    OutlinedTextField(
        value = notes,
        onValueChange = onNotesChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text("Añadir número de referencia, detalles del depósito...", color = TextLightGray, fontSize = 14.sp) },
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = BorderColor,
            focusedBorderColor = BluePrimary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun PeriodicitySelectorRow(selectedPeriod: String, onPeriodSelected: (String) -> Unit) {
    // Usamos un Row con scroll horizontal si los elementos no caben,
    // o simplemente Arrangement.spacedBy si son fijos.
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val periods = listOf(
            Triple("Semanal", Icons.Outlined.Update, 1f),
            Triple("Quincenal", Icons.Outlined.DateRange, 1.2f), // Un poco más ancho para el texto
            Triple("Mensual", Icons.Outlined.CalendarMonth, 1f),
            Triple("Abierto", Icons.Outlined.AllInclusive, 1f)
        )

        periods.forEach { (text, icon, weight) ->
            PeriodItem(
                text = text,
                icon = icon,
                isSelected = selectedPeriod == text.substring(0,1),
                modifier = Modifier.weight(weight),
                onClick = { onPeriodSelected(text.substring(0,1)) }
            )
        }
    }
}

@Composable
fun PeriodItem(text: String, icon: ImageVector, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val borderColor = if (isSelected) BluePrimary else MaterialTheme.colorScheme.surface
    val bgColor = if (isSelected) BlueLightBg else MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) BluePrimary else TextGray

    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
        color = bgColor,
        modifier = modifier
            .height(70.dp) // Un poco más bajo que el de pagos para diferenciar secciones
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(4.dp)
        ) {
            Icon(icon, contentDescription = text, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 11.sp, // Texto ligeramente más pequeño para que quepa bien
                fontWeight = FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}