package com.pocketagent.app

import org.json.JSONObject

object AgentLoop {
    private const val MAX_STEPS = 6
    private val TAG = Regex("<tool>(.*?)</tool>", RegexOption.DOT_MATCHES_ALL)

    fun toolPrompt(): String =
        "\nIMPORTANT: you DO have tools to list and read files in the project folder. This overrides any older text saying you cannot access files. " +
        "To use one, reply with ONLY one line like " +
        "<tool>{\"name\":\"list\",\"path\":\".\"}</tool> or " +
        "<tool>{\"name\":\"read\",\"path\":\"app/index.php\"}</tool>. " +
        "Paths are relative to the project folder. " +
        "The app runs the tool and sends you the result, then you continue. " +
        "Use at most one tool per reply. " +
        "You cannot write or edit files yet and cannot connect to hosts yet. " +
        "When you have enough information, answer normally without a tool tag.\n"

    private fun runTool(json: String, root: String, used: MutableList<String>): String {
        return try {
            val o = JSONObject(json.trim())
            val name = o.optString("name")
            val path = o.optString("path", ".")
            used.add(name + " " + path)
            when (name) {
                "list" -> FileTools.list(root, path)
                "read" -> FileTools.read(root, path)
                else -> "ERROR: unknown tool: " + name
            }
        } catch (e: Exception) {
            "ERROR: bad tool call: " + e.message
        }
    }

    fun run(
        baseUrl: String,
        apiKey: String,
        model: String,
        history: List<Pair<String, String>>,
        root: String,
        onDelta: (String) -> Unit = {}
    ): String {
        val msgs = history.toMutableList()
        val used = mutableListOf<String>()
        var reply = ""
        for (step in 1..MAX_STEPS) {
            reply = ChatStream.send(baseUrl, apiKey, model, msgs) { t -> if (!t.trimStart().startsWith("<")) onDelta(t) }
            val m = TAG.find(reply) ?: break
            val result = runTool(m.groupValues[1], root, used)
            msgs.add(Pair("assistant", reply))
            msgs.add(Pair("user", "TOOL RESULT:\n" + result))
        }
        if (TAG.containsMatchIn(reply)) {
            reply = "Stopped: too many tool steps. Please ask more specifically."
        }
        return if (used.isEmpty()) reply
        else "[" + used.joinToString(", ") + "]\n" + reply
    }
}
