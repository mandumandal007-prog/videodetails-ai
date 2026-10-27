package com.videodetails.ai.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class HistoryItem(
    val url: String,
    val title: String,
    val platform: String,
    val thumbnail: String?,
    val time: Long
)

object HistoryStore {

    private const val PREFS = "video_details_history"
    private const val KEY = "items"
    private const val MAX_ITEMS = 20

    fun load(context: Context): List<HistoryItem> {
        val raw = prefs(context).getString(KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val obj = array.optJSONObject(index) ?: return@mapNotNull null
                HistoryItem(
                    url = obj.optString("url"),
                    title = obj.optString("title"),
                    platform = obj.optString("platform"),
                    thumbnail = obj.optString("thumbnail").takeIf { it.isNotBlank() },
                    time = obj.optLong("time")
                )
            }.filter { it.url.isNotBlank() }
        }.getOrDefault(emptyList())
    }

    fun add(context: Context, info: VideoInfo) {
        if (info.url.isBlank()) return
        val current = load(context).filterNot { it.url == info.url }
        val newItem = HistoryItem(
            url = info.url,
            title = info.title?.takeIf { it.isNotBlank() } ?: info.url,
            platform = info.platform.displayName(),
            thumbnail = info.thumbnailUrl,
            time = System.currentTimeMillis()
        )
        val updated = (listOf(newItem) + current).take(MAX_ITEMS)
        save(context, updated)
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY).apply()
    }

    private fun save(context: Context, items: List<HistoryItem>) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("url", item.url)
                    put("title", item.title)
                    put("platform", item.platform)
                    put("thumbnail", item.thumbnail ?: "")
                    put("time", item.time)
                }
            )
        }
        prefs(context).edit().putString(KEY, array.toString()).apply()
    }

    private fun prefs(context: Context) = context.applicationContext
        .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
