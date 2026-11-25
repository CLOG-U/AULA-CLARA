package ec.edu.aulaclara.data

import kotlinx.coroutines.flow.StateFlow

internal data class Session(val name: String, val email: String)

internal data class Preferences(
    val theme: String = "Sistema",
    val reminders: Boolean = false,
    val order: DeliveryOrder = DeliveryOrder.DATE,
)

internal data class Agenda(
    val session: Session? = null,
    val subjects: List<Subject> = emptyList(),
    val types: List<ActivityType> = emptyList(),
    val deliveries: List<Delivery> = emptyList(),
    val preferences: Preferences = Preferences(),
)

internal interface AgendaRepository {
    val agenda: StateFlow<Agenda>
    suspend fun signIn(name: String, email: String, sample: Boolean)
    suspend fun signOut()
    suspend fun saveDelivery(delivery: Delivery): Long?
    suspend fun setCompleted(id: Long, completed: Boolean): Boolean
    suspend fun deleteDelivery(id: Long): Boolean
    suspend fun saveCatalogItem(id: Long?, isType: Boolean, name: String, color: Int): Boolean
    suspend fun deleteCatalogItem(id: Long, isType: Boolean): Boolean
    suspend fun setTheme(theme: String)
    suspend fun setReminders(enabled: Boolean)
    suspend fun setOrder(order: DeliveryOrder)
}
