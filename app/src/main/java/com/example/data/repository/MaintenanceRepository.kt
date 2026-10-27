package com.example.data.repository

import com.example.data.db.ExtraWorkDao
import com.example.data.db.MaintenanceDao
import com.example.data.model.ExtraWork
import com.example.data.model.MaintenanceTask
import com.example.data.model.Priority
import com.example.data.model.TaskStatus
import com.example.data.model.TaskType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class MaintenanceRepository(
    private val maintenanceDao: MaintenanceDao,
    private val extraWorkDao: ExtraWorkDao
) {
    val allTasks: Flow<List<MaintenanceTask>> = maintenanceDao.getAllTasks()
    val allExtraWork: Flow<List<ExtraWork>> = extraWorkDao.getAllExtraWork()

    fun getTaskById(id: Long): Flow<MaintenanceTask?> = maintenanceDao.getTaskById(id)

    fun getTasksInTimeRange(startMillis: Long, endMillis: Long): Flow<List<MaintenanceTask>> =
        maintenanceDao.getTasksInTimeRange(startMillis, endMillis)

    fun getExtraWorkInMonth(startMillis: Long, endMillis: Long): Flow<List<ExtraWork>> =
        extraWorkDao.getExtraWorkInMonth(startMillis, endMillis)

    suspend fun insertTask(task: MaintenanceTask): Long = maintenanceDao.insertTask(task)

    suspend fun updateTask(task: MaintenanceTask) = maintenanceDao.updateTask(task)

    suspend fun deleteTask(task: MaintenanceTask) = maintenanceDao.deleteTask(task)

    suspend fun updateTaskStatus(id: Long, status: TaskStatus, startedAt: Long?, completedAt: Long?) =
        maintenanceDao.updateTaskStatus(id, status, startedAt, completedAt)

    suspend fun insertExtraWork(extraWork: ExtraWork): Long = extraWorkDao.insertExtraWork(extraWork)

    suspend fun updateExtraWork(extraWork: ExtraWork) = extraWorkDao.updateExtraWork(extraWork)

    suspend fun deleteExtraWork(extraWork: ExtraWork) = extraWorkDao.deleteExtraWork(extraWork)

    suspend fun updateSettledStatus(id: Long, isSettled: Boolean) =
        extraWorkDao.updateSettledStatus(id, isSettled)

    suspend fun populateInitialDataIfEmpty() {
        // Sample data population disabled.
        // Previously this re-inserted demo tasks/extra works whenever the database was empty
        // (e.g. after the user deleted all test entries). That caused deleted items to reappear
        // after app restart. The app now starts empty and keeps only user-created data.
    }
}
