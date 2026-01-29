package com.example.todoapp.domain.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskEntity(@PrimaryKey(autoGenerate = true) var id: Long = 0,
                      val tarea: String,
                      val hora: String,
                       val notas: String = "",
                      val diasSemana: String,
                      @ColumnInfo(defaultValue = "0") val esRecurrente: Boolean)
