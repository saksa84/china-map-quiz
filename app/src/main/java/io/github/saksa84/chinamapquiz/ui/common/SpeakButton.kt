package io.github.saksa84.chinamapquiz.ui.common

import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.sp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.speech.Pronouncer
import kotlinx.coroutines.flow.first

/** Synthèse vocale partagée, créée une fois à la racine de l'application ; null dans les aperçus. */
val LocalPronouncer = staticCompositionLocalOf<Pronouncer?> { null }

/** Réglage « lire les consignes » (écran Paramètres). */
val LocalAutoSpeak = compositionLocalOf { true }

/**
 * Lit [hanzi] à voix haute chaque fois qu'il change (nouvelle question), si le réglage est actif.
 * Au tout début d'une partie, attend que le moteur de synthèse ait fini de démarrer.
 */
@Composable
fun AutoSpeak(hanzi: String?) {
    val pronouncer = LocalPronouncer.current ?: return
    val enabled = LocalAutoSpeak.current
    LaunchedEffect(hanzi, enabled) {
        if (!enabled || hanzi.isNullOrBlank()) return@LaunchedEffect
        pronouncer.status.first { it != Pronouncer.Status.STARTING }
        pronouncer.speak(hanzi)
    }
}

/**
 * Bouton 🔊 qui prononce [hanzi]. Si la voix chinoise manque, explique comment l'installer et
 * ouvre l'écran de téléchargement de voix du moteur de synthèse.
 */
@Composable
fun SpeakButton(hanzi: String?, modifier: Modifier = Modifier) {
    val pronouncer = LocalPronouncer.current ?: return
    if (hanzi.isNullOrBlank()) return
    val status by pronouncer.status.collectAsState()
    val context = LocalContext.current
    val missingVoice = stringResource(R.string.speak_missing_voice)
    val unavailable = stringResource(R.string.speak_unavailable)
    val description = stringResource(R.string.speak_description)
    IconButton(
        onClick = {
            when (status) {
                Pronouncer.Status.READY -> pronouncer.speak(hanzi)
                Pronouncer.Status.MISSING_VOICE -> {
                    Toast.makeText(context, missingVoice, Toast.LENGTH_LONG).show()
                    try {
                        context.startActivity(Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    } catch (e: ActivityNotFoundException) {
                        // Pas d'écran d'installation : le message suffit.
                    }
                }
                Pronouncer.Status.UNAVAILABLE -> Toast.makeText(context, unavailable, Toast.LENGTH_LONG).show()
                Pronouncer.Status.STARTING -> Unit
            }
        },
        modifier = modifier.semantics { contentDescription = description },
    ) {
        Text("🔊", fontSize = 22.sp)
    }
}
