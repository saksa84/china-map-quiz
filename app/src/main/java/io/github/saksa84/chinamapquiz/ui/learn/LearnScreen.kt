package io.github.saksa84.chinamapquiz.ui.learn

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.AssetGeoRepository
import io.github.saksa84.chinamapquiz.data.FeatureType
import io.github.saksa84.chinamapquiz.data.GeoCatalog
import io.github.saksa84.chinamapquiz.data.GeoEntity
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.ui.map.ChinaMap
import io.github.saksa84.chinamapquiz.ui.map.ShapeStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

private sealed interface LoadState {
    data object Loading : LoadState
    data class Ready(val data: MapData) : LoadState
    data class Failed(val message: String) : LoadState
}

/** Mode Learn, version prototype : toucher une région affiche son nom et ses informations. */
@Composable
fun LearnScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val state by produceState<LoadState>(LoadState.Loading) {
        value = withContext(Dispatchers.IO) {
            runCatching { AssetGeoRepository(context.assets).load() }
                .fold({ LoadState.Ready(it) }, { LoadState.Failed(it.message ?: it.toString()) })
        }
    }

    Scaffold(modifier = modifier.fillMaxSize()) { padding ->
        Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val s = state) {
                LoadState.Loading -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.loading))
                }
                is LoadState.Failed -> Text(
                    stringResource(R.string.loading_error, s.message),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(24.dp),
                )
                is LoadState.Ready -> LearnContent(s.data)
            }
        }
    }
}

@Composable
private fun LearnContent(data: MapData) {
    // rememberSaveable : la sélection survit à la rotation de l'écran.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var recenterKey by rememberSaveable { mutableIntStateOf(0) }
    val catalog = data.catalog

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.learn_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = { recenterKey++ }) { Text(stringResource(R.string.map_recenter)) }
        }

        val map = @Composable { modifier: Modifier ->
            ChinaMap(
                geometry = data.geometry,
                styleOf = { id -> styleFor(catalog, id, selected = id == selectedId) },
                selectedId = selectedId,
                onShapeTap = { selectedId = it },
                recenterKey = recenterKey,
                modifier = modifier,
            )
        }
        // Taille fixe du panneau : la carte ne change pas de taille selon la quantité d'informations.
        // En paysage, le panneau passe à droite pour laisser toute la hauteur à la carte.
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            if (maxWidth > maxHeight) {
                Row(Modifier.fillMaxSize()) {
                    map(Modifier.weight(1f).fillMaxHeight())
                    InfoPanel(catalog, selectedId, Modifier.width(340.dp).fillMaxHeight())
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    map(Modifier.weight(1f).fillMaxWidth())
                    InfoPanel(catalog, selectedId, Modifier.fillMaxWidth().height(190.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoPanel(catalog: GeoCatalog, selectedId: String?, modifier: Modifier) {
    Surface(tonalElevation = 3.dp, modifier = modifier) {
        val entity = selectedId?.let { catalog[it] }
        if (entity == null) {
            Text(stringResource(R.string.learn_hint), modifier = Modifier.padding(16.dp))
        } else {
            EntityInfo(catalog, entity)
        }
    }
}

@Composable
private fun EntityInfo(catalog: GeoCatalog, entity: GeoEntity) {
    Column(Modifier.padding(16.dp)) {
        Text(entity.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        val subtitle = listOfNotNull(entity.hanzi, stringResource(typeLabel(entity.type))).joinToString(" · ")
        Text(subtitle, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        entity.capital?.let { Text(stringResource(R.string.info_capital, it)) }
        catalog.topRegionOf(entity.id)?.let { top ->
            val sub = catalog.children(top.id).firstOrNull { entity.id in it.memberIds }
            Text(
                if (sub == null) stringResource(R.string.info_region, top.name)
                else stringResource(R.string.info_region_with_sub, top.name, sub.name),
            )
        }
        if (entity.claimedBy.isNotEmpty()) {
            // Noms des pays fournis par le système, à partir des codes ISO : aucune liste à maintenir.
            val claimants = entity.claimedBy.joinToString(", ") {
                Locale.Builder().setRegion(it).build().getDisplayCountry(Locale.FRENCH)
            }
            Text(stringResource(R.string.info_disputed, claimants), color = MaterialTheme.colorScheme.tertiary)
        }
        if (entity.aliases.isNotEmpty()) {
            Text(
                stringResource(R.string.info_also_known_as, entity.aliases.joinToString(", ")),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

/** Couleurs pastel par grande région ; le lieu sélectionné est plus soutenu. */
private val REGION_COLORS = mapOf(
    "RG-DONGBEI" to Color(0xFFE8A0A8),
    "RG-HUABEI" to Color(0xFFF2C98A),
    "RG-HUADONG" to Color(0xFF9CC3E6),
    "RG-ZHONGNAN" to Color(0xFFA8D5A2),
    "RG-XIBEI" to Color(0xFFD7B8E8),
    "RG-XINAN" to Color(0xFFF0E39A),
)
private val DISPUTED_COLOR = Color(0xFFC9C9C9)
private val DEFAULT_COLOR = Color(0xFFE0E0E0)

private fun styleFor(catalog: GeoCatalog, id: String, selected: Boolean): ShapeStyle {
    val entity = catalog[id]
    val base = when {
        entity == null -> DEFAULT_COLOR
        entity.claimedBy.isNotEmpty() -> DISPUTED_COLOR
        else -> catalog.topRegionOf(id)?.let { REGION_COLORS[it.id] } ?: DEFAULT_COLOR
    }
    val fill = if (selected) base.darken(0.35f) else base
    return ShapeStyle(fill = fill, dashedBorder = entity?.claimedBy?.isNotEmpty() == true)
}

private fun Color.darken(amount: Float) = Color(red * (1 - amount), green * (1 - amount), blue * (1 - amount), alpha)

@StringRes
private fun typeLabel(type: FeatureType): Int = when (type) {
    FeatureType.COUNTRY -> R.string.type_country
    FeatureType.PROVINCE -> R.string.type_province
    FeatureType.AUTONOMOUS_REGION -> R.string.type_autonomous_region
    FeatureType.MUNICIPALITY -> R.string.type_municipality
    FeatureType.SPECIAL_ADMINISTRATIVE_REGION -> R.string.type_special_administrative_region
    FeatureType.ISLAND_GROUP -> R.string.type_island_group
    else -> R.string.type_other
}
