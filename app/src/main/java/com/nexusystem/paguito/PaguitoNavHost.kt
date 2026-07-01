// Path: com/beastspinning/medi/MediNavHost.kt
package com.nexusystem.paguito

import HistorialPagosScreen
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.gson.Gson
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PagostoPreviewTiket
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.domain.data.auth.UserDataModelAuth
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.SelectLanguageScreen
import com.nexusystem.paguito.ui.screens.analisis.AnalisisScreen
import com.nexusystem.paguito.ui.screens.analisis.AnalisisViewModel
import com.nexusystem.paguito.ui.screens.deudores.AddDebtorScreen
import com.nexusystem.paguito.ui.screens.deudores.CustomerProfileScreen
import com.nexusystem.paguito.ui.screens.deudores.DebtorsScreen
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.home.DashboardScreen
import com.nexusystem.paguito.ui.screens.home.ListaClientesDeudaScreen
import com.nexusystem.paguito.ui.screens.login.AuthViewModel
import com.nexusystem.paguito.ui.screens.login.ChangePasswordScreen
import com.nexusystem.paguito.ui.screens.login.ForgotPasswordScreen
import com.nexusystem.paguito.ui.screens.login.LoginScreen
import com.nexusystem.paguito.ui.screens.login.OtpRecoveryScreen
import com.nexusystem.paguito.ui.screens.login.ProcessingScreen
import com.nexusystem.paguito.ui.screens.onboarding.OnboardingScreen
import com.nexusystem.paguito.ui.screens.payments.AccountClientStateScreen
import com.nexusystem.paguito.ui.screens.payments.DetalleVentaScreen
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.payments.RegisterPaymentScreen
import com.nexusystem.paguito.ui.screens.payments.RegisterSellScreen
import com.nexusystem.paguito.ui.screens.payments.TicketReceiptScreen
import com.nexusystem.paguito.ui.screens.perfil.ChangePasswordProfileScreen
import com.nexusystem.paguito.ui.screens.perfil.EditProfileScreen
import com.nexusystem.paguito.ui.screens.perfil.PerfiViewModel
import com.nexusystem.paguito.ui.screens.perfil.ProfileScreen
import com.nexusystem.paguito.ui.screens.productos.AddProductScreen
import com.nexusystem.paguito.ui.screens.productos.ProductListScreen
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel
import com.nexusystem.paguito.ui.screens.registro.OtpVerificationScreen
import com.nexusystem.paguito.ui.screens.registro.RegisterScreen
import com.nexusystem.paguito.ui.screens.registro.RegisterViewModel
import com.nexusystem.paguito.ui.screens.website.WebsiteScreen
import com.nexusystem.paguito.ui.screens.website.setup.WebsiteSetupScreen
import com.nexusystem.paguito.utils.OnboardingStore
import com.nexusystem.paguito.utils.PaguitoStore


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
    var isFlashOn by remember { mutableStateOf(false) }
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
        modifier = modifier.padding(0.dp)
    ) {
        composable(Routes.ScreenOnboarding.route) {
            OnboardingScreen(
                onFinish = {
                    scope.launch { OnboardingStore.setOnboardingSeen(context) }
                    navController.navigate(Routes.LoginGraph.route) {
                        popUpTo(Routes.ScreenOnboarding.route) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        navigation(
            startDestination = Routes.ScreenHome.route,
            route = Routes.HomeGraph.route
        ) {
            composable(Routes.ScreenHome.route) {
                DashboardScreen(
                    seeDeudorProfile = { data ->
                        val json = Uri.encode(Gson().toJson(data))
                        navController.navigate(Routes.ScreenPerfilDeudor.route + "/$json")
                    },
                    registerPayment = {
                        navController.navigate(Routes.ScreenRegisterPayment.route + "/")
                    },
                    registerDebtor = {
                        navController.navigate(Routes.ScreenAddNewDebtor.route)
                    },
                    registerProduct = {
                        navController.navigate(Routes.ScreenNuevoProducto.route + "/")
                    },
                    registerCampaing = {},
                    registerNewSell = {
                        navController.navigate(Routes.ScreenRegisterSell.route + "/")
                    },
                    openWebsite = {
                        navController.navigate(Routes.ScreenWebsite.route)
                    },
                    seeAllDeudores = {
                        navController.navigate(Routes.ScreenViewAllDeudores.route)
                    },
                    seeAllPayments = {
                        navController.navigate(Routes.ScreenViewAllPayments.route)
                    },
                    goToMyProfile = {
                        navController.navigate(Routes.ScreenProfile.route)
                    },
                    deudoresViewModel = deudoresViewModel,
                    pagosViewModel = pagosViewModel,
                    productosViewmodel = productosViewModel
                )
            }

            composable(Routes.ScreenWebsite.route) {
                WebsiteScreen(
                    onBack = {
                        navController.popBackStack()
                    },
                    onCreateWebsite = {
                        navController.navigate(
                            Routes.ScreenWebsiteSetup.createEmptyRoute()
                        )
                    },
                    onEditWebsite = { businessName, subdomain, whatsapp ->
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
                val products by productosViewModel.produtosList.collectAsState()

                WebsiteSetupScreen(
                    onBack = { navController.popBackStack() },
                    initialBusinessName = backStack.arguments?.getString("businessName").orEmpty(),
                    initialSubdomain = backStack.arguments?.getString("subdomain").orEmpty(),
                    initialWhatsapp = backStack.arguments?.getString("whatsapp").orEmpty(),
                    isEditingWebsite = backStack.arguments?.getString("subdomain")?.isNotEmpty() == true,
                    products = products.filterNotNull())
            }

            composable(Routes.ScreenProfile.route) {
                ProfileScreen( {
                    navController.navigate(Routes.ScreenLogin.route) {
                        // 1. Buscamos el ID del grafo o la ruta de inicio y lo removemos
                        popUpTo(Routes.ProfileGraph.route) {
                            inclusive = true // Esto elimina también el grafo de perfil
                        }
                        // 2. Evitamos que se creen múltiples copias del Login
                        launchSingleTop = true
                    }
                },{
                    navController.navigate(Routes.ScreenPerfilEditar.route)
                },{
                    navController.popBackStack()
                }, openIdiomas = {
                    navController.navigate(Routes.ScreenIdioms.route)
                } , openChangePassword = {
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenChangePasswordProfile.route+ "/$json")
                },viewModel = perfilViewModel)
            }

            composable(Routes.ScreenRegisterPayment.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {

                    params ->
                val json = params.arguments?.getString("params") ?: ""
                val data = if (json.isNotEmpty()) {
                    Gson().fromJson(json, DeudoresEntity::class.java)
                } else {
                    DeudoresEntity(id=null)
                }

                RegisterPaymentScreen( data,{ navController.popBackStack()},{
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.popBackStack()
                    navController.navigate(Routes.ScreenPreviewTicket.route+ "/$json")
                },deudoresViewModel,pagosViewModel)
            }

            composable(Routes.ScreenRegisterSell.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                val data = if (json.isNotEmpty()) {
                    Gson().fromJson(json, DeudoresEntity::class.java)
                } else {
                    DeudoresEntity(id=null)
                }
                RegisterSellScreen( data,{ navController.popBackStack()},{
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.popBackStack()
                    navController.navigate(Routes.ScreenPreviewTicket.route+ "/$json")
                },deudoresViewModel,pagosViewModel,productosViewModel)
            }

            composable(Routes.ScreenPreviewTicket.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                val data = if (json.isNotEmpty()) {
                    Gson().fromJson(json, PagostoPreviewTiket::class.java)
                } else {
                    PagostoPreviewTiket()
                }
                TicketReceiptScreen(nombrenegocio = data.nameBussines, nombreCliente = data.nameClient, correonegocio = data.correoAndPhone, total =if(data.isIngreso) ( data.saldoAntesDeAbono - data.montoAbonado).toString() else  ( data.saldoAntesDeAbono + data.montoAbonado).toString() ,
                    abonado = data.montoAbonado.toString(), subtotal = data.saldoAntesDeAbono.toString(), items = arrayListOf(data), isIngreso = data.isIngreso, onBack = {
                        navController.popBackStack()
                    }, pagosViewModel = pagosViewModel)
              /*  TicketShareScreen({
                    navController.popBackStack()
                },data )*/
            }

            composable(Routes.ScreenViewAllDeudores.route) {
                ListaClientesDeudaScreen(deudoresViewModel,{
                    navController.popBackStack()
                } )
            }
            composable(Routes.ScreenViewAllPayments.route) {
                HistorialPagosScreen({
                    navController.popBackStack()
                },pagosViewModel )
            }

            composable(Routes.ScreenNuevoProducto.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                val data = if (json.isNotEmpty()) {
                    Gson().fromJson(json, PorductosEntity::class.java)
                } else {
                    PorductosEntity(
                        id = null,
                        idRemoteDatabase = ""
                    )
                }
                AddProductScreen( { navController.popBackStack()},productosViewModel,data)
            }

            composable(Routes.ScreenAddNewDebtor.route) {
                AddDebtorScreen(deudoresViewModel,{
                    navController.popBackStack()
                } ,productosViewModel)
            }
        }


        navigation(
            startDestination = Routes.ScreenAnalisis.route,
            route = Routes.AnalisisGraph.route
        ) {
            composable(Routes.ScreenAnalisis.route) {
                AnalisisScreen(
                    {   navController.navigate(Routes.ScreenProfile.route)},
                    deudoresViewModel,pagosViewModel,productosViewModel,analisisViewModel)
            }
        }


        navigation(
            startDestination = Routes.ScreenProfile.route,
            route = Routes.ProfileGraph.route
        ) {
            composable(Routes.ScreenProfile.route) {
                ProfileScreen( {
                    navController.navigate(Routes.ScreenLogin.route) {
                        // 1. Buscamos el ID del grafo o la ruta de inicio y lo removemos
                        popUpTo(Routes.ProfileGraph.route) {
                            inclusive = true // Esto elimina también el grafo de perfil
                        }
                        // 2. Evitamos que se creen múltiples copias del Login
                        launchSingleTop = true
                    }
                },{
                    navController.navigate(Routes.ScreenPerfilEditar.route)
                }, onBack = {

                        navController.popBackStack()
                    }
                , openIdiomas = {
                    navController.navigate(Routes.ScreenIdioms.route)
                } , openChangePassword = {
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenChangePasswordProfile.route+ "/$json")
                },viewModel = perfilViewModel)
            }

            composable(Routes.ScreenChangePasswordProfile.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                ChangePasswordProfileScreen(json,authViewModel,{
                    navController.navigate(Routes.ScreenLogin.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },{
                    navController.popBackStack()
                })
            }

            composable(Routes.ScreenPerfilEditar.route) {
                EditProfileScreen( {
                     navController.popBackStack()
                },perfilViewModel)
            }

            composable(Routes.ScreenIdioms.route) {
                SelectLanguageScreen(
                    isOnboarding = false,
                    onContinueClick = {
                        navController.popBackStack()
                    },
                    onBackClick = { navController.popBackStack()}
                )
            }




        }

        navigation(
            startDestination = Routes.ScreenListaProductos.route,
            route = Routes.ProductosGraph.route
        ) {
            composable(Routes.ScreenNuevoProducto.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                val data = if (json.isNotEmpty()) {
                    Gson().fromJson(json, PorductosEntity::class.java)
                } else {
                    PorductosEntity(
                        id = null,
                        idRemoteDatabase = ""
                    )
                }
                AddProductScreen( { navController.popBackStack()},productosViewModel,data)
            }

            composable(Routes.ScreenListaProductos.route) {
                ProductListScreen( { navController.popBackStack()},{
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenNuevoProducto.route+ "/$json")
                },{
                    navController.navigate(Routes.ScreenNuevoProducto.route+"/")
                },{
                    navController.navigate(Routes.ScreenProfile.route)
                },productosViewModel)
            }
            composable(Routes.ScreenProfile.route) {
                ProfileScreen( {
                    navController.navigate(Routes.ScreenLogin.route) {
                        // 1. Buscamos el ID del grafo o la ruta de inicio y lo removemos
                        popUpTo(Routes.ProfileGraph.route) {
                            inclusive = true // Esto elimina también el grafo de perfil
                        }
                        // 2. Evitamos que se creen múltiples copias del Login
                        launchSingleTop = true
                    }
                },{
                    navController.navigate(Routes.ScreenPerfilEditar.route)
                }, {
                    navController.popBackStack()
                },openIdiomas = {
                    navController.navigate(Routes.ScreenIdioms.route)
                } , openChangePassword = {
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenChangePasswordProfile.route+ "/$json")
                },viewModel = perfilViewModel)
            }

        }


        navigation(
            startDestination = Routes.ScreenLogin.route,
            route = Routes.LoginGraph.route
        ) {
            composable(Routes.ScreenLogin.route) {
                LoginScreen( { email->
                    if(email.userSuscription.isActive) {
                        navController.navigate(Routes.ScreenDownloadInfo.route + "/${email.email}")
                    }else{
                        scope.launch {
                            PaguitoStore.setLoged(context)
                            PaguitoStore.setInvitedNot(context)
                        }
                        navController.navigate(Routes.ScreenHome.route)
                    }

                },{
                    navController.navigate(Routes.ScreenRegister.route)
                },{
                    scope.launch {
                        PaguitoStore.setLoged(context)
                        PaguitoStore.setInvited(context)
                    }
                    navController.navigate(Routes.ScreenHome.route)
                },{
                    navController.navigate(Routes.ScreenRecoveryPassword.route)
                },authViewModel)
            }
            composable(Routes.ScreenOtp.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                val data = if (json.isNotEmpty()) {
                    Gson().fromJson(json, UserDataModelAuth::class.java)
                } else {
                    UserDataModelAuth()
                }
                OtpVerificationScreen(data,{
                    navController.navigate(Routes.ScreenLogin.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },{},{
                    navController.popBackStack()
                },authViewModel)
            }

            composable(Routes.ScreenDownloadInfo.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {  data ->
                val email = data.arguments?.getString("params") ?: ""
                ProcessingScreen( email = email,authViewModel,{
                    scope.launch {
                        PaguitoStore.setLoged(context)
                        PaguitoStore.setInvitedNot(context)
                    }
                    navController.navigate(Routes.ScreenHome.route)
                },{
                    scope.launch {
                        PaguitoStore.setLoged(context)
                        PaguitoStore.setInvitedNot(context)
                    }
                    navController.navigate(Routes.ScreenHome.route)
                })
            }

            composable(Routes.ScreenOtpRecovery.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                OtpRecoveryScreen(json,{
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenChangePassword.route+ "/$json")
                },{},{
                    navController.popBackStack()
                },authViewModel)
            }

            composable(Routes.ScreenRecoveryPassword.route) {
                ForgotPasswordScreen( { navController.popBackStack()},{     data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenOtpRecovery.route+ "/$json")
                },authViewModel)
            }

            composable(Routes.ScreenChangePassword.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                ChangePasswordScreen(json,authViewModel,{
                    navController.navigate(Routes.ScreenLogin.route) {
                        // 'popUpTo' le dice a Compose que elimine pantallas del stack
                        // Usamos la ruta de inicio o el ID del grafo para limpiar TODO
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                        // Evita que se creen múltiples copias de la misma pantalla si se presiona rápido
                        launchSingleTop = true
                    }
                })
            }






            composable(Routes.ScreenRegister.route) {
                RegisterScreen( { navController.popBackStack()},{
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenOtp.route+ "/$json")
                },{
                    navController.navigate(Routes.ScreenLogin.route)
                },registerwModel)
            }


        }

        navigation(
            startDestination = Routes.ScreenDeudoresList.route,
            route = Routes.ClientesGraph.route
        ) {
            composable(Routes.ScreenDeudoresList.route) {
                DebtorsScreen( { data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenPerfilDeudor.route+ "/$json")
                }, agregarDeudor = {
                    navController.navigate(Routes.ScreenAddNewDebtor.route )
                },registrarPago={
                    data->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenRegisterPayment.route+ "/$json")
                },{
                        data->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenRegisterSell.route+ "/$json")
                },{
                    navController.navigate(Routes.ScreenProfile.route)
                },deudoresViewModel)
            }

            composable(Routes.ScreenProfile.route) {
                ProfileScreen( {
                    navController.navigate(Routes.ScreenLogin.route) {
                        // 1. Buscamos el ID del grafo o la ruta de inicio y lo removemos
                        popUpTo(Routes.ProfileGraph.route) {
                            inclusive = true // Esto elimina también el grafo de perfil
                        }
                        // 2. Evitamos que se creen múltiples copias del Login
                        launchSingleTop = true
                    }
                },{
                    navController.navigate(Routes.ScreenPerfilEditar.route)
                }, openIdiomas = {
                    navController.navigate(Routes.ScreenIdioms.route)
                }, onBack = {
                    navController.popBackStack()
                }, openChangePassword = {
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.navigate(Routes.ScreenChangePasswordProfile.route+ "/$json")
                },viewModel = perfilViewModel)
            }

            composable(Routes.ScreenPerfilDeudor.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                val data = if (json.isNotEmpty()) {
                    Gson().fromJson(json, DeudoresEntity::class.java)
                } else {
                    DeudoresEntity(id = null)
                }
                CustomerProfileScreen({ navController.popBackStack()},data!!,deudoresViewModel,pagosViewModel ,{
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.popBackStack()
                    navController.navigate(Routes.ScreenPreviewTicket.route+ "/$json")
                },{
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.popBackStack()
                    navController.navigate(Routes.ScreenDetalledeVenta.route+ "/$json")
                },{
                        data ->
                    val json = Uri.encode(Gson().toJson(data))
                    navController.popBackStack()
                    navController.navigate(Routes.ScreenPreviewTicketAccountState.route+ "/$json")
                })
            }

            composable(Routes.ScreenDetalledeVenta.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                val data = if (json.isNotEmpty()) {
                    Gson().fromJson(json, PagosEntinty::class.java)
                } else {
                    PagosEntinty(id = null)
                }
                DetalleVentaScreen(data,productosViewModel,pagosViewModel,{
                    navController.popBackStack()
                })
            }



            composable(Routes.ScreenPreviewTicket.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                val data = if (json.isNotEmpty()) {
                    Gson().fromJson(json, PagostoPreviewTiket::class.java)
                } else {
                    PagostoPreviewTiket()
                }
                TicketReceiptScreen(nombrenegocio = data.nameBussines, nombreCliente = data.nameClient, correonegocio = data.correoAndPhone, total =if(data.isIngreso) ( data.saldoAntesDeAbono - data.montoAbonado).toString() else  ( data.saldoAntesDeAbono + data.montoAbonado).toString() ,
                    abonado = data.montoAbonado.toString(), subtotal = data.saldoAntesDeAbono.toString(), items = arrayListOf(data), isIngreso = data.isIngreso, onBack = {
                        navController.popBackStack()
                    }, pagosViewModel = pagosViewModel)
               /* TicketShareScreen({
                    navController.popBackStack()
                },data )*/
            }

            composable(Routes.ScreenPreviewTicketAccountState.route+ "/{params}",
                arguments = listOf(
                    navArgument("params") {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )) {
                    params ->
                val json = params.arguments?.getString("params") ?: ""
                val data = if (json.isNotEmpty()) {
                    Gson().fromJson(json, DeudoresEntity::class.java)
                } else {
                    DeudoresEntity()
                }
                AccountClientStateScreen(data.nombre,data.idRemoteDatabase,data.montoAcomulado.toInt(),data.montoActualAdeudado.toInt(),data.id!!.toInt()?:0, onBack = {
                        navController.popBackStack()
                    }, pagosViewModel = pagosViewModel)
                /* TicketShareScreen({
                     navController.popBackStack()
                 },data )*/
            }



            composable(Routes.ScreenAddNewDebtor.route) {
                AddDebtorScreen(deudoresViewModel,{
                    navController.popBackStack()
                },productosViewModel )
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