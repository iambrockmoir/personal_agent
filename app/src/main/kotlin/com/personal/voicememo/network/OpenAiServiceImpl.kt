package com.personal.voicememo.network

import android.util.Log
import com.personal.voicememo.data.network.OpenAIService
import com.personal.voicememo.domain.TodoList
import com.personal.voicememo.service.TodoService
import com.personal.voicememo.util.JsonParser
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of OpenAI service for todo extraction.
 * 
 * This service:
 * - Uses GPT-4 to extract structured todo items from transcripts
 * - Formats the response as a TodoList
 * - Handles error cases and logging
 */
@Singleton
class OpenAiServiceImpl @Inject constructor(
    private val api: OpenAIService
) : TodoService {
    companion object {
        private const val TAG = "OpenAiServiceImpl"
        private const val MODEL = "gpt-4-turbo-preview"
    }

    /**
     * Extracts todos from a transcript using OpenAI's GPT-4.
     * 
     * @param transcript The transcript text to extract todos from
     * @return A TodoList containing the extracted todos
     * @throws IllegalStateException if OpenAI returns an empty response
     * @throws Exception for any other errors during extraction
     */
    override suspend fun extractTodos(transcript: String): TodoList {
        Log.d(TAG, "Extracting todos from transcript of length: ${transcript.length}")
        try {
            val prompt = buildExtractionPrompt(transcript)
            val request = buildChatRequest(prompt)
            val response = api.createChatCompletion(request)
            
            if (response.choices.isEmpty()) {
                throw IllegalStateException("No response from OpenAI")
            }

            val jsonResponse = response.choices[0].message.content
            Log.d(TAG, "Received JSON response: $jsonResponse")
            
            // Clean the JSON response before parsing
            val cleanJson = jsonResponse.replace("```json", "")
                .replace("```", "")
                .trim()
                .lines()
                .filter { it.isNotBlank() }
                .joinToString("\n")
            
            Log.d(TAG, "Cleaned JSON response: $cleanJson")
            
            return JsonParser.parseTodoList(cleanJson)
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting todos: ${e.message}", e)
            throw e
        }
    }

    /**
     * Builds the prompt for todo extraction.
     * 
     * @param transcript The transcript to extract todos from
     * @return A formatted prompt string
     */
    private fun buildExtractionPrompt(transcript: String): String {
        return """
            Extract a list of todo items with best time estimates and project/area context from the following transcript.
            Respond in JSON format as { "todos": [ { "item": "...", "timeEstimate": "...", "project": "..." } ] }.
            Only include clear, actionable items from the transcript.
            For each item:
            - Make it specific and actionable
            - Provide a realistic time estimate
            - Format time estimates consistently (e.g., "30 minutes", "2 hours", "1 day")
            - Include the project or area context if mentioned (e.g., "Work", "Personal", "Home")
            - If no project is mentioned, use "Personal" as default
            
            Transcript:
            $transcript
        """.trimIndent()
    }

    /**
     * Builds the chat request for OpenAI.
     * 
     * @param prompt The user prompt to send
     * @return A configured ChatRequest object
     */
    private fun buildChatRequest(prompt: String): OpenAIService.ChatRequest {
        return OpenAIService.ChatRequest(
            model = MODEL,
            messages = listOf(
                OpenAIService.Message(
                    role = "system",
                    content = """
                        You are a helpful assistant that extracts todo items from text.
                        You always respond with valid JSON in the format:
                        {
                            "todos": [
                                {
                                    "item": "Specific, actionable task",
                                    "timeEstimate": "Realistic time estimate",
                                    "project": "Project or area context"
                                }
                            ]
                        }
                    """.trimIndent()
                ),
                OpenAIService.Message(role = "user", content = prompt)
            )
        )
    }

    /**
     * Not supported by this implementation.
     * 
     * @throws UnsupportedOperationException always
     */
    override suspend fun saveTodoList(todoList: TodoList): Boolean {
        throw UnsupportedOperationException("Todo storage is not supported by OpenAiServiceImpl")
    }
} 