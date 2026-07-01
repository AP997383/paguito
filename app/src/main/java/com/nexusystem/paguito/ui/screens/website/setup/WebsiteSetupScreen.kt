package com.nexusystem.paguito.ui.screens.website.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexus.medi.data.local.entity.PorductosEntity
import com.nexusystem.paguito.R
import com.nexusystem.paguito.ui.components.navigation.view.AppHeader
import com.nexusystem.paguito.ui.screens.website.setup.components.*
import com.nexusystem.paguito.ui.screens.website.setup.viewModel.WebsiteSetupViewModel
import com.nexusystem.paguito.ui.theme.WebsiteGreen
import com.nexusystem.paguito.utils.LoadingOverlay

@Composable
fun WebsiteSetupScreen(
    onBack: () -> Unit,
    initialBusinessName: String = "",
    initialSubdomain: String = "",
    initialWhatsapp: String = "",
    isEditingWebsite: Boolean = false,
    products: List<PorductosEntity>,
    viewModel: WebsiteSetupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(
        initialBusinessName,
        initialSubdomain,
        initialWhatsapp,
        isEditingWebsite
    ) {
        viewModel.setupInitialData(
            businessName = initialBusinessName,
            subdomain = initialSubdomain,
            whatsapp = initialWhatsapp,
            isEditingWebsite = isEditingWebsite
        )
    }

    val websiteURL = viewModel.websiteUrl()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 92.dp, bottom = 40.dp)
        ) {
            WebsiteSetupTopTitle()

            Spacer(modifier = Modifier.height(18.dp))

            WebsiteSetupHeader()

            Spacer(modifier = Modifier.height(18.dp))

            WebsiteSetupHeroCard()

            Spacer(modifier = Modifier.height(20.dp))

            WebsiteBusinessInfoCard(
                businessName = uiState.businessName,
                onBusinessNameChange = viewModel::onBusinessNameChange,
                subdomain = uiState.subdomain,
                onSubdomainChange = viewModel::onSubdomainChange,
                whatsapp = uiState.whatsapp,
                onWhatsappChange = viewModel::onWhatsappChange,
                websiteURL = websiteURL,
                isCheckingSubdomain = uiState.isCheckingSubdomain,
                subdomainAvailable = uiState.subdomainAvailable,
                subdomainMessage = uiState.subdomainMessage,
                isEditingWebsite = uiState.isEditingWebsite,
                onCheckSubdomain = {
                    viewModel.checkSubdomain()
                }
            )

            uiState.productsMessage?.let { message ->
                if (message.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))

            WebsiteExamplePreviewCard()

            Spacer(modifier = Modifier.height(22.dp))

            Button(
                onClick = {
                    viewModel.publishWebsite(products)
                },
                enabled = uiState.businessName.isNotBlank() &&
                        uiState.subdomain.isNotBlank() &&
                        uiState.whatsapp.isNotBlank() &&
                        (uiState.isEditingWebsite || uiState.subdomainAvailable == true) &&
                        !uiState.isPublishing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WebsiteGreen,
                    disabledContainerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
                ),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    text = if (uiState.isEditingWebsite) "Guardar cambios" else "Crear sitio web",
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }
        }

        AppHeader(onBack = onBack)

        if (uiState.showSuccessDialog) {
            AppSuccessWebsiteDialog(
                businessName = uiState.publishedBusinessName,
                websiteURL = uiState.publishedWebsiteURL,
                productsCount = uiState.publishedProductsCount,
                onBack = {
                    viewModel.closeSuccessDialog()
                    onBack()
                }
            )
        }

        LoadingOverlay(
            isLoading = uiState.isPublishing,
            lottieRes = R.raw.loadings
        )
    }
}