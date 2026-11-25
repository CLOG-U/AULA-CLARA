@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package ec.edu.aulaclara.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ec.edu.aulaclara.data.DeliveryOrder

@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    onTheme: (String) -> Unit,
    onReminders: (Boolean) -> Unit,
    onOrder: (DeliveryOrder) -> Unit,
    onSignOut: () -> Unit,
) {
    var logout by rememberSaveable { mutableStateOf(false) }
    var reminderInfo by rememberSaveable { mutableStateOf(false) }
    PageColumn {
        PageTitle("Ajustes", "Personaliza tu agenda y tu sesión.")
        SectionLabel("APARIENCIA")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("Claro", "Oscuro", "Sistema").forEach { theme -> FilterChip(state.preferences.theme == theme, { onTheme(theme) }, label = { Text(theme) }) }
        }
        SectionLabel("RECORDATORIOS")
        OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(AulaIcons.NotificationsNone, null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f)) {
                    Text("Vista previa de avisos", style = MaterialTheme.typography.titleMedium)
                    Text("24 horas antes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(state.preferences.reminders, { onReminders(it); if (it) reminderInfo = true }, Modifier.testTag("reminders_switch"))
            }
        }
        Text("En esta etapa se demuestra la preferencia visual. Todavía no se programan notificaciones del sistema.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionLabel("ENTREGAS")
        ChoiceField("Orden de entregas", state.preferences.order.label, DeliveryOrder.entries.map { it.label }, { onOrder(DeliveryOrder.entries[it]) })
        SectionLabel("CUENTA DE DEMOSTRACIÓN")
        OutlinedCard(colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(state.session?.name.orEmpty(), style = MaterialTheme.typography.titleMedium)
                Text(state.session?.email.orEmpty(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton({ logout = true }, Modifier.testTag("sign_out"), colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(AulaIcons.Logout, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text("Cerrar sesión")
                }
            }
        }
        InfoBox("Tus cambios se mantienen durante esta sesión y al girar la pantalla. El cierre de sesión limpia la agenda. El almacenamiento permanente llegará en una etapa posterior.")
        Text("Aula Clara · Etapa 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if (logout) AlertDialog(onDismissRequest = { logout = false }, title = { Text("¿Cerrar sesión?") },
        text = { Text("Se eliminarán los datos de esta demostración. Podrás volver a explorar el ejemplo desde el acceso.") },
        confirmButton = { TextButton(onSignOut) { Text("Cerrar sesión") } }, dismissButton = { TextButton({ logout = false }) { Text("Cancelar") } })
    if (reminderInfo) AlertDialog(onDismissRequest = { reminderInfo = false }, title = { Text("Avisos de demostración") },
        text = { Text("Esta opción actualiza la vista previa de la entrega. En esta etapa no solicita permisos ni envía notificaciones reales.") },
        confirmButton = { TextButton({ reminderInfo = false }) { Text("Entendido") } })
}
