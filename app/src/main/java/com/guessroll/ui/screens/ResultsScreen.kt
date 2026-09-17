package com.guessroll.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guessroll.domain.game.LeaderboardEntry
import com.guessroll.ui.GuessRollViewModel
import com.guessroll.ui.components.AppBackground
import com.guessroll.ui.components.BannerTone
import com.guessroll.ui.components.EmptyStateCard
import com.guessroll.ui.components.GlassCard
import com.guessroll.ui.components.LifecycleEventRefreshEffect
import com.guessroll.ui.components.LifecyclePollingEffect
import com.guessroll.ui.components.PrimaryButton
import com.guessroll.ui.components.ResultsPodiumCard
import com.guessroll.ui.components.RevealBox
import com.guessroll.ui.components.ScreenColumn
import com.guessroll.ui.components.ScreenHeader
import com.guessroll.ui.components.SecondaryButton
import com.guessroll.ui.components.SectionTitle
import com.guessroll.ui.components.StatusBanner
import com.guessroll.ui.components.rememberGameFeedbackController
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Mint
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.PanelStrokeStrong
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary
import com.guessroll.ui.theme.WarmViolet

@Composable
fun ResultsScreen(
    viewModel: GuessRollViewModel,
    onBackHome: () -> Unit,
    onRematch: () -> Unit,
    onRematchStarted: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val results = state.results
    val feedback = rememberGameFeedbackController(
        soundEnabled = state.soundEnabled,
        hapticsEnabled = state.hapticsEnabled,
    )
    val roomChangeEvents = remember(results?.room?.id) {
        results?.room?.id?.let(viewModel::observeRoomChanges)
    }
    val resultsSyncKey = "${results?.room?.id}:${results?.session?.id}"
    LifecyclePollingEffect(
        key = resultsSyncKey,
        enabled = results != null && !state.isLoading && !state.isStartingRematch,
        intervalMillis = 3_000,
        onRefresh = { viewModel.refreshResultsOrOpenGame(onRematchStarted) },
    )
    LifecycleEventRefreshEffect(
        key = resultsSyncKey,
        enabled = results != null && !state.isLoading && !state.isStartingRematch,
        events = roomChangeEvents,
        onEvent = { viewModel.refreshResultsOrOpenGame(onRematchStarted) },
    )
    LaunchedEffect(results?.session?.id) {
        if (results != null) {
            feedback.resultsReveal()
        }
    }

    AppBackground {
        ScreenColumn(
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 26.dp),
            scrollable = true,
        ) {
            if (results == null) {
                EmptyStateCard(
                    title = "Результаты загружаются",
                    subtitle = "Если экран не обновится, вернись домой и создай новую комнату.",
                )
                Spacer(modifier = Modifier.height(16.dp))
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        color = Amber,
                    )
                }
                state.gameErrorMessage?.let {
                    Spacer(modifier = Modifier.height(12.dp))
                    StatusBanner(message = it)
                }
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = "На главный экран",
                    onClick = {
                        feedback.buttonTap()
                        onBackHome()
                    },
                )
                return@ScreenColumn
            }

            val winners = results.winners
            val isTie = winners.size > 1
            val homeWithFeedback = {
                feedback.buttonTap()
                onBackHome()
            }
            val rematchWithFeedback = {
                feedback.buttonTap()
                onRematch()
            }

            RevealBox(delayMillis = 20) {
                ScreenHeader(
                    eyebrow = if (results.isCurrentPlayerHost) "финал · хост" else "финал · игрок",
                    title = if (isTie) "Ничья" else "Победитель",
                    subtitle = winnerSubtitle(winners),
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            RevealBox(delayMillis = 95) {
                ResultsPodiumCard(
                    entries = results.leaderboard,
                    isTie = isTie,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            RevealBox(delayMillis = 185) {
                ResultsActions(
                    isHost = results.isCurrentPlayerHost,
                    isBusy = state.isLoading || state.isStartingRematch,
                    onRematch = rematchWithFeedback,
                    onBackHome = homeWithFeedback,
                )
            }

            state.gameErrorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBanner(message = it)
            }

            Spacer(modifier = Modifier.height(16.dp))
            RevealBox(delayMillis = 255) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                ) {
                    SectionTitle("Итоговый стол", "места и очки", color = WarmViolet)
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        results.leaderboard.forEachIndexed { index, entry ->
                            RevealBox(delayMillis = 280 + index * 35) {
                                LeaderboardRow(entry = entry)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun ResultsActions(
    isHost: Boolean,
    isBusy: Boolean,
    onRematch: () -> Unit,
    onBackHome: () -> Unit,
) {
    GlassCard(
        accent = if (isHost) Amber else WarmViolet,
        compact = true,
        dense = true,
    ) {
        SectionTitle(
            title = if (isHost) "Следующая партия" else "Реванш запускает хост",
            subtitle = if (isHost) {
                "та же комната, новые раунды"
            } else {
                "оставь экран открытым, игра начнется автоматически"
            },
            color = if (isHost) Amber else WarmViolet,
        )
        Spacer(modifier = Modifier.height(12.dp))
        if (isHost) {
            PrimaryButton(
                text = if (isBusy) "Готовим реванш..." else "Реванш",
                enabled = !isBusy,
                onClick = onRematch,
            )
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton(
                text = "Домой",
                enabled = !isBusy,
                onClick = onBackHome,
            )
        } else {
            StatusBanner(
                message = "Ждем, пока хост запустит реванш.",
                tone = BannerTone.Info,
            )
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton(
                text = "Домой",
                enabled = !isBusy,
                onClick = onBackHome,
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier,
) {
    val isWinner = entry.rank == 1
    val color = when {
        isWinner -> Amber
        entry.rank == 2 -> WarmViolet
        entry.rank == 3 -> Mint
        else -> TextMuted
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        color.copy(alpha = if (isWinner) 0.15f else 0.075f),
                        Panel.copy(alpha = 0.92f),
                        PanelDeep.copy(alpha = 0.98f),
                    ),
                ),
                RoundedCornerShape(24.dp),
            )
            .border(1.dp, color.copy(alpha = if (isWinner) 0.30f else 0.16f), RoundedCornerShape(24.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "Место ${entry.rank}. ${entry.player.nickname}. ${entry.player.score} ${scoreWord(entry.player.score)}"
            }
            .padding(horizontal = 15.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(color.copy(alpha = if (isWinner) 0.18f else 0.11f), RoundedCornerShape(16.dp))
                    .border(1.dp, color.copy(alpha = if (isWinner) 0.36f else 0.22f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "#${entry.rank}",
                    color = color,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.player.nickname,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = leaderboardSubtitle(entry),
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box(
            modifier = Modifier
                .background(PanelDeep.copy(alpha = if (isWinner) 0.70f else 0.52f), RoundedCornerShape(18.dp))
                .border(1.dp, PanelStrokeStrong.copy(alpha = if (isWinner) 0.36f else 0.20f), RoundedCornerShape(18.dp))
                .padding(horizontal = 11.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = entry.player.score.toString(),
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = scoreWord(entry.player.score),
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

private fun winnerSubtitle(winners: List<LeaderboardEntry>): String {
    return when {
        winners.isEmpty() -> "Пока нет очков."
        winners.size > 1 -> winners.joinToString(prefix = "Победители: ") { it.player.nickname }
        else -> "${winners.first().player.nickname} забирает финал."
    }
}

private fun leaderboardSubtitle(entry: LeaderboardEntry): String {
    return when {
        entry.isTied && entry.rank == 1 -> "делит первое место"
        entry.isTied -> "равные очки"
        entry.rank == 1 -> "победитель"
        else -> "место #${entry.rank}"
    }
}

private fun scoreWord(score: Int): String {
    val normalized = score % 100
    return when {
        normalized in 11..14 -> "очков"
        score % 10 == 1 -> "очко"
        score % 10 in 2..4 -> "очка"
        else -> "очков"
    }
}
