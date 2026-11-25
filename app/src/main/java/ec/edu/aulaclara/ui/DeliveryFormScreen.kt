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
import ec.edu.aulaclara.data.Priority
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Composable
internal fun DeliveryFormScreen(
    state: DeliveryFormUiState,
    working: Boolean,
    onBack: () -> Unit,
    onNewSubject: () -> Unit,
    onNewType: () -> Unit,
    onDraftChange: ((DeliveryDraft) -> DeliveryDraft) -> Unit,
    onSave: () -> Unit,
) {
    if (state.missing) {
        ScreenFrame("Entrega no disponible", onBack) { p -> PageColumn(Modifier.padding(p)) { InfoBox("Esta entrega ya no existe."); SecondaryButton("Volver", onBack) } }
        return
    }
    val (title, subjectId, typeId, date, time, priority, notes) = state.draft
    val attempted = state.attempted
    var dateOpen by rememberSaveable { mutableStateOf(false) }
    var timeOpen by rememberSaveable { mutableStateOf(false) }
    var discard by rememberSaveable { mutableStateOf(false) }
    val exit: () -> Unit = { if (!working) { if (state.dirty) discard = true else onBack() } }
    BackHandler(enabled = state.dirty || working, onBack = exit)
    ScreenFrame(if (state.id == null) "Nueva entrega" else "Editar entrega", exit) { p ->
        PageColumn(Modifier.padding(p).imePadding()) {
            OutlinedTextField(title, { value -> onDraftChange { it.copy(title = value) } }, Modifier.fillMaxWidth().testTag("delivery_title"), label = { Text("Título") },
                singleLine = true, isError = attempted && title.isBlank(),
                supportingText = if (attempted && title.isBlank()) ({ Text("Escribe un título para continuar.") }) else null)
            if (state.subjects.isEmpty()) {
                InfoBox("Primero crea una materia. Conservaremos los datos de esta entrega.")
                SecondaryButton("Crear primera materia", onNewSubject, enabled = !working)
            } else {
                ChoiceField("Materia", state.subjects.find { it.id == subjectId }?.name.orEmpty(), state.subjects.map { it.name },
                    { i -> onDraftChange { it.copy(subjectId = state.subjects[i].id) } }, attempted && subjectId == null)
                TextButton(onNewSubject, enabled = !working) { Icon(AulaIcons.Add, null, Modifier.size(18.dp)); Text("Añadir materia") }
            }
            if (state.types.isEmpty()) {
                InfoBox("Crea un tipo de actividad para clasificar tu entrega.")
                SecondaryButton("Crear primer tipo", onNewType, enabled = !working)
            } else ChoiceField("Tipo de actividad", state.types.find { it.id == typeId }?.name.orEmpty(), state.types.map { it.name },
                { i -> onDraftChange { it.copy(typeId = state.types[i].id) } }, attempted && typeId == null)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton({ dateOpen = true }, Modifier.heightIn(min = 56.dp).testTag("date_selector"), shape = MaterialTheme.shapes.small) {
                    Icon(AulaIcons.CalendarMonth, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp))
                    Text(date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Spanish)))
                }
                OutlinedButton({ timeOpen = true }, Modifier.heightIn(min = 56.dp).testTag("time_selector"), shape = MaterialTheme.shapes.small) {
                    Icon(AulaIcons.Schedule, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)); Text(time.toString().take(5))
                }
            }
            if (state.pastDue) InfoBox("Esta fecha ya pasó. La entrega se mostrará como vencida si está pendiente.", AulaIcons.Schedule)
            Column {
                SectionLabel("Prioridad")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Priority.entries.forEach { value -> FilterChip(priority == value, { onDraftChange { it.copy(priority = value) } }, label = { Text(value.label) }) }
                }
            }
            OutlinedTextField(notes, { value -> onDraftChange { it.copy(notes = value) } }, Modifier.fillMaxWidth().testTag("delivery_notes"), label = { Text("Notas opcionales") }, minLines = 3, maxLines = 6)
            PrimaryButton(if (state.id == null) "Guardar entrega" else "Guardar cambios", onSave,
                Modifier.testTag("save_delivery"), AulaIcons.Check, enabled = !working)
            SecondaryButton("Cancelar", exit, enabled = !working)
        }
    }
    if (discard) ConfirmDialog("¿Descartar cambios?", "Los cambios de esta entrega todavía no se han guardado.", "Descartar", onBack, { discard = false }, destructive = false)
    if (dateOpen) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli())
        DatePickerDialog(onDismissRequest = { dateOpen = false },
            confirmButton = { TextButton({
                picker.selectedDateMillis?.let { millis ->
                    val selected = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    onDraftChange { it.copy(date = selected) }
                }
                dateOpen = false
            }) { Text("Confirmar fecha") } },
            dismissButton = { TextButton({ dateOpen = false }) { Text("Cancelar") } }) { DatePicker(picker) }
    }
    if (timeOpen) {
        val picker = rememberTimePickerState(time.hour, time.minute, is24Hour = true)
        AlertDialog(onDismissRequest = { timeOpen = false }, title = { Text("Hora de entrega") },
            text = { BoxWithConstraints { if (maxWidth < 340.dp) TimeInput(picker) else TimePicker(picker) } },
            confirmButton = { TextButton({ onDraftChange { it.copy(time = LocalTime.of(picker.hour, picker.minute)) }; timeOpen = false }) { Text("Confirmar hora") } },
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
