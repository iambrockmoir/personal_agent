package com.personal.voicememo.feature

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.personal.voicememo.domain.TodoItem
import com.personal.voicememo.domain.TodoList
import com.personal.voicememo.service.TodoService
import com.personal.voicememo.ui.viewmodel.TodoUiState
import com.personal.voicememo.ui.viewmodel.TodoViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class TodoFeatureTest {
    private lateinit var todoService: TodoService
    private lateinit var viewModel: TodoViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        
        todoService = mockk()
        viewModel = TodoViewModel(todoService)
        
        mockkStatic("androidx.lifecycle.ViewModelKt")
        every { any<TodoViewModel>().viewModelScope } returns testScope
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `end-to-end flow - extract and save todos`() = runTest {
        val mockTodos = listOf(
            TodoItem("Test task 1", "1 hour"),
            TodoItem("Test task 2", "30 minutes")
        )
        
        coEvery { todoService.extractTodos(any()) } returns TodoList(mockTodos)
        coEvery { todoService.saveTodoList(any()) } returns true

        val transcript = "I need to do task 1 which will take an hour, and task 2 which will take 30 minutes"
        
        // Test extraction
        val states = mutableListOf<TodoUiState>()
        viewModel.extractTodos(transcript)
        testDispatcher.scheduler.advanceUntilIdle()
        
        val currentTodos = viewModel.todos.first()
        assertEquals(2, currentTodos.size)
        assertEquals(mockTodos[0].item, currentTodos[0].item)
        
        // Test todo manipulation
        val newTodo = TodoItem("Test task 3", "45 minutes")
        viewModel.addTodo(newTodo)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(3, viewModel.todos.first().size)

        viewModel.updateTodo(0, TodoItem("Updated task 1", "2 hours"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("Updated task 1", viewModel.todos.first()[0].item)

        viewModel.deleteTodo(1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.todos.first().size)

        // Test saving
        viewModel.saveTodos()
        testDispatcher.scheduler.advanceUntilIdle()
        
        coVerify {
            todoService.extractTodos(transcript)
            todoService.saveTodoList(any())
        }
    }

    @Test
    fun `error handling - OpenAI service failure`() = runTest {
        coEvery { todoService.extractTodos(any()) } throws Exception("API Error")

        viewModel.extractTodos("Test transcript")
        testDispatcher.scheduler.advanceUntilIdle()
        
        val currentState = viewModel.uiState.first()
        assertTrue(currentState is TodoUiState.Error)
        assertEquals("API Error", (currentState as TodoUiState.Error).message)
    }

    @Test
    fun `error handling - Google Sheets service failure`() = runTest {
        coEvery { todoService.extractTodos(any()) } returns TodoList(listOf(TodoItem("Test task", "1 hour")))
        coEvery { todoService.saveTodoList(any()) } throws Exception("Failed to save")

        // First extract todos successfully
        viewModel.extractTodos("Test transcript")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then test save failure
        viewModel.saveTodos()
        testDispatcher.scheduler.advanceUntilIdle()

        val currentState = viewModel.uiState.first()
        assertTrue(currentState is TodoUiState.Error)
        assertEquals("Failed to save", (currentState as TodoUiState.Error).message)
    }

    @Test
    fun `concurrent operations handling`() = runTest {
        var callCount = 0
        coEvery { todoService.extractTodos(any()) } coAnswers {
            callCount++
            kotlinx.coroutines.delay(100)
            TodoList(listOf(TodoItem("Test task ${callCount}", "1 hour")))
        }

        viewModel.extractTodos("Transcript 1")
        viewModel.extractTodos("Transcript 2")
        testDispatcher.scheduler.advanceTimeBy(200)
        testDispatcher.scheduler.runCurrent()
        
        val finalTodos = viewModel.todos.first()
        assertEquals(1, finalTodos.size)
        assertEquals("Test task 2", finalTodos[0].item)
    }
} 