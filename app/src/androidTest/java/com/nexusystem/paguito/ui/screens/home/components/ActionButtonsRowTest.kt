package com.nexusystem.paguito.ui.screens.home.components

import androidx.compose.material3.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.ui.theme.PaguitoTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActionButtonsRowTest {
    @get:Rule
    val compose = createComposeRule()

    // Rendering FlowRow with the APK's actual libraries reproduces the reported
    // NoSuchMethodError when compile/runtime Compose versions diverge.
    @Test
    fun actionsRenderAndRespectFreePlanLimits() {
        var website = 0
        var payment = 0
        var debtor = 0
        var product = 0
        var sale = 0
        var limit = 0
        compose.setContent {
            PaguitoTheme(darkTheme = false) {
                Surface {
                    ActionButtonsRow(
                        openWebsite = { website++ }, registerPayment = { payment++ },
                        registerProduct = { product++ }, registerDebtor = { debtor++ },
                        registerCampaing = {}, registerNewSell = { sale++ },
                        sumary1 = DeudoresSummary(10, 0f), isSucriptionActive = false,
                        showDialogLimitFree = { limit++ }, numeroProductos = 10
                    )
                }
            }
        }
        listOf("Sitio\nWeb", "Registrar\nPago", "Nuevo\nDeudor", "Nuevo\nProducto", "Agregar\nVenta").forEach {
            compose.onNodeWithText(it).assertIsDisplayed().performClick()
        }
        compose.runOnIdle {
            assertEquals(1, website)
            assertEquals(1, payment)
            assertEquals(1, sale)
            assertEquals(0, debtor)
            assertEquals(0, product)
            assertEquals(2, limit)
        }
    }

    @Test
    fun premiumPlanCanOpenClientAndProductActions() {
        var debtor = 0
        var product = 0
        var limit = 0
        compose.setContent {
            PaguitoTheme(darkTheme = true) {
                Surface {
                    ActionButtonsRow(
                        openWebsite = {}, registerPayment = {},
                        registerProduct = { product++ }, registerDebtor = { debtor++ },
                        registerCampaing = {}, registerNewSell = {},
                        sumary1 = DeudoresSummary(10, 0f), isSucriptionActive = true,
                        showDialogLimitFree = { limit++ }, numeroProductos = 10
                    )
                }
            }
        }
        compose.onNodeWithText("Nuevo\nDeudor").assertIsDisplayed().performClick()
        compose.onNodeWithText("Nuevo\nProducto").assertIsDisplayed().performClick()
        compose.runOnIdle {
            assertEquals(1, debtor)
            assertEquals(1, product)
            assertEquals(0, limit)
        }
    }
}
