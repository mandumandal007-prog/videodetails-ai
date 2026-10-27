package com.videodetails.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.videodetails.ai.data.extractUrl
import com.videodetails.ai.ui.AppTheme
import com.videodetails.ai.ui.ShareSheet

/**
 * Share target. Transparent activity — pichhli app ke upar neeche se
 * 78% height ka rounded panel dikhata hai.
 */
class ShareActivity : ComponentActivity() {

    private var sharedUrl by mutableStateOf<String?>(null)
    private var sharedText by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        readIntent(intent)
        setContent {
            AppTheme {
                ShareSheet(
                    url = sharedUrl,
                    rawText = sharedText,
                    onDismiss = { finish() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readIntent(intent)
    }

    private fun readIntent(intent: Intent?) {
        val text = buildString {
            intent?.getStringExtra(Intent.EXTRA_TEXT)?.let { append(it) }
            if (isEmpty()) intent?.getStringExtra(Intent.EXTRA_SUBJECT)?.let { append(it) }
            if (isEmpty()) intent?.dataString?.let { append(it) }
        }
        sharedText = text
        sharedUrl = extractUrl(text)
    }

    override fun finish() {
        super.finish()
        @Suppress("DEPRECATION")
        overridePendingTransition(0, android.R.anim.fade_out)
    }
}
