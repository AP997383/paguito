package com.nexusystem.paguito.ui.screens.sms

import com.nexusystem.paguito.ui.components.*
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
                .padding(paddingValues).paguitoBackground(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { ScreenHeader("Recordatorios", "Acompaña a tus clientes a tiempo", onBackClick, horizontalPadding = 0.dp) }
            item { SoftCard {
                Icon(Icons.Outlined.Sms, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                Spacer(Modifier.height(12.dp)); Text("Una cobranza más cercana", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp)); Text("Decide en qué momentos acompañar a tus clientes con un recordatorio automático.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp)); Text("${templates.count { it.isEnabled }} recordatorios activos", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            } }

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
    SoftCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(template.title, style = MaterialTheme.typography.titleMedium)
                Text(template.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = template.isEnabled && (isPremuim || !template.requierePremium), onCheckedChange = onSwitchChanged)
        }
        Spacer(Modifier.height(12.dp))
        Text(if (template.requierePremium) "Incluido en Premium" else "Disponible en tu cuenta", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(18.dp)); HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Spacer(Modifier.height(16.dp)); Text("VISTA PREVIA DEL MENSAJE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(10.dp))
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)) {
            Box(Modifier.padding(16.dp)) { FormattedSmsText(template.previewText) }
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