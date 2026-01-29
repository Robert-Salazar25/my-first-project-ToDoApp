package com.example.todoapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TaskEntity(@PrimaryKey(autoGenerate = true) var id: Long = 0,
                      val title: String,
                      val time: String,
                      val notes: String = "",
                      val weekDays: String,
                      @ColumnInfo(defaultValue = "0") val isRecurrent: Boolean)
