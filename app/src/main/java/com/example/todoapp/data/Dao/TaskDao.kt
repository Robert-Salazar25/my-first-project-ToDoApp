package com.example.todoapp.data.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.todoapp.domain.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM TaskEntity")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM TaskEntity where id = :id")
     fun getTaskbyId(id: Long): LiveData<TaskEntity>

    @Insert
    suspend fun addTask(taskEntity: TaskEntity): Long

    @Update
    suspend fun updateTask(taskEntity: TaskEntity)

    @Delete
    suspend fun deleteTask (taskEntity: TaskEntity)

    @Query("UPDATE TaskEntity SET notas = :notas WHERE id = :taskId")
    suspend fun updateNotas(taskId: Long, notas: String)

    @Query("SELECT * FROM TaskEntity ORDER BY hora ASC")
    suspend fun getAllTaskForWidget(): List<TaskEntity>




}