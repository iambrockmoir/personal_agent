package com.personal.voicememo.network

import android.util.Log
import com.personal.voicememo.data.network.OpenAIService
import com.personal.voicememo.data.network.OpenAIService.ChatResponse
import com.personal.voicememo.data.network.OpenAIService.ChatRequest
import com.personal.voicememo.data.network.OpenAIService.Message
import com.personal.voicememo.domain.TodoItem
import com.personal.voicememo.domain.TodoList
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OpenAiServiceImplTest {
    private lateinit var openAiService: OpenAIService
    private lateinit var todoService: OpenAiServiceImpl

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        
        openAiService = mockk()
        todoService = OpenAiServiceImpl(openAiService)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `extractTodos - successful extraction`() = runTest {
        // Setup mock response
        coEvery { 
            openAiService.createChatCompletion(match { 
                it.model == "gpt-4-turbo-preview" && 
                it.messages.size == 2 && 
                it.messages[0].role == "system" && 
                it.messages[1].role == "user"
            })
        } returns ChatResponse(
            id = "test-id",
            choices = listOf(
                ChatResponse.Choice(
                    index = 0,
                    message = Message(
                        role = "assistant",
                        content = """
                            {
                                "todos": [
                                    {
                                        "item": "Test task 1",
                                        "timeEstimate": "1 hour"
                                    },
                                    {
                                        "item": "Test task 2",
                                        "timeEstimate": "30 minutes"
                                    }
                                ]
                            }
                        """.trimIndent()
                    ),
                    finish_reason = "stop"
                )
            ),
            created = 1234567890,
            model = "gpt-4-turbo-preview",
            usage = OpenAIService.Usage(prompt_tokens = 10, total_tokens = 20)
        )

        // Test extraction
        val transcript = "I need to do task 1 which will take an hour, and task 2 which will take 30 minutes"
        val result = todoService.extractTodos(transcript)

        // Verify results
        assertEquals(2, result.todos.size)
        assertEquals(TodoItem("Test task 1", "1 hour"), result.todos[0])
        assertEquals(TodoItem("Test task 2", "30 minutes"), result.todos[1])
    }

    @Test
    fun `extractTodos - invalid JSON response`() = runTest {
        coEvery { 
            openAiService.createChatCompletion(any())
        } returns ChatResponse(
            id = "test-id",
            choices = listOf(
                ChatResponse.Choice(
                    index = 0,
                    message = Message(
                        role = "assistant",
                        content = "Invalid JSON"
                    ),
                    finish_reason = "stop"
                )
            ),
            created = 1234567890,
            model = "gpt-4-turbo-preview",
            usage = OpenAIService.Usage(prompt_tokens = 10, total_tokens = 20)
        )

        assertFailsWith<IllegalArgumentException> {
            todoService.extractTodos("Test transcript")
        }
    }

    @Test
    fun `extractTodos - API error`() = runTest {
        coEvery { 
            openAiService.createChatCompletion(any())
        } throws Exception("API Error")

        assertFailsWith<Exception> {
            todoService.extractTodos("Test transcript")
        }
    }

    @Test
    fun `extractTodos - empty response`() = runTest {
        coEvery { 
            openAiService.createChatCompletion(any())
        } returns ChatResponse(
            id = "test-id",
            choices = listOf(
                ChatResponse.Choice(
                    index = 0,
                    message = Message(
                        role = "assistant",
                        content = """
                            {
                                "todos": []
                            }
                        """.trimIndent()
                    ),
                    finish_reason = "stop"
                )
            ),
            created = 1234567890,
            model = "gpt-4-turbo-preview",
            usage = OpenAIService.Usage(prompt_tokens = 10, total_tokens = 20)
        )

        val result = todoService.extractTodos("Empty transcript")
        assertEquals(0, result.todos.size)
    }

    @Test
    fun `saveTodoList - throws UnsupportedOperationException`() = runTest {
        val todoList = TodoList(listOf(
            TodoItem("Test task", "1 hour")
        ))

        assertFailsWith<UnsupportedOperationException> {
            todoService.saveTodoList(todoList)
        }
    }
} 