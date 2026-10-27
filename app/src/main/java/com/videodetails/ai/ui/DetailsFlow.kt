package com.videodetails.ai.ui

import android.content.Context
import com.videodetails.ai.data.AiAnalyzer
import com.videodetails.ai.data.FetchStatus
import com.videodetails.ai.data.HistoryStore
import com.videodetails.ai.data.MetadataFetcher
import com.videodetails.ai.data.VideoInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class DetailsUiState(
    val info: VideoInfo? = null,
    val loading: Boolean = true,
    val aiLoading: Boolean = false,
    val aiSummary: String? = null,
    val aiFailed: Boolean = false,
    val aiError: String? = null
)

/**
 * Ek url ke liye poori pipeline:
 * 1. metadata fetch  -> turant UI me
 * 2. history me save
 * 3. (key ho to) AI summary -> baad me update
 */
fun detailsFlow(
    context: Context,
    url: String,
    apiKey: String,
    hindi: Boolean
): Flow<DetailsUiState> = flow {
    val appContext = context.applicationContext
    emit(DetailsUiState(loading = true))

    val info = MetadataFetcher.fetch(url)
    emit(DetailsUiState(info = info, loading = false))

    if (info.status != FetchStatus.ERROR) {
        HistoryStore.add(appContext, info)
    }

    if (apiKey.isNotBlank() && info.status != FetchStatus.ERROR) {
        emit(DetailsUiState(info = info, loading = false, aiLoading = true))
        val summary = AiAnalyzer.summarize(info, apiKey, hindi)
        emit(
            DetailsUiState(
                info = info,
                loading = false,
                aiSummary = summary,
                aiFailed = summary == null,
                aiError = if (summary == null) AiAnalyzer.lastError else null
            )
        )
    }
}
