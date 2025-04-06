package com.personal.voicememo.util

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonParser as GsonJsonParser
import com.google.gson.JsonSyntaxException
import com.personal.voicememo.domain.TodoList
import com.personal.voicememo.domain.TodoItem

/**
 * Utility class for parsing JSON responses from OpenAI
 */
object JsonParser {
    private val gson = Gson().newBuilder()
        .setLenient()
        .create()

    /**
     * Parses a JSON string from OpenAI into a TodoList
     * @param json The JSON string from OpenAI
     * @return TodoList containing the extracted todos
     * @throws IllegalArgumentException if the JSON structure is unexpected
     */
    fun parseTodoList(json: String): TodoList {
        if (json.isBlank()) {
            throw IllegalArgumentException("JSON string cannot be empty")
        }

        // Remove markdown code block markers and any other non-JSON content
        val cleanJson = json.replace("```json", "")
            .replace("```", "")
            .trim()
            .lines()
            .filter { it.isNotBlank() }
            .joinToString("\n")

        Log.d("JsonParser", "Attempting to parse JSON: $cleanJson")
        
        return try {
            // Try parsing as object with todos field first
            val jsonElement = GsonJsonParser.parseString(cleanJson)
            
            when {
                jsonElement.isJsonObject -> {
                    val jsonObject = jsonElement.asJsonObject
                    if (jsonObject.has("todos")) {
                        val todosArray = jsonObject.getAsJsonArray("todos")
                        val todos = todosArray.map { element ->
                            parseTodoItem(element.asJsonObject)
                        }
                        TodoList(todos)
                    } else {
                        throw IllegalArgumentException("JSON object must have a 'todos' field")
                    }
                }
                jsonElement.isJsonArray -> {
                    val todos = jsonElement.asJsonArray.map { element ->
                        parseTodoItem(element.asJsonObject)
                    }
                    TodoList(todos)
                }
                else -> throw IllegalArgumentException("JSON must be either an object with 'todos' array or a direct array of todo items")
            }
        } catch (e: JsonSyntaxException) {
            Log.e("JsonParser", "Invalid JSON syntax: ${e.message}", e)
            throw IllegalArgumentException("Invalid JSON syntax: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("JsonParser", "Failed to parse JSON: ${e.message}", e)
            throw IllegalArgumentException("Failed to parse JSON: ${e.message}", e)
        }
    }

    private fun parseTodoItem(jsonObject: com.google.gson.JsonObject): TodoItem {
        try {
            if (!jsonObject.has("item") || !jsonObject.has("timeEstimate")) {
                throw IllegalArgumentException("Todo item must have both 'item' and 'timeEstimate' fields")
            }
            
            return TodoItem(
                item = jsonObject.get("item").asString,
                timeEstimate = jsonObject.get("timeEstimate").asString,
                project = if (jsonObject.has("project")) jsonObject.get("project").asString else "Personal"
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid todo item format", e)
        }
    }
} 