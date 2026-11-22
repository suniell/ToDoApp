package com.example.todoapp.data

/** A single to-do item. */
data class Task(
    val id: Long,
    val title: String,
    val isDone: Boolean = false,
)
