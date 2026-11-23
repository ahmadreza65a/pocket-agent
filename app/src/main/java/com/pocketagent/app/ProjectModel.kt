package com.pocketagent.app

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class Project(
    val id: String,
    val name: String,
    val path: String,
    val description: String
)

object ProjectStore {
    fun newId(): String = UUID.randomUUID().toString()

    fun load(store: SecureStore): List<Project> {
        val raw = store.get("projects")
        if (raw.isEmpty()) {
            val old = store.get("project_path")
            return if (old.isEmpty()) emptyList()
            else listOf(Project("default", "Default", old, ""))
        }
        val arr = JSONArray(raw)
        val out = mutableListOf<Project>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            out.add(
                Project(
                    o.getString("id"),
                    o.optString("name"),
                    o.optString("path"),
                    o.optString("description")
                )
            )
        }
        return out
    }

    fun save(store: SecureStore, list: List<Project>) {
        val arr = JSONArray()
        for (p in list) {
            arr.put(
                JSONObject()
                    .put("id", p.id)
                    .put("name", p.name)
                    .put("path", p.path)
                    .put("description", p.description)
            )
        }
        store.put("projects", arr.toString())
    }

    fun setActive(store: SecureStore, id: String) {
        store.put("active_project", id)
    }

    fun active(store: SecureStore): Project? {
        val list = load(store)
        val id = store.get("active_project")
        return list.firstOrNull { it.id == id } ?: list.firstOrNull()
    }

    fun systemPrompt(store: SecureStore): String {
        val p = active(store)
        val global = store.get("global_desc")
        val desc = if (p != null && p.description.isNotEmpty()) p.description else global
        val sb = StringBuilder()
        sb.append("You are Pocket Agent, an AI coding assistant inside an Android app. ")
        sb.append("Reply in the language the user writes in. ")
        sb.append("Never ask the user to paste passwords into the chat.\n")
        if (p != null) {
            sb.append("Project name: ").append(p.name).append("\n")
            sb.append("Project folder on the phone: ").append(p.path).append("\n")
        }
        if (desc.isNotEmpty()) {
            sb.append("Project description:\n").append(desc).append("\n")
        }
        return sb.toString()
    }
}
