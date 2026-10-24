package io.github.saksa84.chinamapquiz.ui.find

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.saksa84.chinamapquiz.R
import io.github.saksa84.chinamapquiz.data.GeoCatalog
import io.github.saksa84.chinamapquiz.game.QuizSummary
import io.github.saksa84.chinamapquiz.ui.common.QuizBackground
import io.github.saksa84.chinamapquiz.ui.common.QuizColors
import io.github.saksa84.chinamapquiz.ui.common.ScreenHeader
import io.github.saksa84.chinamapquiz.ui.common.formatDuration

/** Fin de partie : score, pourcentage, temps et liste des erreurs, avec « rejouer les erreurs ». */
@Composable
fun ResultScreen(
    summary: QuizSummary,
    catalog: GeoCatalog,
    onReplayMistakes: () -> Unit,
    onReplay: () -> Unit,
    onBack: () -> Unit,
) {
    fun name(id: String?) = id?.let { catalog[it]?.name } ?: "?"

    QuizBackground {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(stringResource(R.string.result_title), onBack)
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 16.dp)) {
                        Text(stringResource(R.string.result_score, summary.correct, summary.total), color = QuizColors.OnDark, fontSize = 22.sp)
                        Text(stringResource(R.string.result_percent, summary.percent), color = QuizColors.OnDark, fontSize = 22.sp)
                        Text(stringResource(R.string.result_time, formatDuration(summary.durationMs)), color = QuizColors.OnDark, fontSize = 22.sp)
                    }
                }
                item {
                    Text(
                        stringResource(if (summary.mistakes.isEmpty()) R.string.result_no_mistakes else R.string.result_mistakes_title),
                        color = if (summary.mistakes.isEmpty()) QuizColors.Correct else QuizColors.Accent,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                items(summary.mistakes) { mistake ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(QuizColors.Tile, RoundedCornerShape(4.dp))
                            .padding(12.dp),
                    ) {
                        Column {
                            Text(name(mistake.targetId), color = QuizColors.OnDark, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                            Text(stringResource(R.string.result_you_selected, name(mistake.selectedId)), color = QuizColors.Wrong)
                        }
                    }
                }
            }
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (summary.mistakes.isNotEmpty()) {
                    ResultButton(stringResource(R.string.result_replay_mistakes, summary.mistakes.size), onReplayMistakes)
                }
                ResultButton(stringResource(R.string.result_replay), onReplay)
            }
        }
    }
}

@Composable
private fun ResultButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(containerColor = QuizColors.Tile, contentColor = QuizColors.OnDark),
        modifier = Modifier.fillMaxWidth().height(52.dp),
    ) { Text(text, fontSize = 18.sp) }
}
