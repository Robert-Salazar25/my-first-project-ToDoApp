package com.example.todoapp

import android.app.Application
import com.example.todoapp.data.local.TaskDatabase
import com.example.todoapp.data.repository.TaskRepositoryImpl
import com.example.todoapp.domain.repository.TaskRepository

class TodoApp : Application() {

    // ✅ Repository implementado correctamente
    val taskRepository: TaskRepository by lazy {
        val database = TaskDatabase.getDatabase(this)
        TaskRepositoryImpl(database.TaskDao(), applicationContext)
    }
}