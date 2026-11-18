package com.nexusystem.paguito.ui

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.ui.components.*
import com.nexusystem.paguito.ui.components.navigation.*
import com.nexusystem.paguito.ui.screens.payments.*
import com.nexusystem.paguito.ui.screens.perfil.EditProfileContent
import com.nexusystem.paguito.ui.screens.sms.*
import com.nexusystem.paguito.ui.theme.PaguitoTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import kotlinx.coroutines.flow.first

@RunWith(AndroidJUnit4::class)
class NavigationFormsTest {
    @get:Rule val compose = createComposeRule()
    private fun capture(name: String) {
        compose.waitForIdle(); android.os.SystemClock.sleep(400)
        val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir("navigation-review"), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun captureNavigation(name: String) {
        compose.waitForIdle()
        val bitmap = compose.onNodeWithTag("fluid-bottom-bar").captureToImage().asAndroidBitmap()
        val file = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir("navigation-review"), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private val client = DeudoresEntity(id = 5, idRemoteDatabase = "ana", nombre = "Ana Romero")
    private val sales = listOf(
        PagoConNombre(1, "5", "Ana Romero", 600, "2026-11-16", isIngreso = false, idDeudorRemoteDatabase = "ana"),
        PagoConNombre(2, "5", "Ana Romero", 200, "2026-11-17", isIngreso = true, idDeudorRemoteDatabase = "ana"),
        PagoConNombre(3, "5", "Ana Romero", 800, "2026-11-17", isIngreso = false, idDeudorRemoteDatabase = "ana"))

    @Test fun profileTabHidesBackAndAvatarEntryRetainsIt() {
        var showBack by mutableStateOf(false)
        compose.setContent { PaguitoTheme { com.nexusystem.paguito.ui.screens.perfil.ProfileScreenHeader(showBack) {} } }
        compose.onNodeWithContentDescription("Volver").assertDoesNotExist()
        compose.runOnIdle { showBack = true }
        compose.onNodeWithContentDescription("Volver").assertExists()
    }
    @Test fun joinedHistoryDoesNotDuplicateGuestClients() = kotlinx.coroutines.runBlocking {
        val db = androidx.room.Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, com.nexusystem.paguito.data.AppDatabase::class.java).build()
        try {
            val ana = db.deudoresDao().agregarNuevoDeudor(DeudoresEntity(id = null, nombre = "Ana local"))
            val luis = db.deudoresDao().agregarNuevoDeudor(DeudoresEntity(id = null, nombre = "Luis local"))
            db.abonosDao().agregarAbono(com.nexus.medi.data.local.entity.PagosEntinty(id = null, idDeudor = ana.toString(), montoAbonado = 100, isIngreso = false))
            db.abonosDao().agregarAbono(com.nexus.medi.data.local.entity.PagosEntinty(id = null, idDeudor = luis.toString(), montoAbonado = 200, isIngreso = false))
            val records = kotlinx.coroutines.withTimeout(5000) { db.abonosDao().getPagosConNombre().first() }
            assertEquals(2, records.size)
            assertEquals(setOf("Ana local", "Luis local"), records.map { it.nameDeudor }.toSet())
        } finally { db.close() }
    }
    @Test fun salesOnlySortAndResolveLocalAndRemoteClients() {
        assertEquals(listOf(3, 1), salesByDate(sales).map { it.id })
        assertEquals(client, saleClient(sales.first(), listOf(client)))
        assertEquals(client, saleClient(sales.first().copy(idDeudorRemoteDatabase = ""), listOf(client)))
        assertNull(saleClient(sales.first().copy(idDeudorRemoteDatabase = "deleted"), listOf(client)))
    }
    @Test fun salesOpenClientAndStartNewSale() {
        var opened = ""; var created = false
        compose.setContent { PaguitoTheme { Box(Modifier.statusBarsPadding()) { SalesContent(sales, listOf(client), { created = true }, { opened = it.nombre }) } } }
        capture("sales")
        compose.onNodeWithTag("sales-list").performScrollToNode(hasTestTag("movement-${sales[2].toMovement().key}"))
        compose.onNodeWithTag("movement-${sales[2].toMovement().key}").performClick()
        compose.runOnIdle { assertEquals("Ana Romero", opened) }
        compose.onNodeWithTag("new-sale", useUnmergedTree = true).performClick()
        compose.runOnIdle { assertTrue(created) }
    }
    @Test fun passwordCannotSubmitUntilRulesAndConfirmationMatch() {
        var saved = 0
        compose.setContent { PaguitoTheme { Box(Modifier.statusBarsPadding()) {
            var password by remember { mutableStateOf("") }; var confirmation by remember { mutableStateOf("") }
            PasswordEditorContent(password, confirmation, { password = it }, { confirmation = it }, false, {}, { saved++ })
        } } }
        compose.onNodeWithTag("save-password").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("new-password").performScrollTo().performTextInput("Paguito1!")
        compose.onNodeWithTag("confirm-password").performScrollTo().performTextInput("Paguito1!")
        compose.onNodeWithTag("save-password").performScrollTo().assertIsEnabled().performClick()
        compose.runOnIdle { assertEquals(1, saved) }
    }
    @Test fun recoveryValidatesEmailAndBlocksDuplicateSubmission() {
        compose.setContent { PaguitoTheme { Box(Modifier.statusBarsPadding()) {
            var email by remember { mutableStateOf("") }; var loading by remember { mutableStateOf(false) }
            RecoveryContent(email, { email = it }, loading, {}, { loading = true })
        } } }
        capture("recovery")
        compose.onNodeWithTag("send-recovery").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithTag("recovery-email").performScrollTo().performTextInput("ana@example.com")
        compose.onNodeWithTag("send-recovery").performScrollTo().assertIsEnabled().performClick().assertIsNotEnabled()
    }
    @Test fun editProfileShowsExistingDataAndEditsName() {
        compose.setContent { PaguitoTheme { Box(Modifier.statusBarsPadding()) {
            var name by remember { mutableStateOf("Ana Romero") }
            EditProfileContent("Mi tienda", name, "5550101234", "ana@example.com", "", false, {}, { name = it }, {}, {}, {}, {})
        } } }
        capture("edit-profile")
        compose.onNodeWithTag("edit-profile-name").performScrollTo().performTextReplacement("Ana López")
        compose.onNodeWithTag("edit-profile-name").assertTextContains("Ana López")
    }
    @Test fun remindersUseSwitchWithoutLosingPreview() {
        var changed = false
        compose.setContent { PaguitoTheme { Box(Modifier.statusBarsPadding().padding(20.dp)) {
            TemplateCard(true, SmsTemplate("1", "Recordatorio preventivo", "Antes del vencimiento", "Hola [Nombre], tu pago está próximo.", false, true), { changed = it }, {})
        } } }
        capture("reminders")
        compose.onNode(isToggleable()).performClick()
        compose.runOnIdle { assertTrue(changed) }
        compose.onNodeWithText("Hola [Nombre], tu pago está próximo.").assertExists()
    }
    @Test fun bottomBarOpensSalesAndProfileWithoutDuplicateEntries() {
        var route = ""
        compose.setContent { PaguitoTheme {
            val nav = rememberNavController()
            Column { Box(Modifier.weight(1f)) {
                NavHost(nav, startDestination = Routes.HomeGraph.route) {
                    listOf(Routes.HomeGraph.route, Routes.AnalisisGraph.route, Routes.ClientesGraph.route, Routes.ProductosGraph.route, Routes.SalesGraph.route, Routes.ScreenProfileTab.route).forEach { target ->
                        composable(target) { SideEffect { route = target }; Text("Pantalla") }
                    }
                }
            }; RoundedBottomBar(nav) {} }
        } }
        compose.onAllNodes(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).assertCountEquals(5)
        capture("bottom-navigation"); captureNavigation("fluid-navigation-home")
        compose.onNodeWithText("Ventas").performClick()
        compose.runOnIdle { assertEquals(Routes.SalesGraph.route, route) }
        capture("bottom-navigation-sales"); captureNavigation("fluid-navigation-sales")
        compose.onNodeWithText("Perfil").performClick()
        compose.runOnIdle { assertEquals(Routes.ScreenProfileTab.route, route) }
        capture("bottom-navigation-profile"); captureNavigation("fluid-navigation-profile")
        compose.onNodeWithText("Perfil").performClick()
        compose.runOnIdle { assertEquals(Routes.ScreenProfileTab.route, route) }
        capture("bottom-navigation-profile"); captureNavigation("fluid-navigation-profile")
    }
}
