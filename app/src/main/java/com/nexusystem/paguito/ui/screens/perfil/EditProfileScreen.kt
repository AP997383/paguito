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
    var businessName by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var myName by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
    var phone by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("") }
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
    SuccessDialog("!Datos Actualizados!",{
        viewModel.resetSuccess()
        onBackClick()
    }, {
        viewModel.resetSuccess()
        onBackClick()
    })
}

    Box {
        EditProfileContent(businessName, myName, phone, email,
            imageUri?.toString() ?: newUrlFromServer.ifBlank { profile?.fotoUrl.orEmpty() }, isLoading,
            { businessName = it }, { myName = it }, { phone = it }, onBackClick,
            onSave = {
                secureStorage.getUserProfile()?.let { current ->
                    val updated = current.copy(email = email, fotoUrl = newUrlFromServer.ifBlank { current.fotoUrl },
                        fullName = myName, bussinesName = businessName, phone = phone)
                    secureStorage.saveUserProfile(updated)
                    viewModel.updateAllInfo(email, updated)
                }
            },
            photoPicker = { PhotoPickerSection({ imageUri = it; viewModel.updateProfile(email, it) }, imageUri) })
        LoadingOverlay(isLoading = isLoading, lottieRes = R.raw.loadings)
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
        Icon(Icons.Default.PhotoCamera, contentDescription = "Cambiar foto de perfil")
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
            maxLines = 1,
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