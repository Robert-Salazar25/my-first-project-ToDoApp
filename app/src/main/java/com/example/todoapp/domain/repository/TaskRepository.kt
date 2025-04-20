package com.example.todoapp.domain.repository

import androidx.lifecycle.LiveData
import com.example.todoapp.data.Dao.TaskDao
import com.example.todoapp.domain.model.TaskEntity

class TaskRepository(private val taskDao: TaskDao) {

    val allTasks: LiveData<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun addTask(taskEntity: TaskEntity): Long{
        return taskDao.addTask(taskEntity)
    }

    suspend fun updateTask(taskEntity: TaskEntity){
         taskDao.updateTask(taskEntity)
    }

    suspend fun deleteTask(taskEntity: TaskEntity){
        taskDao.deleteTask(taskEntity)
    }

    fun getTaskbyId(id: Long): LiveData<TaskEntity> {
        return taskDao.getTaskbyId(id)

    }

    suspend fun updateTaskNotes(taskId: Long, notas: String){
        taskDao.updateNotas(taskId, notas)

    }






}