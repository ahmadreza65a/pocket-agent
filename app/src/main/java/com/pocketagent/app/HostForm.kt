package com.pocketagent.app

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun Field(label: String, value: String, onChange: (String) -> Unit, secret: Boolean = false) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        visualTransformation = if (secret) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun HostForm(initial: Host, onSave: (Host) -> Unit, onCancel: () -> Unit) {
    var name by remember { mutableStateOf(initial.name) }
    var type by remember { mutableStateOf(initial.type) }
    var host by remember { mutableStateOf(initial.host) }
    var port by remember { mutableStateOf(initial.port) }
    var user by remember { mutableStateOf(initial.user) }
    var password by remember { mutableStateOf(initial.password) }
    var path by remember { mutableStateOf(initial.path) }
    var siteUrl by remember { mutableStateOf(initial.siteUrl) }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            if (initial.name.isEmpty()) "New host" else "Edit host",
            style = MaterialTheme.typography.headlineSmall
        )
        Field("Name (e.g. My website)", name, { name = it })
        Text("Connection type")
        listOf("FTP", "SFTP", "DirectAdmin", "cPanel").forEach { t ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().clickable {
                    type = t
                    port = defaultPort(t)
                }
            ) {
                RadioButton(selected = type == t, onClick = {
                    type = t
                    port = defaultPort(t)
                })
                Text(t)
            }
        }
        Field("Host / server address", host, { host = it })
        Field("Port", port, { port = it })
        Field("Username", user, { user = it })
        Field("Password", password, { password = it }, secret = true)
        Field("Remote project folder (e.g. /public_html)", path, { path = it })
        Field("Website address (e.g. https://example.com)", siteUrl, { siteUrl = it })
        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                if (name.isBlank() || host.isBlank()) {
                    error = "Name and host are required."
                } else {
                    onSave(
                        Host(
                            name.trim(), type, host.trim(), port.trim(),
                            user.trim(), password, path.trim(), siteUrl.trim()
                        )
                    )
                }
            }) { Text("Save") }
            OutlinedButton(onClick = onCancel) { Text("Cancel") }
        }
    }
}
