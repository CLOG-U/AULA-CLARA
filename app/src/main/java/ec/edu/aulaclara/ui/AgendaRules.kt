package ec.edu.aulaclara.ui

import java.text.Normalizer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.util.Locale

data class Subject(val id: Long, val name: String, val color: Int)
data class ActivityType(val id: Long, val name: String)
enum class Priority(val label: String) { LOW("Baja"), MEDIUM("Media"), HIGH("Alta") }
enum class DeliveryFilter(val label: String) { ALL("Todas"), PENDING("Pendientes"), COMPLETED("Completadas") }
enum class DeliveryOrder(val label: String) { DATE("Fecha más próxima"), PRIORITY("Prioridad más alta"), TITLE("Título A–Z") }
data class Delivery(
    val id: Long,
    val title: String,
    val subjectId: Long,
    val typeId: Long,
    val due: LocalDateTime,
    val priority: Priority = Priority.MEDIUM,
    val notes: String = "",
    val completed: Boolean = false,
)

fun normalizedName(value: String): String = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
    .replace("\\p{M}+".toRegex(), "").replace("\\s+".toRegex(), " ").lowercase(Locale.ROOT)

fun isDuplicateName(name: String, others: List<String>): Boolean = others.any { normalizedName(it) == normalizedName(name) }

fun visibleDeliveries(
    deliveries: List<Delivery>,
    subjects: List<Subject>,
    types: List<ActivityType>,
    query: String = "",
    filter: DeliveryFilter = DeliveryFilter.PENDING,
    subjectId: Long? = null,
    order: DeliveryOrder = DeliveryOrder.DATE,
): List<Delivery> {
    val needle = normalizedName(query)
    val results = deliveries.filter { d ->
        val matchesState = when (filter) {
            DeliveryFilter.ALL -> true
            DeliveryFilter.PENDING -> !d.completed
            DeliveryFilter.COMPLETED -> d.completed
        }
        val searchText = listOf(d.title, d.notes,
            subjects.find { it.id == d.subjectId }?.name.orEmpty(),
            types.find { it.id == d.typeId }?.name.orEmpty()).joinToString(" ")
        matchesState && (subjectId == null || d.subjectId == subjectId) && normalizedName(searchText).contains(needle)
    }
    return when (order) {
        DeliveryOrder.DATE -> results.sortedWith(compareBy<Delivery> { it.due }.thenBy { it.id })
        DeliveryOrder.PRIORITY -> results.sortedWith(compareByDescending<Delivery> { it.priority.ordinal }.thenBy { it.due })
        DeliveryOrder.TITLE -> results.sortedBy { normalizedName(it.title) }
    }
}

fun weeklyPending(deliveries: List<Delivery>, today: LocalDate): Int {
    val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val nextMonday = monday.plusWeeks(1)
    return deliveries.count { !it.completed && !it.due.toLocalDate().isBefore(monday) && it.due.toLocalDate().isBefore(nextMonday) }
}

fun canDeleteSubject(id: Long, deliveries: List<Delivery>) = deliveries.none { it.subjectId == id }
fun canDeleteType(id: Long, deliveries: List<Delivery>) = deliveries.none { it.typeId == id }
