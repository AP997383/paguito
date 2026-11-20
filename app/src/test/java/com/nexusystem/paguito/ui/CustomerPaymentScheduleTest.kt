package com.nexusystem.paguito.ui

import com.nexusystem.paguito.ui.screens.deudores.components.customerNextDueDate
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class CustomerPaymentScheduleTest {
    private fun date(value: String) = LocalDate.parse(value)
    @Test fun weeklyPaymentDoesNotCoverAnotherWeekInTheSameMonth() {
        assertEquals(date("2026-11-15"), customerNextDueDate(date("2026-11-01"), "S", listOf(date("2026-11-08")), date("2026-11-19")))
    }
    @Test fun paymentInCurrentPeriodAdvancesDueDate() {
        assertEquals(date("2026-11-22"), customerNextDueDate(date("2026-11-01"), "S", listOf(date("2026-11-16")), date("2026-11-19")))
    }
    @Test fun monthlyScheduleKeepsOriginalDayAfterFebruary() {
        assertEquals(date("2026-03-31"), customerNextDueDate(date("2026-01-31"), "M", listOf(date("2026-03-01")), date("2026-03-30")))
    }
    @Test fun firstDueDateIsAfterTheSaleAndUnknownDatesStayUnknown() {
        assertEquals(date("2026-11-16"), customerNextDueDate(date("2026-11-01"), "Q", emptyList(), date("2026-11-02")))
        assertNull(customerNextDueDate(null, "S", emptyList(), date("2026-11-19")))
        assertNull(customerNextDueDate(date("2026-11-01"), "A", emptyList(), date("2026-11-19")))
    }
    @Test fun futurePaymentDoesNotCoverCurrentPeriod() {
        assertEquals(date("2026-11-15"), customerNextDueDate(date("2026-11-01"), "S", listOf(date("2026-11-20")), date("2026-11-19")))
    }
}
