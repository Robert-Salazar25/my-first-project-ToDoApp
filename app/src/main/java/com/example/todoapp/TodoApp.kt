package com.example.todoapp

import android.app.Application
import com.example.todoapp.data.database.TaskDatabase
import com.example.todoapp.domain.repository.TaskRepository

class TodoApp: Application() {

    val taskRepository: TaskRepository by lazy {
        val database = TaskDatabase.getDatabase(this)
        TaskRepository(database.TaskDao())
    }
}