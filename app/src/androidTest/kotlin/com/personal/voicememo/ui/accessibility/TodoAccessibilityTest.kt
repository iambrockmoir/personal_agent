package com.personal.voicememo.ui.accessibility

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.personal.voicememo.domain.TodoItem
import com.personal.voicememo.ui.screen.TodoScreen
import com.personal.voicememo.ui.theme.VoiceMemoTheme
import com.personal.voicememo.ui.viewmodel.TodoUiState
import com.personal.voicememo.ui.viewmodel.TodoViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class TodoAccessibilityTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): TodoViewModel {
        return mockk(relaxed = true) {
            every { uiState } returns MutableStateFlow(TodoUiState.Initial)
            every { todos } returns MutableStateFlow(emptyList())
        }
    }

    @Test
    fun emptyState_hasProperAccessibilityLabels() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        // Verify empty state message is accessible
        composeTestRule
            .onNodeWithText("No todos yet. Add one or extract from a transcript.")
            .assertExists()
            .assertIsDisplayed()

        // Verify FAB has proper accessibility label
        composeTestRule
            .onNodeWithContentDescription("Add Todo")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun todoList_hasProperAccessibilityLabels() {
        val viewModel = createMockViewModel()
        val todos = listOf(
            TodoItem("Task 1", "1 hour"),
            TodoItem("Task 2", "30 minutes")
        )
        every { viewModel.todos } returns MutableStateFlow(todos)
        every { viewModel.uiState } returns MutableStateFlow(TodoUiState.Success)

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        // Verify list has proper accessibility label
        composeTestRule
            .onNodeWithTag("todo_list")
            .assertExists()
            .assertIsDisplayed()

        // Verify each todo item has proper accessibility labels
        todos.forEachIndexed { index, todo ->
            composeTestRule
                .onNodeWithContentDescription("Todo item ${index + 1}: ${todo.item}")
                .assertExists()
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithContentDescription("Edit ${todo.item}")
                .assertExists()
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithContentDescription("Delete ${todo.item}")
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun loadingState_hasProperAccessibilityLabels() {
        val viewModel = createMockViewModel()
        every { viewModel.uiState } returns MutableStateFlow(TodoUiState.Loading)

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Loading todos")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun errorState_hasProperAccessibilityLabels() {
        val viewModel = createMockViewModel()
        every { viewModel.uiState } returns MutableStateFlow(TodoUiState.Error("Test error"))

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithText("Test error")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Retry")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun editMode_hasProperAccessibilityLabels() {
        val viewModel = createMockViewModel()
        val todo = TodoItem("Task 1", "1 hour")
        every { viewModel.todos } returns MutableStateFlow(listOf(todo))
        every { viewModel.uiState } returns MutableStateFlow(TodoUiState.Success)

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        // Enter edit mode
        composeTestRule
            .onNodeWithContentDescription("Edit Task 1")
            .performClick()

        // Verify edit mode accessibility labels
        composeTestRule
            .onNodeWithContentDescription("Todo item text input")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Time estimate input")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Save changes")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Cancel editing")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun saveButton_hasProperAccessibilityLabels() {
        val viewModel = createMockViewModel()
        val todo = TodoItem("Task 1", "1 hour")
        every { viewModel.todos } returns MutableStateFlow(listOf(todo))
        every { viewModel.uiState } returns MutableStateFlow(TodoUiState.Success)

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Save todos to Google Sheets")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun extractTodos_hasProperAccessibilityLabels() {
        val viewModel = createMockViewModel()
        val transcript = "Test transcript"

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(
                    transcript = transcript,
                    viewModel = viewModel
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Extract todos from transcript")
            .assertExists()
            .assertIsDisplayed()
    }
} 