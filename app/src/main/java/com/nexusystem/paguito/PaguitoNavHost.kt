// Path: com/beastspinning/medi/MediNavHost.kt
package com.nexus.medi

import android.app.Activity
import android.content.Context
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.google.android.play.core.review.ReviewManagerFactory
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.deudores.DebtorsScreen
import com.nexusystem.paguito.ui.screens.home.DashboardScreen
import com.nexusystem.paguito.ui.screens.onboarding.OnboardingScreen
import com.nexusystem.paguito.ui.screens.perfil.ProfileScreen
import com.nexusystem.paguito.utils.OnboardingStore


import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MediNavHost(
    navController: NavHostController,
    startDestination: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isFlashOn by remember { mutableStateOf(false) }
    val resolvedStartDestination = when (startDestination) {
        Routes.ScreenHome.route -> Routes.HomeGraph.route
        Routes.ScreenDeudoresList.route -> Routes.ClientesGraph.route
        Routes.ScreenProfile.route -> Routes.ProfileGraph.route
        else -> startDestination
    }

    NavHost(
        navController = navController,
        startDestination = resolvedStartDestination,
        modifier = modifier.padding(0.dp)
    ) {
        composable(Routes.ScreenOnboarding.route) {
            OnboardingScreen(
                modifier,
                onFinish = {
                    scope.launch { OnboardingStore.setOnboardingSeen(context) }
                    navController.navigate(Routes.HomeGraph.route) {
                        popUpTo(Routes.ScreenOnboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onLoginClick = {}
            )
        }
        navigation(
            startDestination = Routes.ScreenHome.route,
            route = Routes.HomeGraph.route
        ) {
            composable(Routes.ScreenHome.route) {
                DashboardScreen( )
            }
        }

        navigation(
            startDestination = Routes.ScreenProfile.route,
            route = Routes.ProfileGraph.route
        ) {
            composable(Routes.ScreenProfile.route) {
                ProfileScreen( )
            }
        }

        navigation(
            startDestination = Routes.ScreenDeudoresList.route,
            route = Routes.ClientesGraph.route
        ) {
            composable(Routes.ScreenDeudoresList.route) {
                DebtorsScreen( )
            }
        }


    }
}

fun showInAppReview(context: Context) {
    val activity = context as? Activity ?: return
    val reviewManager = ReviewManagerFactory.create(activity)
    val request = reviewManager.requestReviewFlow()
    request.addOnCompleteListener { task ->
        Log.e("ASK_FOR_REVIEW","TRUE")
        if (task.isSuccessful) {
            Log.e("ASK_FOR_REVIEW","task.isSuccessful")
            val reviewInfo = task.result
            reviewManager.launchReviewFlow(activity, reviewInfo)
            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            prefs.edit().putBoolean("appReviwed", true)
        }else{
            Log.e("ASK_FOR_REVIEW","task.isSuccessful else")
        }

    }
}