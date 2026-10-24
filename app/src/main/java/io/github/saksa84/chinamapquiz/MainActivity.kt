package io.github.saksa84.chinamapquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.saksa84.chinamapquiz.ui.ChinaMapQuizApp
import io.github.saksa84.chinamapquiz.ui.theme.ChinaMapQuizTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChinaMapQuizTheme {
                ChinaMapQuizApp()
            }
        }
    }
}
