package com.nexusystem.paguito.ui.components.navigation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

/** Barra sin elevación: una posición animada sincroniza la curva, el indicador y los iconos. */
@Composable
fun RoundedBottomBar(navController: NavHostController, onItemSelected: (String) -> Unit = {}) {
    val destination = navController.currentBackStackEntryAsState().value?.destination
    if (destination?.route in setOf(Routes.ScreenWebsite.route, Routes.ScreenWebsiteSetup.route)) return
    val items = remember {
        listOf(
            BottomNavItem("Inicio", BottomIcon.Vector(Icons.Outlined.Home), Routes.HomeGraph.route),
            BottomNavItem("Clientes", BottomIcon.Vector(Icons.Outlined.People), Routes.ClientesGraph.route),
            BottomNavItem("Productos", BottomIcon.Vector(Icons.Outlined.ShoppingBag), Routes.ProductosGraph.route),
            BottomNavItem("Ventas", BottomIcon.Vector(Icons.Outlined.ReceiptLong), Routes.SalesGraph.route),
            BottomNavItem("Perfil", BottomIcon.Vector(Icons.Outlined.Person), Routes.ScreenProfileTab.route)
        )
    }
    val selected = items.indexOfFirst { item ->
        destination?.hierarchy?.any { it.route == item.route || (item.route == Routes.ScreenProfileTab.route && it.route == Routes.ProfileGraph.route) } == true
    }
    var previousSelection by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(selected) { if (selected >= 0) previousSelection = selected }
    val activeIndex = if (selected >= 0) selected else previousSelection
    val position = animateFloatAsState(
        targetValue = activeIndex.toFloat(),
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "navigation-position"
    )
    val colors = MaterialTheme.colorScheme

    Box(Modifier.testTag("fluid-bottom-bar").fillMaxWidth().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 8.dp).height(92.dp)) {
        // El contorno y el círculo se dibujan sin sombras de RenderNode ni capas elevadas.
        // Leer la posición en dibujo evita recomponer la barra completa en cada frame.
        Canvas(Modifier.matchParentSize()) {
            val logicalCenter = (position.value + 0.5f) / items.size
            val center = if (layoutDirection == LayoutDirection.Rtl) 1f - logicalCenter else logicalCenter
            val path = navigationContour(size, center)
            drawPath(path, colors.surface)
            drawPath(path, colors.outlineVariant.copy(alpha = 0.5f), style = Stroke(1.dp.toPx()))
            drawCircle(colors.primary, radius = 25.dp.toPx(), center = Offset(center * size.width, 33.dp.toPx()))
        }
        Row(Modifier.fillMaxSize().selectableGroup()) {
            items.forEachIndexed { index, item ->
                val isActive = activeIndex == index
                val interactionSource = remember(item.route) { MutableInteractionSource() }
                Box(Modifier.weight(1f).fillMaxHeight().selectable(
                    selected = isActive, interactionSource = interactionSource,
                    indication = null, role = Role.Tab, onClick = {
                        navigateToMainTab(navController, item.route)
                        onItemSelected(item.route)
                    }
                )) {
                    Box(
                        Modifier.align(Alignment.TopCenter).offset {
                            val emphasis = (1f - abs(index - position.value)).coerceIn(0f, 1f)
                            IntOffset(0, (18.dp.toPx() - 10.dp.toPx() * emphasis).roundToInt())
                        }.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        NavigationTabIcon(item.icon, index, position)
                    }
                    Text(
                        item.label,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 13.dp, start = 2.dp, end = 2.dp),
                        style = MaterialTheme.typography.labelSmall, fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isActive) colors.primary else colors.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** Interpola únicamente el icono que cambia; la selección accesible depende de la ruta, no de la animación. */
@Composable
private fun NavigationTabIcon(icon: BottomIcon, index: Int, position: State<Float>) {
    val colors = MaterialTheme.colorScheme
    val emphasis = (1f - abs(index - position.value)).coerceIn(0f, 1f)
    val tint = lerp(colors.onSurfaceVariant, colors.onPrimary, emphasis)
    when (icon) {
        is BottomIcon.Vector -> Icon(icon.icon, null, tint = tint, modifier = Modifier.size(25.dp))
        is BottomIcon.Drawable -> Image(painterResource(icon.resId), null, colorFilter = ColorFilter.tint(tint), modifier = Modifier.size(25.dp))
    }
}

/** Contorno continuo que sigue la misma posición del indicador, incluidos los extremos de la barra. */
private fun Density.navigationContour(size: Size, centerFraction: Float): Path {
    val radius = 24.dp.toPx().coerceAtMost(size.width / 4f)
    val base = 17.dp.toPx()
    val lift = 14.dp.toPx()
    val span = 43.dp.toPx()
    val center = size.width * centerFraction
    fun top(x: Float): Float {
        val distance = (x - center) / span
        return base - lift * exp((-2f * distance * distance).toDouble()).toFloat()
    }
    return Path().apply {
        moveTo(0f, top(0f) + radius)
        quadraticBezierTo(0f, top(0f), radius, top(radius))
        for (index in 1..96) {
            val x = radius + (size.width - 2f * radius) * index / 96f
            lineTo(x, top(x))
        }
        quadraticBezierTo(size.width, top(size.width), size.width, top(size.width) + radius)
        lineTo(size.width, size.height - radius)
        quadraticBezierTo(size.width, size.height, size.width - radius, size.height)
        lineTo(radius, size.height)
        quadraticBezierTo(0f, size.height, 0f, size.height - radius)
        close()
    }
}

sealed class BottomIcon {
    data class Vector(val icon: ImageVector) : BottomIcon()
    data class Drawable(val resId: Int) : BottomIcon()
}

data class BottomNavItem(val label: String, val icon: BottomIcon, val route: String, val showLabel: Boolean = true)

/** Analysis is a secondary screen, never the restored root of a main tab. */
fun navigateToMainTab(navController: NavHostController, route: String) {
    if (runCatching { navController.getBackStackEntry(Routes.AnalisisGraph.route) }.isSuccess) {
        navController.popBackStack(Routes.AnalisisGraph.route, inclusive = true)
    }
    val home = runCatching { navController.getBackStackEntry(Routes.ScreenHome.route) }.getOrNull()
    val target = if (route == Routes.HomeGraph.route && home != null) Routes.ScreenHome.route else route
    navController.navigate(target) {
        popUpTo(home?.destination?.id ?: navController.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = route != Routes.HomeGraph.route
    }
}
