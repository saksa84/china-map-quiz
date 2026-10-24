package io.github.saksa84.chinamapquiz.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.FeatureType
import io.github.saksa84.chinamapquiz.data.GeoCatalog
import io.github.saksa84.chinamapquiz.data.GeoEntity
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
) {
    Column(modifier) {
        Text(entity.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = color)
        val hanzi = entity.hanzi.takeIf { LocalShowHanzi.current }
        val subtitle = listOfNotNull(hanzi, stringResource(typeLabel(entity.type))).joinToString(" · ")
        Text(subtitle, style = MaterialTheme.typography.titleMedium, color = color)
        Spacer(Modifier.height(8.dp))

        entity.capital?.let { Text(stringResource(R.string.info_capital, it), color = color) }
        catalog.topRegionOf(entity.id)?.let { top ->
            val sub = catalog.children(top.id).firstOrNull { entity.id in it.memberIds }
            Text(
                if (sub == null) stringResource(R.string.info_region, top.name)
                else stringResource(R.string.info_region_with_sub, top.name, sub.name),
                color = color,
            )
        }
        if (entity.type == FeatureType.REGION) {
            val members = entity.memberIds.mapNotNull { catalog[it]?.name }
            Text(stringResource(R.string.info_members, members.joinToString(", ")), color = color)
            val subRegions = catalog.children(entity.id).filter { it.type == FeatureType.REGION }
            if (subRegions.isNotEmpty()) {
                val text = subRegions.joinToString(", ") { sub -> "${sub.name} (${sub.memberIds.mapNotNull { catalog[it]?.name }.joinToString(", ")})" }
                Text(stringResource(R.string.info_subregions, text), color = color)
            }
        }
        val parent = entity.parentId?.let { catalog[it] }
        when (entity.type) {
            FeatureType.CITY -> {
                parent?.let { province ->
                    val isCapital = province.capital == entity.name
                    Text(
                        stringResource(if (isCapital) R.string.info_province_capital else R.string.info_province, province.name),
                        color = color,
                    )
                }
                entity.population?.let {
                    Text(stringResource(R.string.info_population, String.format(Locale.FRENCH, "%.1f", it / 1e6)), color = color)
                }
            }
            FeatureType.TRIBUTARY -> parent?.let { Text(stringResource(R.string.info_flows_into, it.name), color = color) }
            FeatureType.MAJOR_RIVER -> {
                val tributaries = catalog.children(entity.id).map { it.name }
                if (tributaries.isNotEmpty()) Text(stringResource(R.string.info_tributaries, tributaries.joinToString(", ")), color = color)
            }
            FeatureType.PEAK -> {
                entity.elevation?.let { Text(stringResource(R.string.info_elevation, String.format(Locale.FRENCH, "%,d", it)), color = color) }
                parent?.let { Text(stringResource(R.string.info_range, it.name), color = color) }
            }
            FeatureType.MOUNTAIN_RANGE -> {
                val peaks = catalog.children(entity.id).map { it.name }
                if (peaks.isNotEmpty()) Text(stringResource(R.string.info_peaks, peaks.joinToString(", ")), color = color)
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
        if (showAliases && entity.aliases.isNotEmpty()) {
            Text(
                stringResource(R.string.info_also_known_as, entity.aliases.joinToString(", ")),
                style = MaterialTheme.typography.bodySmall,
                color = color,
            )
        }
    }
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
    else -> R.string.type_other
}
