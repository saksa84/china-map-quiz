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
import io.github.saksa84.chinamapquiz.game.AnswerChecker
import io.github.saksa84.chinamapquiz.game.AnswerRecord
import io.github.saksa84.chinamapquiz.game.EliminateSession
import io.github.saksa84.chinamapquiz.game.LevelsConfig
import io.github.saksa84.chinamapquiz.game.QuizLevel
import io.github.saksa84.chinamapquiz.game.QuizLevels
import io.github.saksa84.chinamapquiz.game.QuizSession
import io.github.saksa84.chinamapquiz.game.QuizSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class Screen { HOME, LEARN, LEVELS, GAME, RESULT }

/** Modes de quiz jouables. */
enum class GameMode { FIND, TYPE, ELIMINATE }

sealed interface LoadState {
    data object Loading : LoadState
    data class Ready(val data: MapData, val levels: List<QuizLevel>) : LoadState
    data class Failed(val message: String) : LoadState
}

/** Retour bref après un toucher en mode Éliminer (la région clignote). */
data class Flash(val id: String, val correct: Boolean)

/** Ce que l'écran de jeu affiche ; recalculé après chaque action pour que Compose redessine. */
data class GameUiState(
    val mode: GameMode,
    val level: QuizLevel,
    val phase: QuizSession.Phase,
    val targetId: String?,
    val questionNumber: Int,
    val total: Int,
    val score: Int,
    /** Mode Éliminer : erreurs cumulées. */
    val errors: Int = 0,
    /** Mode Trouver : lieu touché mais pas encore confirmé. */
    val pendingId: String? = null,
    /** Modes Trouver et Saisir : réponse affichée pendant le retour. */
    val lastAnswer: AnswerRecord? = null,
    /** Mode Éliminer : régions déjà trouvées. */
    val eliminated: Set<String> = emptySet(),
    val flash: Flash? = null,
)

/**
 * État de l'application, conservé quand l'écran tourne (un ViewModel survit à la recréation
 * de l'activité). Il relie les moteurs de jeu, sans Android, aux écrans Compose.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    var loadState by mutableStateOf<LoadState>(LoadState.Loading)
        private set
    var screen by mutableStateOf(Screen.HOME)
        private set
    var mode by mutableStateOf(GameMode.FIND)
        private set
    var game by mutableStateOf<GameUiState?>(null)
        private set
    var summary by mutableStateOf<QuizSummary?>(null)
        private set

    /** Demande de confirmation affichée quand on veut quitter une partie en cours. */
    var confirmingQuit by mutableStateOf(false)
        private set

    private var level: QuizLevel? = null
    private var quiz: QuizSession? = null
    private var eliminate: EliminateSession? = null
    private var pendingId: String? = null
    private var flash: Flash? = null
    private var checker: AnswerChecker? = null
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
                    checker = AnswerChecker(data.catalog)
                    LoadState.Ready(data, QuizLevels.build(config, mapIds))
                }.getOrElse { LoadState.Failed(it.message ?: it.toString()) }
            }
        }
    }

    fun openLearn() {
        screen = Screen.LEARN
    }

    fun chooseMode(newMode: GameMode) {
        mode = newMode
        screen = Screen.LEVELS
    }

    private val isPlaying: Boolean
        get() = screen == Screen.GAME && when (mode) {
            GameMode.FIND, GameMode.TYPE -> quiz?.phase != QuizSession.Phase.FINISHED
            GameMode.ELIMINATE -> eliminate?.isFinished == false
        }

    /**
     * Retour arrière ; renvoie false sur l'accueil pour laisser Android fermer l'application.
     * Pendant une partie, on demande d'abord confirmation : un appui involontaire ne doit pas la perdre.
     */
    fun back(): Boolean {
        if (isPlaying) {
            confirmingQuit = true
            return true
        }
        screen = when (screen) {
            Screen.HOME -> return false
            Screen.LEARN, Screen.LEVELS -> Screen.HOME
            Screen.GAME, Screen.RESULT -> Screen.LEVELS
        }
        return true
    }

    fun quitGame() {
        confirmingQuit = false
        quiz = null
        eliminate = null
        screen = Screen.LEVELS
    }

    fun keepPlaying() {
        confirmingQuit = false
    }

    fun elapsedMs(): Long = when (mode) {
        GameMode.FIND, GameMode.TYPE -> quiz?.elapsedMs()
        GameMode.ELIMINATE -> eliminate?.elapsedMs()
    } ?: 0L

    fun startLevel(newLevel: QuizLevel) {
        level = newLevel
        quiz = null
        eliminate = null
        when (mode) {
            GameMode.FIND, GameMode.TYPE -> quiz = QuizSession.shuffled(newLevel.targetIds, clock)
            GameMode.ELIMINATE -> eliminate = EliminateSession.shuffled(newLevel.targetIds, clock)
        }
        pendingId = null
        flash = null
        summary = null
        refresh()
        screen = Screen.GAME
    }

    /**
     * Toucher la carte. Trouver : sélectionne, et un second toucher sur la même région confirme.
     * Éliminer : chaque toucher est une réponse.
     */
    fun tap(id: String?) {
        val lvl = level ?: return
        if (id == null || id !in lvl.targetIds) return
        when (mode) {
            GameMode.FIND -> {
                if (quiz?.phase != QuizSession.Phase.ASKING) return
                if (id == pendingId) confirm() else {
                    pendingId = id
                    refresh()
                }
            }
            GameMode.ELIMINATE -> tapEliminate(id)
            GameMode.TYPE -> Unit // en mode Saisir, la carte sert seulement à montrer la région
        }
    }

    fun confirm() {
        val selected = pendingId ?: return
        quiz?.answer(selected)
        pendingId = null
        refresh()
    }

    /** Mode Saisir : valide le texte tapé. */
    fun submitTyped(text: String) {
        val s = quiz ?: return
        val target = s.currentTarget ?: return
        if (text.isBlank()) return
        s.answer(selectedId = null, typed = text.trim(), correct = checker?.isCorrect(target, text) == true)
        refresh()
    }

    /** Mode Saisir : passe la question (comptée comme une erreur). */
    fun skip() {
        quiz?.answer(selectedId = null, typed = null, correct = false)
        refresh()
    }

    fun next() {
        val s = quiz ?: return
        s.next()
        if (s.phase == QuizSession.Phase.FINISHED) finish(s.summary())
        refresh()
    }

    private fun tapEliminate(id: String) {
        val s = eliminate ?: return
        val record = s.tap(id) ?: return
        val current = Flash(id, record.correct)
        flash = current
        refresh()
        viewModelScope.launch {
            delay(if (s.isFinished) 900 else 600)
            if (flash == current) {
                flash = null
                refresh()
            }
            if (s.isFinished && eliminate === s && screen == Screen.GAME) finish(s.summary())
        }
    }

    private fun finish(result: QuizSummary) {
        summary = result
        screen = Screen.RESULT
    }

    fun replay() {
        level?.let(::startLevel)
    }

    /** Nouvelle partie limitée aux lieux manqués. */
    fun replayMistakes() {
        val lvl = level ?: return
        val missed = summary?.missedTargetIds.orEmpty()
        if (missed.isEmpty()) return
        startLevel(lvl.copy(id = "${lvl.id}-mistakes", targetIds = missed))
    }

    private fun refresh() {
        val lvl = level ?: return
        game = when (mode) {
            GameMode.FIND, GameMode.TYPE -> quiz?.let { s ->
                GameUiState(
                    mode = mode,
                    level = lvl,
                    phase = s.phase,
                    targetId = s.currentTarget,
                    questionNumber = (s.index + 1).coerceAtMost(s.total),
                    total = s.total,
                    score = s.score,
                    pendingId = pendingId,
                    lastAnswer = s.lastAnswer,
                )
            }
            GameMode.ELIMINATE -> eliminate?.let { s ->
                GameUiState(
                    mode = mode,
                    level = lvl,
                    phase = if (s.isFinished) QuizSession.Phase.FINISHED else QuizSession.Phase.ASKING,
                    targetId = s.currentTarget,
                    questionNumber = (s.eliminated.size + 1).coerceAtMost(s.total),
                    total = s.total,
                    score = s.eliminated.size,
                    errors = s.errors,
                    eliminated = s.eliminated,
                    flash = flash,
                )
            }
        }
    }
}
