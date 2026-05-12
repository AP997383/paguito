package com.nexusystem.paguito.ui.screens.payments

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalMall
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nexus.medi.data.local.entity.PagosEntinty
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.ui.screens.deudores.BorderColor
import com.nexusystem.paguito.ui.screens.deudores.IconBgGray
import com.nexusystem.paguito.ui.screens.deudores.TextDark2
import com.nexusystem.paguito.ui.screens.deudores.TextLightGray
import com.nexusystem.paguito.ui.screens.perfil.RedLogout
import com.nexusystem.paguito.ui.screens.perfil.TextSecondary
import com.nexusystem.paguito.ui.screens.productos.ChoseProductBottomSheet
import com.nexusystem.paguito.ui.screens.productos.ProductosViewModel
import com.nexusystem.paguito.utils.formatAsCurrency
import org.json.JSONArray

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DetalleVentaScreen(ventaPago : PagosEntinty, productosViewModel: ProductosViewModel,pagosViewModel: PagosViewModel,finish:()->Unit) {
    val listaBusquedaProductos by productosViewModel.produtosList.collectAsState()
    val selectedProducts = remember { mutableStateListOf<PorductosEntity>() }
    val montoRelacionado = remember { mutableStateOf(0) }
    LaunchedEffect(ventaPago.jsonAbonoPorProducto) {
        if (!ventaPago.jsonAbonoPorProducto.isNullOrEmpty()) {
            val listType = object : TypeToken<ArrayList<PorductosEntity>>() {}.type
            val listaRecuperada: ArrayList<PorductosEntity> =try{ Gson().fromJson(ventaPago.jsonAbonoPorProducto, listType)}catch (e: Exception){arrayListOf<PorductosEntity>()}
            listaRecuperada.forEach {
                montoRelacionado.value +=it.precioConGanancia.toInt()
            }
            // Limpiamos la lista antes de añadir, por si acaso el JSON cambió
            selectedProducts.clear()
            selectedProducts.addAll(listaRecuperada)
        }
    }
    var showListProducts by remember { mutableStateOf(false) }

    if(showListProducts){
        ChoseProductBottomSheet({showListProducts = false},{
            showListProducts =false
            selectedProducts.add(it)
            montoRelacionado.value += it.precioConGanancia.toInt()
        },listaBusquedaProductos)
    }
    LaunchedEffect(Unit) {
        productosViewModel.obtenerProductos()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", modifier = Modifier.clickable{
                finish()
            })
            Spacer(modifier = Modifier.width(16.dp))
            Text("Detalle de Venta", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tarjeta de Información Principal
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Fecha de venta", color = Color.Gray, fontSize = 12.sp)
                Text(ventaPago.fechaAbono, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Monto de la transacción", color = Color.Gray, fontSize = 12.sp)
                Text("$"+ventaPago.montoAbonado, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Sección de productos
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Productos relacionados", fontWeight = FontWeight.Bold)
            Badge { Text(selectedProducts.size.toString()) }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = { showListProducts = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+ Relacionar producto")
        }

        Spacer(modifier = Modifier.height(16.dp))
        if(!selectedProducts.isNullOrEmpty()){
            ListaProductos(productos = selectedProducts)
        }else{
            EmptyStateRelatedProductsCard()
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer Total
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total relacionado", color = Color.Gray)
            Text("$"+montoRelacionado.value+" / $"+ventaPago.montoAbonado, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val jsonCompleto: String = Gson().toJson(selectedProducts)
                ventaPago.jsonAbonoPorProducto = jsonCompleto
                pagosViewModel.updateventa(ventaPago)
                finish()
                      },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F0F0)),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Confirmar cambios", color = Color.Black)
        }
    }
}

fun jsonArrayToList(jsonArray: JSONArray): List<PorductosEntity> {
    val gson = Gson()

    // 1. Convertimos el JSONArray a String
    val jsonString = jsonArray.toString()

    // 2. Definimos el tipo de lista para Gson (List<PorductosEntity>)
    val listType = object : TypeToken<List<PorductosEntity>>() {}.type

    // 3. Convertimos el string a la lista
    return gson.fromJson(jsonString, listType)
}

@Composable
fun EmptyStateRelatedProductsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp), // Bordes redondeados de la card
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 24.dp), // Espaciado interno
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Icono central con fondo circular
            Box(
                modifier = Modifier
                    .size(64.dp) // Tamaño del círculo de fondo
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    // Puedes reemplazar este icono por un drawable personalizado
                    imageVector = Icons.Default.AddBox,
                    contentDescription = null, // No es necesario para accesibilidad aquí si el título es claro
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp) // Tamaño del icono interno
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // Espacio entre icono y título

            // 2. Título principal
            Text(
                text = "Sin productos relacionados",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp)) // Espacio entre título y subtítulo

            // 3. Subtítulo o descripción
            Text(
                text = "Aún no has relacionado productos con esta venta. Relaciona los productos vendidos para llevar un registro más detallado.",
                fontSize = 14.sp,
                lineHeight = 20.sp, // Para mejorar la legibilidad en múltiples líneas
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Composable
fun ListaProductos(productos: List<PorductosEntity>) {
    Column {
        productos.forEachIndexed { index, producto ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                productItem(producto,isLast = producto == productos.last(),{},{})
            }
        }
    }
}

@Composable
fun productItem(
    transaction: PorductosEntity,
    isLast: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp)
    ) {
        // --- COLUMNA IZQUIERDA: Ícono y Línea conectora ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(1.dp, BorderColor, CircleShape)
                    .background(IconBgGray, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LocalMall, null, tint = TextDark2, modifier = Modifier.size(16.dp))
            }
            if (!isLast) {
                Box(modifier = Modifier.width(1.dp).weight(1f).background(BorderColor))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // --- COLUMNA DERECHA: Foto, Info y Acciones ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = if (isLast) 0.dp else 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Foto del producto
            AsyncImage(
                model = transaction.urlFoto,
                contentDescription = null,
                modifier = Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IconBgGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Nombre y Precio (Vertical)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.nombre,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1976D2)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatAsCurrency(transaction.precioConGanancia),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            // 3. Botones de acción (Editar / Eliminar)
            Row {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Outlined.Delete, null, tint = RedLogout, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun ProductoItem(producto: PorductosEntity) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen con badge de cantidad si existe
            Box(contentAlignment = Alignment.BottomEnd) {
                Surface(modifier = Modifier.size(60.dp), color = Color(0xFFF5F5F5), shape = RoundedCornerShape(8.dp)) {}
                producto.inventario?.let {
                    Surface(color = Color.White, shape = RoundedCornerShape(4.dp)) {
                        Text("x$it", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(2.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.PriceCheck, contentDescription = null, modifier = Modifier.size(14.dp))
                    Text("Costo: ${producto.precioConGanancia}", fontSize = 12.sp, color = Color.Gray)
                }
            }

            IconButton(onClick = {}) { Icon(Icons.Default.Edit, null, tint = Color.Gray) }
            IconButton(onClick = {}) { Icon(Icons.Default.Delete, null, tint = Color.Gray) }
        }
    }
}