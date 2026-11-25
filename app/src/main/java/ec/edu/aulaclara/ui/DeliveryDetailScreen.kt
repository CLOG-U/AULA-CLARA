package ec.edu.aulaclara.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
internal fun DeliveryDetailScreen(
    state: DeliveryDetailUiState,
    working: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val item = state.item
    val d = item?.delivery
    var delete by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = working) { }
    ScreenFrame("Detalle de entrega", { if (!working) onBack() }) { p ->
        PageColumn(Modifier.padding(p)) {
            if (d == null) {
                InfoBox("Esta entrega ya no está disponible.")
                SecondaryButton("Volver a la agenda", onBack)
            } else {
                if (d.completed) InfoBox("Entrega completada. ¡Un paso más!", AulaIcons.TaskAlt)
                Text("${item.subject?.name.orEmpty()} · ${item.type?.name.orEmpty()}",
                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(d.title, style = MaterialTheme.typography.headlineMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatusPill(if (d.completed) "Completada" else "Pendiente", positive = true)
                    StatusPill("Prioridad ${d.priority.label.lowercase()}")
                }
                if (state.overdue) InfoBox("Entrega vencida. Puedes cambiar su fecha o marcarla como completada.", AulaIcons.Schedule, true)
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
                PrimaryButton(if (d.completed) "Marcar pendiente" else "Marcar completada", onToggle,
                    Modifier.testTag("toggle_completed"), AulaIcons.TaskAlt, enabled = !working)
                SecondaryButton("Editar entrega", onEdit, Modifier.testTag("edit_delivery"), enabled = !working)
                TextButton({ delete = true }, Modifier.fillMaxWidth().heightIn(min = 48.dp).testTag("delete_delivery"),
                    enabled = !working, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Eliminar entrega") }
            }
        }
    }
    if (delete && d != null) ConfirmDialog("¿Eliminar entrega?", "${d.title} se quitará de tu agenda. Esta acción no se puede deshacer.", "Eliminar entrega", {
        delete = false; onDelete()
    }, { delete = false })
}
