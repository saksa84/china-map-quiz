package io.github.saksa84.chinamapquiz

import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.saksa84.chinamapquiz.ui.ChinaMapQuizApp
import io.github.saksa84.chinamapquiz.ui.theme.ChinaMapQuizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Les boutons de volume règlent le volume des médias, celui de la voix (bouton 🔊).
        volumeControlStream = AudioManager.STREAM_MUSIC
        enableEdgeToEdge()
        setContent {
            ChinaMapQuizTheme {
                ChinaMapQuizApp()
            }
        }
    }
}
