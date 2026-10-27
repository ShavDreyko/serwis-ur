package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.Converters
import com.example.data.model.ExtraWork
import com.example.data.model.MaintenanceTask
import com.example.data.model.Priority
import com.example.data.model.TaskNote
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import com.example.data.repository.MaintenanceRepository
import com.example.util.NotificationHelper
import com.example.util.PdfReportGenerator
import com.example.util.SoundHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar
import java.util.UUID

class MaintenanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MaintenanceRepository
    val isDarkTheme = MutableStateFlow(true)

    // Filter states
    val searchQuery = MutableStateFlow("")
    val selectedPriorityFilter = MutableStateFlow<Priority?>(null)
    val selectedStatusFilter = MutableStateFlow<TaskStatus?>(null)
    val selectedTypeFilter = MutableStateFlow<TaskType?>(null)

    // Calendar & Monthly Summary Selection
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    val rawTasks: StateFlow<List<MaintenanceTask>>
    val allExtraWork: StateFlow<List<ExtraWork>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = MaintenanceRepository(db.maintenanceDao(), db.extraWorkDao())

        rawTasks = repository.allTasks.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allExtraWork = repository.allExtraWork.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.populateInitialDataIfEmpty()
            NotificationHelper.createNotificationChannel(application)
        }
    }

    // Filtered tasks for the Tasks list view
    val filteredTasks: StateFlow<List<MaintenanceTask>> = combine(
        rawTasks,
        searchQuery,
        selectedPriorityFilter,
        selectedStatusFilter,
        selectedTypeFilter
    ) { tasks, query, priority, status, type ->
        tasks.filter { task ->
            val matchesQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.machineId.contains(query, ignoreCase = true) ||
                    task.location.contains(query, ignoreCase = true) ||
                    task.reporterName.contains(query, ignoreCase = true)

            val matchesPriority = priority == null || task.priority == priority
            val matchesStatus = status == null || task.status == status
            val matchesType = type == null || task.type == type

            matchesQuery && matchesPriority && matchesStatus && matchesType
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
    }

    fun setMonthAndYear(month: Int, year: Int) {
        _selectedMonth.value = month
        _selectedYear.value = year
    }

    fun previousMonth() {
        if (_selectedMonth.value == 0) {
            _selectedMonth.value = 11
            _selectedYear.value = _selectedYear.value - 1
        } else {
            _selectedMonth.value = _selectedMonth.value - 1
        }
    }

    fun nextMonth() {
        if (_selectedMonth.value == 11) {
            _selectedMonth.value = 0
            _selectedYear.value = _selectedYear.value + 1
        } else {
            _selectedMonth.value = _selectedMonth.value + 1
        }
    }

    // Update status with sound feedback, haptics, and push notifications
    fun updateTaskStatus(context: Context, task: MaintenanceTask, newStatus: TaskStatus) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val startedAt = if (newStatus == TaskStatus.W_TRAKCIE && task.startedAt == null) now else task.startedAt
            val completedAt = if (newStatus == TaskStatus.ZAKONCZONE) now else if (newStatus != TaskStatus.ZAKONCZONE) null else task.completedAt

            repository.updateTaskStatus(task.id, newStatus, startedAt, completedAt)

            // Play distinctive sound and vibration
            SoundHelper.playStatusChangeSound(context, newStatus)

            // Push notification
            val statusMsg = when (newStatus) {
                TaskStatus.W_TRAKCIE -> "Rozpoczęto prace przy maszynie: ${task.machineId}"
                TaskStatus.ZAKONCZONE -> "Awaria usunięta pomyślnie na maszynie: ${task.machineId}"
                TaskStatus.WSTRZYMANE -> "Wstrzymano zadanie: ${task.machineId} (oczekiwanie na części)"
                TaskStatus.NOWE -> "Przywrócono status nowe: ${task.title}"
            }
            NotificationHelper.sendTaskNotification(
                context = context,
                notificationId = task.id.toInt(),
                title = "Status: ${newStatus.label}",
                message = statusMsg,
                priority = task.priority
            )
        }
    }

    // Start task without phone dial (simplified)
    fun startTask(context: Context, task: MaintenanceTask) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.updateTaskStatus(task.id, TaskStatus.W_TRAKCIE, now, null)
            SoundHelper.playStatusChangeSound(context, TaskStatus.W_TRAKCIE)

            NotificationHelper.sendTaskNotification(
                context = context,
                notificationId = task.id.toInt(),
                title = "Rozpoczęto zlecenie",
                message = "Zlecenie na maszynie ${task.machineId} w toku.",
                priority = task.priority
            )
        }
    }

    // Deprecated phone version kept for compatibility (calls startTask)
    fun startBreakdownWithPhoneCall(context: Context, task: MaintenanceTask) {
        startTask(context, task)
    }

    // Add new maintenance / breakdown task
    fun addNewTask(
        context: Context,
        title: String,
        machineId: String,
        location: String,
        type: TaskType,
        priority: Priority,
        reporterName: String,
        reporterPhone: String,
        description: String,
        scheduledDateMillis: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val effectivePriority = if (type == TaskType.AWARIA) Priority.KRYTYCZNY else priority
            val newTask = MaintenanceTask(
                title = title,
                machineId = machineId,
                location = location,
                type = type,
                priority = effectivePriority,
                status = TaskStatus.NOWE,
                reportedAt = now,
                startedAt = null,
                completedAt = null,
                reporterName = reporterName,
                reporterPhone = reporterPhone,
                description = description,
                notesJson = "[]",
                scheduledDateMillis = scheduledDateMillis
            )
            val newId = repository.insertTask(newTask)

            // If critical, trigger alert sound & immediate push notification
            if (effectivePriority == Priority.KRYTYCZNY || effectivePriority == Priority.WYSOKI) {
                SoundHelper.playCriticalAlertSound(context)
                NotificationHelper.sendTaskNotification(
                    context = context,
                    notificationId = newId.toInt(),
                    title = "PRZYJAZD AWARYJNY (250 zł): $machineId",
                    message = "$title (Sytuacja krytyczna). Lokalizacja: $location",
                    priority = effectivePriority
                )
            } else {
                SoundHelper.playStatusChangeSound(context, TaskStatus.NOWE)
                NotificationHelper.sendTaskNotification(
                    context = context,
                    notificationId = newId.toInt(),
                    title = "Nowe zlecenie UR",
                    message = "$title dla $machineId",
                    priority = effectivePriority
                )
            }
        }
    }

    // Copy photo from temporary URI (picker / camera cache) to permanent internal storage
    private fun copyPhotoToInternalStorage(uriString: String): String? {
        val context = getApplication<Application>()
        val uri = Uri.parse(uriString)

        val photosDir = File(context.filesDir, "task_photos")
        if (!photosDir.exists()) photosDir.mkdirs()

        val destFile = File(
            photosDir,
            "photo_${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
        )

        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null

            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Add note and photo to existing task (photos are copied to permanent storage)
    fun addNoteToTask(taskId: Long, noteText: String, photoUri: String?) {
        viewModelScope.launch {
            val currentTask = rawTasks.value.find { it.id == taskId } ?: return@launch
            val existingNotes = Converters.deserializeNotes(currentTask.notesJson).toMutableList()

            val permanentPhotoUri = photoUri?.let { copyPhotoToInternalStorage(it) }

            existingNotes.add(
                TaskNote(
                    text = noteText,
                    timestamp = System.currentTimeMillis(),
                    photoUri = permanentPhotoUri
                )
            )
            val updatedJson = Converters.serializeNotes(existingNotes)
            repository.updateTask(currentTask.copy(notesJson = updatedJson))
        }
    }

    fun deleteTask(task: MaintenanceTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    // Extra Work Management for end-of-month billing (rozliczane indywidualnie)
    fun addExtraWork(
        title: String,
        description: String,
        machineOrClient: String,
        dateMillis: Long,
        hours: Double,
        note: String
    ) {
        viewModelScope.launch {
            val extra = ExtraWork(
                title = title,
                description = description,
                machineOrClient = machineOrClient,
                dateMillis = dateMillis,
                hours = hours,
                isSettled = false,
                note = note
            )
            repository.insertExtraWork(extra)
        }
    }

    fun toggleExtraWorkSettled(extraWork: ExtraWork) {
        viewModelScope.launch {
            repository.updateSettledStatus(extraWork.id, !extraWork.isSettled)
        }
    }

    fun deleteExtraWork(extraWork: ExtraWork) {
        viewModelScope.launch {
            repository.deleteExtraWork(extraWork)
        }
    }

    // Trigger push reminder manually
    fun triggerPushReminder(context: Context, task: MaintenanceTask) {
        NotificationHelper.sendTaskNotification(
            context = context,
            notificationId = (task.id + 1000).toInt(),
            title = "PRZYPOMNIENIE O ZLECENIU",
            message = "${task.title} na maszynie ${task.machineId} (Priorytet: ${task.priority.label})",
            priority = task.priority
        )
        SoundHelper.playStatusChangeSound(context, TaskStatus.W_TRAKCIE)
    }

    // Generate & send monthly summary PDF via email
    fun exportAndEmailMonthlyPdf(
        context: Context,
        recipientEmail: String = "",
        technicianName: String = "Technik UR",
        includePhotos: Boolean = false
    ): Boolean {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _selectedYear.value)
            set(Calendar.MONTH, _selectedMonth.value)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startMillis = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val endMillis = cal.timeInMillis

        val monthTasks = rawTasks.value.filter {
            (it.reportedAt in startMillis..endMillis) || (it.scheduledDateMillis in startMillis..endMillis)
        }
        val monthExtraWork = allExtraWork.value.filter {
            it.dateMillis in startMillis..endMillis
        }

        val monthNames = arrayOf(
            "Styczeń", "Luty", "Marzec", "Kwiecień", "Maj", "Czerwiec",
            "Lipiec", "Sierpień", "Wrzesień", "Październik", "Listopad", "Grudzień"
        )
        val monthLabel = "${monthNames[_selectedMonth.value]} ${_selectedYear.value}"

        val pdfFile = PdfReportGenerator.generateMonthlyPdf(
            context = context,
            monthYearLabel = monthLabel,
            technicianName = technicianName,
            tasks = monthTasks,
            extraWorks = monthExtraWork,
            includePhotos = includePhotos
        )

        return if (pdfFile != null) {
            PdfReportGenerator.sharePdfViaEmail(
                context = context,
                pdfFile = pdfFile,
                recipientEmail = recipientEmail,
                monthYearLabel = monthLabel
            )
            true
        } else {
            false
        }
    }
}
