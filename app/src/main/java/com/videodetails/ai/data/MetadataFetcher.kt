package com.videodetails.ai.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Video metadata nikalne ka kaam:
 *  - YouTube / TikTok / X  -> public oEmbed APIs
 *  - Instagram / Facebook / baaki -> page HTML ke OpenGraph tags
 * Koi API key nahi chahiye. Fail hone par PARTIAL/ERROR status ke saath graceful fallback.
 */
object MetadataFetcher {

    private const val UA =
        "Mozilla/5.0 (Linux; Android 14; Pixel 7 Pro) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetch(url: String): VideoInfo = withContext(Dispatchers.IO) {
        val platform = detectPlatform(url)
        val id = extractVideoId(url, platform)
        try {
            val info = when (platform) {
                Platform.YOUTUBE -> youtube(url, id)
                Platform.TIKTOK -> tiktok(url, id)
                Platform.TWITTER -> twitter(url, id)
                else -> fromPage(url, platform, id)
            }
            val hasSomething = !info.title.isNullOrBlank() || !info.thumbnailUrl.isNullOrBlank()
            info.copy(status = if (hasSomething) FetchStatus.OK else FetchStatus.PARTIAL)
        } catch (e: Exception) {
            VideoInfo(
                url = url,
                platform = platform,
                videoId = id,
                status = FetchStatus.ERROR,
                error = e.message ?: "Network error"
            )
        }
    }

    // ---------- platform specific ----------

    private fun youtube(url: String, id: String?): VideoInfo {
        val watchUrl = if (id != null) "https://www.youtube.com/watch?v=$id" else url
        val json = getJson("https://www.youtube.com/oembed?url=${enc(watchUrl)}&format=json")
        if (json != null) {
            val thumb = json.optString("thumbnail_url").takeIf { it.isNotBlank() }
                ?: id?.let { "https://i.ytimg.com/vi/$it/hqdefault.jpg" }
            return VideoInfo(
                url = url,
                platform = Platform.YOUTUBE,
                videoId = id,
                title = json.optString("title").takeIf { it.isNotBlank() },
                author = json.optString("author_name").takeIf { it.isNotBlank() },
                thumbnailUrl = thumb
            )
        }
        return fromPage(url, Platform.YOUTUBE, id)
    }

    private fun tiktok(url: String, id: String?): VideoInfo {
        val json = getJson("https://www.tiktok.com/oembed?url=${enc(url)}")
        if (json != null) {
            return VideoInfo(
                url = url,
                platform = Platform.TIKTOK,
                videoId = id,
                title = json.optString("title").takeIf { it.isNotBlank() },
                author = json.optString("author_name").takeIf { it.isNotBlank() },
                thumbnailUrl = json.optString("thumbnail_url").takeIf { it.isNotBlank() }
            )
        }
        return fromPage(url, Platform.TIKTOK, id)
    }

    private fun twitter(url: String, id: String?): VideoInfo {
        val json = getJson("https://publish.twitter.com/oembed?url=${enc(url)}&omit_script=1")
        if (json != null) {
            val html = json.optString("html")
            val text = html.replace(Regex("<[^>]+>"), " ")
                .let(::unescape)
                .replace(Regex("""\s+"""), " ")
                .trim()
                .take(500)
            val image = Regex("""<img[^>]+src=["']([^"']+)["']""").find(html)?.groupValues?.get(1)
            return VideoInfo(
                url = url,
                platform = Platform.TWITTER,
                videoId = id,
                title = text.takeIf { it.isNotBlank() },
                author = json.optString("author_name").takeIf { it.isNotBlank() },
                thumbnailUrl = image
            )
        }
        return fromPage(url, Platform.TWITTER, id)
    }

    private fun fromPage(url: String, platform: Platform, id: String?): VideoInfo {
        val html = getHtml(url) ?: return VideoInfo(url, platform, id, status = FetchStatus.PARTIAL)
        val meta = parseMeta(html)
        return VideoInfo(
            url = url,
            platform = platform,
            videoId = id,
            title = meta.title,
            thumbnailUrl = meta.image,
            description = meta.description
        )
    }

    // ---------- HTTP ----------

    private fun getHtml(url: String): String? = try {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", UA)
            .header("Accept", "text/html,application/xhtml+xml")
            .header("Accept-Language", "en-US,en;q=0.9,hi;q=0.8")
            .get()
            .build()
        client.newCall(request).execute().use { response ->
            if (response.isSuccessful) response.body?.string() else null
        }
    } catch (e: Exception) {
        null
    }

    private fun getJson(url: String): JSONObject? {
        val body = getHtml(url) ?: return null
        return runCatching { JSONObject(body) }.getOrNull()
    }

    // ---------- HTML parsing ----------

    private data class PageMeta(val title: String?, val image: String?, val description: String?)

    private val metaTagRegex = Regex("""<meta\s+([^>]+?)/?>""", RegexOption.IGNORE_CASE)
    private val attrRegex = Regex("""([\w:-]+)\s*=\s*["']([^"']*)["']""")

    private fun parseMeta(html: String): PageMeta {
        val tags = HashMap<String, String>()
        metaTagRegex.findAll(html).forEach { match ->
            val attrs = attrRegex.findAll(match.groupValues[1])
                .associate { it.groupValues[1].lowercase() to it.groupValues[2] }
            val key = attrs["property"] ?: attrs["name"] ?: return@forEach
            val content = attrs["content"] ?: return@forEach
            if (!tags.containsKey(key)) tags[key] = content
        }

        val pageTitle = Regex(
            """<title[^>]*>(.*?)</title>""",
            setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        ).find(html)?.groupValues?.get(1)

        return PageMeta(
            title = (tags["og:title"] ?: tags["twitter:title"] ?: pageTitle)
                ?.let(::unescape)?.trim()?.takeIf { it.isNotEmpty() },
            image = (tags["og:image"] ?: tags["og:image:secure_url"] ?: tags["twitter:image"])
                ?.let(::unescape)?.trim()?.takeIf { it.isNotEmpty() },
            description = (tags["og:description"] ?: tags["description"] ?: tags["twitter:description"])
                ?.let(::unescape)?.trim()?.takeIf { it.isNotEmpty() }
        )
    }

    private fun unescape(input: String): String {
        var out = input
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
        out = Regex("""&#x([0-9a-fA-F]+);""").replace(out) { m ->
            runCatching { m.groupValues[1].toInt(16).toChar().toString() }.getOrDefault(m.value)
        }
        out = Regex("""&#(\d+);""").replace(out) { m ->
            runCatching { m.groupValues[1].toInt().toChar().toString() }.getOrDefault(m.value)
        }
        return out
    }

    private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")
}
