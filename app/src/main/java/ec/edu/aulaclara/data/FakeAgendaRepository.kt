package ec.edu.aulaclara.data

import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal class FakeAgendaRepository : AgendaRepository {
    private val current = MutableStateFlow(Agenda())
    private val mutex = Mutex()
    private var nextId = 100L
    override val agenda = current.asStateFlow()

    override suspend fun signIn(name: String, email: String, sample: Boolean) = mutex.withLock {
        val session = Session(name.trim().ifBlank { "Estudiante" }, email.trim())
        val types = listOf(ActivityType(1, "Proyecto"), ActivityType(2, "Examen"), ActivityType(3, "Lectura"))
        current.value = if (sample) demoAgenda(session, types) else Agenda(session = session, types = types)
    }

    override suspend fun signOut() = mutex.withLock {
        current.value = Agenda()
    }

    override suspend fun saveDelivery(delivery: Delivery): Long? = write { before ->
        val original = before.deliveries.find { it.id == delivery.id }
        if (delivery.id != 0L && original == null) return@write null
        if (delivery.title.isBlank() ||
            before.subjects.none { it.id == delivery.subjectId } ||
            before.types.none { it.id == delivery.typeId }
        ) return@write null
        val saved = delivery.copy(id = original?.id ?: nextId++, completed = original?.completed ?: false)
        current.value = before.copy(deliveries = before.deliveries.upsert(saved) { it.id })
        saved.id
    }

    override suspend fun setCompleted(id: Long, completed: Boolean): Boolean = write { before ->
        val original = before.deliveries.find { it.id == id } ?: return@write false
        current.value = before.copy(deliveries = before.deliveries.upsert(original.copy(completed = completed)) { it.id })
        true
    } ?: false

    override suspend fun deleteDelivery(id: Long): Boolean = write { before ->
        if (before.deliveries.none { it.id == id }) return@write false
        current.value = before.copy(deliveries = before.deliveries.filterNot { it.id == id })
        true
    } ?: false

    override suspend fun saveCatalogItem(id: Long?, isType: Boolean, name: String, color: Int): Boolean = write { before ->
        val names = if (isType) before.types.associate { it.id to it.name }
        else before.subjects.associate { it.id to it.name }
        if (name.isBlank() || (id != null && id !in names) ||
            isDuplicateName(name, names.filterKeys { it != id }.values.toList())
        ) return@write false
        val key = id ?: nextId++
        current.value = if (isType) {
            before.copy(types = before.types.upsert(ActivityType(key, name.trim())) { it.id })
        } else {
            before.copy(subjects = before.subjects.upsert(Subject(key, name.trim(), color)) { it.id })
        }
        true
    } ?: false

    override suspend fun deleteCatalogItem(id: Long, isType: Boolean): Boolean = write { before ->
        if (isType) {
            if (before.types.none { it.id == id } || !canDeleteType(id, before.deliveries)) return@write false
            current.value = before.copy(types = before.types.filterNot { it.id == id })
        } else {
            if (before.subjects.none { it.id == id } || !canDeleteSubject(id, before.deliveries)) return@write false
            current.value = before.copy(subjects = before.subjects.filterNot { it.id == id })
        }
        true
    } ?: false

    override suspend fun setTheme(theme: String) = updatePreferences { it.copy(theme = theme) }
    override suspend fun setReminders(enabled: Boolean) = updatePreferences { it.copy(reminders = enabled) }
    override suspend fun setOrder(order: DeliveryOrder) = updatePreferences { it.copy(order = order) }

    private suspend fun updatePreferences(update: (Preferences) -> Preferences) = mutex.withLock {
        val before = current.value
        if (before.session != null) current.value = before.copy(preferences = update(before.preferences))
    }

    private suspend fun <T> write(update: (Agenda) -> T): T? = mutex.withLock {
        if (current.value.session == null) return@withLock null
        delay(120)
        update(current.value)
    }

    private fun <T> List<T>.upsert(value: T, id: (T) -> Long): List<T> =
        if (none { id(it) == id(value) }) this + value
        else map { if (id(it) == id(value)) value else it }

    private fun demoAgenda(session: Session, types: List<ActivityType>): Agenda {
        val today = LocalDate.now()
        return Agenda(
            session = session,
            types = types,
            subjects = listOf(
                Subject(1, "Diseño de apps", 0), Subject(2, "Bases de datos", 1),
                Subject(3, "Interacción", 2), Subject(4, "Estadística", 3),
            ),
            deliveries = listOf(
                Delivery(1, "Maquetado móvil", 1, 1, today.plusDays(1).atTime(18, 0), Priority.HIGH, "Revisar pantallas y flujo principal."),
                Delivery(2, "Informe de bases", 2, 1, today.plusDays(3).atTime(20, 0), Priority.MEDIUM, "Incluir el diagrama de relaciones."),
                Delivery(3, "Lectura unidad 3", 3, 3, today.plusWeeks(1).atTime(16, 0), Priority.LOW),
            ),
        )
    }
}
