package com.pocketagent.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MessageBubble(role: String, text: String) {
    val isUser = role == "user"
    val color = when (role) {
        "user" -> MaterialTheme.colorScheme.primaryContainer
        "error" -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.secondaryContainer
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = color),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            SelectionContainer {
                Text(text, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
fun ChatScreen(store: SecureStore) {
    val messages = remember { mutableStateListOf<Pair<String, String>>() }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, loading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun send() {
        val text = input.trim()
        if (text.isEmpty() || loading) return
        val baseUrl = store.get("base_url")
        val apiKey = store.get("api_key")
        val model = store.get("model")
        if (baseUrl.isEmpty() || apiKey.isEmpty() || model.isEmpty()) {
            messages.add("error" to "Please fill Base URL, API Key and Model in Settings first.")
            return
        }
        messages.add("user" to text)
        input = ""
        loading = true
        val history = messages
            .filter { it.first == "user" || it.first == "assistant" }
            .toList()
        scope.launch {
            val reply = withContext(Dispatchers.IO) {
                try {
                    Pair("assistant", ChatApi.send(baseUrl, apiKey, model, history))
                } catch (e: Exception) {
                    Pair("error", "Error: ${e.message}")
                }
            }
            messages.add(reply)
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (messages.isEmpty()) {
                Text(
                    "Send a message to start chatting.",
                    modifier = Modifier.padding(16.dp)
                )
            }
            androidx.compose.foundation.lazy.LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages.size) { i ->
                    val (role, text) = messages[i]
                    MessageBubble(role, text)
                }
                if (loading) {
                    item { Text("...") }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") },
                maxLines = 4
            )
            Button(onClick = { send() }, enabled = !loading) {
                Text("Send")
            }
        }
    }
}
