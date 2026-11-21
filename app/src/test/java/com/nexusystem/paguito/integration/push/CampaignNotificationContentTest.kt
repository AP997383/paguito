package com.nexusystem.paguito.integration.push

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Los avisos pueden abrir sitios públicos, pero nunca esquemas ejecutables ni destinos locales. */
class CampaignNotificationContentTest {
    @Test fun acceptsPublicHttpsWithoutChangingItsPath() {
        assertEquals("https://nexusecosystem-mx.com/paguito?campaign=1", CampaignNotificationContent.publicUrl("https://nexusecosystem-mx.com/paguito?campaign=1"))
    }

    @Test fun rejectsUnsafeNotificationLinks() {
        listOf(null, "javascript:alert(1)", "http://example.com", "https://127.0.0.1/test", "https://example.local", "https://user:secret@example.com", "https://[::ffff:192.168.1.1]/").forEach {
            assertNull(CampaignNotificationContent.publicUrl(it))
        }
    }
}
