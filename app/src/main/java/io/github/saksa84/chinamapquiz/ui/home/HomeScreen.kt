package io.github.saksa84.chinamapquiz.ui.home

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
import io.github.saksa84.chinamapquiz.ui.GameMode
import io.github.saksa84.chinamapquiz.ui.common.MenuTile
import io.github.saksa84.chinamapquiz.ui.common.QuizBackground
import io.github.saksa84.chinamapquiz.ui.common.ScreenHeader

/** Accueil : choix du mode. Les modes pas encore développés sont visibles mais grisés. */
@Composable
fun HomeScreen(onLearn: () -> Unit, onMode: (GameMode) -> Unit) {
    QuizBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.home_title), onBack = null)
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item { MenuTile(stringResource(R.string.mode_learn), stringResource(R.string.mode_learn_sub), onLearn) }
                item { MenuTile(stringResource(R.string.mode_find), stringResource(R.string.mode_find_sub), { onMode(GameMode.FIND) }) }
                item { MenuTile(stringResource(R.string.mode_eliminate), stringResource(R.string.mode_eliminate_sub), { onMode(GameMode.ELIMINATE) }) }
                item { MenuTile(stringResource(R.string.mode_type), stringResource(R.string.mode_type_sub), { onMode(GameMode.TYPE) }) }
                item { MenuTile(stringResource(R.string.mode_outlines), stringResource(R.string.mode_soon), {}, enabled = false) }
            }
        }
    }
}
