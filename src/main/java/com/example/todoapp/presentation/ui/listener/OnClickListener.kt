package com.example.todoapp.presentation.ui.listener

import com.example.todoapp.domain.model.Task // <-- CAMBIADO

interface onClickListener {
    fun onClick(task: Task) // <-- CAMBIADO
    fun onLongClick(task: Task) // <-- CAMBIADO
    fun onEditClick(task: Task) // <-- CAMBIADO
}