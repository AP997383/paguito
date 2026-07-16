// Path:
// app/src/main/java/com/nexusystem/paguito/MediNavHost.kt

package com.nexusystem.paguito

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.android.play.core.review.ReviewManagerFactory
import com.nexusystem.paguito.navigation.analisisGraph
import com.nexusystem.paguito.navigation.clientesGraph
import com.nexusystem.paguito.navigation.homeGraph
import com.nexusystem.paguito.navigation.loginGraph
import com.nexusystem.paguito.navigation.productosGraph
import com.nexusystem.paguito.navigation.profileGraph
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.analisis.AnalisisViewModel
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.login.AuthViewModel
import com.nexusystem.paguito.ui.screens.onboarding.OnboardingScreen
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.perfil.PerfiViewModel
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel
import com.nexusystem.paguito.ui.screens.registro.RegisterViewModel
import com.nexusystem.paguito.utils.OnboardingStore
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MediNavHost(
    navController: NavHostController,
    deudoresViewModel: DeudoresViewModel,
    pagosViewModel: PagosViewModel,
    productosViewModel: ProductosViewModel,
    analisisViewModel: AnalisisViewModel,
    perfilViewModel: PerfiViewModel,
    authViewModel: AuthViewModel,
    registerwModel: RegisterViewModel,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val resolvedStartDestination = when (startDestination) {
        Routes.ScreenHome.route -> Routes.HomeGraph.route
        Routes.ScreenDeudoresList.route -> Routes.ClientesGraph.route
        Routes.ScreenProfile.route -> Routes.ProfileGraph.route
        Routes.ScreenAnalisis.route -> Routes.AnalisisGraph.route
        Routes.ScreenLogin.route -> Routes.LoginGraph.route
        else -> startDestination
    }

    NavHost(
        navController = navController,
        startDestination = resolvedStartDestination,
        modifier = modifier
    ) {
        composable(Routes.ScreenOnboarding.route) {
            OnboardingScreen(
                onFinish = {
                    scope.launch {
                        OnboardingStore.setOnboardingSeen(context)
                    }

                    navController.navigate(Routes.LoginGraph.route) {
                        popUpTo(Routes.ScreenOnboarding.route) {
                            inclusive = true
                        }

                        launchSingleTop = true
                    }
                }
            )
        }

        homeGraph(
            navController = navController,
            deudoresViewModel = deudoresViewModel,
            pagosViewModel = pagosViewModel,
            productosViewModel = productosViewModel
        )

        analisisGraph(
            navController = navController,
            deudoresViewModel = deudoresViewModel,
            pagosViewModel = pagosViewModel,
            productosViewModel = productosViewModel,
            analisisViewModel = analisisViewModel
        )

        profileGraph(
            navController = navController,
            perfilViewModel = perfilViewModel,
            authViewModel = authViewModel
        )

        productosGraph(
            navController = navController,
            productosViewModel = productosViewModel,
            perfilViewModel = perfilViewModel
        )

        loginGraph(
            navController = navController,
            context = context,
            authViewModel = authViewModel,
            registerViewModel = registerwModel
        )

        clientesGraph(
            navController = navController,
            deudoresViewModel = deudoresViewModel,
            pagosViewModel = pagosViewModel,
            productosViewModel = productosViewModel,
            perfilViewModel = perfilViewModel
        )
    }
}

fun showInAppReview(context: Context) {
    val activity = context as? Activity ?: return
    val reviewManager = ReviewManagerFactory.create(activity)

    reviewManager
        .requestReviewFlow()
        .addOnCompleteListener { task ->
            Log.d(
                "ASK_FOR_REVIEW",
                "Request completed: ${task.isSuccessful}"
            )

            if (!task.isSuccessful) {
                return@addOnCompleteListener
            }

            reviewManager.launchReviewFlow(
                activity,
                task.result
            )

            context
                .getSharedPreferences(
                    "settings",
                    Context.MODE_PRIVATE
                )
                .edit()
                .putBoolean(
                    "appReviwed",
                    true
                )
                .apply()
        }
}