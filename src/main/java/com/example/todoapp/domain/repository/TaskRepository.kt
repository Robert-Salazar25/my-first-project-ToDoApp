package com.example.todoapp.domain.repository

import com.example.todoapp.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    val allTasks: Flow<List<Task>>
    suspend fun addTask(task: Task): Long
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    fun getTaskById(id: Long): Flow<Task>
    suspend fun updateTaskNotes(taskId: Long, notes: String)
}