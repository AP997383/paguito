package com.nexusystem.paguito.ui.screens.perfil

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.AsyncImage // Asegúrate de tener la librería Coil para imágenes
import com.nexusystem.paguito.R
import com.nexusystem.paguito.data.local.entity.UserProfileEntity
import com.nexusystem.paguito.utils.LoadingOverlay
import com.nexusystem.paguito.utils.SecureStorageManager
import com.nexusystem.paguito.utils.dialogs.SuccessDialog

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBackClick: () -> Unit = {},viewModel:PerfiViewModel) {
    val context = LocalContext.current
    var businessName by remember { mutableStateOf("") }
    var myName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val secureStorage = SecureStorageManager(context)
    val profile = viewModel.profileState

    val isLoading by viewModel.isLoading.collectAsState()
    val newUrlFromServer by viewModel.newUrlServer.collectAsState()
    val updateSucess by viewModel.updateSuccess.collectAsState()
    var isFirstLoad by remember { mutableStateOf(true) } // Bandera de control

    LaunchedEffect(profile) {
         viewModel.loadUserProfile()
        if (profile != null && isFirstLoad) {
            Log.e("DATA_PROFILE", "Carga inicial --> $profile")
            businessName = profile.bussinesName
            myName = profile.fullName
            phone = profile.phone
            email = profile.email
            isFirstLoad = false // Bloqueamos futuras sobrescripciones automáticas
        }
    }


if(updateSucess) {
    SuccessDialog({
        viewModel.resetSuccess()
        onBackClick()
    }, {
        viewModel.resetSuccess()
        onBackClick()
    })
}

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Editar Perfil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor =MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0.dp)
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, bottom = 50.dp, end = 16.dp, top = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /*TextButton(onClick = { /* Acción eliminar */ }) {
                    Text("Eliminar cuenta", color = Color.Red)
                }*/
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        val profileToSave = UserProfileEntity(
                            email = email,
                            fotoUrl = newUrlFromServer,
                            fullName = myName,
                            bussinesName = businessName,
                            phone = phone,
                            token = "" // Mantener el token de sesión
                        )
                        secureStorage.saveUserProfile(profileToSave)
                        viewModel.updateAllInfo(email,profileToSave)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10CE81) // Verde esmeralda del diseño
                    )
                ) {
                    Text("Guardar cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background // Fondo gris muy claro
    ) { paddingValues ->
        Box{
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Sección de Foto de Perfil
                Box(contentAlignment = Alignment.BottomEnd) {
                    AsyncImage(
                        model = newUrlFromServer, // Reemplazar con URL real
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray),
                        contentScale = ContentScale.Crop
                    )
                    // Botón flotante de cámara
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10CE81),
                        modifier = Modifier
                            .size(36.dp)
                            .padding(2.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                    ) {
                        PhotoPickerSection({
                            imageUri = it
                            viewModel.updateProfile(email,it)
                        },imageUri)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Card de Información del Perfil
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        // Campo Nombre del Negocio
                        CustomInputField(
                            label = "Nombre del negocio",
                            optional = true,
                            value = businessName,
                            onValueChange = { businessName = it },
                            placeholder = "Ej. Mi Tienda Increíble",
                            leadingIcon = Icons.Outlined.Store
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Campo Mi Nombre
                        CustomInputField(
                            label = "Mi nombre",
                            required = true,
                            value = myName,
                            onValueChange = { myName = it },
                            leadingIcon = Icons.Outlined.Person
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Campo Teléfono
                        CustomInputField(
                            label = "Teléfono",
                            required = true,
                            value = phone,
                            onValueChange = { phone = it },
                            leadingIcon = Icons.Outlined.Phone
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Campo Correo (Deshabilitado según diseño)
                        CustomInputField(
                            label = "Correo electrónico",
                            value = email,
                            onValueChange = { },
                            leadingIcon = Icons.Outlined.Email,
                            enabled = false,
                            footerText = "El correo no se puede cambiar desde aquí."
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
            LoadingOverlay(
                isLoading = isLoading,
                lottieRes = R.raw.loadings
            )
        }

    }
}

@Composable
fun PhotoPickerSection(onImagePicked: (Uri) -> Unit, imageUriparam: Uri?) {
    val context = LocalContext.current
    // Launcher para Galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
           // imageUriparam = it
            onImagePicked(it)
        }
    }
    // Launcher para Cámara (requiere configuración de FileProvider)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUriparam?.let { onImagePicked(it) }
        }
    }

    // UI: El botón de la cámara que vimos en tu diseño
    IconButton(onClick = {
        // Aquí podrías mostrar un pequeño BottomSheet para elegir entre Cámara o Galería
        galleryLauncher.launch("image/*")
    }) {
        Icon(Icons.Default.PhotoCamera, contentDescription = null)
    }
}

@Composable
fun CustomInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    required: Boolean = false,
    optional: Boolean = false,
    enabled: Boolean = true,
    footerText: String? = null
) {
    Column {
        Row {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (required) Text(" *", color = Color.Red, fontSize = 14.sp)
            if (optional) Text(" (opcional)", color = Color.Gray, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.LightGray) },
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            enabled = enabled,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10CE81),
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        if (footerText != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(footerText, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}