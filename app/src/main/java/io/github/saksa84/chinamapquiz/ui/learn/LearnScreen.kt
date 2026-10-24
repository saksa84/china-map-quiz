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
import io.github.saksa84.chinamapquiz.data.PlaceFacts
import io.github.saksa84.chinamapquiz.game.GeoCategory
import io.github.saksa84.chinamapquiz.map.MapPoint
import io.github.saksa84.chinamapquiz.map.ShapeKind
import io.github.saksa84.chinamapquiz.ui.home.categoryName
import androidx.annotation.StringRes
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
fun LearnScreen(data: MapData, category: GeoCategory, onBack: () -> Unit) {
    // rememberSaveable : sélection et options survivent à la rotation de l'écran.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var regionView by rememberSaveable { mutableStateOf(false) }
    var showNames by rememberSaveable { mutableStateOf(true) }
    var recenterKey by rememberSaveable { mutableIntStateOf(0) }
    // « En savoir plus » déplié : replié de nouveau dès qu'on touche un autre lieu.
    var factsExpanded by rememberSaveable(selectedId) { mutableStateOf(false) }
    val catalog = data.catalog
    val isRegions = category == GeoCategory.REGIONS

    val provinceLabels = remember(data) { provinceLabels(data) }
    val regionLabels = remember(data) { regionLabels(data) }
    // Villes, cours d'eau ou montagnes de la catégorie : les seuls lieux affichés par-dessus les provinces.
    val featureIds = remember(data, category) {
        catalog.entities.filter { it.category in category.categories && data.geometry[it.id]?.overlay == true }.map { it.id }.toSet()
    }
    val featureLabels = remember(data, category) { featureLabels(data, featureIds) }

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
                Text(
                    stringResource(R.string.learn_title_category, stringResource(categoryName(category))),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { recenterKey++ }) { Text(stringResource(R.string.map_recenter)) }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (isRegions) {
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
                }
                FilterChip(
                    selected = showNames,
                    onClick = { showNames = !showNames },
                    label = { Text(stringResource(R.string.learn_show_names)) },
                )
            }

            val map = @Composable { modifier: Modifier ->
                ChinaMap(
                    geometry = data.geometry,
                    styleOf = { id ->
                        if (isRegions) styleFor(catalog, id, highlighted, data) else featureStyle(data, id, featureIds, highlighted)
                    },
                    selectedId = selectedId?.takeIf { catalog[it]?.type != FeatureType.REGION },
                    onShapeTap = { id ->
                        // Vue régions : une province touchée sélectionne sa grande région.
                        selectedId = if (isRegions && regionView && id != null) catalog.topRegionOf(id)?.id ?: id else id
                    },
                    recenterKey = recenterKey,
                    labels = when {
                        !showNames -> emptyList()
                        !isRegions -> featureLabels
                        regionView -> regionLabels
                        else -> provinceLabels
                    },
                    modifier = modifier,
                )
            }
            // Taille fixe du panneau : la carte ne change pas de taille selon la quantité d'informations,
            // sauf quand on déplie « En savoir plus » : le panneau prend alors 60 % de la hauteur.
            // En paysage, le panneau passe à droite pour laisser toute la hauteur à la carte.
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
                if (maxWidth > maxHeight) {
                    Row(Modifier.fillMaxSize()) {
                        map(Modifier.weight(1f).fillMaxHeight())
                        InfoPanel(catalog, data.facts, selectedId, hint(category, regionView), factsExpanded, { factsExpanded = it }, Modifier.width(340.dp).fillMaxHeight())
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        map(Modifier.weight(1f).fillMaxWidth())
                        InfoPanel(
                            catalog, data.facts, selectedId, hint(category, regionView), factsExpanded, { factsExpanded = it },
                            Modifier.fillMaxWidth().then(if (factsExpanded) Modifier.fillMaxHeight(0.6f) else Modifier.height(200.dp)),
                        )
                    }
                }
            }
        }
    }
}

@StringRes
private fun hint(category: GeoCategory, regionView: Boolean): Int = when (category) {
    GeoCategory.REGIONS -> if (regionView) R.string.learn_hint_regions else R.string.learn_hint
    GeoCategory.CITIES -> R.string.learn_hint_cities
    GeoCategory.RIVERS -> R.string.learn_hint_rivers
    GeoCategory.MOUNTAINS -> R.string.learn_hint_mountains
}

@Composable
private fun InfoPanel(
    catalog: GeoCatalog,
    facts: Map<String, PlaceFacts>,
    selectedId: String?,
    @StringRes hint: Int,
    factsExpanded: Boolean,
    onFactsExpandedChange: (Boolean) -> Unit,
    modifier: Modifier,
) {
    Surface(tonalElevation = 3.dp, modifier = modifier) {
        val entity = selectedId?.let { catalog[it] }
        // Défilement : la fiche d'une grande région (liste des provinces) peut dépasser le panneau.
        Column(Modifier.verticalScroll(rememberScrollState())) {
            if (entity == null) {
                Text(stringResource(hint), modifier = Modifier.padding(16.dp))
            } else {
                EntityInfo(catalog, entity, Modifier.padding(16.dp), facts = facts[entity.id], factsExpanded = factsExpanded, onFactsExpandedChange = onFactsExpandedChange)
            }
        }
    }
}

/** Noms des provinces, à leur point d'étiquette ; la place disponible vient de la distance au bord. */
private fun provinceLabels(data: MapData): List<MapLabel> = data.geometry.shapes.filter { !it.overlay }.mapNotNull { shape ->
    val name = data.catalog[shape.id]?.shortName ?: return@mapNotNull null
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

/**
 * Noms des villes (au-dessus de leur point), des cours d'eau (au milieu du plus long tronçon) et
 * des chaînes (au cœur de la zone). Les sommets n'ont pas de nom permanent : trop nombreux près des chaînes.
 */
private fun featureLabels(data: MapData, ids: Set<String>): List<MapLabel> = ids.mapNotNull { id ->
    val shape = data.geometry[id] ?: return@mapNotNull null
    val entity = data.catalog[id] ?: return@mapNotNull null
    when (shape.kind) {
        // Une ville peut toujours écrire son nom ; les chevauchements sont évités au dessin, les plus grandes d'abord.
        ShapeKind.POINT -> if (entity.type == FeatureType.CITY) {
            MapLabel(entity.shortName, shape.label, fitWidth = 1e6f + (entity.population ?: 0L) / 1e3f, aboveDp = 10f)
        } else {
            null
        }
        ShapeKind.LINE, ShapeKind.POLYGON -> MapLabel(entity.shortName, shape.label, fitWidth = shape.labelRoom * 4.5f)
    }
}

/** Couleurs du mode Apprendre pour les villes, cours d'eau et montagnes. */
private val FEATURE_LAND = Color(0xFFEDE7DA)
private val FEATURE_RIVER = Color(0xFF1E88E5)
private val FEATURE_RANGE = Color(0xFFA0522D)
private val FEATURE_CITY = Color(0xFFD32F2F)
private val FEATURE_PEAK = Color(0xFF5D4037)

/** Villes, cours d'eau, montagnes : provinces en fond neutre non touchable, lieux de la catégorie par-dessus. */
private fun featureStyle(data: MapData, id: String, featureIds: Set<String>, highlighted: Set<String>): ShapeStyle? {
    val shape = data.geometry[id] ?: return null
    if (!shape.overlay) return ShapeStyle(FEATURE_LAND, smallMarker = false, tappable = false)
    if (id !in featureIds) return null
    val entity = data.catalog[id]
    val selected = id in highlighted
    val base = when {
        shape.kind == ShapeKind.LINE -> FEATURE_RIVER
        shape.kind == ShapeKind.POLYGON -> FEATURE_RANGE.copy(alpha = if (selected) 0.75f else 0.4f)
        entity?.type == FeatureType.CITY -> FEATURE_CITY
        else -> FEATURE_PEAK
    }
    val width = if (entity?.type == FeatureType.MAJOR_RIVER) 3.5f else 2.2f
    return ShapeStyle(
        fill = if (selected && shape.kind != ShapeKind.POLYGON) base.darken(0.3f) else base,
        emphasized = selected,
        lineWidthDp = if (selected) width * 1.6f else width,
    )
}

private fun styleFor(catalog: GeoCatalog, id: String, highlighted: Set<String>, data: MapData): ShapeStyle? {
    // Vue des régions : villes, cours d'eau et montagnes ne sont pas affichés.
    if (data.geometry[id]?.overlay == true) return null
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
