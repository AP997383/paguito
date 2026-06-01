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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PagostoPreviewTiket
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.screens.deudores.BluePrimary2
import com.nexusystem.paguito.ui.screens.deudores.GrayBackground
import com.nexusystem.paguito.ui.screens.deudores.TextGray1
import com.nexusystem.paguito.utils.dialogs.GrayBorder
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.formatFecha
import com.nexusystem.paguito.utils.formatTicketNumber
import com.nexusystem.paguito.utils.generarCodigoBarras
import com.nexusystem.paguito.utils.generarReferenciaUnica
import com.nexusystem.paguito.utils.getFormattedDate
import kotlinx.coroutines.flow.StateFlow


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AccountClientStateScreen(
    nombreCliente: String = "Sin Nombre",
    idClienteFirebase:String="",
    total:Int=0,
    inicial:Int=0,
    idClienteLocal: Int=0,
    fecha: String = getFormattedDate(),
    onBack: () -> Unit,
    pagosViewModel: PagosViewModel
) {
    val nextNumberTiket by pagosViewModel.nextTikectNumber.collectAsState()
    val pagosPorCliente by pagosViewModel.pagos.collectAsState()
    val totalPagos by pagosViewModel.totalPagos.collectAsState()
    LaunchedEffect(Unit) {
        pagosViewModel.obtenerAbonos(idClienteLocal.toString(),idClienteFirebase)
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
                    text =  "Estado de cuenta",
                    fontWeight = FontWeight.ExtraBold, // Para que resalte en el recibo
                    fontSize = 20.sp,
                    color =  Color(0xFF1976D2), // Verde para abono, Azul para venta
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
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
                // Usamos un Box para poder encimar el sello
                Box(contentAlignment = Alignment.Center) {

                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        // --- SECCIÓN 1: HEADER ---
                        TicketAccountStateHeaderSection( nextNumberTiket.toString())

                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 10.dp))

                        // --- SECCIÓN 2: BILL TO / DATE ---
                        TicketBillToDateSectionAcountState(false, "", fecha, nombreCliente)

                        Spacer(modifier = Modifier.height(3.dp))

                        // --- SECCIÓN 3: TABLA DE MOVIMIENTOS ---
                        TicketAccountStateTableSection(pagosPorCliente)

                        Spacer(modifier = Modifier.height(32.dp))

                        HorizontalDivider(color = BorderColor, modifier = Modifier.padding(vertical = 24.dp))

                        // --- SECCIÓN 5: TOTALES ---
                        TicketTotalsSectionAcoountState(totalPagos.toString(), total,inicial.toString())

                        Spacer(modifier = Modifier.height(24.dp))
                    }


                    if (total.toInt() <= 0) {
                        Image(
                            painter = painterResource(id = R.drawable.liquidado), // Tu recurso aquí
                            contentDescription = "Sello de cuenta liquidada",
                            modifier = Modifier
                                .size(280.dp) // Un poco más grande para que destaque como sello
                                .rotate(-22f) // Rotación clásica de sello de oficina
                                .align(Alignment.Center), // Asegura que esté centrado en el Box
                            alpha = 0.35f, // Control de transparencia (0.0 a 1.0)
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }


            // --- SECCIÓN 6: FOOTER EXTERNO (COPYRIGHT / LINKS) ---
            TicketFooterExternalSection()
        }
    }
}
@Composable
fun TicketBillToDateSectionAcountState(isIngreso:Boolean,accountNum: String, transDate: String,nombreCliente:String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.weight(1f)) {
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
        }
    }
}
@Composable
fun TicketTotalsSectionAcoountState( abonado:String,total: Int,restante:String) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        TotalsRow(label = "Saldo Inicial", value = formatAsCurrency( total))
        TotalsRow(label = "Abonado", value = "- "+ formatAsCurrency( abonado))
        //TotalsRow(label = "IVA (0%)", value = tax)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Saldo Final", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextDark)
            Text(
                text = formatAsCurrency(restante),
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                textAlign = TextAlign.End
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun TicketAccountStateTableSection(items: List<PagosEntinty?>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Cabecera de la tabla
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TableHeadText(text = "FECHA", modifier = Modifier.weight(1.3f))
            TableHeadText(text = "CONCEPTO", modifier = Modifier.weight(1.5f))
            TableHeadText(text = "MONTO", modifier = Modifier.weight(2f), textAlign = TextAlign.End)
        }
        HorizontalDivider(color = TextDark, thickness = 1.dp)

        // Filas de la tabla
        items.forEach { item ->
            // Contenedor de la fila (para manejar el color de fondo especial)
            val rowModifier = if (false) {
                Modifier.fillMaxWidth().background(GreenPaid).padding(vertical = 5.dp, horizontal = 4.dp)
            } else {
                Modifier.fillMaxWidth().padding(vertical = 5.dp)
            }

            Row(
                modifier = rowModifier,
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Estilos de texto dinámicos
                val fontWeight = if (item?.isIngreso!!) FontWeight.Bold else FontWeight.Normal
                val textColor =if (item?.isIngreso!!) GreenPaid else RedDebt

                Text(
                    text = formatFecha(item?.fechaAbono!!),
                    fontSize = 12.sp,
                    color = TextGray,
                    modifier = Modifier.weight(1.3f)
                )
                val concepto = if (item.isIngreso) "ABONO" else "VENTA"
                Text(
                    text = concepto,
                    fontSize = 12.sp,
                    fontWeight = fontWeight,
                    color = TextDark, // Concepto suele ser más oscuro
                    modifier = Modifier.weight(1.5f).padding(horizontal = 4.dp)
                )
                Column(modifier = Modifier.weight(2f)) {
                    Text(
                        text = "Anterior : " +  formatAsCurrency(item.saldoAntesDeAbono.toString()),
                        fontSize = 12.sp,
                        fontWeight = fontWeight,
                        color = TextGray1,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text =(if (item.isIngreso) "- " else "+ ") +  formatAsCurrency(item.montoAbonado.toString()) ,
                        fontSize = 12.sp,
                        fontWeight = fontWeight,
                        color = textColor,
                        textAlign = TextAlign.End
                    )
                    Text(
                        text = "Nuevo:" + formatAsCurrency(if(item.isIngreso)(item.saldoAntesDeAbono - item.montoAbonado)else (item.saldoAntesDeAbono + item.montoAbonado) ),
                        fontSize = 12.sp,
                        fontWeight = fontWeight,
                        color = TextGray1,
                        textAlign = TextAlign.End
                    )
                }

            }
        }
    }
}


// --- SUB-COMPONENTES DETALLADOS ---

@Composable
fun TicketAccountStateHeaderSection(nextNumberTiket: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column {
            Text(text = "Estado de cuenta" , fontSize = 12.sp, color = TextDark, fontWeight = FontWeight.Bold)
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
        }
    }
}
