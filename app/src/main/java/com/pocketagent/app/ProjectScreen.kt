package com.pocketagent.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import java.io.File

fun hasAllFilesAccess(): Boolean {
    return if (Build.VERSION.SDK_INT >= 30) Environment.isExternalStorageManager() else true
}

@Composable
fun ProjectScreen(store: SecureStore) {
    val context = LocalContext.current
    var path by remember {
        mutableStateOf(store.get("project_path").ifEmpty { "/storage/emulated/0/PocketAgent" })
    }
    var status by remember { mutableStateOf("") }
    var access by remember { mutableStateOf(hasAllFilesAccess()) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Project folder", style = MaterialTheme.typography.headlineSmall)
        Text("All-files access: " + (if (access) "Granted" else "Not granted"))

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
        }) { Text("Grant all-files access") }

        OutlinedButton(onClick = { access = hasAllFilesAccess() }) {
            Text("Check access again")
        }

        Field("Project folder path", path, { path = it })

        Button(onClick = {
            store.put("project_path", path.trim())
            val dir = File(path.trim())
            status = if (dir.exists() || dir.mkdirs()) {
                "Folder ready: " + dir.absolutePath
            } else {
                "Could not create the folder. Grant all-files access first."
            }
        }) { Text("Save and create folder") }

        OutlinedButton(onClick = {
            val names = File(path.trim()).list()
            status = if (names == null) {
                "Cannot read this folder."
            } else if (names.isEmpty()) {
                "The folder is empty."
            } else {
                names.take(30).joinToString("\n")
            }
        }) { Text("List files") }

        if (status.isNotEmpty()) {
            Text(status)
        }
    }
}
