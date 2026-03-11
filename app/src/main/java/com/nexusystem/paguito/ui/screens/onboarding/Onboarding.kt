package com.nexusystem.paguito.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import com.nexusystem.paguito.R
// --- 1. Definición de la estructura de datos para cada página ---
data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int, // R.drawable.tu_imagen
    val buttonText: String = "Siguiente"
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Dile adiós a la libreta",
        description = "Registra tus clientes, ventas y abonos en un solo lugar. Lleva el control de tu negocio en tu bolsillo, sin tachaduras ni hojas perdidas.",
        imageRes = R.drawable.banne1 // Reemplaza con tu R.drawable
    ),
    OnboardingPage(
        title = "Que no se te pase ningún cobro",
        description = "Visualiza tu agenda diaria. Descubre quién te debe pagar hoy, mañana y revisa rápidamente quiénes están atrasados con sus cuotas.",
        imageRes = R.drawable.banner2
    ),
    OnboardingPage(
        title = "Cobranza en piloto automático",
        description = "¡Olvídate de la pena de cobrar! Configura recordatorios amigables por SMS que se enviarán automáticamente a tus clientes antes de su fecha de pago.",
        imageRes = R.drawable.banner3
    ),
    OnboardingPage(
        title = "Haz crecer tu negocio",
        description = "Mide tus ganancias reales, descubre tus productos más vendidos y toma decisiones inteligentes para vender más.",
        imageRes =R.drawable.banner4 ,
        buttonText = "Comenzar a vender"
    )
)

// --- 2. Color Principal basado en tus capturas ---
val PrimaryBlue = Color(0xFF3B71F6)
val TextGray = Color(0xFF8B92A5)

// --- 3. UI Principal ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    modifier: Modifier,
    onFinish: () -> Unit, // Acción al terminar el onboarding
    onLoginClick: () -> Unit // Acción al dar clic en "Inicia sesión"
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            OnboardingTopBar(
                showBack = pagerState.currentPage > 0,
                onBackClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                },
                onSkipClick ={onFinish()}
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA)), // Fondo gris muy claro
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- PAGER CON EFECTO FADE IN / FADE OUT ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->

                // Cálculo mágico para el Fade In/Out durante el swipe
                val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                val alphaFade = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            alpha = alphaFade // Aplica el efecto de desvanecimiento
                        },
                    contentAlignment = Alignment.Center
                ) {
                    OnboardingPageLayout(page = onboardingPages[page])
                }
            }

            // --- SECCIÓN INFERIOR (Puntos y Botones) ---
            BottomSection(
                currentPage = pagerState.currentPage,
                totalPages = onboardingPages.size,
                buttonText = onboardingPages[pagerState.currentPage].buttonText,
                onButtonClick = {
                    if (pagerState.currentPage < onboardingPages.size - 1) {
                        coroutineScope.launch {
                            // Avanzar a la siguiente página
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinish() // Finalizar onboarding
                    }
                },
                onLoginClick = onLoginClick
            )
        }
    }
}

// --- Componente: Barra Superior ---
@Composable
fun OnboardingTopBar(
    showBack: Boolean,
    onBackClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón Atrás (solo se muestra si no es la primera página)
        if (showBack) {
            IconButton(onClick = onBackClick) {
                // Icono de flecha (reemplazar por painterResource si tienes tu propio icono)
                Text(text = "<", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        } else {
            Spacer(modifier = Modifier.width(48.dp)) // Espacio vacío para mantener centrado el título
        }

        Text(
            text = "VendeMás",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Text(
            text = "Omitir",
            color = TextGray,
            fontSize = 16.sp,
            modifier = Modifier
                .clickable { onSkipClick() }
                .padding(8.dp)
        )
    }
}

// --- Componente: Contenido de la página (Ilustración + Texto) ---
@Composable
fun OnboardingPageLayout(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Área de Ilustración (Reemplazar Icon con Image usando tu page.imageRes)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Placeholder: Aquí debes usar la etiqueta Image(painter = painterResource(id = page.imageRes)...)
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = null,
                modifier = Modifier.size(300.dp),
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Título
        Text(
            text = page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Descripción
        Text(
            text = page.description,
            fontSize = 15.sp,
            color = TextGray,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- Componente: Footer (Indicadores y Botones) ---
@Composable
fun BottomSection(
    currentPage: Int,
    totalPages: Int,
    buttonText: String,
    onButtonClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indicadores (Puntitos) - Desaparecen en la última página
        if (currentPage < totalPages - 1) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                repeat(totalPages - 1) { index ->
                    val color = if (index == currentPage) PrimaryBlue else Color(0xFFE2E8F0)
                    val width = if (index == currentPage) 24.dp else 8.dp

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(8.dp)
                            .width(width)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }
        }

        // Botón Principal
        Button(
            onClick = onButtonClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text(
                text = buttonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Texto de inicio de sesión (Solo visible en la última página)
        if (currentPage == totalPages - 1) {
            Spacer(modifier = Modifier.height(24.dp))

            val loginText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = TextGray)) {
                    append("¿Ya tienes cuenta? ")
                }
                withStyle(style = SpanStyle(color = PrimaryBlue, fontWeight = FontWeight.Bold)) {
                    append("Inicia sesión")
                }
            }

            Text(
                text = loginText,
                modifier = Modifier.clickable { onLoginClick() }
            )
        } else {
            // Mantener el mismo espacio para evitar saltos bruscos en el diseño
            Spacer(modifier = Modifier.height(44.dp))
        }
    }
}