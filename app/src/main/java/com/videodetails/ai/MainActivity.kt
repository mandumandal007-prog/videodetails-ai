package com.videodetails.ai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.videodetails.ai.ui.AppTheme
import com.videodetails.ai.ui.HomeScreen

/** Launcher screen: link paste + history + settings. */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                HomeScreen(onAnalyze = ::showDetails)
            }
        }
    }

    /** Details wahi half-screen panel me dikhte hain jo share se khulta hai. */
    private fun showDetails(url: String) {
        val intent = Intent(this, ShareActivity::class.java).apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, url)
        }
        startActivity(intent)
    }
}
