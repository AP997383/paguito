// Path: com/beastspinning/medi/ui/components/RoundedBottomBar.kt
package com.nexusystem.paguito.ui.components.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.nexusystem.paguito.R
import kotlin.collections.forEach
import kotlin.sequences.any

@Composable
fun RoundedBottomBar(
    navController: NavHostController,
    onItemSelected: (String) -> Unit,
) {

    // ✅ IMPORTANTE:
    // item.route debe ser la ROUTE DEL GRAPH (HOME_GRAPH, PROFILE_GRAPH, etc.)
    val items = listOf(
        BottomNavItem(
            label = "Home",
            icon = BottomIcon.Vector(Icons.Default.Home),
            route = Routes.HomeGraph.route
        ),
        BottomNavItem(
            label = "Calendario",
            icon = BottomIcon.Vector(Icons.Default.Checklist),
            route = Routes.ClientesGraph.route
        ),
        BottomNavItem(
            label = "Perfil",
            icon = BottomIcon.Vector(Icons.Default.Settings),
            route = Routes.ProfileGraph.route
        )
    )

    val navBarsPadding = WindowInsets
        .navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    val iconSize = 18.dp

    val currentDestination =
        navController.currentBackStackEntryAsState().value?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = navBarsPadding + 10.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .height(60.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {

                items.forEach { item ->

                    // ✅ Selected correcto aunque estés en pantallas internas del flujo:
                    // revisa jerarquía del graph (HomeGraph/ProfileGraph/etc.)
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.route == item.route } == true

                    IconButton(
                        onClick = {
                            // ✅ Navegación "segura" a graphs: evita crashes y duplicados
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true

                                // 👇 mantiene el estado de cada tab y evita que se acumulen destinos
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }

                            onItemSelected(item.route)
                        },
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                                        )
                                )
                            }

                            when (val icon = item.icon) {
                                is BottomIcon.Vector -> Icon(
                                    imageVector = icon.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(iconSize),
                                    tint = if (selected)
                                        colorResource(id = R.color.medi_blue)
                                    else
                                        colorResource(id = R.color.medi_gray)
                                )

                                is BottomIcon.Drawable -> Image(
                                    painter = painterResource(id = icon.resId),
                                    contentDescription = item.label,
                                    modifier = Modifier.size(iconSize),
                                    colorFilter = ColorFilter.tint(
                                        if (selected)
                                            colorResource(id = R.color.medi_blue)
                                        else
                                            colorResource(id = R.color.medi_gray)
                                    )
                                )
                            }
                        }
                    }
                }
            }
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
    val route: String
)