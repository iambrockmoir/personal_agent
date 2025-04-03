package com.personal.voicememo.service

import com.personal.voicememo.domain.TodoItem
import com.personal.voicememo.domain.TodoList
import com.personal.voicememo.network.GoogleSheetsServiceImpl
import com.personal.voicememo.network.OpenAiServiceImpl
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TodoServiceImplTest {
    private lateinit var openAiService: OpenAiServiceImpl
    private lateinit var googleSheetsService: GoogleSheetsServiceImpl
    private lateinit var todoService: TodoServiceImpl

    @Before
    fun setup() {
        openAiService = mockk()
        googleSheetsService = mockk()
        todoService = TodoServiceImpl(openAiService, googleSheetsService)
    }

    @Test
    fun `extractTodos returns TodoList from transcript`() = runBlocking {
        // Given
        val transcript = "I need to buy groceries and call mom tomorrow"
        val todoList = TodoList(
            todos = listOf(
                TodoItem("Buy groceries", "1 hour"),
                TodoItem("Call mom", "30 minutes")
            )
        )

        coEvery { openAiService.extractTodos(transcript) } returns todoList

        // When
        val result = todoService.extractTodos(transcript)

        // Then
        assertEquals(2, result.todos.size)
        
        val firstTodo = result.todos[0]
        assertEquals("Buy groceries", firstTodo.item)
        assertEquals("1 hour", firstTodo.timeEstimate)
        
        val secondTodo = result.todos[1]
        assertEquals("Call mom", secondTodo.item)
        assertEquals("30 minutes", secondTodo.timeEstimate)
    }

    @Test
    fun `saveTodoList successfully saves to Google Sheets`() = runBlocking {
        // Given
        val todoList = TodoList(
            todos = listOf(
                TodoItem("Buy groceries", "1 hour"),
                TodoItem("Call mom", "30 minutes")
            )
        )

        coEvery { googleSheetsService.saveTodoList(todoList) } returns true

        // When
        val result = todoService.saveTodoList(todoList)

        // Then
        assertTrue(result)
    }
} 