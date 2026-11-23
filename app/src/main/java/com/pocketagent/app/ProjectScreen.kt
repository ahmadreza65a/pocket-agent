package com.pocketagent.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

fun hasAllFilesAccess(): Boolean {
    return if (Build.VERSION.SDK_INT >= 30) Environment.isExternalStorageManager() else true
}

@Composable
fun ProjectScreen(store: SecureStore) {
    val context = LocalContext.current
    var projects by remember { mutableStateOf(ProjectStore.load(store)) }
    var activeId by remember { mutableStateOf(ProjectStore.active(store)?.id ?: "") }
    var editing by remember { mutableStateOf<Project?>(null) }
    var access by remember { mutableStateOf(hasAllFilesAccess()) }

    fun commit(list: List<Project>) {
        projects = list
        ProjectStore.save(store, list)
    }

    val current = editing
    val shown = projects.firstOrNull { it.id == activeId }?.id ?: projects.firstOrNull()?.id

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (current != null) {
            ProjectForm(
                project = current,
                onSave = { p ->
                    val exists = projects.any { it.id == p.id }
                    commit(if (exists) projects.map { if (it.id == p.id) p else it } else projects + p)
                    editing = null
                },
                onDelete = {
                    commit(projects.filter { it.id != current.id })
                    editing = null
                },
                onCancel = { editing = null }
            )
        } else {
            Text("Projects", style = MaterialTheme.typography.headlineSmall)
            Text("All-files access: " + (if (access) "Granted" else "Not granted"))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    try {
                        context.startActivity(
                            Intent(
                                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                                Uri.parse("package:" + context.packageName)
                            )
                        )
                    } catch (e: Exception) {
                        context.startActivity(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
                    }
                }) { Text("Grant access") }
                OutlinedButton(onClick = { access = hasAllFilesAccess() }) { Text("Check again") }
            }
            projects.forEach { p ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            p.name + (if (p.id == shown) "  (active)" else ""),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(p.path)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (p.id != shown) {
                                OutlinedButton(onClick = {
                                    activeId = p.id
                                    ProjectStore.setActive(store, p.id)
                                }) { Text("Use") }
                            }
                            OutlinedButton(onClick = { editing = p }) { Text("Edit") }
                        }
                    }
                }
            }
            Button(onClick = {
                editing = Project(ProjectStore.newId(), "", "/storage/emulated/0/PocketAgent/", "")
            }) { Text("New project") }
        }
    }
}
