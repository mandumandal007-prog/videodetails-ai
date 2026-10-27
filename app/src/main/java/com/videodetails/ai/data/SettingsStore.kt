package com.videodetails.ai.data

import android.content.Context

class SettingsStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("video_details_settings", Context.MODE_PRIVATE)

    var geminiKey: String
        get() = prefs.getString(KEY_GEMINI, "").orEmpty()
        set(value) {
            prefs.edit().putString(KEY_GEMINI, value.trim()).apply()
        }

    /** AI summary Hindi me (true, default) ya English me (false). */
    var aiHindi: Boolean
        get() = prefs.getBoolean(KEY_AI_HINDI, true)
        set(value) {
            prefs.edit().putBoolean(KEY_AI_HINDI, value).apply()
        }

    private companion object {
        const val KEY_GEMINI = "gemini_api_key"
        const val KEY_AI_HINDI = "ai_hindi"
    }
}
