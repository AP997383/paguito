// Path:
// app/src/main/java/com/nexusystem/paguito/navigation/ClientesGraph.kt

package com.nexusystem.paguito.navigation

import android.net.Uri
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.google.gson.Gson
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PagostoPreviewTiket
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.deudores.AddDebtorScreen
import com.nexusystem.paguito.ui.screens.deudores.CustomerProfileScreen
import com.nexusystem.paguito.ui.screens.deudores.DebtorsScreen
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.payments.AccountClientStateScreen
import com.nexusystem.paguito.ui.screens.payments.DetalleVentaScreen
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.payments.TicketReceiptScreen
import com.nexusystem.paguito.ui.screens.perfil.PerfiViewModel
import com.nexusystem.paguito.ui.screens.perfil.ProfileScreen
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel

fun NavGraphBuilder.clientesGraph(
    navController: NavHostController,
    deudoresViewModel: DeudoresViewModel,
    pagosViewModel: PagosViewModel,
    productosViewModel: ProductosViewModel,
    perfilViewModel: PerfiViewModel
) {
    navigation(
        startDestination = Routes.ScreenDeudoresList.route,
        route = Routes.ClientesGraph.route
    ) {
        composable(Routes.ScreenDeudoresList.route) {
            DebtorsScreen(
                { data ->
                    val json = Uri.encode(
                        Gson().toJson(data)
                    )

                    navController.navigate(
                        Routes.ScreenPerfilDeudor.route +
                                "/$json"
                    )
                },
                agregarDeudor = {
                    navController.navigate(
                        Routes.ScreenAddNewDebtor.route
                    )
                },
                registrarPago = { data ->
                    val json = Uri.encode(
                        Gson().toJson(data)
                    )

                    navController.navigate(
                        Routes.ScreenRegisterPayment.route +
                                "/$json"
                    )
                },
                { data ->
                    val json = Uri.encode(
                        Gson().toJson(data)
                    )

                    navController.navigate(
                        Routes.ScreenRegisterSell.route +
                                "/$json"
                    )
                },
                {
                    navController.navigate(
                        Routes.ScreenProfile.route
                    )
                },
                deudoresViewModel
            )
        }

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
                openIdiomas = {
                    navController.navigate(
                        Routes.ScreenIdioms.route
                    )
                },
                onBack = navController::popBackStack,
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
            route = Routes.ScreenPerfilDeudor.route +
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

            CustomerProfileScreen(
                navController::popBackStack,
                data,
                deudoresViewModel,
                pagosViewModel,
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
                { payment ->
                    val paymentJson = Uri.encode(
                        Gson().toJson(payment)
                    )

                    navController.popBackStack()

                    navController.navigate(
                        Routes.ScreenDetalledeVenta.route +
                                "/$paymentJson"
                    )
                },
                { account ->
                    val accountJson = Uri.encode(
                        Gson().toJson(account)
                    )

                    navController.popBackStack()

                    navController.navigate(
                        Routes.ScreenPreviewTicketAccountState
                            .route +
                                "/$accountJson"
                    )
                }
            )
        }

        composable(
            route = Routes.ScreenDetalledeVenta.route +
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
                    PagosEntinty::class.java
                )
            } else {
                PagosEntinty(id = null)
            }

            DetalleVentaScreen(
                data,
                productosViewModel,
                pagosViewModel,
                navController::popBackStack
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
            route = Routes.ScreenPreviewTicketAccountState.route +
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
                DeudoresEntity()
            }

            AccountClientStateScreen(
                data.nombre,
                data.idRemoteDatabase,
                data.montoAcomulado.toInt(),
                data.montoActualAdeudado.toInt(),
                data.id?.toInt() ?: 0,
                onBack = navController::popBackStack,
                pagosViewModel = pagosViewModel
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