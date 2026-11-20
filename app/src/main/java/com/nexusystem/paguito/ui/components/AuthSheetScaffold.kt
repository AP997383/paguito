package com.nexusystem.paguito.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.nexusystem.paguito.R
import kotlin.math.cos
import kotlin.math.sin

/** Shared presentation shell; form state and validation stay in the feature screens. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthSheetScaffold(
    registering: Boolean,
    onBack: (() -> Unit)? = null,
    startExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val height = maxHeight
        val sheetState = rememberStandardBottomSheetState(
            initialValue = if (startExpanded) SheetValue.Expanded else SheetValue.PartiallyExpanded, skipHiddenState = true
        )
        val density = LocalDensity.current
        val keyboardVisible = WindowInsets.ime.getBottom(density) > 0
        LaunchedEffect(keyboardVisible) { if (keyboardVisible) sheetState.expand() }
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            scaffoldState = rememberBottomSheetScaffoldState(bottomSheetState = sheetState),
            sheetPeekHeight = height * 0.56f,
            sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            sheetContainerColor = MaterialTheme.colorScheme.background,
            sheetContentColor = MaterialTheme.colorScheme.onBackground,
            sheetShadowElevation = 16.dp,
            sheetTonalElevation = 0.dp,
            sheetDragHandle = { BottomSheetDefaults.DragHandle() },
            sheetContent = {
                Column(
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp).padding(bottom = 32.dp),
                    content = content
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            AuthHero(registering, onBack)
        }
    }
}

@Composable
private fun AuthHero(registering: Boolean, onBack: (() -> Unit)?) {
    val colors = MaterialTheme.colorScheme
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
        listOf(colors.primary.copy(alpha = 0.13f), colors.background, colors.secondary.copy(alpha = 0.07f))
    ))) {
        Box(Modifier.matchParentSize().paguitoBackground(fillBackground = false))
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.abonia_a), contentDescription = null, modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(12.dp))
                Text("Paguito", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                if (onBack != null) FilledTonalIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Volver")
                }
            }
            if (registering) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(50), color = colors.background.copy(alpha = 0.9f)) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Lock, contentDescription = null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Registro seguro", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
            Column {
                Text(if (registering) "Empieza algo" else "Tu negocio,", style = MaterialTheme.typography.headlineLarge)
                Text(if (registering) "grande." else "contigo.", style = MaterialTheme.typography.headlineLarge, color = colors.primary)
                Spacer(Modifier.height(10.dp))
                Text(if (registering) "Crea tu cuenta y comienza a organizar tu negocio." else "Organiza tus clientes, registra tus ventas y lleva tus pagos al día.",
                    style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}
