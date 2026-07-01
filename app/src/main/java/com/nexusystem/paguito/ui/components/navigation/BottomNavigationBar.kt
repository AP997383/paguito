package com.nexusystem.paguito.ui.components.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

private val NavTextInactive = Color(0xFF8A9198)

@Composable
fun RoundedBottomBar(
    navController: NavHostController,
    onItemSelected: (String) -> Unit
) {
    val currentDestination =
        navController.currentBackStackEntryAsState().value?.destination

    val currentRoute = currentDestination?.route

    val hiddenRoutes = setOf(
        Routes.ScreenWebsite.route,
        Routes.ScreenWebsiteSetup.route
    )

    if (currentRoute in hiddenRoutes) {
        return
    }

    val items = listOf(
        BottomNavItem("Home", BottomIcon.Vector(Icons.Default.Home), Routes.HomeGraph.route),
        BottomNavItem("Analisis", BottomIcon.Vector(Icons.Default.BarChart), Routes.AnalisisGraph.route),
        BottomNavItem("Clientes", BottomIcon.Vector(Icons.Default.People), Routes.ClientesGraph.route),
        BottomNavItem("Productos", BottomIcon.Vector(Icons.Default.ShoppingCart), Routes.ProductosGraph.route)
    )

    val navBarsPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = navBarsPadding),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 24.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                BottomBarItem(
                    item = item,
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                        onItemSelected(item.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor =
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    val contentColor =
        if (selected) MaterialTheme.colorScheme.background else NavTextInactive

    Box(
        modifier = Modifier
            .background(containerColor, RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val icon = item.icon) {
                is BottomIcon.Vector -> Icon(
                    imageVector = icon.icon,
                    contentDescription = item.label,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )

                is BottomIcon.Drawable -> Image(
                    painter = painterResource(id = icon.resId),
                    contentDescription = item.label,
                    colorFilter = ColorFilter.tint(contentColor),
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.label.uppercase(),
                color = contentColor,
                fontSize = 6.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
        }
    }
}

sealed class BottomIcon {
    data class Vector(val icon: ImageVector) : BottomIcon()
    data class Drawable(val resId: Int) : BottomIcon()
}

data class BottomNavItem(
    val label: String,
    val icon: BottomIcon,
    val route: String,
    val showLabel: Boolean = true
)