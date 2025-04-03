package com.personal.voicememo.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personal.voicememo.domain.TodoItem

/**
 * A card that displays a todo item with options to edit and delete it.
 * 
 * @param todo The todo item to display
 * @param onUpdate Callback function that is called when the todo is updated
 * @param onDelete Callback function that is called when the delete button is clicked
 * @param modifier Modifier to be applied to the layout
 */
@Composable
fun TodoItemCard(
    todo: TodoItem,
    onUpdate: (TodoItem) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editedItem by remember { mutableStateOf(todo.item) }
    var editedTimeEstimate by remember { mutableStateOf(todo.timeEstimate) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (isEditing) {
                EditMode(
                    editedItem = editedItem,
                    editedTimeEstimate = editedTimeEstimate,
                    onItemChange = { editedItem = it },
                    onTimeEstimateChange = { editedTimeEstimate = it },
                    onCancel = { isEditing = false },
                    onSave = {
                        onUpdate(TodoItem(editedItem, editedTimeEstimate))
                        isEditing = false
                    }
                )
            } else {
                DisplayMode(
                    todo = todo,
                    onEdit = { isEditing = true },
                    onDelete = onDelete
                )
            }
        }
    }
}

/**
 * The editing mode of the todo item card.
 * 
 * @param editedItem The current value of the todo item text field
 * @param editedTimeEstimate The current value of the time estimate text field
 * @param onItemChange Callback function that is called when the todo item text field changes
 * @param onTimeEstimateChange Callback function that is called when the time estimate text field changes
 * @param onCancel Callback function that is called when the cancel button is clicked
 * @param onSave Callback function that is called when the save button is clicked
 */
@Composable
private fun EditMode(
    editedItem: String,
    editedTimeEstimate: String,
    onItemChange: (String) -> Unit,
    onTimeEstimateChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    OutlinedTextField(
        value = editedItem,
        onValueChange = onItemChange,
        label = { Text("Todo Item") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = editedTimeEstimate,
        onValueChange = onTimeEstimateChange,
        label = { Text("Time Estimate") },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onCancel) {
            Text("Cancel")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onSave) {
            Text("Save")
        }
    }
}

/**
 * The display mode of the todo item card.
 * 
 * @param todo The todo item to display
 * @param onEdit Callback function that is called when the edit button is clicked
 * @param onDelete Callback function that is called when the delete button is clicked
 */
@Composable
private fun DisplayMode(
    todo: TodoItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = todo.item,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "Estimated time: ${todo.timeEstimate}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row {
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete")
            }
        }
    }
} 