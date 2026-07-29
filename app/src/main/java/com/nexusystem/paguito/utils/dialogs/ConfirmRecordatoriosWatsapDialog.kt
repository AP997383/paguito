package com.nexusystem.paguito.utils.dialogs
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

val PrimaryBlue = Color(0xFF4A6FFF)
val TextDark = Color(0xFF1A1F2C)
val TextSecondary = Color(0xFF8D95A6)
val CardBackground = Color(0xFFEEF2F6)

@Composable
fun WhatsappReminderDialog(
    onDismissRequest: () -> Unit,
    onActivateClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Permite ajustar el ancho libremente
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.925f) // Ocupa el ancho adecuado dejando márgenes a los lados
                .wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pequeña barra indicadora superior (estilo modal/sheet)
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Título
                Text(
                    text = "¿Activar recordatorios\nde WhatsApp?",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextDark,
                        lineHeight = 32.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Descripción
                Text(
                    text = "Al activar los recordatorios, se enviarán " +
                            "mensajes automáticos a tus clientes cada " +
                            "vez que se acerque su fecha de pago o " +
                            "cuando tengan un pago atrasado.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = TextSecondary,
                        lineHeight = 22.sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta de Beneficios interna
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardBackground, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BenefitItem("Ahorro de tiempo en cobranza")
                    BenefitItem("Comunicación 24/7 sin intervención")
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botón de Acción Principal
                Button(
                    onClick = onActivateClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Text(
                        text = "Activar ahora",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Cancelar
                TextButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "CANCELAR",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pie de página de seguridad
                Text(
                    text = "SEGURIDAD FINANCIERA PROTEGIDA",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary.copy(alpha = 0.5f),
                        letterSpacing = 0.5.sp,
                        fontSize = 10.sp
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BenefitItemWatsap(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = TextDark,
                fontWeight = FontWeight.Medium
            )
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF333333)
@Composable
fun DialogPreview() {
    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            WhatsappReminderDialog(
                onDismissRequest = {},
                onActivateClick = {}
            )
        }
    }
}