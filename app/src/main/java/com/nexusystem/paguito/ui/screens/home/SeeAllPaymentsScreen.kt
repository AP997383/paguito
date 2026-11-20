import androidx.compose.runtime.*
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.ui.components.MovementCard
import com.nexusystem.paguito.ui.components.toMovement
import com.nexusystem.paguito.ui.screens.home.components.TransactionHistoryContent
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel

@Composable
fun HistorialPagosScreen(onBackClick: () -> Unit, pagosViewModel: PagosViewModel, onMovement: (PagoConNombre) -> Unit = {}) {
    val payments by pagosViewModel.pagosConNombre.collectAsState()
    LaunchedEffect(Unit) { pagosViewModel.obtenerUltimosAbonos() }
    TransactionHistoryContent(payments.filterNotNull(), onBackClick, onMovement)
}

@Composable
fun PagoItem(pago: PagoConNombre) { MovementCard(pago.toMovement()) }
