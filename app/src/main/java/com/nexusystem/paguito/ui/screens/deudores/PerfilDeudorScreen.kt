package com.nexusystem.paguito.ui.screens.deudores

import com.nexusystem.paguito.ui.components.toMovement
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
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.payments.PeriodItem
import com.nexusystem.paguito.ui.screens.productos.RedAlert
import com.nexusystem.paguito.utils.LoadingOverlay
import com.nexusystem.paguito.utils.bottomsSheets.AbonoBottomSheet
import com.nexusystem.paguito.utils.calcularSiguienteVencimiento
import com.nexusystem.paguito.utils.dialogs.PremiumLimitWatsapsReachedDialog
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
val LightBlueBg = Color(0xFFF4F8FF)
val IconBgGray: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceVariant

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
    val isLoading by viewmodelDeudro.isLoading.collectAsState()
    val pagosPorCliente by pagosViewModel.pagos.collectAsState()
    val deleteSuccess by pagosViewModel.deleteSuccess.collectAsState()
    val reminderSuccess by viewmodelDeudro.reminderSuccess.collectAsState()
    var showSuccessPayment by remember { mutableStateOf(false) }
    var paymentData by remember{ mutableStateOf(PagosEntinty())}
    var paymentDatapreviewTiket by remember{ mutableStateOf(PagostoPreviewTiket())}
    var currentpaymentSelected by remember{ mutableStateOf(PagosEntinty())}
    val pagado = pagosPorCliente.filterNotNull().filter { it.isIngreso }.sumOf { it.montoAbonado.toDouble() }.toFloat()
    val saldoPendiente = deudorData.montoActualAdeudado
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showReminders by remember { mutableStateOf(false) }
    var showNOMoreWatsaps by remember { mutableStateOf(false) }

    var filtroSeleccionado by remember { mutableStateOf(PagoFiltro.TODOS) }
    Log.e("DATA_CALCULATE","-->"+pagosPorCliente)

    if(reminderSuccess) {
        SuccessDialog("!Se envio el recordatorio a tu cliente correctamente!", {
            viewmodelDeudro.resetreminderSuccess()
            onBackClick()
        }, {
            onBackClick()
        })
    }

    val profile = pagosViewModel.profileState
    val fechasPagosRealizados = remember(pagosPorCliente) {
        pagosPorCliente.filterNotNull().filter { it.isIngreso }.mapNotNull { pago ->
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
    if(showNOMoreWatsaps){
        PremiumLimitWatsapsReachedDialog({
            showNOMoreWatsaps =false
        },{
            showNOMoreWatsaps =false
        })
    }

    if(showReminders){
        ElegirTipoRecordatorioBottomSheet({
            showReminders =false
        },{
            if(profile?.userSuscription?.isActive==true) {
                viewmodelDeudro.manualWatsap(
                    it,
                    deudorData.nombre,
                    deudorData.telefono,
                    saldoPendiente.toString(),
                    proximaFecha.toString()
                )
            }else{
                if(profile?.watsapSended?:0>=10){
                    showNOMoreWatsaps =true
                }
            }
            showReminders =false
        })
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

    fun previewMovement(tx: PagosEntinty) {
        val contactText = listOfNotNull(profile?.email, profile?.phone).joinToString(" / ")
        paymentDatapreviewTiket = PagostoPreviewTiket(
            contactText, deudorData.nombre, profile?.bussinesName.orEmpty(), tx.montoAbonado,
            saldoAntesDeAbono = tx.saldoAntesDeAbono, getTodayDateString(),
            deudorData.montoActualAdeudado.toInt() - tx.montoAbonado, 1,
            tx.jsonAbonoPorProducto, isIngreso = tx.isIngreso
        )
        openPreviewTicket(paymentDatapreviewTiket)
    }
    Box {
        com.nexusystem.paguito.ui.screens.deudores.components.CustomerProfileContent(
            client = deudorData, payments = pagosPorCliente.filterNotNull(),
            nextDate = if (runCatching { LocalDate.parse(deudorData.fechaInicialDeuda) }.isSuccess) proximaFecha.toString() else "",
            isLate = esAtrasado, onBack = onBackClick, onPayment = { showFastPayment = true },
            onCall = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${deudorData.telefono}"))) },
            onReminder = { showReminders = true }, onStatement = { openAcountState(deudorData) },
            onOpenMovement = { tx -> if (tx.isIngreso) previewMovement(tx) else openDetailVenta(tx) },
            onReceipt = { previewMovement(it) },
            onDelete = { currentpaymentSelected = it; showDeleteDialog = true }
        )
        LoadingOverlay(
            isLoading = isLoading,
            lottieRes = R.raw.loadings
        )
    }


}
enum class PagoFiltro {
    TODOS,
    VENTAS,
    ABONOS
}
// --- COMPONENTES SECUNDARIOS ---

@Composable
fun ProfileHeaderSection(name: String, phone: String, openAcountState: () -> Unit, OpenEndRangeeminders: () -> Unit) {
        val context = LocalContext.current
        com.nexusystem.paguito.ui.screens.deudores.components.CustomerIdentityCard(
            DeudoresEntity(nombre = name, telefono = phone),
            { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))) },
            OpenEndRangeeminders, openAcountState
        )
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
fun BalanceCard(montoActualAdeudado: Float, pagado: Float, saldoPendiente: Float, periodicidad: String, proximaFecha: String) {
        com.nexusystem.paguito.ui.screens.deudores.components.CustomerBalanceCard(montoActualAdeudado, pagado, saldoPendiente, periodicidad, proximaFecha)
    }


@Composable
fun TransactionItem(transaction: PagosEntinty, isLast: Boolean, openPaymentDetail: () -> Unit, openPreviewTiket: () -> Unit, onDelete: () -> Unit) {
        com.nexusystem.paguito.ui.components.MovementCard(transaction.toMovement(),
            onOpen = if (transaction.isIngreso) openPreviewTiket else openPaymentDetail,
            onReceipt = openPreviewTiket, onDelete = onDelete)
    }