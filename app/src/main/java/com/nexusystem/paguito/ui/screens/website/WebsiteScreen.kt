package com.nexusystem.paguito.ui.screens.website

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusystem.paguito.BuildConfig
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.components.navigation.view.AppHeader
import com.nexusystem.paguito.ui.screens.website.components.AppDeleteWarningDialog
import com.nexusystem.paguito.ui.screens.website.components.WebsiteBenefitCard
import com.nexusystem.paguito.ui.screens.website.components.WebsiteHowItWorksSection
import com.nexusystem.paguito.ui.screens.website.components.WebsitePreviewCard
import com.nexusystem.paguito.ui.screens.website.components.WebsitePublishedPreviewCard
import com.nexusystem.paguito.ui.screens.website.components.WebsiteStatusCard
import com.nexusystem.paguito.ui.screens.website.components.WebsiteToolsCard
import com.nexusystem.paguito.ui.screens.website.viewModel.WebsiteViewModel
import com.nexusystem.paguito.ui.theme.WebsiteDanger
import com.nexusystem.paguito.ui.theme.WebsiteGreen
import com.nexusystem.paguito.utils.LoadingOverlay
import kotlinx.coroutines.delay

@Composable
fun WebsiteScreen(
    onBack: () -> Unit,
    onCreateWebsite: () -> Unit = {},
    onEditWebsite: (
        businessName: String,
        subdomain: String,
        whatsapp: String
    ) -> Unit = { _, _, _ -> },
    viewModel: WebsiteViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    val uiState by viewModel.uiState.collectAsState()

    val profile = remember { viewModel.getUserProfile() }
    val isPremium = profile?.userSuscription?.isActive == true

    var showHowItWorks by remember { mutableStateOf(false) }
    var showCopiedToast by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    val websiteUrl = uiState.publishedWebsite?.subdomain?.let { subdomain ->
        "${BuildConfig.PUBLIC_STORE_URL}/$subdomain"
    } ?: "${BuildConfig.PUBLIC_STORE_URL}/mi-negocio"

    val displayUrl = websiteUrl
        .replace("https://", "")
        .replace("http://", "")

    fun copyUrl() {
        if (!uiState.isWebsiteActive) return
        clipboard.setText(AnnotatedString(websiteUrl))
        showCopiedToast = true
    }

    fun shareUrl() {
        if (!uiState.isWebsiteActive) return

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, websiteUrl)
        }

        context.startActivity(
            Intent.createChooser(intent, "Compartir sitio web")
        )
    }

    fun openUrl() {
        if (!uiState.isWebsiteActive) return
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(top = 104.dp, bottom = 40.dp)
        ) {
            Text(
                text = "Sitio Web",
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (uiState.isWebsiteActive) {
                    "Administra el enlace público de tu catálogo y compártelo con tus clientes."
                } else {
                    "Crea un sitio público para que tus clientes vean tus productos y te contacten por WhatsApp."
                },
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.isWebsiteActive) {
                WebsiteStatusCard(
                    isActive = true,
                    url = displayUrl,
                    onCopyTap = { copyUrl() }
                )

                Spacer(modifier = Modifier.height(18.dp))

                WebsiteToolsCard(
                    isActive = true,
                    onEdit = {
                        uiState.publishedWebsite?.let { website ->
                            onEditWebsite(
                                website.businessName,
                                website.subdomain,
                                website.whatsapp
                            )
                        }
                    },
                    onCopy = { copyUrl() },
                    onShare = { shareUrl() },
                    onOpen = { openUrl() }
                )

                Spacer(modifier = Modifier.height(18.dp))

                WebsitePublishedPreviewCard(
                    businessName = uiState.publishedWebsite?.businessName ?: "Tu negocio",
                    url = displayUrl,
                    products = uiState.previewProducts,
                    onOpen = { openUrl() }
                )

                Spacer(modifier = Modifier.height(18.dp))

                HowItWorksCollapsed(
                    expanded = showHowItWorks,
                    onToggle = { showHowItWorks = !showHowItWorks }
                )

                Spacer(modifier = Modifier.height(18.dp))

                DeleteWebsiteButton(
                    onClick = { showDeleteDialog = true }
                )
            } else {
                WebsitePreviewCard()

                Spacer(modifier = Modifier.height(18.dp))

                WebsiteHowItWorksSection()

                Spacer(modifier = Modifier.height(18.dp))

                WebsiteBenefitCard()

                Spacer(modifier = Modifier.height(20.dp))

                if (isPremium) {
                    Button(
                        onClick = onCreateWebsite,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WebsiteGreen
                        )
                    ) {
                        Text(
                            text = "Crear sitio web",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                } else {
                    PremiumWebsiteNotice()
                }
            }
        }

        AppHeader(onBack = onBack)

        AnimatedVisibility(
            visible = showCopiedToast,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        ) {
            LaunchedEffect(showCopiedToast) {
                delay(1400)
                showCopiedToast = false
            }

            Surface(
                color = Color.Black.copy(alpha = 0.86f),
                shape = CircleShape
            ) {
                Text(
                    text = "Enlace copiado",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                )
            }
        }

        if (showDeleteDialog) {
            AppDeleteWarningDialog(
                title = "¿Eliminar sitio web?",
                message = "Esta acción eliminará tu sitio web público y dejará de estar disponible para tus clientes. Tus productos y demás información no se eliminarán.",
                cancelTitle = "Cancelar",
                deleteTitle = "Eliminar",
                onCancel = {
                    showDeleteDialog = false
                },
                onDelete = {
                    showDeleteDialog = false
                    viewModel.deleteWebsite()
                }
            )
        }

        LoadingOverlay(
            isLoading = uiState.isLoading,
            lottieRes = R.raw.loadings
        )
    }
}

@Composable
private fun PremiumWebsiteNotice() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Disponible con Premium",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Para crear y publicar el sitio web de tu negocio, activa Premium desde la sección Perfil.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun HowItWorksCollapsed(
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Column {
        Card(
            onClick = onToggle,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = WebsiteGreen
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Cómo funciona",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Ver pasos de publicación",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = if (expanded) {
                        Icons.Outlined.KeyboardArrowUp
                    } else {
                        Icons.Outlined.KeyboardArrowDown
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(modifier = Modifier.height(12.dp))
                WebsiteHowItWorksSection()
            }
        }
    }
}

@Composable
private fun DeleteWebsiteButton(
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = WebsiteDanger.copy(alpha = 0.07f),
            contentColor = WebsiteDanger
        ),
        border = BorderStroke(
            1.dp,
            WebsiteDanger.copy(alpha = 0.18f)
        )
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Eliminar sitio web",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}