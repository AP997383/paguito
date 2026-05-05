// Path: com/beastspinning/medi/ui/components/RoundedBottomBar.kt
package com.nexusystem.paguito.ui.components.navigation


import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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

// === COLORES DEL DISEÑO ===
private val NavBgDark = Color(0xFF141619) // Fondo casi negro de la barra
private val NavTextActive = Color.Black // Texto e icono negro cuando está seleccionado
private val NavTextInactive = Color(0xFF8A9198) // Gris para íconos/texto inactivos

@Composable
fun RoundedBottomBar(
    navController: NavHostController,
    onItemSelected: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(
            label = "Resumen",
            icon = BottomIcon.Vector(Icons.Default.Home),
            route = Routes.HomeGraph.route
        ),
        BottomNavItem(
            label = "Clientes",
            icon = BottomIcon.Vector(Icons.Default.People),
            route = Routes.ClientesGraph.route
        ),
        BottomNavItem(
            label = "Productos",
            icon = BottomIcon.Vector(Icons.Default.ShoppingCart),
            route = Routes.ProductosGraph.route
        ),
        BottomNavItem(
            label = "Perfil",
            icon = BottomIcon.Vector(Icons.Default.Settings),
            route = Routes.ProfileGraph.route
        )
    )

    val navBarsPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = navBarsPadding), // La barra toca los bordes laterales y se asienta abajo
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp), // Solo bordes superiores redondeados
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 24.dp // Sombra fuerte hacia arriba
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp) // Espaciado interno generoso
                .height(64.dp), // Barra un poco más alta para acomodar el diseño apilado
            horizontalArrangement = Arrangement.SpaceBetween, // Distribuye los ítems uniformemente
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val selected = currentRoute?.hierarchy?.any { it.route == item.route } == true

                BottomBarItem(
                    item = item,
                    selected = selected,
                    onClick = {
                        // Navegación segura
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
    // Definimos los colores basados en el estado
    val containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.background else NavTextInactive

    // Usamos Box en lugar de Surface para tener más control sobre el ripple effect y el tamaño exacto
    Box(
        modifier = Modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(24.dp) // Forma de píldora (ovalada)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null, // Quitamos el efecto ripple por defecto para un look más limpio
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 10.dp), // Padding interno de la píldora
        contentAlignment = Alignment.Center
    ) {
        // En este diseño, el icono y el texto siempre están apilados verticalmente
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ICONO
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

            // TEXTO (Siempre visible en este diseño, apilado debajo)
            Spacer(modifier = Modifier.height(4.dp)) // Pequeña separación entre icono y texto

            Text(
                text = item.label.uppercase(), // Forzamos mayúsculas como en el diseño
                color = contentColor,
                fontSize = 6.sp, // Letra pequeña
                fontWeight = FontWeight.ExtraBold, // Muy negrita
                letterSpacing = 1.sp // Un poco de espacio entre letras
            )
        }
    }
}

// === TUS CLASES DE DATOS (Se mantienen igual) ===

sealed class BottomIcon {
    data class Vector(val icon: ImageVector) : BottomIcon()
    data class Drawable(val resId: Int) : BottomIcon()
}

data class BottomNavItem(
    val label: String,
    val icon: BottomIcon,
    val route: String,
    val showLabel: Boolean = true)


