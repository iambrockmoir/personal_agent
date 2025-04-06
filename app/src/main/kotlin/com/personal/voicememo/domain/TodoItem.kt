package com.personal.voicememo.domain

import com.google.gson.annotations.SerializedName

/**
 * Represents a single todo item with its task, time estimate, and project context.
 * 
 * @param item The specific task to be done
 * @param timeEstimate The estimated time to complete the task
 * @param project The project or area context for the task (defaults to "Personal")
 */
data class TodoItem(
    @SerializedName("item")
    val item: String,
    @SerializedName("timeEstimate")
    val timeEstimate: String,
    @SerializedName("project")
    val project: String = "Personal"
) 