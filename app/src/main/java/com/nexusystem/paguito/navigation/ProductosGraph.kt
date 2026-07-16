// Path:
// app/src/main/java/com/nexusystem/paguito/navigation/ProductosGraph.kt

package com.nexusystem.paguito.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.perfil.PerfiViewModel
import com.nexusystem.paguito.ui.screens.perfil.ProfileScreen
import com.nexusystem.paguito.ui.screens.productos.AddProductScreen
import com.nexusystem.paguito.ui.screens.productos.ProductListScreen
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel

fun NavGraphBuilder.productosGraph(
    navController: NavHostController,
    productosViewModel: ProductosViewModel,
    perfilViewModel: PerfiViewModel
) {
    navigation(
        startDestination = Routes.ScreenListaProductos.route,
        route = Routes.ProductosGraph.route
    ) {
        composable(
            route = Routes.ScreenNuevoProducto.route +
                    "/{params}",
            arguments = listOf(
                navArgument("params") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { params ->
            val json = params.arguments
                ?.getString("params")
                .orEmpty()

            val data = if (json.isNotEmpty()) {
                Gson().fromJson(
                    json,
                    PorductosEntity::class.java
                )
            } else {
                PorductosEntity(
                    id = null,
                    idRemoteDatabase = ""
                )
            }

            AddProductScreen(
                navController::popBackStack,
                productosViewModel,
                data
            )
        }

        composable(
            Routes.ScreenListaProductos.route
        ) {
            ProductListScreen(
                navController::popBackStack,
                { data ->
                    val json = Uri.encode(
                        Gson().toJson(data)
                    )

                    navController.navigate(
                        Routes.ScreenNuevoProducto.route +
                                "/$json"
                    )
                },
                {
                    navController.navigate(
                        Routes.ScreenNuevoProducto.route + "/"
                    )
                },
                {
                    navController.navigate(
                        Routes.ScreenProfile.route
                    )
                },
                productosViewModel
            )
        }

        composable(
            Routes.ScreenProfile.route
        ) {
            ProfileScreen(
                {
                    navController.navigate(
                        Routes.ScreenLogin.route
                    ) {
                        popUpTo(Routes.ProfileGraph.route) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                },
                {
                    navController.navigate(
                        Routes.ScreenPerfilEditar.route
                    )
                },
                navController::popBackStack,
                openIdiomas = {
                    navController.navigate(
                        Routes.ScreenIdioms.route
                    )
                },
                openChangePassword = { data ->
                    val json = Uri.encode(
                        Gson().toJson(data)
                    )

                    navController.navigate(
                        Routes.ScreenChangePasswordProfile.route +
                                "/$json"
                    )
                },
                viewModel = perfilViewModel
            )
        }
    }
}