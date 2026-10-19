package ec.edu.aulaclara.ui

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime

class AgendaRulesTest {
    private val subjects = listOf(Subject(1, "Diseño de apps", 0), Subject(2, "Bases de datos", 1))
    private val types = listOf(ActivityType(1, "Proyecto"), ActivityType(2, "Lectura"))
    private val deliveries = listOf(
        Delivery(1, "Prototipo", 1, 1, LocalDateTime.of(2026, 9, 24, 18, 0), Priority.HIGH),
        Delivery(2, "Informe", 2, 1, LocalDateTime.of(2026, 9, 26, 20, 0), Priority.MEDIUM, completed = true),
        Delivery(3, "Unidad 3", 1, 2, LocalDateTime.of(2026, 9, 28, 16, 0), Priority.LOW),
    )

    @Test fun duplicateNamesIgnoreAccentsCaseAndExtraSpaces() {
        assertTrue(isDuplicateName("  DISENO   de apps ", listOf("Diseño de apps")))
        assertFalse(isDuplicateName("Bases de datos", listOf("Diseño de apps")))
    }
    @Test fun pendingListExcludesCompletedAndSortsByDeadline() {
        assertEquals(listOf(1L, 3L), visibleDeliveries(deliveries.reversed(), subjects, types).map { it.id })
    }
    @Test fun searchCombinesStateSubjectAndTypeWithoutAccents() {
        assertEquals(listOf(1L), visibleDeliveries(deliveries, subjects, types, "diseno", DeliveryFilter.PENDING, 1).filter { it.typeId == 1L }.map { it.id })
        assertEquals(listOf(3L), visibleDeliveries(deliveries, subjects, types, "lectura").map { it.id })
        assertTrue(visibleDeliveries(deliveries, subjects, types, "informe", DeliveryFilter.PENDING).isEmpty())
    }
    @Test fun completedFilterAndAllFilterHaveDistinctResults() {
        assertEquals(listOf(2L), visibleDeliveries(deliveries, subjects, types, filter = DeliveryFilter.COMPLETED).map { it.id })
        assertEquals(3, visibleDeliveries(deliveries, subjects, types, filter = DeliveryFilter.ALL).size)
    }
    @Test fun preferredOrderChangesListWithoutChangingDeadlines() {
        assertEquals(listOf(1L, 2L, 3L), visibleDeliveries(deliveries, subjects, types, filter = DeliveryFilter.ALL, order = DeliveryOrder.PRIORITY).map { it.id })
        assertEquals(listOf(2L, 1L, 3L), visibleDeliveries(deliveries, subjects, types, filter = DeliveryFilter.ALL, order = DeliveryOrder.TITLE).map { it.id })
    }
    @Test fun weeklySummaryUsesMondayThroughSundayAndExcludesCompleted() {
        assertEquals(1, weeklyPending(deliveries, LocalDate.of(2026, 9, 22)))
        assertEquals(1, weeklyPending(deliveries, LocalDate.of(2026, 9, 27)))
        assertEquals(1, weeklyPending(deliveries, LocalDate.of(2026, 9, 28)))
    }
    @Test fun associatedCatalogsCannotBeDeletedEvenForCompletedDeliveries() {
        assertFalse(canDeleteSubject(2, deliveries))
        assertFalse(canDeleteType(1, deliveries))
        assertTrue(canDeleteSubject(99, deliveries))
        assertTrue(canDeleteType(99, deliveries))
    }
    @Test fun completingDeliveryRemovesItFromPendingAndWeeklySummary() {
        val changed = deliveries.map { if (it.id == 1L) it.copy(completed = true) else it }
        assertEquals(listOf(3L), visibleDeliveries(changed, subjects, types).map { it.id })
        assertEquals(0, weeklyPending(changed, LocalDate.of(2026, 9, 22)))
    }
}
