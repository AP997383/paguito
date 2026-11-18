package com.nexusystem.paguito.ui.screens.deudores

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.ui.screens.deudores.components.ClientDirectoryContent
import com.nexusystem.paguito.ui.screens.productos.NativeAdBanner
import com.nexusystem.paguito.utils.dialogs.EliminarClienteDialog
import com.nexusystem.paguito.utils.dialogs.PremiumLimitReachedDialog

// Compatibility aliases shared by the existing feature screens.
val BluePrimary2 = Color(0xFF3B82F6)
val TextDark2: Color @Composable get() = MaterialTheme.colorScheme.onSurface
val TextGray1: Color @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
val BorderLight: Color @Composable get() = MaterialTheme.colorScheme.outlineVariant
val RedUrgent = Color(0xFFEF4444)
val RedBackground = Color(0xFFFEE2E2)
val GrayBackground: Color @Composable get() = MaterialTheme.colorScheme.surfaceVariant

@Composable
fun DebtorsScreen(
    verPerfilDeudor: (DeudoresEntity) -> Unit,
    agregarDeudor: () -> Unit,
    registrarPago: (DeudoresEntity) -> Unit,
    registrarVenta: (DeudoresEntity) -> Unit,
    goToMyProfile: () -> Unit,
    viewmodel: DeudoresViewModel
) {
    val rawClients by viewmodel.deudores.collectAsState()
    val clients = rawClients.filterNotNull()
    val profile = viewmodel.profileState
    val premium = profile?.userSuscription?.isActive == true
    var pendingDelete by remember { mutableStateOf<DeudoresEntity?>(null) }
    var pendingEdit by remember { mutableStateOf<DeudoresEntity?>(null) }
    var showLimit by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewmodel.loadUserProfile(); viewmodel.obtenerDeudores() }

    pendingDelete?.let { client ->
        EliminarClienteDialog(client.nombre, { pendingDelete = null }, {
            viewmodel.eliminarDeudorYpagos(client.idRemoteDatabase)
            pendingDelete = null
        })
    }
    pendingEdit?.let { client ->
        EditContactDialog(client, { pendingEdit = null }) { phone, email, name, paymentType ->
            viewmodel.updateDeudor(client.copy(telefono = phone, correo = email, nombre = name, periodicidad = paymentType))
            pendingEdit = null
        }
    }
    if (showLimit) PremiumLimitReachedDialog({ showLimit = false }, { showLimit = false })

    ClientDirectoryContent(
        clients = clients, imageUser = profile?.fotoUrl.orEmpty(), onProfile = goToMyProfile,
        onAddClient = { if (clients.size >= 10 && !premium) showLimit = true else agregarDeudor() },
        onOpenClient = verPerfilDeudor, onPayment = registrarPago, onSale = registrarVenta,
        onEdit = { pendingEdit = it }, onDelete = { pendingDelete = it },
        adContent = if (premium) null else {
            { NativeAdBanner(adUnitId = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110" else "ca-app-pub-1155673544372892/8012390999") }
        }
    )
}
