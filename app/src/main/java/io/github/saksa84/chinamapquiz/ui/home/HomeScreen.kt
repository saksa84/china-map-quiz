package io.github.saksa84.chinamapquiz.ui.home

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.game.GeoCategory
import io.github.saksa84.chinamapquiz.game.QuizLevel
import io.github.saksa84.chinamapquiz.progress.Progress
import io.github.saksa84.chinamapquiz.ui.GameMode
import io.github.saksa84.chinamapquiz.ui.common.MenuTile
import io.github.saksa84.chinamapquiz.ui.common.QuizBackground
import io.github.saksa84.chinamapquiz.ui.common.ScreenHeader

/** Accueil : que veut-on apprendre ? Régions, villes, cours d'eau, montagnes ; puis progression et paramètres. */
@Composable
fun HomeScreen(
    levels: Map<GeoCategory, List<QuizLevel>>,
    progress: Progress,
    onCategory: (GeoCategory) -> Unit,
    onProgress: () -> Unit,
    onSettings: () -> Unit,
) {
    QuizBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.home_title), onBack = null)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                for (category in GeoCategory.entries) {
                    item {
                        MenuTile(
                            title = stringResource(categoryName(category)),
                            subtitle = stringResource(categoryDescription(category)),
                            onClick = { onCategory(category) },
                            progress = categoryCompletion(category, levels[category].orEmpty(), progress),
                        )
                    }
                }
                item { MenuTile(stringResource(R.string.progress_title), stringResource(R.string.progress_sub), onProgress) }
                item { MenuTile(stringResource(R.string.settings_title), null, onSettings) }
            }
        }
    }
}

/** Progression d'une catégorie : moyenne de ses trois modes de quiz. */
fun categoryCompletion(category: GeoCategory, levels: List<QuizLevel>, progress: Progress): Float {
    val sizes = levels.map { it.id to it.targetIds.size }
    val modes = GameMode.entries
    return modes.sumOf { progress.completion(category.progressKey(it.name), sizes).toDouble() }.toFloat() / modes.size
}

@StringRes
fun categoryName(category: GeoCategory): Int = when (category) {
    GeoCategory.REGIONS -> R.string.category_regions
    GeoCategory.CITIES -> R.string.category_cities
    GeoCategory.RIVERS -> R.string.category_rivers
    GeoCategory.MOUNTAINS -> R.string.category_mountains
    GeoCategory.LAKES -> R.string.category_lakes
    GeoCategory.PARKS -> R.string.category_parks
}

@StringRes
private fun categoryDescription(category: GeoCategory): Int = when (category) {
    GeoCategory.REGIONS -> R.string.category_regions_sub
    GeoCategory.CITIES -> R.string.category_cities_sub
    GeoCategory.RIVERS -> R.string.category_rivers_sub
    GeoCategory.MOUNTAINS -> R.string.category_mountains_sub
    GeoCategory.LAKES -> R.string.category_lakes_sub
    GeoCategory.PARKS -> R.string.category_parks_sub
}
