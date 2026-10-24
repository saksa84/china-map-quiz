package io.github.saksa84.chinamapquiz.ui.game

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.saksa84.chinamapquiz.data.FeatureType
import io.github.saksa84.chinamapquiz.data.MapData
import io.github.saksa84.chinamapquiz.map.ShapeKind
import io.github.saksa84.chinamapquiz.ui.map.ShapeStyle
import io.github.saksa84.chinamapquiz.ui.common.BackButton
import io.github.saksa84.chinamapquiz.ui.common.EntityInfo
import io.github.saksa84.chinamapquiz.ui.common.QuizBackground
import io.github.saksa84.chinamapquiz.ui.common.QuizColors
import io.github.saksa84.chinamapquiz.ui.common.formatDuration
import kotlinx.coroutines.delay

/**
 * Mise en page commune aux modes de jeu. La carte garde toujours la même taille : le texte de
 * retour a une hauteur réservée et la fiche de réponse s'affiche par-dessus le haut de la carte,
 * au-dessus de la mer, pour ne jamais cacher une réponse au sud (Hainan, Hong Kong, Paracel).
 * En paysage, tout passe dans une colonne à droite de la carte.
 */
@Composable
fun GameLayout(
    topBar: @Composable () -> Unit,
    feedback: @Composable () -> Unit,
    map: @Composable (Modifier) -> Unit,
    overlay: @Composable ColumnScope.() -> Unit,
    bottom: @Composable ColumnScope.() -> Unit,
) {
    QuizBackground {
        // imePadding : en mode Saisir, le bas de l'écran remonte au-dessus du clavier.
        BoxWithConstraints(Modifier.fillMaxSize().imePadding()) {
            if (maxWidth > maxHeight) {
                Row(Modifier.fillMaxSize()) {
                    map(Modifier.weight(1f).fillMaxHeight())
                    Column(Modifier.width(360.dp).fillMaxHeight().padding(8.dp)) {
                        topBar()
                        feedback()
                        Spacer(Modifier.weight(1f))
                        overlay()
                        bottom()
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    Column(Modifier.padding(horizontal = 8.dp)) {
                        topBar()
                        feedback()
                    }
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        map(Modifier.fillMaxSize())
                        Column(Modifier.align(Alignment.TopCenter).padding(8.dp), content = overlay)
                    }
                    Column(Modifier.padding(8.dp), content = bottom)
                }
            }
        }
    }
}

/**
 * Apparence d'un lieu pendant un quiz, commune aux modes Trouver, Saisir et Éliminer.
 *
 * - Quiz sur les régions : les provinces du niveau sont neutres (blanches pour les îlots disputés),
 *   les autres grisées ; villes, cours d'eau et montagnes ne sont pas affichés.
 * - Quiz sur les villes, cours d'eau ou montagnes : les provinces forment un fond neutre et non
 *   touchable ; seuls les lieux du niveau sont affichés (les affluents disparaissent d'un quiz
 *   sur les grands fleuves, par exemple).
 *
 * @param state couleur d'état (sélection, bonne ou mauvaise réponse, éliminé) ; null au repos
 */
fun gameStyle(id: String, data: MapData, inLevel: Set<String>, state: Color?, emphasized: Boolean): ShapeStyle? {
    val shape = data.geometry[id] ?: return null
    val entity = data.catalog[id]
    val disputed = entity?.claimedBy?.isNotEmpty() == true
    val regionQuiz = inLevel.any { data.geometry[it]?.overlay == false }

    if (!shape.overlay) {
        if (!regionQuiz) return ShapeStyle(QuizColors.Land, dashedBorder = disputed, smallMarker = false, tappable = false)
        return ShapeStyle(
            fill = state ?: baseLandColor(id, inLevel, disputed),
            dashedBorder = disputed,
            smallMarker = id in inLevel,
            emphasized = emphasized,
            tappable = id in inLevel,
        )
    }
    if (id !in inLevel) return null
    val color = state ?: when (shape.kind) {
        ShapeKind.LINE -> QuizColors.River
        ShapeKind.POLYGON -> QuizColors.Range
        ShapeKind.POINT -> QuizColors.Point
    }
    // Zone de montagne translucide pour laisser voir les provinces dessous.
    val fill = if (shape.kind == ShapeKind.POLYGON) color.copy(alpha = if (state != null) 0.8f else 0.5f) else color
    val width = (if (entity?.type == FeatureType.MAJOR_RIVER) 3.5f else 2.2f) * (if (state != null) 1.5f else 1f)
    return ShapeStyle(fill, emphasized = emphasized, lineWidthDp = width)
}

/** Temps écoulé relu dix fois par seconde (le moteur le fige lui-même en fin de partie). */
@Composable
fun rememberElapsed(elapsedMs: () -> Long): Long {
    var shown by remember { mutableLongStateOf(elapsedMs()) }
    LaunchedEffect(Unit) {
        while (true) {
            shown = elapsedMs()
            delay(100)
        }
    }
    return shown
}

/** Bouton retour, chronomètre à gauche ; deux lignes d'indicateurs à droite (points, tour…). */
@Composable
fun GameTopBar(elapsedMs: Long, firstLine: String, secondLine: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        BackButton(onBack)
        Text(formatDuration(elapsedMs), color = QuizColors.OnDark, fontSize = 24.sp, modifier = Modifier.padding(start = 12.dp))
        Spacer(Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(firstLine, color = QuizColors.OnDark, fontSize = 20.sp)
            Text(secondLine, color = QuizColors.OnDark)
        }
    }
}

/** Zone de hauteur fixe pour « Correct ! » / « Faux ! … » (vide quand rien à dire). */
@Composable
fun FeedbackText(text: String?, color: Color) {
    Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
        if (text != null) {
            Text(text, color = color, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

/** Fiche du lieu qu'il fallait trouver, affichée pendant le retour. */
@Composable
fun AnswerCard(data: MapData, entityId: String?) {
    val entity = entityId?.let { data.catalog[it] } ?: return
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
fun PromptText(text: String) {
    Text(
        text,
        color = QuizColors.Accent,
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun GameButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier.fillMaxWidth(), enabled: Boolean = true) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = QuizColors.Tile,
            contentColor = QuizColors.OnDark,
            disabledContainerColor = QuizColors.Tile.copy(alpha = 0.4f),
            disabledContentColor = QuizColors.OnDark.copy(alpha = 0.4f),
        ),
        modifier = modifier.height(56.dp),
    ) { Text(text, fontSize = 22.sp) }
}

@Composable
fun GameProgress(done: Int, total: Int) {
    Spacer(Modifier.height(8.dp))
    LinearProgressIndicator(
        progress = { if (total == 0) 0f else done.toFloat() / total },
        modifier = Modifier.fillMaxWidth(),
        color = QuizColors.OnDark,
        trackColor = QuizColors.Tile,
    )
}
