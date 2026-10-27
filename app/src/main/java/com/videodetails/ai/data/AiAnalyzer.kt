package com.videodetails.ai.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Google Gemini se video ka chhota summary. Key user khud Settings me daalta hai.
 * Key na ho to ye class call hi nahi hoti — app bina AI ke bhi chalta hai.
 */
object AiAnalyzer {

    /** Model change karna ho to sirf ye line badlo. */
    private const val MODEL = "gemini-flash-latest"

    private val JSON = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .build()

    /** Last failure reason (HTTP code / Google ka message) — UI me dikhane ke liye. */
    @Volatile
    var lastError: String? = null
        private set

    suspend fun summarize(info: VideoInfo, apiKey: String, hindi: Boolean): String? =
        withContext(Dispatchers.IO) {
            try {
                val language = if (hindi) "Hindi (Devanagari script)" else "English"
                val prompt = buildString {
                    appendLine("You are a helpful video analyst. Analyse this video link and the metadata given below.")
                    appendLine("Reply in $language as 4 short plain-text lines (no markdown, no asterisks, no headings):")
                    appendLine("1) what this video is most likely about")
                    appendLine("2) 2 key things a viewer can expect")
                    appendLine("3) who will find it useful")
                    appendLine("4) if metadata is incomplete, say so briefly")
                    appendLine()
                    appendLine("Link: ${info.url}")
                    appendLine("Platform: ${info.platform}")
                    info.title?.let { appendLine("Title: $it") }
                    info.author?.let { appendLine("Author/Channel: $it") }
                    info.description?.let { appendLine("Description: ${it.take(500)}") }
                }

                val body = JSONObject().apply {
                    put(
                        "contents",
                        JSONArray().put(
                            JSONObject().put(
                                "parts",
                                JSONArray().put(JSONObject().put("text", prompt))
                            )
                        )
                    )
                    put(
                        "generationConfig",
                        JSONObject().put("temperature", 0.4).put("maxOutputTokens", 2048)
                    )
                }.toString()

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent")
                    .header("X-goog-api-key", apiKey.trim())
                    .header("Content-Type", "application/json")
                    .post(body.toRequestBody(JSON))
                    .build()

                client.newCall(request).execute().use { response ->
                    val text = response.body?.string()
                    if (!response.isSuccessful || text == null) {
                        val msg = text?.let {
                            runCatching { JSONObject(it).optJSONObject("error")?.optString("message") }.getOrNull()
                        }
                        lastError = "HTTP ${response.code}" + (msg?.takeIf { it.isNotBlank() }?.let { ": ${it.take(160)}" } ?: "")
                        null
                    } else {
                        lastError = null
                        val parts = JSONObject(text)
                            .optJSONArray("candidates")
                            ?.optJSONObject(0)
                            ?.optJSONObject("content")
                            ?.optJSONArray("parts")
                        val joined = buildString {
                            if (parts != null) {
                                for (i in 0 until parts.length()) {
                                    val part = parts.optJSONObject(i) ?: continue
                                    if (part.optBoolean("thought", false)) continue
                                    append(part.optString("text"))
                                }
                            }
                        }.replace("**", "").trim()
                        if (joined.isEmpty()) lastError = "Empty response"
                        joined.takeIf { it.isNotEmpty() }
                    }
                }
            } catch (e: Exception) {
                lastError = e.message ?: e.javaClass.simpleName
                null
            }
        }
}
