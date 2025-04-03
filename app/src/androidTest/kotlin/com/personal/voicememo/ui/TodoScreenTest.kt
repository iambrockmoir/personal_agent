package com.personal.voicememo.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.personal.voicememo.domain.TodoItem
import com.personal.voicememo.ui.screen.TodoScreen
import com.personal.voicememo.ui.theme.VoiceMemoTheme
import com.personal.voicememo.ui.viewmodel.TodoUiState
import com.personal.voicememo.ui.viewmodel.TodoViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test

class TodoScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(): TodoViewModel {
        return mockk(relaxed = true) {
            every { uiState } returns MutableStateFlow(TodoUiState.Initial)
            every { todos } returns MutableStateFlow(emptyList())
        }
    }

    @Test
    fun initialEmptyState_showsEmptyMessage() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithText("No todos yet. Add one or extract from a transcript.")
            .assertExists()
    }

    @Test
    fun loadingState_showsLoadingIndicator() {
        val viewModel = createMockViewModel()
        every { viewModel.uiState } returns MutableStateFlow(TodoUiState.Loading)

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Loading")
            .assertExists()
    }

    @Test
    fun errorState_showsErrorMessage() {
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
    }

    @Test
    fun todoList_showsAllTodos() {
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

        composeTestRule.onNodeWithText("Task 1").assertExists()
        composeTestRule.onNodeWithText("1 hour").assertExists()
        composeTestRule.onNodeWithText("Task 2").assertExists()
        composeTestRule.onNodeWithText("30 minutes").assertExists()
    }

    @Test
    fun editTodo_updatesItem() {
        val viewModel = createMockViewModel()
        val todo = TodoItem("Task 1", "1 hour")
        every { viewModel.todos } returns MutableStateFlow(listOf(todo))
        every { viewModel.uiState } returns MutableStateFlow(TodoUiState.Success)

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        // Click edit button
        composeTestRule
            .onNodeWithContentDescription("Edit")
            .performClick()

        // Enter new values
        composeTestRule
            .onNodeWithText("Task 1")
            .performTextReplacement("Updated Task")

        composeTestRule
            .onNodeWithText("1 hour")
            .performTextReplacement("2 hours")

        // Save changes
        composeTestRule
            .onNodeWithText("Save")
            .performClick()

        // Verify update was called
        coVerify {
            viewModel.updateTodo(0, TodoItem("Updated Task", "2 hours"))
        }
    }

    @Test
    fun deleteTodo_removesItem() {
        val viewModel = createMockViewModel()
        val todo = TodoItem("Task to delete", "1 hour")
        every { viewModel.todos } returns MutableStateFlow(listOf(todo))
        every { viewModel.uiState } returns MutableStateFlow(TodoUiState.Success)

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Delete")
            .performClick()

        coVerify {
            viewModel.deleteTodo(0)
        }
    }

    @Test
    fun addTodo_createsNewItem() {
        val viewModel = createMockViewModel()
        every { viewModel.uiState } returns MutableStateFlow(TodoUiState.Success)

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        // Click FAB to add new todo
        composeTestRule
            .onNodeWithContentDescription("Add Todo")
            .performClick()

        coVerify {
            viewModel.addTodo(any())
        }
    }

    @Test
    fun saveTodos_triggersViewModelSave() {
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
            .onNodeWithText("Save to Google Sheets")
            .performClick()

        coVerify {
            viewModel.saveTodos()
        }
    }

    @Test
    fun longTodoList_isScrollable() {
        val viewModel = createMockViewModel()
        val todos = List(20) { index ->
            TodoItem("Task $index", "${index + 1} hour")
        }
        every { viewModel.todos } returns MutableStateFlow(todos)
        every { viewModel.uiState } returns MutableStateFlow(TodoUiState.Success)

        composeTestRule.setContent {
            VoiceMemoTheme {
                TodoScreen(viewModel = viewModel)
            }
        }

        // Verify first items are visible
        composeTestRule.onNodeWithText("Task 0").assertExists()
        composeTestRule.onNodeWithText("1 hour").assertExists()

        // Scroll to bottom
        composeTestRule.onNodeWithTag("todo_list").performScrollToIndex(19)

        // Verify last items are now visible
        composeTestRule.onNodeWithText("Task 19").assertExists()
        composeTestRule.onNodeWithText("20 hour").assertExists()
    }

    @Test
    fun extractTodos_fromTranscript() {
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

        // Verify extractTodos was called with the transcript
        coVerify {
            viewModel.extractTodos(transcript)
        }
    }
} 