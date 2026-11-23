package com.pocketagent.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ChatScreen(store: SecureStore) {
    val context = LocalContext.current
    val project = ProjectStore.active(store)
    val projectId = project?.id ?: "none"
    val key = "active_chat_" + projectId
    var chatId by remember { mutableStateOf(store.get(key).ifEmpty { ChatStore.newId() }) }
    val messages = remember {
        mutableStateListOf<Pair<String, String>>().apply { addAll(ChatStore.load(context, projectId, chatId)) }
    }
    var input by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    var chats by remember { mutableStateOf(ChatStore.list(context, projectId)) }
    val scope = rememberCoroutineScope()

    fun persist() {
        ChatStore.save(context, projectId, chatId, messages.toList())
        store.put(key, chatId)
    }

    fun openChat(id: String) {
        chatId = id
        messages.clear()
        messages.addAll(ChatStore.load(context, projectId, id))
        store.put(key, id)
        showHistory = false
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
        persist()
        input = ""
        loading = true
        val system = ProjectStore.systemPrompt(store) + memoryPrompt(context, project) + AgentLoop.toolPrompt()
        val history = listOf("system" to system) +
            messages.filter { it.first == "user" || it.first == "assistant" }
        scope.launch {
            messages.add("assistant" to "")
            val idx = messages.size - 1
            val reply = withContext(Dispatchers.IO) {
                try {
                    Pair("assistant", AgentLoop.run(baseUrl, apiKey, model, history, project?.path ?: "") { t -> scope.launch { if (idx < messages.size) messages[idx] = "assistant" to t } })
                } catch (e: Exception) {
                    Pair("error", "Error: ${e.message}")
                }
            }
            messages[idx] = reply
            persist()
            loading = false
            updateMemoryAfterReply(context, project, baseUrl, apiKey, model, text, reply)
        }
    }

    if (showHistory) {
        HistoryList(
            chats = chats,
            onOpen = { openChat(it) },
            onDelete = { id ->
                ChatStore.delete(context, projectId, id)
                chats = ChatStore.list(context, projectId)
                if (id == chatId) {
                    openChat(ChatStore.newId())
                    showHistory = true
                }
            },
            onBack = { showHistory = false }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(project?.name ?: "No project", modifier = Modifier.weight(1f))
                OutlinedButton(enabled = !loading, onClick = { openChat(ChatStore.newId()) }) { Text("New") }
                OutlinedButton(enabled = !loading, onClick = {
                    chats = ChatStore.list(context, projectId)
                    showHistory = true
                }) { Text("History") }
            }
            ChatBody(messages, loading, input, { input = it }, { send() }, Modifier.weight(1f))
        }
    }
}
