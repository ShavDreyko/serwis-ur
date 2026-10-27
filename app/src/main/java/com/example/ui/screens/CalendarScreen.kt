package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExtraWork
import com.example.data.model.MaintenanceTask
import com.example.data.model.TaskType
import com.example.ui.components.AddNoteWithPhotoDialog
import com.example.ui.components.NewExtraWorkDialog
import com.example.ui.components.NewTaskDialog
import com.example.ui.components.TaskCard
import com.example.ui.theme.AmberGlow
import com.example.ui.theme.AmberSafety
import com.example.ui.theme.CrimsonAlert
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.LocalIsDarkTheme
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.viewmodel.MaintenanceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: MaintenanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalIsDarkTheme.current
    val allTasks by viewModel.rawTasks.collectAsState()
    val allExtraWork by viewModel.allExtraWork.collectAsState()

    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDayOfYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.DAY_OF_YEAR)) }
    var selectedYear by remember { mutableStateOf(Calendar.getInstance().get(Calendar.YEAR)) }

    var showNewScheduledTaskDialog by remember { mutableStateOf(false) }
    var showNewExtraWorkDialog by remember { mutableStateOf(false) }
    var taskForNoteDialog by remember { mutableStateOf<MaintenanceTask?>(null) }

    val monthFormatter = remember { SimpleDateFormat("LLLL yyyy", Locale("pl", "PL")) }
    val dayHeaderFormatter = remember { SimpleDateFormat("EEEE, d MMMM yyyy", Locale("pl", "PL")) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    val selectedDateCal = remember(selectedDayOfYear, selectedYear) {
        Calendar.getInstance().apply {
            set(Calendar.YEAR, selectedYear)
            set(Calendar.DAY_OF_YEAR, selectedDayOfYear)
        }
    }

    // Helper: precompute day-of-year key for timestamp
    fun toDayKey(millis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = millis }
        return cal.get(Calendar.YEAR).toLong() * 1000L + cal.get(Calendar.DAY_OF_YEAR)
    }

    // Precomputed event sets for instant lookup during calendar rendering
    val breakdownDays = remember(allTasks) {
        val set = mutableSetOf<Long>()
        allTasks.forEach { task ->
            if (task.type == TaskType.AWARIA) {
                set.add(toDayKey(task.reportedAt))
                set.add(toDayKey(task.scheduledDateMillis))
            }
        }
        set
    }

    val extraWorkDays = remember(allExtraWork) {
        val set = mutableSetOf<Long>()
        allExtraWork.forEach { extra ->
            set.add(toDayKey(extra.dateMillis))
        }
        set
    }

    val maintenanceDays = remember(allTasks) {
        val set = mutableSetOf<Long>()
        allTasks.forEach { task ->
            if (task.type != TaskType.AWARIA) {
                set.add(toDayKey(task.scheduledDateMillis))
            }
        }
        set
    }

    val selectedDayKey = remember(selectedYear, selectedDayOfYear) {
        selectedYear.toLong() * 1000L + selectedDayOfYear
    }

    // Filter tasks for the selected date (either reported or scheduled on this date)
    val tasksOnSelectedDay = remember(allTasks, selectedDayKey) {
        allTasks.filter { task ->
            toDayKey(task.scheduledDateMillis) == selectedDayKey ||
                    toDayKey(task.reportedAt) == selectedDayKey
        }
    }

    // Filter extra works for the selected date
    val extraWorkOnSelectedDay = remember(allExtraWork, selectedDayKey) {
        allExtraWork.filter { extra ->
            toDayKey(extra.dateMillis) == selectedDayKey
        }
    }

    val dayFailuresCount = tasksOnSelectedDay.count { it.type == TaskType.AWARIA }
    val dayExtraHours = extraWorkOnSelectedDay.sumOf { it.hours }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(10.dp))

            // Calendar Header card with month navigation
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = {
                            val newCal = (selectedCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, -1)
                            }
                            selectedCalendar = newCal
                            selectedDayOfYear = newCal.get(Calendar.DAY_OF_YEAR)
                            selectedYear = newCal.get(Calendar.YEAR)
                        }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Poprzedni miesiąc", tint = AmberSafety)
                        }

                        Text(
                            text = monthFormatter.format(selectedCalendar.time).replaceFirstChar { it.uppercase() },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(onClick = {
                            val newCal = (selectedCalendar.clone() as Calendar).apply {
                                add(Calendar.MONTH, 1)
                            }
                            selectedCalendar = newCal
                            selectedDayOfYear = newCal.get(Calendar.DAY_OF_YEAR)
                            selectedYear = newCal.get(Calendar.YEAR)
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Następny miesiąc", tint = AmberSafety)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Day of week labels (Pn, Wt, Śr, Cz, Pt, Sb, Nd)
                    val daysOfWeek = listOf("Pn", "Wt", "Śr", "Cz", "Pt", "Sb", "Nd")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        daysOfWeek.forEach { dayName ->
                            Text(
                                text = dayName,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(36.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Month days grid
                    val monthDays = remember(selectedCalendar) {
                        val cal = (selectedCalendar.clone() as Calendar).apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                        }
                        val firstDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) + 5) % 7 // Monday = 0
                        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val daysList = mutableListOf<CalendarDay?>()

                        for (i in 0 until firstDayOfWeek) {
                            daysList.add(null)
                        }
                        val year = cal.get(Calendar.YEAR)
                        for (day in 1..maxDays) {
                            cal.set(Calendar.DAY_OF_MONTH, day)
                            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
                            daysList.add(
                                CalendarDay(
                                    dayNumber = day,
                                    dayOfYear = dayOfYear,
                                    year = year,
                                    dayKey = year.toLong() * 1000L + dayOfYear
                                )
                            )
                        }
                        daysList
                    }

                    // Render weeks in chunks of 7
                    val todayKey = remember {
                        val todayCal = Calendar.getInstance()
                        todayCal.get(Calendar.YEAR).toLong() * 1000L + todayCal.get(Calendar.DAY_OF_YEAR)
                    }
                    val weeks = monthDays.chunked(7)
                    weeks.forEach { week ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            week.forEach { dayInfo ->
                                if (dayInfo != null) {
                                    val isSelected = dayInfo.dayKey == selectedDayKey
                                    val isToday = dayInfo.dayKey == todayKey

                                    // O(1) Instant lookup from precomputed sets
                                    val hasBreakdown = breakdownDays.contains(dayInfo.dayKey)
                                    val hasExtraWork = extraWorkDays.contains(dayInfo.dayKey)
                                    val hasMaintenance = maintenanceDays.contains(dayInfo.dayKey)

                                    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant
                                    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .background(
                                                color = when {
                                                    isSelected -> AmberSafety
                                                    isToday -> surfaceVariantColor
                                                    else -> Color.Transparent
                                                },
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = if (isToday && !isSelected) 1.dp else 0.dp,
                                                color = if (isToday && !isSelected) AmberSafety.copy(alpha = 0.8f) else Color.Transparent,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                selectedDayOfYear = dayInfo.dayOfYear
                                                selectedYear = dayInfo.year
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "${dayInfo.dayNumber}",
                                                color = if (isSelected) Color.Black else if (isToday) (if (isDark) AmberSafety else AmberGlow) else onSurfaceColor,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                                            )

                                            // Indicators row
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                modifier = Modifier.padding(top = 1.dp)
                                            ) {
                                                if (hasBreakdown) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .background(if (isSelected) Color.Black else CrimsonAlert, CircleShape)
                                                    )
                                                }
                                                if (hasExtraWork) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .background(if (isSelected) Color.Black else AmberSafety, CircleShape)
                                                    )
                                                }
                                                if (hasMaintenance) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(4.dp)
                                                            .background(if (isSelected) Color.Black else CyanAccent, CircleShape)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(38.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Legend
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(6.dp).background(CrimsonAlert, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Awaria (250 zł)", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.size(6.dp).background(AmberSafety, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Praca dodatkowa", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.size(6.dp).background(CyanAccent, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Przegląd TPM", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day schedule header and action buttons
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = dayHeaderFormatter.format(selectedDateCal.time).replaceFirstChar { it.uppercase() },
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (dayFailuresCount > 0) {
                        Text(
                            text = "⚡ $dayFailuresCount awarii (krytyczna • ${dayFailuresCount * 250} zł)",
                            color = CrimsonAlert,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (dayExtraHours > 0) {
                        Text(
                            text = "🔧 ${String.format(Locale.US, "%.1f", dayExtraHours)} h prac dodatkowych",
                            color = AmberSafety,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (dayFailuresCount == 0 && dayExtraHours == 0.0) {
                        Text(
                            text = "Zdarzenia w tym dniu: ${tasksOnSelectedDay.size + extraWorkOnSelectedDay.size}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showNewScheduledTaskDialog = true },
                        modifier = Modifier.weight(1f).testTag("add_scheduled_task_btn")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = AmberSafety, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Zadanie / Awaria", fontSize = 11.sp, color = AmberSafety)
                    }

                    OutlinedButton(
                        onClick = { showNewExtraWorkDialog = true },
                        modifier = Modifier.weight(1f).testTag("add_extra_work_calendar_btn")
                    ) {
                        Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = AmberSafety, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Praca dodatkowa", fontSize = 11.sp, color = AmberSafety)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        }

        // Section 1: Tasks & Breakdowns on this day
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ReportProblem, contentDescription = null, tint = CrimsonAlert, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Awarie, zdarzenia i przeglądy w tym dniu (${tasksOnSelectedDay.size}):",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (tasksOnSelectedDay.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Brak awarii i przeglądów odnotowanych w tym dniu.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(tasksOnSelectedDay, key = { it.id }) { task ->
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

        // Section 2: Extra Work on this day
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = AmberSafety, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Prace dodatkowe wykonane w tym dniu (${extraWorkOnSelectedDay.size}):",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (extraWorkOnSelectedDay.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Brak prac dodatkowych przypisanych do tego dnia.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        } else {
            items(extraWorkOnSelectedDay, key = { it.id }) { extra ->
                CalendarExtraWorkCard(
                    extraWork = extra,
                    onToggleSettled = { viewModel.toggleExtraWorkSettled(extra) },
                    onDelete = { viewModel.deleteExtraWork(extra) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showNewScheduledTaskDialog) {
        val selectedDateMillis = selectedDateCal.timeInMillis
        NewTaskDialog(
            onDismiss = { showNewScheduledTaskDialog = false },
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
                    description = description,
                    scheduledDateMillis = selectedDateMillis
                )
                showNewScheduledTaskDialog = false
            }
        )
    }

    if (showNewExtraWorkDialog) {
        val selectedDateMillis = selectedDateCal.timeInMillis
        NewExtraWorkDialog(
            onDismiss = { showNewExtraWorkDialog = false },
            onSave = { title, description, client, _, hours, note ->
                viewModel.addExtraWork(
                    title = title,
                    description = description,
                    machineOrClient = client,
                    dateMillis = selectedDateMillis,
                    hours = hours,
                    note = note
                )
                showNewExtraWorkDialog = false
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

@Composable
fun CalendarExtraWorkCard(
    extraWork: ExtraWork,
    onToggleSettled: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (extraWork.isSettled) EmeraldSuccess.copy(alpha = 0.4f) else AmberSafety.copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = extraWork.isSettled,
                        onCheckedChange = { onToggleSettled() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = EmeraldSuccess,
                            uncheckedColor = AmberSafety
                        )
                    )
                    Text(
                        text = if (extraWork.isSettled) "Rozliczone indywidualnie" else "Do rozliczenia indywidualnego",
                        color = if (extraWork.isSettled) EmeraldSuccess else AmberSafety,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Usuń", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }

            Text(
                text = extraWork.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Stanowisko/Klient: ${extraWork.machineOrClient}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            if (extraWork.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Wykonano: ${extraWork.description}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            }

            if (extraWork.note.isNotBlank()) {
                Text(
                    text = "Notatka: ${extraWork.note}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Przepracowano: ${extraWork.hours} h",
                    color = AmberSafety,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rozliczenie indywidualne",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }
        }
    }
}

private data class CalendarDay(
    val dayNumber: Int,
    val dayOfYear: Int,
    val year: Int,
    val dayKey: Long
)
