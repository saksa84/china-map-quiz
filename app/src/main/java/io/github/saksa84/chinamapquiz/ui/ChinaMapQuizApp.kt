package io.github.saksa84.chinamapquiz.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.ui.common.QuizBackground
import io.github.saksa84.chinamapquiz.ui.common.QuizColors
import io.github.saksa84.chinamapquiz.ui.game.EliminateScreen
import io.github.saksa84.chinamapquiz.ui.game.FindScreen
import io.github.saksa84.chinamapquiz.ui.game.LevelSelectScreen
import io.github.saksa84.chinamapquiz.ui.game.ResultScreen
import io.github.saksa84.chinamapquiz.ui.game.TypeScreen
import io.github.saksa84.chinamapquiz.ui.home.HomeScreen
import io.github.saksa84.chinamapquiz.ui.learn.LearnScreen

/**
 * Racine de l'application : affiche l'écran courant du [AppViewModel].
 * Navigation volontairement simple (une variable d'état) ; une bibliothèque de navigation viendra à l'étape 10 si besoin.
 */
@Composable
fun ChinaMapQuizApp(vm: AppViewModel = viewModel()) {
    val activity = LocalContext.current as? Activity
    // Bouton retour du téléphone : écran précédent, ou fermeture depuis l'accueil.
    BackHandler { if (!vm.back()) activity?.finish() }

    when (val load = vm.loadState) {
        LoadState.Loading -> QuizBackground {
            Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = QuizColors.OnDark)
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.loading), color = QuizColors.OnDark)
            }
        }
        is LoadState.Failed -> QuizBackground {
            Text(
                stringResource(R.string.loading_error, load.message),
                color = QuizColors.Wrong,
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
            )
        }
        is LoadState.Ready -> when (vm.screen) {
            Screen.HOME -> HomeScreen(onLearn = vm::openLearn, onMode = vm::chooseMode)
            Screen.LEARN -> LearnScreen(load.data, onBack = { vm.back() })
            Screen.LEVELS -> LevelSelectScreen(vm.mode, load.levels, onStart = vm::startLevel, onBack = { vm.back() })
            Screen.GAME -> vm.game?.let { state ->
                when (state.mode) {
                    GameMode.FIND -> FindScreen(
                        data = load.data,
                        state = state,
                        elapsedMs = vm::elapsedMs,
                        onTap = vm::tap,
                        onConfirm = vm::confirm,
                        onNext = vm::next,
                        onBack = { vm.back() },
                    )
                    GameMode.TYPE -> TypeScreen(
                        data = load.data,
                        state = state,
                        elapsedMs = vm::elapsedMs,
                        onSubmit = vm::submitTyped,
                        onSkip = vm::skip,
                        onNext = vm::next,
                        onBack = { vm.back() },
                    )
                    GameMode.ELIMINATE -> EliminateScreen(
                        data = load.data,
                        state = state,
                        elapsedMs = vm::elapsedMs,
                        onTap = vm::tap,
                        onBack = { vm.back() },
                    )
                }
                if (vm.confirmingQuit) QuitDialog(onQuit = vm::quitGame, onStay = vm::keepPlaying)
            }
            Screen.RESULT -> vm.summary?.let { summary ->
                ResultScreen(
                    mode = vm.mode,
                    summary = summary,
                    catalog = load.data.catalog,
                    onReplayMistakes = vm::replayMistakes,
                    onReplay = vm::replay,
                    onBack = { vm.back() },
                )
            }
        }
    }
}

@Composable
private fun QuitDialog(onQuit: () -> Unit, onStay: () -> Unit) {
    AlertDialog(
        onDismissRequest = onStay,
        title = { Text(stringResource(R.string.quit_title)) },
        text = { Text(stringResource(R.string.quit_message)) },
        confirmButton = { TextButton(onClick = onQuit) { Text(stringResource(R.string.quit_confirm)) } },
        dismissButton = { TextButton(onClick = onStay) { Text(stringResource(R.string.quit_stay)) } },
    )
}
