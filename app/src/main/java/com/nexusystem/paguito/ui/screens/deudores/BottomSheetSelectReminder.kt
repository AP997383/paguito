package com.nexusystem.paguito.ui.screens.deudores

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElegirTipoRecordatorioBottomSheet(
    onDismissRequest: () -> Unit,
    onOptionSelected: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = Color(0xFFF4F6F9),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE3EDF9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Elegir tipo de recordatorio",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1E293B)
                    )
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar",
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtítulo descriptivo
            Text(
                text = "Selecciona la plantilla de mensaje adecuada para notificar al cliente vía WhatsApp.",
                fontSize = 13.sp,
                color = Color(0xFF64748B),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Opción 1: Recordatorio preventivo (Azul)
            RecordatorioCard(
                titulo = "Recordatorio preventivo",
                descripcion = "Orientado a pagos próximos a vencer. Notification cordial de aviso previo.",
                ejemploLabel = "Ej. Para pagos próximos",
                badgeText = "Próximo",
                badgeColor = Color(0xFFE0E7FF),
                badgeTextColor = Color(0xFF3B82F6),
                borderColor = Color(0xFF93C5FD),
                containerColor = Color.White,
                icono = Icons.Default.Notifications,
                iconoBgColor = Color(0xFFE3EDF9),
                iconoTintColor = Color(0xFF3B82F6),
                botonTexto = "Enviar preventivo",
                botonColor = Color(0xFF3B82F6),
                isSeleccionado = true,
                onClick = {
                    onOptionSelected(1)
                    onDismissRequest()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Opción 2: Aviso de morosidad (Rojo/Coral)
            RecordatorioCard(
                titulo = "Aviso de morosidad",
                descripcion = "Orientado a pagos vencidos. Notificación formal de cobro pendiente.",
                ejemploLabel = "Ej. Para pagos atrasados",
                badgeText = "Atrasado",
                badgeColor = Color(0xFFFEE2E2),
                badgeTextColor = Color(0xFFEF4444),
                borderColor = Color(0xFFFCA5A5),
                containerColor = Color.White,
                icono = Icons.Default.Warning,
                iconoBgColor = Color(0xFFFEE2E2),
                iconoTintColor = Color(0xFFEF4444),
                botonTexto = "Enviar aviso",
                botonColor = Color(0xFFEF4444),
                isSeleccionado = false,
                onClick = {
                    onOptionSelected(2)
                    onDismissRequest()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Vista previa de plantilla
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEBF3FC)),
                border = BorderStroke(1.dp, Color(0xFFD6E4F0))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Vista previa de plantilla:",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Icono de WhatsApp simulado
                            Text(
                                text = "WhatsApp",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF25D366)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "\"Hola Carlos, te recordamos amablemente que tu próximo pago por $1,500.00 vence pronto. ¡Agradecemos tu puntualidad!\"",
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón CANCELAR inferior
            OutlinedButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF64748B))
            ) {
                Text(
                    text = "CANCELAR",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun RecordatorioCard(
    titulo: String,
    descripcion: String,
    ejemploLabel: String,
    badgeText: String,
    badgeColor: Color,
    badgeTextColor: Color,
    borderColor: Color,
    containerColor: Color,
    icono: ImageVector,
    iconoBgColor: Color,
    iconoTintColor: Color,
    botonTexto: String,
    botonColor: Color,
    isSeleccionado: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono izquierdo con fondo circular
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(iconoBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = iconoTintColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = badgeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = titulo,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            // Badge pequeño (Próximo / Atrasado)

                        }
                    }
                }

                // Check indicativo derecho si está seleccionado
                if (isSeleccionado) {
                    RadioButton(
                        selected = true,
                        onClick = onClick,
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF3B82F6))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = descripcion,
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ejemploLabel,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(containerColor = botonColor),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = botonTexto,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}