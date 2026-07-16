// Path:
// app/src/main/java/com/nexusystem/paguito/navigation/HomeGraph.kt

package com.nexusystem.paguito.navigation

import HistorialPagosScreen
import android.net.Uri
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagostoPreviewTiket
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.artifacts.NexusPayLauncher
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.deudores.AddDebtorScreen
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.home.DashboardScreen
import com.nexusystem.paguito.ui.screens.home.ListaClientesDeudaScreen
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.payments.RegisterPaymentScreen
import com.nexusystem.paguito.ui.screens.payments.RegisterSellScreen
import com.nexusystem.paguito.ui.screens.payments.TicketReceiptScreen
import com.nexusystem.paguito.ui.screens.productos.AddProductScreen
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel
import com.nexusystem.paguito.ui.screens.website.WebsiteScreen
import com.nexusystem.paguito.ui.screens.website.setup.WebsiteSetupScreen

fun NavGraphBuilder.homeGraph(
    navController: NavHostController,
    deudoresViewModel: DeudoresViewModel,
    pagosViewModel: PagosViewModel,
    productosViewModel: ProductosViewModel
) {
    navigation(
        startDestination = Routes.ScreenHome.route,
        route = Routes.HomeGraph.route
    ) {
        composable(Routes.ScreenHome.route) {
            val context = LocalContext.current

            DashboardScreen(
                seeDeudorProfile = { data ->
                    val json = Uri.encode(
                        Gson().toJson(data)
                    )

                    navController.navigate(
                        Routes.ScreenPerfilDeudor.route +
                                "/$json"
                    )
                },
                registerPayment = {
                    navController.navigate(
                        Routes.ScreenRegisterPayment.route + "/"
                    )
                },
                registerDebtor = {
                    navController.navigate(
                        Routes.ScreenAddNewDebtor.route
                    )
                },
                registerProduct = {
                    navController.navigate(
                        Routes.ScreenNuevoProducto.route + "/"
                    )
                },
                registerCampaing = {},
                registerNewSell = {
                    navController.navigate(
                        Routes.ScreenRegisterSell.route + "/"
                    )
                },
                openWebsite = {
                    navController.navigate(
                        Routes.ScreenWebsite.route
                    )
                },
                seeAllDeudores = {
                    navController.navigate(
                        Routes.ScreenViewAllDeudores.route
                    )
                },
                seeAllPayments = {
                    navController.navigate(
                        Routes.ScreenViewAllPayments.route
                    )
                },
                goToMyProfile = {
                    navController.navigate(
                        Routes.ScreenProfile.route
                    )
                },
                deudoresViewModel = deudoresViewModel,
                pagosViewModel = pagosViewModel,
                productosViewmodel = productosViewModel,
                openNexusPay = {
                    val profile = deudoresViewModel.profileState

                    NexusPayLauncher.open(
                        context = context,
                        email = profile
                            ?.email
                            ?.trim()
                            .orEmpty(),
                        fcmToken = profile
                            ?.token
                            ?.trim()
                            .orEmpty(),
                        isPremium = profile
                            ?.userSuscription
                            ?.isActive
                            ?: false
                    )
                }
            )
        }

        composable(Routes.ScreenWebsite.route) {
            WebsiteScreen(
                onBack = navController::popBackStack,
                onCreateWebsite = {
                    navController.navigate(
                        Routes.ScreenWebsiteSetup
                            .createEmptyRoute()
                    )
                },
                onEditWebsite = {
                        businessName,
                        subdomain,
                        whatsapp ->

                    navController.navigate(
                        Routes.ScreenWebsiteSetup.createRoute(
                            businessName = businessName,
                            subdomain = subdomain,
                            whatsapp = whatsapp
                        )
                    )
                }
            )
        }

        composable(
            route = Routes.ScreenWebsiteSetup.route,
            arguments = listOf(
                navArgument("businessName") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("subdomain") {
                    type = NavType.StringType
                    defaultValue = ""
                },
                navArgument("whatsapp") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStack ->
            val products by productosViewModel
                .produtosList
                .collectAsState()

            WebsiteSetupScreen(
                onBack = navController::popBackStack,
                initialBusinessName = backStack
                    .arguments
                    ?.getString("businessName")
                    .orEmpty(),
                initialSubdomain = backStack
                    .arguments
                    ?.getString("subdomain")
                    .orEmpty(),
                initialWhatsapp = backStack
                    .arguments
                    ?.getString("whatsapp")
                    .orEmpty(),
                isEditingWebsite = backStack
                    .arguments
                    ?.getString("subdomain")
                    ?.isNotEmpty() == true,
                products = products.filterNotNull()
            )
        }

        composable(
            route = Routes.ScreenRegisterPayment.route +
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
                    DeudoresEntity::class.java
                )
            } else {
                DeudoresEntity(id = null)
            }

            RegisterPaymentScreen(
                data,
                navController::popBackStack,
                { ticket ->
                    val ticketJson = Uri.encode(
                        Gson().toJson(ticket)
                    )

                    navController.popBackStack()

                    navController.navigate(
                        Routes.ScreenPreviewTicket.route +
                                "/$ticketJson"
                    )
                },
                deudoresViewModel,
                pagosViewModel
            )
        }

        composable(
            route = Routes.ScreenRegisterSell.route +
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
                    DeudoresEntity::class.java
                )
            } else {
                DeudoresEntity(id = null)
            }

            RegisterSellScreen(
                data,
                navController::popBackStack,
                { ticket ->
                    val ticketJson = Uri.encode(
                        Gson().toJson(ticket)
                    )

                    navController.popBackStack()

                    navController.navigate(
                        Routes.ScreenPreviewTicket.route +
                                "/$ticketJson"
                    )
                },
                deudoresViewModel,
                pagosViewModel,
                productosViewModel
            )
        }

        composable(
            route = Routes.ScreenPreviewTicket.route +
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
                    PagostoPreviewTiket::class.java
                )
            } else {
                PagostoPreviewTiket()
            }

            TicketReceiptScreen(
                nombrenegocio = data.nameBussines,
                nombreCliente = data.nameClient,
                correonegocio = data.correoAndPhone,
                total = if (data.isIngreso) {
                    (
                            data.saldoAntesDeAbono -
                                    data.montoAbonado
                            ).toString()
                } else {
                    (
                            data.saldoAntesDeAbono +
                                    data.montoAbonado
                            ).toString()
                },
                abonado = data.montoAbonado.toString(),
                subtotal = data.saldoAntesDeAbono.toString(),
                items = arrayListOf(data),
                isIngreso = data.isIngreso,
                onBack = navController::popBackStack,
                pagosViewModel = pagosViewModel
            )
        }

        composable(
            Routes.ScreenViewAllDeudores.route
        ) {
            ListaClientesDeudaScreen(
                deudoresViewModel,
                navController::popBackStack
            )
        }

        composable(
            Routes.ScreenViewAllPayments.route
        ) {
            HistorialPagosScreen(
                navController::popBackStack,
                pagosViewModel
            )
        }

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
            Routes.ScreenAddNewDebtor.route
        ) {
            AddDebtorScreen(
                deudoresViewModel,
                navController::popBackStack,
                productosViewModel
            )
        }
    }
}