package ec.edu.aulaclara.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import ec.edu.aulaclara.data.ActivityType
import ec.edu.aulaclara.data.Agenda
import ec.edu.aulaclara.data.AgendaRepository
import ec.edu.aulaclara.data.Delivery
import ec.edu.aulaclara.data.Priority
import ec.edu.aulaclara.data.Subject
import ec.edu.aulaclara.data.isDuplicateName
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Immutable
internal data class DeliveryDraft(
    val title: String,
    val subjectId: Long?,
    val typeId: Long?,
    val date: LocalDate,
    val time: LocalTime,
    val priority: Priority,
    val notes: String,
) {
    val due: LocalDateTime get() = LocalDateTime.of(date, time)
}

@Immutable
internal data class DeliveryFormUiState(
    val id: Long?,
    val missing: Boolean,
    val draft: DeliveryDraft,
    val subjects: List<Subject>,
    val types: List<ActivityType>,
    val attempted: Boolean,
    val dirty: Boolean,
    val pastDue: Boolean,
)

internal class DeliveryFormViewModel(private val repository: AgendaRepository, private val id: Long?) : ActionViewModel() {
    private val original = repository.agenda.value.deliveries.find { it.id == id }
    private val initial = DeliveryDraft(
        original?.title.orEmpty(), original?.subjectId, original?.typeId,
        original?.due?.toLocalDate() ?: LocalDate.now().plusDays(1),
        original?.due?.toLocalTime() ?: LocalTime.of(18, 0),
        original?.priority ?: Priority.MEDIUM, original?.notes.orEmpty(),
    )
    private val draft = MutableStateFlow(initial)
    private val attempted = MutableStateFlow(false)

    private fun withDefaults(value: DeliveryDraft, agenda: Agenda) = value.copy(
        subjectId = value.subjectId?.takeIf { id -> agenda.subjects.any { it.id == id } } ?: agenda.subjects.firstOrNull()?.id,
        typeId = value.typeId?.takeIf { id -> agenda.types.any { it.id == id } } ?: agenda.types.firstOrNull()?.id,
    )

    private fun state(agenda: Agenda, input: DeliveryDraft, submitted: Boolean): DeliveryFormUiState {
        val resolved = withDefaults(input, agenda)
        return DeliveryFormUiState(
            id, id != null && agenda.deliveries.none { it.id == id }, resolved, agenda.subjects, agenda.types,
            submitted, resolved != withDefaults(initial, agenda), resolved.due.isBefore(LocalDateTime.now()),
        )
    }

    val uiState = combine(repository.agenda, draft, attempted, ::state)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), state(repository.agenda.value, initial, false))

    fun updateDraft(update: (DeliveryDraft) -> DeliveryDraft) = draft.update(update)

    fun save() {
        attempted.value = true
        val value = withDefaults(draft.value, repository.agenda.value)
        val subject = value.subjectId ?: return
        val type = value.typeId ?: return
        if (value.title.isBlank()) return
        perform {
            val saved = repository.saveDelivery(Delivery(
                id ?: 0L, value.title.trim(), subject, type, value.due, value.priority, value.notes.trim(),
            ))
            if (saved == null) ScreenAction.Message("No se pudo guardar. Revisa la materia y el tipo.")
            else ScreenAction.Saved(saved)
        }
    }
}

@Immutable
internal data class CatalogDraft(val name: String, val color: Int)

@Immutable
internal data class CatalogEditorUiState(
    val id: Long?,
    val isType: Boolean,
    val draft: CatalogDraft,
    val missing: Boolean,
    val attempted: Boolean,
    val duplicate: Boolean,
    val associated: Int,
    val dirty: Boolean,
)

internal class CatalogEditorViewModel(
    private val repository: AgendaRepository,
    private val id: Long?,
    private val isType: Boolean,
) : ActionViewModel() {
    private val initial = original(repository.agenda.value) ?: CatalogDraft("", 0)
    private val draft = MutableStateFlow(initial)
    private val attempted = MutableStateFlow(false)

    private fun original(agenda: Agenda): CatalogDraft? = if (isType) {
        agenda.types.find { it.id == id }?.let { CatalogDraft(it.name, 0) }
    } else {
        agenda.subjects.find { it.id == id }?.let { CatalogDraft(it.name, it.color) }
    }

    private fun state(agenda: Agenda, input: CatalogDraft, submitted: Boolean): CatalogEditorUiState {
        val names = if (isType) agenda.types.filterNot { it.id == id }.map { it.name }
        else agenda.subjects.filterNot { it.id == id }.map { it.name }
        val associated = agenda.deliveries.count { if (isType) it.typeId == id else it.subjectId == id }
        return CatalogEditorUiState(
            id, isType, input, id != null && original(agenda) == null, submitted,
            input.name.isNotBlank() && isDuplicateName(input.name, names), associated, input != initial,
        )
    }

    val uiState = combine(repository.agenda, draft, attempted, ::state)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), state(repository.agenda.value, initial, false))

    fun setName(name: String) = draft.update { it.copy(name = name) }
    fun setColor(color: Int) = draft.update { it.copy(color = color) }

    fun save() {
        attempted.value = true
        val input = draft.value
        val current = state(repository.agenda.value, input, true)
        if (input.name.isBlank() || current.duplicate || current.missing) return
        perform {
            if (repository.saveCatalogItem(id, isType, input.name, input.color)) ScreenAction.Saved(id ?: 0L)
            else ScreenAction.Message("No se pudieron guardar los cambios.")
        }
    }

    fun delete() {
        val key = id ?: return
        perform {
            if (repository.deleteCatalogItem(key, isType)) ScreenAction.Deleted
            else ScreenAction.Message("No se pudo eliminar. Revisa si el elemento tiene entregas asociadas.")
        }
    }
}
