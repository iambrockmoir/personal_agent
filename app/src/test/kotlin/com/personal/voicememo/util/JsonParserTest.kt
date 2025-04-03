package com.personal.voicememo.util

import android.util.Log
import com.google.gson.JsonSyntaxException
import com.personal.voicememo.domain.TodoItem
import com.personal.voicememo.domain.TodoList
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class JsonParserTest {

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `parseTodoList should parse valid JSON with todos array`() {
        val json = """
            {
                "todos": [
                    {
                        "item": "Buy groceries",
                        "timeEstimate": "30 minutes"
                    },
                    {
                        "item": "Call dentist",
                        "timeEstimate": "15 minutes"
                    }
                ]
            }
        """.trimIndent()

        val result = JsonParser.parseTodoList(json)
        assertEquals(2, result.todos.size)
        assertEquals("Buy groceries", result.todos[0].item)
        assertEquals("30 minutes", result.todos[0].timeEstimate)
        assertEquals("Call dentist", result.todos[1].item)
        assertEquals("15 minutes", result.todos[1].timeEstimate)
    }

    @Test
    fun `parseTodoList should parse direct array of todo items`() {
        val json = """
            [
                {
                    "item": "Buy groceries",
                    "timeEstimate": "30 minutes"
                },
                {
                    "item": "Call dentist",
                    "timeEstimate": "15 minutes"
                }
            ]
        """.trimIndent()

        val result = JsonParser.parseTodoList(json)
        assertEquals(2, result.todos.size)
        assertEquals("Buy groceries", result.todos[0].item)
        assertEquals("30 minutes", result.todos[0].timeEstimate)
    }

    @Test
    fun `parseTodoList should handle single todo item`() {
        val json = """
            {
                "todos": [
                    {
                        "item": "Buy groceries",
                        "timeEstimate": "30 minutes"
                    }
                ]
            }
        """.trimIndent()

        val result = JsonParser.parseTodoList(json)
        assertEquals(1, result.todos.size)
        assertEquals("Buy groceries", result.todos[0].item)
        assertEquals("30 minutes", result.todos[0].timeEstimate)
    }

    @Test
    fun `parseTodoList should handle empty todos array`() {
        val json = """
            {
                "todos": []
            }
        """.trimIndent()

        val result = JsonParser.parseTodoList(json)
        assertTrue(result.todos.isEmpty())
    }

    @Test
    fun `parseTodoList should throw exception for invalid JSON`() {
        val json = "{ invalid json }"
        
        assertFailsWith<IllegalArgumentException> {
            JsonParser.parseTodoList(json)
        }
    }

    @Test
    fun `parseTodoList should throw exception for missing required fields`() {
        val json = """
            {
                "todos": [
                    {
                        "item": "Buy groceries"
                    }
                ]
            }
        """.trimIndent()

        assertFailsWith<IllegalArgumentException> {
            JsonParser.parseTodoList(json)
        }
    }

    @Test
    fun `parseTodoList should throw exception for empty string`() {
        assertFailsWith<IllegalArgumentException> {
            JsonParser.parseTodoList("")
        }
    }

    @Test
    fun `parseTodoList should handle JSON with extra fields`() {
        val json = """
            {
                "todos": [
                    {
                        "item": "Buy groceries",
                        "timeEstimate": "30 minutes",
                        "priority": "high",
                        "notes": "Don't forget milk"
                    }
                ]
            }
        """.trimIndent()

        val result = JsonParser.parseTodoList(json)
        assertEquals(1, result.todos.size)
        assertEquals("Buy groceries", result.todos[0].item)
        assertEquals("30 minutes", result.todos[0].timeEstimate)
    }

    @Test
    fun `parseTodoList should handle JSON with escaped characters`() {
        val json = """
            {
                "todos": [
                    {
                        "item": "Buy \"organic\" groceries",
                        "timeEstimate": "30 minutes"
                    }
                ]
            }
        """.trimIndent()

        val result = JsonParser.parseTodoList(json)
        assertEquals(1, result.todos.size)
        assertEquals("Buy \"organic\" groceries", result.todos[0].item)
    }

    @Test
    fun `parseTodoList should handle JSON with special characters`() {
        val json = """
            {
                "todos": [
                    {
                        "item": "Call John's office",
                        "timeEstimate": "15 minutes"
                    },
                    {
                        "item": "Review project #123",
                        "timeEstimate": "2 hours"
                    }
                ]
            }
        """.trimIndent()

        val result = JsonParser.parseTodoList(json)
        assertEquals(2, result.todos.size)
        assertEquals("Call John's office", result.todos[0].item)
        assertEquals("Review project #123", result.todos[1].item)
    }
} 