package com.pocketagent.app

import android.content.Context
import java.io.File

object ProjectMemory {
    private const val NAME = "PROJECT_MEMORY.md"

    private const val RULES =
        "You maintain PROJECT_MEMORY.md, the long-term memory of a software project. " +
        "You receive the current file and the latest chat exchange. " +
        "Return the FULL updated file in Markdown and nothing else, without code fences. " +
        "Keep these sections: # Project Memory, ## Done, ## Planned, ## Access, ## Agent capabilities and limits. " +
        "Add only facts that appear in the chat and never invent anything. " +
        "Never store passwords or API keys. " +
        "Merge duplicates and keep the file under 60 lines. " +
        "Write in the same language the user writes in."

    private fun main(p: Project): File? =
        if (p.path.isBlank()) null else File(p.path, NAME)

    private fun backup(ctx: Context, p: Project): File =
        File(ctx.filesDir, "memory_" + p.id + ".md")

    fun read(ctx: Context, p: Project): String {
        try {
            val m = main(p)
            if (m != null && m.exists()) return m.readText()
        } catch (e: Exception) {
        }
        val b = backup(ctx, p)
        return if (b.exists()) b.readText() else ""
    }

    fun write(ctx: Context, p: Project, text: String) {
        try {
            val m = main(p)
            if (m != null) {
                m.parentFile?.mkdirs()
                m.writeText(text)
                return
            }
        } catch (e: Exception) {
        }
        backup(ctx, p).writeText(text)
    }

    fun update(
        ctx: Context,
        p: Project,
        baseUrl: String,
        apiKey: String,
        model: String,
        userText: String,
        reply: String
    ) {
        try {
            val sb = StringBuilder()
            sb.append("Current memory file:\n")
            sb.append(read(ctx, p).take(8000)).append("\n\n")
            sb.append("Latest exchange:\n")
            sb.append("USER: ").append(userText.take(4000)).append("\n")
            sb.append("ASSISTANT: ").append(reply.take(4000)).append("\n")
            val history = listOf(
                Pair("system", RULES),
                Pair("user", sb.toString())
            )
            var out = ChatApi.send(baseUrl, apiKey, model, history).trim()
            if (out.startsWith("\u0060\u0060\u0060")) {
                out = out.substringAfter("\n").removeSuffix("\u0060\u0060\u0060").trim()
            }
            if (out.length > 40 && out.startsWith("#")) {
                write(ctx, p, out)
            }
        } catch (e: Exception) {
        }
    }
}
