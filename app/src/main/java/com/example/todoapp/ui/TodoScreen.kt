package com.example.todoapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.todoapp.R
import com.example.todoapp.data.Task
import com.example.todoapp.ui.theme.ToDoAppTheme

/** Screen entry point: connects the ViewModel to the stateless [TodoContent]. */
@Composable
fun TodoScreen(viewModel: TaskViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    TodoContent(
        tasks = tasks,
        onAddTask = viewModel::addTask,
        onDoneChange = viewModel::setDone,
        onDelete = viewModel::deleteTask,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoContent(
    tasks: List<Task>,
    onAddTask: (String) -> Unit,
    onDoneChange: (Task, Boolean) -> Unit,
    onDelete: (Task) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("To-Do List") }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AddTaskRow(onAddTask = onAddTask)
            if (tasks.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No tasks yet. Add one above!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(tasks, key = { it.id }) { task ->
                        SwipeToDeleteTaskItem(
                            task = task,
                            onDoneChange = { onDoneChange(task, it) },
                            onDelete = { onDelete(task) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddTaskRow(onAddTask: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf("") }
    val submit = {
        if (text.isNotBlank()) {
            onAddTask(text)
            text = ""
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("New task") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        Button(onClick = submit, enabled = text.isNotBlank()) {
            Text("Add")
        }
    }
}

/** Wraps a task row so it can be swiped left or right to delete it. */
@Composable
private fun SwipeToDeleteTaskItem(
    task: Task,
    onDoneChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = dismissState,
        onDismiss = { value ->
            if (value != SwipeToDismissBoxValue.Settled) onDelete()
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.medium,
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                },
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        TaskItem(task = task, onDoneChange = onDoneChange, onDelete = onDelete)
    }
}

@Composable
private fun TaskItem(
    task: Task,
    onDoneChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = task.isDone, onCheckedChange = onDoneChange)
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None,
                color = if (task.isDone) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(R.drawable.ic_delete),
                    contentDescription = "Delete task",
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TodoContentPreview() {
    ToDoAppTheme {
        TodoContent(
            tasks = listOf(
                Task(id = 1, title = "Buy groceries"),
                Task(id = 2, title = "Finish homework", isDone = true),
            ),
            onAddTask = {},
            onDoneChange = { _, _ -> },
            onDelete = {},
        )
    }
}
