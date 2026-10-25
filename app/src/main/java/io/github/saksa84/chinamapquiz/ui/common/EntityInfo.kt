package io.github.saksa84.chinamapquiz.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.Designation
import io.github.saksa84.chinamapquiz.data.DesignationType
import io.github.saksa84.chinamapquiz.data.FeatureType
import io.github.saksa84.chinamapquiz.data.GeoCatalog
import io.github.saksa84.chinamapquiz.data.GeoEntity
import io.github.saksa84.chinamapquiz.data.PlaceFacts
import io.github.saksa84.chinamapquiz.game.AnswerNormalizer
import java.util.Locale

/** Réglage « afficher les caractères chinois », fourni une fois à la racine de l'application. */
val LocalShowHanzi = compositionLocalOf { true }

/** Fiche d'un lieu : nom, hanzi, type, capitale, grande région, statut disputé, alias. */
@Composable
fun EntityInfo(
    catalog: GeoCatalog,
    entity: GeoEntity,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    showAliases: Boolean = true,
    /** Fiche « En savoir plus » (mode Apprendre), dépliable sous les informations de base. */
    facts: PlaceFacts? = null,
    factsExpanded: Boolean = false,
    onFactsExpandedChange: (Boolean) -> Unit = {},
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                entity.displayName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold,
                color = color, modifier = Modifier.weight(1f, fill = false),
            )
            SpeakButton(entity.hanzi)
        }
        val subtitle = subtitle(entity)
        Text(subtitle, style = MaterialTheme.typography.titleMedium, color = color)
        Spacer(Modifier.height(8.dp))

        entity.capital?.let { Text(stringResource(R.string.info_capital, it), color = color) }
        catalog.topRegionOf(entity.id)?.let { top ->
            val sub = catalog.children(top.id).firstOrNull { entity.id in it.memberIds }
            Text(
                if (sub == null) stringResource(R.string.info_region, top.shortName)
                else stringResource(R.string.info_region_with_sub, top.shortName, sub.shortName),
                color = color,
            )
        }
        if (entity.type == FeatureType.REGION) {
            val members = entity.memberIds.mapNotNull { catalog[it]?.shortName }
            Text(stringResource(R.string.info_members, members.joinToString(", ")), color = color)
            val subRegions = catalog.children(entity.id).filter { it.type == FeatureType.REGION }
            if (subRegions.isNotEmpty()) {
                val text = subRegions.joinToString(", ") { sub -> "${sub.shortName} (${sub.memberIds.mapNotNull { catalog[it]?.shortName }.joinToString(", ")})" }
                Text(stringResource(R.string.info_subregions, text), color = color)
            }
        }
        val parent = entity.parentId?.let { catalog[it] }
        when (entity.type) {
            FeatureType.CITY -> {
                parent?.let { province ->
                    val isCapital = province.capital == entity.name
                    Text(
                        stringResource(if (isCapital) R.string.info_province_capital else R.string.info_province, province.shortName),
                        color = color,
                    )
                }
                entity.population?.let {
                    Text(stringResource(R.string.info_population, String.format(Locale.FRENCH, "%.1f", it / 1e6)), color = color)
                }
            }
            FeatureType.TRIBUTARY -> {
                RiverLength(entity, color)
                parent?.let { Text(stringResource(R.string.info_flows_into, it.shortName), color = color) }
            }
            FeatureType.MAJOR_RIVER -> {
                RiverLength(entity, color)
                val tributaries = catalog.children(entity.id).map { it.shortName }
                if (tributaries.isNotEmpty()) Text(stringResource(R.string.info_tributaries, tributaries.joinToString(", ")), color = color)
            }
            FeatureType.PEAK -> {
                entity.elevation?.let { Text(stringResource(R.string.info_elevation, String.format(Locale.FRENCH, "%,d", it)), color = color) }
                parent?.let { Text(stringResource(R.string.info_range, it.shortName), color = color) }
            }
            FeatureType.MOUNTAIN_RANGE -> {
                val peaks = catalog.children(entity.id).map { it.shortName }
                if (peaks.isNotEmpty()) Text(stringResource(R.string.info_peaks, peaks.joinToString(", ")), color = color)
            }
            FeatureType.LAKE, FeatureType.RESERVOIR -> {
                parent?.let { Text(stringResource(R.string.info_province, it.shortName), color = color) }
                entity.areaKm2?.let { Text(stringResource(R.string.facts_area, areaText(it)), color = color) }
                entity.elevation?.let { Text(stringResource(R.string.info_elevation, String.format(Locale.FRENCH, "%,d", it)), color = color) }
            }
            FeatureType.PARK -> {
                parent?.let { Text(stringResource(R.string.info_province, it.shortName), color = color) }
                entity.designations.forEach { Text(designationText(it), color = color) }
            }
            else -> Unit
        }
        if (entity.claimedBy.isNotEmpty()) {
            // Noms des pays fournis par le système, à partir des codes ISO : aucune liste à maintenir.
            val claimants = entity.claimedBy.joinToString(", ") {
                Locale.Builder().setRegion(it).build().getDisplayCountry(Locale.FRENCH)
            }
            Text(stringResource(R.string.info_disputed, claimants), color = MaterialTheme.colorScheme.tertiary)
        }
        // Le nom connu est déjà dans le titre : on n'en répète pas la variante sans accents (Mekong pour Mékong).
        val known = entity.knownAs?.let(AnswerNormalizer::normalize)
        val aliases = entity.aliases.filter { AnswerNormalizer.normalize(it) != known }
        if (showAliases && aliases.isNotEmpty()) {
            Text(
                stringResource(R.string.info_also_known_as, aliases.joinToString(", ")),
                style = MaterialTheme.typography.bodySmall,
                color = color,
            )
        }
        if (facts != null) FactsSection(facts, factsExpanded, onFactsExpandedChange, color)
    }
}

/**
 * « En savoir plus » : rubriques de la fiche, repliées par défaut pour garder la carte visible.
 * L'état est tenu par l'écran, qui agrandit le panneau quand la section est dépliée.
 */
@Composable
private fun FactsSection(facts: PlaceFacts, expanded: Boolean, onExpandedChange: (Boolean) -> Unit, color: Color) {
    TextButton(onClick = { onExpandedChange(!expanded) }, contentPadding = PaddingValues(0.dp)) {
        // Sur le fond sombre des quiz, le bouton prend la couleur des consignes pour rester lisible.
        Text(
            stringResource(if (expanded) R.string.facts_less else R.string.facts_more),
            color = if (color == Color.Unspecified) Color.Unspecified else QuizColors.Accent,
        )
    }
    if (!expanded) return
    val lines = buildList {
        facts.overview?.let { add(it) }
        facts.legend?.let { add(stringResource(R.string.facts_legend, it)) }
        populationText(facts)?.let { add(it) }
        facts.areaKm2?.let { add(stringResource(R.string.facts_area, String.format(Locale.FRENCH, "%,d", it))) }
        facts.languages?.let { add(stringResource(R.string.facts_languages, it)) }
        facts.role?.let { add(stringResource(R.string.facts_role, it)) }
        facts.history?.let { add(stringResource(R.string.facts_history, it)) }
        facts.economy?.let { add(stringResource(R.string.facts_economy, it)) }
        facts.dish?.let { add(stringResource(R.string.facts_dish, it)) }
        facts.landmark?.let { add(stringResource(R.string.facts_landmark, it)) }
        facts.anecdotes.forEach { add(stringResource(R.string.facts_anecdote, it)) }
    }
    lines.forEach { Text(it, color = color, modifier = Modifier.padding(bottom = 6.dp)) }
    if (!facts.reviewed) {
        Text(stringResource(R.string.facts_unreviewed), style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic, color = color)
    }
}

@Composable
private fun populationText(facts: PlaceFacts): String? {
    val millions = facts.populationMillions ?: return null
    val year = facts.populationYear ?: return null
    return stringResource(R.string.facts_population, millionsText(millions), year)
}

/** « Patrimoine mondial de l'UNESCO (1992) ». */
@Composable
private fun designationText(designation: Designation): String {
    val name = stringResource(
        when (designation.type) {
            DesignationType.NATIONAL_PARK -> R.string.designation_national_park
            DesignationType.WORLD_HERITAGE -> R.string.designation_world_heritage
            DesignationType.GLOBAL_GEOPARK -> R.string.designation_global_geopark
            DesignationType.NATIONAL_NATURE_RESERVE -> R.string.designation_nature_reserve
            DesignationType.NATIONAL_SCENIC_AREA -> R.string.designation_scenic_area
        },
    )
    return designation.year?.let { stringResource(R.string.designation_with_year, name, it) } ?: name
}

/** Superficie d'un lac : « 4 489 », « 6,5 » pour Xi Hu. */
private fun areaText(km2: Double): String =
    if (km2 < 100) String.format(Locale.FRENCH, "%.1f", km2) else String.format(Locale.FRENCH, "%,d", Math.round(km2))

/** 83,7 ; 0,68 pour Macao (moins d'un million). */
private fun millionsText(millions: Double): String =
    String.format(Locale.FRENCH, if (millions < 1) "%.2f" else "%.1f", millions)

/**
 * Fiche compacte du retour de réponse en jeu (étape E) : 4 lignes au plus, pour ne pas cacher
 * la carte. Nom connu / pinyin, hanzi et type, un repère (capitale, province, longueur, altitude),
 * la langue parlée.
 */
@Composable
fun CompactEntityInfo(
    catalog: GeoCatalog,
    entity: GeoEntity,
    facts: PlaceFacts?,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    /** Fiche « En savoir plus » dépliée sous les 4 lignes ; sans bouton si [onFactsExpandedChange] est null. */
    factsExpanded: Boolean = false,
    onFactsExpandedChange: ((Boolean) -> Unit)? = null,
) {
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                entity.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                color = color, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false),
            )
            SpeakButton(entity.hanzi)
        }
        Text(subtitle(entity), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        val parent = entity.parentId?.let { catalog[it] }
        val key = when (entity.type) {
            FeatureType.CITY -> listOfNotNull(
                parent?.let { stringResource(R.string.info_province, it.shortName) },
                entity.population?.let { stringResource(R.string.compact_population, millionsText(it / 1e6)) },
            )
            FeatureType.MAJOR_RIVER, FeatureType.TRIBUTARY ->
                listOfNotNull(entity.lengthKm?.let { stringResource(R.string.info_length, String.format(Locale.FRENCH, "%,d", it)) })
            FeatureType.PEAK -> listOfNotNull(
                entity.elevation?.let { stringResource(R.string.info_elevation, String.format(Locale.FRENCH, "%,d", it)) },
                parent?.shortName,
            )
            FeatureType.LAKE, FeatureType.RESERVOIR -> listOfNotNull(
                parent?.shortName,
                entity.areaKm2?.let { stringResource(R.string.compact_area, areaText(it)) },
                entity.elevation?.let { stringResource(R.string.compact_elevation, String.format(Locale.FRENCH, "%,d", it)) },
            )
            FeatureType.PARK -> listOfNotNull(parent?.shortName) + entity.designations.take(2).map { designationText(it) }
            else -> listOfNotNull(
                entity.capital?.let { stringResource(R.string.info_capital, it) },
                facts?.populationMillions?.let { stringResource(R.string.compact_population, millionsText(it)) },
            )
        }
        if (key.isNotEmpty()) Text(key.joinToString(" · "), color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        facts?.languages?.let {
            Text(
                stringResource(R.string.facts_languages, it), color = color, style = MaterialTheme.typography.bodySmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        if (facts != null && onFactsExpandedChange != null) FactsSection(facts, factsExpanded, onFactsExpandedChange, color)
    }
}

/** « Longueur : 6 300 km », avec la partie chinoise pour les fleuves transfrontaliers. */
@Composable
private fun RiverLength(entity: GeoEntity, color: Color) {
    val total = entity.lengthKm ?: return
    fun km(value: Int) = String.format(Locale.FRENCH, "%,d", value)
    val inChina = entity.lengthInChinaKm
    val text = if (inChina == null) {
        stringResource(R.string.info_length, km(total))
    } else {
        stringResource(R.string.info_length_in_china, km(total), km(inChina))
    }
    Text(text, color = color)
}

/** « Sìchuān · 四川 · Province » : pinyin avec tons, hanzi (selon le réglage) et type. */
@Composable
private fun subtitle(entity: GeoEntity): String {
    val hanzi = entity.hanzi.takeIf { LocalShowHanzi.current }
    return listOfNotNull(entity.pinyinTones, hanzi, stringResource(typeLabel(entity))).joinToString(" · ")
}

/** Un parc n'est « parc national » que s'il en a le statut ; sinon c'est un site naturel. */
@StringRes
private fun typeLabel(entity: GeoEntity): Int =
    if (entity.type == FeatureType.PARK) {
        if (entity.designations.any { it.type == DesignationType.NATIONAL_PARK }) R.string.type_national_park else R.string.type_natural_site
    } else {
        typeLabel(entity.type)
    }

@StringRes
private fun typeLabel(type: FeatureType): Int = when (type) {
    FeatureType.COUNTRY -> R.string.type_country
    FeatureType.PROVINCE -> R.string.type_province
    FeatureType.AUTONOMOUS_REGION -> R.string.type_autonomous_region
    FeatureType.MUNICIPALITY -> R.string.type_municipality
    FeatureType.SPECIAL_ADMINISTRATIVE_REGION -> R.string.type_special_administrative_region
    FeatureType.ISLAND_GROUP -> R.string.type_island_group
    FeatureType.REGION -> R.string.type_region
    FeatureType.CITY -> R.string.type_city
    FeatureType.MAJOR_RIVER -> R.string.type_major_river
    FeatureType.TRIBUTARY -> R.string.type_tributary
    FeatureType.MOUNTAIN_RANGE -> R.string.type_mountain_range
    FeatureType.PEAK -> R.string.type_peak
    FeatureType.LAKE -> R.string.type_lake
    FeatureType.RESERVOIR -> R.string.type_reservoir
    else -> R.string.type_other
}
