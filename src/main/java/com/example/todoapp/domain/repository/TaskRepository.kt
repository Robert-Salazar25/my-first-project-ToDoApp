package com.example.todoapp.domain.repository

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import com.example.todoapp.R
import com.example.todoapp.data.Dao.TaskDao
import com.example.todoapp.domain.model.TaskEntity
import com.example.todoapp.presentation.ui.widget.TaskWidget
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import kotlin.math.abs

class TaskRepository(private val taskDao: TaskDao,
                     private val applicationContext: Context
) {

    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun addTask(taskEntity: TaskEntity): Long{
        notifyWidgets()
        return taskDao.addTask(taskEntity)

    }

    suspend fun updateTask(taskEntity: TaskEntity){
         taskDao.updateTask(taskEntity)
        notifyWidgets()
    }

    suspend fun deleteTask(taskEntity: TaskEntity){
        taskDao.deleteTask(taskEntity)
        notifyWidgets()
    }

    fun getTaskbyId(id: Long): LiveData<TaskEntity> {
        return taskDao.getTaskbyId(id)

    }

    suspend fun updateTaskNotes(taskId: Long, notas: String){
        taskDao.updateNotas(taskId, notas)

    }


    private fun notifyWidgets() {
        val context = applicationContext ?: return
        val intent = Intent(context, TaskWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                AppWidgetManager.getInstance(context).getAppWidgetIds(
                    ComponentName(context, TaskWidget::class.java)
                ))
        }
        context.sendBroadcast(intent)
    }





}