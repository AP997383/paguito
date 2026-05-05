package com.nexusystem.paguito.utils.emptyStates

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- COLORES ---
val GreenPrimary = Color( 0xFF15956F)
val TextDark = Color(0xFF1F2937)
val TextLightGray = Color(0xFF9CA3AF)
val BgLightGreenCircle = Color(0xFFE8F5E9) // Color de fondo circular suave
val BgScreenColor = Color(0xFFF9FAFB)

/**
 * COMPONENTE DINÁMICO REUTILIZABLE
 *
 * @param imagePainter La imagen o ilustración principal. Usa `painterResource(id)`.
 * @param title Título principal en negrita.
 * @param description Texto secundario descriptivo.
 * @param buttonText Texto del botón de acción principal.
 * @param buttonIcon Icono opcional para el botón (ej. un signo "+").
 * @param onButtonClick Acción a ejecutar al pulsar el botón.
 */
@Composable
fun DynamicEmptyState(
    imagePainter: Painter,
    title: String,
    description: String,
    buttonText: String,
    buttonIcon: ImageVector? = null,
    onButtonClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 32.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Imagen Central con Fondo Circular
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(BgLightGreenCircle, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = imagePainter,
                contentDescription = null,
                modifier = Modifier.size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 2. Textos
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = description,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // 3. Botón de Acción
        Button(
            onClick = onButtonClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            if (buttonIcon != null) {
                Icon(buttonIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = buttonText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ==========================================
// EJEMPLOS DE USO (PREVIEWS)
// ==========================================

// NOTA: Reemplaza R.drawable.tu_imagen por los IDs reales de tus recursos drawable.

@Preview(showBackground = true, name = "1. Sin Productos")
@Composable
fun PreviewEmptyProducts() {
    // Supongamos que tu imagen de la caja se llama "ic_empty_box"
    DynamicEmptyState(
        imagePainter = painterResource(id = android.R.drawable.ic_menu_gallery), // Reemplazar con R.drawable.ic_empty_box
        title = "Aún no tienes productos",
        description = "Comienza a agregar tu inventario para gestionar tus ventas y calcular tus ganancias fácilmente.",
        buttonText = "Agregar Primer Producto",
        buttonIcon = Icons.Default.Add,
        onButtonClick = { /* Navegar a Agregar Producto */ }
    )
}

@Preview(showBackground = true, name = "2. Sin Deudores")
@Composable
fun PreviewEmptyDebtors() {
    // Supongamos que tu imagen de la libreta se llama "ic_empty_debtors"
    DynamicEmptyState(
        imagePainter = painterResource(id = android.R.drawable.ic_menu_my_calendar), // Reemplazar con R.drawable.ic_empty_debtors
        title = "Sin deudores registrados",
        description = "Agrega a tus clientes para llevar el control de sus cuentas y registrar sus deudas.",
        buttonText = "Agregar Nuevo Cliente",
        buttonIcon = Icons.Default.PersonAdd,
        onButtonClick = { /* Navegar a Agregar Cliente */ }
    )
}

@Preview(showBackground = true, name = "3. Sin Abonos")
@Composable
fun PreviewEmptyPayments() {
    // Supongamos que tu imagen del celular se llama "ic_empty_payments"
    DynamicEmptyState(
        imagePainter = painterResource(id = android.R.drawable.ic_menu_call), // Reemplazar con R.drawable.ic_empty_payments
        title = "No hay abonos registrados",
        description = "Aquí aparecerá el historial de pagos cuando comiences a registrar los abonos de tus clientes.",
        buttonText = "Registrar Primer Abono",
        // buttonIcon = null, // Este ejemplo no tiene icono en el botón, si quisieras
        onButtonClick = { /* Navegar a Registrar Abono */ }
    )
}




// Colores sugeridos para el Empty State (puedes mover esto a tu Theme)
private object PaymentHistoryEmptyColors {
    val iconContainer = Color(0xFFF3F4F6) // Gris muy claro (fondo icono)
    val iconTint = Color(0xFF9CA3AF) // Gris medio (icono)
    val textPrimary = Color(0xFF1F2937) // Gris muy oscuro (Título)
    val textSecondary = Color(0xFF6B7280) // Gris medio (Descripción)
}

@Composable
fun PaymentHistoryEmptyState(
    modifier: Modifier = Modifier,
    onMakePaymentClick: () -> Unit // Acción opcional para invitar a pagar
) {
    // Usamos Box para centrar todo el contenido en la pantalla
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp), // Margen externo para que no toque bordes
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // --- Icono Ilustrativo (Círculo con recibo y reloj) ---
            PaymentEmptyIcon()

            Spacer(modifier = Modifier.height(32.dp)) // Espaciado generoso

            // --- Título Principal ---
            Text(
                text = "Sin historial de pagos",
                style = MaterialTheme.typography.headlineSmall, // headlineSmall (aprox 24sp)
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )


            Spacer(modifier = Modifier.height(16.dp)) // Espaciado corto

            // --- Texto Descriptivo ---
            Text(
                text = "Aquí aparecerán tus pagos realizados en Paguito. ¡Realiza tu primer pago para comenzar!",
                style = MaterialTheme.typography.bodyLarge, // bodyLarge (aprox 16sp)
                color = PaymentHistoryEmptyColors.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp, // Mejorar legibilidad de líneas múltiples
                modifier = Modifier.padding(horizontal = 16.dp) // Padding extra para el texto
            )

            Spacer(modifier = Modifier.height(40.dp)) // Espaciado largo antes del botón
        }
    }
}

/**
 * Componente que recrea el icono para el Empty State.
 * Usamos Box para centrar el icono dentro del círculo de fondo.
 */
@Composable
private fun PaymentEmptyIcon() {
    Box(
        modifier = Modifier
            .size(120.dp) // Tamaño del círculo de fondo
            .clip(CircleShape)
            .background(PaymentHistoryEmptyColors.iconContainer),
        contentAlignment = Alignment.Center
    ) {
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
                    stroke = SolidColor(PaymentHistoryEmptyColors.iconContainer),
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
                .background(PaymentHistoryEmptyColors.iconTint),
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
}

// --- Vista previa en Android Studio ---
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF) // Fondo blanco para contraste
@Composable
fun PaymentHistoryEmptyStatePreview() {
    MaterialTheme {
        PaymentHistoryEmptyState(
            onMakePaymentClick = {} // Qué hacer al pulsar el botón
        )
    }
}