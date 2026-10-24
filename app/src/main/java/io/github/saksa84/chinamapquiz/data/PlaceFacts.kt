package io.github.saksa84.chinamapquiz.data

import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Fiche « En savoir plus » d'un lieu, rattachée par son identifiant (fichier `facts.json`).
 * Contenu non géographique, séparé des noms et des tracés, comme le seront les spécialités culinaires.
 * Toutes les rubriques sont facultatives : une province n'a pas de « rôle », une ville pas de superficie.
 */
@Serializable
data class PlaceFacts(
    val id: String,
    /** Population en millions et année du recensement ou de l'estimation. */
    val populationMillions: Double? = null,
    val populationYear: Int? = null,
    val areaKm2: Int? = null,
    /** Langues ou dialectes parlés. */
    val languages: String? = null,
    val history: String? = null,
    val economy: String? = null,
    /** Plat emblématique. */
    val dish: String? = null,
    /** Villes : capitale, port, centre industriel… */
    val role: String? = null,
    /** Villes : monument ou lieu célèbre. */
    val landmark: String? = null,
    /** Une ou deux anecdotes. */
    val anecdotes: List<String> = emptyList(),
    /** Faux tant que la fiche n'a pas été relue : l'application affiche « à vérifier ». */
    val reviewed: Boolean = false,
) {
    val texts: List<String> get() = listOfNotNull(languages, history, economy, dish, role, landmark) + anecdotes
}

@Serializable
data class PlaceFactsFile(
    val schemaVersion: Int,
    /** Origine du contenu (rédaction, sources consultées). */
    val origin: String,
    val facts: List<PlaceFacts>,
)

sealed interface FactsError {
    data class UnsupportedSchema(val version: Int) : FactsError
    data class DuplicateId(val id: String) : FactsError
    /** Fiche d'un lieu absent des données géographiques. */
    data class UnknownId(val id: String) : FactsError
    data class Empty(val id: String) : FactsError
    /** Texte trop long pour rester une fiche courte, ou trop d'anecdotes. */
    data class TooLong(val id: String, val text: String) : FactsError
    /** Population, année ou superficie impossible. */
    data class InvalidNumber(val id: String) : FactsError
}

sealed interface FactsResult {
    data class Success(val facts: Map<String, PlaceFacts>) : FactsResult
    data class Malformed(val message: String) : FactsResult
    data class Invalid(val errors: List<FactsError>) : FactsResult
}

/** Lit et valide `facts.json` par rapport au catalogue déjà chargé. Ne dépend pas d'Android. */
object PlaceFactsLoader {
    const val SUPPORTED_SCHEMA_VERSION = 1
    const val MAX_TEXT_LENGTH = 280
    const val MAX_ANECDOTES = 2

    private val json = Json { ignoreUnknownKeys = true }

    fun load(text: String, catalog: GeoCatalog): FactsResult {
        val file = try {
            json.decodeFromString<PlaceFactsFile>(text)
        } catch (e: SerializationException) {
            return FactsResult.Malformed(e.message ?: "JSON invalide")
        } catch (e: IllegalArgumentException) {
            return FactsResult.Malformed(e.message ?: "JSON invalide")
        }
        val errors = validate(file, catalog)
        return if (errors.isEmpty()) FactsResult.Success(file.facts.associateBy { it.id }) else FactsResult.Invalid(errors)
    }

    fun validate(file: PlaceFactsFile, catalog: GeoCatalog): List<FactsError> {
        val errors = mutableListOf<FactsError>()
        if (file.schemaVersion != SUPPORTED_SCHEMA_VERSION) errors += FactsError.UnsupportedSchema(file.schemaVersion)
        val seen = mutableSetOf<String>()
        for (facts in file.facts) {
            if (!seen.add(facts.id)) errors += FactsError.DuplicateId(facts.id)
            if (catalog[facts.id] == null) errors += FactsError.UnknownId(facts.id)
            if (facts.texts.isEmpty() && facts.populationMillions == null && facts.areaKm2 == null) errors += FactsError.Empty(facts.id)
            facts.texts.filter { it.isBlank() || it.length > MAX_TEXT_LENGTH }.forEach { errors += FactsError.TooLong(facts.id, it) }
            if (facts.anecdotes.size > MAX_ANECDOTES) errors += FactsError.TooLong(facts.id, "${facts.anecdotes.size} anecdotes")
            val population = facts.populationMillions
            val numbersOk = (population == null || (population > 0 && facts.populationYear in 1950..2100)) &&
                (population != null || facts.populationYear == null) &&
                (facts.areaKm2 == null || facts.areaKm2 > 0)
            if (!numbersOk) errors += FactsError.InvalidNumber(facts.id)
        }
        return errors
    }
}
