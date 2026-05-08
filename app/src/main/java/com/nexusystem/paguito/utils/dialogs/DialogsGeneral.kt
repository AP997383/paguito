package com.nexusystem.paguito.utils.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.material3.OutlinedTextFieldDefaults.colors
import androidx.compose.material3.SegmentedButtonDefaults.borderStroke
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nexusystem.paguito.ui.screens.deudores.GreenPrimary
import com.nexusystem.paguito.ui.screens.deudores.TextDark
import com.nexusystem.paguito.ui.screens.payments.GreenLightBg
import com.nexusystem.paguito.ui.screens.perfil.TextSecondary
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.screens.deudores.GrayBackground
import com.nexusystem.paguito.ui.screens.deudores.TextGray
import kotlinx.coroutines.delay

val GrayBorder = Color(0xFFE5E7EB)
val GrayBadgeBg = Color(0xFFF3F4F6)
val AmberLight = Color(0xFFFEF3C7)
val AmberIcon = Color(0xFFD97706)
@Composable
fun SuccessAbonoDialog(
    onDismiss: () -> Unit,
    onViewTicket: () -> Unit,
    title: String,
    description: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícono de Check con fondo verde suave
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color(0xFF2E7D32)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Título
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mensaje
                Text(
                    text = description,
                    fontSize = 16.sp,
                    color = Color(0xFF757575),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Botón principal (Ver ticket)
                Button(
                    onClick = onViewTicket,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF27AE60) // Verde del diseño
                    )
                ) {
                    Text(
                        text = "Ver ticket",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Botón secundario (Cerrar)
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE0E0E0))
                    )
                ) {
                    Text(
                        text = "Cerrar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}



// Definimos una paleta de colores para el estado de error
private object ErrorDialogColors {
    val errorContainer = Color(0xFFFEF2F2) // Rojo muy claro (fondo icono)
    val onErrorContainer = Color(0xFFEF4444) // Rojo vibrante (icono x)
    val onSuccessContainer = Color(0xFF69F0AE) // Rojo vibrante (icono x)
    val textPrimary = Color(0xFF1F2937) // Gris muy oscuro (Casi negro para título)
    val textSecondary = Color(0xFF6B7280) // Gris medio (para descripción)
}

@Composable
fun SuccessAlertDialog(
    mesage:String,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        // Contenedor principal del diálogo (la tarjeta blanca)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(24.dp), // Margen externo del diálogo
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp // Sombra suave
        ) {
            Column(
                modifier = Modifier.padding(24.dp), // Padding interno de la tarjeta
                horizontalAlignment = Alignment.CenterHorizontally // Centrar contenido horizontalmente
            ) {
                // --- Icono de Error (La "X" roja) ---
                SucessIcon()

                Spacer(modifier = Modifier.height(24.dp)) // Espaciado entre icono y texto

                // --- Título del Diálogo ---
                Text(
                    text = "Éxito",
                    style = MaterialTheme.typography.headlineSmall, // headlineSmall es apropiado (aprox 24sp)
                    fontWeight = FontWeight.Bold,
                    color = ErrorDialogColors.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp)) // Espaciado corto entre título y descripción

                // --- Texto Descriptivo ---
                Text(
                    text = mesage,
                    style = MaterialTheme.typography.bodyLarge, // bodyLarge es legible (aprox 16sp)
                    color = ErrorDialogColors.textSecondary,
                    textAlign = TextAlign.Center, // Centrar el texto de múltiples líneas
                    lineHeight = 24.sp, // Mejorar legibilidad de líneas múltiples
                    modifier = Modifier.padding(horizontal = 16.dp) // Padding extra para que no toque bordes
                )

                Spacer(modifier = Modifier.height(32.dp)) // Espaciado largo antes del botón

                // --- Botón de Acción Principal (Cerrar) ---
                ErrorDialogConfirmButton(onConfirmClick = onConfirmClick)
            }
        }
    }
}

@Composable
fun ErrorAlertDialog(
    mesage:String,
    onDismissRequest: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        // Contenedor principal del diálogo (la tarjeta blanca)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(24.dp), // Margen externo del diálogo
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp // Sombra suave
        ) {
            Column(
                modifier = Modifier.padding(24.dp), // Padding interno de la tarjeta
                horizontalAlignment = Alignment.CenterHorizontally // Centrar contenido horizontalmente
            ) {
                // --- Icono de Error (La "X" roja) ---
                ErrorIcon()

                Spacer(modifier = Modifier.height(24.dp)) // Espaciado entre icono y texto

                // --- Título del Diálogo ---
                Text(
                    text = "Error",
                    style = MaterialTheme.typography.headlineSmall, // headlineSmall es apropiado (aprox 24sp)
                    fontWeight = FontWeight.Bold,
                    color = ErrorDialogColors.textPrimary
                )

                Spacer(modifier = Modifier.height(16.dp)) // Espaciado corto entre título y descripción

                // --- Texto Descriptivo ---
                Text(
                    text = mesage,
                    style = MaterialTheme.typography.bodyLarge, // bodyLarge es legible (aprox 16sp)
                    color = ErrorDialogColors.textSecondary,
                    textAlign = TextAlign.Center, // Centrar el texto de múltiples líneas
                    lineHeight = 24.sp, // Mejorar legibilidad de líneas múltiples
                    modifier = Modifier.padding(horizontal = 16.dp) // Padding extra para que no toque bordes
                )

                Spacer(modifier = Modifier.height(32.dp)) // Espaciado largo antes del botón

                // --- Botón de Acción Principal (Cerrar) ---
                ErrorDialogConfirmButton(onConfirmClick = onConfirmClick)
            }
        }
    }
}

@Composable
private fun SucessIcon() {


    // 2. Lo mostramos dentro del círculo
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(ErrorDialogColors.onSuccessContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Error",
            tint = Color.Unspecified, // Para que use el color definido en el path (rojo)
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Componente que recrea el icono rojo con la 'X' en el centro.
 */
@Composable
private fun ErrorIcon() {
    // 1. Creamos el ImageVector primero
    val errorXIcon = remember {
        ImageVector.Builder(
            name = "error_x",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                stroke = SolidColor(ErrorDialogColors.onErrorContainer),
                strokeLineWidth = 3f, // Un poco más grueso para que resalte
                strokeLineCap = StrokeCap.Round
            ) {
                moveTo(17f, 7f)
                lineTo(7f, 17f)
                moveTo(7f, 7f)
                lineTo(17f, 17f)
            }
        }.build()
    }

    // 2. Lo mostramos dentro del círculo
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(ErrorDialogColors.errorContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = errorXIcon,
            contentDescription = "Error",
            tint = Color.Unspecified, // Para que use el color definido en el path (rojo)
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Componente que recrea el botón ancho con bordes redondeados.
 */
@Composable
private fun ErrorDialogConfirmButton(onConfirmClick: () -> Unit) {
    OutlinedButton(
        onClick = onConfirmClick,
        modifier = Modifier
            .fillMaxWidth() // El botón ocupa todo el ancho disponible
            .height(56.dp), // Altura cómoda para tocar (similar a la imagen)
        shape = RoundedCornerShape(28.dp), // Bordes muy redondeados (Píldora)
        // Definimos el estilo del borde de la imagen
    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorDialogColors.textPrimary)
    ) {
        Text(
            text = "Cerrar",
            style = MaterialTheme.typography.titleMedium, // titleMedium es bueno para botones (aprox 16sp)
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SuccessDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF69F0AE) // Rojo suave
            )
        },
        title = {
            Text(text = "!Datos Actualizados!")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF69F0AE) // Color de alerta
                )
            ) {
                Text("Cerrar")
            }
        },

        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = Color(0xFFEF4444) // Rojo suave
            )
        },
        title = {
            Text(text = "Cerrar sesión")
        },
        text = {
            Text(text = "¿Estás seguro de que deseas salir? Tendrás que ingresar tus credenciales nuevamente para acceder.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444) // Color de alerta
                )
            ) {
                Text("Sí, cerrar sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun PremiumLimitReachedDialog(
    onDismiss: () -> Unit,
    onUpgrade: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header (Icono y Badge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(GreenLightBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Reemplaza con tu icono de corona
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cown),
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Surface(
                        color = GrayBadgeBg,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Plan: FREE",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Título y Descripción
                Text(
                    text = "Límite alcanzado",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Has alcanzado el número máximo de clientes y productos en el plan FREE. Actualiza a Premium para eliminar límites y acceder a beneficios exclusivos.",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Lista de Beneficios
                Surface(
                    color = GreenLightBg.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GreenPrimary.copy(alpha = 0.1f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        BenefitItem("Clientes y productos ilimitados")
                        BenefitItem("Reportes avanzados")
                        BenefitItem("Soporte prioritario")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Botones
                Button(
                    onClick = onUpgrade,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("Actualizar a Premium", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, GrayBorder)
                ) {
                    Text("Cerrar", fontSize = 16.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun BenefitItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = GreenPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, fontSize = 15.sp, color = TextDark)
    }
}

@Composable
fun AbonoMayorSaldoDialog(
    saldoPendiente: String,
    montoIngresado: String,
    onDismiss: () -> Unit,
    onCorregir: () -> Unit,
    onAceptarSaldoAFavor: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono de advertencia
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(AmberLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = null,
                        tint = AmberIcon,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Título
                Text(
                    text = "Abono mayor al saldo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Descripción
                Text(
                    text = "El monto ingresado supera la deuda pendiente. ¿Deseas corregir el monto o aceptar que quede un saldo a favor del cliente?",
                    fontSize = 14.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tarjeta de montos
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GrayBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Saldo pendiente:", color = TextGray)
                            Text(saldoPendiente, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Monto ingresado:", color = TextGray)
                            Text(montoIngresado, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botones
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onCorregir,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, GrayBorder)
                    ) {
                        Text("Corregir", color = TextDark, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = onAceptarSaldoAFavor,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Aceptar", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
val SuccessGreen = Color(0xFF22C55E) // El verde vibrante de los botones/checks
val SuccessGreenBg = Color(0xFFF0FDF4) // El verde muy claro de fondo del icono
val ErrorRed = Color(0xFFEF4444) // Color de error estándar
val ErrorRedBg = Color(0xFFFEF2F2) // Fondo claro de error
// --- ENUM PARA EL ESTADO ---
enum class DialogState {
    SUCCESS,
    ERROR
}

@Composable
fun SubscriptionStatusDialog(
    state: DialogState,
    onDismiss: () -> Unit,
    onPrimaryAction: () -> Unit
) {
    // Definimos los recursos según el estado (Éxito o Error)
    val icon: ImageVector
    val iconColor: Color
    val iconBgColor: Color
    val title: String
    val description: String
    val primaryButtonText: String
    val primaryButtonColor: Color

    when (state) {
        DialogState.SUCCESS -> {
            icon = Icons.Default.Check
            iconColor = SuccessGreen
            iconBgColor = SuccessGreenBg
            title = "Suscripción activada"
            description = "Tu suscripción mensual fue activada correctamente. Disfruta de los beneficios Premium."
            primaryButtonText = "Ver beneficios"
            primaryButtonColor = SuccessGreen
        }
        DialogState.ERROR -> {
            icon = Icons.Default.Close
            iconColor = ErrorRed
            iconBgColor = ErrorRedBg
            title = "Error en la suscripción"
            description = "Hubo un problema al procesar tu suscripción. Por favor, intenta de nuevo o contacta a soporte."
            primaryButtonText = "Reintentar"
            primaryButtonColor = ErrorRed
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp), // Bordes redondeados del diálogo
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp), // Espaciado interno
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Icono central con fondo circular
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Título principal
                Text(
                    text = title,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Descripción
                Text(
                    text = description,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = TextGray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 4. Botón de Acción Principal (Ver beneficios / Reintentar)
              /*  Button(
                    onClick = onPrimaryAction,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryButtonColor)
                ) {
                    Text(
                        text = primaryButtonText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }*/

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Botón Secundario (Cerrar)
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, TextGray.copy(alpha = 0.2f)) // Borde sutil
                ) {
                    Text(
                        text = "Cerrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextGray
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessRegistrationDialog(
    onNavigateLogin: () -> Unit,
    onDismiss: () -> Unit
) {
    // Estado para el contador de segundos
    var secondsLeft by remember { mutableStateOf(10) }

    // Lógica del contador regresivo
    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000L) // Espera 1 segundo
            secondsLeft--
        }
        // Al terminar el contador, se dispara la acción
        onNavigateLogin()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono circular de éxito
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE6F9F0),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Registro completado",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Tu cuenta ha sido creada correctamente. Serás redirigido a la pantalla de inicio de sesión para acceder con tus credenciales.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Badge de redirección
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE6F9F0)
                ) {
                    Text(
                        text = "Redirigiendo en $secondsLeft...",
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón principal
                Button(
                    onClick = onNavigateLogin,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Ir a Iniciar Sesión →", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón secundario
                TextButton(onClick = onDismiss) {
                    Text("Volver al inicio", color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
