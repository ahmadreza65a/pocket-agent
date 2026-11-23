package com.pocketagent.app

import java.io.File

object FileTools {
    private const val MAX_READ = 20000
    private const val MAX_LIST = 200

    private fun resolve(root: String, rel: String): File? {
        if (root.isBlank()) return null
        val base = File(root).canonicalFile
        val clean = rel.trim().trimStart('/')
        val target = File(base, clean).canonicalFile
        val inside = target.path == base.path ||
            target.path.startsWith(base.path + File.separator)
        return if (inside) target else null
    }

    fun list(root: String, rel: String): String {
        try {
            val dir = resolve(root, rel)
                ?: return "ERROR: path is outside the project folder."
            if (!dir.exists()) return "ERROR: not found: $rel"
            if (!dir.isDirectory) return "ERROR: not a folder: $rel"
            val items = dir.listFiles()?.sortedBy { it.name }
                ?: return "ERROR: cannot read this folder."
            val sb = StringBuilder()
            for (f in items.take(MAX_LIST)) {
                if (f.isDirectory) sb.append("[dir]  ").append(f.name).append("/\n")
                else sb.append("[file] ").append(f.name).append(" (").append(f.length()).append(" bytes)\n")
            }
            if (items.size > MAX_LIST) sb.append("... ").append(items.size - MAX_LIST).append(" more items\n")
            return if (sb.isEmpty()) "(empty folder)" else sb.toString()
        } catch (e: Exception) {
            return "ERROR: " + e.message
        }
    }

    fun read(root: String, rel: String): String {
        try {
            val f = resolve(root, rel)
                ?: return "ERROR: path is outside the project folder."
            if (!f.exists()) return "ERROR: not found: $rel"
            if (!f.isFile) return "ERROR: not a file: $rel"
            val text = f.readText()
            if (text.length <= MAX_READ) return text
            return text.take(MAX_READ) + "\n...[truncated, file has " + text.length + " characters]"
        } catch (e: Exception) {
            return "ERROR: " + e.message
        }
    }
}
