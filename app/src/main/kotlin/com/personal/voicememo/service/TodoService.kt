package com.personal.voicememo.service

import com.personal.voicememo.domain.TodoList
import com.personal.voicememo.domain.TodoStorageService
import com.personal.voicememo.network.OpenAiServiceImpl
import com.personal.voicememo.network.GoogleSheetsServiceImpl
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service interface for managing todo operations.
 * 
 * This service handles:
 * - Extracting todos from transcripts using OpenAI
 * - Saving todos to Google Sheets
 */
interface TodoService {
    /**
     * Extracts todos from a transcript using OpenAI.
     * 
     * @param transcript The transcript text to extract todos from
     * @return A TodoList containing the extracted todos
     */
    suspend fun extractTodos(transcript: String): TodoList

    /**
     * Saves a todo list to Google Sheets.
     * 
     * @param todoList The todo list to save
     * @return true if the save was successful, false otherwise
     */
    suspend fun saveTodoList(todoList: TodoList): Boolean
}

/**
 * Implementation of the TodoService interface.
 * 
 * This implementation:
 * - Uses OpenAI service for todo extraction
 * - Uses Google Sheets service for todo storage
 * - Is scoped as a singleton for the application lifecycle
 */
@Singleton
class TodoServiceImpl @Inject constructor(
    private val openAiService: OpenAiServiceImpl,
    private val googleSheetsService: GoogleSheetsServiceImpl
) : TodoService, TodoStorageService {

    override suspend fun extractTodos(transcript: String): TodoList {
        return openAiService.extractTodos(transcript)
    }

    override suspend fun saveTodoList(todoList: TodoList): Boolean {
        return googleSheetsService.saveTodoList(todoList)
    }
} 