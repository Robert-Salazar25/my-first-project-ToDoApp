package com.example.todoapp.data.local.mapper

import com.example.todoapp.data.local.entity.TaskEntity
import com.example.todoapp.domain.model.Task

// data/local/mapper/TaskMapper.kt
object TaskMapper {
    fun toDomain(entity: TaskEntity): Task {
        return Task(
            id = entity.id,
            title = entity.title,
            time = entity.time,
            notes = entity.notes,
            weekDays = entity.weekDays,
            isRecurrent = entity.isRecurrent
        )
    }

    fun toEntity(domain: Task): TaskEntity {
        return TaskEntity(
            id = domain.id,
            title = domain.title,
            time = domain.time,
            notes = domain.notes,
            weekDays = domain.weekDays,
            isRecurrent = domain.isRecurrent
        )
    }
}