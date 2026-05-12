package com.nexusystem.paguito.utils.dialogs

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
// Enumeración para gestionar el estado de la pantalla
enum class EstadoEliminacion {
    CONFIRMACION_BENEFICIOS,
    ADVERTENCIA_IRREVERSIBLE
}
val VerdeCheck = Color(0xFF2ECC71)
val VerdeFondoEscudo = Color(0xFFEAFAF1)
val AzulPrimario = Color(0xFF3498DB)
val RojoAdvertencia = Color(0xFFE74C3C)
val RojoFondoIcono = Color(0xFFFDEDEC)
val GrisTexto = Color(0xFF7F8C8D)
val GrisBordeList = Color(0xFFECF0F1)


@Composable
fun CuentaEliminacionScreen(onDismiss: () -> Unit, onFinalConfirm: () -> Unit) {
    // Estado que controla qué pantalla mostrar
    var estadoActual by remember { mutableStateOf(EstadoEliminacion.CONFIRMACION_BENEFICIOS) }

    // Usamos un Dialog para que aparezca sobre el contenido (como en las imágenes)
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón de cerrar superior derecho
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Outlined.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Usamos Crossfade para una transición suave entre pantallas
                Crossfade(targetState = estadoActual, label = "CambioPantallaEliminacion") { estado ->
                    when (estado) {
                        EstadoEliminacion.CONFIRMACION_BENEFICIOS -> {
                            PantallaConfirmacionBeneficios(
                                onSiguiente = { estadoActual = EstadoEliminacion.ADVERTENCIA_IRREVERSIBLE },
                                onCancelar = onDismiss
                            )
                        }
                        EstadoEliminacion.ADVERTENCIA_IRREVERSIBLE -> {
                            PantallaAdvertenciaIrreversible(
                                onConfirmarEliminacion = onFinalConfirm,
                                onCancelar = onDismiss
                            )
                        }
                    }
                }
            }
        }
    }
}

// --- PRIMERA PANTALLA (imagen_4.png) ---
@Composable
fun PantallaConfirmacionBeneficios(onSiguiente: () -> Unit, onCancelar: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Icono de Escudo Verde
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(VerdeFondoEscudo, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Shield, // O una imagen personalizada del escudo
                contentDescription = null,
                tint = VerdeCheck,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "¿Estás seguro?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Antes de eliminar tu cuenta, considera estos beneficios por mantenerla activa:",
            fontSize = 14.sp,
            color = GrisTexto,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Lista de beneficios
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BeneficioItem("Clientes y productos guardados")
            BeneficioItem("Historial de pagos y reportes")
            BeneficioItem("Acceso a beneficios de la suscripción (si aplica)")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Siguiente (Azul)
        Button(
            onClick = onSiguiente,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Siguiente", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón Cancelar (Texto)
        TextButton(onClick = onCancelar) {
            Text("Cancelar", color = GrisTexto, fontSize = 14.sp)
        }
    }
}

// Componente reutilizable para cada ítem de beneficio
@Composable
fun BeneficioItem(texto: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = VerdeCheck,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = texto, fontSize = 14.sp, color = Color.Black)
    }
}


// --- SEGUNDA PANTALLA (imagen_5.png) ---
@Composable
fun PantallaAdvertenciaIrreversible(onConfirmarEliminacion: () -> Unit, onCancelar: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Icono de Advertencia Rojo
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(RojoFondoIcono, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Filled.Warning, // O una imagen personalizada del icono de alerta
                contentDescription = null,
                tint = RojoAdvertencia,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Advertencia: eliminación irreversible",
            fontSize = 20.sp, // Un poco más pequeño para que quepa bien
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Si eliminas tu cuenta perderás toda tu información: clientes, pagos, productos y beneficios de suscripción. Esta acción no se puede deshacer.",
            fontSize = 14.sp,
            color = GrisTexto,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Recuadro gris con la lista de lo que se pierde
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = GrisBordeList.copy(alpha = 0.5f)),
            border = BorderStroke(1.dp, GrisBordeList)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ItemPerdida("Se eliminarán: usuarios, historial de pagos, clientes, productos y configuraciones.")
                Spacer(modifier = Modifier.height(12.dp))
                ItemPerdida("Si tienes suscripción activa, se cancelará y no habrá reembolso automático.")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Confirmar Eliminación (Rojo)
        Button(
            onClick = onConfirmarEliminacion,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = RojoAdvertencia),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Confirmar eliminación", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Botón Cancelar (Borde Negro)
        OutlinedButton(
            onClick = onCancelar,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text("Cancelar", color = Color.Black, fontSize = 16.sp)
        }
    }
}

// Componente para los ítems de la lista de pérdidas (con punto rojo)
@Composable
fun ItemPerdida(texto: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = "• ", color = RojoAdvertencia, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = texto, fontSize = 14.sp, color = GrisTexto, lineHeight = 18.sp)
    }
}