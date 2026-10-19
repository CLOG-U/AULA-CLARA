@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package ec.edu.aulaclara.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
fun DeliveryFormScreen(state: AgendaState, id: Long?, onBack: () -> Unit, onSaved: (Long) -> Unit, onNewSubject: () -> Unit, onNewType: () -> Unit) {
    val original = state.deliveries.find { it.id == id }
    if (id != null && original == null) {
        ScreenFrame("Entrega no disponible", onBack) { p -> PageColumn(Modifier.padding(p)) { InfoBox("Esta entrega ya no existe."); SecondaryButton("Volver", onBack) } }
        return
    }
    var title by rememberSaveable { mutableStateOf(original?.title ?: "") }
    var subjectId by rememberSaveable { mutableStateOf(original?.subjectId ?: state.subjects.firstOrNull()?.id) }
    var typeId by rememberSaveable { mutableStateOf(original?.typeId ?: state.types.firstOrNull()?.id) }
    var date by rememberSaveable { mutableStateOf((original?.due?.toLocalDate() ?: LocalDate.now().plusDays(1)).toString()) }
    var time by rememberSaveable { mutableStateOf((original?.due?.toLocalTime() ?: LocalTime.of(18, 0)).toString()) }
    var priority by rememberSaveable { mutableStateOf(original?.priority ?: Priority.MEDIUM) }
    var notes by rememberSaveable { mutableStateOf(original?.notes ?: "") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    var dateOpen by rememberSaveable { mutableStateOf(false) }
    var timeOpen by rememberSaveable { mutableStateOf(false) }
    var discard by rememberSaveable { mutableStateOf(false) }
    // Values remain in this back-stack entry when a missing catalog is created.
    LaunchedEffect(state.subjects.size) { if (subjectId == null) subjectId = state.subjects.firstOrNull()?.id }
    LaunchedEffect(state.types.size) { if (typeId == null) typeId = state.types.firstOrNull()?.id }
    val initialDate = (original?.due?.toLocalDate() ?: LocalDate.now().plusDays(1)).toString()
    val initialTime = (original?.due?.toLocalTime() ?: LocalTime.of(18, 0)).toString()
    val dirty = title != (original?.title ?: "") || notes != (original?.notes ?: "") || date != initialDate || time != initialTime ||
        priority != (original?.priority ?: Priority.MEDIUM) || subjectId != (original?.subjectId ?: state.subjects.firstOrNull()?.id) ||
        typeId != (original?.typeId ?: state.types.firstOrNull()?.id)
    val exit: () -> Unit = { if (dirty) discard = true else onBack() }
    BackHandler(enabled = dirty) { discard = true }
    ScreenFrame(if (id == null) "Nueva entrega" else "Editar entrega", exit) { p ->
        PageColumn(Modifier.padding(p).imePadding()) {
            OutlinedTextField(title, { title = it }, Modifier.fillMaxWidth().testTag("delivery_title"), label = { Text("Título") },
                singleLine = true, isError = attempted && title.isBlank(),
                supportingText = if (attempted && title.isBlank()) ({ Text("Escribe un título para continuar.") }) else null)
            if (state.subjects.isEmpty()) {
                InfoBox("Primero crea una materia. Conservaremos los datos de esta entrega.")
                SecondaryButton("Crear primera materia", onNewSubject)
            } else {
                ChoiceField("Materia", state.subjects.find { it.id == subjectId }?.name.orEmpty(), state.subjects.map { it.name },
                    { i -> subjectId = state.subjects[i].id }, attempted && subjectId == null)
                TextButton(onNewSubject) { Icon(AulaIcons.Add, null, Modifier.size(18.dp)); Text("Añadir materia") }
            }
            if (state.types.isEmpty()) {
                InfoBox("Crea un tipo de actividad para clasificar tu entrega.")
                SecondaryButton("Crear primer tipo", onNewType)
            } else ChoiceField("Tipo de actividad", state.types.find { it.id == typeId }?.name.orEmpty(), state.types.map { it.name },
                { i -> typeId = state.types[i].id }, attempted && typeId == null)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton({ dateOpen = true }, Modifier.heightIn(min = 56.dp).testTag("date_selector"), shape = MaterialTheme.shapes.small) {
                    Icon(AulaIcons.CalendarMonth, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                    Text(LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy", Spanish)))
                }
                OutlinedButton({ timeOpen = true }, Modifier.heightIn(min = 56.dp).testTag("time_selector"), shape = MaterialTheme.shapes.small) {
                    Icon(AulaIcons.Schedule, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(time.take(5))
                }
            }
            val due = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time))
            if (due.isBefore(LocalDateTime.now())) InfoBox("Esta fecha ya pasó. La entrega se mostrará como vencida si está pendiente.", AulaIcons.Schedule)
            Column {
                SectionLabel("Prioridad")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Priority.entries.forEach { value -> FilterChip(priority == value, { priority = value }, label = { Text(value.label) }) }
                }
            }
            OutlinedTextField(notes, { notes = it }, Modifier.fillMaxWidth().testTag("delivery_notes"), label = { Text("Notas opcionales") }, minLines = 3, maxLines = 6)
            PrimaryButton(if (id == null) "Guardar entrega" else "Guardar cambios", {
                attempted = true
                if (title.isNotBlank() && state.subjects.any { it.id == subjectId } && state.types.any { it.id == typeId }) {
                    val savedId = id ?: state.nextId()
                    state.save(Delivery(savedId, title.trim(), subjectId!!, typeId!!, due, priority, notes.trim(), original?.completed ?: false))
                    onSaved(savedId)
                }
            }, Modifier.testTag("save_delivery"), AulaIcons.Check)
            SecondaryButton("Cancelar", exit)
        }
    }
    if (discard) ConfirmDialog("¿Descartar cambios?", "Los cambios de esta entrega todavía no se han guardado.", "Descartar", onBack, { discard = false }, destructive = false)
    if (dateOpen) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = LocalDate.parse(date).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(onDismissRequest = { dateOpen = false },
            confirmButton = { TextButton({ picker.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString() }; dateOpen = false }) { Text("Confirmar fecha") } },
            dismissButton = { TextButton({ dateOpen = false }) { Text("Cancelar") } }) { DatePicker(picker) }
    }
    if (timeOpen) {
        val current = LocalTime.parse(time)
        val picker = rememberTimePickerState(current.hour, current.minute, is24Hour = true)
        AlertDialog(onDismissRequest = { timeOpen = false }, title = { Text("Hora de entrega") },
            text = { BoxWithConstraints { if (maxWidth < 340.dp) TimeInput(picker) else TimePicker(picker) } },
            confirmButton = { TextButton({ time = LocalTime.of(picker.hour, picker.minute).toString(); timeOpen = false }) { Text("Confirmar hora") } },
            dismissButton = { TextButton({ timeOpen = false }) { Text("Cancelar") } })
    }
}

@Composable
fun ChoiceField(label: String, selected: String, choices: List<String>, onSelect: (Int) -> Unit, error: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded, { expanded = it }) {
        OutlinedTextField(selected, {}, Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(), readOnly = true,
            label = { Text(label) }, isError = error, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) })
        ExposedDropdownMenu(expanded, { expanded = false }) {
            choices.forEachIndexed { i, text -> DropdownMenuItem(text = { Text(text) }, onClick = { onSelect(i); expanded = false }) }
        }
    }
}
