package io.github.saksa84.chinamapquiz.ui

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.saksa84.chinamapquiz.data.AssetGeoRepository
import io.github.saksa84.chinamapquiz.data.Category
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.game.AnswerRecord
import io.github.saksa84.chinamapquiz.game.LevelsConfig
import io.github.saksa84.chinamapquiz.game.QuizLevel
import io.github.saksa84.chinamapquiz.game.QuizLevels
import io.github.saksa84.chinamapquiz.game.QuizSession
import io.github.saksa84.chinamapquiz.game.QuizSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen { HOME, LEARN, LEVELS, FIND, RESULT }

sealed interface LoadState {
    data object Loading : LoadState
    data class Ready(val data: MapData, val levels: List<QuizLevel>) : LoadState
    data class Failed(val message: String) : LoadState
}

/** Ce que l'écran de jeu affiche ; recalculé après chaque action pour que Compose redessine. */
data class FindUiState(
    val level: QuizLevel,
    val phase: QuizSession.Phase,
    val targetId: String?,
    val questionNumber: Int,
    val total: Int,
    val score: Int,
    /** Lieu touché mais pas encore confirmé. */
    val pendingId: String?,
    val lastAnswer: AnswerRecord?,
)

/**
 * État de l'application, conservé quand l'écran tourne (un ViewModel survit à la recréation
 * de l'activité). Il relie le moteur de jeu, sans Android, aux écrans Compose.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    var loadState by mutableStateOf<LoadState>(LoadState.Loading)
        private set
    var screen by mutableStateOf(Screen.HOME)
        private set
    var find by mutableStateOf<FindUiState?>(null)
        private set
    var summary by mutableStateOf<QuizSummary?>(null)
        private set

    /** Demande de confirmation affichée quand on veut quitter une partie en cours. */
    var confirmingQuit by mutableStateOf(false)
        private set

    private var session: QuizSession? = null
    private var pendingId: String? = null
    private val clock = { SystemClock.elapsedRealtime() }

    init {
        viewModelScope.launch {
            loadState = withContext(Dispatchers.IO) {
                runCatching {
                    val assets = getApplication<Application>().assets
                    val data = AssetGeoRepository(assets).load()
                    val config = LevelsConfig.parse(assets.open("game/levels.json").bufferedReader().use { it.readText() })
                    val mapIds = (data.catalog.inCategory(Category.PROVINCIAL) + data.catalog.inCategory(Category.ISLAND))
                        .map { it.id }
                        .filter { data.geometry[it] != null }
                        .toSet()
                    LoadState.Ready(data, QuizLevels.build(config, mapIds))
                }.getOrElse { LoadState.Failed(it.message ?: it.toString()) }
            }
        }
    }

    fun open(target: Screen) {
        screen = target
    }

    /**
     * Retour arrière ; renvoie false sur l'accueil pour laisser Android fermer l'application.
     * Pendant une partie, on demande d'abord confirmation : un appui involontaire ne doit pas la perdre.
     */
    fun back(): Boolean {
        if (screen == Screen.FIND && session?.phase != QuizSession.Phase.FINISHED) {
            confirmingQuit = true
            return true
        }
        screen = when (screen) {
            Screen.HOME -> return false
            Screen.LEARN, Screen.LEVELS -> Screen.HOME
            Screen.FIND, Screen.RESULT -> Screen.LEVELS
        }
        return true
    }

    fun quitGame() {
        confirmingQuit = false
        session = null
        screen = Screen.LEVELS
    }

    fun keepPlaying() {
        confirmingQuit = false
    }

    fun elapsedMs(): Long = session?.elapsedMs() ?: 0L

    fun startLevel(level: QuizLevel) {
        session = QuizSession.shuffled(level.targetIds, clock)
        pendingId = null
        summary = null
        refresh(level)
        screen = Screen.FIND
    }

    /** Toucher pendant une question : sélectionne ; toucher à nouveau la même région confirme. */
    fun tap(id: String?) {
        val state = find ?: return
        if (state.phase != QuizSession.Phase.ASKING || id == null || id !in state.level.targetIds) return
        if (id == pendingId) confirm() else {
            pendingId = id
            refresh(state.level)
        }
    }

    fun confirm() {
        val state = find ?: return
        val selected = pendingId ?: return
        session?.answer(selected)
        pendingId = null
        refresh(state.level)
    }

    fun next() {
        val state = find ?: return
        val s = session ?: return
        s.next()
        if (s.phase == QuizSession.Phase.FINISHED) {
            summary = s.summary()
            screen = Screen.RESULT
        }
        refresh(state.level)
    }

    fun replay() {
        find?.level?.let(::startLevel)
    }

    /** Nouvelle partie limitée aux lieux manqués. */
    fun replayMistakes() {
        val level = find?.level ?: return
        val missed = summary?.mistakes?.map { it.targetId }.orEmpty()
        if (missed.isEmpty()) return
        startLevel(level.copy(id = "${level.id}-mistakes", targetIds = missed))
    }

    private fun refresh(level: QuizLevel) {
        val s = session ?: return
        find = FindUiState(
            level = level,
            phase = s.phase,
            targetId = s.currentTarget,
            questionNumber = (s.index + 1).coerceAtMost(s.total),
            total = s.total,
            score = s.score,
            pendingId = pendingId,
            lastAnswer = s.lastAnswer,
        )
    }
}
