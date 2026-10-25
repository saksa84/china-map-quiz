package io.github.saksa84.chinamapquiz.ui.game

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.ui.common.AutoSpeak
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.ui.GameUiState
import io.github.saksa84.chinamapquiz.ui.common.QuizColors
import io.github.saksa84.chinamapquiz.ui.map.ChinaMap
import io.github.saksa84.chinamapquiz.ui.map.ShapeStyle

/**
 * Mode Éliminer : une consigne à la fois, une seule chance. Comme dans Trouver, toucher sélectionne
 * et Confirmer (ou toucher à nouveau la même région) valide. Un lieu trouvé s'efface et n'est plus
 * touchable ; un lieu manqué reste sur la carte. À la fin, il ne reste que les lieux manqués.
 */
@Composable
fun EliminateScreen(
    data: MapData,
    state: GameUiState,
    elapsedMs: () -> Long,
    onTap: (String?) -> Unit,
    onConfirm: () -> Unit,
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
                    stringResource(R.string.eliminate_wrong, data.catalog[flash.id]?.displayName ?: "?"),
                    QuizColors.Wrong,
                )
            }
        },
        map = { modifier ->
            ChinaMap(
                geometry = data.geometry,
                styleOf = { id -> eliminateStyle(id, state, inLevel, data) },
                selectedId = state.pendingId,
                onShapeTap = onTap,
                modifier = modifier,
                seaColor = QuizColors.Sea,
                borderColor = QuizColors.Border,
                selectedBorderColor = QuizColors.Accent,
            )
        },
        overlay = {},
        bottom = {
            val target = state.targetId?.let { data.catalog[it] }
            AutoSpeak(target?.hanzi)
            PromptWithSpeech(stringResource(R.string.find_prompt, target?.displayName ?: ""), target?.hanzi)
            GameButton(stringResource(R.string.find_confirm), onConfirm, enabled = state.flash == null && state.pendingId != null)
            GameProgress(state.questionNumber - if (state.flash == null) 1 else 0, state.total)
        },
    )
}

/**
 * Lieux trouvés effacés (presque transparents, plus touchables) ; clignotement vif sur le dernier
 * toucher : vert si juste, rouge si faux, et le lieu demandé en vert après une erreur.
 */
private fun eliminateStyle(id: String, state: GameUiState, inLevel: Set<String>, data: MapData): ShapeStyle? {
    val flash = state.flash
    val touched = flash?.id == id
    val missedTarget = flash != null && !flash.correct && flash.targetId == id
    val stateColor = when {
        touched -> if (flash?.correct == true) QuizColors.Correct else QuizColors.Wrong
        missedTarget -> QuizColors.Correct
        id == state.pendingId -> QuizColors.Accent
        else -> null
    }
    val emphasized = touched || missedTarget || id == state.pendingId
    val style = gameStyle(id, data, inLevel, stateColor, emphasized) ?: return null
    if (id !in state.eliminated || touched) return style
    // Province trouvée : voile très léger sur la mer ; lieu posé (ville, fleuve, montagne) presque invisible.
    val faded = if (data.geometry[id]?.overlay == false) QuizColors.Eliminated else style.fill.copy(alpha = 0.18f)
    return style.copy(fill = faded, smallMarker = false, tappable = false)
}
