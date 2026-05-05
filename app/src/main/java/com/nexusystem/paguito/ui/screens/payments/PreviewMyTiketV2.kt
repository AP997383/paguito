package com.nexusystem.paguito.ui.screens.payments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PagostoPreviewTiket
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.screens.deudores.BluePrimary2
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.formatTicketNumber
import com.nexusystem.paguito.utils.generarCodigoBarras
import com.nexusystem.paguito.utils.generarReferenciaUnica
import com.nexusystem.paguito.utils.getFormattedDate


val TicketBg = Color(0xFFFFFFFF)

val GreenPaid = Color(0xFF15956F) // Verde para estados "PAID"
val GreenLightBg = Color(0xFFE6F9F3) // Fondo verde sutil
data class TicketItem(
    val date: String,
    val concept: String,
    val amount: String, // Usamos String para mostrar formato ($99.00 o -$50.00)
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val specialColor: Color? = null,
    val specialBg: Color? = null
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TicketReceiptScreen(
    nombreCliente: String = "Sin Nombre",
    isIngreso: Boolean,
    nombrenegocio: String = "",
    correonegocio:String="",
    fecha: String = getFormattedDate(),
    items: ArrayList<PagostoPreviewTiket> = arrayListOf<PagostoPreviewTiket>(),
    total: String = "$0.00",
    abonado: String = "$0.00",
    subtotal: String = "$0.00",
    tax: String = "$0.00",
    ordenId: String = "0",
    onBack: () -> Unit,
    pagosViewModel: PagosViewModel
) {
    val nextNumberTiket by pagosViewModel.nextTikectNumber.collectAsState()
    LaunchedEffect(Unit) {
        pagosViewModel.nextNumberTiket()
    }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
                floatingActionButton = {
                    FloatingActionButton(
                        modifier = Modifier.padding(end = 5.dp, bottom = 10.dp),
                        onClick = { coroutineScope.launch {
                            captureAndShareTicket(context, graphicsLayer)
                        } },
                        containerColor = BluePrimary2,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = "Agregar Cliente")
                    }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp)
                .verticalScroll(rememberScrollState()) // Soporte para pantallas pequeñas
                .padding(16.dp)

        ) {
            Row{
                Icon(
                    modifier = Modifier.clickable{
                        onBack()
                    },
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Regresar",
                    tint = Color.Black
                )
                Spacer(Modifier.width(20.dp))
                Text(
                    text = if (isIngreso) "Ticket de Abono" else "Ticket de Venta",
                    fontWeight = FontWeight.ExtraBold, // Para que resalte en el recibo
                    fontSize = 20.sp,
                    color = if (isIngreso) Color(0xFF2E7D32) else Color(0xFF1976D2), // Verde para abono, Azul para venta
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            // Contenedor blanco del ticket (Card o Surface)
            Surface(
                modifier = Modifier.fillMaxWidth().wrapContentHeight()
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    },
                shape = RoundedCornerShape(8.dp),
                color = TicketBg,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth()
                ) {
                    // --- SECCIÓN 1: HEADER (RECEIPT / ORDER ID / LOGO) ---
                    TicketHeaderSection(isIngreso,nextNumberTiket.toString(),nombrenegocio)

                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 24.dp))

                    // --- SECCIÓN 2: BILL TO / DATE / STATUS ---
                    TicketBillToDateSection(isIngreso,correonegocio, fecha,nombreCliente)

                    Spacer(modifier = Modifier.height(3.dp))

                    // --- SECCIÓN 3: TABLA DE MOVIMIENTOS ---
                    TicketTableSection(items)

                    Spacer(modifier = Modifier.height(32.dp))


                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 24.dp))

                    // --- SECCIÓN 5: TOTALES ---
                    TicketTotalsSection(isIngreso,subtotal, abonado, tax, total)

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // --- SECCIÓN 6: FOOTER EXTERNO (COPYRIGHT / LINKS) ---
            TicketFooterExternalSection()
        }
    }
}

// --- SUB-COMPONENTES DETALLADOS ---

@Composable
fun TicketHeaderSection(isIngreso:Boolean,nextNumberTiket: String,nombrenegocio: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(text = if(isIngreso) "RECIBO DE ABONO" else "RECIBO DE VENTA" , fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Bold)
            Text(text = "TICKET NO. "+ formatTicketNumber(nextNumberTiket.toInt()), fontSize = 10.sp, color = TextLightGray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                textAlign = TextAlign.End
            )
            Text(
                fontWeight = FontWeight.ExtraBold,
                text = nombrenegocio,
                fontSize = 10.sp,
                color = TextGray,
                lineHeight = 12.sp,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun TicketBillToDateSection(isIngreso:Boolean,accountNum: String, transDate: String,nombreCliente:String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = "Comunicarse a:", fontSize = 12.sp, color = TextLightGray, fontWeight = FontWeight.SemiBold)
            Text(
                text = " $accountNum",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Text(text = "Cliente: (" + nombreCliente+")", fontSize = 13.sp, color = TextGray)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(text = "Fecha de Impresión", fontSize = 12.sp, color = TextLightGray, fontWeight = FontWeight.SemiBold)
            Text(
                text = transDate,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Estatus: ", fontSize = 13.sp, color = TextGray)
                Text(text = if(isIngreso) "Pagado" else "Agregado", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = GreenPaid)
            }
        }
    }
}

@Composable
fun TicketTableSection(items: List<PagostoPreviewTiket>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Cabecera de la tabla
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TableHeadText(text = "FECHA", modifier = Modifier.weight(1f))
            TableHeadText(text = "CONCEPTO", modifier = Modifier.weight(1.8f))
            TableHeadText(text = "MONTO", modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        }
        HorizontalDivider(color = TextDark, thickness = 1.dp)

        // Filas de la tabla
        items.forEach { item ->
            // Contenedor de la fila (para manejar el color de fondo especial)
            val rowModifier = if (false) {
                Modifier.fillMaxWidth().background(GreenPaid).padding(vertical = 12.dp, horizontal = 4.dp)
            } else {
                Modifier.fillMaxWidth().padding(vertical = 12.dp)
            }

            Row(
                modifier = rowModifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estilos de texto dinámicos
                val fontWeight = if (item.isIngreso) FontWeight.Bold else FontWeight.Normal
                val textColor = if (item.isIngreso) RedDebt else TextGray

                Text(
                    text = item.fechaAbono,
                    fontSize = 12.sp,
                    color = TextGray,
                    modifier = Modifier.weight(1f)
                )
                val concepto = if (item.isIngreso) "ABONO" else "VENTA"
                Text(
                    text = concepto,
                    fontSize = 12.sp,
                    fontWeight = fontWeight,
                    color = TextDark, // Concepto suele ser más oscuro
                    modifier = Modifier.weight(1.8f).padding(horizontal = 4.dp)
                )
                Text(
                    text = formatAsCurrency(item.montoAbonado.toString()) ,
                    fontSize = 12.sp,
                    fontWeight = fontWeight,
                    color = textColor,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@Composable
fun TableHeadText(text: String, modifier: Modifier, textAlign: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = TextDark,
        modifier = modifier,
        textAlign = textAlign
    )
}


@Composable
fun TicketTotalsSection(isIngreso:Boolean,subtotal: String, credits: String, tax: String, total: String) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        TotalsRow(label = "Saldo Anterior", value = formatAsCurrency( subtotal))
        TotalsRow(label =if(isIngreso) "PAGO(S)" else "VENTA(S)", value =  formatAsCurrency(credits))
        //TotalsRow(label = "IVA (0%)", value = tax)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Saldo Final", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(
                text = formatAsCurrency(total),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun TotalsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = TextGray)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark, textAlign = TextAlign.End)
    }
}

@Composable
fun TicketFooterExternalSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "© 2026 PAGUITO.", fontSize = 10.sp, color = TextLightGray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            FooterLinkText("TERMINOS")
            Text(text = "   ", fontSize = 10.sp) // Espaciador simple
            FooterLinkText("PRIVACIDAD")
            Text(text = "   ", fontSize = 10.sp)
            FooterLinkText("SOPORTE")
        }
    }
}

@Composable
fun FooterLinkText(text: String) {
    Text(text = text, fontSize = 10.sp, color = TextGray, fontWeight = FontWeight.SemiBold)
}


