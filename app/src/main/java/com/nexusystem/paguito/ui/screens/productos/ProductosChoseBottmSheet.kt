package com.nexusystem.paguito.ui.screens.productos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.R
import com.nexusystem.paguito.utils.bottomsSheets.BgBottomSheet
import com.nexusystem.paguito.utils.bottomsSheets.BlueLight
import com.nexusystem.paguito.utils.bottomsSheets.BluePrimary
import com.nexusystem.paguito.utils.emptyStates.DynamicEmptyState

// Blanco puro para tarjetas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoseProductBottomSheet(
    onDismiss: () -> Unit,
    onConfirm: (PorductosEntity) -> Unit,
    listaProductos: List<PorductosEntity?>
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
            listaProductos
        )
    }
}

@Composable
fun AbonoBottomSheetContent(
    onDismiss: () -> Unit,
    onConfirm: (PorductosEntity) -> Unit,
    listaProductos: List<PorductosEntity?>
) {
    var searchQuery by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("1,250.00") }
    var isFullPayment by remember { mutableStateOf(true) }
    var sendSms by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Buscador
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Buscar por nombre de producto...", color = TextLightGray, fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = TextLightGray) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor =  MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        if(listaProductos.size>0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(listaProductos) { product ->
                    ProductCardItem(product = product!!,onConfirm,{})
                }
            }
        }else{
            DynamicEmptyState(
                imagePainter = painterResource(id = R.drawable.empty_state_products),
                title = "Aún no tienes productos",
                description = "Comienza a agregar tu inventario para gestionar tus ventas...",
                buttonText = "Agregar Primer Producto",
                buttonIcon = Icons.Default.Add,
                onButtonClick = {  }
            )
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