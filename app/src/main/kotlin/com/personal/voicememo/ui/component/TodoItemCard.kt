package com.personal.voicememo.ui.component

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import com.personal.voicememo.domain.TodoItem

private const val TAG = "TodoItemCard"

/**
 * A card component that displays and allows editing of a todo item.
 * 
 * @param todo The todo item to display and edit
 * @param index The index of the todo in the list
 * @param onUpdate Callback function that is called when the todo is updated
 * @param onDelete Callback function that is called when the todo is deleted
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItemCard(
    todo: TodoItem,
    index: Int,
    onUpdate: (Int, TodoItem) -> Unit,
    onDelete: (Int) -> Unit
) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val isActive = remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editedItem by remember { mutableStateOf(todo.item) }
    var editedTimeEstimate by remember { mutableStateOf(todo.timeEstimate) }
    var editedProject by remember { mutableStateOf(todo.project) }

    // Handle lifecycle events
    DisposableEffect(lifecycle) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> isActive.value = true
                Lifecycle.Event.ON_PAUSE -> isActive.value = false
                else -> {}
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    // Only render if active to prevent state issues during configuration changes
    if (!isActive.value) {
        return
    }

    Log.d(TAG, "Rendering TodoItemCard for index $index with todo: $todo")

    if (hasError) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Error displaying todo item",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isEditing) {
                OutlinedTextField(
                    value = editedItem,
                    onValueChange = { editedItem = it },
                    label = { Text("Todo Item") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = editedTimeEstimate,
                    onValueChange = { editedTimeEstimate = it },
                    label = { Text("Time Estimate") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = editedProject,
                    onValueChange = { editedProject = it },
                    label = { Text("Project") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { 
                            isEditing = false
                            editedItem = todo.item
                            editedTimeEstimate = todo.timeEstimate
                            editedProject = todo.project
                        }
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(
                        onClick = { 
                            try {
                                onUpdate(index, TodoItem(editedItem, editedTimeEstimate, editedProject))
                                isEditing = false
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating todo at index $index", e)
                                hasError = true
                            }
                        }
                    ) {
                        Text("Save")
                    }
                }
            } else {
                Text(
                    text = todo.item,
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Time: ${todo.timeEstimate}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Text(
                        text = "Project: ${todo.project}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { isEditing = true }
                    ) {
                        Text("Edit")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { 
                            try {
                                onDelete(index)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error deleting todo at index $index", e)
                                hasError = true
                            }
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Todo")
                    }
                }
            }
        }
    }
} 