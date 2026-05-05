package com.nexusystem.paguito.ui.screens.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.utils.getDaysUntilNextPayment
import java.util.Locale

// Paleta de colores basada en la imagen de referencia
val GrisFondoCard = Color(0xFFF2F2F2) // O Color(0xFFE0E0E0)
val RojoAlerta = Color(0xFFEB5757) // Color aproximado del punto y monto
val GrisTextoSecundario = Color(0xFF757575)
val GrisIcono = Color(0xFF616161)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaClientesDeudaScreen(deudoresViewModel: DeudoresViewModel,onBackClick:()->Unit) {
    // Datos de ejemplo basados en tu imagen
    val listaOriginal by deudoresViewModel.deudores.collectAsState()


    var textoBusqueda by remember { mutableStateOf("") }

    // Filtrar la lista basada en la búsqueda
    val listaFiltrada = remember(textoBusqueda, listaOriginal) {
        if (textoBusqueda.isEmpty()) {
            listaOriginal
        } else {
            listaOriginal.filter {
                it!!.nombre.contains(textoBusqueda, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Fondo principal
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            // Header con Flecha Back
            TopAppBar(
                title = { Text("Proximos Cliente", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
           ,    windowInsets = WindowInsets(0.dp) )
        }
        // --- BUSCADOR ---
        OutlinedTextField(
            value = textoBusqueda,
            onValueChange = { textoBusqueda = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Buscar cliente...", color = GrisTextoSecundario) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GrisIcono) },
            singleLine = true,
            shape = RoundedCornerShape(24.dp), // Bordes muy redondeados para estilo moderno
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.LightGray,
                unfocusedBorderColor = Color.LightGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = Color.Black
            )
        )

        // --- LISTA VERTICAL ---
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Espacio entre elementos
        ) {
            items(listaFiltrada) { cliente ->
                ClienteDeudaListItem(cliente = cliente!!)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ClienteDeudaListItem(cliente: DeudoresEntity) {
    // Contenedor principal del ítem (reemplaza la Card grande)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GrisFondoCard,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Columna 1: Icono de Persona ---
            Box(contentAlignment = Alignment.TopEnd) {
                // Icono principal gris
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = GrisIcono,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // --- Columna 2: Info del Cliente (Información Horizontal) ---
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Nombre
                Text(
                    text = cliente.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Fila de Info Económica y Fecha
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Saldo Restante
                    Column {
                        Text(
                            text = "Saldo restante:",
                            fontSize = 12.sp,
                            color = GrisTextoSecundario
                        )
                        Text(
                            text = String.format(Locale.US, "$%.2f", cliente.montoActualAdeudado),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }

                    // Siguiente Pago (con icono)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Siguiente pago en:",
                            fontSize = 12.sp,
                            color = GrisTextoSecundario
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = RojoAlerta,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${getDaysUntilNextPayment(cliente.fechaInicialDeuda,cliente.periodicidad).toString()+" dìas"}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = RojoAlerta
                            )
                        }
                    }
                }
            }
        }
    }
}