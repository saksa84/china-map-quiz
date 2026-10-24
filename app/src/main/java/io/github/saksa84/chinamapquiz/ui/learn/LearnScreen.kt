package io.github.saksa84.chinamapquiz.ui.learn

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.Category
import io.github.saksa84.chinamapquiz.data.FeatureType
import io.github.saksa84.chinamapquiz.data.GeoCatalog
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.map.MapPoint
import io.github.saksa84.chinamapquiz.ui.common.EntityInfo
import io.github.saksa84.chinamapquiz.ui.map.ChinaMap
import io.github.saksa84.chinamapquiz.ui.map.MapLabel
import io.github.saksa84.chinamapquiz.ui.map.ShapeStyle
import kotlin.math.sqrt

/**
 * Mode Apprendre : explorer la carte sans chronomètre. Toucher un lieu affiche sa fiche.
 * Deux vues : provinces, ou grandes régions (toucher une province sélectionne toute sa région).
 * Les noms peuvent être écrits sur la carte : seuls ceux qui ont la place apparaissent, et il y en
 * a de plus en plus quand on zoome.
 */
@Composable
fun LearnScreen(data: MapData, onBack: () -> Unit) {
    // rememberSaveable : sélection et options survivent à la rotation de l'écran.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var regionView by rememberSaveable { mutableStateOf(false) }
    var showNames by rememberSaveable { mutableStateOf(true) }
    var recenterKey by rememberSaveable { mutableIntStateOf(0) }
    val catalog = data.catalog

    val provinceLabels = remember(data) { provinceLabels(data) }
    val regionLabels = remember(data) { regionLabels(data) }

    // En vue régions, le lieu sélectionné est une grande région ; ses provinces sont mises en évidence.
    val highlighted: Set<String> = remember(selectedId, regionView) {
        val selected = selectedId?.let { catalog[it] }
        if (selected?.type == FeatureType.REGION) selected.memberIds.toSet() else setOfNotNull(selectedId)
    }

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
                Text(stringResource(R.string.learn_title), style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = { recenterKey++ }) { Text(stringResource(R.string.map_recenter)) }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !regionView,
                    onClick = { regionView = false; selectedId = null },
                    label = { Text(stringResource(R.string.learn_view_provinces)) },
                )
                FilterChip(
                    selected = regionView,
                    onClick = { regionView = true; selectedId = null },
                    label = { Text(stringResource(R.string.learn_view_regions)) },
                )
                FilterChip(
                    selected = showNames,
                    onClick = { showNames = !showNames },
                    label = { Text(stringResource(R.string.learn_show_names)) },
                )
            }

            val map = @Composable { modifier: Modifier ->
                ChinaMap(
                    geometry = data.geometry,
                    styleOf = { id -> styleFor(catalog, id, highlighted) },
                    selectedId = selectedId?.takeIf { catalog[it]?.type != FeatureType.REGION },
                    onShapeTap = { id ->
                        // Vue régions : une province touchée sélectionne sa grande région.
                        selectedId = if (regionView && id != null) catalog.topRegionOf(id)?.id ?: id else id
                    },
                    recenterKey = recenterKey,
                    labels = when {
                        !showNames -> emptyList()
                        regionView -> regionLabels
                        else -> provinceLabels
                    },
                    modifier = modifier,
                )
            }
            // Taille fixe du panneau : la carte ne change pas de taille selon la quantité d'informations.
            // En paysage, le panneau passe à droite pour laisser toute la hauteur à la carte.
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                if (maxWidth > maxHeight) {
                    Row(Modifier.fillMaxSize()) {
                        map(Modifier.weight(1f).fillMaxHeight())
                        InfoPanel(catalog, selectedId, regionView, Modifier.width(340.dp).fillMaxHeight())
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        map(Modifier.weight(1f).fillMaxWidth())
                        InfoPanel(catalog, selectedId, regionView, Modifier.fillMaxWidth().height(200.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoPanel(catalog: GeoCatalog, selectedId: String?, regionView: Boolean, modifier: Modifier) {
    Surface(tonalElevation = 3.dp, modifier = modifier) {
        val entity = selectedId?.let { catalog[it] }
        // Défilement : la fiche d'une grande région (liste des provinces) peut dépasser le panneau.
        Column(Modifier.verticalScroll(rememberScrollState())) {
            if (entity == null) {
                Text(
                    stringResource(if (regionView) R.string.learn_hint_regions else R.string.learn_hint),
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                EntityInfo(catalog, entity, Modifier.padding(16.dp))
            }
        }
    }
}

/** Noms des provinces, à leur point d'étiquette ; la place disponible vient de la distance au bord. */
private fun provinceLabels(data: MapData): List<MapLabel> = data.geometry.shapes.mapNotNull { shape ->
    val name = data.catalog[shape.id]?.name ?: return@mapNotNull null
    // Le nom peut déborder un peu du cercle inscrit : il est bien plus large que haut, et les
    // chevauchements entre noms sont de toute façon évités au dessin.
    MapLabel(name, shape.label, fitWidth = shape.labelRoom * 4.5f)
}

/** Noms des grandes régions, au barycentre de leurs provinces pondéré par la surface. */
private fun regionLabels(data: MapData): List<MapLabel> {
    val catalog = data.catalog
    return catalog.inCategory(Category.REGION)
        .filter { region -> catalog.topRegionOf(region.memberIds.firstOrNull() ?: "")?.id == region.id }
        .mapNotNull { region ->
            val shapes = region.memberIds.mapNotNull { data.geometry[it] }
            val area = shapes.sumOf { it.area.toDouble() }.toFloat()
            if (shapes.isEmpty() || area == 0f) return@mapNotNull null
            val x = shapes.sumOf { (it.label.x * it.area).toDouble() }.toFloat() / area
            val y = shapes.sumOf { (it.label.y * it.area).toDouble() }.toFloat() / area
            MapLabel(region.name, MapPoint(x, y), fitWidth = sqrt(area))
        }
}

/** Couleurs pastel par grande région ; le lieu (ou la région) sélectionné est plus soutenu. */
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

private fun styleFor(catalog: GeoCatalog, id: String, highlighted: Set<String>): ShapeStyle {
    val entity = catalog[id]
    val base = when {
        entity == null -> DEFAULT_COLOR
        entity.claimedBy.isNotEmpty() -> DISPUTED_COLOR
        else -> catalog.topRegionOf(id)?.let { REGION_COLORS[it.id] } ?: DEFAULT_COLOR
    }
    val selected = id in highlighted
    val fill = if (selected) base.darken(0.35f) else base
    return ShapeStyle(fill = fill, dashedBorder = entity?.claimedBy?.isNotEmpty() == true, emphasized = selected)
}

private fun Color.darken(amount: Float) = Color(red * (1 - amount), green * (1 - amount), blue * (1 - amount), alpha)
