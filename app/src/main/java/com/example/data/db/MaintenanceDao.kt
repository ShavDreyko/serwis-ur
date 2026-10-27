package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MaintenanceTask
import com.example.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface MaintenanceDao {
    @Query("SELECT * FROM maintenance_tasks ORDER BY priority ASC, reportedAt DESC")
    fun getAllTasks(): Flow<List<MaintenanceTask>>

    @Query("SELECT * FROM maintenance_tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<MaintenanceTask?>

    @Query("SELECT * FROM maintenance_tasks WHERE reportedAt BETWEEN :startMillis AND :endMillis OR scheduledDateMillis BETWEEN :startMillis AND :endMillis ORDER BY reportedAt DESC")
    fun getTasksInTimeRange(startMillis: Long, endMillis: Long): Flow<List<MaintenanceTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: MaintenanceTask): Long

    @Update
    suspend fun updateTask(task: MaintenanceTask)

    @Delete
    suspend fun deleteTask(task: MaintenanceTask)

    @Query("UPDATE maintenance_tasks SET status = :status, startedAt = CASE WHEN :startedAt IS NOT NULL THEN :startedAt ELSE startedAt END, completedAt = CASE WHEN :completedAt IS NOT NULL THEN :completedAt ELSE completedAt END WHERE id = :id")
    suspend fun updateTaskStatus(id: Long, status: TaskStatus, startedAt: Long?, completedAt: Long?)
}
