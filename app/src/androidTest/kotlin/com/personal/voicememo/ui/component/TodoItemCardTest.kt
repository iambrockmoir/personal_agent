package com.personal.voicememo.ui.component

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.personal.voicememo.domain.TodoItem
import com.personal.voicememo.ui.theme.VoiceMemoTheme
import org.junit.Rule
import org.junit.Test

class TodoItemCardTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displayMode_showsCorrectContent() {
        val todo = TodoItem("Test task", "2 hours")
        
        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoItemCard(
                    todo = todo,
                    onUpdate = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Test task").assertExists()
        composeTestRule.onNodeWithText("Estimated time: 2 hours").assertExists()
        composeTestRule.onNodeWithContentDescription("Edit").assertExists()
        composeTestRule.onNodeWithContentDescription("Delete").assertExists()
    }

    @Test
    fun editMode_showsEditableFields() {
        val todo = TodoItem("Test task", "2 hours")
        
        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoItemCard(
                    todo = todo,
                    onUpdate = {},
                    onDelete = {}
                )
            }
        }

        // Enter edit mode
        composeTestRule
            .onNodeWithContentDescription("Edit")
            .performClick()

        // Verify edit mode UI
        composeTestRule.onNodeWithText("Todo Item").assertExists()
        composeTestRule.onNodeWithText("Time Estimate").assertExists()
        composeTestRule.onNodeWithText("Test task").assertExists()
        composeTestRule.onNodeWithText("2 hours").assertExists()
        composeTestRule.onNodeWithText("Save").assertExists()
        composeTestRule.onNodeWithText("Cancel").assertExists()
    }

    @Test
    fun editMode_canUpdateValues() {
        var updatedTodo: TodoItem? = null
        val todo = TodoItem("Test task", "2 hours")
        
        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoItemCard(
                    todo = todo,
                    onUpdate = { updatedTodo = it },
                    onDelete = {}
                )
            }
        }

        // Enter edit mode
        composeTestRule
            .onNodeWithContentDescription("Edit")
            .performClick()

        // Update values
        composeTestRule
            .onNodeWithText("Test task")
            .performTextReplacement("Updated task")

        composeTestRule
            .onNodeWithText("2 hours")
            .performTextReplacement("3 hours")

        // Save changes
        composeTestRule
            .onNodeWithText("Save")
            .performClick()

        // Verify update callback
        assert(updatedTodo?.item == "Updated task")
        assert(updatedTodo?.timeEstimate == "3 hours")
    }

    @Test
    fun editMode_canCancel() {
        var updateCalled = false
        val todo = TodoItem("Test task", "2 hours")
        
        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoItemCard(
                    todo = todo,
                    onUpdate = { updateCalled = true },
                    onDelete = {}
                )
            }
        }

        // Enter edit mode
        composeTestRule
            .onNodeWithContentDescription("Edit")
            .performClick()

        // Update values
        composeTestRule
            .onNodeWithText("Test task")
            .performTextReplacement("Updated task")

        // Cancel edit
        composeTestRule
            .onNodeWithText("Cancel")
            .performClick()

        // Verify original values are shown
        composeTestRule.onNodeWithText("Test task").assertExists()
        composeTestRule.onNodeWithText("Estimated time: 2 hours").assertExists()
        assert(!updateCalled)
    }

    @Test
    fun delete_triggersCallback() {
        var deleteTriggered = false
        val todo = TodoItem("Test task", "2 hours")
        
        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoItemCard(
                    todo = todo,
                    onUpdate = {},
                    onDelete = { deleteTriggered = true }
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Delete")
            .performClick()

        assert(deleteTriggered)
    }

    @Test
    fun longText_isHandledGracefully() {
        val longText = "This is a very long task description that should be handled gracefully by the UI without breaking the layout or becoming unreadable"
        val todo = TodoItem(longText, "2 hours")
        
        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoItemCard(
                    todo = todo,
                    onUpdate = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithText(longText).assertExists()
    }

    @Test
    fun specialCharacters_areDisplayedCorrectly() {
        val textWithSpecialChars = "Task with special chars: !@#$%^&*()"
        val todo = TodoItem(textWithSpecialChars, "2 hours")
        
        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoItemCard(
                    todo = todo,
                    onUpdate = {},
                    onDelete = {}
                )
            }
        }

        composeTestRule.onNodeWithText(textWithSpecialChars).assertExists()
    }

    @Test
    fun multipleEdits_maintainState() {
        val todo = TodoItem("Test task", "2 hours")
        var updateCount = 0
        
        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoItemCard(
                    todo = todo,
                    onUpdate = { updateCount++ },
                    onDelete = {}
                )
            }
        }

        repeat(3) {
            // Enter edit mode
            composeTestRule
                .onNodeWithContentDescription("Edit")
                .performClick()

            // Update text
            composeTestRule
                .onNodeWithText(if (it == 0) "Test task" else "Updated task $it")
                .performTextReplacement("Updated task ${it + 1}")

            // Save changes
            composeTestRule
                .onNodeWithText("Save")
                .performClick()
        }

        assert(updateCount == 3)
    }
} 