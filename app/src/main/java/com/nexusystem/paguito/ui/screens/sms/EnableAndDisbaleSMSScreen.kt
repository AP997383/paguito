package com.nexusystem.paguito.ui.screens.sms

import android.app.Activity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ModeEditOutline
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.screens.deudores.DeudoresViewModel
import com.nexusystem.paguito.ui.screens.login.AuthViewModel
import com.nexusystem.paguito.ui.screens.perfil.PerfiViewModel
import com.nexusystem.paguito.utils.PaguitoStore
import com.nexusystem.paguito.utils.dialogs.NoPremiumDialog
import com.nexusystem.paguito.utils.dialogs.SuccessAlertDialog
import com.nexusystem.paguito.utils.dialogs.WhatsappReminderDialog
import kotlinx.coroutines.launch

// Modelo de datos para las plantillas
data class SmsTemplate(
    val id: String,
    val title: String,
    val subtitle: String,
    val previewText: String,
    val isEnabled: Boolean,
    val requierePremium:Boolean
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsRemindersScreen(
    onBackClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onEditClick: (String) -> Unit = {},
    perfilViewModel: AuthViewModel
) {

    val profile = perfilViewModel.profileState

    var showDialogConfirm by remember { mutableStateOf(false) }
    var showNoSucription by remember { mutableStateOf(false) }
    var typeReminderEnableOrDisabel by remember { mutableStateOf(0) }
    var enableOrDisable by remember { mutableStateOf(0) }

    // NUEVO: Guardamos temporalmente el ID y el nuevo estado que el usuario quiere aplicar
    var pendingTemplateId by remember { mutableStateOf<String?>(null) }
    var pendingNewState by remember { mutableStateOf(false) }

    val remindersSucess by perfilViewModel.remindersSucess.collectAsState()

    if(showNoSucription){
        NoPremiumDialog({
            showNoSucription =false
        },{

            showNoSucription =false

        })
    }

    if(remindersSucess){
        SuccessAlertDialog("Recordatorios activados correctamente !",{
            perfilViewModel.resetRemindersSuccess()
        },{
            perfilViewModel.resetRemindersSuccess()
        })
    }
    // Estado de las plantillas (activadas/desactivadas)
    var templates by remember {
        mutableStateOf(
            listOf(
                SmsTemplate(
                    id = "1",
                    title = "Recordatorio Preventivo",
                    subtitle = "1 día antes del vencimiento",
                    previewText = "Hola [Nombre] , mañana toca tu pago de [Monto] . Te invitamos a tenerlo listo. Saludos.",
                    isEnabled = profile?.reminderPreventivo ?: false,
                    requierePremium =true
                ),
                SmsTemplate(
                    id = "2",
                    title = "Aviso de Atraso",
                    subtitle = "Al día siguiente del vencimiento",
                    previewText = "Hola [Nombre] , notamos que tu pago de [Monto] está vencido. Por favor comunícate con nosotros para regularizarlo.",
                    isEnabled = profile?.reminderAtraso ?: false,
                    requierePremium =true
                ),
                SmsTemplate(
                    id = "3",
                    title = "Confirmación de Pago",
                    subtitle = "Inmediatamente al cobrar",
                    previewText = "¡Gracias [Nombre] ! Hemos recibido tu abono de [Monto] correctamente. Tu nuevo saldo es [Saldo] .",
                    isEnabled = profile?.reminderAvisoPago ?: false,
                    requierePremium =false
                )
            )
        )
    }
    if(showDialogConfirm){
        WhatsappReminderDialog(
            onDismissRequest = {
                // Si cancela, NO cambiamos nada y cerramos el popup
                pendingTemplateId = null
                showDialogConfirm = false
            },
            onActivateClick = {
                // Si confirma, ejecutamos la lógica del ViewModel
                if (profile?.RemindersActive == false) {
                    perfilViewModel.crearRecordatorios(typeReminderEnableOrDisabel)
                } else {
                    perfilViewModel.updateMyRemindersStatus(
                        typeReminderEnableOrDisabel,
                        if (enableOrDisable == 1) true else false,
                        false
                    )
                }

                // Y ahora sí, aplicamos el cambio visual a la lista de plantillas
                pendingTemplateId?.let { targetId ->
                    templates = templates.map {
                        if (it.id == targetId) it.copy(isEnabled = pendingNewState) else it
                    }
                }

                pendingTemplateId = null
                showDialogConfirm = false
            },
            enableOrDisable = enableOrDisable,
            typeReminderEnableOrDisabel = typeReminderEnableOrDisabel
        )
    }



    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item{
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp, vertical = 1.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Espacio de separación entre la flecha y el texto
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Recordatorios clientes",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

            }
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Automatiza la cobranza enviando mensajes de texto a tus clientes. Ajusta los textos y decide cuándo enviarlos.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Lista de plantillas
            items(templates) { template ->
                TemplateCard(
                    isPremuim = profile?.userSuscription?.isActive?:false,
                    template = template,
                    onSwitchChanged = { newState ->
                        if(profile?.userSuscription?.isActive==true){
                        pendingTemplateId = template.id
                        pendingNewState = newState
                        typeReminderEnableOrDisabel = template.id.toInt()

                        if (newState) {
                            enableOrDisable = 1
                        } else {
                            enableOrDisable = 2
                        }
                        showDialogConfirm = true
                    } else {
                        if(!template.requierePremium){
                            pendingTemplateId = template.id
                            pendingNewState = newState
                            typeReminderEnableOrDisabel = template.id.toInt()

                            if (newState) {
                                enableOrDisable = 1
                            } else {
                                enableOrDisable = 2
                            }
                            showDialogConfirm = true
                        }else{
                            showNoSucription=true
                        }
                    }

                    },
                    onEditClick = { onEditClick(template.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


@Composable
fun TemplateCard(
    isPremuim: Boolean,
    template: SmsTemplate,
    onSwitchChanged: (Boolean) -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Cabecera de la tarjeta: Título, Subtítulo, Icono Editar y Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = template.subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if(template.requierePremium)
                   IconButton(onClick = onEditClick, modifier = Modifier.size(36.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.premium),
                        contentDescription = "Editar",
                        modifier = Modifier.size(30.dp)
                    )
                }
                // Switch de habilitar / deshabilitar
                Switch(
                    checked = if(isPremuim) template.isEnabled else {if(template.requierePremium) false else template.isEnabled} ,
                    onCheckedChange = onSwitchChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF4A6FFF),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFE5E9F0)
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Etiqueta: VISTA PREVIA DEL SMS
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Sms,
                    contentDescription = null,
                    tint = Color(0xFF838996),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "VISTA PREVIA DEL MENSAJE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF838996),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Caja interna con el texto simulando las "píldoras" de variables
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color =MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp)
            ) {
                // Aquí simulamos el formateo de las etiquetas clave (como [Nombre], [Monto], [Saldo])
                // Renderizado simple de texto con acento en azul para las variables
                FormattedSmsText(text = template.previewText)
            }
        }
    }
}

@Composable
fun FormattedSmsText(text: String) {
    // Reemplazamos los corchetes para simular visualmente las "píldoras" de variables de la app
    // Como un componente rápido, dividimos el texto o mostramos un texto estilizado
    val parts = text.split(Regex("(\\[.*?\\])"))

    // Extraemos las etiquetas coincidentes
    val matches = Regex("\\[(.*?)\\]").findAll(text).map { it.value }.toList()

    Text(
        text = buildString {
            // Texto normal e interactivo simulado
            append(text)
        },
        fontSize = 13.sp,
        color = MaterialTheme.colorScheme.onSurface,
        lineHeight = 20.sp
    )

    // Una forma visual más limpia para simular las cajitas azules de las variables en Compose:
    // (Opcionalmente puedes usar un FlowRow si prefieres pintar cajitas individuales reales)
}