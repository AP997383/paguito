package com.nexusystem.paguito.ui.screens.analisis

import android.graphics.Paint
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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
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
import com.nexusystem.paguito.R
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.domain.data.ProductosSummary1
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
import java.text.NumberFormat
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
fun AnalisisScreen(
                    goToMyProfile:()->Unit,
                    deudoresViewModel: DeudoresViewModel,
                    pagosViewModel: PagosViewModel,
    productosViewmodel: ProductosViewModel,
    analisisViewmodel: AnalisisViewModel) {
    val deudoresHisotiral by deudoresViewModel.deudores.collectAsState()
    val sumary2 by analisisViewmodel.sumaryInvestment.collectAsState()
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
        analisisViewmodel.obtenerInversionTotal()
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
        TopBar(urlPhotoProfile,{goToMyProfile()})


        Column(modifier = Modifier.padding(horizontal = 6.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            // 2. Tarjeta Principal
            BalanceCarousel(deudoresHisotiral,pagosPormes,sumary2?: ProductosSummary1(0,0f),isBalanceVisible,{
                isBalanceVisible = !isBalanceVisible
            })
            Top5BalanceCarousel(deudoresHisotiral,pagosPormes,isBalanceVisible,{
                isBalanceVisible = !isBalanceVisible
            })
            Spacer(modifier = Modifier.height(20.dp))
            if(isSucriptionActive) {

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 0.dp)) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Cobros - últimos 30 días",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.padding(bottom = 24.dp, start = 10.dp)
                        )
                        CobrosChartScreen(pagosPormes)
                    }
                }
            }
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@Composable
fun Top5BalanceCarousel( clients: List<DeudoresEntity?>, pagosList: List<AbonosDelMes>,showBalance: Boolean,  onToggleBalance: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 1 })

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 5.dp), // Para que se vean las orillas de las otras cards
            pageSpacing = 5.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 ->  TopDebtorsCard(clients = clients.sortedByDescending { it?.montoActualAdeudado })
                1 ->  TopDebtorsCard(clients = clients.sortedByDescending { it?.montoActualAdeudado })
                2 ->  TopDebtorsCard(clients = clients.sortedByDescending { it?.montoActualAdeudado })
            }
        }
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
@Composable
fun BalanceCarousel( clients: List<DeudoresEntity?>, pagosList: List<AbonosDelMes>,sumary1: ProductosSummary1,showBalance: Boolean,  onToggleBalance: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 1 })

    Column(modifier = Modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 5.dp), // Para que se vean las orillas de las otras cards
            pageSpacing = 5.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> MainBalanceCard(
                    title = "Inversión real(Costo directo)",
                    amount = formatAsCurrency(sumary1.totalCostoProductos?:0.0f),
                    count1 = sumary1!!.numeroProductos.toString()?:"0", label1 = "Productos",
                    count2 = "12", label2 = "Vencidos",
                    backgroundColor = Color(0xFF2E7D32), // Azul
                    icon = Icons.Outlined.CheckCircle,
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
fun TopBar(imageUser: String?,onClick:()->Unit) {
    Log.e("PROFILE_PHOTO","-->"+imageUser)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Se usa .padding para separar la barra de los bordes
            .padding(horizontal = 16.dp, vertical = 12.dp),
        // Alinea verticalmente todos los elementos de la fila
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Mis resultados",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            // Eliminé top y bottom padding de aquí para que el verticalAlignment del Row funcione perfectamente
            modifier = Modifier.padding(start = 16.dp)
        )

        // --- ESTE ES EL CAMBIO CLAVE ---
        // Este Spacer actúa como un "muelle" que empuja el Box de la derecha hasta el extremo
        Spacer(modifier = Modifier.weight(1f))

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
                    onClick()
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

            Column() {
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        // Contenedor de la Gráfica
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            EarningsChart(points = chartData)
        }
        Spacer(modifier = Modifier.height(32.dp))
        // --- DIVISOR INFERIOR ---
        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)
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
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatAsCurrency(total30D),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "PROMEDIO DIARIO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        formatAsCurrency(promedioDiario),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                }
            }


    }
}
data class ChartPoint(val day: Int, val amount: Float)
@Composable
fun EarningsChart(points: List<ChartPoint>, modifier: Modifier = Modifier) {
    val activePoints = remember(points) { points.filter { it.amount > 0f } }
    val maxAmount = remember(activePoints) { (activePoints.maxOfOrNull { it.amount } ?: 80f).coerceAtLeast(80f) }
    val yLines = listOf(maxAmount, maxAmount * 0.75f, maxAmount * 0.5f, maxAmount * 0.25f, 0f)
    // Generar dinámicamente las etiquetas del eje X basadas en tus ChartPoints
    val xLabels = listOf("01","05", "10", "15", "20", "25", "30")
    // Configurar la densidad de pixeles para convertir SP de texto a pixeles nativos dentro del Canvas
    val density = LocalDensity.current
    val textPixelSize = with(density) { 10.sp.toPx() }

    Row(modifier = modifier.fillMaxSize()) {
        // 1. Indicadores del Eje Y
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(end = 8.dp, bottom = 24.dp),
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

        // 2. Lienzo de la Gráfica (Líneas + Curva + Cantidades + Eje X)
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width - 30
                    val height = size.height
                    val spacingX = if (points.size > 1) width / (points.size - 1) else width

                    // Dibujar líneas guía horizontales punteadas
                    yLines.forEach { value ->
                        val yRatio = 1f - (value / maxAmount)
                        val yPos = height * yRatio

                        drawLine(
                            color = Color(0xFFE2E8F0),
                            start = Offset(0f, yPos),
                            end = Offset(width, yPos),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    if (points.size > 0) {
                        val strokePath = Path()
                        val fillPath = Path()

                        // Punto inicial de la curva
                        val firstYRatio = 1f - (points[0].amount / maxAmount)
                        val firstX = 0f
                        val firstY = height * firstYRatio

                        strokePath.moveTo(firstX, firstY)
                        fillPath.moveTo(firstX, height)
                        fillPath.lineTo(firstX, firstY)

                        // Dibujar conexiones curvas suaves (Cubic Bézier)
                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]

                            val x0 = i * spacingX
                            val y0 = height * (1f - (p0.amount / maxAmount))
                            val x1 = (i + 1) * spacingX
                            val y1 = height * (1f - (p1.amount / maxAmount))

                            val controlX1 = x0 + (spacingX / 2f)
                            val controlY1 = y0
                            val controlX2 = x0 + (spacingX / 2f)
                            val controlY2 = y1

                            strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, x1, y1)
                            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x1, y1)
                        }

                        if (points.size > 1) {
                            fillPath.lineTo((points.size - 1) * spacingX, height)
                            fillPath.close()

                            // Dibujar el área sombreada translúcida verde esmeralda
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF10B981).copy(alpha = 0.22f),
                                        Color(0xFF10B981).copy(alpha = 0.00f)
                                    )
                                )
                            )

                            // Dibujar la línea sólida verde principal
                            drawPath(
                                path = strokePath,
                                color = Color(0xFF10B981),
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            )
                        }

                        // 🔴 NUEVO: Dibujar la cantidad abonada arriba de cada onda/punto
                        // Usamos la API nativa de Canvas de Android mediante nativeCanvas
                        val paint = Paint().apply {
                            color = android.graphics.Color.parseColor("#0F172A") // Color Slate 900 oscuro
                            textSize = textPixelSize
                            textAlign = Paint.Align.CENTER // Centra el texto horizontalmente en el eje X del punto
                            isAntiAlias = true
                            fontMetrics
                        }

                        points.forEachIndexed { index, point ->
                            val currentX = index * spacingX
                            val currentY = height * (1f - (point.amount / maxAmount))

                            // Solo si el monto es mayor que cero para no saturar con ceros la gráfica (opcional)
                            if (point.amount > 0f) {
                                val textToDraw = "$${point.amount.toInt()}"

                                // 'currentY - 12f' eleva el texto 12 pixeles por encima de la línea verde para que no se encimen
                                drawContext.canvas.nativeCanvas.drawText(
                                    textToDraw,
                                    currentX,
                                    currentY - 15f,
                                    paint
                                )
                            }
                        }
                    }
                }
            }

            // 3. Eje X: Etiquetas de los Días del Mes dinámicas
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                xLabels.forEach { label ->
                    Text(
                        text = label,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
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
            Text(text = text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center, lineHeight = 16.sp)
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

@Composable
fun TopDebtorsCard(
    clients: List<DeudoresEntity?>,
    onDownloadClick: () -> Unit = {}
) {
    // Formateador de moneda (lo declaramos arriba para reutilizarlo en las barras y en el total)
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "MX"))

    // Calculamos el total de la deuda sumando los elementos
    val totalDebt = clients.sumOf { it?.montoActualAdeudado!!.toDouble() }

    // Tomamos la deuda máxima (asumiendo que la lista ya viene ordenada de mayor a menor)
    // Evitamos división por cero asegurando un mínimo de 1.0f
    val maxDebt = if (clients.firstOrNull()?.montoActualAdeudado == 0f) 1.0f else clients.firstOrNull()?.montoActualAdeudado ?: 1.0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            // --- ENCABEZADO ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, top = 24.dp, end = 14.dp, bottom = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Top 10 clientes - Mayor deuda",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2937)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saldos pendientes ordenados de mayor a menor",
                        fontSize = 12.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CUERPO: LISTA DE BARRAS ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp), // Ajustado ligeramente para dar espacio al texto del monto
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                clients.take(10).forEach { client ->
                    if (client != null) {
                        // Calculamos la fracción del ancho proporcional (entre 0.05f y 1.0f para que no desaparezca si es muy bajo)
                        val barProgress = (client.montoActualAdeudado / maxDebt).coerceIn(0.05f, 1f)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Nombre del cliente
                            Text(
                                text = client.nombre,
                                fontSize = 13.sp,
                                color = Color(0xFF374151),
                                modifier = Modifier.width(65.dp),
                                textAlign = TextAlign.Start
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // --- CAMBIO CLAVE: Contenedor como Row para empujar el monto ---
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Contenedor dinámico de la barra
                                Box(
                                    modifier = Modifier
                                        .weight(barProgress) // El peso maneja la longitud proporcional
                                        .fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(24.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF10B981))
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Monto al final de la barra
                                Text(
                                    text = currencyFormatter.format(client.montoActualAdeudado),
                                    fontSize = 11.sp, // Un tamaño ligeramente menor para que quepa bien
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4B5563),
                                    modifier = Modifier.wrapContentWidth()
                                )

                                // Espaciador invisible a la derecha para compensar el espacio si la barra es corta
                                // Esto evita que el texto se "coma" el final de la pantalla si la barra mide 1f
                                Spacer(modifier = Modifier.weight(1.01f - barProgress, fill = true))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- DIVISOR INFERIOR ---
            HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)

            // --- PIE DE PÁGINA: TOTAL ---
            Text(
                text = "Total pendiente Top10: ${currencyFormatter.format(totalDebt)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF111827),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            )
        }
    }
}