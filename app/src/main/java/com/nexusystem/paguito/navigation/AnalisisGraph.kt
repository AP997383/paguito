// Path:
// app/src/main/java/com/nexusystem/paguito/navigation/AnalisisGraph.kt

package com.nexusystem.paguito.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.analisis.AnalisisScreen
import com.nexusystem.paguito.ui.screens.analisis.AnalisisViewModel
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel

fun NavGraphBuilder.analisisGraph(
    navController: NavHostController,
    deudoresViewModel: DeudoresViewModel,
    pagosViewModel: PagosViewModel,
    productosViewModel: ProductosViewModel,
    analisisViewModel: AnalisisViewModel
) {
    navigation(
        startDestination = Routes.ScreenAnalisis.route,
        route = Routes.AnalisisGraph.route
    ) {
        composable(Routes.ScreenAnalisis.route) {
            AnalisisScreen(
                {
                    navController.navigate(
                        Routes.ScreenProfile.route
                    )
                },
                deudoresViewModel,
                pagosViewModel,
                productosViewModel,
                analisisViewModel
            )
        }
    }
}