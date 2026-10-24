package io.github.saksa84.chinamapquiz.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.game.QuizSession
import io.github.saksa84.chinamapquiz.ui.GameUiState
import io.github.saksa84.chinamapquiz.ui.common.QuizColors
import io.github.saksa84.chinamapquiz.ui.map.ChinaMap
import io.github.saksa84.chinamapquiz.ui.map.ShapeStyle

/**
 * Mode Saisir : la région demandée est colorée sur la carte ; on tape son nom au clavier.
 * Une seule chance (ou « Passer ») ; ensuite le bon nom est affiché.
 */
@Composable
fun TypeScreen(
    data: MapData,
    state: GameUiState,
    elapsedMs: () -> Long,
    onSubmit: (String) -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    val shownMs = rememberElapsed(elapsedMs)
    val inLevel = remember(state.level) { state.level.targetIds.toSet() }
    val answer = state.lastAnswer.takeIf { state.phase == QuizSession.Phase.FEEDBACK }
    val asking = state.phase == QuizSession.Phase.ASKING
    // Texte remis à zéro à chaque question ; conservé si l'écran tourne.
    var typed by rememberSaveable(state.questionNumber) { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(state.questionNumber, asking) {
        if (asking) runCatching { focus.requestFocus() }
    }

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
            val targetName = answer?.targetId?.let { data.catalog[it]?.name }
            when {
                answer == null -> FeedbackText(null, QuizColors.OnDark)
                answer.correct -> FeedbackText(stringResource(R.string.type_correct, targetName ?: ""), QuizColors.Correct)
                answer.typed == null -> FeedbackText(stringResource(R.string.type_skipped, targetName ?: ""), QuizColors.Wrong)
                else -> FeedbackText(stringResource(R.string.type_wrong, targetName ?: "", answer.typed), QuizColors.Wrong)
            }
        },
        map = { modifier ->
            ChinaMap(
                geometry = data.geometry,
                styleOf = { id -> typeStyle(id, state, inLevel, data) },
                selectedId = null,
                onShapeTap = {},
                modifier = modifier,
                seaColor = QuizColors.Sea,
                borderColor = QuizColors.Border,
                selectedBorderColor = QuizColors.Accent,
            )
        },
        overlay = { if (answer != null) AnswerCard(data, answer.targetId) },
        bottom = {
            if (asking) {
                PromptText(stringResource(R.string.type_prompt))
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = typed,
                    onValueChange = { typed = it },
                    singleLine = true,
                    placeholder = { Text(stringResource(R.string.type_placeholder)) },
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp, color = QuizColors.OnDark),
                    // Pas de correcteur ni de suggestions du clavier : ils donneraient la réponse.
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        autoCorrectEnabled = false,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSubmit(typed) }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = QuizColors.Accent,
                        unfocusedBorderColor = QuizColors.Muted,
                        cursorColor = QuizColors.Accent,
                        focusedPlaceholderColor = QuizColors.Muted,
                        unfocusedPlaceholderColor = QuizColors.Muted,
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focus),
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GameButton(stringResource(R.string.type_skip), onSkip, Modifier.weight(1f))
                    GameButton(stringResource(R.string.type_validate), { onSubmit(typed) }, Modifier.weight(2f), enabled = typed.isNotBlank())
                }
            } else {
                val isLast = state.questionNumber >= state.total
                GameButton(stringResource(if (isLast) R.string.find_results else R.string.find_next), onNext)
            }
            GameProgress(state.questionNumber - if (asking) 1 else 0, state.total)
        },
    )
}

/** La région à nommer est en jaune ; pendant le retour, verte si juste, rouge sinon. */
private fun typeStyle(id: String, state: GameUiState, inLevel: Set<String>, data: MapData): ShapeStyle {
    val disputed = data.catalog[id]?.claimedBy?.isNotEmpty() == true
    val answer = state.lastAnswer.takeIf { state.phase == QuizSession.Phase.FEEDBACK }
    val isTarget = id == (answer?.targetId ?: state.targetId)
    val fill = when {
        isTarget && answer != null -> if (answer.correct) QuizColors.Correct else QuizColors.Wrong
        isTarget -> QuizColors.Accent
        else -> baseLandColor(id, inLevel, disputed)
    }
    return ShapeStyle(fill, dashedBorder = disputed, smallMarker = id in inLevel, emphasized = isTarget)
}
