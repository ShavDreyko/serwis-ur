package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ExtraWork
import kotlinx.coroutines.flow.Flow

@Dao
interface ExtraWorkDao {
    @Query("SELECT * FROM extra_work ORDER BY dateMillis DESC")
    fun getAllExtraWork(): Flow<List<ExtraWork>>

    @Query("SELECT * FROM extra_work WHERE dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getExtraWorkInMonth(startMillis: Long, endMillis: Long): Flow<List<ExtraWork>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtraWork(extraWork: ExtraWork): Long

    @Update
    suspend fun updateExtraWork(extraWork: ExtraWork)

    @Delete
    suspend fun deleteExtraWork(extraWork: ExtraWork)

    @Query("UPDATE extra_work SET isSettled = :isSettled WHERE id = :id")
    suspend fun updateSettledStatus(id: Long, isSettled: Boolean)
}
