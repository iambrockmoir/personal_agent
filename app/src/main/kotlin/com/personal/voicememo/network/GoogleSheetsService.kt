package com.personal.voicememo.network

import android.util.Log
import com.personal.voicememo.BuildConfig
import com.personal.voicememo.domain.TodoList
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service interface for Google Sheets API interactions.
 * 
 * This service handles:
 * - Appending todo items to a Google Sheet
 * - Managing the sheet's data format and structure
 */
interface GoogleSheetsService {
    /**
     * Appends a todo list to the specified Google Sheet.
     * 
     * @param sheetId The ID of the target Google Sheet
     * @param valueInputOption The input option for values (default: "RAW")
     * @param request The todo list data to append
     * @return A response containing update details
     */
    @POST("v4/spreadsheets/{sheetId}/values/A1:append")
    suspend fun appendTodoList(
        @Path("sheetId") sheetId: String = BuildConfig.GOOGLE_SHEET_ID,
        @Query("valueInputOption") valueInputOption: String = "RAW",
        @Body request: GoogleSheetsRequest
    ): GoogleSheetsResponse
}

/**
 * Request body for Google Sheets API.
 * 
 * @property values A list of rows, where each row is a list of string values
 */
data class GoogleSheetsRequest(
    val values: List<List<String>>
)

/**
 * Response structure from Google Sheets API.
 * 
 * @property spreadsheetId The ID of the updated spreadsheet
 * @property tableRange The range of cells that were updated
 * @property updates Details about the updates performed
 */
data class GoogleSheetsResponse(
    val spreadsheetId: String,
    val tableRange: String,
    val updates: Updates
)

/**
 * Updates structure from Google Sheets API response.
 * 
 * @property spreadsheetId The ID of the updated spreadsheet
 * @property updatedRange The range of cells that were updated
 * @property updatedRows Number of rows updated
 * @property updatedColumns Number of columns updated
 * @property updatedCells Number of cells updated
 */
data class Updates(
    val spreadsheetId: String,
    val updatedRange: String,
    val updatedRows: Int,
    val updatedColumns: Int,
    val updatedCells: Int
)

/**
 * Implementation of Google Sheets service for todo storage.
 * 
 * This implementation:
 * - Converts todo items to a format suitable for Google Sheets
 * - Handles API communication and error cases
 * - Provides logging for debugging purposes
 */
@Singleton
class GoogleSheetsServiceImpl @Inject constructor(
    private val api: GoogleSheetsService
) {
    companion object {
        private const val TAG = "GoogleSheetsServiceImpl"
    }

    /**
     * Saves a todo list to Google Sheets.
     * 
     * @param todoList The todo list to save
     * @return true if the save was successful, false otherwise
     */
    suspend fun saveTodoList(todoList: TodoList): Boolean {
        val values = todoList.todos.map { todo ->
            listOf(
                todo.item,
                todo.timeEstimate,
                todo.project,
                System.currentTimeMillis().toString() // Timestamp
            )
        }

        val request = GoogleSheetsRequest(values = values)
        
        return try {
            api.appendTodoList(request = request)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving todo list", e)
            false
        }
    }
} 