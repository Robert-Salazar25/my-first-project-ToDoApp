package com.example.todoapp.presentation.ui.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.todoapp.R
import com.example.todoapp.TodoApp
import com.example.todoapp.domain.model.Task
import com.example.todoapp.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TaskWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TaskRemoteViewsFactory(this.applicationContext)
    }
}

class TaskRemoteViewsFactory(private val context: Context) :
    RemoteViewsService.RemoteViewsFactory {

    // ✅ Usa Task (domain), NO TaskEntity
    private var tasks: List<Task> = emptyList()

    private val taskRepository: TaskRepository by lazy {
        (context.applicationContext as TodoApp).taskRepository
    }

    override fun onCreate() {
        // Inicialización si es necesaria
    }

    override fun onDataSetChanged() {
        tasks = runBlocking {
            taskRepository.allTasks.first()
                .filter { it.id != -1L }
        }
    }

    override fun onDestroy() {
        // Limpieza si es necesaria
    }

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        val task = tasks[position]
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)

        views.setTextViewText(R.id.widget_task, task.title)
        views.setTextViewText(
            R.id.widget_time,
            "Hora: ${formatTimeForWidget(task.time)} - " +
                    "Dias: ${formatDias(task.weekDays, task.isRecurrent)}"
        )
        return views
    }

    private fun formatDias(diasSemana: String, esRecurrente: Boolean): String {
        if (diasSemana.isEmpty()) return "Sin seleccionar dias"

        val dias = diasSemana.split(",").filter { it.isNotEmpty() }

        return when {
            dias.size == 7 -> if (esRecurrente) "Todos los dias"
            else "Unica vez: Todos los dias"
            dias.size > 3 -> {
                val textoDias = dias.joinToString(", ") { abreviateDia(it) }
                if (esRecurrente) "Varios dias" else "Unica vez: $textoDias"
            }
            else -> {
                val textoDias = dias.joinToString(", ") { abreviateDialarga(it) }
                if (esRecurrente) textoDias else "Unica vez: $textoDias"
            }
        }
    }

    private fun abreviateDia(dia: String): String = when (dia) {
        "LUN" -> "L"
        "MAR" -> "M"
        "MIE" -> "Mi"
        "JUE" -> "J"
        "VIE" -> "V"
        "SAB" -> "S"
        "DOM" -> "D"
        else -> dia
    }

    private fun abreviateDialarga(dia: String): String = when (dia) {
        "LUN" -> "Lun"
        "MAR" -> "Mar"
        "MIE" -> "Mie"
        "JUE" -> "Jue"
        "VIE" -> "Vie"
        "SAB" -> "Sab"
        "DOM" -> "Dom"
        else -> dia
    }

    private fun formatTimeForWidget(time: String): String {
        return try {
            val parts = time.split(":")
            val hour = parts[0].padStart(2, '0')
            val minute = parts.getOrElse(1) { "00" }.padStart(2, '0')
            "$hour:$minute"
        } catch (e: Exception) {
            time
        }
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = position.toLong()
    override fun hasStableIds(): Boolean = true
}