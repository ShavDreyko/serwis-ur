package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MaintenanceTask
import com.example.data.model.Priority
import com.example.data.model.TaskStatus
import com.example.ui.components.AddNoteWithPhotoDialog
import com.example.ui.components.NewTaskDialog
import com.example.ui.components.TaskCard
import com.example.ui.theme.AmberSafety
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.MaintenanceViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TasksScreen(
    viewModel: MaintenanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val tasks by viewModel.filteredTasks.collectAsState()
    val rawTasks by viewModel.rawTasks.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedPriority by viewModel.selectedPriorityFilter.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()

    var showNewTaskDialog by remember { mutableStateOf(false) }
    var taskForNoteDialog by remember { mutableStateOf<MaintenanceTask?>(null) }

    val criticalCount = rawTasks.count {
        it.priority == Priority.KRYTYCZNY && it.status != TaskStatus.ZAKONCZONE
    }

    val chipBg = MaterialTheme.colorScheme.surfaceVariant
    val chipText = MaterialTheme.colorScheme.onSurfaceVariant

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Urgent alert banner if any active critical breakdown
                AnimatedVisibility(visible = criticalCount > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrimsonAlert.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                            .border(1.dp, CrimsonAlert, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = CrimsonAlert,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ALARM: $criticalCount AWARIA KRYTYCZNA (STOP LINII)",
                                color = CrimsonAlert,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Wymagana natychmiastowa interwencja serwisu UR.",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Szukaj maszyny, awarii, lokalizacji...", color = chipText) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AmberSafety)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Wyczyść", tint = chipText)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = AmberSafety,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = AmberSafety
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("search_tasks_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Priority Filter Chips
                Text(
                    text = "Widok priorytetów:",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAllSelected = selectedPriority == null
                    Box(
                        modifier = Modifier
                            .background(
                                if (isAllSelected) AmberSafety else chipBg,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.selectedPriorityFilter.value = null }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Wszystkie priorytety",
                            color = if (isAllSelected) Color.Black else chipText,
                            fontSize = 11.5.sp,
                            fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                    }

                    Priority.values().forEach { priority ->
                        val isSelected = selectedPriority == priority
                        val color = priority.getColor(isDark)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) color else chipBg,
                                    RoundedCornerShape(16.dp)
                                )
                                .border(0.8.dp, color, RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.selectedPriorityFilter.value =
                                        if (isSelected) null else priority
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = priority.label,
                                color = if (isSelected) Color.Black else color,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Status Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isAllStatus = selectedStatus == null
                    Box(
                        modifier = Modifier
                            .background(
                                if (isAllStatus) AmberSafety else chipBg,
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { viewModel.selectedStatusFilter.value = null }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Wszystkie statusy",
                            color = if (isAllStatus) Color.Black else chipText,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }

                    TaskStatus.values().forEach { status ->
                        val isSelected = selectedStatus == status
                        val statusColor = status.getColor(isDark)
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) statusColor else chipBg,
                                    RoundedCornerShape(16.dp)
                                )
                                .border(
                                    0.8.dp,
                                    if (isSelected) statusColor else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    viewModel.selectedStatusFilter.value =
                                        if (isSelected) null else status
                                }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = status.label,
                                color = if (isSelected) Color.Black else statusColor,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            if (tasks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Brak zadań dla wybranych filtrów",
                            color = chipText,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Użyj przycisku '+' poniżej, aby zgłosić nową awarię.",
                            color = chipText,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                items(tasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        onStatusChange = { newStatus ->
                            viewModel.updateTaskStatus(context, task, newStatus)
                        },
                        onQuickStartCall = {
                            viewModel.startTask(context, task)
                        },
                        onAddNoteClick = {
                            taskForNoteDialog = task
                        },
                        onPushReminderClick = {
                            viewModel.triggerPushReminder(context, task)
                        },
                        onDeleteClick = {
                            viewModel.deleteTask(task)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Floating Action Button to report breakdown
        FloatingActionButton(
            onClick = { showNewTaskDialog = true },
            containerColor = AmberSafety,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("fab_new_task")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zgłoś awarię")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Zgłoś awarię", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // Dialogs
    if (showNewTaskDialog) {
        NewTaskDialog(
            onDismiss = { showNewTaskDialog = false },
            onSave = { title, machineId, location, type, priority, reporterName, reporterPhone, description ->
                viewModel.addNewTask(
                    context = context,
                    title = title,
                    machineId = machineId,
                    location = location,
                    type = type,
                    priority = priority,
                    reporterName = reporterName,
                    reporterPhone = reporterPhone,
                    description = description
                )
                showNewTaskDialog = false
            }
        )
    }

    if (taskForNoteDialog != null) {
        AddNoteWithPhotoDialog(
            taskTitle = taskForNoteDialog!!.title,
            onDismiss = { taskForNoteDialog = null },
            onSaveNote = { text, photoUri ->
                viewModel.addNoteToTask(taskForNoteDialog!!.id, text, photoUri)
                taskForNoteDialog = null
            }
        )
    }
}
