package com.nexusystem.paguito.utils.bottomsSheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.utils.formatAsCurrency

// --- COLORES ---
val BluePrimary = Color(0xFF4064F6)       // Azul vibrante de los botones
val BlueLight = Color(0xFFEEF2FF)         // Fondo circular del ícono
val TextDark = Color(0xFF111827)          // Texto principal
val TextGray = Color(0xFF6B7280)          // Textos secundarios
val TextLightGray = Color(0xFF9CA3AF)     // Textos más tenues
val BgBottomSheet = Color(0xFFF9FAFB)     // Fondo del bottom sheet (gris muy tenue)
val CardWhite = Color(0xFFFFFFFF)         // Blanco puro para tarjetas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AbonoBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean, Boolean) -> Unit,
    ammount:Float,
    name: String
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BgBottomSheet,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        AbonoBottomSheetContent(
            onDismiss = onDismiss,
            onConfirm = onConfirm,
            ammount,
            name
        )
    }
}

@Composable
fun AbonoBottomSheetContent(
    onDismiss: () -> Unit,
    onConfirm: (String, Boolean, Boolean) -> Unit,
    ammount:Float,
    name: String
) {
    var amount by remember { mutableStateOf(ammount.toString()) }
    var isFullPayment by remember { mutableStateOf(true) }
    var sendSms by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp, top = 8.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                Text(
                    text = "Registrar Abono",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = TextLightGray)) { append("Cliente: ") }
                        withStyle(style = SpanStyle(color = TextDark, fontWeight = FontWeight.SemiBold)) { append(name) }
                    },
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = TextLightGray)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // --- TIPO DE PAGO (Botones) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PaymentTypeOption(
                title = "Pago completo",
                subtitle = formatAsCurrency(ammount),
                isSelected = isFullPayment,
                modifier = Modifier.weight(1f)
            ) {
                amount = ammount.toString()
                isFullPayment = true
            }

            PaymentTypeOption(
                title = "Pago parcial",
                subtitle = "Otro monto",
                isSelected = !isFullPayment,
                modifier = Modifier.weight(1f)
            ) {
                amount ="0"
                isFullPayment = false
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- MONTO A COBRAR ---
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MONTO A COBRAR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextLightGray,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Input personalizado con línea azul
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "$", fontSize = 32.sp, color = TextLightGray, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    BasicTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        textStyle = TextStyle(
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextDark,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(IntrinsicSize.Min)
                    )
                    // Línea azul debajo del texto
                    HorizontalDivider(
                        modifier = Modifier.width(160.dp).padding(top = 4.dp),
                        thickness = 2.dp,
                        color = BluePrimary
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFF3F4F6))
        Spacer(modifier = Modifier.height(24.dp))

        // --- COMPROBANTE SMS (Toggle) ---
   /*     Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardWhite,
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), spotColor = Color(0x1A000000))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ícono circular
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(BlueLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Textos
                Column(modifier = Modifier.weight(1f)) {
                    Text("Comprobante SMS", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextDark)
                    Text("Enviar recibo digital al cliente", fontSize = 12.sp, color = TextLightGray)
                }

                // Switch
                Switch(
                    checked = sendSms,
                    onCheckedChange = { sendSms = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = BluePrimary,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFD1D5DB),
                        uncheckedBorderColor = Color.Transparent
                    )
                )
            }
        }
*/
        Spacer(modifier = Modifier.height(32.dp))
        val cleanAmount = amount.filter { it.isDigit() }

// 2. Usamos toIntOrNull() para evitar que la app truene si el string está vacío o mal formado
        val isButtonEnabled = cleanAmount.isNotEmpty() && (cleanAmount.toIntOrNull() ?: 0) > 0
        // --- BOTÓN CONFIRMAR ---
        Button(
            onClick = { onConfirm(amount, isFullPayment, sendSms) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            enabled = isButtonEnabled
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Confirmar Abono", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// --- COMPONENTES SECUNDARIOS ---

@Composable
fun PaymentTypeOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) BluePrimary else CardWhite
    val titleColor = if (isSelected) Color.White else TextDark
    val subtitleColor = if (isSelected) BlueLight else TextLightGray

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = modifier
            .clickable { onClick() }
            .then(
                // Agrega una sombra tenue solo si NO está seleccionado
                if (!isSelected) Modifier.shadow(2.dp, RoundedCornerShape(12.dp), spotColor = Color(0x1A000000)) else Modifier
            )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = titleColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, fontSize = 12.sp, color = subtitleColor)
        }
    }
}