package com.example.todoapp.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.todoapp.data.local.entity.TaskEntity
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

    @Query("UPDATE TaskEntity SET notes = :notas WHERE id = :taskId")
    suspend fun updateNotas(taskId: Long, notas: String)

    @Query("SELECT * FROM TaskEntity ORDER BY time ASC")
    suspend fun getAllTaskForWidget(): List<TaskEntity>

    @Query("SELECT * FROM TaskEntity where id = :id")
    fun getTaskById(id: Long): Flow<TaskEntity>  // <-- Cambia LiveData por Flow






}