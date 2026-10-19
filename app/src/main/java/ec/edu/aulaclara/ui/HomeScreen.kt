@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package ec.edu.aulaclara.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.time.DayOfWeek

@Composable
fun HomeScreen(state: AgendaState, onNew: () -> Unit, onAll: () -> Unit, onDetail: (Long) -> Unit) {
    val today = LocalDate.now()
    val pending = state.deliveries.filter { !it.completed }.sortedBy { it.due }
    PageColumn {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("AULA CLARA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(6.dp))
                PageTitle("Hola, ${state.name.substringBefore(' ')}", today.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Spanish)).replaceFirstChar { it.titlecase(Spanish) })
            }
            Spacer(Modifier.width(12.dp)); BrandMark(48)
        }
        if (state.deliveries.isEmpty()) {
            EmptyContent("Todo por organizar", "Añade una entrega con su materia y fecha.\nAquí verás qué hacer a continuación.", "Crear primera entrega", onNew)
        } else {
            val monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(7) { i ->
                    val date = monday.plusDays(i.toLong())
                    val selected = date == today
                    Column(Modifier.weight(1f).background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface, RoundedCornerShape(13.dp))
                        .padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(listOf("L", "M", "X", "J", "V", "S", "D")[i], style = MaterialTheme.typography.labelSmall,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(date.dayOfMonth.toString(), fontWeight = FontWeight.Bold,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                Column(Modifier.fillMaxWidth().padding(22.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ESTA SEMANA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    val count = weeklyPending(state.deliveries, today)
                    Text("$count ${if (count == 1) "entrega pendiente" else "entregas pendientes"}", style = MaterialTheme.typography.titleLarge)
                    Text("Un paso a la vez. Tú puedes.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            val overdue = pending.count { it.due.isBefore(LocalDateTime.now()) }
            if (overdue > 0) InfoBox("Tienes $overdue ${if (overdue == 1) "entrega vencida" else "entregas vencidas"}. Revisa su fecha o márcala como completada.", AulaIcons.Schedule, error = true)
            PrimaryButton("Nueva entrega", onNew, Modifier.testTag("new_delivery"), AulaIcons.Add)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Próximas entregas", Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                TextButton(onAll) { Text("Ver todas") }
            }
            if (pending.isEmpty()) InfoBox("¡Todo al día! Tus entregas están completadas.", AulaIcons.TaskAlt)
            pending.take(2).forEach { d -> DeliveryCard(d, state.subjects.find { it.id == d.subjectId }, state.types.find { it.id == d.typeId }) { onDetail(d.id) } }
            if (pending.isEmpty()) SecondaryButton("Ver entregas completadas", onAll)
        }
    }
}
