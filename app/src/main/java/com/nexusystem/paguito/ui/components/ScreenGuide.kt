package com.nexusystem.paguito.ui.components

import android.content.Context
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import kotlin.math.roundToInt

data class GuideStep(val target: String, val title: String, val description: String)

object ScreenGuidePreferences {
    private const val FILE = "screen_guides"
    fun isCompleted(context: Context, id: String) = context.getSharedPreferences(FILE, Context.MODE_PRIVATE).getBoolean(id, false)
    fun complete(context: Context, id: String) { context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit().putBoolean(id, true).apply() }
}

private class GuideAnchor(val requester: BringIntoViewRequester) { var bounds by mutableStateOf(Rect.Zero) }
private class GuideAnchors {
    val values = mutableStateMapOf<String, GuideAnchor>()
    var viewport by mutableStateOf(Rect.Zero)
}
private val LocalGuideAnchors = staticCompositionLocalOf<GuideAnchors?> { null }
val LocalScreenGuideVisible = staticCompositionLocalOf { false }

/** Apply outside verticalScroll so this measures the scroll viewport, not its entire content. */
fun Modifier.guideViewport(): Modifier = composed {
    val registry = LocalGuideAnchors.current
    if (registry == null) this else onGloballyPositioned {
        registry.viewport = Rect(it.positionInWindow(), Size(it.size.width.toFloat(), it.size.height.toFloat()))
    }
}

fun Modifier.guideTarget(id: String): Modifier = composed {
    val registry = LocalGuideAnchors.current
    if (registry == null) this else {
        val requester = remember { BringIntoViewRequester() }
        val anchor = remember(requester) { GuideAnchor(requester) }
        DisposableEffect(registry, id, anchor) {
            registry.values[id] = anchor
            onDispose { if (registry.values[id] === anchor) registry.values.remove(id) }
        }
        bringIntoViewRequester(requester).onGloballyPositioned {
            anchor.bounds = Rect(it.positionInWindow(), Size(it.size.width.toFloat(), it.size.height.toFloat()))
        }.testTag("guide-target-$id")
    }
}

/** Extra space lets the final section reach the same top position as the first one. */
@Composable
fun GuideScrollSpacer() {
    Spacer(Modifier.height(if (LocalScreenGuideVisible.current) LocalConfiguration.current.screenHeightDp.dp else 200.dp))
}

@Composable
fun ScreenGuideHost(id: String, label: String, steps: List<GuideStep>, scrollState: ScrollState? = null, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val anchors = remember(id) { GuideAnchors() }
    var completed by rememberSaveable(id) { mutableStateOf(ScreenGuidePreferences.isCompleted(context, id)) }
    var index by rememberSaveable(id) { mutableIntStateOf(0) }
    val visible = !completed && steps.isNotEmpty()
    val finish = { ScreenGuidePreferences.complete(context, id); completed = true }
    CompositionLocalProvider(LocalGuideAnchors provides anchors, LocalScreenGuideVisible provides visible) { content() }
    if (visible) {
        val safeIndex = index.coerceIn(steps.indices)
        GuideOverlay(label, steps[safeIndex], safeIndex, steps.size, anchors, scrollState,
            onNext = { if (safeIndex == steps.lastIndex) finish() else index = safeIndex + 1 }, onSkip = finish)
    }
}

@Composable
private fun GuideOverlay(label: String, step: GuideStep, index: Int, count: Int, registry: GuideAnchors, scroll: ScrollState?, onNext: () -> Unit, onSkip: () -> Unit) {
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme
    val anchor = registry.values[step.target]
    Dialog(onDismissRequest = onSkip, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        val view = LocalView.current
        SideEffect {
            (view.parent as? DialogWindowProvider)?.window?.let {
                it.setDimAmount(0f)
                it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            }
        }
        BoxWithConstraints(Modifier.fillMaxSize().testTag("screen-guide")) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val topInset = WindowInsets.statusBars.getTop(density).toFloat()
            val bottomInset = WindowInsets.navigationBars.getBottom(density).toFloat()
            val margin = with(density) { 24.dp.toPx() }
            val pointerGap = with(density) { 52.dp.toPx() }
            val desiredTop = maxOf(registry.viewport.top, topInset) + with(density) { 56.dp.toPx() }
            var captionHeight by remember { mutableIntStateOf(with(density) { 230.dp.roundToPx() }) }
            var ready by remember(step.target) { mutableStateOf(false) }
            val maxTargetHeight = (height - desiredTop - pointerGap - captionHeight - bottomInset - margin).coerceAtLeast(with(density) { 88.dp.toPx() })

            LaunchedEffect(step.target, anchor, desiredTop, captionHeight) {
                if (anchor != null) {
                    withFrameNanos { }
                    if (scroll != null) {
                        val destination = scroll.value + (anchor.bounds.top - desiredTop).roundToInt()
                        scroll.animateScrollTo(destination.coerceIn(0, scroll.maxValue))
                    } else {
                        val viewportHeight = registry.viewport.height.takeIf { it > 0f } ?: height
                        anchor.requester.bringIntoView(Rect(0f, -with(density) { 56.dp.toPx() }, anchor.bounds.width, viewportHeight - with(density) { 56.dp.toPx() }))
                    }
                    withFrameNanos { }
                    ready = true
                }
            }

            val highlight = if (ready) anchor?.bounds?.let {
                val pad = with(density) { 6.dp.toPx() }
                Rect((it.left - pad).coerceAtLeast(8.dp.toPx(density)), it.top - pad,
                    (it.right + pad).coerceAtMost(width - 8.dp.toPx(density)), it.top + it.height.coerceAtMost(maxTargetHeight) + pad)
            } else null

            Canvas(Modifier.fillMaxSize().graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                .clickable(remember { MutableInteractionSource() }, indication = null, onClickLabel = "Siguiente paso", onClick = onNext).testTag("guide-next-area")) {
                drawRect(Color.Black.copy(alpha = 0.94f))
                if (highlight != null) {
                    drawRoundRect(Color.Transparent, highlight.topLeft, highlight.size, CornerRadius(24.dp.toPx()), blendMode = BlendMode.Clear)
                    drawRoundRect(colors.primary.copy(alpha = 0.9f), highlight.topLeft, highlight.size, CornerRadius(24.dp.toPx()), style = Stroke(1.5.dp.toPx()))
                }
            }

            Row(Modifier.fillMaxWidth().offset { IntOffset(0, (topInset + with(density) { 4.dp.toPx() }).roundToInt()) }
                .padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("$label · ${index + 1} de $count", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.8f), modifier = Modifier.weight(1f))
                TextButton(onClick = onSkip, colors = ButtonDefaults.textButtonColors(contentColor = Color.White), modifier = Modifier.testTag("guide-skip")) { Text("Omitir") }
            }

            if (highlight != null) {
                val pointerTop = highlight.bottom + with(density) { 6.dp.toPx() }
                Canvas(Modifier.offset { IntOffset((highlight.center.x - with(density) { 18.dp.toPx() }).roundToInt(), pointerTop.roundToInt()) }
                    .size(36.dp).testTag("guide-pointer").semantics { contentDescription = "Explicación de ${step.title}" }) {
                    val center = size.width / 2f
                    val shaft = Path().apply {
                        moveTo(center, size.height)
                        cubicTo(center + 8.dp.toPx(), size.height * 0.6f, center + 6.dp.toPx(), size.height * 0.3f, center, 2.dp.toPx())
                    }
                    drawPath(shaft, Color.White.copy(alpha = 0.95f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
                    val head = Path().apply { moveTo(center - 6.dp.toPx(), 10.dp.toPx()); lineTo(center, 2.dp.toPx()); lineTo(center + 7.dp.toPx(), 8.dp.toPx()) }
                    drawPath(head, Color.White.copy(alpha = 0.95f), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
            }

            // The explanation deliberately has no card/background: it belongs to the guide, not the app.
            if (ready || anchor == null) {
                val captionTop = highlight?.let { it.bottom + pointerGap } ?: desiredTop
                val captionLimit = with(density) { (height - desiredTop - bottomInset - margin - pointerGap - 100.dp.toPx()).coerceAtLeast(96.dp.toPx()).toDp() }
                Column(Modifier.offset { IntOffset(margin.roundToInt(), captionTop.roundToInt()) }.width(maxWidth - 48.dp)
                    .heightIn(max = captionLimit).verticalScroll(rememberScrollState())
                    .pointerInput(onNext) { detectTapGestures(onTap = { onNext() }) }
                    .onSizeChanged { captionHeight = it.height }.testTag("guide-panel")
                    .semantics { liveRegion = LiveRegionMode.Polite; paneTitle = "$label, paso ${index + 1} de $count" },
                    verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(step.title, style = MaterialTheme.typography.titleLarge, color = Color.White, modifier = Modifier.testTag("guide-title"))
                    Text(step.description, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.88f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        LinearProgressIndicator(progress = { (index + 1f) / count }, modifier = Modifier.weight(1f).height(3.dp),
                            color = Color.White.copy(alpha = 0.9f), trackColor = Color.White.copy(alpha = 0.2f))
                        Button(onClick = onNext, shape = MaterialTheme.shapes.medium, modifier = Modifier.heightIn(min = 48.dp).testTag("guide-next")) {
                            Text(if (index == count - 1) "Entendido" else "Siguiente")
                            Spacer(Modifier.width(8.dp)); Icon(if (index == count - 1) Icons.Outlined.Check else Icons.AutoMirrored.Outlined.ArrowForward, null, modifier = Modifier.size(18.dp))
                        }
                    }
                    Text("Toca la pantalla para continuar", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.65f))
                }
            }
        }
    }
}

private fun androidx.compose.ui.unit.Dp.toPx(density: androidx.compose.ui.unit.Density) = with(density) { toPx() }
