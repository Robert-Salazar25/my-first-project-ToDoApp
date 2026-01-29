package com.example.todoapp.presentation.ui.viewModel

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.presentation.ui.receivers.TaskNotificationReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(
    private val taskRepository: TaskRepository,
    private val applicationContext: Context
) : ViewModel() {

    // ✅ Usa Task (domain), NO TaskEntity
    val allTasks: Flow<List<Task>> = taskRepository.allTasks

    // ✅ Método para obtener tarea por ID (Flow en vez de LiveData)
    fun getTaskById(id: Long): Flow<Task> {
        return taskRepository.getTaskById(id)
    }

    fun addTask(task: Task) = viewModelScope.launch {
        val taskId = taskRepository.addTask(task)
        val taskWithId = task.copy(id = taskId)
        scheduleTaskNotification(applicationContext, taskWithId)
    }

    fun updateTask(task: Task) = viewModelScope.launch {
        cancelTaskNotification(task)
        taskRepository.updateTask(task)
        scheduleTaskNotification(applicationContext, task)
    }

    fun deleteTask(task: Task) = viewModelScope.launch {
        cancelTaskNotification(task)
        taskRepository.deleteTask(task)
    }

    fun updateTaskNotes(taskId: Long, notes: String) = viewModelScope.launch {
        taskRepository.updateTaskNotes(taskId, notes)
    }

    private fun scheduleTaskNotification(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val timeParts = task.time.split(":")
        val hour = timeParts[0].toInt()
        val minute = timeParts.getOrElse(1) { "0" }.toInt()

        task.weekDays.split(",").forEach { dia ->
            if (dia.isEmpty()) return@forEach

            val dayOfWeek = when (dia) {
                "LUN" -> Calendar.MONDAY
                "MAR" -> Calendar.TUESDAY
                "MIE" -> Calendar.WEDNESDAY
                "JUE" -> Calendar.THURSDAY
                "VIE" -> Calendar.FRIDAY
                "SAB" -> Calendar.SATURDAY
                "DOM" -> Calendar.SUNDAY
                else -> return@forEach
            }

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    if (task.isRecurrent) {
                        add(Calendar.DAY_OF_YEAR, 7)
                    } else {
                        Log.d("Notification", "No se programa notificación para hora pasada")
                        return@forEach
                    }
                }
            }

            val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
                putExtra("TASK_NAME", task.title)
                putExtra("TASK_ID", task.id)
                putExtra("TASK_DAY", dia)
            }

            val requestCode = "${task.id}_$dia".hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val alarmClockInfo = AlarmManager.AlarmClockInfo(
                    calendar.timeInMillis,
                    pendingIntent
                )
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("Notification", "Notif programada para ${calendar.time} (Día: $dia)")
        }
    }

    private fun cancelTaskNotification(task: Task) {
        try {
            val alarmManager =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                        as NotificationManager

            val mainIntent = Intent(applicationContext, TaskNotificationReceiver::class.java)
            val mainPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                task.id.toInt(),
                mainIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            mainPendingIntent?.let { alarmManager.cancel(it) }

            task.weekDays.split(",").forEach { dia ->
                val dayIntent = Intent(applicationContext, TaskNotificationReceiver::class.java)
                val dayPendingIntent = PendingIntent.getBroadcast(
                    applicationContext,
                    "${task.id}_$dia".hashCode(),
                    dayIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                dayPendingIntent?.let { alarmManager.cancel(it) }
            }

            notificationManager.cancel(task.id.toInt())
        } catch (e: Exception) {
            Log.e("TaskNotification",
                "Error cancelando notificaciones para tarea ${task.id}", e)
        }
    }
}