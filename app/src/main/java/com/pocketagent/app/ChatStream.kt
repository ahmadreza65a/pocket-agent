package com.pocketagent.app

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ChatStream {
    fun send(
        baseUrl: String,
        apiKey: String,
        model: String,
        history: List<Pair<String, String>>,
        onDelta: (String) -> Unit
    ): String {
        val url = URL(baseUrl.trimEnd('/') + "/chat/completions")
        val messages = JSONArray()
        for ((role, text) in history) {
            messages.put(JSONObject().put("role", role).put("content", text))
        }
        val body = JSONObject()
            .put("model", model)
            .put("messages", messages)
            .put("stream", true)
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
            if (code !in 200..299) {
                val err = conn.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
                throw Exception("HTTP $code: $err")
            }
            val full = StringBuilder()
            val raw = StringBuilder()
            conn.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                while (true) {
                    val line = reader.readLine() ?: break
                    if (!line.startsWith("data:")) {
                        raw.append(line)
                        continue
                    }
                    val data = line.removePrefix("data:").trim()
                    if (data == "[DONE]") break
                    if (data.isEmpty()) continue
                    try {
                        val choices = JSONObject(data).optJSONArray("choices") ?: continue
                        if (choices.length() == 0) continue
                        val delta = choices.getJSONObject(0).optJSONObject("delta") ?: continue
                        val piece = delta.optString("content", "")
                        if (piece.isNotEmpty() && piece != "null") {
                            full.append(piece)
                            onDelta(full.toString())
                        }
                    } catch (e: Exception) {
                    }
                }
            }
            if (full.isEmpty() && raw.isNotEmpty()) {
                try {
                    val c = JSONObject(raw.toString()).getJSONArray("choices")
                        .getJSONObject(0).getJSONObject("message").optString("content", "")
                    if (c.isNotBlank()) {
                        onDelta(c)
                        return c
                    }
                } catch (e: Exception) {
                }
            }
            val out = full.toString()
            return if (out.isBlank()) "(empty reply)" else out
        } finally {
            conn.disconnect()
        }
    }
}
