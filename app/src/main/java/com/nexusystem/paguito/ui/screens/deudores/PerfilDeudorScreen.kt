package com.nexusystem.paguito.ui.screens.deudores

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PagostoPreviewTiket
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.payments.PeriodItem
import com.nexusystem.paguito.ui.screens.productos.RedAlert
import com.nexusystem.paguito.utils.bottomsSheets.AbonoBottomSheet
import com.nexusystem.paguito.utils.calcularSiguienteVencimiento
import com.nexusystem.paguito.utils.dialogs.SuccessAbonoDialog
import com.nexusystem.paguito.utils.dialogs.SuccessDialog
import com.nexusystem.paguito.utils.emptyStates.PaymentHistoryEmptyState
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.getTodayDateString
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.toInt

// --- COLORES PRINCIPALES ---
val BluePrimary = Color(0xFF3B82F6)
val RedAlert = Color(0xFFEF4444)
val BgColor = Color(0xFFF9FAFB)
val CardWhite = Color(0xFFFFFFFF)
val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF6B7280)
val TextLightGray = Color(0xFF9CA3AF)
val BorderColor = Color(0xFFE5E7EB)
val LightBlueBg = Color(0xFFF4F8FF)
val IconBgGray = Color(0xFFF3F4F6)

// --- MODELO DE DATOS ---
data class Transaction(
    val title: String,
    val dateText: String,
    val amount: String,
    val icon: ImageVector
)


// --- PANTALLA PRINCIPAL ---
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerProfileScreen(onBackClick: () -> Unit = {},deudorData: DeudoresEntity,viewmodelDeudro: DeudoresViewModel,pagosViewModel: PagosViewModel,
                          openPreviewTicket:(PagostoPreviewTiket)->Unit,openDetailVenta:(PagosEntinty)->Unit,openAcountState:(DeudoresEntity)->Unit) {
    var showFastPayment by remember { mutableStateOf(false) }
    val pagosPorCliente by pagosViewModel.pagos.collectAsState()
    val deleteSuccess by pagosViewModel.deleteSuccess.collectAsState()
    var showSuccessPayment by remember { mutableStateOf(false) }
    var paymentData by remember{ mutableStateOf(PagosEntinty())}
    var paymentDatapreviewTiket by remember{ mutableStateOf(PagostoPreviewTiket())}
    var currentpaymentSelected by remember{ mutableStateOf(PagosEntinty())}
    var pagado = 0.0f
    var saldoPendiente = 0.0f
    var showDeleteDialog by remember { mutableStateOf(false) }

    var filtroSeleccionado by remember { mutableStateOf(PagoFiltro.TODOS) }

    // 2. Filtrado inteligente de la lista usando derivedStateOf
    val listaFiltrada by remember {
        derivedStateOf {
            when (filtroSeleccionado) {
                PagoFiltro.TODOS -> pagosPorCliente
                PagoFiltro.ABONOS -> pagosPorCliente.filter { it?.isIngreso == true }
                PagoFiltro.VENTAS -> pagosPorCliente.filter { it?.isIngreso == false }
            }
        }
    }
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar eliminación", fontWeight = FontWeight.Bold) },
            text = { Text("Al borrar el pago se reajustaran los montos anteriores") },
            confirmButton = {
                TextButton(
                    onClick = {
                        pagosViewModel.deletePagoRemoteFirebase(currentpaymentSelected)
                        showDeleteDialog = false
                    }
                ) {
                    Text("ELIMINAR", color = RedAlert, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("CANCELAR")
                }
            }
        )
    }

    LaunchedEffect(pagosPorCliente) {
        if(pagosPorCliente.size>0) {
            pagosPorCliente.forEach {
                if (it!!.isIngreso) {
                    pagado += it!!.montoAbonado
                }
            }
        }
    }


    val profile = pagosViewModel.profileState
    val fechasPagosRealizados = remember(pagosPorCliente) {
        pagosPorCliente.filter { it?.isIngreso!! }.mapNotNull { pago ->
            pago?.fechaAbono?.let { fechaStr ->
                try {
                    // Intentar primero como fecha y hora (yyyy-MM-dd HH:mm:ss)
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    LocalDateTime.parse(fechaStr, formatter).toLocalDate()
                } catch (e: Exception) {
                    try {
                        // Si falla, intentar como fecha simple (yyyy-MM-dd)
                        LocalDate.parse(fechaStr)
                    } catch (e2: Exception) {
                        Log.e("DATA_CALCULATE", "Error al parsear fecha: $fechaStr", e2)
                        null
                    }
                }
            }
        }.sorted() // Ordenamos las fechas ya parseadas
    }
    Log.e("DATA_CALCULATE","-->"+pagosPorCliente)
    val proximaFecha by remember(deudorData.fechaInicialDeuda, fechasPagosRealizados) {
        derivedStateOf {
            try {
                val fechaInicio = LocalDate.parse(deudorData.fechaInicialDeuda)
                calcularSiguienteVencimiento(
                    fechaInicio,
                    deudorData.periodicidad,
                    fechasPagosRealizados
                )
            } catch (e: Exception) {
                LocalDate.now() // Fallback seguro
            }
        }
    }

    val esAtrasado = remember(proximaFecha) { proximaFecha.isBefore(LocalDate.now())}


    if(showSuccessPayment){
        SuccessAbonoDialog({
            showSuccessPayment = false
            onBackClick()
        },{
            showSuccessPayment =false
            openPreviewTicket(paymentDatapreviewTiket)
        },"Pago registrado","El abono se registro correctamente.\n ¿Deseas ver el  ticket?")
    }
    if(showFastPayment){
        val contactText = listOfNotNull(profile?.email, profile?.phone).joinToString(" / ")
        AbonoBottomSheet(onDismiss = {
            showFastPayment =false
        }, onConfirm ={monto, esPagoCompleto, enviarSms ->
            paymentDatapreviewTiket =PagostoPreviewTiket(
                contactText,
                deudorData.nombre,"",
                monto.toFloat().toInt(),
                saldoAntesDeAbono =      (deudorData.montoActualAdeudado -pagado  ).toInt(),
                getTodayDateString(),
                deudorData.montoActualAdeudado.toFloat().toInt() - monto.toFloat().toInt() ,
                1,
                "",
                isIngreso = true

            )
            paymentData = PagosEntinty(
                null,
                "",
                deudorData!!.idRemoteDatabase,
                deudorData!!.id.toString(),
                monto.toFloat().toInt(),
                saldoAntesDeAbono =  ( deudorData.montoActualAdeudado - pagado ).toInt(),
                getTodayDateString(),
                true,
                1,
                true,
                "",
                ""

            )
            pagosViewModel.guardarDeudor(paymentData)
            showFastPayment =false
            showSuccessPayment =true
        },
            deudorData.montoActualAdeudado,
            deudorData.nombre)
    }

    LaunchedEffect(Unit) {
        pagosViewModel.obtenerAbonos(deudorData.id.toString(),deudorData.idRemoteDatabase.toString())
    }
        if(deleteSuccess) {
            SuccessDialog("¡Se elimino el pago correctamente!",{
                pagosViewModel.resetdeleteSuccess()
                onBackClick()
            }, {
                pagosViewModel.resetdeleteSuccess()
                onBackClick()
            })

        }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Cliente", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextDark2) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Atrás", tint = TextDark2)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardWhite),
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showFastPayment = true },
                containerColor = BluePrimary2,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(56.dp)
            ) {
                Text("$", fontSize = 24.sp, fontWeight = FontWeight.Medium)
            }
        },
        containerColor = BgColor
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                item {
                // 1. BANNER DE ALERTA ROJO
                if (esAtrasado) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(RedAlert)
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Atrasado",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. CABECERA DEL PERFIL (Avatar, Nombre, Botones)
                ProfileHeaderSection(deudorData.nombre, deudorData.telefono,{
                    openAcountState(deudorData)
                })

                Spacer(modifier = Modifier.height(24.dp))

                saldoPendiente = deudorData.montoActualAdeudado
                // 3. TARJETA DE SALDO PENDIENTE
                BalanceCard(
                    deudorData.montoAcomulado,
                    deudorData.montoAcomulado- saldoPendiente,
                    saldoPendiente,
                    deudorData.periodicidad,
                    proximaFecha.toString()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 4. HISTORIAL DE TRANSACCIONES
                Text(
                    text = "Historial de Transacciones",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark2,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalArrangement = Arrangement.Center, // Centra los elementos horizontalmente
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PagoFiltro.values().forEach { filtro ->
                            val isSelected = filtroSeleccionado == filtro

                            Surface(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp) // Espaciado entre chips
                                    .clickable { filtroSeleccionado = filtro },
                                shape = RoundedCornerShape(20.dp), // Esquinas muy redondeadas tipo píldora
                                color = if (isSelected) Color(0xFF3283F6) else Color(0xFFF1F3F4) // Color azul o gris de image_14.png
                            ) {
                                Text(
                                    text = filtro.name, // Usar title con capitalización correcta
                                    color = if (isSelected) Color.White else Color(0xFF5F6368), // Texto blanco o gris oscuro
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp) // Padding interno generoso
                                )
                            }
                        }
                    }
                    }

                // 3. Barra Horizontal de Chips

                if (listaFiltrada.isEmpty()) {
                    item {   PaymentHistoryEmptyState(modifier = Modifier, {}) }
                } else {
                    items(listaFiltrada) { tx ->
                        if (tx != null) {
                            val contactText = listOfNotNull(profile?.email, profile?.phone).joinToString(" / ")
                            TransactionItem(transaction = tx, isLast = tx == listaFiltrada.last(),{
                                openDetailVenta(tx)
                            },{
                                paymentDatapreviewTiket =PagostoPreviewTiket(
                                    contactText,
                                    deudorData.nombre,profile?.bussinesName.toString(),tx.montoAbonado,
                                    saldoAntesDeAbono =  (tx.saldoAntesDeAbono).toInt(),
                                    getTodayDateString(),
                                    deudorData.montoActualAdeudado.toInt() - tx.montoAbonado,
                                    1,
                                    tx.jsonAbonoPorProducto,
                                    isIngreso = tx.isIngreso

                                )
                                openPreviewTicket(paymentDatapreviewTiket)
                            },{
                                currentpaymentSelected = tx
                                showDeleteDialog =true
                            })
                        }
                    }
                }
                item{
                    Spacer(modifier = Modifier.height(80.dp)) // Espacio para que el FAB no tape el contenido
                }

            }
        }
    }
}
enum class PagoFiltro {
    TODOS,
    VENTAS,
    ABONOS
}
// --- COMPONENTES SECUNDARIOS ---

@Composable
fun ProfileHeaderSection(name:String,phone: String,openAcountState:()->Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar Placeholder (En app real usa AsyncImage)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(BorderColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Person, contentDescription = null, tint = TextGray1, modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(name, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextDark2)
        Text(phone, fontSize = 14.sp, color = TextGray1)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // --- BOTÓN LLAMAR ---
            ActionIconButton(
                icon = Icons.Outlined.Phone,
                tint = BluePrimary2,
                label = "Llamar"
            ) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                context.startActivity(intent)
            }

            // --- BOTÓN WHATSAPP ---
            // --- BOTÓN WHATSAPP ---
            ActionIconButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                tint = TextDark2,
                label = "WhatsApp"
            ) {
                // 1. Limpiamos el número (solo dígitos)
                val cleanNumber = phone.replace(Regex("[^0-9]"), "")
                val waUrl = "https://api.whatsapp.com/send?phone=$cleanNumber"

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(waUrl)
                }

                try {
                    // Intentamos abrirlo. Al no poner .setPackage,
                    // Android buscará CUALQUIER app que maneje URLs de WhatsApp.
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Si por alguna razón falla el intent (raro en Android modernos),
                    // abrimos el navegador como último recurso
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                    context.startActivity(browserIntent)
                }
            }
            ActionIconButton(
                icon = Icons.Outlined.AccountBalance,
                tint = BluePrimary2,
                label = "Enviar Historial"
            ) {
                openAcountState()
            }
        }
    }
}

@Composable
fun ActionIconButton(icon: ImageVector, tint: Color, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = CardWhite,
            border = BorderStroke(1.dp, BorderColor),
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape) // Importante: clip antes del clickable para que el efecto visual sea circular
                .clickable { onClick() }
                .shadow(2.dp, CircleShape, spotColor = Color(0x1A000000))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = TextGray1)
    }
}

@Composable
fun BalanceCard(montoActualAdeudado:Float,pagado:Float,saldoPendiente:Float,periodicidad: String,proximaFecha: String) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardWhite,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp), spotColor = Color(0x1A000000))
    ) {
        Column {
            // Línea de acento azul en la parte superior
            Box(modifier = Modifier.fillMaxWidth().height(4.dp).background(BluePrimary2))

            Column(modifier = Modifier.padding(20.dp)) {
                Text("SALDO PENDIENTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextLightGray, letterSpacing = 1.sp)

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(formatAsCurrency(saldoPendiente), fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = TextDark2)
                    Text(" MXN", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextLightGray, modifier = Modifier.padding(bottom = 6.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Pagado:"+ formatAsCurrency(pagado), fontSize = 12.sp, color = TextDark2, fontWeight = FontWeight.Medium)
                    Text("De: "+formatAsCurrency(montoActualAdeudado), fontSize = 12.sp, color = TextLightGray)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Barra de progreso
                val progress = if (montoActualAdeudado > 0f) {
                    (pagado / montoActualAdeudado).coerceIn(0f, 1f)
                } else {
                    0f
                }
                Log.e("PROGRESS_BAR","-->"+progress)
                LinearProgressIndicator(
                    progress = { progress}, // 1500 de 6000
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = BluePrimary2,
                    trackColor = IconBgGray,
                    strokeCap = StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Próximo pago
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = LightBlueBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth() // Importante para que el peso tenga de donde repartir
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center // Asegura centrado general
                    ) {
                        // --- PRIMER ELEMENTO (50% del ancho) ---
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start // Alinea contenido al inicio de su mitad
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Outlined.CalendarToday,
                                    contentDescription = null,
                                    tint = BluePrimary2,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Próximo pago", fontSize = 11.sp, color = TextGray1)
                                Text(proximaFecha, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark2)
                            }
                        }

                        // --- SEGUNDO ELEMENTO (50% del ancho) ---
                        // Quitamos el size(100.dp) para que el weight controle el tamaño
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp),
                            horizontalArrangement = Arrangement.End, // Alinea el chip al final de su mitad
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val periods = remember(periodicidad) {
                                when(periodicidad) {
                                    "S" -> listOf(Triple("Semanal", Icons.Outlined.Update, 1f))
                                    "Q" -> listOf(Triple("Quincenal", Icons.Outlined.DateRange, 1f))
                                    "M" -> listOf(Triple("Mensual", Icons.Outlined.CalendarMonth, 1f))
                                    "A" -> listOf(Triple("Abierto", Icons.Outlined.AllInclusive, 1f))
                                    else -> emptyList()
                                }
                            }

                            periods.forEach { (text, icon, _) ->
                                PeriodItem(
                                    text = text,
                                    icon = icon,
                                    isSelected = true,
                                    modifier = Modifier.wrapContentWidth(), // El item solo ocupa lo que necesita
                                    onClick = { }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun TransactionItem(
    transaction: PagosEntinty,
    isLast: Boolean,
    openPaymentDetail: () -> Unit,
    openPreviewTiket: () -> Unit,
    onDelete: () -> Unit // Nuevo parámetro para la acción de eliminar
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp)
            .clickable { if (transaction.isIngreso) openPreviewTiket() else openPaymentDetail() }
    ) {
        // --- COLUMNA IZQUIERDA: Ícono y Línea conectora ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, BorderColor, CircleShape)
                    .background(IconBgGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.isIngreso) Icons.Outlined.Download else Icons.Outlined.LocalMall,
                    contentDescription = null,
                    tint = TextDark2,
                    modifier = Modifier.size(16.dp)
                )
            }
            if (!isLast) {
                Box(modifier = Modifier.width(1.dp).weight(1f).background(BorderColor))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- COLUMNA DERECHA: Timeline de Contenido ---
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 24.dp)
        ) {
            // Header: Título, Fecha y Botón de Eliminar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (transaction.isIngreso) "Abono" else "Venta",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (transaction.isIngreso) Color(0xFF2E7D32) else Color(0xFF1976D2)
                    )
                    Text(text = transaction.fechaAbono, fontSize = 11.sp, color = TextLightGray)
                }

                if (transaction.isIngreso) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de productos
            if (!transaction.jsonAbonoPorProducto.isNullOrEmpty()) {
                val listType = object : TypeToken<ArrayList<PorductosEntity>>() {}.type
                val listaRecuperada: ArrayList<PorductosEntity> = try {
                    Gson().fromJson(transaction.jsonAbonoPorProducto, listType)
                } catch (e: Exception) {
                    arrayListOf()
                }

                listaRecuperada.forEachIndexed { index, producto ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .border(1.dp, BorderColor, CircleShape)
                                .background(IconBgGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Sell,
                                contentDescription = null,
                                tint = TextDark2,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(text = producto.nombre, fontSize = 12.sp, color = Color.DarkGray)
                        Text(text = "---", fontSize = 12.sp, color = Color.DarkGray)
                        Text(
                            text = formatAsCurrency(producto.precioConGanancia),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    if (index < listaRecuperada.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            thickness = 0.5.dp,
                            color = BorderColor.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Total
            Text(
                text = "Total: ${formatAsCurrency(transaction.montoAbonado)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}