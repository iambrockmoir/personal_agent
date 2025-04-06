package com.personal.voicememo.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personal.voicememo.domain.TodoItem
import com.personal.voicememo.domain.TodoList
import com.personal.voicememo.service.TodoService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

/**
 * ViewModel for managing todos extracted from transcripts.
 * 
 * This ViewModel handles the business logic for:
 * - Extracting todos from transcripts
 * - Adding, updating, and deleting todos
 * - Saving todos to Google Sheets
 */
@HiltViewModel
class TodoViewModel @Inject constructor(
    private val todoService: TodoService
) : ViewModel() {
    companion object {
        private const val TAG = "TodoViewModel"
    }

    private val mutex = Mutex()
    private val _uiState = MutableStateFlow<TodoUiState>(TodoUiState.Initial)
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    private val _todos = MutableStateFlow<List<TodoItem>>(emptyList())
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    /**
     * Extracts todos from a transcript using the OpenAI service.
     * 
     * @param transcript The transcript text to extract todos from
     */
    fun extractTodos(transcript: String) {
        viewModelScope.launch {
            try {
                mutex.withLock {
                    _uiState.value = TodoUiState.Loading
                }
                val todoList = todoService.extractTodos(transcript)
                mutex.withLock {
                    _todos.value = todoList.todos
                    _uiState.value = TodoUiState.Success
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to extract todos", e)
                mutex.withLock {
                    _uiState.value = TodoUiState.Error(e.message ?: "Failed to extract todos")
                }
            }
        }
    }

    /**
     * Updates a todo at the specified index.
     * 
     * @param index The index of the todo to update
     * @param updatedTodo The updated todo item
     */
    fun updateTodo(index: Int, updatedTodo: TodoItem) {
        viewModelScope.launch {
            try {
                mutex.withLock {
                    val currentTodos = _todos.value.toMutableList()
                    if (index in currentTodos.indices) {
                        currentTodos[index] = updatedTodo
                        _todos.value = currentTodos
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update todo", e)
                mutex.withLock {
                    _uiState.value = TodoUiState.Error(e.message ?: "Failed to update todo")
                }
            }
        }
    }

    /**
     * Deletes a todo at the specified index.
     * 
     * @param index The index of the todo to delete
     */
    fun deleteTodo(index: Int) {
        viewModelScope.launch {
            try {
                mutex.withLock {
                    val currentTodos = _todos.value.toMutableList()
                    if (index in currentTodos.indices) {
                        currentTodos.removeAt(index)
                        _todos.value = currentTodos
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete todo", e)
                mutex.withLock {
                    _uiState.value = TodoUiState.Error(e.message ?: "Failed to delete todo")
                }
            }
        }
    }

    /**
     * Adds a new todo to the list.
     * 
     * @param todo The todo item to add
     */
    fun addTodo(todo: TodoItem) {
        viewModelScope.launch {
            try {
                mutex.withLock {
                    val currentTodos = _todos.value.toMutableList()
                    currentTodos.add(todo)
                    _todos.value = currentTodos
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to add todo", e)
                mutex.withLock {
                    _uiState.value = TodoUiState.Error(e.message ?: "Failed to add todo")
                }
            }
        }
    }

    /**
     * Saves the current list of todos to Google Sheets.
     */
    fun saveTodos() {
        viewModelScope.launch {
            try {
                mutex.withLock {
                    _uiState.value = TodoUiState.Loading
                }
                val success = todoService.saveTodoList(TodoList(_todos.value))
                mutex.withLock {
                    if (success) {
                        _uiState.value = TodoUiState.Success
                        // Clear the todos list after successful save
                        _todos.value = emptyList()
                    } else {
                        _uiState.value = TodoUiState.Error("Failed to save todos")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save todos", e)
                mutex.withLock {
                    _uiState.value = TodoUiState.Error(e.message ?: "Failed to save todos")
                }
            }
        }
    }
}

sealed class TodoUiState {
    object Initial : TodoUiState()
    object Loading : TodoUiState()
    object Success : TodoUiState()
    data class Error(val message: String) : TodoUiState()
} 