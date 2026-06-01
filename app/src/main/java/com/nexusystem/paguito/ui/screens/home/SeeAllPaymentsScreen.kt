import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.ui.screens.deudores.BorderColor
import com.nexusystem.paguito.ui.screens.deudores.IconBgGray
import com.nexusystem.paguito.ui.screens.deudores.TextDark2
import com.nexusystem.paguito.ui.screens.payments.PagosViewModel
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.formatFecha
import com.nexusystem.paguito.utils.formatLongDateTime
import java.util.Locale


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialPagosScreen(onBackClick: () -> Unit,pagosViewModel: PagosViewModel) {
    val pagosHistorial by pagosViewModel.pagosConNombre.collectAsState()

    var query by remember { mutableStateOf("") }
    val pagosFiltrados = remember(query, pagosHistorial) {
        pagosHistorial.filter { pago ->
            pago?.nameDeudor?.contains(query, ignoreCase = true) == true
        }
    }
    LaunchedEffect(Unit) {
        pagosViewModel.obtenerUltimosAbonos()
    }
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                // Header con Flecha Back
                TopAppBar(
                    title = { Text("Historial de Pagos", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                         ,   windowInsets = WindowInsets(0.dp)
                )

                // Buscador
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Buscar por nombre...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedBorderColor = Color.LightGray,
                        unfocusedBorderColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        // Contenedor gris claro como el de tu imagen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background) // El fondo gris de la imagen
        ) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), // Fondo del contenedor de la lista
                shape = RoundedCornerShape(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp) // Espacio entre registros
                ) {
                    items(pagosFiltrados) { pago ->
                        PagoItem(pago!!)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PagoItem(pago: PagoConNombre) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de Persona
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Info Central (Nombre, Fecha, Método)
        Column(modifier = Modifier.weight(1f)) {
            var stringTyePayment ="Efectivo"
            when(pago!!.tipoPago){
                1->{
                    stringTyePayment = "Efectivo"
                }
                2->{
                    stringTyePayment = "Transferencia"
                }
                3->{
                    stringTyePayment = "Trajeta"
                }
            }
            Text(
                text = pago.nameDeudor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${formatFecha(pago.fechaAbono)} • ${stringTyePayment}",
                fontSize = 13.sp,
                color = Color(0xFF8E8E93) // Color gris de la imagen
            )
            Row{
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, BorderColor, CircleShape)
                        .background(IconBgGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if(pago.isIngreso)
                        Icon(Icons.Outlined.Download, contentDescription = null, tint = TextDark2, modifier = Modifier.size(16.dp))
                    else
                        Icon(Icons.Outlined.LocalMall, contentDescription = null, tint = TextDark2, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text( text = if (pago.isIngreso) "Abono" else "Venta", fontSize = 13.sp, fontWeight = FontWeight.Bold,  color = if (pago.isIngreso) Color(0xFF2E7D32) else Color(0xFF1976D2),)
            }

        }

        // Monto a la derecha
        Text(
            text =formatAsCurrency(pago.montoAbonado),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}