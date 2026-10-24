package io.github.saksa84.chinamapquiz.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Palette des écrans de quiz : inspirée des applications de quiz géographique, pas copiée. */
object QuizColors {
    val BackgroundTop = Color(0xFF0E4F57)
    val BackgroundBottom = Color(0xFF138C93)
    val Tile = Color(0xFF0A3B41)
    val OnDark = Color(0xFFF2F5F5)
    val Muted = Color(0xFFB5CACC)
    val Accent = Color(0xFFFFC94D) // consigne, sélection en attente
    val Correct = Color(0xFF3CC56E)
    val Wrong = Color(0xFFE5534B)

    // Carte en mode jeu : terres grises neutres (aucun indice) et frontières blanches nettes.
    val Sea = Color(0xFF1D5E66)
    val Land = Color(0xFFC9CFCF)
    val Highlight = Color.White
    val LandOutOfLevel = Color(0xFF6C8487)
    val Border = Color.White
}

/** Fond dégradé, contenu tenu à l'écart des barres système. */
@Composable
fun QuizBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(QuizColors.BackgroundTop, QuizColors.BackgroundBottom)))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        content = content,
    )
}

/** Titre d'écran avec un bouton retour. */
@Composable
fun ScreenHeader(title: String, onBack: (() -> Unit)?) {
    Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) BackButton(onBack) else Spacer(Modifier.width(8.dp))
        Text(
            title,
            color = QuizColors.OnDark,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
}

@Composable
fun BackButton(onBack: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .background(QuizColors.Tile.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .clickable(onClick = onBack),
        contentAlignment = Alignment.Center,
    ) {
        Text("‹", color = QuizColors.OnDark, fontSize = 32.sp)
    }
}

/** Tuile carrée des menus (modes, niveaux). */
@Composable
fun MenuTile(
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Column(
        modifier
            .aspectRatio(1f)
            .alpha(if (enabled) 1f else 0.45f)
            .background(QuizColors.Tile, RoundedCornerShape(4.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(title, color = QuizColors.OnDark, fontSize = 22.sp, textAlign = TextAlign.Center, fontWeight = FontWeight.Medium)
        if (subtitle != null) {
            Text(subtitle, color = QuizColors.Muted, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }
    }
}

/** 83 456 ms -> « 1:23.4 ». */
fun formatDuration(ms: Long): String {
    val tenths = ms / 100
    return "%d:%02d.%d".format(tenths / 600, (tenths / 10) % 60, tenths % 10)
}
