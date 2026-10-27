package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maintenance_tasks")
data class MaintenanceTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val machineId: String,
    val location: String = "",
    val type: TaskType = TaskType.AWARIA,
    val priority: Priority = Priority.WYSOKI,
    val status: TaskStatus = TaskStatus.NOWE,
    val reportedAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val completedAt: Long? = null,
    val reporterName: String = "",
    val reporterPhone: String = "",
    val description: String = "",
    val notesJson: String = "[]",
    val scheduledDateMillis: Long = System.currentTimeMillis()
)
