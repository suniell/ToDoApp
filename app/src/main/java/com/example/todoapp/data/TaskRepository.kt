package com.example.todoapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.taskDataStore: DataStore<Preferences> by preferencesDataStore(name = "tasks")

/**
 * Stores the task list on disk with DataStore, as a single JSON string.
 * Every change rewrites the whole list, which is fine for a personal to-do list.
 */
class TaskRepository(context: Context) {

    private val dataStore = context.applicationContext.taskDataStore
    private val tasksKey = stringPreferencesKey("tasks_json")

    /** Emits the current task list, and again every time it changes. */
    val tasks: Flow<List<Task>> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs -> decode(prefs[tasksKey]) }

    suspend fun add(title: String) = update { tasks ->
        val nextId = (tasks.maxOfOrNull { it.id } ?: 0L) + 1
        tasks + Task(id = nextId, title = title)
    }

    suspend fun setDone(id: Long, isDone: Boolean) = update { tasks ->
        tasks.map { if (it.id == id) it.copy(isDone = isDone) else it }
    }

    suspend fun delete(id: Long) = update { tasks ->
        tasks.filterNot { it.id == id }
    }

    private suspend fun update(transform: (List<Task>) -> List<Task>) {
        dataStore.edit { prefs ->
            prefs[tasksKey] = encode(transform(decode(prefs[tasksKey])))
        }
    }

    private fun encode(tasks: List<Task>): String {
        val array = JSONArray()
        tasks.forEach { task ->
            array.put(
                JSONObject()
                    .put("id", task.id)
                    .put("title", task.title)
                    .put("isDone", task.isDone)
            )
        }
        return array.toString()
    }

    private fun decode(json: String?): List<Task> {
        if (json.isNullOrEmpty()) return emptyList()
        val array = JSONArray(json)
        return List(array.length()) { i ->
            val obj = array.getJSONObject(i)
            Task(
                id = obj.getLong("id"),
                title = obj.getString("title"),
                isDone = obj.optBoolean("isDone", false),
            )
        }
    }
}
