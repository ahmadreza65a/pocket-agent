package com.pocketagent.app

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ChatApi {
    fun send(
        baseUrl: String,
        apiKey: String,
        model: String,
        history: List<Pair<String, String>>
    ): String {
        val url = URL(baseUrl.trimEnd('/') + "/chat/completions")
        val messages = JSONArray()
        for ((role, text) in history) {
            messages.put(JSONObject().put("role", role).put("content", text))
        }
        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .toString()

        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 30000
            conn.readTimeout = 180000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            if (code !in 200..299) {
                throw Exception("HTTP $code: $text")
            }
            val content = JSONObject(text)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .optString("content", "")
            return if (content.isBlank()) "(empty reply)" else content
        } finally {
            conn.disconnect()
        }
    }
}
