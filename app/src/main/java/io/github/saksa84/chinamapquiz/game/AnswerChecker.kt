package io.github.saksa84.chinamapquiz.game

import io.github.saksa84.chinamapquiz.data.GeoCatalog
import java.text.Normalizer
import java.util.Locale

/**
 * Met une réponse tapée sous une forme comparable : minuscules, sans tons ni accents
 * (Sìchuān → sichuan, îles → iles), sans espaces, apostrophes ni tirets (Xi'an → xian).
 * Seules les lettres et les chiffres restent.
 */
object AnswerNormalizer {
    private val COMBINING_MARKS = Regex("\\p{M}+")
    private val NOT_ALPHANUMERIC = Regex("[^a-z0-9]")

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(COMBINING_MARKS, "")
            .lowercase(Locale.ROOT)
            .replace(NOT_ALPHANUMERIC, "")
}

/**
 * Valide un nom tapé (mode Saisir) : accepte le nom principal et les alias déclarés dans les
 * données, après normalisation. Aucune autre faute n'est tolérée (décision du 2026-10-24).
 */
class AnswerChecker(private val catalog: GeoCatalog) {

    private val accepted: Map<String, Set<String>> = catalog.entities.associate { entity ->
        entity.id to (listOf(entity.name) + entity.aliases).map(AnswerNormalizer::normalize).filter { it.isNotEmpty() }.toSet()
    }

    fun isCorrect(targetId: String, typed: String): Boolean {
        val normalized = AnswerNormalizer.normalize(typed)
        return normalized.isNotEmpty() && normalized in accepted[targetId].orEmpty()
    }
}
