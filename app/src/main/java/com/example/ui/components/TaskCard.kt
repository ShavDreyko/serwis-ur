package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.db.Converters
import com.example.data.model.MaintenanceTask
import com.example.data.model.TaskNote
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import com.example.ui.theme.AmberSafety
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalIsDarkTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCard(
    task: MaintenanceTask,
    onStatusChange: (TaskStatus) -> Unit,
    onQuickStartCall: () -> Unit,
    onAddNoteClick: () -> Unit,
    onPushReminderClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    var isExpanded by remember { mutableStateOf(false) }
    var statusMenuExpanded by remember { mutableStateOf(false) }
    var selectedPhotoUrlForDialog by remember { mutableStateOf<String?>(null) }

    val dateTimeFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()) }
    val notes = remember(task.notesJson) { Converters.deserializeNotes(task.notesJson) }

    val isUrgent = task.priority.level <= 2
    val borderColor = if (task.status == TaskStatus.W_TRAKCIE) CyanAccent.copy(alpha = 0.6f)
    else if (isUrgent && task.status != TaskStatus.ZAKONCZONE) AmberSafety.copy(alpha = 0.6f)
    else MaterialTheme.colorScheme.outline

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .animateContentSize()
            .testTag("task_card_${task.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Top Row: Badges and Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    PriorityBadge(priority = task.priority)
                    StatusBadge(status = task.status)
                    TypeBadge(type = task.type)
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp).testTag("delete_task_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Usuń zadanie",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title and Machine name
            Text(
                text = task.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(AmberSafety, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = task.machineId,
                    color = AmberSafety,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (task.location.isNotBlank()) {
                    Text(
                        text = " • ${task.location}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamps: Exact reported date and time
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Zgłoszono: ${dateTimeFormat.format(Date(task.reportedAt))}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.5.sp
                )
            }

            if (task.startedAt != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Rozpoczęto: ${dateTimeFormat.format(Date(task.startedAt))}",
                    color = CyanAccent,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (task.completedAt != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Zakończono: ${dateTimeFormat.format(Date(task.completedAt))}",
                    color = EmeraldSuccess,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (task.reporterName.isNotBlank() || task.reporterPhone.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Zgłaszający: ${task.reporterName} ${if (task.reporterPhone.isNotBlank()) "(${task.reporterPhone})" else ""}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            if (task.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = task.description,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.5.sp,
                    maxLines = if (isExpanded) 10 else 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Start button for new breakdowns
                if (task.status == TaskStatus.NOWE && task.type == TaskType.AWARIA) {
                    FilledTonalButton(
                        onClick = onQuickStartCall,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = AmberSafety,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("quick_start_btn_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Rozpocznij zlecenie",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Status selector dropdown
                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { statusMenuExpanded = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("status_menu_btn_${task.id}")
                        ) {
                            Text(
                                text = "Status: ${task.status.label}",
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        DropdownMenu(
                            expanded = statusMenuExpanded,
                            onDismissRequest = { statusMenuExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            TaskStatus.values().forEach { status ->
                                DropdownMenuItem(
                                    text = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (status == task.status) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = CyanAccent,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                            }
                                            Text(
                                                text = status.label,
                                                color = status.getColor(isDark),
                                                fontWeight = if (status == task.status) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    },
                                    onClick = {
                                        statusMenuExpanded = false
                                        onStatusChange(status)
                                    }
                                )
                            }
                        }
                    }
                }

                // Push reminder button
                IconButton(
                    onClick = onPushReminderClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .testTag("push_reminder_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Wyślij przypomnienie push",
                        tint = CyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Add note / photo button
                IconButton(
                    onClick = onAddNoteClick,
                    modifier = Modifier
                        .size(38.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .testTag("add_note_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = "Dodaj notatkę i zdjęcie",
                        tint = AmberSafety,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Expand / Collapse details
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Zwiń" else "Rozwiń",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // Expanded section: Notes and attached photos
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Text(
                        text = "Notatki i zdjęcia z interwencji (${notes.size}):",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (notes.isEmpty()) {
                        Text(
                            text = "Brak notatek. Kliknij ikonę aparatu, aby dodać zdjęcie z usterki.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    } else {
                        notes.forEach { note ->
                            NoteItemView(
                                note = note,
                                onPhotoClick = { selectedPhotoUrlForDialog = it }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }

    // Fullscreen photo preview dialog
    if (selectedPhotoUrlForDialog != null) {
        Dialog(onDismissRequest = { selectedPhotoUrlForDialog = null }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .clickable { selectedPhotoUrlForDialog = null }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = selectedPhotoUrlForDialog,
                    contentDescription = "Powiększone zdjęcie usterki",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun NoteItemView(note: TaskNote, onPhotoClick: (String) -> Unit) {
    val timeFormat = remember { SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (note.photoUri != null) {
            AsyncImage(
                model = note.photoUri,
                contentDescription = "Zdjęcie do notatki",
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onPhotoClick(note.photoUri) }
                    .border(1.dp, AmberSafety.copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = timeFormat.format(Date(note.timestamp)),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.5.sp
            )
            if (note.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = note.text,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }
        }
    }
}
