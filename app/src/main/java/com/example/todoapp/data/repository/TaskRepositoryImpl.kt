package com.example.todoapp.data.repository

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.todoapp.data.local.TaskDao
import com.example.todoapp.data.local.mapper.TaskMapper
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.presentation.ui.widget.TaskWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepositoryImpl(
    private val taskDao: TaskDao,
    private val applicationContext: Context
) : TaskRepository {

    override val allTasks: Flow<List<Task>> = taskDao.getAllTasks().map { entities ->
        entities.map { TaskMapper.toDomain(it) }
    }

    override suspend fun addTask(task: Task): Long {
        val entity = TaskMapper.toEntity(task)
        val id = taskDao.addTask(entity)
        notifyWidgets()
        return id
    }

    override suspend fun updateTask(task: Task) {
        val entity = TaskMapper.toEntity(task)
        taskDao.updateTask(entity)
        notifyWidgets()
    }

    override suspend fun deleteTask(task: Task) {
        val entity = TaskMapper.toEntity(task)
        taskDao.deleteTask(entity)
        notifyWidgets()
    }

    override fun getTaskById(id: Long): Flow<Task> {
        return taskDao.getTaskById(id).map { entity ->
            TaskMapper.toDomain(entity)
        }
    }

    override suspend fun updateTaskNotes(taskId: Long, notes: String) {
        taskDao.updateNotas(taskId, notes)
        notifyWidgets()
    }


    suspend fun getAllTasksForWidget(): List<Task> {
        val entities = taskDao.getAllTaskForWidget()
        return entities.map { TaskMapper.toDomain(it) }
    }

    private fun notifyWidgets() {
        val context = applicationContext ?: return
        val intent = Intent(context, TaskWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TaskWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
        }
        context.sendBroadcast(intent)
    }
}