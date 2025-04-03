package com.personal.voicememo.domain

interface TodoStorageService {
    suspend fun saveTodoList(todoList: TodoList): Boolean
} 