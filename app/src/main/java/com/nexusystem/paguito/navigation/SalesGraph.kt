package com.nexusystem.paguito.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.google.gson.Gson
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.payments.SalesScreen

fun NavGraphBuilder.salesGraph(navController: NavHostController, payments: PagosViewModel, clients: DeudoresViewModel) {
    navigation(startDestination = Routes.ScreenSales.route, route = Routes.SalesGraph.route) {
        composable(Routes.ScreenSales.route) {
            SalesScreen(payments, clients,
                onNewSale = { navController.navigate(Routes.ScreenRegisterSell.route + "/") },
                onClient = { client -> navController.navigate(Routes.ScreenPerfilDeudor.route + "/" + Uri.encode(Gson().toJson(client))) },
                onAnalysis = { navController.navigate(Routes.AnalisisGraph.route) { launchSingleTop = true } })
        }
    }
}
