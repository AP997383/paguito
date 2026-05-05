package com.nexusystem.paguito.ui.screens.payments

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PagostoPreviewTiket
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.generarCodigoBarras
import com.nexusystem.paguito.utils.generarReferenciaUnica

// Colores del diseño
val PaguitoDarkBlue = Color(0xFF1A2C42)
val PaguitoGold = Color(0xFFD4AF37)
val PaguitoBg = Color(0xFFF5F5F5)



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketShareScreen(onBack: () -> Unit,payment: PagostoPreviewTiket) { // Agregué un callback para el botón de atrás
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val graphicsLayer = rememberGraphicsLayer()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = if (payment.isIngreso) "Ticket de Abono" else "Ticket de Venta",
                        fontWeight = FontWeight.ExtraBold, // Para que resalte en el recibo
                        fontSize = 20.sp,
                        color = if (payment.isIngreso) Color(0xFF2E7D32) else Color(0xFF1976D2), // Verde para abono, Azul para venta
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Regresar",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = PaguitoDarkBlue
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        captureAndShareTicket(context, graphicsLayer)
                    }
                },
                containerColor = Color(0xFF25D366),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Send, contentDescription = null) },
                text = { Text("Compartir por WhatsApp", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White) // Fondo gris clarito para que resalte el ticket blanco
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // El Box que captura los pixeles del ticket
            Box(
                modifier = Modifier
                    .drawWithContent {
                        graphicsLayer.record {
                            this@drawWithContent.drawContent()
                        }
                        drawLayer(graphicsLayer)
                    }
            ) {
                TicketPreview(payment)
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// Esta función hace toda la magia
suspend fun captureAndShareTicket(context: Context, graphicsLayer: androidx.compose.ui.graphics.layer.GraphicsLayer) {
    // 1. Convertir el GraphicsLayer a Bitmap
    val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()

    // 2. Guardar el Bitmap en el caché (usando la ruta que configuraste en file_paths.xml)
    val imagesFolder = File(context.cacheDir, "tickets")
    imagesFolder.mkdirs()
    val file = File(imagesFolder, "ticket_abono.png")
    val stream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    stream.flush()
    stream.close()

    // 3. Obtener la URI mediante FileProvider
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        file
    )

    // 4. Lanzar el Intent de WhatsApp
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, "Hola, adjunto el comprobante de tu abono.")
        setPackage("com.whatsapp")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(Intent.createChooser(intent, "Compartir ticket"))
}

@Composable
fun TicketPreview(payment: PagostoPreviewTiket) {

    // Generamos la referencia (como hicimos en el paso anterior)
    val referencia = remember { generarReferenciaUnica("DEP") }

// Generamos el bitmap del código de barras
    val barcodeBitmap = remember(referencia) {
        generarCodigoBarras(referencia, 300, 100) // Resolución interna
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier.width(350.dp)
        ) {
            Column {
                // --- SECCIÓN SUPERIOR (QR y Logo) ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    // 1. Cambiamos SpaceBetween por Center
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 2. Esta Column ahora estará en el centro exacto de la Row
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = payment.nameBussines ?: "PAGUITO",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = PaguitoDarkBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (payment.isIngreso) "RECIBO DE ABONO" else "RECIBO DE VENTA",
                            fontWeight = FontWeight.Bold,
                            color = PaguitoDarkBlue
                        )
                        Text(
                            text = if (payment.isIngreso) "¡Gracias por tu pago!" else "¡Gracias por su compra!",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                DashedDivider()

                // --- SECCIÓN AZUL (Monto y Fecha) ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PaguitoDarkBlue)
                        .padding(20.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("MONTO ABONADO:", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text("$"+payment.montoAbonado, color = PaguitoGold, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("FECHA:", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(payment.fechaAbono, color = PaguitoGold, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Resumen de Cuenta (Cuadro Blanco Interno)
                   /* Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("RESUMEN DE CUENTA", fontSize = 11.sp, fontWeight = FontWeight.Black, color = PaguitoDarkBlue)
                            Spacer(modifier = Modifier.height(4.dp))
                            TicketRow("DEUDA: Bicicleta MTB", "ABONO: $200.00")
                            TicketRow("DEUDA: Laptop Gamer", "ABONO: $300.00")
                        }
                    }*/

                    Spacer(modifier = Modifier.height(16.dp))

                    // Información del Cliente
                    Text("INFORMACIÓN DEL CLIENTE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Nombre : "+payment.nameClient, color = Color.White, fontSize = 13.sp)
                       // Text("#15", color = Color.White, fontSize = 13.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("DEUDA TOTAL PENDIENTE", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(formatAsCurrency(payment.ammountTotal), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // --- SECCIÓN INFERIOR (Detalles y Código de Barras) ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text("DETALLES ADICIONALES", fontSize = 11.sp, fontWeight = FontWeight.Black, color = PaguitoDarkBlue)
                    Text("Referencia: "+referencia, fontSize = 13.sp, color = PaguitoDarkBlue)

                    Spacer(modifier = Modifier.height(20.dp))

                    Text("¡Sigue así!", color = PaguitoGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)

                    // Placeholder Código de Barras
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .width(220.dp)
                            .height(65.dp) // Un poco más alto para que se vea bien
                            .background(Color.White, shape = RoundedCornerShape(4.dp))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (barcodeBitmap != null) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = barcodeBitmap.asImageBitmap(),
                                    contentDescription = "Código de barras $referencia",
                                    modifier = Modifier.fillMaxWidth().height(60.dp),
                                    contentScale = ContentScale.FillBounds
                                )
                                // Opcional: El texto pequeño debajo de las barras
                                Text(
                                    text = referencia,
                                    fontSize = 8.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            // Fallback en caso de error
                            Box(Modifier.fillMaxSize().background(Color.LightGray))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TicketRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PaguitoDarkBlue)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = PaguitoDarkBlue)
    }
}

@Composable
fun DashedDivider() {
    Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
        drawLine(
            color = Color.LightGray,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}
