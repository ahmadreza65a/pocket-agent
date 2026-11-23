package com.pocketagent.app

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun memoryPrompt(ctx: Context, p: Project?): String {
    if (p == null) return ""
    val mem = ProjectMemory.read(ctx, p)
    if (mem.isBlank()) return ""
    return "\nLong-term project memory (PROJECT_MEMORY.md). Keep it in mind:\n" + mem + "\n"
}

suspend fun updateMemoryAfterReply(
    ctx: Context,
    p: Project?,
    baseUrl: String,
    apiKey: String,
    model: String,
    userText: String,
    reply: Pair<String, String>
) {
    if (p == null || reply.first != "assistant") return
    withContext(Dispatchers.IO) {
        ProjectMemory.update(ctx, p, baseUrl, apiKey, model, userText, reply.second)
    }
}
