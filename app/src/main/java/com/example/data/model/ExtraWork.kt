package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "extra_work")
data class ExtraWork(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val machineOrClient: String = "",
    val dateMillis: Long = System.currentTimeMillis(),
    val hours: Double = 1.0,
    val isSettled: Boolean = false,
    val note: String = ""
)
