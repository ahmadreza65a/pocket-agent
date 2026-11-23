package com.pocketagent.app

import org.json.JSONArray
import org.json.JSONObject

data class Host(
    val name: String,
    val type: String,
    val host: String,
    val port: String,
    val user: String,
    val password: String,
    val path: String,
    val siteUrl: String
)

fun defaultPort(type: String): String = when (type) {
    "FTP" -> "21"
    "SFTP" -> "22"
    "DirectAdmin" -> "2222"
    else -> "2083"
}

fun loadHosts(store: SecureStore): MutableList<Host> {
    val list = mutableListOf<Host>()
    try {
        val arr = JSONArray(store.get("hosts").ifEmpty { "[]" })
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Host(
                    o.optString("name"), o.optString("type"),
                    o.optString("host"), o.optString("port"),
                    o.optString("user"), o.optString("password"),
                    o.optString("path"), o.optString("siteUrl")
                )
            )
        }
    } catch (e: Exception) {
    }
    return list
}

fun saveHosts(store: SecureStore, hosts: List<Host>) {
    val arr = JSONArray()
    for (h in hosts) {
        arr.put(
            JSONObject()
                .put("name", h.name).put("type", h.type)
                .put("host", h.host).put("port", h.port)
                .put("user", h.user).put("password", h.password)
                .put("path", h.path).put("siteUrl", h.siteUrl)
        )
    }
    store.put("hosts", arr.toString())
}
