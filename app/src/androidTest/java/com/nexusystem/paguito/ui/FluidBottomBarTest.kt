package com.nexusystem.paguito.ui

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nexusystem.paguito.ui.components.navigation.RoundedBottomBar
import com.nexusystem.paguito.ui.components.navigation.Routes
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Comprueba la barra real con rutas vacías: no modifica sesión, compras ni datos de clientes del dispositivo. */
@RunWith(AndroidJUnit4::class)
class FluidBottomBarTest {
    @get:Rule val compose = createComposeRule()
    private val background = Color(0xFF292D3E)

    /** Monta las mismas rutas de tabs sin iniciar los modelos ni los servicios de la aplicación. */
    private fun showBar() {
        compose.setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF7952B3), surface = Color(0xFFF5F5F5))) {
                val controller = rememberNavController()
                Column(Modifier.fillMaxSize().background(background).testTag("navigation-probe")) {
                    NavHost(controller, startDestination = Routes.HomeGraph.route, modifier = Modifier.weight(1f)) {
                        navigation(startDestination = Routes.ScreenHome.route, route = Routes.HomeGraph.route) {
                            composable(Routes.ScreenHome.route) { Text("Contenido Inicio") }
                        }
                        navigation(startDestination = "probe-clients", route = Routes.ClientesGraph.route) {
                            composable("probe-clients") { Text("Contenido Clientes") }
                        }
                        navigation(startDestination = "probe-products", route = Routes.ProductosGraph.route) {
                            composable("probe-products") { Text("Contenido Productos") }
                        }
                        navigation(startDestination = "probe-sales", route = Routes.SalesGraph.route) {
                            composable("probe-sales") { Text("Contenido Ventas") }
                        }
                        composable(Routes.ScreenProfileTab.route) { Text("Contenido Perfil") }
                    }
                    RoundedBottomBar(controller)
                }
            }
        }
        compose.waitForIdle()
    }

    /** Captura frames de la barra para revisar la transición y comprobar las áreas exteriores. */
    private fun capture(name: String): Bitmap {
        val bitmap = compose.onNodeWithTag("fluid-bottom-bar").captureToImage().asAndroidBitmap()
        val folder = File(InstrumentationRegistry.getInstrumentation().targetContext.externalCacheDir, "navigation-review")
        folder.mkdirs()
        File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return bitmap
    }

    /** Mantener pulsado el tab seleccionado no dibuja halo, polígono ni capa adicional. */
    @Test fun holdingSelectedTabDoesNotChangeItsPixels() {
        showBar()
        compose.mainClock.autoAdvance = false
        val before = capture("press-before")
        compose.onNodeWithText("Inicio").performTouchInput { down(center) }
        compose.mainClock.advanceTimeBy(160)
        val pressed = capture("press-held")
        assertEquals(before.width, pressed.width)
        assertEquals(before.height, pressed.height)
        val original = IntArray(before.width * before.height)
        val holding = IntArray(pressed.width * pressed.height)
        before.getPixels(original, 0, before.width, 0, 0, before.width, before.height)
        pressed.getPixels(holding, 0, pressed.width, 0, 0, pressed.width, pressed.height)
        org.junit.Assert.assertArrayEquals(original, holding)
        compose.onNodeWithText("Inicio").performTouchInput { up() }
    }

    /** La transición completa conserva limpio el margen exterior, sin sombras proyectadas fuera del contorno. */
    @Test fun animationFramesHaveNoOuterShadow() {
        showBar()
        compose.mainClock.autoAdvance = false
        compose.onNodeWithText("Perfil").performClick()
        for (frame in 0..5) {
            compose.mainClock.advanceTimeBy(64)
            val bitmap = capture("motion-$frame")
            for (x in 0 until bitmap.width) {
                assertEquals("Halo exterior en frame $frame, x=$x", background.toArgb(), bitmap.getPixel(x, 1))
            }
        }
        compose.onNodeWithText("Perfil").assertIsSelected()
    }

    /** Cambios rápidos mantienen una sola selección y la ruta final correcta. */
    @Test fun rapidTabChangesKeepNavigationAndSelection() {
        showBar()
        compose.mainClock.autoAdvance = false
        for (tab in listOf("Clientes", "Ventas", "Productos", "Perfil", "Inicio")) {
            compose.onNodeWithText(tab).performClick()
            compose.mainClock.advanceTimeBy(48)
        }
        compose.mainClock.advanceTimeBy(400)
        compose.onNodeWithText("Inicio").assertIsSelected()
        compose.onNodeWithText("Clientes").assertIsNotSelected()
        compose.onNodeWithText("Productos").assertIsNotSelected()
        compose.onNodeWithText("Ventas").assertIsNotSelected()
        compose.onNodeWithText("Perfil").assertIsNotSelected()
        compose.onNodeWithText("Contenido Inicio").assertExists()
        capture("rapid-navigation-settled")
    }
}
