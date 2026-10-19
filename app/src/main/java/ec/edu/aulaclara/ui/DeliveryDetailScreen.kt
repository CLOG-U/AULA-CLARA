package ec.edu.aulaclara.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime

@Composable
fun DeliveryDetailScreen(state: AgendaState, id: Long, onBack: () -> Unit, onEdit: () -> Unit, onDeleted: () -> Unit, notify: (String) -> Unit) {
    val d = state.deliveries.find { it.id == id }
    var delete by rememberSaveable { mutableStateOf(false) }
    ScreenFrame("Detalle de entrega", onBack) { p ->
        PageColumn(Modifier.padding(p)) {
            if (d == null) {
                InfoBox("Esta entrega ya no está disponible.")
                SecondaryButton("Volver a la agenda", onBack)
            } else {
                if (d.completed) InfoBox("Entrega completada. ¡Un paso más!", AulaIcons.TaskAlt)
                Text("${state.subjects.find { it.id == d.subjectId }?.name.orEmpty()} · ${state.types.find { it.id == d.typeId }?.name.orEmpty()}",
                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(d.title, style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusPill(if (d.completed) "Completada" else "Pendiente", positive = true)
                    StatusPill("Prioridad ${d.priority.label.lowercase()}")
                }
                if (!d.completed && d.due.isBefore(LocalDateTime.now())) InfoBox("Entrega vencida. Puedes cambiar su fecha o marcarla como completada.", AulaIcons.Schedule, true)
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.elevatedCardElevation(0.dp)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(AulaIcons.Event, null, tint = MaterialTheme.colorScheme.primary)
                            Text(fullDate(d.due), style = MaterialTheme.typography.titleMedium)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(AulaIcons.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${d.due.toLocalTime()} · ${if (state.reminders) "aviso 24 h antes, vista previa" else "avisos desactivados"}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                SectionLabel("Descripción")
                Text(d.notes.ifBlank { "Sin notas adicionales." }, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                PrimaryButton(if (d.completed) "Marcar pendiente" else "Marcar completada", {
                    state.save(d.copy(completed = !d.completed))
                    notify(if (d.completed) "Entrega marcada como pendiente" else "Entrega completada")
                }, Modifier.testTag("toggle_completed"), AulaIcons.TaskAlt)
                SecondaryButton("Editar entrega", onEdit, Modifier.testTag("edit_delivery"))
                TextButton({ delete = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("delete_delivery"),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Eliminar entrega") }
            }
        }
    }
    if (delete && d != null) ConfirmDialog("¿Eliminar entrega?", "${d.title} se quitará de tu agenda. Esta acción no se puede deshacer.", "Eliminar entrega", {
        state.deliveries.removeAll { it.id == id }; delete = false; onDeleted(); notify("Entrega eliminada")
    }, { delete = false })
}
