package com.nexusystem.paguito.ui.screens.deudores

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.screens.home.BorderLight
import com.nexusystem.paguito.ui.screens.productos.NativeAdBanner
import com.nexusystem.paguito.ui.screens.productos.ProductCardItem
import com.nexusystem.paguito.utils.dialogs.EliminarClienteDialog
import com.nexusystem.paguito.utils.dialogs.PremiumLimitReachedDialog
import com.nexusystem.paguito.utils.emptyStates.DynamicEmptyState
import com.nexusystem.paguito.utils.formatAsCurrency

// --- COLORES ---
val BluePrimary2 = Color(0xFF3B82F6)

val TextDark2 = Color(0xFF1F2937)
val TextGray1 = Color(0xFF6B7280)
val BorderLight = Color(0xFFE5E7EB)
val RedUrgent = Color(0xFFEF4444)
val RedBackground = Color(0xFFFEE2E2)
val GrayBackground = Color(0xFFF3F4F6)

// --- MODELO DE DATOS ---
enum class DebtorStatus { OVERDUE, UPCOMING, UP_TO_DATE }

data class Debtor(
    val name: String,
    val phone: String,
    val amount: Double,
    val status: DebtorStatus,
    val dateText: String
)

// Datos de prueba basados en tu imagen
val sampleDebtors = arrayListOf<Debtor>()
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DebtorsScreen(
    verPerfilDeudor: (DeudoresEntity) -> Unit,
    agregarDeudor: () -> Unit,
    registrarPago: (DeudoresEntity) -> Unit,
    registrarVenta: (DeudoresEntity) -> Unit,
    goToMyProfile:()->Unit,
    viewmodel: DeudoresViewModel
) {
    var isSucriptionActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showAlertFreeLimited by remember { mutableStateOf(false) }
    var showDeleteClient by remember { mutableStateOf(false) }
    var currentDebtorSelected by remember { mutableStateOf(DeudoresEntity()) }

    if(showDeleteClient) {
        EliminarClienteDialog(currentDebtorSelected.nombre, {
            showDeleteClient =false
        }, {
            viewmodel.eliminarDeudorYpagos(currentDebtorSelected.idRemoteDatabase)
            showDeleteClient=false
        })
    }

    val sampleDebtors by viewmodel.deudores.collectAsState()
  if(showAlertFreeLimited)
  {
      PremiumLimitReachedDialog({
          showAlertFreeLimited =false
      },{
          showAlertFreeLimited =false
      })
  }
        LaunchedEffect(Unit) {
            viewmodel.loadUserProfile()
        }
    val profile = viewmodel.profileState
    var urlPhotoProfile by remember { mutableStateOf("") }
    LaunchedEffect(profile) {
        if (profile != null) {
            isSucriptionActive = profile.userSuscription.isActive
            urlPhotoProfile = profile.fotoUrl
        }
    }
    val filteredDebtors = remember(searchQuery, sampleDebtors) {
        if (searchQuery.isEmpty()) {
            sampleDebtors
        } else {
            sampleDebtors.filter { deudor ->
                // Filtramos por nombre, ignorando mayúsculas/minúsculas y espacios extras
                deudor?.nombre?.contains(searchQuery.trim(), ignoreCase = true) == true
            }
        }
    }

    LaunchedEffect(Unit) {
        viewmodel.loadUserProfile()
        viewmodel.obtenerDeudores()
    }

    Scaffold(
        floatingActionButton = {
            // Usamos la lista original para saber si mostrar el FAB o el Empty State
            if (sampleDebtors.isNotEmpty()) {
                FloatingActionButton(
                    modifier = Modifier.padding(end = 5.dp, bottom = 90.dp),
                    onClick = {
                        if(sampleDebtors.size>=10 && !isSucriptionActive){
                            showAlertFreeLimited=true
                        }else{
                            agregarDeudor()
                        } },
                    containerColor = BluePrimary2,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Deudor")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp) // Aplicamos paddingValues aquí para evitar superposición
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    // Se usa .padding para separar la barra de los bordes
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                // Alinea verticalmente todos los elementos de la fila
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Clientes",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    // Eliminé top y bottom padding de aquí para que el verticalAlignment del Row funcione perfectamente
                    modifier = Modifier.padding(start = 16.dp)
                )

                // --- ESTE ES EL CAMBIO CLAVE ---
                // Este Spacer actúa como un "muelle" que empuja el Box de la derecha hasta el extremo
                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            goToMyProfile()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // Lógica corregida: Si NO hay imagen, muestra el icono. Si hay, usa AsyncImage.
                    if (urlPhotoProfile.isNullOrEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = "Placeholder de perfil",
                            tint = Color(0xFFEC4899),
                            modifier = Modifier.size(40.dp)
                        )
                    } else {
                        AsyncImage(
                            model = urlPhotoProfile,
                            contentDescription = "Imagen de perfil del usuario",
                            modifier = Modifier
                                .size(45.dp) // Reducido para que quepa bien en el Box de 50.dp
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }


            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar cliente por nombre...", color = TextGray1) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextGray1) },
                // Añadimos un botón para limpiar la búsqueda si hay texto
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = TextGray1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BluePrimary2,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- LISTA DE DEUDORES FILTRADA ---
            if (sampleDebtors.isNotEmpty()) {
                if (filteredDebtors.isEmpty()) {
                    // Estado cuando la búsqueda no coincide con nadie
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No se encontraron resultados para \"$searchQuery\"", color = TextGray1)
                    }
                } else {

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(filteredDebtors) { index, debtor ->
                            DebtorListItem(debtor = debtor!!, verPerfilDeudor, registrarPago, registrarVenta,{
                                currentDebtorSelected =it
                                showDeleteClient=true
                            })
                            Divider(color = MaterialTheme.colorScheme.background, thickness = 1.dp)
                            if(isSucriptionActive==false){
                            val isLast = index == filteredDebtors.lastIndex
                            val isEveryFive = (index + 1) % 5 == 0

                            if (isEveryFive || (isLast && (index + 1) % 5 != 0)) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(1.dp)
                                ) {
                                    NativeAdBanner(
                                        adUnitId = if (BuildConfig.DEBUG)
                                            "ca-app-pub-3940256099942544/2247696110"
                                        else
                                            "ca-app-pub-1155673544372892/8012390999"
                                    )
                                }
                            }
                                }
                        }
                        item{
                            Spacer(modifier = Modifier.height(90.dp))
                        }
                    }
                }
            } else {
                DynamicEmptyState(
                    imagePainter = painterResource(id = R.drawable.empty_state_products),
                    title = "Aún no tienes deudores",
                    description = "Comienza a agregar a tus deudores para gestionar tus ventas...",
                    buttonText = "Agregar",
                    buttonIcon = Icons.Default.Add,
                    onButtonClick = { agregarDeudor() }
                )
            }
        }
    }
}

// --- COMPONENTES SECUNDARIOS ---

@Composable
fun FilterChipItem(text: String, count: Int, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) BluePrimary2 else  MaterialTheme.colorScheme.surface
    val contentColor = if (isSelected) Color.White else TextGray1
    val borderColor = if (isSelected) BluePrimary2 else MaterialTheme.colorScheme.surface
    val badgeBgColor = if (isSelected) Color.White.copy(alpha = 0.2f) else GrayBackground

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = text, color = contentColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(badgeBgColor)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = count.toString(), color = contentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DebtorListItem(debtor: DeudoresEntity,verPerfilDeudor:(DeudoresEntity)->Unit,registrarPago:(DeudoresEntity)->Unit,registrarVenta:(DeudoresEntity)->Unit,elminarCliente:(DeudoresEntity)->Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth().padding(16.dp)

        ) {
            // --- Fila Superior: Info + Monto ---
            Row(modifier = Modifier.fillMaxWidth()) {
                // Avatar Placeholder (En una app real usa AsyncImage de Coil)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BorderLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = TextGray1)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Datos del usuario
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = debtor.nombre, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = TextGray1, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = debtor.telefono, fontSize = 13.sp, color =  MaterialTheme.colorScheme.onSurface)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badges de Estado y Fecha
                   /* Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(DebtorStatus.OVERDUE)
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint =  MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "hoy", fontSize = 12.sp, color =  MaterialTheme.colorScheme.onSurface)
                    }*/
                }

                // Monto Total
                Column(horizontalAlignment = Alignment.End) {
                    val amountColor = if (DebtorStatus.OVERDUE == DebtorStatus.OVERDUE) RedUrgent else  MaterialTheme.colorScheme.onSurface
                    Text(
                        text = formatAsCurrency(debtor.montoActualAdeudado),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = amountColor
                    )
                    Text(text = "Total adeudado", fontSize = 11.sp, color =  MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End, modifier = Modifier.padding(start = 8.dp)) {
                    if(debtor.inRemote){
                    Icon(
                        Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = TextGray1,
                        modifier = Modifier.size(14.dp)
                    )}else{
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = TextGray1,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- Fila Inferior: Botones de Acción ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { registrarPago(debtor) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePrimary2),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Nuevo pago", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { registrarVenta(debtor) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Nueva venta", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { elminarCliente(debtor) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedUrgent),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp), // Reducido para el icono
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete, // O usa tu icono vectorial de basura
                        contentDescription = "Eliminar Cliente",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp) // Tamaño adecuado para el icono
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement =Arrangement.Center, modifier = Modifier.fillMaxWidth() ) {
                Text(
                    text = "Ver detalles",
                    fontSize = 14.sp,
                    color =  MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { verPerfilDeudor(debtor) }.padding(8.dp)
                )
            }
        }
    }

}

@Composable
fun StatusBadge(status: DebtorStatus) {
    val (bgColor, textColor, icon, text) = when (status) {
        DebtorStatus.OVERDUE -> listOf(RedBackground, RedUrgent, Icons.Outlined.ErrorOutline, "Vencido")
        DebtorStatus.UPCOMING -> listOf(Color.Transparent, TextDark2, Icons.Outlined.Schedule, "Próximo")
        DebtorStatus.UP_TO_DATE -> listOf(Color.Transparent, TextGray1, null, "Al día")
    }

    Surface(
        color = bgColor as Color,
        shape = RoundedCornerShape(12.dp),
        border = if (status != DebtorStatus.OVERDUE) BorderStroke(1.dp, BorderLight) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(icon as ImageVector, contentDescription = null, tint = textColor as Color, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(text = text as String, color = textColor as Color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}