package com.example.todoapp

import android.app.Application
import com.example.todoapp.data.local.TaskDatabase
import com.example.todoapp.data.repository.TaskRepositoryImpl  // ← CAMBIADO
import com.example.todoapp.domain.repository.TaskRepository

class TodoApp : Application() {

    // Usar TaskRepositoryImpl que es la implementación
    val taskRepository: TaskRepository by lazy {
        val database = TaskDatabase.getDatabase(this)
        TaskRepositoryImpl(  // ← CAMBIADO
            taskDao = database.TaskDao(),
            applicationContext = applicationContext
        )
    }
}