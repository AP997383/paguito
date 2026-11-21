package com.nexusystem.paguito.ui

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Prueba física autorizada: recibe un aviso real, captura su imagen y abre el enlace público.
 * No inicia sesión ni limpia los datos de Paguito. Solo ejecuta acciones en la notificación de prueba.
 */
@RunWith(AndroidJUnit4::class)
class PushMediaDeliveryTest {
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val ui = instrumentation.uiAutomation
    private val title = "Paguito · Prueba con imagen"
    private val output get() = File(instrumentation.targetContext.getExternalFilesDir("push-media-e2e"), "").apply { mkdirs() }

    private fun nodes(root: AccessibilityNodeInfo?): List<AccessibilityNodeInfo> {
        if (root == null) return emptyList()
        val result = mutableListOf(root)
        for (i in 0 until root.childCount) result += nodes(root.getChild(i))
        return result
    }
    private fun notice(): AccessibilityNodeInfo? = nodes(ui.rootInActiveWindow).filter {
        it.text?.toString()?.contains(title) == true || it.contentDescription?.toString()?.contains(title) == true
    }.minByOrNull { val r = Rect(); it.getBoundsInScreen(r); r.width().toLong() * r.height() }
    private fun click(node: AccessibilityNodeInfo) {
        var parent: AccessibilityNodeInfo? = node
        while (parent != null) {
            if (parent.isClickable && parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return
            parent = parent.parent
        }
        val r = Rect(); node.getBoundsInScreen(r)
        val time = SystemClock.uptimeMillis()
        for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
            val event = MotionEvent.obtain(time, SystemClock.uptimeMillis(), action, r.exactCenterX(), r.exactCenterY(), 0)
            event.source = InputDevice.SOURCE_TOUCHSCREEN
            ui.injectInputEvent(event, true); event.recycle()
        }
    }
    private fun capture(name: String, bounds: Rect? = null) {
        val image = requireNotNull(ui.takeScreenshot())
        val clipped = bounds?.let {
            val left = it.left.coerceIn(0, image.width - 1); val top = it.top.coerceIn(0, image.height - 1)
            val width = it.width().coerceAtMost(image.width - left); val height = it.height().coerceAtMost(image.height - top)
            if (width > 0 && height > 0) Bitmap.createBitmap(image, left, top, width, height) else image
        } ?: image
        File(output, "$name.png").outputStream().use { clipped.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
    @Test fun authorizedImageNotificationOpensNexusWebsite() {
        ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        instrumentation.sendStatus(1, Bundle().apply { putString("PAGUITO_MEDIA_TEST_READY", "true") })
        File(output, "ready").writeText("waiting for authorized test notification")
        val deadline = SystemClock.uptimeMillis() + 180_000
        var target: AccessibilityNodeInfo? = null
        while (SystemClock.uptimeMillis() < deadline && target == null) {
            ui.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
            SystemClock.sleep(700)
            target = notice()
            if (target == null) SystemClock.sleep(500)
        }
        assertNotNull("No llegó la notificación autorizada al Motorola", target)
        var card = requireNotNull(target)
        repeat(5) { if (card.viewIdResourceName?.contains("status_bar_latest_event_content") != true && card.parent != null) card = card.parent }
        nodes(card).firstOrNull { it.viewIdResourceName?.endsWith("expand_button") == true }?.let { click(it) }
        SystemClock.sleep(1800)
        target = notice() ?: target
        var expanded = requireNotNull(target)
        repeat(5) { if (expanded.viewIdResourceName?.contains("status_bar_latest_event_content") != true && expanded.parent != null) expanded = expanded.parent }
        val bounds = Rect(); expanded.getBoundsInScreen(bounds)
        capture("android-notificacion-imagen-real", bounds)
        File(output, "notification-expanded").writeText("captured authorized notification")
        click(requireNotNull(target))
        val linkDeadline = SystemClock.uptimeMillis() + 30_000
        var opened = false
        while (SystemClock.uptimeMillis() < linkDeadline && !opened) {
            opened = nodes(ui.rootInActiveWindow).any {
                val id = it.viewIdResourceName.orEmpty()
                (id.contains("url_bar") || id.contains("urlbar") || id.contains("location_bar")) && it.text?.toString()?.contains("nexusecosystem-mx.com") == true
            }
            if (!opened) SystemClock.sleep(500)
        }
        capture("android-enlace-Nexus-abierto")
        assertTrue("La notificación no abrió el dominio de Nexus en el navegador", opened)
        File(output, "link-verified").writeText("https://www.nexusecosystem-mx.com/")
    }
}
