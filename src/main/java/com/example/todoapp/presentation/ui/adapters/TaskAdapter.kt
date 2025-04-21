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
import com.example.todoapp.domain.model.TaskEntity
import com.example.todoapp.presentation.ui.listener.onClickListener

class TaskAdapter (private val listTask: MutableList<TaskEntity>, private val listener: onClickListener):
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

        private var taskSelected = mutableSetOf<TaskEntity>()
        private var isSelectionMode = false

        lateinit var context: Context

        private var cardBackground = Color.parseColor("#2f2f2f")
    private var selectedCardBackground = Color.parseColor("#D5B30E")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context

        val view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val task = listTask.get(position)

        with(holder){
            setListener(task)
            binding.tvTarea.text = task.tarea
            binding.tvHora.text = task.hora
            binding.tvDias.text = formatDias(task.diasSemana, task.esRecurrente)

            binding.root.isSelected = taskSelected.contains(task)
            binding.root.isActivated = taskSelected.contains(task)

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

        return when{
            dias.size == 7 -> if (esRecurrente) "Todos los dias" else "Unica vez: Todos los dias"
            dias.size > 3 -> {
                val textoDias = dias.joinToString (", "){abreviateDia(it)}
                if (esRecurrente) "Varios dias" else "Unica vez: $textoDias "
            }
            else -> {
                val textoDias = dias.joinToString (", "){abreviateDialarga(it)}
                if (esRecurrente) "$textoDias" else "Unica vez: $textoDias"
            }
        }

    }

    private fun  abreviateDia(dia:String): String = when (dia){

            "LUN" -> "L"
            "MAR" -> "M"
            "MIE" -> "Mi"
            "JUE" -> "J"
            "VIE" -> "V"
            "SAB" -> "S"
            "DOM" -> "D"
            else -> dia

    }

    private fun  abreviateDialarga(dia:String): String = when (dia){

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

    fun update(task: List<TaskEntity>) {
        listTask.clear()
        listTask.addAll(task)
        notifyDataSetChanged()
    }

    fun getSelectedTasks(): Set<TaskEntity> = taskSelected

    fun isInSelectionMode (): Boolean = isSelectionMode

    fun clearSelection(){
        taskSelected.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }


    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){

        val binding = ItemTaskBinding.bind(view)

        fun setListener(taskEntity: TaskEntity){

                binding.root.setOnClickListener {
                    if (isSelectionMode) {
                        toggleSelection(taskEntity)
                    } else  { listener.onClick(taskEntity)
                    }
                }

            binding.root.setOnLongClickListener {
                   if (!isSelectionMode){
                       isSelectionMode = true
                       toggleSelection(taskEntity)
                       listener.onLongClick(taskEntity)
                   }
                true
                }

            binding.btnEdit.setOnClickListener {
                if (!isSelectionMode){
                    listener.onEditClick(taskEntity)
                }
            }

        }

        private fun toggleSelection(taskEntity: TaskEntity){
            if (taskSelected.contains(taskEntity)){
                taskSelected.remove(taskEntity)

            }else{
                taskSelected.add(taskEntity)
            }
            notifyItemChanged(adapterPosition)
        }

    }


}