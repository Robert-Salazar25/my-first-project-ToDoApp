package com.example.todoapp.presentation.ui.listener

import com.example.todoapp.domain.model.TaskEntity

interface onClickListener {
    fun onClick(taskEntity: TaskEntity)
    fun onLongClick(taskEntity: TaskEntity)
    fun onEditClick(taskEntity: TaskEntity)

}