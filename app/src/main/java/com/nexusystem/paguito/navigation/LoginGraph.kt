// Path:
// app/src/main/java/com/nexusystem/paguito/navigation/LoginGraph.kt

package com.nexusystem.paguito.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.nexusystem.paguito.domain.data.auth.UserDataModelAuth
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.login.AuthViewModel
import com.nexusystem.paguito.ui.screens.login.ChangePasswordScreen
import com.nexusystem.paguito.ui.screens.login.ForgotPasswordScreen
import com.nexusystem.paguito.ui.screens.login.LoginScreen
import com.nexusystem.paguito.ui.screens.login.OtpRecoveryScreen
import com.nexusystem.paguito.ui.screens.login.ProcessingScreen
import com.nexusystem.paguito.ui.screens.registro.OtpVerificationScreen
import com.nexusystem.paguito.ui.screens.registro.RegisterScreen
import com.nexusystem.paguito.ui.screens.registro.RegisterViewModel
import com.nexusystem.paguito.utils.PaguitoStore
import kotlinx.coroutines.launch

fun NavGraphBuilder.loginGraph(
    navController: NavHostController,
    context: Context,
    authViewModel: AuthViewModel,
    registerViewModel: RegisterViewModel
) {
    navigation(
        startDestination = Routes.ScreenLogin.route,
        route = Routes.LoginGraph.route
    ) {
        composable(Routes.ScreenLogin.route) {
            val scope = rememberCoroutineScope()

            LoginScreen(
                { user ->
                    if (user.userSuscription.isActive) {
                        navController.navigate(
                            Routes.ScreenDownloadInfo.route +
                                    "/${user.email}"
                        )
                    } else {
                        scope.launch {
                            PaguitoStore.setLoged(context)
                            PaguitoStore.setInvitedNot(context)
                        }

                        navController.navigate(
                            Routes.ScreenHome.route
                        )
                    }
                },
                {
                    navController.navigate(
                        Routes.ScreenRegister.route
                    )
                },
                {
                    scope.launch {
                        PaguitoStore.setLoged(context)
                        PaguitoStore.setInvited(context)
                    }

                    navController.navigate(
                        Routes.ScreenHome.route
                    )
                },
                {
                    navController.navigate(
                        Routes.ScreenRecoveryPassword.route
                    )
                },
                authViewModel
            )
        }

        composable(
            route = Routes.ScreenOtp.route +
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
                    UserDataModelAuth::class.java
                )
            } else {
                UserDataModelAuth()
            }

            OtpVerificationScreen(
                data,
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
                {},
                navController::popBackStack,
                authViewModel
            )
        }

        composable(
            route = Routes.ScreenDownloadInfo.route +
                    "/{params}",
            arguments = listOf(
                navArgument("params") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { params ->
            val scope = rememberCoroutineScope()

            val email = params.arguments
                ?.getString("params")
                .orEmpty()

            ProcessingScreen(
                email = email,
                authViewModel,
                {
                    scope.launch {
                        PaguitoStore.setLoged(context)
                        PaguitoStore.setInvitedNot(context)
                    }

                    navController.navigate(
                        Routes.ScreenHome.route
                    )
                },
                {
                    scope.launch {
                        PaguitoStore.setLoged(context)
                        PaguitoStore.setInvitedNot(context)
                    }

                    navController.navigate(
                        Routes.ScreenHome.route
                    )
                }
            )
        }

        composable(
            route = Routes.ScreenOtpRecovery.route +
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

            OtpRecoveryScreen(
                json,
                { data ->
                    val encodedData = Uri.encode(
                        Gson().toJson(data)
                    )

                    navController.navigate(
                        Routes.ScreenChangePassword.route +
                                "/$encodedData"
                    )
                },
                {},
                navController::popBackStack,
                authViewModel
            )
        }

        composable(
            Routes.ScreenRecoveryPassword.route
        ) {
            ForgotPasswordScreen(
                navController::popBackStack,
                { data ->
                    val json = Uri.encode(
                        Gson().toJson(data)
                    )

                    navController.navigate(
                        Routes.ScreenOtpRecovery.route +
                                "/$json"
                    )
                },
                authViewModel
            )
        }

        composable(
            route = Routes.ScreenChangePassword.route +
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

            ChangePasswordScreen(
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
                }
            )
        }

        composable(
            Routes.ScreenRegister.route
        ) {
            RegisterScreen(
                navController::popBackStack,
                { data ->
                    val json = Uri.encode(
                        Gson().toJson(data)
                    )

                    navController.navigate(
                        Routes.ScreenOtp.route +
                                "/$json"
                    )
                },
                {
                    navController.navigate(
                        Routes.ScreenLogin.route
                    )
                },
                registerViewModel
            )
        }
    }
}