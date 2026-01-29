package com.example.todoapp.domain.model

// domain/model/Task.kt
data class Task(
    val id: Long = 0,
    val title: String,
    val time: String,
    val notes: String = "",
    val weekDays: String,
    val isRecurrent: Boolean = false
)