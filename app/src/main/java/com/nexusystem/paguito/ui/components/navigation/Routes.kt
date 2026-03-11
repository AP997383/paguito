// Path: com/beastspinning/medi/ui/components/navigation/Routes.kt
package com.nexusystem.paguito.ui.components.navigation

sealed class Routes(val route: String) {

    /* =====================================================
     *  GRAPHS (✅ estos son los que debe usar el BottomBar)
     * ===================================================== */

    object HomeGraph : Routes("home_graph")
    object ClientesGraph : Routes("clientes_graph")
    object ProfileGraph : Routes("profile_graph")



    /* =====================================================
     *  SCREENS PRINCIPALES (tabs)
     *  (✅ estos son startDestination de cada graph)
     * ===================================================== */

    object ScreenHome : Routes("home")
    object ScreenDeudoresList : Routes("deudores")
    object ScreenCalendar : Routes("calendar")
    object ScreenRecordatorios : Routes("recordatorios")
    object ScreenRecetas : Routes("recetas")
    object ScreenProfile : Routes("profile")

    object ScreenSelectIdiomOnb : Routes("selectIdiomOnb")
    /* =====================================================
     *  FLUJOS INTERNOS / AUXILIARES
     *  (✅ viven dentro del graph correspondiente)
     * ===================================================== */

    object ScreenSplash : Routes("splash")
    object ScreenOnboarding : Routes("onboarding")
    object ScreenIdioms : Routes("idioms")

    // Recordatorios (dentro de RemindersGraph)
    object ScreenNewRecordatorio : Routes("newRecordatorio")

    // Medicamentos (dentro de HomeGraph)
    object ScreenListMedicines : Routes("listMedicines")
    object ScreenMyBotiquin : Routes("MyBotiquin")
    object ScreenMycartilla : Routes("MyCartilla")
    object ScreenResumePresureAndGlucosa : Routes("ResumePresureAndGlucosa")
    object ScreenGlucosaAndPresure : Routes("GlucosaAndPresure")
    object ScreenMyHeartRate : Routes("MyHeartRate")
    object ScreenMyHeartRateMedir : Routes("MyHeartRateMedir")
    object ScreenMyBotiquibDetail : Routes("MyBotiquibDetail")
    object ScreenDetailMedicine : Routes("detailMedicine")
    object ScreenAddNewMedicine : Routes("addNewMedicine")
    object ScreenScanCodeBar : Routes("scanCodeBar")

    // Recetas (dentro de RecipesGraph)
    object ScreenAddNewReceta : Routes("addNewReceta")
    object ScreenDetalleReceta : Routes("detalleReceta")

    // Perfil / Ajustes (dentro de ProfileGraph)
    object ScreenAboutMedi : Routes("aboutMedi")

    // WebView (dentro de ProfileGraph normalmente)
    object ScreenWebView : Routes("webView")
}