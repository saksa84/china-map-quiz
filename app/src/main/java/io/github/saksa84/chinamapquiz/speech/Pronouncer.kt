package io.github.saksa84.chinamapquiz.speech

import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Prononce un nom de lieu avec la synthèse vocale d'Android, en mandarin (zh-CN).
 *
 * On lui donne les caractères chinois (hanzi) plutôt que le pinyin : la voix chinoise lit les
 * hanzi avec les bons tons, alors qu'elle déchiffrerait mal du pinyin sans tons.
 * Aucun fichier audio n'est livré : tout dépend de la voix installée sur le téléphone, qui
 * fonctionne hors ligne une fois téléchargée. Ne dépend ni du moteur de jeu ni de l'interface.
 */
class Pronouncer(context: Context) {

    enum class Status {
        /** Le moteur de synthèse démarre (quelques centaines de millisecondes). */
        STARTING,
        READY,
        /** Moteur présent, mais voix chinoise absente : il faut la télécharger une fois. */
        MISSING_VOICE,
        /** Aucun moteur de synthèse vocale sur l'appareil. */
        UNAVAILABLE,
    }

    private val _status = MutableStateFlow(Status.STARTING)
    val status: StateFlow<Status> = _status.asStateFlow()

    // L'initialisation est asynchrone : le moteur appelle onInit quand il est prêt.
    private val tts: TextToSpeech = TextToSpeech(context.applicationContext) { code -> onInit(code) }

    private fun onInit(code: Int) {
        if (code != TextToSpeech.SUCCESS) {
            _status.value = Status.UNAVAILABLE
            return
        }
        // Canal « médias » : la voix suit le volume des médias, que règlent les boutons du téléphone.
        // Par défaut, la synthèse passe par le volume d'accessibilité, souvent bas.
        tts.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
        )
        val result = tts.setLanguage(Locale.SIMPLIFIED_CHINESE)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            _status.value = Status.MISSING_VOICE
            return
        }
        // La voix choisie par défaut peut être une voix en ligne ou pas encore téléchargée : on
        // préfère une voix mandarin installée sur l'appareil, qui marche hors ligne.
        val installed = tts.voices.orEmpty().filter { voice ->
            voice.locale.language == "zh" && voice.locale.country in setOf("CN", "") &&
                TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED !in voice.features.orEmpty()
        }
        val offline = installed.firstOrNull { !it.isNetworkConnectionRequired }
        (offline ?: installed.firstOrNull())?.let { tts.voice = it }
        // Journal du début et de la fin de chaque lecture : utile pour diagnostiquer sans le son.
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit.also { Log.i(TAG, "Lecture commencée") }
            override fun onDone(utteranceId: String?) = Unit.also { Log.i(TAG, "Lecture terminée") }
            @Deprecated("Remplacée par onError(String, Int)")
            override fun onError(utteranceId: String?) = Unit.also { Log.w(TAG, "Erreur de lecture") }
            override fun onError(utteranceId: String?, errorCode: Int) = Unit.also { Log.w(TAG, "Erreur de lecture $errorCode") }
        })
        _status.value = if (installed.isEmpty()) Status.MISSING_VOICE else Status.READY
        Log.i(TAG, "Voix chinoises installées : ${installed.map { it.name }} ; choisie : ${tts.voice?.name}")
    }

    /** Lit [hanzi] en interrompant la lecture précédente. Faux si la voix n'est pas prête. */
    fun speak(hanzi: String): Boolean {
        if (_status.value != Status.READY || hanzi.isBlank()) return false
        // Volume de la synthèse au maximum (1.0) ; le volume réel est celui des médias.
        val params = Bundle().apply { putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f) }
        return tts.speak(hanzi, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_ID) == TextToSpeech.SUCCESS
    }

    /** Libère le moteur ; à appeler quand l'écran qui l'a créé disparaît. */
    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }

    private companion object {
        const val UTTERANCE_ID = "place-name"
        const val TAG = "Pronouncer"
    }
}
