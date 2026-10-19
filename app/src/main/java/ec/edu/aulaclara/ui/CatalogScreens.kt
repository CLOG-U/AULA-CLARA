@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package ec.edu.aulaclara.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun CatalogScreen(state: AgendaState, onSubject: (Long?) -> Unit, onType: (Long?) -> Unit) {
    var typesTab by rememberSaveable { mutableStateOf(false) }
    PageColumn {
        PageTitle("Organización", "Todo en su materia y en su lugar.")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            FilterChip(!typesTab, { typesTab = false }, label = { Text("Materias") }, modifier = Modifier.weight(1f).testTag("subjects_tab"))
            FilterChip(typesTab, { typesTab = true }, label = { Text("Tipos") }, modifier = Modifier.weight(1f).testTag("types_tab"))
        }
        SectionLabel(if (typesTab) "Tipos de actividad" else "Mis materias")
        if (typesTab) {
            if (state.types.isEmpty()) InfoBox("Todavía no tienes tipos de actividad. Crea uno para clasificar tus entregas.")
            state.types.forEach { t -> CatalogCard(t.name, state.deliveries.count { it.typeId == t.id }, MaterialTheme.colorScheme.primary) { onType(t.id) } }
            PrimaryButton("Nuevo tipo", { onType(null) }, icon = AulaIcons.Add)
        } else {
            if (state.subjects.isEmpty()) InfoBox("Todavía no tienes materias. Añade la primera para comenzar.")
            state.subjects.forEach { s -> CatalogCard(s.name, state.deliveries.count { it.subjectId == s.id }, SubjectColors[s.color]) { onSubject(s.id) } }
            PrimaryButton("Nueva materia", { onSubject(null) }, Modifier.testTag("new_subject"), AulaIcons.Add)
        }
    }
}

@Composable
private fun CatalogCard(name: String, count: Int, color: Color, onClick: () -> Unit) {
    OutlinedCard(onClick, Modifier.fillMaxWidth(), colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Surface(color = color.copy(alpha = 0.12f), shape = MaterialTheme.shapes.small) {
                Icon(AulaIcons.MenuBook, null, Modifier.padding(12.dp).size(22.dp), tint = color)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(if (count == 0) "Sin entregas" else "$count ${if (count == 1) "entrega" else "entregas"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(AulaIcons.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CatalogEditorScreen(state: AgendaState, id: Long?, isType: Boolean, onBack: () -> Unit, notify: (String) -> Unit) {
    val originalName = if (isType) state.types.find { it.id == id }?.name else state.subjects.find { it.id == id }?.name
    val originalColor = state.subjects.find { it.id == id }?.color ?: 0
    var name by rememberSaveable { mutableStateOf(originalName.orEmpty()) }
    var color by rememberSaveable { mutableIntStateOf(originalColor) }
    var attempted by rememberSaveable { mutableStateOf(false) }
    var discard by rememberSaveable { mutableStateOf(false) }
    var deleting by rememberSaveable { mutableStateOf(false) }
    val noun = if (isType) "tipo" else "materia"
    val title = if (id != null) "Editar $noun" else if (isType) "Nuevo tipo" else "Nueva materia"
    val associated = if (isType) state.deliveries.count { it.typeId == id } else state.deliveries.count { it.subjectId == id }
    val dirty = name != originalName.orEmpty() || (!isType && color != originalColor)
    val exit: () -> Unit = { if (dirty) discard = true else onBack() }
    BackHandler(dirty) { discard = true }
    val others = if (isType) state.types.filter { it.id != id }.map { it.name } else state.subjects.filter { it.id != id }.map { it.name }
    val duplicate = name.isNotBlank() && isDuplicateName(name, others)
    ScreenFrame(title, exit) { p ->
        PageColumn(Modifier.padding(p).imePadding()) {
            if (id != null && originalName == null) {
                InfoBox("Este elemento ya no existe."); SecondaryButton("Volver", onBack)
            } else {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth().testTag("catalog_name"), label = { Text(if (isType) "Nombre del tipo" else "Nombre de la materia") },
                    singleLine = true, isError = attempted && (name.isBlank() || duplicate), supportingText = if (attempted && (name.isBlank() || duplicate)) ({
                        Text(if (name.isBlank()) "Escribe un nombre para continuar." else "Ya existe ${if (isType) "un tipo" else "una materia"} con ese nombre.")
                    }) else null)
                if (!isType) {
                    SectionLabel("Color identificador")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SubjectColors.forEachIndexed { index, value ->
                            IconButton({ color = index }, Modifier.size(52.dp).testTag("subject_color_$index")) {
                                Box(Modifier.size(38.dp).background(value, CircleShape).border(if (color == index) 3.dp else 0.dp,
                                    if (color == index) MaterialTheme.colorScheme.onSurface else Color.Transparent, CircleShape), contentAlignment = Alignment.Center) {
                                    Icon(if (color == index) AulaIcons.Check else AulaIcons.Circle, "Color ${listOf("azul", "violeta", "verde", "naranja", "rosa")[index]}${if (color == index) ", seleccionado" else ""}",
                                        tint = if (color == index) Color.White else Color.Transparent, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                    }
                }
                if (id != null) {
                    if (associated > 0) InfoBox("$associated ${if (associated == 1) "entrega asociada" else "entregas asociadas"}. Para borrar ${if (isType) "este tipo" else "esta materia"}, reasigna primero sus entregas${if (isType) " a otro tipo" else " a otra materia"}.", AulaIcons.Link)
                    else InfoBox("Sin entregas asociadas. Puedes eliminar ${if (isType) "este tipo" else "esta materia"}.")
                }
                PrimaryButton(if (id == null) (if (isType) "Crear tipo" else "Crear materia") else "Guardar cambios", {
                    attempted = true
                    if (name.isNotBlank() && !duplicate) {
                        val key = id ?: state.nextId()
                        if (isType) {
                            val value = ActivityType(key, name.trim())
                            val index = state.types.indexOfFirst { it.id == key }
                            if (index < 0) state.types.add(value) else state.types[index] = value
                        } else {
                            val value = Subject(key, name.trim(), color)
                            val index = state.subjects.indexOfFirst { it.id == key }
                            if (index < 0) state.subjects.add(value) else state.subjects[index] = value
                        }
                        onBack(); notify(if (id == null) "${if (isType) "Tipo creado" else "Materia creada"}" else "Cambios guardados")
                    }
                }, Modifier.testTag("save_catalog"))
                if (id != null) SecondaryButton("Eliminar $noun", { deleting = true }, Modifier.testTag("delete_catalog"), destructive = true, enabled = associated == 0)
                SecondaryButton("Cancelar", exit)
            }
        }
    }
    if (discard) ConfirmDialog("¿Descartar cambios?", "Los cambios de este formulario todavía no se han guardado.", "Descartar", onBack, { discard = false }, false)
    if (deleting && id != null) ConfirmDialog("¿Eliminar $noun?", "Se eliminará $name. Esta acción no se puede deshacer.", "Eliminar $noun", {
        val deleted = if (isType) state.deleteType(id) else state.deleteSubject(id)
        deleting = false
        if (deleted) { onBack(); notify("${if (isType) "Tipo eliminado" else "Materia eliminada"}") }
        else notify("No se puede eliminar: tiene entregas asociadas")
    }, { deleting = false })
}
