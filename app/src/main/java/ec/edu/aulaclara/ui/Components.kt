@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
package ec.edu.aulaclara.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val Spanish: Locale = Locale.forLanguageTag("es-EC")
val SubjectColors = listOf(Color(0xFF5879CF), Color(0xFF8966BD), Color(0xFF007F78), Color(0xFFC37132), Color(0xFFB2557E))
fun shortDate(date: LocalDateTime): String = date.format(DateTimeFormatter.ofPattern("d MMM · HH:mm", Spanish))
fun fullDate(date: LocalDateTime): String = date.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Spanish)).replaceFirstChar { it.titlecase(Spanish) }

@Composable
fun PageColumn(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = 840.dp).fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(20.dp), content = content)
    }
}

@Composable
fun PageTitle(title: String, subtitle: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.semantics { heading() })
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable
fun BrandMark(size: Int = 68) {
    Box(Modifier.size(size.dp).clip(RoundedCornerShape((size / 3).dp))
        .background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
        Icon(AulaIcons.TaskAlt, "Aula Clara", Modifier.size((size * 0.54f).dp), tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    Button(onClick, modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp), enabled = enabled,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)) {
        icon?.let { Icon(it, null, Modifier.size(19.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text)
    }
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, destructive: Boolean = false, enabled: Boolean = true) {
    val color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    OutlinedButton(onClick, modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(14.dp), enabled = enabled,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)) { Text(text) }
}

@Composable
fun InfoBox(text: String, icon: ImageVector = AulaIcons.Info, error: Boolean = false) {
    Surface(color = if (error) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium) {
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, Modifier.size(21.dp), tint = if (error) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.semantics { heading() })
}

@Composable
fun ScreenFrame(title: String, onBack: () -> Unit, content: @Composable (PaddingValues) -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background, contentWindowInsets = WindowInsets(0),
        topBar = { TopAppBar(title = { Text(title, style = MaterialTheme.typography.titleLarge) },
            navigationIcon = { IconButton(onBack) { Icon(AulaIcons.ArrowBack, "Volver") } },
            windowInsets = WindowInsets(0), colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        content = content)
}

@Composable
fun ConfirmDialog(title: String, text: String, confirmLabel: String = "Eliminar", onConfirm: () -> Unit, onDismiss: () -> Unit, destructive: Boolean = true) {
    AlertDialog(onDismissRequest = onDismiss, icon = { Icon(if (destructive) AulaIcons.DeleteOutline else AulaIcons.EditNote, null) },
        title = { Text(title) }, text = { Text(text) },
        confirmButton = { TextButton(onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)) { Text(confirmLabel) } },
        dismissButton = { TextButton(onDismiss) { Text(if (destructive) "Cancelar" else "Seguir editando") } })
}

@Composable
fun DeliveryCard(delivery: Delivery, subject: Subject?, type: ActivityType?, onClick: () -> Unit) {
    val overdue = !delivery.completed && delivery.due.isBefore(LocalDateTime.now())
    OutlinedCard(onClick, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Row(Modifier.height(IntrinsicSize.Min).fillMaxWidth()) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(SubjectColors.getOrElse(subject?.color ?: 0) { SubjectColors[0] }))
            Column(Modifier.padding(16.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(delivery.title, style = MaterialTheme.typography.titleMedium)
                Text("${subject?.name ?: "Sin materia"} · ${type?.name ?: "Sin tipo"}",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(AulaIcons.Event, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(shortDate(delivery.due), style = MaterialTheme.typography.labelSmall)
                    }
                    StatusPill(delivery.priority.label)
                    if (delivery.completed) StatusPill("Completada", positive = true)
                    if (overdue) StatusPill("Vencida", error = true)
                }
            }
            Icon(AulaIcons.ChevronRight, null, Modifier.align(Alignment.CenterVertically).padding(end = 12.dp).size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StatusPill(text: String, positive: Boolean = false, error: Boolean = false) {
    val bg = when { error -> MaterialTheme.colorScheme.errorContainer; positive -> MaterialTheme.colorScheme.primaryContainer; else -> MaterialTheme.colorScheme.surfaceVariant }
    val fg = when { error -> MaterialTheme.colorScheme.error; positive -> MaterialTheme.colorScheme.primary; else -> MaterialTheme.colorScheme.onSurfaceVariant }
    Surface(color = bg, shape = CircleShape) { Text(text, Modifier.padding(horizontal = 11.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall, color = fg) }
}

@Composable
fun EmptyContent(title: String, message: String, action: String, onAction: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        BrandMark(88)
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        PrimaryButton(action, onAction, icon = AulaIcons.Add)
    }
}
