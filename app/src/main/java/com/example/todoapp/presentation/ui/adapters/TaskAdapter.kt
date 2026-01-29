package com.example.todoapp.presentation.ui.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.databinding.ItemTaskBinding
import com.example.todoapp.domain.model.Task // <-- CAMBIADO
import com.example.todoapp.presentation.ui.listener.onClickListener

class TaskAdapter(
    private val listTask: MutableList<Task>, // <-- CAMBIADO: Task en vez de TaskEntity
    private val listener: onClickListener
) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    private var taskSelected = mutableSetOf<Task>() // <-- CAMBIADO
    private var isSelectionMode = false
    private lateinit var context: Context
    private var cardBackground = Color.parseColor("#2f2f2f")
    private var selectedCardBackground = Color.parseColor("#D5B30E")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = listTask[position]

        with(holder) {
            setListener(task)
            binding.tvTarea.text = task.title // <-- CAMBIADO: .title en vez de .tarea
            binding.tvHora.text = task.time // <-- CAMBIADO: .time en vez de .hora
            binding.tvDias.text = formatDias(task.weekDays, task.isRecurrent) // <-- CAMBIADO

            val isSelected = taskSelected.contains(task)
            binding.root.setCardBackgroundColor(
                if (isSelected) selectedCardBackground else cardBackground
            )
            binding.btnEdit.backgroundTintList = ColorStateList.valueOf(
                if (isSelected) selectedCardBackground else cardBackground
            )
            binding.root.cardElevation = if (isSelected) 8f else 2f
        }
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

    override fun getItemCount(): Int = listTask.size

    fun update(tasks: List<Task>) { // <-- CAMBIADO
        listTask.clear()
        listTask.addAll(tasks)
        notifyDataSetChanged()
    }

    fun getSelectedTasks(): Set<Task> = taskSelected // <-- CAMBIADO
    fun isInSelectionMode(): Boolean = isSelectionMode

    fun clearSelection() {
        taskSelected.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ItemTaskBinding.bind(view)

        fun setListener(task: Task) { // <-- CAMBIADO
            binding.root.setOnClickListener {
                if (isSelectionMode) {
                    toggleSelection(task)
                } else {
                    listener.onClick(task)
                }
            }

            binding.root.setOnLongClickListener {
                if (!isSelectionMode) {
                    isSelectionMode = true
                    toggleSelection(task)
                    listener.onLongClick(task)
                }
                true
            }

            binding.btnEdit.setOnClickListener {
                if (!isSelectionMode) {
                    listener.onEditClick(task)
                }
            }
        }

        private fun toggleSelection(task: Task) { // <-- CAMBIADO
            if (taskSelected.contains(task)) {
                taskSelected.remove(task)
            } else {
                taskSelected.add(task)
            }
            notifyItemChanged(adapterPosition)
        }
    }
}