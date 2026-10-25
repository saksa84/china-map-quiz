package io.github.saksa84.chinamapquiz.ui

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.AssetGeoRepository
import io.github.saksa84.chinamapquiz.game.GeoCategory
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.game.AnswerChecker
import io.github.saksa84.chinamapquiz.game.AnswerRecord
import io.github.saksa84.chinamapquiz.game.EliminateSession
import io.github.saksa84.chinamapquiz.game.LevelsConfig
import io.github.saksa84.chinamapquiz.game.QuizLevel
import io.github.saksa84.chinamapquiz.game.QuizLevels
import io.github.saksa84.chinamapquiz.game.QuizSession
import io.github.saksa84.chinamapquiz.game.QuizSummary
import io.github.saksa84.chinamapquiz.progress.Progress
import io.github.saksa84.chinamapquiz.progress.ProgressStore
import io.github.saksa84.chinamapquiz.progress.Settings
import io.github.saksa84.chinamapquiz.speech.AnswerSounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Écrans. Parcours : accueil (catégories) → catégorie (modes) → niveau → partie → résultat. */
enum class Screen { HOME, CATEGORY, LEARN, LEVELS, GAME, RESULT, PROGRESS, SETTINGS }

/** Modes de quiz jouables. */
enum class GameMode { FIND, TYPE, ELIMINATE }

sealed interface LoadState {
    data object Loading : LoadState
    data class Ready(val data: MapData, val levels: Map<GeoCategory, List<QuizLevel>>) : LoadState {
        /** Tous les lieux jouables, toutes catégories confondues. */
        val playableIds: Set<String> get() = levels.values.flatten().flatMap { it.targetIds }.toSet()
    }
    data class Failed(val message: String) : LoadState
}

/**
 * Retour bref après un toucher en mode Éliminer : le lieu touché clignote (et, en cas d'erreur,
 * le lieu demandé [targetId] est montré) avant la consigne suivante.
 */
data class Flash(val id: String, val correct: Boolean, val targetId: String)

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
    /** Modes Trouver et Éliminer : lieu touché mais pas encore confirmé. */
    val pendingId: String? = null,
    /** Modes Trouver et Saisir : réponse affichée pendant le retour. */
    val lastAnswer: AnswerRecord? = null,
    /** Mode Éliminer : régions déjà trouvées. */
    val eliminated: Set<String> = emptySet(),
    val flash: Flash? = null,
    /** Modes Trouver et Saisir : fiche « En savoir plus » dépliée pendant le retour (chrono en pause). */
    val factsExpanded: Boolean = false,
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
    var category by mutableStateOf(GeoCategory.REGIONS)
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

    /** Progression sauvegardée (meilleurs scores, erreurs fréquentes) et réglages. */
    var progress by mutableStateOf(Progress())
        private set
    var settings by mutableStateOf(Settings())
        private set

    /** La dernière partie a battu le meilleur résultat du niveau. */
    var newRecord by mutableStateOf(false)
        private set

    private val store = ProgressStore(application)

    private var level: QuizLevel? = null
    private var quiz: QuizSession? = null
    private var eliminate: EliminateSession? = null
    private var pendingId: String? = null
    private var flash: Flash? = null
    private var checker: AnswerChecker? = null
    private var factsExpanded = false
    private val sounds = AnswerSounds(application)
    private val clock = { SystemClock.elapsedRealtime() }

    init {
        viewModelScope.launch { store.progress.collect { progress = it } }
        viewModelScope.launch { store.settings.collect { settings = it } }
        viewModelScope.launch {
            loadState = withContext(Dispatchers.IO) {
                runCatching {
                    val assets = getApplication<Application>().assets
                    val data = AssetGeoRepository(assets).load()
                    val config = LevelsConfig.parse(assets.open("game/levels.json").bufferedReader().use { it.readText() })
                    // Jouable : présent dans les données ET dessiné sur la carte.
                    val playable = data.catalog.entities.map { it.id }.filter { data.geometry[it] != null }.toSet()
                    checker = AnswerChecker(data.catalog)
                    LoadState.Ready(data, QuizLevels.buildAll(config, playable))
                }.getOrElse { LoadState.Failed(it.message ?: it.toString()) }
            }
        }
    }

    fun open(target: Screen) {
        screen = target
    }

    fun resetProgress() {
        viewModelScope.launch { store.reset() }
    }

    fun setShowHanzi(show: Boolean) {
        viewModelScope.launch { store.setShowHanzi(show) }
    }

    fun setAutoSpeak(enabled: Boolean) {
        viewModelScope.launch { store.setAutoSpeak(enabled) }
    }

    fun setAnswerSounds(enabled: Boolean) {
        viewModelScope.launch { store.setAnswerSounds(enabled) }
    }

    private fun playAnswerSound(correct: Boolean) {
        if (settings.answerSounds) sounds.play(correct)
    }

    override fun onCleared() {
        sounds.release()
    }

    fun chooseCategory(newCategory: GeoCategory) {
        category = newCategory
        screen = Screen.CATEGORY
    }

    fun chooseMode(newMode: GameMode) {
        mode = newMode
        screen = Screen.LEVELS
    }

    /** Clé de progression du mode en cours dans la catégorie en cours. */
    val progressKey: String get() = category.progressKey(mode.name)

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
            Screen.CATEGORY, Screen.PROGRESS, Screen.SETTINGS -> Screen.HOME
            Screen.LEARN, Screen.LEVELS -> Screen.CATEGORY
            // Une révision lancée depuis l'écran Progression y ramène.
            Screen.GAME, Screen.RESULT -> if (level?.id?.startsWith(REVIEW_PREFIX) == true) Screen.PROGRESS else Screen.LEVELS
        }
        return true
    }

    fun quitGame() {
        confirmingQuit = false
        quiz = null
        eliminate = null
        screen = if (level?.id?.startsWith(REVIEW_PREFIX) == true) Screen.PROGRESS else Screen.LEVELS
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
        factsExpanded = false
        summary = null
        refresh()
        screen = Screen.GAME
    }

    /**
     * Toucher la carte (Trouver et Éliminer) : sélectionne, et un second toucher sur la même
     * région confirme, comme le bouton Confirmer.
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
            GameMode.ELIMINATE -> {
                val s = eliminate ?: return
                if (flash != null || s.isFinished || id in s.eliminated) return
                if (id == pendingId) confirm() else {
                    pendingId = id
                    refresh()
                }
            }
            GameMode.TYPE -> Unit // en mode Saisir, la carte sert seulement à montrer la région
        }
    }

    fun confirm() {
        val selected = pendingId ?: return
        pendingId = null
        when (mode) {
            GameMode.ELIMINATE -> answerEliminate(selected)
            else -> {
                quiz?.answer(selected)?.let { playAnswerSound(it.correct) }
                refresh()
            }
        }
    }

    /** Mode Saisir : valide le texte tapé. */
    fun submitTyped(text: String) {
        val s = quiz ?: return
        val target = s.currentTarget ?: return
        if (text.isBlank()) return
        s.answer(selectedId = null, typed = text.trim(), correct = checker?.isCorrect(target, text) == true)
            ?.let { playAnswerSound(it.correct) }
        refresh()
    }

    /** Mode Saisir : passe la question (comptée comme une erreur). */
    fun skip() {
        quiz?.answer(selectedId = null, typed = null, correct = false)
        refresh()
    }

    /** Déplie ou replie la fiche du lieu pendant le retour ; le chrono est en pause tant qu'elle est ouverte. */
    fun setFactsExpanded(expanded: Boolean) {
        val s = quiz ?: return
        if (s.phase != QuizSession.Phase.FEEDBACK) return
        factsExpanded = expanded
        if (expanded) s.pause() else s.resume()
        refresh()
    }

    fun next() {
        val s = quiz ?: return
        factsExpanded = false
        s.next()
        if (s.phase == QuizSession.Phase.FINISHED) finish(s.summary())
        refresh()
    }

    /**
     * Éliminer : la réponse confirmée, juste ou fausse. Pendant le clignotement, la consigne
     * reste affichée et la carte ne répond pas ; ensuite vient la consigne suivante. Une erreur reste
     * affichée plus longtemps, pour voir où était le lieu demandé.
     */
    private fun answerEliminate(id: String) {
        val s = eliminate ?: return
        if (flash != null) return
        val target = s.currentTarget ?: return
        val record = s.tap(id) ?: return
        playAnswerSound(record.correct)
        val current = Flash(id, record.correct, target)
        flash = current
        refresh()
        viewModelScope.launch {
            delay(if (record.correct) 600 else 1_500)
            if (flash == current) {
                flash = null
                refresh()
            }
            if (s.isFinished && eliminate === s && screen == Screen.GAME) finish(s.summary())
        }
    }

    private fun finish(result: QuizSummary) {
        val lvl = level
        if (lvl != null) {
            // « Rejouer les erreurs » ne porte que sur une partie du niveau : pas de record, mais les erreurs comptent.
            val isReplay = lvl.id.endsWith(MISTAKES_SUFFIX)
            val outcome = progress.record(progressKey, lvl.id.removeSuffix(MISTAKES_SUFFIX), result, countsForBest = !isReplay)
            progress = outcome.progress
            newRecord = outcome.newRecord
            viewModelScope.launch { store.save(outcome.progress) }
        }
        summary = result
        screen = Screen.RESULT
    }

    fun replay() {
        level?.let(::startLevel)
    }

    /**
     * Révision (mode Trouver) des lieux le plus souvent manqués, toutes catégories confondues :
     * provinces, villes, cours d'eau et montagnes peuvent se mêler dans la même partie.
     */
    fun reviewMissed(count: Int) {
        val ready = loadState as? LoadState.Ready ?: return
        val playable = ready.playableIds
        val ids = progress.mostMissed(count).map { it.first }.filter { it in playable }
        if (ids.isEmpty()) return
        mode = GameMode.FIND
        val name = getApplication<Application>().getString(R.string.review_level)
        startLevel(QuizLevel("$REVIEW_PREFIX$MISTAKES_SUFFIX", name, includesSpecial = true, targetIds = ids))
    }

    /** Nouvelle partie limitée aux lieux manqués. */
    fun replayMistakes() {
        val lvl = level ?: return
        val missed = summary?.missedTargetIds.orEmpty()
        if (missed.isEmpty()) return
        val baseId = lvl.id.removeSuffix(MISTAKES_SUFFIX)
        startLevel(lvl.copy(id = baseId + MISTAKES_SUFFIX, targetIds = missed))
    }

    private companion object {
        const val MISTAKES_SUFFIX = "-mistakes"
        const val REVIEW_PREFIX = "review"
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
                    factsExpanded = factsExpanded,
                )
            }
            GameMode.ELIMINATE -> eliminate?.let { s ->
                GameUiState(
                    mode = mode,
                    level = lvl,
                    phase = if (s.isFinished && flash == null) QuizSession.Phase.FINISHED else QuizSession.Phase.ASKING,
                    // Pendant le clignotement, on garde la consigne à laquelle on vient de répondre.
                    targetId = flash?.targetId ?: s.currentTarget,
                    questionNumber = (s.asked + 1).coerceAtMost(s.total),
                    total = s.total,
                    score = s.eliminated.size,
                    errors = s.errors,
                    eliminated = s.eliminated,
                    pendingId = pendingId,
                    flash = flash,
                )
            }
        }
    }
}
