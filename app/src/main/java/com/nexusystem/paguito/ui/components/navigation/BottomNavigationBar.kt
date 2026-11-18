package com.nexusystem.paguito.ui.components.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
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
import kotlin.math.exp

@Composable
fun RoundedBottomBar(navController: NavHostController, onItemSelected: (String) -> Unit = {}) {
    val destination = navController.currentBackStackEntryAsState().value?.destination
    if (destination?.route in setOf(Routes.ScreenWebsite.route, Routes.ScreenWebsiteSetup.route)) return
    val items = listOf(
        BottomNavItem("Inicio", BottomIcon.Vector(Icons.Outlined.Home), Routes.HomeGraph.route),
        BottomNavItem("Clientes", BottomIcon.Vector(Icons.Outlined.People), Routes.ClientesGraph.route),
        BottomNavItem("Productos", BottomIcon.Vector(Icons.Outlined.ShoppingBag), Routes.ProductosGraph.route),
        BottomNavItem("Ventas", BottomIcon.Vector(Icons.Outlined.ReceiptLong), Routes.SalesGraph.route),
        BottomNavItem("Perfil", BottomIcon.Vector(Icons.Outlined.Person), Routes.ScreenProfileTab.route)
    )
    val selected = items.indexOfFirst { item ->
        destination?.hierarchy?.any { it.route == item.route || (item.route == Routes.ScreenProfileTab.route && it.route == Routes.ProfileGraph.route) } == true
    }
    var previousSelection by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(selected) { if (selected >= 0) previousSelection = selected }
    val activeIndex = if (selected >= 0) selected else previousSelection
    val crest by animateFloatAsState((activeIndex + 0.5f) / items.size,
        animationSpec = spring(dampingRatio = 0.82f, stiffness = Spring.StiffnessMediumLow), label = "navigation-curve")
    val shape = remember(crest) { CurvedNavigationShape(crest) }
    val colors = MaterialTheme.colorScheme

    Box(Modifier.testTag("fluid-bottom-bar").fillMaxWidth().navigationBarsPadding().padding(horizontal = 14.dp, vertical = 8.dp).height(92.dp)) {
        Surface(modifier = Modifier.matchParentSize(), shape = shape, color = colors.surface,
            tonalElevation = 0.dp, shadowElevation = 14.dp, border = androidx.compose.foundation.BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.5f))) {}
        Row(Modifier.fillMaxSize().selectableGroup()) {
            items.forEachIndexed { index, item ->
                val isActive = activeIndex == index
                val rise by animateDpAsState(if (isActive) 10.dp else 0.dp,
                    animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow), label = "navigation-icon-rise")
                val iconColor by animateColorAsState(if (isActive) colors.onPrimary else colors.onSurfaceVariant, label = "navigation-icon-color")
                val bubbleColor by animateColorAsState(if (isActive) colors.primary else Color.Transparent, label = "navigation-bubble-color")
                Box(Modifier.weight(1f).fillMaxHeight().selectable(selected = isActive, role = Role.Tab, onClick = {
                    navigateToMainTab(navController, item.route)
                    onItemSelected(item.route)
                })) {
                    Box(Modifier.align(Alignment.TopCenter).offset(y = 18.dp - rise).size(50.dp)
                        .shadow(if (isActive) 6.dp else 0.dp, CircleShape).background(bubbleColor, CircleShape), contentAlignment = Alignment.Center) {
                        when (val icon = item.icon) {
                            is BottomIcon.Vector -> Icon(icon.icon, null, tint = iconColor, modifier = Modifier.size(25.dp))
                            is BottomIcon.Drawable -> Image(painterResource(icon.resId), null, colorFilter = ColorFilter.tint(iconColor), modifier = Modifier.size(25.dp))
                        }
                    }
                    Text(item.label, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 13.dp, start = 2.dp, end = 2.dp),
                        style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (isActive) colors.primary else colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

/** A soft crest follows the selected icon; its continuous profile also handles the edge tabs. */
private class CurvedNavigationShape(private val centerFraction: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline = with(density) {
        val radius = 24.dp.toPx().coerceAtMost(size.width / 4f)
        val base = 17.dp.toPx()
        val lift = 14.dp.toPx()
        val span = 43.dp.toPx()
        val center = size.width * centerFraction
        fun top(x: Float): Float { val distance = (x - center) / span; return base - lift * exp((-2f * distance * distance).toDouble()).toFloat() }
        val path = Path().apply {
            moveTo(0f, top(0f) + radius)
            quadraticBezierTo(0f, top(0f), radius, top(radius))
            for (index in 1..96) { val x = radius + (size.width - 2f * radius) * index / 96f; lineTo(x, top(x)) }
            quadraticBezierTo(size.width, top(size.width), size.width, top(size.width) + radius)
            lineTo(size.width, size.height - radius)
            quadraticBezierTo(size.width, size.height, size.width - radius, size.height)
            lineTo(radius, size.height)
            quadraticBezierTo(0f, size.height, 0f, size.height - radius)
            close()
        }
        Outline.Generic(path)
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
