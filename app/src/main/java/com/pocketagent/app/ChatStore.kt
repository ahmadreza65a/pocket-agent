package com.pocketagent.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class ChatInfo(val id: String, val title: String, val updated: Long)

object ChatStore {
    private fun dir(context: Context, projectId: String): File {
        val d = File(context.filesDir, "chats/" + projectId)
        d.mkdirs()
        return d
    }

    fun newId(): String = UUID.randomUUID().toString()

    fun list(context: Context, projectId: String): List<ChatInfo> {
        val files = dir(context, projectId).listFiles() ?: return emptyList()
        val out = mutableListOf<ChatInfo>()
        for (f in files) {
            if (!f.name.endsWith(".json")) continue
            try {
                val o = JSONObject(f.readText())
                out.add(ChatInfo(f.name.removeSuffix(".json"), o.optString("title", "Chat"), f.lastModified()))
            } catch (e: Exception) {
            }
        }
        return out.sortedByDescending { it.updated }
    }

    fun load(context: Context, projectId: String, chatId: String): List<Pair<String, String>> {
        val f = File(dir(context, projectId), chatId + ".json")
        if (!f.exists()) return emptyList()
        return try {
            val arr = JSONObject(f.readText()).getJSONArray("messages")
            val out = mutableListOf<Pair<String, String>>()
            for (i in 0 until arr.length()) {
                val m = arr.getJSONObject(i)
                out.add(m.getString("role") to m.getString("text"))
            }
            out
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun save(context: Context, projectId: String, chatId: String, messages: List<Pair<String, String>>) {
        val arr = JSONArray()
        for ((role, text) in messages) {
            arr.put(JSONObject().put("role", role).put("text", text))
        }
        val first = messages.firstOrNull { it.first == "user" }?.second ?: "New chat"
        val title = first.replace("\n", " ").take(40)
        val o = JSONObject().put("title", title).put("messages", arr)
        File(dir(context, projectId), chatId + ".json").writeText(o.toString())
    }

    fun delete(context: Context, projectId: String, chatId: String) {
        File(dir(context, projectId), chatId + ".json").delete()
    }
}
