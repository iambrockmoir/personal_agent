package com.personal.voicememo.data.service

import android.util.Log
import com.personal.voicememo.data.network.OpenAIService
import com.personal.voicememo.data.network.OpenAIService.Embedding
import com.personal.voicememo.data.network.OpenAIService.EmbeddingResponse
import com.personal.voicememo.data.network.OpenAIService.Usage
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
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

class OpenAIEmbeddingServiceTest {
    private lateinit var openAiService: OpenAIService
    private lateinit var openAiEmbeddingService: OpenAIEmbeddingService

    @Before
    fun setup() {
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        
        openAiService = mockk()
        openAiEmbeddingService = OpenAIEmbeddingService(openAiService)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `generateEmbedding returns valid embedding vector`() = runTest {
        // Given
        val text = "Test text for embedding"
        val embeddingVector = List(1536) { 0.1f } // OpenAI's ada-002 model uses 1536 dimensions
        
        val embeddingResponse = EmbeddingResponse(
            data = listOf(Embedding(embedding = embeddingVector)),
            model = "text-embedding-ada-002",
            usage = Usage(prompt_tokens = 10, total_tokens = 10)
        )

        coEvery { 
            openAiService.createEmbedding(match { 
                it.model == "text-embedding-ada-002" && 
                it.input == text 
            })
        } returns embeddingResponse

        // When
        val result = openAiEmbeddingService.generateEmbedding(text)

        // Then
        assertNotNull(result)
        assertEquals(1536, result.size)
        assertEquals(0.1f, result[0])
    }

    @Test
    fun `generateEmbedding handles empty text`() = runTest {
        // Given
        val text = ""
        val embeddingVector = List(1536) { 0.0f }
        
        val embeddingResponse = EmbeddingResponse(
            data = listOf(Embedding(embedding = embeddingVector)),
            model = "text-embedding-ada-002",
            usage = Usage(prompt_tokens = 1, total_tokens = 1)
        )

        coEvery { 
            openAiService.createEmbedding(match { 
                it.model == "text-embedding-ada-002" && 
                it.input == text 
            })
        } returns embeddingResponse

        // When
        val result = openAiEmbeddingService.generateEmbedding(text)

        // Then
        assertNotNull(result)
        assertEquals(1536, result.size)
        assertEquals(0.0f, result[0])
    }

    @Test
    fun `extractTodos throws UnsupportedOperationException`() = runTest {
        assertFailsWith<UnsupportedOperationException> {
            openAiEmbeddingService.extractTodos("Test transcript")
        }
    }
} 