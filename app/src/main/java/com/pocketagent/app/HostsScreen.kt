package com.pocketagent.app

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

fun testHost(h: Host): String {
    return try {
        Socket().use { s ->
            s.connect(InetSocketAddress(h.host, h.port.toIntOrNull() ?: 21), 8000)
        }
        "Reachable: " + h.host + ":" + h.port
    } catch (e: Exception) {
        "Failed: " + e.message
    }
}

@Composable
fun HostsScreen(store: SecureStore) {
    val hosts = remember { mutableStateListOf<Host>().apply { addAll(loadHosts(store)) } }
    var editing by remember { mutableStateOf<Host?>(null) }
    var editIndex by remember { mutableStateOf(-1) }
    var testResult by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val current = editing

    if (current != null) {
        HostForm(
            initial = current,
            onSave = { h ->
                if (editIndex >= 0) {
                    hosts[editIndex] = h
                } else {
                    hosts.add(h)
                }
                saveHosts(store, hosts)
                editing = null
            },
            onCancel = { editing = null }
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hosts", style = MaterialTheme.typography.headlineSmall)
        Button(onClick = {
            editIndex = -1
            editing = Host("", "FTP", "", "21", "", "", "/public_html", "")
        }) { Text("Add host") }
        if (hosts.isEmpty()) {
            Text("No hosts saved yet.")
        }
        hosts.forEachIndexed { index, h ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(h.name, style = MaterialTheme.typography.titleMedium)
                    Text(h.type + "  -  " + h.host + ":" + h.port)
                    if (h.siteUrl.isNotEmpty()) {
                        Text(h.siteUrl)
                    }
                    Row {
                        TextButton(onClick = {
                            editIndex = index
                            editing = h
                        }) { Text("Edit") }
                        TextButton(onClick = {
                            scope.launch {
                                testResult = "Testing " + h.name + " ..."
                                testResult = withContext(Dispatchers.IO) { testHost(h) }
                            }
                        }) { Text("Test") }
                        TextButton(onClick = {
                            hosts.removeAt(index)
                            saveHosts(store, hosts)
                        }) { Text("Delete") }
                    }
                }
            }
        }
        if (testResult.isNotEmpty()) {
            Text(testResult)
        }
    }
}
