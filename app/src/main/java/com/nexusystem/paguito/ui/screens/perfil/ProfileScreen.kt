package com.nexusystem.paguito.ui.screens.perfil

import android.app.Activity
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.R
import com.nexusystem.paguito.data.local.entity.SuscriptionsItems
import com.nexusystem.paguito.utils.LoadingOverlay
import com.nexusystem.paguito.utils.PaguitoStore
import com.nexusystem.paguito.utils.dialogs.CuentaEliminacionScreen
import com.nexusystem.paguito.utils.dialogs.DialogState
import com.nexusystem.paguito.utils.dialogs.LogoutConfirmationDialog
import com.nexusystem.paguito.utils.dialogs.SubscriptionStatusDialog
import com.nexusystem.paguito.utils.findActivity
import com.nexusystem.paguito.utils.formatAsCurrency
import com.nexusystem.paguito.utils.languajes
import com.nexusystem.paguito.utils.shimmerEffect
import kotlinx.coroutines.launch

// --- COLORES ---
val RedLogout = Color(0xFFEF4444)
val BluePrimary = Color(0xFF3B82F6)
val TextSecondary = Color(0xFF9CA3AF)
val BorderColor = Color(0xFFE5E7EB)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileScreen(
    logout: () -> Unit,
    editProfile: () -> Unit,
    openChangePassword:(String)->Unit,
    viewModel: PerfiViewModel,
    openIdiomas: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var businessName by remember { mutableStateOf("") }
    var myName by remember { mutableStateOf("") }
    var isSucriptionActive by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }
    var fotoUrl by remember { mutableStateOf("") }
    var isBiometricEnabled by remember { mutableStateOf(true) }
    var isInvited by remember { mutableStateOf(true) }
    val listaSucripciones by viewModel.suscriptions.collectAsState()
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    val activity = context.findActivity()
    val isSuscribedSuccess by viewModel.isSubscribed.collectAsState()
    val isSuscribedError by viewModel.isSubscribedError.collectAsState()
    val accountDelete by viewModel.accountDelete.collectAsState()

    var isDarkMode by remember { mutableStateOf(prefs.getBoolean("dark_mode", false)) }
    val profile = viewModel.profileState
    var closeSessionDialog by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }

    val currentLangCode = prefs.getString("app_language", "es") ?: "es"
    val prefijo = languajes.filter { it.regionCode == currentLangCode }
    val pref = if (prefijo.isNotEmpty()) prefijo[0].prefij else "es"
    val isLoading by viewModel.isLoading.collectAsState()
    val currentLangLabel = remember(pref) {
        when (pref) {
            "en" -> "English"
            "pt" -> "Português"
            else -> "Español"
        }
    }
    if(showDeleteAccount){
        CuentaEliminacionScreen({
            showDeleteAccount =false
        },{
            viewModel.deleteMyAccount()
            showDeleteAccount =false
        })
    }
    LaunchedEffect(accountDelete) {
        if (accountDelete == 1) {
            scope.launch {
                PaguitoStore.setLogout(context)
                logout()
                viewModel.resetDelete()
            }
        }
    }


    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
        isInvited = PaguitoStore.isInvited(context)

    }

    LaunchedEffect(profile) {
        profile?.let {
            businessName = it.bussinesName
            myName = it.fullName
            phone = it.phone
            fotoUrl = it.fotoUrl
            isSucriptionActive =it.userSuscription.isActive
            if(!isSucriptionActive){
                viewModel.getAllSuscriptions()
            }
        }
    }

    if(isSuscribedSuccess){
        SubscriptionStatusDialog(DialogState.SUCCESS,{
            viewModel.resetAllStatusPurchase()
        },{
            viewModel.resetAllStatusPurchase()
        })
    }

    if(isSuscribedError){
        SubscriptionStatusDialog(DialogState.ERROR,{
           viewModel.resetAllStatusPurchase()
        },{
            viewModel.resetAllStatusPurchase()
        })
    }
    if (closeSessionDialog) {
        LogoutConfirmationDialog({
            closeSessionDialog = false
            scope.launch {
                PaguitoStore.setLogout(context)
                logout()
            }
        }, {
            closeSessionDialog = false
        })
    }
    Box{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.profile_settings_title),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isInvited) {
                GuestModeScreen(onLoginClick = { logout() })
            } else {
                ProfileHeaderCard(editProfile, myName, businessName, phone, fotoUrl,isSucriptionActive)

                Spacer(modifier = Modifier.height(24.dp))
                if(isSucriptionActive==false) {
                    // Usamos un Key para que el Shimmer no se reinicie innecesariamente
                    val showShimmer = remember(listaSucripciones) { listaSucripciones.isNullOrEmpty() }

                    if (showShimmer) {
                        // Evita usar Logs directamente en el cuerpo si no es con un Side Effect
                        LaunchedEffect(Unit) {
                            Log.e("SHIMMMER", "SI - Iniciando carga")
                        }

                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.LightGray.copy(alpha = 0.3f))
                                .shimmerEffect()
                        )
                    } else {
                        SubscriptionCarousel(listaSucripciones, {
                            val activity = context as? Activity
                            viewModel.setCurrentProduct(it)
                            activity?.let { viewModel.launchPurchaseFlow(it) }
                        })
                    }

                }
                Spacer(modifier = Modifier.height(24.dp))
                SectionTitle(title = stringResource(R.string.section_app_preferences))
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Language,
                        title = stringResource(R.string.item_language),
                        onClick = { openIdiomas() },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = currentLangLabel, color = TextSecondary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(start = 56.dp))

                    SettingsItem(
                        icon = Icons.Outlined.DarkMode,
                        title = stringResource(R.string.item_dark_mode),
                        onClick = { isDarkMode = !isDarkMode },
                        trailingContent = {
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = {
                                    isDarkMode = it
                                    prefs.edit().putBoolean("dark_mode", it).apply()
                                    activity?.recreate()
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = BluePrimary)
                            )
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(start = 56.dp))

                    /*   SettingsItem(
                           icon = Icons.Outlined.Lock,
                           title = stringResource(R.string.item_biometric),
                           onClick = { isBiometricEnabled = !isBiometricEnabled },
                           trailingContent = {
                               Switch(
                                   checked = isBiometricEnabled,
                                   onCheckedChange = { isBiometricEnabled = it },
                                   colors = SwitchDefaults.colors(checkedTrackColor = BluePrimary)
                               )
                           }
                       )*/
                }
                Spacer(modifier = Modifier.height(10.dp))
                SectionTitle(title = "Cuenta")
                SettingsCard {
                    SettingsItem(
                        icon = Icons.Outlined.Password,
                        title = "Cambiar contraseña",
                        onClick = { openChangePassword(profile!!.email) },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                    HorizontalDivider(color = BorderColor, modifier = Modifier.padding(start = 56.dp))

                    SettingsItem(
                        icon = Icons.Outlined.Delete,
                        title = "Eliminar cuenta",
                        onClick = { showDeleteAccount =true },
                        trailingContent = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surface, modifier = Modifier.padding(start = 56.dp))

                    /*   SettingsItem(
                           icon = Icons.Outlined.Lock,
                           title = stringResource(R.string.item_biometric),
                           onClick = { isBiometricEnabled = !isBiometricEnabled },
                           trailingContent = {
                               Switch(
                                   checked = isBiometricEnabled,
                                   onCheckedChange = { isBiometricEnabled = it },
                                   colors = SwitchDefaults.colors(checkedTrackColor = BluePrimary)
                               )
                           }
                       )*/
                }

                Spacer(modifier = Modifier.height(40.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { closeSessionDialog = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, stringResource(R.string.cd_logout_icon), tint = RedLogout, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.btn_logout), color = RedLogout, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = BuildConfig.VERSION_NAME,
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(10.dp))
                PoweredByNexus()
                Spacer(modifier = Modifier.height(150.dp))
            }
        }
        LoadingOverlay(
            isLoading = isLoading,
            lottieRes = R.raw.loadings
        )
    }


}

@Composable
fun PoweredByNexus(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center // Lo centra en la pantalla
    ) {

        Image(
            painter = painterResource(id = R.drawable.nexus),
            contentDescription = "Nexus Logo",
            modifier = Modifier
                .size(24.dp) // Tamaño discreto para un pie de página
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "By",
            style = MaterialTheme.typography.bodySmall.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Light
            )
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "Nexus Ecosystem",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        )



    }
}

@Composable
fun SubscriptionCarousel(
    subscriptions: List<SuscriptionsItems>, // Tu array de modelos de datos
    onUpgradeClick: (SuscriptionsItems) -> Unit // Callback para manejar la acción
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(subscriptions) { sub ->
            SubscriptionCard(
                subs = sub,
                onUpgradeClick = {
                    // Pasamos la suscripción seleccionada al callback
                    onUpgradeClick(sub)
                }
            )
        }
    }
}

@Composable
fun ProfileHeaderCard(
    editProfile: () -> Unit,
    name: String,
    bussinesName: String,
    email: String, // He cambiado 'phone' por 'email' para coincidir con tu requerimiento
    fotoUrl: String,
    isPremium: Boolean // Nuevo parámetro para mostrar el badge
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila superior: Foto, Información y Botón Editar
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Foto de perfil
                Box(
                    modifier = Modifier.size(70.dp).clip(CircleShape).background(Color(0xFFFCE7F3)),
                    contentAlignment = Alignment.Center
                ) {
                    if (fotoUrl.isEmpty()) {
                        Icon(Icons.Filled.Person, null, tint = Color(0xFFEC4899), modifier = Modifier.size(40.dp))
                    } else {
                        AsyncImage(
                            model = fotoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(70.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Columna de textos
                Column(modifier = Modifier.weight(1f)) {
                    // Badge Premium
                    if (isPremium) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7) // Amarillo claro premium
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, null, tint = Color(0xFFD97706), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PREMIUM", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    Text(text = name, fontSize = 19.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = email, fontSize = 12.sp, color = TextSecondary)
                    Text(text = bussinesName.ifEmpty { "Sin nombre comercial" }, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Editar Perfil (Ahora ocupa el ancho completo abajo)
            OutlinedButton(
                onClick = editProfile,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderColor)
            ) {
                Text("Editar perfil", color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextSecondary,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column { content() }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit = {
        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
    }
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) {
                Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        trailingContent()
    }
}

@Composable
fun SubscriptionCard(subs:SuscriptionsItems,onUpgradeClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, BluePrimary.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Activar Suscripción",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = formatAsCurrency(subs.price),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = BluePrimary
                    )
                    Text(
                        text = "/mes",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            subs.benefits.forEach { benefit ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = BluePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(text = benefit, fontSize = 14.sp, color = Color(0xFF6B7280))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onUpgradeClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text(
                    text = "Adquirir Beneficios",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
fun GuestModeScreen(onLoginClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.AccountCircle, null, modifier = Modifier.size(120.dp), tint = Color.Gray.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(32.dp))
        Text(stringResource(R.string.guest_mode_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.guest_mode_description), textAlign = TextAlign.Center, lineHeight = 24.sp)
        Spacer(modifier = Modifier.height(48.dp))
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
        ) {
            Text(stringResource(R.string.btn_login_now), fontWeight = FontWeight.Bold, color = Color.White)
        }
        TextButton(onClick = { }, modifier = Modifier.padding(top = 8.dp)) {
            Text(stringResource(R.string.btn_later), color = Color.Gray)
        }
    }
}