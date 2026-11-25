@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package ec.edu.aulaclara.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ec.edu.aulaclara.data.DeliveryFilter

@Composable
internal fun DeliveryListScreen(
    state: DeliveryListUiState,
    onBack: () -> Unit,
    onNew: () -> Unit,
    onDetail: (Long) -> Unit,
    onQueryChange: (String) -> Unit,
    onApplyFilters: (DeliveryFilter, Long?) -> Unit,
    onClearFilters: () -> Unit,
) {
    val (query, filter, subjectId) = state.filters
    var showFilters by rememberSaveable { mutableStateOf(false) }
    val visible = state.visible
    ScreenFrame("Entregas", onBack) { padding ->
        PageColumn(Modifier.padding(padding)) {
            Text("Orden: ${state.order.label.lowercase()}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(query, onQueryChange, Modifier.weight(1f).testTag("delivery_search"), singleLine = true,
                    placeholder = { Text("Buscar entrega") }, leadingIcon = { Icon(AulaIcons.Search, null) },
                    trailingIcon = if (query.isNotEmpty()) ({ IconButton({ onQueryChange("") }) { Icon(AulaIcons.Close, "Borrar búsqueda") } }) else null)
                FilledTonalIconButton({ showFilters = true }, Modifier.size(52.dp).testTag("open_filters")) { Icon(AulaIcons.Tune, "Filtrar entregas") }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InputChip(selected = true, onClick = { showFilters = true }, label = { Text(filter.label) })
                InputChip(selected = subjectId != null, onClick = { showFilters = true }, label = { Text(state.subjects.find { it.id == subjectId }?.name ?: "Todas las materias") })
            }
            Text("${visible.size} ${if (visible.size == 1) "entrega" else "entregas"}", style = MaterialTheme.typography.labelSmall)
            if (visible.isEmpty()) {
                if (state.empty) EmptyContent("Tu lista empieza aquí", "Registra una entrega para organizar tu semana.", "Nueva entrega", onNew)
                else {
                    InfoBox("No hay entregas que coincidan con tu búsqueda y filtros.", AulaIcons.SearchOff)
                    SecondaryButton("Mostrar todas", onClearFilters)
                }
            }
            visible.forEach { item -> DeliveryCard(item.delivery, item.subject, item.type) { onDetail(item.delivery.id) } }
            if (!state.empty) PrimaryButton("Nueva entrega", onNew, Modifier.testTag("new_delivery"), AulaIcons.Add)
        }
    }
    if (showFilters) {
        var draftFilter by rememberSaveable { mutableStateOf(filter) }
        var draftSubject by rememberSaveable { mutableStateOf(subjectId) }
        ModalBottomSheet(onDismissRequest = { showFilters = false }) {
            Column(Modifier.fillMaxWidth().heightIn(max = 620.dp).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Filtrar entregas", style = MaterialTheme.typography.titleLarge)
                SectionLabel("Estado")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DeliveryFilter.entries.forEach { value -> FilterChip(draftFilter == value, { draftFilter = value }, label = { Text(value.label) }) }
                }
                SectionLabel("Materia")
                FilterSubject("Todas las materias", draftSubject == null) { draftSubject = null }
                state.subjects.forEach { s -> FilterSubject(s.name, draftSubject == s.id) { draftSubject = s.id } }
                TextButton({ draftFilter = DeliveryFilter.ALL; draftSubject = null }) { Text("Limpiar filtros") }
                PrimaryButton("Aplicar filtros", { onApplyFilters(draftFilter, draftSubject); showFilters = false })
            }
        }
    }
}

@Composable
private fun FilterSubject(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surface) {
        Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected, onClick); Text(label, Modifier.padding(end = 12.dp))
        }
    }
}
