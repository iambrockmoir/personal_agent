package com.personal.voicememo.domain

import com.google.gson.annotations.SerializedName

data class TodoList(
    @SerializedName("todos")
    val todos: List<TodoItem>
) 