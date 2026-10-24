package io.github.saksa84.chinamapquiz.ui.find

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.game.QuizSession
import io.github.saksa84.chinamapquiz.ui.FindUiState
import io.github.saksa84.chinamapquiz.ui.common.BackButton
import io.github.saksa84.chinamapquiz.ui.common.EntityInfo
import io.github.saksa84.chinamapquiz.ui.common.QuizBackground
import io.github.saksa84.chinamapquiz.ui.common.QuizColors
import io.github.saksa84.chinamapquiz.ui.common.formatDuration
import io.github.saksa84.chinamapquiz.ui.map.ChinaMap
import io.github.saksa84.chinamapquiz.ui.map.ShapeStyle
import kotlinx.coroutines.delay

/**
 * Mode Trouver : « Trouver : Sichuan ». Toucher sélectionne, Confirmer (ou toucher à nouveau
 * la même région) valide. Une seule chance ; ensuite la bonne réponse est montrée.
 */
@Composable
fun FindScreen(
    data: MapData,
    state: FindUiState,
    elapsedMs: () -> Long,
    onTap: (String?) -> Unit,
    onConfirm: () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    // Chronomètre : relu dix fois par seconde. Le moteur le fige lui-même après la dernière réponse.
    var shownMs by remember { mutableLongStateOf(elapsedMs()) }
    LaunchedEffect(Unit) {
        while (true) {
            shownMs = elapsedMs()
            delay(100)
        }
    }

    val inLevel = remember(state.level) { state.level.targetIds.toSet() }
    val styleOf = { id: String -> styleFor(id, state, inLevel, data) }

    QuizBackground {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val landscape = maxWidth > maxHeight
            val map = @Composable { modifier: Modifier ->
                ChinaMap(
                    geometry = data.geometry,
                    styleOf = styleOf,
                    selectedId = state.pendingId,
                    onShapeTap = onTap,
                    modifier = modifier,
                    seaColor = QuizColors.Sea,
                    borderColor = QuizColors.Border,
                    selectedBorderColor = QuizColors.Accent,
                )
            }
            // La carte garde toujours la même taille : le texte de retour a une hauteur réservée
            // et la fiche du lieu s'affiche par-dessus le haut de la carte, au-dessus de la mer,
            // pour ne jamais cacher une réponse au sud (Hainan, Hong Kong, Paracel).
            if (landscape) {
                Row(Modifier.fillMaxSize()) {
                    map(Modifier.weight(1f).fillMaxHeight())
                    Column(Modifier.width(360.dp).fillMaxHeight().padding(8.dp)) {
                        TopBar(state, shownMs, onBack)
                        Feedback(state, data)
                        Spacer(Modifier.weight(1f))
                        AnswerCard(state, data)
                        BottomBar(state, data, onConfirm, onNext)
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(horizontal = 8.dp)) {
                        TopBar(state, shownMs, onBack)
                        Feedback(state, data)
                    }
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        map(Modifier.fillMaxSize())
                        Column(Modifier.align(Alignment.TopCenter).padding(8.dp)) { AnswerCard(state, data) }
                    }
                    Column(Modifier.padding(8.dp)) { BottomBar(state, data, onConfirm, onNext) }
                }
            }
        }
    }
}

@Composable
private fun TopBar(state: FindUiState, shownMs: Long, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        BackButton(onBack)
        Text(formatDuration(shownMs), color = QuizColors.OnDark, fontSize = 24.sp, modifier = Modifier.padding(start = 12.dp))
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(stringResource(R.string.find_points, state.score), color = QuizColors.OnDark, fontSize = 20.sp)
            Text(stringResource(R.string.find_round, state.questionNumber, state.total), color = QuizColors.OnDark)
        }
    }
}

/** « Correct ! » ou « Faux ! Vous avez sélectionné : … », puis la fiche du lieu cherché. */
@Composable
private fun Feedback(state: FindUiState, data: MapData) {
    val answer = state.lastAnswer
    Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
        if (state.phase == QuizSession.Phase.FEEDBACK && answer != null) {
            val text = if (answer.correct) {
                stringResource(R.string.find_correct)
            } else {
                val selectedName = answer.selectedId?.let { data.catalog[it]?.name } ?: "?"
                stringResource(R.string.find_wrong, selectedName)
            }
            Text(
                text,
                color = if (answer.correct) QuizColors.Correct else QuizColors.Wrong,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** Pendant le retour, fiche du lieu qu'il fallait trouver. */
@Composable
private fun AnswerCard(state: FindUiState, data: MapData) {
    val answer = state.lastAnswer
    if (state.phase != QuizSession.Phase.FEEDBACK || answer == null) return
    val entity = data.catalog[answer.targetId] ?: return
    EntityInfo(
        data.catalog,
        entity,
        Modifier
            .fillMaxWidth()
            .background(QuizColors.Tile.copy(alpha = 0.9f), RoundedCornerShape(6.dp))
            .padding(12.dp),
        color = QuizColors.OnDark,
        showAliases = false,
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun ColumnScope.BottomBar(state: FindUiState, data: MapData, onConfirm: () -> Unit, onNext: () -> Unit) {
    val target = state.targetId?.let { data.catalog[it] }

    Text(
        stringResource(R.string.find_prompt, target?.name ?: ""),
        color = QuizColors.Accent,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(8.dp))

    val asking = state.phase == QuizSession.Phase.ASKING
    val isLast = state.questionNumber >= state.total
    Button(
        onClick = if (asking) onConfirm else onNext,
        enabled = !asking || state.pendingId != null,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = QuizColors.Tile, contentColor = QuizColors.OnDark),
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        Text(
            stringResource(
                when {
                    asking -> R.string.find_confirm
                    isLast -> R.string.find_results
                    else -> R.string.find_next
                },
            ),
            fontSize = 22.sp,
        )
    }
    Spacer(Modifier.height(8.dp))
    val answered = state.questionNumber - if (asking) 1 else 0
    LinearProgressIndicator(
        progress = { if (state.total == 0) 0f else answered.toFloat() / state.total },
        modifier = Modifier.fillMaxWidth(),
        color = QuizColors.OnDark,
        trackColor = QuizColors.Tile,
    )
}

/**
 * Couleurs de la carte en jeu : terres neutres (aucun indice), hors niveau grisées,
 * sélection en attente en jaune ; pendant le retour, bonne réponse en vert et erreur en rouge.
 */
private fun styleFor(id: String, state: FindUiState, inLevel: Set<String>, data: MapData): ShapeStyle {
    val disputed = data.catalog[id]?.claimedBy?.isNotEmpty() == true
    val answer = state.lastAnswer.takeIf { state.phase == QuizSession.Phase.FEEDBACK }
    val fill = when {
        answer != null && id == answer.targetId -> QuizColors.Correct
        answer != null && id == answer.selectedId -> QuizColors.Wrong
        id == state.pendingId -> QuizColors.Accent
        id in inLevel && disputed -> QuizColors.Highlight // îlots minuscules : contraste maximal
        id in inLevel -> QuizColors.Land
        else -> QuizColors.LandOutOfLevel
    }
    // Réticule (si le lieu est minuscule) sur la sélection en attente et, pendant le retour,
    // sur la bonne réponse et l'erreur. Pas de point grossi hors niveau : non sélectionnable.
    val emphasized = id == state.pendingId || (answer != null && (id == answer.targetId || id == answer.selectedId))
    return ShapeStyle(fill, dashedBorder = disputed, smallMarker = id in inLevel, emphasized = emphasized)
}
