package io.github.saksa84.chinamapquiz.game

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Contenu de `assets/game/levels.json`. Les niveaux se règlent là, pas dans le code. */
@Serializable
data class LevelsConfig(
    val schemaVersion: Int,
    /** Unités ajoutées par la variante « complet » : Hong Kong, Macao, Taïwan, Paracel. */
    val specialIds: List<String>,
    val allZone: ZoneName,
    val zones: List<ZoneConfig>,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun parse(text: String): LevelsConfig = json.decodeFromString<LevelsConfig>(text).also {
            require(it.schemaVersion == 1) { "Version de niveaux non prise en charge : ${it.schemaVersion}" }
        }
    }
}

@Serializable
data class ZoneName(val id: String, val name: String)

@Serializable
data class ZoneConfig(val id: String, val name: String, val memberIds: List<String>)

/** Un niveau jouable : une zone et une variante, avec la liste exacte des lieux à trouver. */
data class QuizLevel(
    val id: String,
    val zoneName: String,
    val includesSpecial: Boolean,
    val targetIds: List<String>,
)

object QuizLevels {

    /**
     * Construit les niveaux zone × variante. Une variante identique à la précédente
     * (le Nord n'a pas d'unité spéciale) n'est pas répétée.
     *
     * @param playableIds lieux réellement jouables (présents dans les données ET dessinés sur la carte) :
     *   aucune question ne peut viser un lieu absent.
     */
    fun build(config: LevelsConfig, playableIds: Set<String>): List<QuizLevel> {
        val special = config.specialIds.toSet()
        val wholeCountry = ZoneConfig(config.allZone.id, config.allZone.name, config.zones.flatMap { it.memberIds })

        return (config.zones + wholeCountry).flatMap { zone ->
            val all = zone.memberIds.filter { it in playableIds }.distinct()
            val mainland = all.filter { it !in special }
            buildList {
                if (mainland.isNotEmpty()) add(QuizLevel("${zone.id}-mainland", zone.name, false, mainland))
                if (all.size > mainland.size) add(QuizLevel("${zone.id}-complete", zone.name, true, all))
            }
        }
    }
}
