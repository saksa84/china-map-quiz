package io.github.saksa84.chinamapquiz.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.ui.GameUiState
import io.github.saksa84.chinamapquiz.ui.common.QuizColors
import io.github.saksa84.chinamapquiz.ui.map.ChinaMap
import io.github.saksa84.chinamapquiz.ui.map.ShapeStyle

/**
 * Mode Éliminer : une consigne à la fois, erreurs illimitées. Chaque région trouvée reste
 * colorée et n'est plus touchable : la carte se vide peu à peu.
 */
@Composable
fun EliminateScreen(
    data: MapData,
    state: GameUiState,
    elapsedMs: () -> Long,
    onTap: (String?) -> Unit,
    onBack: () -> Unit,
) {
    val shownMs = rememberElapsed(elapsedMs)
    val inLevel = remember(state.level) { state.level.targetIds.toSet() }

    GameLayout(
        topBar = {
            GameTopBar(
                shownMs,
                stringResource(R.string.eliminate_found, state.score, state.total),
                stringResource(R.string.eliminate_errors, state.errors),
                onBack,
            )
        },
        feedback = {
            val flash = state.flash
            when {
                flash == null -> FeedbackText(null, QuizColors.OnDark)
                flash.correct -> FeedbackText(stringResource(R.string.find_correct), QuizColors.Correct)
                else -> FeedbackText(
                    stringResource(R.string.eliminate_wrong, data.catalog[flash.id]?.name ?: "?"),
                    QuizColors.Wrong,
                )
            }
        },
        map = { modifier ->
            ChinaMap(
                geometry = data.geometry,
                styleOf = { id -> eliminateStyle(id, state, inLevel, data) },
                selectedId = null,
                onShapeTap = onTap,
                modifier = modifier,
                seaColor = QuizColors.Sea,
                borderColor = QuizColors.Border,
                selectedBorderColor = QuizColors.Accent,
            )
        },
        overlay = {},
        bottom = {
            PromptText(stringResource(R.string.find_prompt, state.targetId?.let { data.catalog[it]?.name } ?: ""))
            GameProgress(state.score, state.total)
        },
    )
}

/** Régions trouvées en vert sourd ; clignotement vif (vert ou rouge) sur le dernier toucher. */
private fun eliminateStyle(id: String, state: GameUiState, inLevel: Set<String>, data: MapData): ShapeStyle {
    val disputed = data.catalog[id]?.claimedBy?.isNotEmpty() == true
    val flash = state.flash?.takeIf { it.id == id }
    val fill = when {
        flash != null -> if (flash.correct) QuizColors.Correct else QuizColors.Wrong
        id in state.eliminated -> QuizColors.Eliminated
        else -> baseLandColor(id, inLevel, disputed)
    }
    // Les régions éliminées gardent leur point grossi (on voit qu'elles sont trouvées) ;
    // le réticule n'apparaît que sur le dernier toucher.
    return ShapeStyle(fill, dashedBorder = disputed, smallMarker = id in inLevel, emphasized = flash != null)
}
