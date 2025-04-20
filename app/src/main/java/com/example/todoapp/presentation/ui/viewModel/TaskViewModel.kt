package com.example.todoapp.presentation.ui.viewModel

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.presentation.ui.receivers.TaskNotificationReceiver
import com.example.todoapp.domain.repository.TaskRepository
import com.example.todoapp.domain.model.TaskEntity
import com.example.todoapp.presentation.ui.widget.TaskWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel (private val taskRepository: TaskRepository,
                     private val applicationContext : Context): ViewModel() {

    val allTaks: Flow<List<TaskEntity>> = taskRepository.allTasks

    fun addTask(taskEntity: TaskEntity) = viewModelScope.launch {
        val taskId = taskRepository.addTask(taskEntity)
        taskEntity.id = taskId
        scheduleTaskNotification(applicationContext, taskEntity.copy(id = taskId))

    }

     fun getTaskbyId(id: Long): LiveData<TaskEntity> {
        return taskRepository.getTaskbyId(id)

    }

    fun updateTask(taskEntity: TaskEntity) = viewModelScope.launch {
        cancelTaskNotification(taskEntity)
        taskRepository.updateTask(taskEntity)
        scheduleTaskNotification(applicationContext, taskEntity)

    }

    fun deleteTask(taskEntity: TaskEntity) = viewModelScope.launch {
        cancelTaskNotification(taskEntity)
        taskRepository.deleteTask(taskEntity)
    }

    fun updateTaskNotas(taskId: Long, notas: String){
        viewModelScope.launch {
            taskRepository.updateTaskNotes(taskId, notas)
        }
    }


    private fun scheduleTaskNotification(context: Context, taskEntity: TaskEntity){
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val timeParts = taskEntity.hora.split(":")
        val hour = timeParts[0].toInt()
        val minute =timeParts.getOrElse(1){"0"}.toInt()

        taskEntity.diasSemana.split(",").forEach { dia ->
            if (dia.isEmpty()) return@forEach

            val dayOfWeek = when(dia){
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
                    if (taskEntity.esRecurrente) {
                        add(Calendar.DAY_OF_YEAR, 7) // Programar para la próxima semana si es recurrente
                    } else {
                        Log.d("Notification", "No se programa notificación para hora pasada")
                        return@forEach // Salir sin programar si no es recurrente
                    }
                }
            }

            val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
                putExtra("TASK_NAME", taskEntity.tarea)
                putExtra("TASK_ID", taskEntity.id)
                putExtra("TASK_DAY", dia)
            }

            val requestCode = "${taskEntity.id}_$dia".hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                val alarmClockInfo = AlarmManager.AlarmClockInfo(calendar.timeInMillis,pendingIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }else{
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("Notification", "Notif programada para ${calendar.time} (Día: $dia)")
        }
    }

    private fun cancelTaskNotification(taskEntity: TaskEntity) {
        try {
            val alarmManager =
                applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val notificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val mainIntent = Intent(applicationContext, TaskNotificationReceiver::class.java)
            val mainPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                taskEntity.id.toInt(),
                mainIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            mainPendingIntent?.let {
                alarmManager.cancel(it)
            }

            taskEntity.diasSemana.split(",").forEach { dia ->
                val dayIntent = Intent(applicationContext, TaskNotificationReceiver::class.java)
                val dayPendingIntent = PendingIntent.getBroadcast(
                    applicationContext,
                    "${taskEntity.id}_$dia".hashCode(),
                    dayIntent,
                    PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
                )
                dayPendingIntent?.let {
                    alarmManager.cancel(it)
                }
            }

            notificationManager.cancel(taskEntity.id.toInt())
        } catch (e: Exception){
            Log.e("TaskNotification", "Error cancelaando notificaciones para tarea " +
                    "${taskEntity.id}", e)
        }

    }


}