package com.nexusystem.paguito.ui

import android.graphics.Bitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexusystem.paguito.MainActivity
import androidx.compose.ui.semantics.SemanticsActions
import com.nexusystem.paguito.utils.SecureStorageManager
import com.nexusystem.paguito.utils.OnboardingStore
import com.nexusystem.paguito.utils.PaguitoStore
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class RealNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun capture(name: String) {
        compose.waitForIdle(); android.os.SystemClock.sleep(500)
        val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(context.getExternalFilesDir("navigation-review"), "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    @Test fun realGuestNavigationOpensFormsAndPreservesTabs() {
        val storage = SecureStorageManager(context)
        val savedProfile = storage.getUserProfile()
        storage.clearUserData()
        val wasLogged = runBlocking { PaguitoStore.isLoged(context) }
        val wasInvited = runBlocking { PaguitoStore.isInvited(context) }
        runBlocking { OnboardingStore.setOnboardingSeen(context); PaguitoStore.setLoged(context); PaguitoStore.setInvited(context) }
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            compose.waitUntil(15000) { compose.onAllNodesWithText("Inicio").fetchSemanticsNodes().isNotEmpty() }
            if (compose.onAllNodesWithTag("guide-skip").fetchSemanticsNodes().isNotEmpty()) compose.onNodeWithTag("guide-skip").performClick()
            compose.onNodeWithText("Ventas").performClick()
            compose.onNodeWithText("Tu actividad comercial").assertExists()
            capture("real-sales")
            compose.onNodeWithTag("new-sale", useUnmergedTree = true).performClick()
            compose.onNodeWithText("Nueva venta").assertExists()
            capture("real-new-sale")
            compose.onNodeWithContentDescription("Volver").performClick()
            compose.onNodeWithText("Tu actividad comercial").assertExists()
            compose.onNodeWithText("Inicio").performClick()
            compose.onNodeWithText("Registrar\nPago").performScrollTo().performClick()
            compose.onNodeWithText("Registrar pago").assertExists()
            capture("real-payment")
            compose.onNodeWithContentDescription("Volver").performClick()
            compose.onNodeWithText("Nuevo\nDeudor").performScrollTo().performClick()
            compose.onNodeWithText("Nuevo cliente").assertExists()
            if (compose.onAllNodesWithText("¡Entendido!").fetchSemanticsNodes().isNotEmpty()) compose.onNodeWithText("¡Entendido!").performClick()
            capture("real-new-client")
            compose.onNodeWithContentDescription("Volver").performClick()
            compose.onNodeWithText("Perfil").performClick()
            compose.onNodeWithContentDescription("Volver").assertDoesNotExist()
            capture("real-profile-tab")
            compose.onNodeWithText("Ventas").performClick()
            compose.onNodeWithText("Tu actividad comercial").assertExists()
            compose.onNodeWithText("Inicio").performClick()
            compose.onNodeWithText("Sitio\nWeb").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
            compose.onNodeWithText("Inicia sesión para consultar el sitio de tu cuenta.").assertExists()
            compose.onNodeWithText("Crear sitio web").assertDoesNotExist()
        } finally {
            scenario.close()
            if (savedProfile != null) storage.saveUserProfile(savedProfile) else storage.clearUserData()
            runBlocking {
                if (wasLogged) PaguitoStore.setLoged(context) else PaguitoStore.setLogout(context)
                if (wasInvited) PaguitoStore.setInvited(context) else PaguitoStore.setInvitedNot(context)
            }
        }
    }
}
