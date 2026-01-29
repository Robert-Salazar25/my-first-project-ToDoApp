package com.example.todoapp.presentation.ui.activities

import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.todoapp.R
import com.example.todoapp.TodoApp
import com.example.todoapp.databinding.ActivityMainBinding
import com.example.todoapp.domain.model.Task // <-- CAMBIADO
import com.example.todoapp.presentation.ui.adapters.TaskAdapter
import com.example.todoapp.presentation.ui.listener.onClickListener
import com.example.todoapp.presentation.ui.viewModel.TaskViewModel
import com.example.todoapp.presentation.ui.viewModel.TaskViewModelFactory
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity(), onClickListener {

    private lateinit var taskAdapter: TaskAdapter
    private lateinit var binding: ActivityMainBinding

    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(
            (application as TodoApp).taskRepository,
            applicationContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkNotificationPermission()
        setUpRecyclerView()
        observeTasks()
        showExampleTask()

        binding.imAddTask.setOnClickListener {
            showAddTaskDialog()
        }

        binding.bottomDeleteBar.delete.setOnClickListener {
            deleteSelectedTask()
        }
    }

    private fun showExampleTask() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskViewModel.allTasks.collect { tasks -> // <-- CAMBIADO: allTaks → allTasks
                    if (tasks.isEmpty()) {
                        val actualTime = getActualTime()
                        val task = Task( // <-- CAMBIADO: Task en vez de TaskEntity
                            id = -1L,
                            title = "Añade una tarea!", // <-- CAMBIADO: title en vez de tarea
                            time = actualTime, // <-- CAMBIADO: time en vez de hora
                            notes = "", // <-- CAMBIADO: notes en vez de notas
                            weekDays = "LUN", // <-- CAMBIADO: weekDays en vez de diasSemana
                            isRecurrent = false // <-- CAMBIADO: isRecurrent en vez de esRecurrente
                        )
                        taskViewModel.addTask(task)
                    }
                }
            }
        }
    }

    private fun getActualTime(): String {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        return String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskViewModel.allTasks.collect { tasks -> // <-- CAMBIADO: allTaks → allTasks
                    taskAdapter.update(tasks)
                }
            }
        }
    }

    private fun setUpRecyclerView() {
        taskAdapter = TaskAdapter(mutableListOf(), this)
        binding.recyclerView.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        taskAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                updateBottomDeleteBar()
            }
        })
    }

    private fun updateBottomDeleteBar() {
        val bottomDeleteBar = findViewById<ConstraintLayout>(R.id.bottomDeleteBar)
        bottomDeleteBar.visibility = if (taskAdapter.isInSelectionMode()) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    private fun exitSelectionMode() {
        taskAdapter.clearSelection()
        updateBottomDeleteBar()
    }

    override fun onBackPressed() {
        if (taskAdapter.isInSelectionMode()) {
            exitSelectionMode()
        } else {
            super.onBackPressed()
        }
    }

    private fun deleteSelectedTask() {
        val selectedTasks = taskAdapter.getSelectedTasks()
        selectedTasks.forEach { task ->
            taskViewModel.deleteTask(task)
            Toast.makeText(this, getString(R.string.delete_task), Toast.LENGTH_SHORT).show()
        }
        taskAdapter.clearSelection()
    }

    override fun onLongClick(task: Task) { // <-- CAMBIADO
        updateBottomDeleteBar()
    }

    override fun onClick(task: Task) { // <-- CAMBIADO
        val intent = Intent(this, NotasActivity::class.java).apply {
            putExtra("TASK_ID", task.id)
        }
        startActivity(intent)
    }

    override fun onEditClick(task: Task) { // <-- CAMBIADO
        showEditTaskDialog(task)
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
        var selectedTime = ""

        btnSelectDays.setOnClickListener {
            Log.d("DIALOG", "Botón selectDays clickeado")
            showDaySelectionDialog(selectedDays, isRecurrente) { dias, recurrente ->
                selectedDays = dias
                isRecurrente = recurrente
                btnSelectDays.text = when {
                    dias.isEmpty() -> "Seleccionar dias"
                    recurrente -> "Cada ${dias.joinToString(", ")}"
                    else -> "Unica vez: ${dias.joinToString(", ")}"
                }
            }
        }

        btnSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                R.style.CustomTimePickerDialogTheme,
                { _, hourOfDay, minuteSelected ->
                    selectedTime = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        hourOfDay,
                        minuteSelected
                    )
                    btnSelectTime.text = selectedTime
                },
                hour,
                minute,
                false
            )
            timePickerDialog.show()
        }

        btnSaveTask.setOnClickListener {
            val taskName = etTaskName.text.toString()
            if (taskName.isNotEmpty() && selectedTime.isNotEmpty() && selectedDays.isNotEmpty()) {
                val timeParts = selectedTime.split(":")
                val hour = timeParts[0].toInt()
                val minute = timeParts[1].toInt()

                val hasFutureNotification = selectedDays.any { dia ->
                    val dayOfWeek = when (dia) {
                        "LUN" -> Calendar.MONDAY
                        "MAR" -> Calendar.TUESDAY
                        "MIE" -> Calendar.WEDNESDAY
                        "JUE" -> Calendar.THURSDAY
                        "VIE" -> Calendar.FRIDAY
                        "SAB" -> Calendar.SATURDAY
                        "DOM" -> Calendar.SUNDAY
                        else -> return@any false
                    }

                    Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, dayOfWeek)
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis > System.currentTimeMillis()
                }

                if (hasFutureNotification || isRecurrente) {
                    val newTask = Task( // <-- CAMBIADO: Task en vez de TaskEntity
                        title = taskName, // <-- CAMBIADO: title en vez de tarea
                        time = selectedTime, // <-- CAMBIADO: time en vez de hora
                        notes = "", // <-- CAMBIADO: notes en vez de notas
                        weekDays = selectedDays.joinToString(","), // <-- CAMBIADO: weekDays
                        isRecurrent = isRecurrente // <-- CAMBIADO: isRecurrent
                    )
                    taskViewModel.addTask(newTask)
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        getString(R.string.add_task_secces),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "La hora debe ser futura para al menos un día seleccionado",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.parameters_empty),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.show()
    }

    private fun showEditTaskDialog(task: Task) { // <-- CAMBIADO: Task en vez de TaskEntity
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setWindowAnimations(R.style.DialogAnimation)

        val etTaskName = dialogView.findViewById<EditText>(R.id.etTaskName)
        val btnSelectTime = dialogView.findViewById<Button>(R.id.btnSelectTime)
        val btnSaveTask = dialogView.findViewById<Button>(R.id.btnSaveTask)
        val btnSelectDays = dialogView.findViewById<Button>(R.id.btnSelectDays)

        etTaskName.setText(task.title) // <-- CAMBIADO: .title
        btnSelectTime.text = task.time // <-- CAMBIADO: .time

        val diasFormateados = task.weekDays.split(",") // <-- CAMBIADO: .weekDays
            .filter { it.isNotEmpty() }
            .joinToString(", ") { dia ->
                when (dia) {
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

        btnSelectDays.text = when {
            task.weekDays.isEmpty() -> "Seleccionar dias" // <-- CAMBIADO: .weekDays
            task.isRecurrent -> "Cada $diasFormateados" // <-- CAMBIADO: .isRecurrent
            else -> "Unica vez: $diasFormateados"
        }

        var selectedDays = task.weekDays.split(",").filter { it.isNotEmpty() } // <-- CAMBIADO
        var isRecurrente = task.isRecurrent // <-- CAMBIADO
        var selectedTime = task.time // <-- CAMBIADO

        btnSelectDays.setOnClickListener {
            showDaySelectionDialog(selectedDays, isRecurrente) { dias, recurrente ->
                selectedDays = dias
                isRecurrente = recurrente
                btnSelectDays.text = when {
                    dias.isEmpty() -> "Seleccionar dias"
                    recurrente -> "Cada ${dias.joinToString(", ")}"
                    else -> "Unica vez: ${dias.joinToString(", ")}"
                }
            }
        }

        btnSelectTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                R.style.CustomTimePickerDialogTheme,
                { _, hourOfDay, minuteSelected ->
                    selectedTime = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        hourOfDay,
                        minuteSelected
                    )
                    btnSelectTime.text = selectedTime
                },
                hour,
                minute,
                false
            )
            timePickerDialog.show()
        }

        btnSaveTask.setOnClickListener {
            val taskName = etTaskName.text.toString()
            if (taskName.isNotEmpty() && selectedTime.isNotEmpty() && selectedDays.isNotEmpty()) {
                val updatedTask = task.copy( // <-- CAMBIADO: task.copy() en vez de taskEntity.copy()
                    title = taskName,
                    time = selectedTime,
                    weekDays = selectedDays.joinToString(","),
                    isRecurrent = isRecurrente
                )
                taskViewModel.updateTask(updatedTask)
                dialog.dismiss()
                Toast.makeText(
                    this,
                    getString(R.string.edit_task_secces),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.parameters_empty),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        dialog.show()
    }

    private fun showDaySelectionDialog(
        selectedDays: List<String>,
        isRecurrente: Boolean,
        onDiasSelected: (List<String>, Boolean) -> Unit
    ) {
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
            val selectedDays = dayCheckBoxes
                .filter { it.value.isChecked }
                .keys
                .toList()

            onDiasSelected(selectedDays, cbRecurrente.isChecked)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("Notification", "Permisos de notificación concedidos")
                }
                shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    showPermissionExplanationDialog()
                }
                else -> {
                    requestPermissions(
                        arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
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
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notificaciones activadas", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 100
    }
}