package com.example.todoapp.presentation.ui.activities

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.todoapp.R
import com.example.todoapp.TodoApp
import com.example.todoapp.databinding.TaskNotesBinding
import com.example.todoapp.presentation.ui.viewModel.TaskViewModel
import com.example.todoapp.presentation.ui.viewModel.TaskViewModelFactory
import kotlinx.coroutines.launch

class NotasActivity : AppCompatActivity() {

    private lateinit var binding: TaskNotesBinding
    private val taskViewModel: TaskViewModel by viewModels {
        TaskViewModelFactory(
            (application as TodoApp).taskRepository,
            applicationContext
        )
    }
    private var currentTaskId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TaskNotesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        currentTaskId = intent.getLongExtra("TASK_ID", -1)

        if (currentTaskId == -1L) {
            Toast.makeText(this, "Error: Tarea no válida", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // OBSERVAR CON FLOW EN VEZ DE LIVEDATA
        observeTask()

        binding.btnSave.setOnClickListener {
            val notas = binding.etNotas.text.toString()
            if (notas.isNotEmpty()) {
                taskViewModel.updateTaskNotes(currentTaskId, notas) // <-- CAMBIADO: updateTaskNotas → updateTaskNotes
                Toast.makeText(this, "Notas Guardada", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Escribe algo antes de guardar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeTask() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                taskViewModel.getTaskById(currentTaskId).collect { task -> // <-- CAMBIADO: Flow en vez de LiveData
                    task?.let {
                        binding.etNotas.setText(it.notes) // <-- CAMBIADO: .notes
                    }
                }
            }
        }
    }
}