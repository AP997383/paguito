package com.nexusystem.paguito.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexus.medi.data.local.entity.DeudoresEntity
import com.nexusystem.paguito.data.local.entity.AbonosDelMes
import com.nexusystem.paguito.data.local.entity.PagoConNombre
import com.nexusystem.paguito.domain.data.DeudoresSummary
import com.nexusystem.paguito.ui.components.paguitoBackground
import com.nexusystem.paguito.ui.screens.analisis.components.*
import com.nexusystem.paguito.ui.screens.deudores.components.*
import com.nexusystem.paguito.ui.screens.home.components.*
import com.nexusystem.paguito.ui.screens.perfil.components.AppearanceSettingsCard
import com.nexusystem.paguito.ui.theme.*
import com.nexusystem.paguito.utils.formatAsCurrency
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class VisualFeaturesTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before fun resetAppearance() {
        context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
            .putString("appearance_accent", AccentColor.Paguito.name)
            .putString("appearance_mode", AppearanceMode.Light.name)
            .putBoolean("appearance_waves", true).commit()
    }

    private fun capture(name: String) {
        val bitmap = compose.onRoot().captureToImage().asAndroidBitmap()
        val file = File(context.getExternalFilesDir("ui-review"), "$name.png")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }

    private fun sampleClients() = listOf(
        DeudoresEntity(id = 1, idRemoteDatabase = "ana", nombre = "Ana Romero", telefono = "5550101", correo = "ana@example.com", montoActualAdeudado = 1250f, inRemote = true),
        DeudoresEntity(id = 2, idRemoteDatabase = "bruno", nombre = "Bruno Ruiz", telefono = "5550202", correo = "bruno@example.com", montoActualAdeudado = 0f, inRemote = true),
        DeudoresEntity(id = 3, idRemoteDatabase = "carla", nombre = "Carla Méndez", telefono = "5550303", correo = "carla@example.com", montoActualAdeudado = 3200f)
    )

    @Test fun appearanceChangesImmediatelyAndPersists() {
        var renderedPrimary: Color? = null
        compose.setContent {
            PaguitoTheme {
                val primary = MaterialTheme.colorScheme.primary
                SideEffect { renderedPrimary = primary }
                Column(Modifier.fillMaxSize().paguitoBackground().verticalScroll(rememberScrollState()).padding(20.dp)) {
                    Text("Perfil y ajustes", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    AppearanceSettingsCard()
                }
            }
        }
        compose.onNodeWithText("Violeta").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(AccentColor.Violet.light, renderedPrimary)
            assertEquals(AccentColor.Violet, AppearancePreferences.read(context.getSharedPreferences("settings", Context.MODE_PRIVATE)).accent)
        }
        compose.onNodeWithText("Hazlo tuyo").performScrollTo()
        capture("appearance-light")
        compose.onNodeWithText("Oscuro").performScrollTo().performClick()
        compose.runOnIdle {
            assertEquals(AccentColor.Violet.dark, renderedPrimary)
            assertEquals(AppearanceMode.Dark, AppearancePreferences.read(context.getSharedPreferences("settings", Context.MODE_PRIVATE)).mode)
        }
        capture("appearance-dark")
        compose.onNodeWithContentDescription("Fondo financiero").performClick()
        compose.runOnIdle {
            assertFalse(AppearancePreferences.read(context.getSharedPreferences("settings", Context.MODE_PRIVATE)).showWaves)
        }
        compose.onNodeWithText("Automático").performClick()
        compose.runOnIdle {
            assertEquals(AppearanceMode.System, AppearancePreferences.read(context.getSharedPreferences("settings", Context.MODE_PRIVATE)).mode)
        }
    }

    @Test fun clientSearchFindsPhoneAndPaymentDoesNotOpenProfile() {
        var paidClient = ""
        var openedClient = ""
        compose.setContent {
            PaguitoTheme {
                ClientDirectoryContent(sampleClients(), onProfile = {}, onAddClient = {},
                    onOpenClient = { openedClient = it.idRemoteDatabase }, onPayment = { paidClient = it.idRemoteDatabase },
                    onSale = {}, onEdit = {}, onDelete = {})
            }
        }
        capture("clients")
        compose.onNodeWithTag("client-search").performScrollTo().performTextInput("5550202")
        compose.onNodeWithTag("client-directory").performScrollToNode(hasText("Bruno Ruiz"))
        compose.onNodeWithText("Bruno Ruiz").assertIsDisplayed()
        compose.onNodeWithContentDescription("Registrar pago de Bruno Ruiz").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("bruno", paidClient); assertEquals("", openedClient) }
        compose.onNodeWithTag("client-bruno").performClick()
        compose.runOnIdle { assertEquals("bruno", openedClient) }
    }

    @Test fun chartSwitchesSeriesAndInspectsADay() {
        val month = YearMonth.now().toString()
        val payments = listOf(
            AbonosDelMes(250, "$month-02", true), AbonosDelMes(1500, "$month-18", true),
            AbonosDelMes(300, "$month-02", false), AbonosDelMes(640, "$month-12", false), AbonosDelMes(2000, "$month-24", false)
        )
        compose.setContent {
            PaguitoTheme {
                Column(Modifier.fillMaxSize().paguitoBackground().padding(20.dp)) {
                    Text("Tus estadísticas", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(20.dp))
                    PaymentTrendCard(payments, month, listOf(month), {})
                }
            }
        }
        compose.onNodeWithTag("trend-amount").assertTextEquals(formatAsCurrency(1750f))
        compose.onNodeWithText("Ventas").performClick()
        compose.onNodeWithTag("trend-amount").assertTextEquals(formatAsCurrency(2940f))
        compose.onNodeWithTag("payment-trend-chart").performTouchInput { click(Offset(width * 0.55f, height * 0.4f)) }
        compose.onNodeWithTag("trend-selection").assertTextContains("Día", substring = true)
        capture("chart")
        compose.onNodeWithText("Ver total").performClick()
        compose.onNodeWithTag("trend-amount").assertTextEquals(formatAsCurrency(2940f))
    }

    @Test fun chartAggregatesOnlySelectedMonthAndSupportsLeapDays() {
        val points = dailyPaymentPoints(listOf(
            AbonosDelMes(50, "2024-02-29", true), AbonosDelMes(100, "2024-02-29T12:30:00", false),
            AbonosDelMes(999, "2024-03-01", true), AbonosDelMes(999, "invalid", true)
        ), "2024-02")
        assertEquals(29, points.size)
        assertEquals(50f, points.sumOf { it.collections.toDouble() }.toFloat(), 0f)
        assertEquals(100f, points.last().sales, 0f)
        assertEquals(listOf("bruno"), filterClients(sampleClients(), " BRUNO@ ", ClientFilter.Settled, false).map { it.idRemoteDatabase })
        assertEquals(listOf("carla", "ana"), filterClients(sampleClients(), "", ClientFilter.Balance, true).map { it.idRemoteDatabase })
    }

    @Test fun homeCardsRenderLiveSummaryAndAllSixActions() {
        val today = LocalDate.now().toString()
        val payments = listOf(AbonosDelMes(1233, today, false), AbonosDelMes(499, today, false), AbonosDelMes(500, today, true))
        var clientsOpened = 0
        compose.setContent {
            PaguitoTheme {
                Column(Modifier.fillMaxSize().paguitoBackground().verticalScroll(rememberScrollState()).padding(20.dp)) {
                    Text("Buenas tardes", style = MaterialTheme.typography.headlineMedium)
                    Text("Todo lo importante, a un toque.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(20.dp))
                    HomeOverviewCard(payments, DeudoresSummary(3, 4450f), 12, true, {}, {})
                    Spacer(Modifier.height(20.dp))
                    Text("Acciones rápidas", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    ActionButtonsRow({}, {}, {}, {}, {}, {}, DeudoresSummary(3, 4450f), true, {}, 12, { clientsOpened++ })
                    RecentPaymentsSection(listOf(PagoConNombre(id = 1, nameDeudor = "Ana Romero", montoAbonado = 1233, fechaAbono = today, isIngreso = false)), {})
                }
            }
        }
        compose.onNodeWithText(formatAsCurrency(1732f)).assertExists()
        capture("home")
        compose.onNodeWithText("Mis\nClientes").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, clientsOpened) }
    }
    @Test fun clientBalanceRankingHandlesNullAndSettledClients() {
        compose.setContent {
            PaguitoTheme {
                Column(Modifier.fillMaxSize().paguitoBackground().padding(20.dp)) {
                    ClientBalancesCard(sampleClients() + listOf(null))
                }
            }
        }
        compose.onNodeWithText("Carla Méndez").assertIsDisplayed()
        compose.onNodeWithText("Ana Romero").assertIsDisplayed()
        compose.onNodeWithText("Bruno Ruiz").assertDoesNotExist()
        compose.onNodeWithText(formatAsCurrency(4450f)).assertIsDisplayed()
        capture("client-balances")
    }

}
