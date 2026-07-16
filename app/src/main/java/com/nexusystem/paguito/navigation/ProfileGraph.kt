// Path:
// app/src/main/java/com/nexusystem/paguito/navigation/ProfileGraph.kt

package com.nexusystem.paguito.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.SelectLanguageScreen
import com.nexusystem.paguito.ui.screens.login.AuthViewModel
import com.nexusystem.paguito.ui.screens.perfil.ChangePasswordProfileScreen
import com.nexusystem.paguito.ui.screens.perfil.EditProfileScreen
import com.nexusystem.paguito.ui.screens.perfil.PerfiViewModel
import com.nexusystem.paguito.ui.screens.perfil.ProfileScreen

fun NavGraphBuilder.profileGraph(
    navController: NavHostController,
    perfilViewModel: PerfiViewModel,
    authViewModel: AuthViewModel
) {
    navigation(
        startDestination = Routes.ScreenProfile.route,
        route = Routes.ProfileGraph.route
    ) {
        composable(Routes.ScreenProfile.route) {
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
                onBack = navController::popBackStack,
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

        composable(
            route = Routes.ScreenChangePasswordProfile.route +
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

            ChangePasswordProfileScreen(
                json,
                authViewModel,
                {
                    navController.navigate(
                        Routes.ScreenLogin.route
                    ) {
                        popUpTo(
                            navController.graph.startDestinationId
                        ) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                },
                navController::popBackStack
            )
        }

        composable(
            Routes.ScreenPerfilEditar.route
        ) {
            EditProfileScreen(
                navController::popBackStack,
                perfilViewModel
            )
        }

        composable(
            Routes.ScreenIdioms.route
        ) {
            SelectLanguageScreen(
                isOnboarding = false,
                onContinueClick = navController::popBackStack,
                onBackClick = navController::popBackStack
            )
        }
    }
}