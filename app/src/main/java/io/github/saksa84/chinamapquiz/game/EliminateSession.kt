package io.github.saksa84.chinamapquiz.game

import kotlin.random.Random

/**
 * Partie du mode Éliminer : une consigne à la fois, erreurs illimitées. Une région trouvée est
 * éliminée (plus touchable) et l'on passe à la suivante ; la partie finit quand tout est trouvé.
 *
 * @param clock horloge en millisecondes, injectée pour pouvoir tester le chronomètre
 */
class EliminateSession(
    val targetIds: List<String>,
    private val clock: () -> Long,
) {
    private val startMs = clock()
    private var endMs: Long? = if (targetIds.isEmpty()) startMs else null
    private val records = mutableListOf<AnswerRecord>()
    private val found = mutableSetOf<String>()

    init {
        require(targetIds.toSet().size == targetIds.size) { "Un lieu ne peut être demandé qu'une fois par partie" }
    }

    val total: Int get() = targetIds.size
    val isFinished: Boolean get() = found.size == targetIds.size
    val currentTarget: String? get() = targetIds.getOrNull(found.size)
    val eliminated: Set<String> get() = found.toSet()
    val errors: Int get() = records.count { !it.correct }
    val lastAnswer: AnswerRecord? get() = records.lastOrNull()

    fun elapsedMs(): Long = (endMs ?: clock()) - startMs

    /**
     * Toucher une région. Ignoré si la partie est finie ou si la région est déjà éliminée
     * (on ne peut pas se tromper deux fois sur une région déjà trouvée).
     */
    fun tap(id: String): AnswerRecord? {
        val target = currentTarget ?: return null
        if (id in found) return null
        val record = AnswerRecord(target, id, clock() - startMs)
        records += record
        if (record.correct) {
            found += id
            if (isFinished) endMs = clock()
        }
        return record
    }

    fun summary(): QuizSummary = QuizSummary(total, elapsedMs(), records.toList())

    companion object {
        fun shuffled(ids: Collection<String>, clock: () -> Long, random: Random = Random.Default) =
            EliminateSession(ids.distinct().shuffled(random), clock)
    }
}
