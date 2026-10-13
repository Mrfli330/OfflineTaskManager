
package com.example.offlinetaskmanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinetaskmanager.data.TaskEntity
import com.example.offlinetaskmanager.data.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    val allTasks: Flow<List<TaskEntity>> = repository.allTasks

    fun getTaskById(taskId: Int): Flow<TaskEntity?> {
        return repository.getTaskById(taskId)
    }

    fun insertTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.insertTask(task)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }
}
