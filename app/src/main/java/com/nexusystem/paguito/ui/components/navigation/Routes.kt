// Path: com/nexusystem/paguito/ui/components/navigation/Routes.kt
package com.nexusystem.paguito.ui.components.navigation

import android.net.Uri

sealed class Routes(val route: String) {

    /* =====================================================
     * GRAPHS
     * ===================================================== */

    object HomeGraph : Routes("home_graph")
    object ClientesGraph : Routes("clientes_graph")
    object ProductosGraph : Routes("productos_graph")
    object ProfileGraph : Routes("profile_graph")
    object AnalisisGraph : Routes("analisis_graph")
    object LoginGraph : Routes("login_graph")

    /* =====================================================
     * HOME
     * ===================================================== */

    object ScreenHome : Routes("home")
    object ScreenWebsite : Routes("website")
    object ScreenWebsiteSetup : Routes(
        "websiteSetup?businessName={businessName}&subdomain={subdomain}&whatsapp={whatsapp}"
    ) {
        fun createRoute(
            businessName: String,
            subdomain: String,
            whatsapp: String
        ): String {
            return "websiteSetup" +
                    "?businessName=${Uri.encode(businessName)}" +
                    "&subdomain=${Uri.encode(subdomain)}" +
                    "&whatsapp=${Uri.encode(whatsapp)}"
        }

        fun createEmptyRoute(): String {
            return "websiteSetup"
        }
    }
    object ScreenViewAllDeudores : Routes("viewAllDeudores")
    object ScreenViewAllPayments : Routes("viewAllPayments")
    object ScreenRegisterPayment : Routes("registerPayment")
    object ScreenRegisterSell : Routes("registerSell")
    object ScreenDetalledeVenta : Routes("detalleVenta")
    object ScreenPreviewTicket : Routes("previewTicket")
    object ScreenPreviewTicketAccountState : Routes("previewTicketAccountState")

    /* =====================================================
     * CLIENTES / DEUDORES
     * ===================================================== */

    object ScreenDeudoresList : Routes("deudores")
    object ScreenPerfilDeudor : Routes("perfilDeudor")
    object ScreenAddNewDebtor : Routes("newDebtor")

    /* =====================================================
     * PRODUCTOS
     * ===================================================== */

    object ScreenNuevoProducto : Routes("newProduct")
    object ScreenListaProductos : Routes("listProducts")

    /* =====================================================
     * ANÁLISIS
     * ===================================================== */

    object ScreenAnalisis : Routes("analisis")

    /* =====================================================
     * PERFIL
     * ===================================================== */

    object ScreenProfile : Routes("profile")
    object ScreenPerfilEditar : Routes("editarPerfil")
    object ScreenIdioms : Routes("idioms")
    object ScreenChangePasswordProfile : Routes("changePasswordProfile")
    object ScreenWebView : Routes("webView")

    /* =====================================================
     * AUTH
     * ===================================================== */

    object ScreenLogin : Routes("login")
    object ScreenRegister : Routes("register")
    object ScreenOtp : Routes("otp")
    object ScreenOtpRecovery : Routes("otpRecovery")
    object ScreenRecoveryPassword : Routes("recoveryPassword")
    object ScreenChangePassword : Routes("changePassword")
    object ScreenDownloadInfo : Routes("downloadInfo")

    /* =====================================================
     * ONBOARDING / SPLASH / IDIOMA
     * ===================================================== */
    object ScreenOnboarding : Routes("onboarding")

}