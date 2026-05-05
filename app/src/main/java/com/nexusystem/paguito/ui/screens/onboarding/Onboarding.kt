package com.nexusystem.paguito.ui.screens.onboarding

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
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
import com.nexusystem.paguito.ui.screens.login.GreenPrimary

// Modelo de datos para las páginas
data class OnboardingData(
    val title: String,
    val description: String,
    val image: Int // ID del recurso drawable
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardingData(
            title = "Administra tu cobranza",
            description = "Registra a todos tus clientes de forma organizada y lleva un control preciso de tus cuentas por cobrar.",
            image = R.drawable.onb1 // Reemplaza con tu ilustración
        ),
        OnboardingData(
            title = "Gestiona tus productos",
            description = "Mantén tu inventario al día. Agrega, edita y controla todos los productos que vendes en un solo lugar.",
            image = R.drawable.onb2 // Reemplaza con tu ilustración
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp)
    ) {
        // Pager con el contenido
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { position ->
            OnboardingPagerItem(pages[position])
        }

        // Fila inferior (Skip, Dots, Next)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botón Skip
            TextButton(
                onClick = onFinish,
                enabled = pagerState.currentPage < pages.size - 1
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) "Saltar" else "",
                    color = Color.Gray,
                    fontSize = 16.sp
                )
            }

            // Indicadores (Puntos)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(pages.size) { iteration ->
                    val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else Color.LightGray
                    val width = animateDpAsState(targetValue = if (pagerState.currentPage == iteration) 24.dp else 8.dp)
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(width.value)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // Botón Next / Start
            TextButton(
                onClick = {
                    if (pagerState.currentPage < pages.size - 1) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onFinish()
                    }
                }
            ) {
                Text(
                    text = if (pagerState.currentPage < pages.size - 1) "Siguiente" else "Empezar",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun OnboardingPagerItem(data: OnboardingData) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Círculo decorativo con Imagen (como el diseño)
        Box(
            modifier = Modifier
                .size(320.dp)
                .clip(CircleShape)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = data.image),
                contentDescription = null,
                modifier = Modifier.size(250.dp), // Ajustado para que la imagen luzca bien
                contentScale = ContentScale.Fit // O ContentScale.Crop si tu imagen es cuadrada
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = data.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color(0xFF1A1A1A)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = data.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 20.dp),
            lineHeight = 24.sp
        )
    }
}