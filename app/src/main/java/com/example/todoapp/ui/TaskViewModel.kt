package com.example.todoapp.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.todoapp.data.Task
import com.example.todoapp.data.TaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Holds the task list for the UI and forwards user actions to the repository. */
class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TaskRepository(application)

    val tasks: StateFlow<List<Task>> = repository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun addTask(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { repository.add(trimmed) }
    }

    fun setDone(task: Task, isDone: Boolean) {
        viewModelScope.launch { repository.setDone(task.id, isDone) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { repository.delete(task.id) }
    }
}
