package com.nexusystem.paguito.ui.screens.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.Brush
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.deudores.IconBgGray
import com.nexusystem.paguito.ui.screens.deudores.TextDark2
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.perfil.BorderColor
import com.nexusystem.paguito.ui.screens.productos.NativeAdBanner
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel
import com.nexusystem.paguito.utils.dashedBorder
import com.nexusystem.paguito.utils.dialogs.PremiumLimitReachedDialog
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.formatFecha
import com.nexusystem.paguito.utils.formatLongDateTime
import com.nexusystem.paguito.utils.getDaysUntilNextPayment
import com.nexusystem.paguito.utils.toPx
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// --- COLORES ---
val BluePrimary = Color(0xFF1AAF83)
val TextDark = Color(0xFF1F2937)
val TextGray = Color(0xFF6B7280)
val BorderLight = Color(0xFFF3F4F6)
val RedUrgent = Color(0xFFEF4444)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(seeDeudorProfile:(DeudoresEntity)->Unit, registerPayment:() ->Unit,
                    registerDebtor:() ->Unit,
                    registerProduct:() ->Unit,
                    registerCampaing:() ->Unit,
                    registerNewSell:() ->Unit,
                    seeAllDeudores:()->Unit,
                    seeAllPayments:()->Unit,
                    goToMyProfile:()->Unit,
                    deudoresViewModel: DeudoresViewModel,
                    pagosViewModel: PagosViewModel,productosViewmodel: ProductosViewModel) {
    val deudoresHisotiral by deudoresViewModel.deudores.collectAsState()
    val sumary1 by deudoresViewModel.sumaryFive.collectAsState()
    val numeroProductos by productosViewmodel.numProducts.collectAsState()
    var isBalanceVisible by remember { mutableStateOf(false) }
    val pagosHistorial by pagosViewModel.pagosConNombre5.collectAsState()
    val profile = deudoresViewModel.profileState
    val pagosPormes by pagosViewModel.pagosByMonth.collectAsState()
    var isSucriptionActive by remember { mutableStateOf(false) }
    var showAlertFreeLimited by remember { mutableStateOf(false) }
    var urlPhotoProfile by remember { mutableStateOf("") }
    val deudoresOrdenados = remember(deudoresHisotiral) {
        deudoresHisotiral
            .filterNotNull()
            .map { deudor ->
                val daysRemaining = getDaysUntilNextPayment(deudor.fechaInicialDeuda, deudor.periodicidad)
                deudor to daysRemaining
            }
            .sortedBy { it.second }
            .map { it.first } // <--- AGREGA ESTO: Extrae solo el deudor, manteniendo el orden físico
    }

    LaunchedEffect(Unit) {
        productosViewmodel.obtenerNumeroProductos()
        deudoresViewModel.loadUserProfile()
        deudoresViewModel.obtenerDeudores()
        deudoresViewModel.obtener5DatosCards()
        pagosViewModel.obtenerUltimos5Abonos()
        pagosViewModel.obtenerAbonosdelMes()
    }

    if(showAlertFreeLimited)
    {
        PremiumLimitReachedDialog({
            showAlertFreeLimited =false
        },{
            showAlertFreeLimited =false
        })
    }
    LaunchedEffect(profile) {
        if (profile != null) {
            Log.e("PROFILE_PHOTO","-->"+profile)
            isSucriptionActive = profile.userSuscription.isActive
            urlPhotoProfile =profile.fotoUrl
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. App Bar
        TopBar(urlPhotoProfile,{
            goToMyProfile()
        })


        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Tarjeta Principal
            BalanceCarousel(pagosPormes,sumary1?: DeudoresSummary(0,0f),isBalanceVisible,{
                isBalanceVisible = !isBalanceVisible
            })
            Spacer(modifier = Modifier.height(20.dp))

            // 3. Tarjetas de Acción (3 botones)
            ActionButtonsRow(registerPayment,registerProduct,registerDebtor, registerCampaing,registerNewSell,sumary1,isSucriptionActive,{
                showAlertFreeLimited =true
            },numeroProductos)
            Spacer(modifier = Modifier.height(24.dp))
            if(isSucriptionActive==false) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    if (BuildConfig.DEBUG) {
                        NativeAdBanner(
                            adUnitId = "ca-app-pub-3940256099942544/2247696110"
                        )
                    } else {
                        NativeAdBanner(
                            adUnitId = "ca-app-pub-1155673544372892/6066860296"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            // 4. Carrusel de Próximos Vencimientos
            if(deudoresOrdenados.size>0)
               UpcomingDeadlinesSection(deudoresOrdenados,seeAllDeudores,seeDeudorProfile)
            else{
                    PaymentHistoryEmptyState("Cuando registres tu primer, deudor, podras ver los proximos a vencer en esta seccion")
            }

            // 5. Lista de Pagos Recientes
            if(pagosHistorial.size>0)
                RecentPaymentsSection(pagosHistorial,seeAllPayments)
            else{
                Log.e("EMPTYSECTION","---")
                PaymentHistoryVerticalEmptyState("Cuando registres tu primer pago,podras consultar tus ultimos pagos o ventas ")
            }
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@Composable
fun BalanceCarousel( pagosList: List<AbonosDelMes>,sumary1: DeudoresSummary,showBalance: Boolean,  onToggleBalance: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 1 })

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 18.dp), // Para que se vean las orillas de las otras cards
            pageSpacing = 5.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> MainBalanceCard(
                    title = "Saldo Pendiente Total",
                    amount = formatAsCurrency(sumary1.sumaTotalMontos?:0.0f),
                    count1 = sumary1!!.totalDeudores.toString()?:"0", label1 = "Clientes",
                    count2 = "12", label2 = "Vencidos",
                    backgroundColor = Color(0xFF1A73E8), // Azul
                    icon = Icons.Outlined.AccountBalanceWallet,
                    showBalance = showBalance,
                    onToggleBalance = {onToggleBalance() }
                )
                1 -> MainRecoveryCard(
                    pagosList,
                    backgroundColor = Color(0xFF2E7D32), // Verde
                    icon = Icons.Default.CheckCircle,
                )
                2 -> MainBalanceCard(
                    title = "Cuentas por Vencer",
                    amount = "$8,900",
                    count1 = "8", label1 = "Próximos",
                    count2 = "3", label2 = "Críticos",
                    backgroundColor = Color(0xFFD32F2F), // Rojo/Naranja
                    icon = Icons.Default.Warning,
                    showBalance =showBalance ,
                    onToggleBalance = {onToggleBalance() }
                )
            }
        }

        // Indicador de puntos (Dots)
        Row(
            Modifier
                .height(24.dp)
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(1) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}
// --- 1. TOP BAR ---
@Composable
fun TopBar(imageUser: String?,goToMyProfile:()->Unit) {
    Log.e("PROFILE_PHOTO","-->"+imageUser)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Se usa .padding para separar la barra de los bordes
            .padding(horizontal = 16.dp, vertical = 12.dp),
        // Alinea verticalmente todos los elementos de la fila
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- SECCIÓN IZQUIERDA: Logo y Nombre ---
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                // Reemplaza con tu referencia de recurso de logo correcta
                painter =  painterResource(id = com.nexusystem.paguito.R.drawable.abonia_a),

                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "Paguito",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // --- SECCIÓN CENTRAL: Espaciador Flexible ---
        // Este espaciador "empuja" todo lo que sigue hacia la derecha
        Spacer(modifier = Modifier.weight(1f))

        // --- SECCIÓN DERECHA: Imagen de Perfil ---
        // ESTO ES LO QUE ESTABA FUERA Y AHORA ESTÁ DENTRO DEL ROW
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary).clickable{
                    goToMyProfile()
                },
            contentAlignment = Alignment.Center
        ) {
            // Lógica corregida: Si NO hay imagen, muestra el icono. Si hay, usa AsyncImage.
            if (imageUser.isNullOrEmpty()) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Placeholder de perfil",
                    tint = Color(0xFFEC4899),
                    modifier = Modifier.size(40.dp)
                )
            } else {
                AsyncImage(
                    model = imageUser,
                    contentDescription = "Imagen de perfil del usuario",
                    modifier = Modifier
                        .size(45.dp) // Reducido para que quepa bien en el Box de 50.dp
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}
@Composable
fun MainRecoveryCard(
    pagosList: List<AbonosDelMes>,
    backgroundColor: Color,
    icon: ImageVector,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-30).dp)
            )

            Column(modifier = Modifier.padding(24.dp)) {
                CobrosChartScreen(pagosList)
            }
        }
    }
}
fun prepareChartData(pagos: List<AbonosDelMes>): List<ChartPoint> {
    val parser =
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Ajusta el formato al que uses
    val calendar = Calendar.getInstance()

    // Agrupar la suma de montos por el número de día del mes
    val groupedByDay = pagos
        .filter { it?.isIngreso!! } // Filtrar solo si es un ingreso/cobro
        .groupBy {
            try {
                val date = parser.parse(it?.fechaAbono)
                if (date != null) {
                    calendar.time = date
                    calendar.get(Calendar.DAY_OF_MONTH)
                } else { -1 }
            } catch (e: Exception) { -1 }
        }
        .filterKeys { it != -1 }
        .mapValues { entry -> entry.value.sumOf { it?.montoAbonado!! }.toFloat() }

    // Generar la lista completa para los 30 días (rellenando con 0f si no hubo cobro ese día)
    return (1..30).map { day ->
        ChartPoint(day = day, amount = groupedByDay[day] ?: 0f)
    }
}
@Composable
fun CobrosChartScreen(pagosList: List<AbonosDelMes>) {
    val chartData = remember(pagosList) { prepareChartData(pagosList) }

    // Cálculos para los cuadros informativos inferiores
    val total30D = remember(chartData) { chartData.sumOf { it.amount.toDouble() }.toInt() }
    val promedioDiario = remember(chartData) { if (chartData.isNotEmpty()) total30D / 30 else 0 }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFF))
            .padding(14.dp)
    ) {
        // Contenedor de la Gráfica
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        ) {
            EarningsChart(points = chartData)
        }
        Spacer(modifier = Modifier.height(32.dp))

        // Tarjeta de Resumen Inferior (Total y Promedio)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "TOTAL 30D",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatAsCurrency(total30D),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "PROMEDIO DIARIO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatAsCurrency(promedioDiario),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }
    }
    }
}
data class ChartPoint(val day: Int, val amount: Float)
@Composable
fun EarningsChart(points: List<ChartPoint>, modifier: Modifier = Modifier) {
    val maxAmount = remember(points) { (points.maxOfOrNull { it.amount } ?: 80f).coerceAtLeast(80f) }

    // Ajustar renglones de guía del eje Y basados en el máximo
    val yLines = listOf(maxAmount, maxAmount * 0.75f, maxAmount * 0.5f, maxAmount * 0.25f, 0f)
    val xLabels = listOf("05", "10", "15", "20", "25", "30")

    Row(modifier = modifier.fillMaxSize()) {
        // 1. Indicadores del Eje Y
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 8.dp, bottom = 24.dp), // Espacio para alineación con las líneas horizontales
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            yLines.forEach { valText ->
                Text(
                    text = "$${valText.toInt()}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(35.dp)
                )
            }
        }

        // 2. Lienzo de la Gráfica (Líneas + Curva + Eje X)
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val spacingX = width / (points.size - 1)

                    // Dibujar líneas guía horizontales punteadas
                    yLines.forEach { value ->
                        val yRatio = 1f - (value / maxAmount)
                        val yPos = height * yRatio

                        // Línea punteada de guía
                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(0f, yPos),
                            end = Offset(width, yPos),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    if (points.size > 1) {
                        val strokePath = Path()
                        val fillPath = Path()

                        // Punto inicial de la curva
                        val firstYRatio = 1f - (points[0].amount / maxAmount)
                        val firstX = 0f
                        val firstY = height * firstYRatio

                        strokePath.moveTo(firstX, firstY)
                        fillPath.moveTo(firstX, height) // Iniciar sombra en la base
                        fillPath.lineTo(firstX, firstY)

                        // Dibujar conexiones curvas suaves (Cubic Bézier)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]

                            val x0 = i * spacingX
                            val y0 = height * (1f - (p0.amount / maxAmount))
                            val x1 = (i + 1) * spacingX
                            val y1 = height * (1f - (p1.amount / maxAmount))

                            // Puntos de control para suavizar la curva intermedia
                            val controlX1 = x0 + (spacingX / 2f)
                            val controlY1 = y0
                            val controlX2 = x0 + (spacingX / 2f)
                            val controlY2 = y1

                            strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, x1, y1)
                            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x1, y1)
                        }

                        // Cerrar el path de relleno/sombra hacia el fondo del lienzo
                        fillPath.lineTo(width, height)
                        fillPath.close()

                        // Dibujar el área sombreada translúcida verde esmeralda
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF10B981).copy(alpha = 0.22f), // Verde esmeralda translúcido arriba
                                    Color(0xFF10B981).copy(alpha = 0.00f)  // Desvanecido a cero abajo
                                )
                            )
                        )

                        // Dibujar la línea sólida verde principal
                        drawPath(
                            path = strokePath,
                            color = Color(0xFF10B981),
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round // <-- CORRECCIÓN: Se cambió 'strokeCap' por 'cap'
                            )
                        )
                    }
                }
            }

            // 3. Eje X: Etiquetas de los Días del Mes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Espaciadores para cuadrar las posiciones 5, 10, 15, 20...
                Spacer(modifier = Modifier.weight(4f))
                xLabels.forEach { label ->
                    Text(
                        text = label,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(4f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

// --- 2. TARJETA PRINCIPAL ---
@Composable
fun MainBalanceCard(
    title: String,
    amount: String,
    decimals: String = "",
    count1: String,
    label1: String,
    count2: String,
    label2: String,
    backgroundColor: Color,
    icon: ImageVector,
    showBalance: Boolean, // El estado actual
    onToggleBalance: () -> Unit // Función para cambiar el estado
) {
    // Lógica de enmascaramiento
    val displayAmount = if (showBalance) amount else "$***.**"
    val displayDecimals = if (showBalance) decimals else ""
    val buttonText = if (showBalance) "Ocultar balance" else "Mostrar balance"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.12f),
                modifier = Modifier
                    .size(150.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 40.dp, y = (-30).dp)
            )

            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(title, color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Actualizado", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Aquí aplicamos el enmascaramiento
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)) {
                            append(displayAmount)
                        }
                        withStyle(style = SpanStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)) {
                            append(displayDecimals)
                        }
                    },
                    color = Color.White
                )

                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    InfoItem(Icons.Default.Group, count1, label1)
                    Spacer(modifier = Modifier.width(16.dp))
                    VerticalDivider(modifier = Modifier.height(14.dp), color = Color.White.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.width(16.dp))

                    // Botón con lógica de click
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { onToggleBalance() } // Llamamos al callback
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = buttonText, // Cambia dinámicamente
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoItem(icon: ImageVector, count: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append(count) }
                append(" $label")
            },
            color = Color.White, fontSize = 12.sp
        )
    }
}

// --- 3. BOTONES DE ACCIÓN ---
@Composable
fun ActionButtonsRow(registerPayment:() ->Unit,registerProduct:() ->Unit,registerDebtor:() ->Unit,registerCampaing:() ->Unit,registerNewSell:()->Unit,sumary1: DeudoresSummary?,isSucriptionActive: Boolean,showDialogLimitFree:()->Unit,numeroProductos: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(icon = Icons.Outlined.AccountBalanceWallet, text = "Registrar\nPago", iconTint=MaterialTheme.colorScheme.onSurface,modifier = Modifier.weight(1f), onclick = registerPayment)
        ActionButton(icon = Icons.Outlined.PersonAdd, text = "Nuevo\nDeudor", iconTint = BluePrimary, modifier = Modifier.weight(1f),
            {
                if (sumary1 != null) {
                    if (sumary1?.totalDeudores!! >= 10 && !isSucriptionActive) {
                        showDialogLimitFree()
                    } else {
                        registerDebtor()
                    }
                } else {
                    registerDebtor()
                }
            }
        )
        ActionButton(icon = Icons.Outlined.AttachMoney, text = "Agregar\nventa", iconTint = Color(0xFF10B981), modifier = Modifier.weight(1f),{registerNewSell()})
        ActionButton(icon = Icons.Outlined.LocalGroceryStore, text = "Nuevo\nProducto", iconTint = Color(
            0xFF40C4FF
        ), modifier = Modifier.weight(1f),{
            if (numeroProductos!! >= 10 && !isSucriptionActive) {
                showDialogLimitFree()
            } else {
                registerProduct()
            }
            })
    }
}

@Composable
fun ActionButton(icon: ImageVector, text: String, iconTint: Color = TextDark, modifier: Modifier = Modifier,onclick:()->Unit) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable { onclick()},
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 16.sp)
        }
    }
}

// --- 4. CARRUSEL PRÓXIMOS VENCIMIENTOS ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpcomingDeadlinesSection(deudores:List<DeudoresEntity?>, seeAllDeudores:()->Unit,openDetailDeudor:(DeudoresEntity)->Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Próximos Vencimientos", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text("Ver todos", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = BluePrimary, modifier = Modifier.clickable {seeAllDeudores() })
    }

    Spacer(modifier = Modifier.height(12.dp))

    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        deudores.forEach {
            item { UpcomingCard({
                openDetailDeudor(it)
            },name = it!!.nombre, amount = it.montoActualAdeudado.toString(), date = it.fechaInicialDeuda, isUrgent = true,it.periodicidad) }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun UpcomingCard(openDetailDeudor:()->Unit,name: String, amount: String, date: String, isUrgent: Boolean,periodisity: String) {
    Card(
        modifier = Modifier.width(150.dp).clickable{
            openDetailDeudor()
        },
        colors = CardDefaults.cardColors(containerColor =MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                // Avatar Placeholder
                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(BorderLight), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextGray)
                }
                if (isUrgent) {
                    Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(RedUrgent))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Saldo restante: ", fontSize = 12.sp, color =  MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Normal)
            Text(formatAsCurrency(amount), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (isUrgent) RedUrgent else MaterialTheme.colorScheme.onSurface)

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderLight, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Siguiente pago en: ", fontSize = 12.sp, color =  MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = if (isUrgent) RedUrgent else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text( getDaysUntilNextPayment(date,periodisity).toString()+" dìas", fontSize = 12.sp, color = if (isUrgent) RedUrgent else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// --- 5. PAGOS RECIENTES ---
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun RecentPaymentsSection(pagos :List<PagoConNombre?>, seeAllPayments:()->Unit) {
    Spacer(modifier = Modifier.height(20.dp))
    Text("Pagos Recientes", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    Spacer(modifier = Modifier.height(12.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            pagos.forEach {
                var stringTyePayment ="Efectivo"
                when(it!!.tipoPago){
                    1->{
                        stringTyePayment = "Efectivo"
                    }
                    2->{
                        stringTyePayment = "Transferencia"
                    }
                    3->{
                        stringTyePayment = "Trajeta"
                    }
                }
                RecentPaymentItem(it.nameDeudor?:"", formatFecha(it.fechaAbono)+" • $stringTyePayment", it.montoAbonado.toString(),it.isIngreso)
                Divider(color = BorderLight, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
            // Footer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { seeAllPayments()}
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Ver historial completo", color = TextGray, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
fun PaymentHistoryVerticalEmptyState(
    dynamicDescription: String,
    modifier: Modifier = Modifier
) {
    // ELIMINAMOS .verticalScroll(...) de aquí para evitar el error de constraints infinitos
    Column(
        modifier = modifier
            .fillMaxWidth() // Cambiamos fillMaxSize por fillMaxWidth
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // --- Contenedor de items dummy ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(start = 5.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Historial de pagos",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            repeat(2) { index ->
                DummyVerticalPaymentItem()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- Título ---
        Text(
            text = "No hay historial de pagos",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Descripción ---
        Text(
            text = dynamicDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = PaguitoThemeColors.textSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
    }
}
@Composable
fun PaymentHistoryEmptyState(
    dynamicDescription: String,
    modifier: Modifier = Modifier
) {
    // Usamos Column para apilar la lista y los textos verticalmente
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp), // Margen externo
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Proximos cobros",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        )
        // --- Lista Horizontal (LazyRow) de 3 items dummy ---
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Espacio entre items
        ) {
            items(2) { // Crea 3 items dummy
                DummyPaymentItem()
            }
        }

        Spacer(modifier = Modifier.height(40.dp)) // Espaciado generoso antes de textos

        // --- Título en Negritas y Centrado ---
        Text(
            text = "No hay deudores registrados",
            style = MaterialTheme.typography.headlineSmall, // headlineSmall (aprox 24sp)
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp)) // Espaciado corto entre título y descripción

        // --- Descripción Dinámica y Centrada ---
        Text(
            text = dynamicDescription,
            style = MaterialTheme.typography.bodyLarge, // bodyLarge (aprox 16sp)
            color =MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp, // Mejorar legibilidad de líneas múltiples
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun RecentPaymentItem(name: String, details: String, amount: String,isIngreso: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Placeholder
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(1.dp, com.nexusystem.paguito.ui.screens.deudores.BorderColor, CircleShape)
                .background(IconBgGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if(isIngreso)
                Icon(Icons.Outlined.Download, contentDescription = null, tint = TextDark2, modifier = Modifier.size(16.dp))
            else
                Icon(Icons.Outlined.LocalMall, contentDescription = null, tint = TextDark2, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(details, fontSize = 12.sp, color = TextGray)
        }

        Text(formatAsCurrency(amount), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

// Colores sugeridos para Paguito (puedes mover esto a tu Theme)
private object PaguitoThemeColors {
    val errorColor = Color(0xFFEF4444) // Rojo vibrante para cantidad y fecha
    val iconTint = Color(0xFF9CA3AF) // Gris medio para iconos
    val textPrimary = Color(0xFF1F2937) // Gris muy oscuro
    val textSecondary = Color(0xFF6B7280) // Gris medio
    val cardBackground = Color(0xFFF9FAFB) // Gris muy pálido para fondo de tarjeta
}

@Composable
fun DummyPaymentItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            // Aplicamos el bordado discontinuo personalizado
            .dashedBorder(
                width = 2.dp,
                radius = 20.dp,
                color = PaguitoThemeColors.textSecondary,
                dashLength = 10.dp,
                gapLength = 8.dp
            )
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .width(200.dp) // Ancho fijo para los items de la lista horizontal
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            // --- Icono de Usuario (Perfil gris) ---
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = PaguitoThemeColors.iconTint,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background) // Gris claro para fondo icono
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Nombre Dummy ---
            Text(
                text = "----- -----",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 16.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
                // Sin .orEmpty() ni .getFullName() porque es dummy
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- Cantidad en $0 pesos (Color Rojo) ---
            Text(
                text = "$0.00 mxn",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 26.sp),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // --- Fecha Ejemplo (Icono + Texto Rojo) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "-- / -- / --",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // --- Leyenda de Item de Ejemplo ---
            Text(
                text = "---- ---- ---- --",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }

}


@Composable
fun DummyVerticalPaymentItem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            // Aplicamos el bordado discontinuo personalizado (Modifier corregido de antes)
            .dashedBorder(
                width = 2.dp,
                radius = 16.dp,
                color = PaguitoThemeColors.textSecondary,
                dashLength = 10.dp,
                gapLength = 8.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .fillMaxWidth() // Ocupa todo el ancho en la lista vertical
    ) {
        // --- Redeposición de elementos en Fila (Row) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween // Separa nombre de cantidad
        ) {
            // Columna Izquierda: Nombre y Fecha
            Column(modifier = Modifier.weight(1f)) {
                // --- Nombre Dummy (Peso 1f para dar espacio) ---
                Text(
                    text = "--- ---",
                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // --- Fecha Ejemplo (Icono + Texto Rojo) ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "-- / -- / --",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Leyenda de Item de Ejemplo ---
                Text(
                    text = "--- --- ---",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Columna Derecha: Cantidad
            // --- Cantidad en $0 pesos (Color Rojo, alineada a la derecha) ---
            Text(
                text = "$0.00",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                fontWeight = FontWeight.Bold,
                color =MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}