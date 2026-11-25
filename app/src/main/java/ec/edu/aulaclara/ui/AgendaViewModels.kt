package ec.edu.aulaclara.ui

import android.util.Patterns
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ec.edu.aulaclara.data.ActivityType
import ec.edu.aulaclara.data.Agenda
import ec.edu.aulaclara.data.AgendaRepository
import ec.edu.aulaclara.data.Delivery
import ec.edu.aulaclara.data.DeliveryFilter
import ec.edu.aulaclara.data.DeliveryOrder
import ec.edu.aulaclara.data.Preferences
import ec.edu.aulaclara.data.Session
import ec.edu.aulaclara.data.Subject
import ec.edu.aulaclara.data.visibleDeliveries
import ec.edu.aulaclara.data.weeklyPending
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
internal data class AppUiState(val signedIn: Boolean, val theme: String)

internal class AppViewModel(repository: AgendaRepository) : ViewModel() {
    private fun state(agenda: Agenda) =
        AppUiState(agenda.session != null, agenda.preferences.theme)

    val uiState = repository.agenda.map(::state)
        .stateIn(viewModelScope, SharingStarted.Eagerly, state(repository.agenda.value))
}

@Immutable
internal data class AuthUiState(
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val confirmation: String = "",
    val attempted: Boolean = false,
    val submitting: Boolean = false,
) {
    val validEmail get() = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val validPassword get() = password.length >= 8
}

internal class AuthViewModel(private val repository: AgendaRepository, private val register: Boolean) : ViewModel() {
    private val form = MutableStateFlow(AuthUiState())
    val uiState = form.asStateFlow()

    fun setName(value: String) = form.update { it.copy(name = value) }
    fun setEmail(value: String) = form.update { it.copy(email = value) }
    fun setPassword(value: String) = form.update { it.copy(password = value) }
    fun setConfirmation(value: String) = form.update { it.copy(confirmation = value) }

    fun submit() {
        form.update { it.copy(attempted = true) }
        val input = form.value
        if (!input.validEmail || !input.validPassword ||
            (register && (input.name.isBlank() || input.password != input.confirmation))
        ) return
        enter(if (register) input.name else input.email.trim().substringBefore('@'), input.email, false)
    }

    fun exploreDemo() = enter("Carlos Ortiz", "carlos@ejemplo.com", true)

    private fun enter(name: String, email: String, sample: Boolean) {
        if (form.value.submitting) return
        form.update { it.copy(password = "", confirmation = "", attempted = false, submitting = true) }
        viewModelScope.launch { repository.signIn(name, email, sample) }
    }
}

@Immutable
internal data class DeliveryItem(val delivery: Delivery, val subject: Subject?, val type: ActivityType?)

private fun Agenda.item(delivery: Delivery) = DeliveryItem(
    delivery, subjects.find { it.id == delivery.subjectId }, types.find { it.id == delivery.typeId },
)

@Immutable
internal data class HomeUiState(
    val name: String,
    val today: LocalDate,
    val empty: Boolean,
    val weeklyPending: Int,
    val overdue: Int,
    val upcoming: List<DeliveryItem>,
)

internal class HomeViewModel(repository: AgendaRepository) : ViewModel() {
    private fun state(agenda: Agenda): HomeUiState {
        val now = LocalDateTime.now()
        val today = now.toLocalDate()
        val pending = agenda.deliveries.filterNot { it.completed }.sortedBy { it.due }
        return HomeUiState(
            agenda.session?.name.orEmpty().substringBefore(' '), today, agenda.deliveries.isEmpty(),
            weeklyPending(agenda.deliveries, today), pending.count { it.due.isBefore(now) },
            pending.take(2).map(agenda::item),
        )
    }

    val uiState = repository.agenda.map(::state)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), state(repository.agenda.value))
}

@Immutable
internal data class DeliveryFilters(
    val query: String = "",
    val status: DeliveryFilter = DeliveryFilter.PENDING,
    val subjectId: Long? = null,
)

@Immutable
internal data class DeliveryListUiState(
    val filters: DeliveryFilters,
    val subjects: List<Subject>,
    val order: DeliveryOrder,
    val empty: Boolean,
    val visible: List<DeliveryItem>,
)

internal class DeliveryListViewModel(repository: AgendaRepository) : ViewModel() {
    private val filters = MutableStateFlow(DeliveryFilters())

    private fun state(agenda: Agenda, selected: DeliveryFilters): DeliveryListUiState {
        val criteria = selected.copy(subjectId = selected.subjectId?.takeIf { id -> agenda.subjects.any { it.id == id } })
        val order = agenda.preferences.order
        val visible = visibleDeliveries(
            agenda.deliveries, agenda.subjects, agenda.types, criteria.query, criteria.status, criteria.subjectId, order,
        )
        return DeliveryListUiState(criteria, agenda.subjects, order, agenda.deliveries.isEmpty(), visible.map(agenda::item))
    }

    val uiState = combine(repository.agenda, filters, ::state)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), state(repository.agenda.value, filters.value))

    fun setQuery(query: String) = filters.update { it.copy(query = query) }
    fun applyFilters(status: DeliveryFilter, subjectId: Long?) = filters.update { it.copy(status = status, subjectId = subjectId) }
    fun clearFilters() { filters.value = DeliveryFilters(status = DeliveryFilter.ALL) }
}

@Immutable
internal data class DeliveryDetailUiState(val item: DeliveryItem?, val reminders: Boolean, val overdue: Boolean)

internal class DeliveryDetailViewModel(private val repository: AgendaRepository, private val id: Long) : ActionViewModel() {
    private fun state(agenda: Agenda): DeliveryDetailUiState {
        val delivery = agenda.deliveries.find { it.id == id }
        return DeliveryDetailUiState(
            delivery?.let(agenda::item), agenda.preferences.reminders,
            delivery != null && !delivery.completed && delivery.due.isBefore(LocalDateTime.now()),
        )
    }

    val uiState = repository.agenda.map(::state)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), state(repository.agenda.value))

    fun toggleCompleted() {
        val delivery = repository.agenda.value.deliveries.find { it.id == id } ?: return
        val completed = !delivery.completed
        perform {
            if (!repository.setCompleted(id, completed)) ScreenAction.Message("La entrega ya no está disponible.")
            else ScreenAction.Message(if (completed) "Entrega completada" else "Entrega marcada como pendiente")
        }
    }

    fun delete() = perform {
        if (repository.deleteDelivery(id)) ScreenAction.Deleted
        else ScreenAction.Message("La entrega ya no está disponible.")
    }
}

@Immutable
internal data class CatalogRow(val id: Long, val name: String, val count: Int, val color: Int = 0)
@Immutable
internal data class CatalogUiState(val typesTab: Boolean, val rows: List<CatalogRow>)

internal class CatalogViewModel(repository: AgendaRepository) : ViewModel() {
    private val typesTab = MutableStateFlow(false)

    private fun state(agenda: Agenda, types: Boolean): CatalogUiState {
        val rows = if (types) {
            agenda.types.map { type -> CatalogRow(type.id, type.name, agenda.deliveries.count { it.typeId == type.id }) }
        } else {
            agenda.subjects.map { subject ->
                CatalogRow(subject.id, subject.name, agenda.deliveries.count { it.subjectId == subject.id }, subject.color)
            }
        }
        return CatalogUiState(types, rows)
    }

    val uiState = combine(repository.agenda, typesTab, ::state)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), state(repository.agenda.value, false))

    fun selectTypes(show: Boolean) { typesTab.value = show }
}

@Immutable
internal data class SettingsUiState(val session: Session?, val preferences: Preferences)

internal class SettingsViewModel(private val repository: AgendaRepository) : ViewModel() {
    private fun state(agenda: Agenda) = SettingsUiState(agenda.session, agenda.preferences)

    val uiState = repository.agenda.map(::state)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), state(repository.agenda.value))

    fun setTheme(theme: String) { viewModelScope.launch { repository.setTheme(theme) } }
    fun setReminders(enabled: Boolean) { viewModelScope.launch { repository.setReminders(enabled) } }
    fun setOrder(order: DeliveryOrder) { viewModelScope.launch { repository.setOrder(order) } }
    fun signOut() { viewModelScope.launch { repository.signOut() } }
}
