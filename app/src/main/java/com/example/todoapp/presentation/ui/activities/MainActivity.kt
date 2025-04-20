package com.example.todoapp.presentation.ui.activities


import android.app.TimePickerDialog

import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import android.Manifest
import android.os.Build
import com.example.todoapp.R
import com.example.todoapp.presentation.ui.adapters.TaskAdapter
import com.example.todoapp.presentation.ui.viewModel.TaskViewModelFactory
import com.example.todoapp.TodoApp
import com.example.todoapp.presentation.ui.viewModel.TaskViewModel
import com.example.todoapp.domain.model.TaskEntity
import com.example.todoapp.presentation.ui.listener.onClickListener
import java.util.Locale


class MainActivity : AppCompatActivity(), onClickListener {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var linearLayoutManager: RecyclerView.LayoutManager
    private lateinit var binding: ActivityMainBinding

    private val taskViewModel: TaskViewModel by viewModels{
        TaskViewModelFactory((application as TodoApp).taskRepository,
            applicationContext)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkNotificationPermission()


        setUpRecyclerView()
        observeTasks()
        showExampleTask()

        binding.imAddTask.setOnClickListener{
            showAddTaskDialog()
        }

        binding.bottomDeleteBar.delete.setOnClickListener{
            deleteSelectedTask()

        }

    }



    private fun showExampleTask() {

        taskViewModel.allTaks.observe(this, {
            tasks ->
            if (tasks.isEmpty()){

                val actualTime = getActualTime()
                val task = TaskEntity(id = -1L ,tarea = "Añade una tarea!", hora = actualTime,
                    diasSemana = "LUN", esRecurrente = false)
                taskViewModel.addTask(task)
            }

        })
    }

    private fun getActualTime(): String{
        val calendar = Calendar.getInstance()
        val formato = SimpleDateFormat("HH:mm", Locale.getDefault())
        return formato.format(calendar.time)

    }

    private fun getActualDate(): String{
        val calendar = Calendar.getInstance()
        val formato = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formato.format(calendar.time)

    }


    private fun observeTasks() {
        taskViewModel.allTaks.observe(this, Observer { task ->
            task?.let {
                taskAdapter.update(task)
            }
        })
    }

    private fun setUpRecyclerView() {
        taskAdapter = TaskAdapter(mutableListOf(), this)
        linearLayoutManager = LinearLayoutManager(this)

        binding.recyclerView.apply {
            adapter = taskAdapter
            layoutManager = linearLayoutManager
        }

        taskAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver(){

            override fun onChanged() {
                super.onChanged()
                updateBottomDeleteBar()
            }
        })

    }

    private fun updateBottomDeleteBar() {
        val bottomDeleteBar = findViewById<ConstraintLayout>(R.id.bottomDeleteBar)
        if (taskAdapter.isInSelectionMode()){
            bottomDeleteBar.visibility = View.VISIBLE

        }else{
            bottomDeleteBar.visibility = View.GONE
        }

    }

    private fun exitSelectionMode() {
        taskAdapter.clearSelection()
        updateBottomDeleteBar()
    }

    override fun onBackPressed() {

        if (taskAdapter.isInSelectionMode()){
            exitSelectionMode()

        }else {

           super.onBackPressed()

        }



    }

    private fun deleteSelectedTask(){
        val selectesTask = taskAdapter.getSelectedTasks()
        selectesTask.forEach{
            task ->
            taskViewModel.deleteTask(task)
            Toast.makeText(this, getString(R.string.delete_task), Toast.LENGTH_SHORT)
                .show()

        }
        taskAdapter.clearSelection()
    }

    override fun onLongClick(taskEntity: TaskEntity) {
        updateBottomDeleteBar()
    }


    override fun onClick(taskEntity: TaskEntity) {
        val intent = Intent(this, NotasActivity::class.java).apply {
            putExtra("TASK_ID", taskEntity.id)
        }

        startActivity(intent)
    }

    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setWindowAnimations(R.style.DialogAnimation)

        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val btnSelectTime = dialogView.findViewById<Button>(R.id.btnSelectTime)
        val btnSaveTask = dialogView.findViewById<Button>(R.id.btnSaveTask)
        val btnSelectDays = dialogView.findViewById<Button>(R.id.btnSelectDays)


        var selectedDays = emptyList<String>()
        var isRecurrente = false
        var selectTime = ""


        btnSelectDays.setOnClickListener {
            Log.d("DIALOG", "Botón selectDays clickeado")
            showDaySelectionDialog(selectedDays,isRecurrente ) { dias, recurrente ->
                selectedDays = dias
                isRecurrente = recurrente
                btnSelectDays.text = when{
                    dias.isEmpty() -> "Seleccionar dias"
                    recurrente -> "Cada ${dias.joinToString(", ")}"
                    else -> "Unica vez: ${dias.joinToString(", ")}"

                }

            }

        }


        btnSelectTime.setOnClickListener{
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this,
                R.style.CustomTimePickerDialogTheme,
                {_, hourOfDay, minuteSelected ->
                    selectTime = "$hourOfDay:$minuteSelected"
                    btnSelectTime.text = selectTime
                },
                hour,
                minute,
                false)

            timePickerDialog.show()


        }

        btnSaveTask.setOnClickListener{
            val taskName = etTaskName.text.toString()
            if (taskName.isNotEmpty() && selectTime.isNotEmpty() && selectedDays.isNotEmpty()){

                val timeParts = selectTime.split(":")
                val hour = timeParts[0].toInt()
                val minute = timeParts.getOrElse(1) { "0" }.toInt()

                val calendarNow = Calendar.getInstance()
                val calendarTask = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }

                if (calendarTask.timeInMillis > calendarNow.timeInMillis + 60000){

                    val newTask = TaskEntity(
                        tarea = taskName,
                        hora = selectTime,
                        diasSemana = selectedDays.joinToString(","),
                        esRecurrente = isRecurrente

                    )
                    taskViewModel.addTask(newTask)
                    dialog.dismiss()
                    Toast.makeText(this, getString(R.string.add_task_secces), Toast.LENGTH_SHORT)
                        .show()
                }else{
                    Toast.makeText(this, "Por favor, selecciona una hora futura", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, getString(R.string.parameters_empty), Toast.LENGTH_SHORT)
                .show()

            }
        }

        dialog.show()

    }

    override fun onEditClick(taskEntity: TaskEntity) {
        showEditTaskDialog(taskEntity)
    }

    private fun showEditTaskDialog(taskEntity: TaskEntity) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setWindowAnimations(R.style.DialogAnimation)

        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val btnSelectTime = dialogView.findViewById<Button>(R.id.btnSelectTime)
        val btnSaveTask = dialogView.findViewById<Button>(R.id.btnSaveTask)
        val btnSelectDays = dialogView.findViewById<Button>(R.id.btnSelectDays)


        etTaskName.setText(taskEntity.tarea)
        btnSelectTime.text = taskEntity.hora

        val diasFormateados = taskEntity.diasSemana.split(",")
            .filter { it.isNotEmpty() }
            .joinToString(", ") { dia ->
                when(dia) {
                    "LUN" -> "Lun"
                    "MAR" -> "Mar"
                    "MIE" -> "Mié"
                    "JUE" -> "Jue"
                    "VIE" -> "Vie"
                    "SAB" -> "Sáb"
                    "DOM" -> "Dom"
                    else -> dia
                }
            }

        btnSelectDays.text = when{
            taskEntity.diasSemana.isEmpty() -> "Seleccionar dias"
            taskEntity.esRecurrente -> "Cada $diasFormateados"
            else -> "Unica vez: $diasFormateados"
        }

        var selectedDays = taskEntity.diasSemana.split(",").filter { it.isNotEmpty() }
        var isRecurrente = taskEntity.esRecurrente
        var selectedTime = taskEntity.hora

        btnSelectDays.setOnClickListener {
            Log.d("DIALOG", "Botón selectDays clickeado")
            showDaySelectionDialog(selectedDays,isRecurrente ) { dias, recurrente ->
                selectedDays = dias
                isRecurrente = recurrente
                btnSelectDays.text = when{
                    dias.isEmpty() -> "Seleccionar dias"
                    recurrente -> "Cada ${dias.joinToString(", ")}"
                    else -> "Unica vez: ${dias.joinToString(", ")}"

                }

            }

        }


        btnSelectTime.setOnClickListener{
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(this,
                R.style.CustomTimePickerDialogTheme,
                {_, hourOfDay, minuteSelected ->
                    selectedTime = "$hourOfDay:$minuteSelected"
                    btnSelectTime.text = selectedTime
                },
                hour,
                minute,
                false)

            timePickerDialog.show()


        }

        btnSaveTask.setOnClickListener{
            val taskName = etTaskName.text.toString()
            if (taskName.isNotEmpty() && selectedTime.isNotEmpty() && selectedDays.isNotEmpty()){

                val updateTask = taskEntity.copy(
                    tarea = taskName,
                    hora = selectedTime,
                    diasSemana = selectedDays.joinToString (","),
                    esRecurrente = isRecurrente
                )

                taskViewModel.updateTask(updateTask)
                dialog.dismiss()
                Toast.makeText(this, getString(R.string.edit_task_secces), Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(this, getString(R.string.parameters_empty), Toast.LENGTH_SHORT)
                    .show()

            }
        }

        dialog.show()


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100){
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDaySelectionDialog(selectedDays: List<String>,
                                       isRecurrente: Boolean,
                                       onDiasSelected: (List<String>, Boolean) -> Unit){
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_recurrence, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val dayCheckBoxes = mapOf(
            "LUN" to dialogView.findViewById<CheckBox>(R.id.cbLunes),
            "MAR" to dialogView.findViewById<CheckBox>(R.id.cbMartes),
            "MIE" to dialogView.findViewById<CheckBox>(R.id.cbMiercoles),
            "JUE" to dialogView.findViewById<CheckBox>(R.id.cbJueves),
            "VIE" to dialogView.findViewById<CheckBox>(R.id.cbViernes),
            "SAB" to dialogView.findViewById<CheckBox>(R.id.cbSabado),
            "DOM" to dialogView.findViewById<CheckBox>(R.id.cbDomingo)
        )

        selectedDays.forEach { dia ->
            dayCheckBoxes[dia]?.isChecked = true
        }

        val cbRecurrente = dialogView.findViewById<CheckBox>(R.id.cbRecurrente).apply {
            isChecked = isRecurrente
        }

        dialogView.findViewById<Button>(R.id.btnConfirmDays).setOnClickListener {
            val selectDays = dayCheckBoxes
                .filter { it.value.isChecked }
                .keys
                .toList()

            onDiasSelected(selectDays, cbRecurrente.isChecked)
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permiso ya concedido
                    Log.d("Notification", "Permisos de notificación concedidos")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Mostrar explicación al usuario
                    showPermissionExplanationDialog()
                }
                else -> {
                    // Solicitar permiso directamente
                    requestPermissions(
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permiso necesario")
            .setMessage("Las notificaciones son necesarias para recordarte tus tareas")
            .setPositiveButton("Entendido") { _, _ ->
                requestPermissions(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
    }




}