package com.videodetails.ai.data

import java.net.URL

enum class Platform { YOUTUBE, INSTAGRAM, FACEBOOK, TIKTOK, TWITTER, OTHER }

enum class FetchStatus { OK, PARTIAL, ERROR }

data class VideoInfo(
    val url: String,
    val platform: Platform,
    val videoId: String? = null,
    val title: String? = null,
    val author: String? = null,
    val thumbnailUrl: String? = null,
    val description: String? = null,
    val status: FetchStatus = FetchStatus.OK,
    val error: String? = null
) {
    val domain: String
        get() = runCatching { URL(url).host.removePrefix("www.") }.getOrDefault("")
}

/** Shared text me se pehla http(s) link nikalta hai. */
fun extractUrl(text: String): String? {
    val match = Regex("""https?://[^\s<>"']+""").find(text) ?: return null
    return match.value.trimEnd('.', ',', ')', ']', '"', '\'', '!', '?')
}

fun detectPlatform(url: String): Platform {
    val host = runCatching { URL(url).host.lowercase() }.getOrDefault("")
    return when {
        host.contains("youtube") || host.contains("youtu.be") -> Platform.YOUTUBE
        host.contains("instagram") -> Platform.INSTAGRAM
        host.contains("facebook") || host.contains("fb.watch") -> Platform.FACEBOOK
        host.contains("tiktok") -> Platform.TIKTOK
        host.contains("twitter") || host == "x.com" || host.endsWith(".x.com") -> Platform.TWITTER
        else -> Platform.OTHER
    }
}

fun extractVideoId(url: String, platform: Platform): String? {
    val patterns = when (platform) {
        Platform.YOUTUBE -> listOf(
            """[?&]v=([A-Za-z0-9_-]{6,})""",
            """youtu\.be/([A-Za-z0-9_-]{6,})""",
            """/(?:shorts|embed|live|v)/([A-Za-z0-9_-]{6,})"""
        )
        Platform.INSTAGRAM -> listOf("""/(?:reel|reels|p|tv)/([A-Za-z0-9_-]+)""")
        Platform.TIKTOK -> listOf("""/video/(\d+)""")
        Platform.TWITTER -> listOf("""/status(?:es)?/(\d+)""")
        Platform.FACEBOOK -> listOf("""[?&]v=(\d+)""", """/videos/(\d+)""")
        Platform.OTHER -> emptyList()
    }
    for (pattern in patterns) {
        Regex(pattern).find(url)?.let { return it.groupValues[1] }
    }
    return null
}

/** Short / Reel / Video — UI chip ke liye. */
fun typeLabel(url: String, platform: Platform): String = when {
    platform == Platform.YOUTUBE && url.contains("/shorts/") -> "Short"
    platform == Platform.INSTAGRAM && url.contains("/reel") -> "Reel"
    platform == Platform.TIKTOK -> "Video"
    else -> "Video"
}

fun Platform.displayName(): String = when (this) {
    Platform.YOUTUBE -> "YouTube"
    Platform.INSTAGRAM -> "Instagram"
    Platform.FACEBOOK -> "Facebook"
    Platform.TIKTOK -> "TikTok"
    Platform.TWITTER -> "X (Twitter)"
    Platform.OTHER -> "Web"
}
