package ec.edu.aulaclara.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.Saver
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalDateTime

/** UI state for the stage-2 demonstration. No database, backend or credential storage. */
class AgendaState {
    val subjects = mutableStateListOf<Subject>()
    val types = mutableStateListOf<ActivityType>()
    val deliveries = mutableStateListOf<Delivery>()
    var signedIn by mutableStateOf(false)
    var name by mutableStateOf("Carlos Ortiz")
    var email by mutableStateOf("carlos@ejemplo.com")
    var theme by mutableStateOf("Sistema")
    var reminders by mutableStateOf(false)
    var order by mutableStateOf(DeliveryOrder.DATE)
    private var nextId = 100L
    fun nextId() = nextId++

    fun startSession(userName: String, userEmail: String, sample: Boolean) {
        name = userName.trim().ifBlank { "Estudiante" }
        email = userEmail.trim()
        subjects.clear(); types.clear(); deliveries.clear()
        types.addAll(listOf(ActivityType(1, "Proyecto"), ActivityType(2, "Examen"), ActivityType(3, "Lectura")))
        theme = "Sistema"; reminders = false; order = DeliveryOrder.DATE
        if (sample) {
            subjects.addAll(listOf(Subject(1, "Diseño de apps", 0), Subject(2, "Bases de datos", 1),
                Subject(3, "Interacción", 2), Subject(4, "Estadística", 3)))
            val today = LocalDate.now()
            deliveries.addAll(listOf(
                Delivery(1, "Maquetado móvil", 1, 1, today.plusDays(1).atTime(18, 0), Priority.HIGH, "Revisar pantallas y flujo principal."),
                Delivery(2, "Informe de bases", 2, 1, today.plusDays(3).atTime(20, 0), Priority.MEDIUM, "Incluir el diagrama de relaciones."),
                Delivery(3, "Lectura unidad 3", 3, 3, today.plusWeeks(1).atTime(16, 0), Priority.LOW),
            ))
        }
        signedIn = true
    }
    fun signOut() {
        signedIn = false; subjects.clear(); types.clear(); deliveries.clear()
        name = ""; email = ""; reminders = false; theme = "Sistema"; order = DeliveryOrder.DATE
    }
    fun save(delivery: Delivery) {
        val index = deliveries.indexOfFirst { it.id == delivery.id }
        if (index < 0) deliveries.add(delivery) else deliveries[index] = delivery
    }
    fun deleteSubject(id: Long): Boolean {
        if (!canDeleteSubject(id, deliveries)) return false
        return subjects.removeAll { it.id == id }
    }
    fun deleteType(id: Long): Boolean {
        if (!canDeleteType(id, deliveries)) return false
        return types.removeAll { it.id == id }
    }
    private fun snapshot(): String = JSONObject().apply {
        put("signedIn", signedIn); put("name", name); put("email", email)
        put("theme", theme); put("reminders", reminders); put("order", order.name); put("nextId", nextId)
        put("subjects", JSONArray().apply { subjects.forEach { put(JSONObject().put("id", it.id).put("name", it.name).put("color", it.color)) } })
        put("types", JSONArray().apply { types.forEach { put(JSONObject().put("id", it.id).put("name", it.name)) } })
        put("deliveries", JSONArray().apply { deliveries.forEach {
            put(JSONObject().put("id", it.id).put("title", it.title).put("subject", it.subjectId).put("type", it.typeId)
                .put("due", it.due.toString()).put("priority", it.priority.name).put("notes", it.notes).put("completed", it.completed))
        } })
    }.toString()

    companion object {
        val Saver = Saver<AgendaState, String>(save = { it.snapshot() }, restore = { text ->
            val j = JSONObject(text)
            AgendaState().apply {
                signedIn = j.getBoolean("signedIn"); name = j.getString("name"); email = j.getString("email")
                theme = j.getString("theme"); reminders = j.getBoolean("reminders"); order = DeliveryOrder.valueOf(j.getString("order")); nextId = j.getLong("nextId")
                val s = j.getJSONArray("subjects")
                repeat(s.length()) { s.getJSONObject(it).let { subjects.add(Subject(it.getLong("id"), it.getString("name"), it.getInt("color"))) } }
                val t = j.getJSONArray("types")
                repeat(t.length()) { t.getJSONObject(it).let { types.add(ActivityType(it.getLong("id"), it.getString("name"))) } }
                val d = j.getJSONArray("deliveries")
                repeat(d.length()) { d.getJSONObject(it).let { deliveries.add(Delivery(it.getLong("id"), it.getString("title"), it.getLong("subject"),
                    it.getLong("type"), LocalDateTime.parse(it.getString("due")), Priority.valueOf(it.getString("priority")), it.getString("notes"), it.getBoolean("completed"))) } }
            }
        })
    }
}
