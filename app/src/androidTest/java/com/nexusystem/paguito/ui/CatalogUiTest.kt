package com.nexusystem.paguito.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexus.medi.data.local.entity.*
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.ui.components.*
import com.nexusystem.paguito.ui.screens.deudores.components.CustomerProfileContent
import com.nexusystem.paguito.ui.screens.home.components.TransactionHistoryContent
import com.nexusystem.paguito.ui.screens.productos.ProductCardItem
import com.nexusystem.paguito.ui.screens.productos.components.*
import com.nexusystem.paguito.ui.theme.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CatalogUiTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val today get() = LocalDate.now().toString()

    @Before fun appearance() {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit().putString("appearance_accent", AccentColor.Paguito.name)
            .putString("appearance_mode", AppearanceMode.Light.name).putBoolean("appearance_waves", true).commit()
    }
    @Composable private fun ReviewTheme(content: @Composable () -> Unit) {
        PaguitoTheme { Box(Modifier.fillMaxSize().statusBarsPadding()) { content() } }
    }
    private fun capture(name: String) {
        val file = File(context.getExternalFilesDir("catalog-review"), "$name.png")
        compose.waitForIdle()
        android.os.SystemClock.sleep(500)
        val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    private fun products() = listOf(
        PorductosEntity(id = 1, idRemoteDatabase = "bag", nombre = "Mochila urbana", precioOriginal = 450f, precioConGanancia = 675f, inventario = 8, ventas = 12, notasAdicionales = "Impermeable"),
        PorductosEntity(id = 2, idRemoteDatabase = "shoes", nombre = "Tenis de lino", precioOriginal = 600f, precioConGanancia = 899f, inventario = 2, ventas = 3),
        PorductosEntity(id = 3, idRemoteDatabase = "cup", nombre = "Taza de cerámica", precioOriginal = 80f, precioConGanancia = 120f, inventario = 0)
    )

    @Test fun customerProfileRoutesPaymentsAndStatements() {
        val client = DeudoresEntity(id = 1, idRemoteDatabase = "ana", nombre = "Ana Romero", telefono = "5550101234", correo = "ana@example.com",
            montoActualAdeudado = 600f, montoAcomulado = 1000f, periodicidad = "M", inRemote = true)
        val payment = PagosEntinty(id = 1, idRemoteDatabase = "p1", montoAbonado = 400.0, fechaAbono = today, isIngreso = true, tipoPago = 2)
        val sale = PagosEntinty(id = 2, idRemoteDatabase = "p2", montoAbonado = 1000.0, fechaAbono = today, isIngreso = false)
        var paymentRequested = 0
        var statementRequested = 0
        var opened: Int? = null
        compose.setContent {
            ReviewTheme { CustomerProfileContent(client, listOf(payment, sale), LocalDate.now().plusDays(7).toString(), false,
                onBack = {}, onPayment = { paymentRequested++ }, onCall = {}, onReminder = {}, onStatement = { statementRequested++ },
                onOpenMovement = { opened = it.id }, onReceipt = {}, onDelete = {}) }
        }
        capture("customer-profile")
        compose.onNodeWithTag("customer-register-payment").assertIsDisplayed().assertHasClickAction().performClick()
        compose.onNodeWithText("Estado de cuenta").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, paymentRequested); assertEquals(1, statementRequested) }
        val tag = "movement-${sale.toMovement().key}"
        compose.onNodeWithTag("customer-profile").performScrollToNode(hasTestTag(tag))
        compose.onNodeWithTag(tag).performClick()
        compose.runOnIdle { assertEquals(2, opened) }
        capture("customer-movements")
    }

    @Test fun catalogFiltersStockAndOpensTheSelectedProduct() {
        var opened = ""
        compose.setContent { ReviewTheme { ProductDirectoryContent(products(), onProfile = {}, onAdd = {}, onOpen = { opened = it.idRemoteDatabase }, onDelete = {}) } }
        capture("products")
        compose.onNodeWithTag("product-search").performScrollTo().performTextInput("cerámica")
        compose.onNodeWithText("Agotados").performScrollTo().performClick()
        compose.onNodeWithTag("product-directory").performScrollToNode(hasTestTag("product-cup"))
        compose.onNodeWithTag("product-cup").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals("cup", opened) }
    }

    @Test fun deletingAProductRequiresConfirmation() {
        var deleted = false
        compose.setContent { ReviewTheme { Column(Modifier.fillMaxSize().paguitoBackground().padding(20.dp)) { ProductCardItem(products()[0], {}, { deleted = true }) } } }
        compose.onNodeWithContentDescription("Opciones de Mochila urbana").performClick()
        compose.onNodeWithText("Eliminar producto").performClick()
        compose.onNodeWithText("Confirmar eliminación").assertIsDisplayed()
        compose.runOnIdle { assertFalse(deleted) }
        compose.onNodeWithText("Eliminar", ignoreCase = true).performClick()
        compose.runOnIdle { assertTrue(deleted) }
    }

    @Test fun historyFiltersPaymentsAndExpandsNotes() {
        val payment = PagoConNombre(id = 1, idDeudor = "ana", nameDeudor = "Ana Romero", montoAbonado = 400.0, fechaAbono = today, isIngreso = true, tipoPago = 2, notas = "Anticipo de pedido")
        val sale = PagoConNombre(id = 2, idDeudor = "bruno", nameDeudor = "Bruno Ruiz", montoAbonado = 675.0, fechaAbono = LocalDate.now().minusDays(1).toString(), isIngreso = false, tipoPago = 3)
        compose.setContent { ReviewTheme { TransactionHistoryContent(listOf(payment, sale), {}) } }
        capture("history")
        compose.onNodeWithText("Pagos").performScrollTo().performClick()
        val tag = "movement-${payment.toMovement().key}"
        compose.onNodeWithTag("movement-list").performScrollToNode(hasTestTag(tag))
        compose.onNodeWithTag(tag).performClick()
        compose.onNodeWithText("Anticipo de pedido").assertIsDisplayed()
        compose.onNodeWithTag("movement-list").performScrollToNode(hasTestTag("movement-search"))
        compose.onNodeWithTag("movement-search").performTextInput("no existe")
        compose.onNodeWithTag("movement-list").performScrollToNode(hasText("Sin coincidencias"))
        compose.onNodeWithText("Sin coincidencias").assertIsDisplayed()
    }

    @Test fun productEditorKeepsEnteredValuesAndDisablesDuplicateSave() {
        var state by mutableStateOf(ProductEditorState())
        var saved: ProductEditorState? = null
        var gallery = 0
        var camera = 0
        var keyboard: SoftwareKeyboardController? = null
        compose.setContent {
            ReviewTheme {
                val currentKeyboard = LocalSoftwareKeyboardController.current
                SideEffect { keyboard = currentKeyboard }
                ProductEditorContent(state.copy(canSave = isValidProductInput(state.name, state.cost)), ProductEditorActions(
                    onName = { state = state.copy(name = it) }, onCost = { state = state.copy(cost = it) },
                    onStock = { state = state.copy(stock = it) }, onNotes = { state = state.copy(notes = it) },
                    onPrice = { state = state.copy(finalPrice = it) }, onMode = { state = state.copy(multiplierMode = it) },
                    onMultiplier = { state = state.copy(multiplier = it) }, onPercentage = { state = state.copy(percentage = it) },
                    onGallery = { gallery++ }, onCamera = { camera++ }, onSave = { saved = state }, onBack = {}
                ))
            }
        }
        compose.onNodeWithTag("save-product").assertIsNotEnabled()
        compose.onNodeWithText("Galería").performClick()
        compose.onNodeWithText("Cámara").performClick()
        compose.onNodeWithTag("product-name").performScrollTo().performTextInput("Mochila urbana")
        compose.onNodeWithTag("product-cost").performScrollTo().performTextInput("450")
        compose.onNodeWithTag("save-product").assertIsEnabled()
        compose.onNodeWithText("Porcentaje").performScrollTo().performClick()
        compose.onNodeWithTag("product-final-price").performScrollTo().performTextClearance()
        compose.onNodeWithTag("product-final-price").performTextInput("675.00")
        compose.onNodeWithText("Precio personalizado").assertExists()
        compose.onNodeWithTag("product-stock").performScrollTo().performTextInput("12")
        compose.onNodeWithTag("product-notes").performScrollTo().performTextInput("Color negro")
        compose.onNodeWithTag("save-product").performClick()
        compose.runOnIdle {
            assertEquals("Mochila urbana", saved?.name); assertEquals("450", saved?.cost)
            assertEquals("675.00", saved?.finalPrice); assertEquals("12", saved?.stock); assertEquals("Color negro", saved?.notes)
            assertEquals(false, saved?.multiplierMode); assertEquals(1, gallery); assertEquals(1, camera)
            // A presentation fixture for screenshot review, not a persisted product.
            state = state.copy(profit = "225.00")
        }
        compose.runOnIdle { keyboard?.hide() }
        compose.onNodeWithText("Foto del producto").performScrollTo()
        capture("new-product")
        compose.onNodeWithTag("product-final-price").performScrollTo()
        capture("product-pricing")
        compose.runOnIdle { state = state.copy(saving = true) }
        compose.onNodeWithTag("save-product").assertIsNotEnabled()
    }

    @Test fun existingProductPhotoStillOffersReplacementActions() {
        var gallery = 0
        var camera = 0
        compose.setContent { ReviewTheme { Column(Modifier.fillMaxSize().padding(20.dp)) {
            ProductPhotoCard("content://test/photo", { gallery++ }, { camera++ })
        } } }
        compose.onNodeWithText("Galería").assertIsDisplayed().performClick()
        compose.onNodeWithText("Cámara").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(1, gallery); assertEquals(1, camera) }
    }

    @Test fun stockDateAndInputFiltersHandleEdgeCases() {
        assertEquals(listOf("shoes"), filterProducts(products(), "", StockFilter.Low, ProductSort.Name).map { it.idRemoteDatabase })
        assertEquals(listOf("bag"), filterProducts(products(), "IMPERMEABLE", StockFilter.Available, ProductSort.Name).map { it.idRemoteDatabase })
        assertEquals(listOf("cup"), filterProducts(products(), "", StockFilter.Empty, ProductSort.Price).map { it.idRemoteDatabase })
        val entries = listOf(MovementEntry("old", "Ana", 10.0, "invalid", true, 1), MovementEntry("new", "Bruno", 20.0, today, false, 3, notes = "Entrega"))
        assertEquals(listOf("new", "old"), filterMovements(entries, "", MovementFilter.All).map { it.key })
        assertEquals(listOf("new"), filterMovements(entries, "tarjeta", MovementFilter.Sales).map { it.key })
        assertEquals(listOf("new"), filterMovements(entries, "entrega", MovementFilter.All).map { it.key })
        assertNull(movementDate("invalid"))
        assertFalse(isValidProductInput("", "10")); assertFalse(isValidProductInput("A", "0")); assertFalse(isValidProductInput("A", "-1"))
        assertTrue(isValidProductInput("A", "0.50"))
    }
}
