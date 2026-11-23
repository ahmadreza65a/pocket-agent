package com.pocketagent.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.io.File

@Composable
fun ProjectForm(
    project: Project,
    onSave: (Project) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(project.name) }
    var path by remember { mutableStateOf(project.path) }
    var desc by remember { mutableStateOf(project.description) }
    var status by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Project", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Project name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = path,
            onValueChange = { path = it },
            label = { Text("Project folder path") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Project description (overrides the global one)") },
            minLines = 6,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = {
                val dir = File(path.trim())
                if (dir.exists() || dir.mkdirs()) {
                    onSave(
                        project.copy(
                            name = name.trim().ifEmpty { "Untitled" },
                            path = path.trim(),
                            description = desc.trim()
                        )
                    )
                } else {
                    status = "Could not create the folder. Grant all-files access first."
                }
            }) { Text("Save") }

            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }

        OutlinedButton(onClick = onDelete) { Text("Delete project") }

        if (status.isNotEmpty()) {
            Text(status)
        }
    }
}
