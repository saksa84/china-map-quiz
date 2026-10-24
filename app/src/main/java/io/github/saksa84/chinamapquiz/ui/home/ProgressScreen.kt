package io.github.saksa84.chinamapquiz.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.GeoCatalog
import io.github.saksa84.chinamapquiz.game.GeoCategory
import io.github.saksa84.chinamapquiz.game.QuizLevel
import io.github.saksa84.chinamapquiz.progress.Progress
import io.github.saksa84.chinamapquiz.ui.GameMode
import io.github.saksa84.chinamapquiz.ui.common.QuizBackground
import io.github.saksa84.chinamapquiz.ui.common.QuizColors
import io.github.saksa84.chinamapquiz.ui.common.ScreenHeader
import io.github.saksa84.chinamapquiz.ui.common.formatDuration
import io.github.saksa84.chinamapquiz.ui.game.GameButton
import io.github.saksa84.chinamapquiz.ui.game.modeName

/** Combien de lieux « les plus manqués » afficher et proposer en révision. */
const val REVIEW_SIZE = 10

/**
 * Progression : pour chaque catégorie, pourcentage par mode et meilleurs résultats des niveaux
 * déjà joués ; puis les lieux les plus souvent manqués, toutes catégories confondues.
 */
@Composable
fun ProgressScreen(
    catalog: GeoCatalog,
    levels: Map<GeoCategory, List<QuizLevel>>,
    progress: Progress,
    onReview: () -> Unit,
    onBack: () -> Unit,
) {
    val missed = progress.mostMissed(REVIEW_SIZE).filter { catalog[it.first] != null }

    QuizBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.progress_title), onBack)
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item { Text(stringResource(R.string.progress_games, progress.gamesPlayed), color = QuizColors.Muted) }

                for (category in GeoCategory.entries) {
                    val categoryLevels = levels[category].orEmpty()
                    val sizes = categoryLevels.map { it.id to it.targetIds.size }
                    item(key = "title/${category.name}") { SectionTitle(stringResource(categoryName(category))) }
                    for (mode in listOf(GameMode.FIND, GameMode.ELIMINATE, GameMode.TYPE)) {
                        val key = category.progressKey(mode.name)
                        item(key = "mode/$key") {
                            val percent = (progress.completion(key, sizes) * 100).toInt()
                            Text("${stringResource(modeName(mode))} · $percent %", color = QuizColors.OnDark, fontWeight = FontWeight.SemiBold)
                        }
                        // Seuls les niveaux déjà joués : la liste reste courte.
                        val played = categoryLevels.mapNotNull { level -> progress.bestFor(key, level.id)?.let { level to it } }
                        items(played, key = { (level, _) -> "$key/${level.id}" }) { (level, best) ->
                            val variant = level.subtitle
                                ?: stringResource(if (level.includesSpecial) R.string.variant_complete else R.string.variant_mainland)
                            Row(
                                Modifier.fillMaxWidth().background(QuizColors.Tile, RoundedCornerShape(4.dp)).padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("${level.zoneName} · $variant", color = QuizColors.OnDark, modifier = Modifier.weight(1f))
                                Text(
                                    "${if (best.perfect) "★ " else ""}${best.correct} / ${best.total} · ${formatDuration(best.timeMs)}",
                                    color = if (best.perfect) QuizColors.Accent else QuizColors.OnDark,
                                )
                            }
                        }
                    }
                }

                item { SectionTitle(stringResource(R.string.progress_most_missed)) }
                if (missed.isEmpty()) {
                    item { Text(stringResource(R.string.progress_no_missed), color = QuizColors.Muted) }
                }
                items(missed, key = { "missed/${it.first}" }) { (id, count) ->
                    Row(
                        Modifier.fillMaxWidth().background(QuizColors.Tile, RoundedCornerShape(4.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(catalog[id]?.displayName ?: id, color = QuizColors.OnDark, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(stringResource(R.string.progress_missed_count, count), color = QuizColors.Wrong)
                    }
                }
            }
            if (missed.isNotEmpty()) {
                Column(Modifier.padding(16.dp)) { GameButton(stringResource(R.string.progress_review, missed.size), onReview) }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        color = QuizColors.Accent,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}
