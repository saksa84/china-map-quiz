package io.github.saksa84.chinamapquiz.game

import kotlin.random.Random

/** Réponse donnée à une question : conservée en entier pour l'écran de résultat et la révision des erreurs. */
data class AnswerRecord(
    val targetId: String,
    /** Lieu touché, ou null si la question a été passée. */
    val selectedId: String?,
    /** Moment de la réponse, en ms depuis le début de la partie. */
    val atMs: Long,
) {
    val correct: Boolean get() = selectedId == targetId
}

data class QuizSummary(
    val total: Int,
    val correct: Int,
    val durationMs: Long,
    val answers: List<AnswerRecord>,
) {
    val mistakes: List<AnswerRecord> get() = answers.filter { !it.correct }
    val percent: Double get() = if (total == 0) 0.0 else 100.0 * correct / total
}

/**
 * Une partie de quiz, indépendante de l'écran et du mode : une liste de lieux à trouver,
 * une seule réponse par question, puis la question suivante.
 *
 * Cycle d'une question : [Phase.ASKING] → [answer] → [Phase.FEEDBACK] → [next] → question suivante,
 * jusqu'à [Phase.FINISHED]. Une liste vide donne directement une partie terminée.
 *
 * @param clock horloge en millisecondes, injectée pour pouvoir tester le chronomètre
 */
class QuizSession(
    val targetIds: List<String>,
    private val clock: () -> Long,
) {
    enum class Phase { ASKING, FEEDBACK, FINISHED }

    private val startMs = clock()
    private var endMs: Long? = null
    private val records = mutableListOf<AnswerRecord>()

    var index: Int = 0
        private set

    var phase: Phase = if (targetIds.isEmpty()) Phase.FINISHED else Phase.ASKING
        private set

    init {
        require(targetIds.toSet().size == targetIds.size) { "Un lieu ne peut être demandé qu'une fois par partie" }
        if (targetIds.isEmpty()) endMs = startMs
    }

    val total: Int get() = targetIds.size
    val currentTarget: String? get() = targetIds.getOrNull(index)?.takeIf { phase != Phase.FINISHED }
    val score: Int get() = records.count { it.correct }
    val answers: List<AnswerRecord> get() = records.toList()

    /** Dernière réponse, affichée pendant [Phase.FEEDBACK]. */
    val lastAnswer: AnswerRecord? get() = records.lastOrNull()

    /** Temps écoulé ; figé à la dernière réponse. */
    fun elapsedMs(): Long = (endMs ?: clock()) - startMs

    /** Enregistre la réponse à la question en cours. Ignorée si on n'attend pas de réponse. */
    fun answer(selectedId: String?): AnswerRecord? {
        if (phase != Phase.ASKING) return null
        val record = AnswerRecord(targetIds[index], selectedId, clock() - startMs)
        records += record
        if (index == targetIds.lastIndex) endMs = clock()
        phase = Phase.FEEDBACK
        return record
    }

    /** Passe à la question suivante après le retour visuel. */
    fun next() {
        if (phase != Phase.FEEDBACK) return
        index++
        phase = if (index >= targetIds.size) Phase.FINISHED else Phase.ASKING
    }

    fun summary(): QuizSummary = QuizSummary(total, score, elapsedMs(), answers)

    companion object {
        /** Partie sur des lieux tirés dans un ordre aléatoire ; [random] est injectable pour des tests reproductibles. */
        fun shuffled(ids: Collection<String>, clock: () -> Long, random: Random = Random.Default) =
            QuizSession(ids.distinct().shuffled(random), clock)
    }
}
