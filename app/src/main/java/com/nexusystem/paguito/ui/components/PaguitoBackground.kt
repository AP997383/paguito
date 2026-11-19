package com.nexusystem.paguito.ui.components

import android.animation.ValueAnimator
import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nexusystem.paguito.ui.theme.LocalAppearance
import kotlin.math.sin

/** Vector artwork; animation updates drawing only, pauses offscreen and honors Android's animation setting. */
fun Modifier.paguitoBackground(fillBackground: Boolean = true): Modifier = composed {
    val colors = MaterialTheme.colorScheme
    val enabled = LocalAppearance.current.showWaves
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var active by remember(lifecycle) { mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, _ -> active = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED) }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val phase = if (enabled && active && !LocalInspectionMode.current && ValueAnimator.areAnimatorsEnabled()) {
        rememberInfiniteTransition(label = "financial-background").animateFloat(
            0f, 6.283185f, infiniteRepeatable(tween(24000, easing = LinearEasing)), label = "financial-drift")
    } else rememberUpdatedState(0f)
    drawWithCache {
        val tile = 220.dp.toPx()
        val ink = colors.primary.copy(alpha = 0.085f)
        val faintInk = colors.primary.copy(alpha = 0.045f)
        val line = Stroke(1.25.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        onDrawBehind {
            if (fillBackground) drawRect(colors.background)
            if (enabled) {
                val motion = phase.value
                val rows = (size.height / tile).toInt() + 1
                for (row in 0..rows) {
                    val drift = sin(motion + row * 1.2f) * 5.dp.toPx()
                    val walletX = size.width * (if (row % 2 == 0) 0.08f else 0.78f)
                    val baseY = row * tile + 42.dp.toPx()
                    withTransform({ translate(walletX, baseY + drift); rotate(sin(motion + row) * 3f, Offset.Zero) }) {
                        financeWallet(58.dp.toPx(), ink, faintInk, line)
                    }
                    val coin = Offset(size.width * (if (row % 2 == 0) 0.86f else 0.18f), baseY + 112.dp.toPx() - drift)
                    withTransform({ translate(coin.x, coin.y); rotate(-8f + sin(motion + row) * 4f, Offset.Zero) }) {
                        financeCoin(23.dp.toPx(), ink, faintInk, line)
                    }
                    val noteOrigin = Offset(size.width * 0.48f, baseY + 72.dp.toPx() + drift * 0.5f)
                    withTransform({ translate(noteOrigin.x, noteOrigin.y); rotate(-12f + sin(motion) * 2f, Offset.Zero) }) {
                        financeNote(64.dp.toPx(), faintInk, line)
                    }
                    for (dot in 0..4) drawCircle(faintInk, 1.1.dp.toPx(), Offset(size.width * 0.34f + dot * 8.dp.toPx(), baseY + 168.dp.toPx() + sin(motion + dot) * 2.dp.toPx()))
                }
            }
        }
    }
}

private fun DrawScope.financeWallet(width: Float, ink: Color, fill: Color, line: Stroke) {
    val height = width * 0.68f
    drawRoundRect(fill, size = Size(width, height), cornerRadius = CornerRadius(width * 0.14f))
    drawRoundRect(ink, size = Size(width, height), cornerRadius = CornerRadius(width * 0.14f), style = line)
    drawLine(ink, Offset(width * 0.08f, height * 0.21f), Offset(width * 0.86f, height * 0.21f), line.width, StrokeCap.Round)
    drawRoundRect(ink, topLeft = Offset(width * 0.65f, height * 0.4f), size = Size(width * 0.41f, height * 0.37f), cornerRadius = CornerRadius(width * 0.07f), style = line)
    drawCircle(ink, width * 0.025f, Offset(width * 0.77f, height * 0.58f))
}

private fun DrawScope.financeCoin(radius: Float, ink: Color, fill: Color, line: Stroke) {
    drawCircle(fill, radius, Offset.Zero)
    drawCircle(ink, radius, Offset.Zero, style = line)
    drawCircle(ink.copy(alpha = ink.alpha * 0.45f), radius * 0.8f, Offset.Zero, style = line)
    val dollar = Path().apply {
        moveTo(radius * 0.28f, -radius * 0.34f)
        cubicTo(-radius * 0.38f, -radius * 0.7f, -radius * 0.52f, -radius * 0.04f, 0f, 0f)
        cubicTo(radius * 0.55f, radius * 0.04f, radius * 0.36f, radius * 0.65f, -radius * 0.3f, radius * 0.34f)
    }
    drawPath(dollar, ink, style = line)
    drawLine(ink, Offset(0f, -radius * 0.57f), Offset(0f, radius * 0.57f), line.width, StrokeCap.Round)
}

private fun DrawScope.financeNote(width: Float, ink: Color, line: Stroke) {
    val height = width * 0.48f
    drawRoundRect(ink, size = Size(width, height), cornerRadius = CornerRadius(width * 0.06f), style = line)
    drawRoundRect(ink, topLeft = Offset(width * 0.1f, height * 0.17f), size = Size(width * 0.8f, height * 0.66f), cornerRadius = CornerRadius(width * 0.04f), style = line)
    drawCircle(ink, height * 0.19f, Offset(width / 2f, height / 2f), style = line)
}
