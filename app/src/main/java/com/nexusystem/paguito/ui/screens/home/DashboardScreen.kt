package com.nexusystem.paguito.ui.screens.home

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.home.components.*
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.ui.screens.productos.NativeAdBanner
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel
import com.nexusystem.paguito.utils.dialogs.PremiumLimitReachedDialog
import com.nexusystem.paguito.utils.getDaysUntilNextPayment

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    seeDeudorProfile: (DeudoresEntity) -> Unit,
    registerPayment: () -> Unit,
    registerDebtor: () -> Unit,
    registerProduct: () -> Unit,
    registerCampaing: () -> Unit,
    registerNewSell: () -> Unit,
    openWebsite: () -> Unit,
    seeAllDeudores: () -> Unit,
    seeAllPayments: () -> Unit,
    goToMyProfile: () -> Unit,
    deudoresViewModel: DeudoresViewModel,
    pagosViewModel: PagosViewModel,
    productosViewmodel: ProductosViewModel
) {
    val deudoresHistorial by deudoresViewModel.deudores.collectAsState()
    val sumary1 by deudoresViewModel.sumaryFive.collectAsState()
    val numeroProductos by productosViewmodel.numProducts.collectAsState()
    val pagosHistorial by pagosViewModel.pagosConNombre5.collectAsState()
    val pagosPormes by pagosViewModel.pagosByMonth.collectAsState()

    val profile = deudoresViewModel.profileState

    var isBalanceVisible by remember { mutableStateOf(false) }
    var isSucriptionActive by remember { mutableStateOf(false) }
    var showAlertFreeLimited by remember { mutableStateOf(false) }
    var urlPhotoProfile by remember { mutableStateOf("") }

    val deudoresOrdenados = remember(deudoresHistorial) {
        deudoresHistorial
            .filterNotNull()
            .map { deudor ->
                val daysRemaining = getDaysUntilNextPayment(
                    deudor.fechaInicialDeuda,
                    deudor.periodicidad
                )
                deudor to daysRemaining
            }
            .sortedBy { it.second }
            .map { it.first }
    }

    LaunchedEffect(Unit) {
        productosViewmodel.obtenerNumeroProductos()
        deudoresViewModel.loadUserProfile()
        deudoresViewModel.obtenerDeudores()
        deudoresViewModel.obtener5DatosCards()
        pagosViewModel.obtenerUltimos5Abonos()
        pagosViewModel.obtenerAbonosdelMes()
    }

    LaunchedEffect(profile) {
        if (profile != null) {
            Log.e("PROFILE_PHOTO", "-->$profile")
            isSucriptionActive = profile.userSuscription.isActive
            urlPhotoProfile = profile.fotoUrl
        }
    }

    if (showAlertFreeLimited) {
        PremiumLimitReachedDialog(
            { showAlertFreeLimited = false },
            { showAlertFreeLimited = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        TopBar(
            imageUser = urlPhotoProfile,
            goToMyProfile = goToMyProfile
        )

        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            BalanceCarousel(
                pagosList = pagosPormes,
                sumary1 = sumary1 ?: DeudoresSummary(0, 0f),
                showBalance = isBalanceVisible,
                onToggleBalance = { isBalanceVisible = !isBalanceVisible }
            )

            Spacer(modifier = Modifier.height(20.dp))

            ActionButtonsRow(
                openWebsite = openWebsite,
                registerPayment = registerPayment,
                registerProduct = registerProduct,
                registerDebtor = registerDebtor,
                registerCampaing = registerCampaing,
                registerNewSell = registerNewSell,
                sumary1 = sumary1,
                isSucriptionActive = isSucriptionActive,
                showDialogLimitFree = { showAlertFreeLimited = true },
                numeroProductos = numeroProductos
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (!isSucriptionActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    NativeAdBanner(
                        adUnitId = if (BuildConfig.DEBUG) {
                            "ca-app-pub-3940256099942544/2247696110"
                        } else {
                            "ca-app-pub-1155673544372892/6066860296"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (deudoresOrdenados.isNotEmpty()) {
                UpcomingDeadlinesSection(
                    deudores = deudoresOrdenados,
                    seeAllDeudores = seeAllDeudores,
                    openDetailDeudor = seeDeudorProfile
                )
            } else {
                PaymentHistoryEmptyState(
                    "Cuando registres tu primer deudor, podrás ver los próximos a vencer en esta sección"
                )
            }

            if (pagosHistorial.isNotEmpty()) {
                RecentPaymentsSection(
                    pagos = pagosHistorial,
                    seeAllPayments = seeAllPayments
                )
            } else {
                PaymentHistoryVerticalEmptyState(
                    "Cuando registres tu primer pago, podrás consultar tus últimos pagos o ventas"
                )
            }

            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}