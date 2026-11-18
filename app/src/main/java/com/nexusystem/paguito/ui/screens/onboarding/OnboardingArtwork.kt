package com.nexusystem.paguito.ui.screens.onboarding

import android.animation.ValueAnimator
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nexusystem.paguito.R
import kotlin.math.sin

@Composable
internal fun OnboardingArtwork(scene: IntroScene, active: Boolean, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var resumed by remember(lifecycle) { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ -> resumed = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
        lifecycle.addObserver(observer); onDispose { lifecycle.removeObserver(observer) }
    }
    val phase = if (active && resumed && ValueAnimator.areAnimatorsEnabled()) rememberInfiniteTransition(label = "intro-art").animateFloat(
        0f, 6.283185f, infiniteRepeatable(tween(10000, easing = LinearEasing)), label = "intro-card-float") else rememberUpdatedState(0f)
    BoxWithConstraints(modifier.clearAndSetSemantics {}.clipToBounds(), contentAlignment = Alignment.Center) {
        val scale = minOf(maxWidth / 330.dp, maxHeight / 260.dp).coerceIn(0.4f, 1.15f)
        Box(Modifier.requiredSize(310.dp, 250.dp).graphicsLayer { scaleX = scale; scaleY = scale }) {
            Box(Modifier.size(230.dp).align(Alignment.Center).background(Brush.radialGradient(listOf(colors.primary.copy(alpha = 0.14f), Color.Transparent)), CircleShape))
            Text(stringResource(R.string.intro_preview), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, modifier = Modifier.align(Alignment.TopCenter))
            Surface(modifier = Modifier.align(Alignment.Center).width(286.dp).graphicsLayer {
                translationY = sin(phase.value) * 3.dp.toPx(); rotationZ = sin(phase.value) * 0.5f
            }, shape = MaterialTheme.shapes.extraLarge, color = colors.surface,
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.16f)), shadowElevation = 5.dp) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (scene) {
                        IntroScene.Overview -> OverviewPreview()
                        IntroScene.Clients -> ClientsPreview()
                        IntroScene.Products -> ProductsPreview()
                        IntroScene.Growth -> GrowthPreview(phase)
                    }
                }
            }
            Surface(modifier = Modifier.align(Alignment.BottomEnd).padding(end = 2.dp, bottom = 2.dp).graphicsLayer { translationY = -sin(phase.value + 1f) * 4.dp.toPx() },
                shape = MaterialTheme.shapes.large, color = colors.primaryContainer, shadowElevation = 5.dp,
                border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.14f))) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Outlined.CheckCircle, null, tint = colors.primary, modifier = Modifier.size(18.dp))
                    Text(stringResource(when (scene) {
                        IntroScene.Overview -> R.string.intro_art_overview_badge
                        IntroScene.Clients -> R.string.intro_art_clients_badge
                        IntroScene.Products -> R.string.intro_art_products_badge
                        IntroScene.Growth -> R.string.intro_art_growth_badge
                    }), style = MaterialTheme.typography.labelMedium, color = colors.onPrimaryContainer)
                }
            }
        }
    }
}

@Composable
private fun PreviewHeading(icon: ImageVector, title: String, chip: String? = null) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
        if (chip != null) Text(chip, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable private fun OverviewPreview() {
    PreviewHeading(Icons.Outlined.AccountBalanceWallet, stringResource(R.string.intro_art_today), stringResource(R.string.intro_art_today_chip))
    Column {
        Text("$1,250.00", style = MaterialTheme.typography.displaySmall)
        Text(stringResource(R.string.intro_art_sales_label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        PreviewMetric(Icons.Outlined.PeopleAlt, "12", stringResource(R.string.intro_art_clients))
        PreviewMetric(Icons.Outlined.Inventory2, "24", stringResource(R.string.intro_art_products))
    }
}

@Composable private fun PreviewMetric(icon: ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Column { Text(value, style = MaterialTheme.typography.titleMedium); Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun ClientsPreview() {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) { Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) { Text("AL", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall) } }
        Column { Text("Ana López", style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.intro_art_customer), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
    Column { Text(stringResource(R.string.intro_art_balance), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("$600.00", style = MaterialTheme.typography.headlineLarge) }
    LinearProgressIndicator(progress = { 0.4f }, modifier = Modifier.fillMaxWidth().height(6.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(R.string.intro_art_paid), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.intro_art_payment_date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun ProductsPreview() {
    PreviewHeading(Icons.Outlined.Inventory2, stringResource(R.string.intro_art_catalog), "24")
    ProductPreviewRow(Icons.Outlined.MenuBook, stringResource(R.string.intro_art_notebook), "$120.00", "8")
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    ProductPreviewRow(Icons.Outlined.Coffee, stringResource(R.string.intro_art_mug), "$90.00", "3")
}

@Composable private fun ProductPreviewRow(icon: ImageVector, name: String, amount: String, stock: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.07f)) { Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(10.dp).size(24.dp)) }
        Column(Modifier.weight(1f)) { Text(name, style = MaterialTheme.typography.labelLarge); Text(amount, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        Text(stock, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleSmall)
    }
}

@Composable private fun GrowthPreview(phase: State<Float>) {
    val colors = MaterialTheme.colorScheme
    PreviewHeading(Icons.Outlined.Insights, stringResource(R.string.intro_art_week), "+18%")
    Text("$2,480.00", style = MaterialTheme.typography.headlineLarge)
    Canvas(Modifier.fillMaxWidth().height(66.dp)) {
        val curve = Path().apply {
            moveTo(0f, size.height * 0.85f)
            cubicTo(size.width * 0.2f, size.height * 0.95f, size.width * 0.18f, size.height * 0.45f, size.width * 0.4f, size.height * 0.57f)
            cubicTo(size.width * 0.7f, size.height * 0.85f, size.width * 0.72f, size.height * 0.2f, size.width, size.height * 0.13f)
        }
        val area = Path().apply { addPath(curve); lineTo(size.width, size.height); lineTo(0f, size.height); close() }
        drawPath(area, Brush.verticalGradient(listOf(colors.primary.copy(alpha = 0.13f), Color.Transparent)))
        drawPath(curve, colors.primary, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(colors.primary.copy(alpha = 0.12f), (7f + sin(phase.value)).dp.toPx(), Offset(size.width, size.height * 0.13f))
        drawCircle(colors.primary, 3.dp.toPx(), Offset(size.width, size.height * 0.13f))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        stringResource(R.string.intro_art_days).split(" ").forEach { Text(it, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant) }
    }
}
