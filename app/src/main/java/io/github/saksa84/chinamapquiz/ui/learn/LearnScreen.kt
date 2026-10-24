package io.github.saksa84.chinamapquiz.ui.learn

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.GeoCatalog
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.ui.common.EntityInfo
import io.github.saksa84.chinamapquiz.ui.map.ChinaMap
import io.github.saksa84.chinamapquiz.ui.map.ShapeStyle

/** Mode Learn : toucher une région affiche son nom et ses informations, sans chronomètre. */
@Composable
fun LearnScreen(data: MapData, onBack: () -> Unit) {
    // rememberSaveable : la sélection survit à la rotation de l'écran.
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    var recenterKey by rememberSaveable { mutableIntStateOf(0) }
    val catalog = data.catalog

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
}

@Composable
private fun InfoPanel(catalog: GeoCatalog, selectedId: String?, modifier: Modifier) {
    Surface(tonalElevation = 3.dp, modifier = modifier) {
        val entity = selectedId?.let { catalog[it] }
        if (entity == null) {
            Text(stringResource(R.string.learn_hint), modifier = Modifier.padding(16.dp))
        } else {
            EntityInfo(catalog, entity, Modifier.padding(16.dp))
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
    return ShapeStyle(fill = fill, dashedBorder = entity?.claimedBy?.isNotEmpty() == true, emphasized = selected)
}

private fun Color.darken(amount: Float) = Color(red * (1 - amount), green * (1 - amount), blue * (1 - amount), alpha)
