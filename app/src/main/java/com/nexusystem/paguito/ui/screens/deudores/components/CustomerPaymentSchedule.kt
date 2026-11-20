package com.nexusystem.paguito.ui.screens.deudores.components

import java.time.LocalDate
import java.util.Locale

/**
 * Fecha pendiente según el último periodo exigible, igual que el perfil iOS.
 * Un abono cubre su intervalo semanal, quincenal o mensual; no todo el mes calendario.
 * Los meses se calculan desde la fecha original para conservar el día después de febrero.
 * Devuelve null cuando no hay fecha o periodicidad válidas, o el crédito es abierto.
 */
fun customerNextDueDate(initial: LocalDate?, period: String, paymentDates: List<LocalDate>, today: LocalDate = LocalDate.now()): LocalDate? {
    if (initial == null || initial.isAfter(today)) return initial?.let { start -> when (period.uppercase(Locale.ROOT)) {
        "S", "SEMANAL", "WEEKLY" -> start.plusWeeks(1)
        "Q", "QUINCENAL", "BIWEEKLY" -> start.plusDays(15)
        "M", "MENSUAL", "MONTHLY" -> start.plusMonths(1)
        else -> null
    } }
    fun due(index: Long): LocalDate? = when (period.uppercase(Locale.ROOT)) {
        "S", "SEMANAL", "WEEKLY" -> initial.plusWeeks(index)
        "Q", "QUINCENAL", "BIWEEKLY" -> initial.plusDays(index * 15)
        "M", "MENSUAL", "MONTHLY" -> initial.plusMonths(index)
        else -> null
    }
    var index = 1L
    var current = due(index) ?: return null
    if (today.isBefore(current)) return current
    var following = due(index + 1) ?: return null
    while (!following.isAfter(today)) {
        index++
        current = following
        following = due(index + 1) ?: return null
    }
    val covered = paymentDates.any { !it.isBefore(current) && it.isBefore(following) && !it.isAfter(today) }
    return if (covered) following else current
}
