package com.nexusystem.paguito

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.nexusystem.paguito.ui.components.navigation.RoundedBottomBar
import com.nexusystem.paguito.ui.components.navigation.Routes
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.login.AuthViewModel
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.perfil.PerfiViewModel
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel
import com.nexusystem.paguito.ui.screens.registro.RegisterViewModel
import com.nexusystem.paguito.ui.theme.PaguitoTheme
import com.nexusystem.paguito.utils.OnboardingStore
import com.nexusystem.paguito.utils.PaguitoStore
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Paguito)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean("dark_mode", false)
        //MobileAds.initialize(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "DEFAULT_CHANNEL",
                "Notificaciones de Medi",
                NotificationManager.IMPORTANCE_HIGH
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        setContent {
            val context = LocalContext.current
            PaguitoTheme(
                darkTheme = isDark,
                dynamicColor = false
            ) {
                val navController = rememberNavController()
                var startDestination by remember { mutableStateOf<String?>(null) }
                var showDialog by remember { mutableStateOf(false) }
                val deudoresViewModel: DeudoresViewModel by viewModels()
                val productosViewModel: ProductosViewModel by viewModels()
                val authViewModel:AuthViewModel by viewModels()
                val registerwModel: RegisterViewModel by viewModels()
                val pagosViewModel: PagosViewModel by viewModels()
                val perfilViewModel: PerfiViewModel by viewModels()
                val currentDestination =
                    navController.currentBackStackEntryAsState().value?.destination?.route
                        ?: Routes.ScreenHome.route
                LaunchedEffect(Unit) {
                    val seen = OnboardingStore.hasSeenOnboarding(this@MainActivity)
                    startDestination = if (seen) {
                        val islogged = PaguitoStore.isLoged(this@MainActivity)
                        if(islogged) {
                            Routes.ScreenHome.route
                        }else{
                            Routes.ScreenLogin.route
                        }
                    } else {
                        Routes.ScreenOnboarding
                            .route
                    }


                    FirebaseMessaging.getInstance().token
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                Log.e("FCM_TOKEN", "Error obteniendo token", task.exception)
                                return@addOnCompleteListener
                            }

                            val token = task.result
                            val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                            val lang = prefs.getString("app_language", "es") ?: "es"
                            if (!prefs.getString("MyToken", "").equals(token)) {
                                // reminderViewModel.saveMyToken(token, lang)
                                prefs.edit().putString("MyToken", token).apply()
                            }

                            Log.d("FCM_TOKEN", "Token: $token")

                            // 👇 opcional: guardarlo o enviarlo a tu backend
                            // saveTokenToServer(token)
                        }



                }


                if (startDestination == null) {
                    return@PaguitoTheme
                }

                Scaffold(
                    bottomBar = {
                        val route = navController.currentBackStackEntryAsState()
                            .value?.destination?.route

                        if (
                            route != null &&
                            route != Routes.ScreenOnboarding.route &&

                            // Medicinas (flujo HomeGraph)
                            route != Routes.ScreenLogin.route &&
                            route != Routes.ScreenRegister.route &&
                            route != Routes.ScreenOtp.route+ "/{params}" &&
                            route != Routes.ScreenDetalledeVenta.route+ "/{params}" &&
                            route != Routes.ScreenPreviewTicket.route+ "/{params}"&&
                            route != Routes.ScreenRecoveryPassword.route &&
                            route != Routes.ScreenNuevoProducto.route+ "/{params}" &&
                            route != Routes.ScreenNuevoProducto.route &&
                            route != Routes.ScreenAddNewDebtor.route&&
                            route != Routes.ScreenPerfilDeudor.route+ "/{params}"&&
                            route != Routes.ScreenPerfilDeudor.route &&
                            route != Routes.ScreenPerfilEditar.route &&
                            route != Routes.ScreenDownloadInfo.route + "/{params}" &&
                            route != Routes.ScreenRegisterPayment.route &&
                            route != Routes.ScreenRegisterPayment.route+ "/{params}" &&
                            route != Routes.ScreenIdioms.route + "/{from}"&&
                            route != Routes.ScreenIdioms.route&&
                            route != Routes.ScreenRegisterSell.route + "/{params}"&&
                            route != Routes.ScreenOtpRecovery.route + "/{params}"&&
                            route != Routes.ScreenChangePassword.route + "/{params}"&&
                            route != Routes.ScreenChangePasswordProfile.route + "/{params}"&&
                            route != Routes.ScreenRegisterSell.route + "/"&&
                            route != Routes.ScreenRegisterSell.route&&
                            route != Routes.ScreenViewAllDeudores.route&&
                            route != Routes.ScreenViewAllPayments.route

                        ) {
                            RoundedBottomBar(navController) {
                                navController.navigate(it)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)

                ) { innerPadding ->

                    MediNavHost(
                        navController = navController,
                        deudoresViewModel=deudoresViewModel,
                        pagosViewModel,
                        productosViewModel,
                        perfilViewModel,
                        authViewModel =authViewModel,
                        registerwModel=registerwModel,
                        startDestination = startDestination!!,
                        modifier = Modifier
                            .padding(
                                top = innerPadding.calculateTopPadding(),
                                bottom = 0.dp,
                                start = 0.dp,
                                end = 0.dp
                            )
                    )
                }
            }
        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
}

