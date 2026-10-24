package io.github.saksa84.chinamapquiz.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.game.QuizLevel
import io.github.saksa84.chinamapquiz.ui.GameMode
import io.github.saksa84.chinamapquiz.ui.common.MenuTile
import io.github.saksa84.chinamapquiz.ui.common.QuizBackground
import io.github.saksa84.chinamapquiz.ui.common.ScreenHeader

/** Choix du niveau : zone (Nord, Sud, Toute la Chine) × variante (continent, complet). Communs aux modes. */
@Composable
fun LevelSelectScreen(mode: GameMode, levels: List<QuizLevel>, onStart: (QuizLevel) -> Unit, onBack: () -> Unit) {
    QuizBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.levels_title, stringResource(modeName(mode))), onBack)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(levels, key = { it.id }) { level ->
                    val variant = stringResource(if (level.includesSpecial) R.string.variant_complete else R.string.variant_mainland)
                    val count = pluralStringResource(R.plurals.question_count, level.targetIds.size, level.targetIds.size)
                    MenuTile(level.zoneName, "$variant\n$count", onClick = { onStart(level) })
                }
            }
        }
    }
}

fun modeName(mode: GameMode): Int = when (mode) {
    GameMode.FIND -> R.string.mode_find
    GameMode.TYPE -> R.string.mode_type
    GameMode.ELIMINATE -> R.string.mode_eliminate
}
