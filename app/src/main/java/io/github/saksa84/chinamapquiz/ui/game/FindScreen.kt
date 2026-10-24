package io.github.saksa84.chinamapquiz.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.game.QuizSession
import io.github.saksa84.chinamapquiz.ui.GameUiState
import io.github.saksa84.chinamapquiz.ui.common.QuizColors
import io.github.saksa84.chinamapquiz.ui.map.ChinaMap
import io.github.saksa84.chinamapquiz.ui.map.ShapeStyle

/**
 * Mode Trouver : « Trouver : Sichuan ». Toucher sélectionne, Confirmer (ou toucher à nouveau
 * la même région) valide. Une seule chance ; ensuite la bonne réponse est montrée.
 */
@Composable
fun FindScreen(
    data: MapData,
    state: GameUiState,
    elapsedMs: () -> Long,
    onTap: (String?) -> Unit,
    onConfirm: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val shownMs = rememberElapsed(elapsedMs)
    val inLevel = remember(state.level) { state.level.targetIds.toSet() }
    val answer = state.lastAnswer.takeIf { state.phase == QuizSession.Phase.FEEDBACK }
    val asking = state.phase == QuizSession.Phase.ASKING

    GameLayout(
        topBar = {
            GameTopBar(
                shownMs,
                stringResource(R.string.find_points, state.score),
                stringResource(R.string.find_round, state.questionNumber, state.total),
                onBack,
            )
        },
        feedback = {
            when {
                answer == null -> FeedbackText(null, QuizColors.OnDark)
                answer.correct -> FeedbackText(stringResource(R.string.find_correct), QuizColors.Correct)
                else -> FeedbackText(
                    stringResource(R.string.find_wrong, answer.selectedId?.let { data.catalog[it]?.name } ?: "?"),
                    QuizColors.Wrong,
                )
            }
        },
        map = { modifier ->
            ChinaMap(
                geometry = data.geometry,
                styleOf = { id -> findStyle(id, state, inLevel, data) },
                selectedId = state.pendingId,
                onShapeTap = onTap,
                modifier = modifier,
                seaColor = QuizColors.Sea,
                borderColor = QuizColors.Border,
                selectedBorderColor = QuizColors.Accent,
            )
        },
        overlay = { if (answer != null) AnswerCard(data, answer.targetId) },
        bottom = {
            PromptText(stringResource(R.string.find_prompt, state.targetId?.let { data.catalog[it]?.name } ?: ""))
            val isLast = state.questionNumber >= state.total
            GameButton(
                stringResource(
                    when {
                        asking -> R.string.find_confirm
                        isLast -> R.string.find_results
                        else -> R.string.find_next
                    },
                ),
                onClick = if (asking) onConfirm else onNext,
                enabled = !asking || state.pendingId != null,
            )
            GameProgress(state.questionNumber - if (asking) 1 else 0, state.total)
        },
    )
}

/**
 * Couleurs de la carte en jeu : terres neutres (aucun indice), hors niveau grisées,
 * sélection en attente en jaune ; pendant le retour, bonne réponse en vert et erreur en rouge.
 */
private fun findStyle(id: String, state: GameUiState, inLevel: Set<String>, data: MapData): ShapeStyle {
    val disputed = data.catalog[id]?.claimedBy?.isNotEmpty() == true
    val answer = state.lastAnswer.takeIf { state.phase == QuizSession.Phase.FEEDBACK }
    val fill = when {
        answer != null && id == answer.targetId -> QuizColors.Correct
        answer != null && id == answer.selectedId -> QuizColors.Wrong
        id == state.pendingId -> QuizColors.Accent
        else -> baseLandColor(id, inLevel, disputed)
    }
    // Réticule (si le lieu est minuscule) sur la sélection en attente et, pendant le retour,
    // sur la bonne réponse et l'erreur. Pas de point grossi hors niveau : non sélectionnable.
    val emphasized = id == state.pendingId || (answer != null && (id == answer.targetId || id == answer.selectedId))
    return ShapeStyle(fill, dashedBorder = disputed, smallMarker = id in inLevel, emphasized = emphasized)
}

/** Couleur de repos : terre neutre dans le niveau (blanche pour les îlots disputés), grisée hors niveau. */
internal fun baseLandColor(id: String, inLevel: Set<String>, disputed: Boolean) = when {
    id in inLevel && disputed -> QuizColors.Highlight
    id in inLevel -> QuizColors.Land
    else -> QuizColors.LandOutOfLevel
}
