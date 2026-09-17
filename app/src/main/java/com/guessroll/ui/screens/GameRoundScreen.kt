package com.guessroll.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guessroll.domain.game.GameRoundSnapshot
import com.guessroll.domain.game.Player
import com.guessroll.domain.game.RoundStatus
import com.guessroll.ui.GuessRollViewModel
import com.guessroll.ui.components.AppBackground
import com.guessroll.ui.components.EmptyStateCard
import com.guessroll.ui.components.GamePhotoFrame
import com.guessroll.ui.components.InlineUtilityButton
import com.guessroll.ui.components.LifecycleEventRefreshEffect
import com.guessroll.ui.components.LifecyclePollingEffect
import com.guessroll.ui.components.MotionContent
import com.guessroll.ui.components.PhotoPrepAccent
import com.guessroll.ui.components.ReactionQuickTray
import com.guessroll.ui.components.RoundReactionOverlay
import com.guessroll.ui.components.ScreenColumn
import com.guessroll.ui.components.SectionTitle
import com.guessroll.ui.components.StatusBanner
import com.guessroll.ui.components.TopActionButton
import com.guessroll.ui.components.UtilityButton
import com.guessroll.ui.components.feedbackMotion
import com.guessroll.ui.components.motionColor
import com.guessroll.ui.components.motionContentSize
import com.guessroll.ui.components.rememberGameFeedbackController
import com.guessroll.ui.components.tactilePress
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Danger
import com.guessroll.ui.theme.Mint
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.PanelStroke
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary
import com.guessroll.ui.theme.WarmViolet
import kotlinx.coroutines.delay

@Composable
fun GameRoundScreen(
    viewModel: GuessRollViewModel,
    onBackHome: () -> Unit,
    onFinished: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val game = state.game
    val feedback = rememberGameFeedbackController(
        soundEnabled = state.soundEnabled,
        hapticsEnabled = state.hapticsEnabled,
    )
    var isReactionTrayExpanded by rememberSaveable { mutableStateOf(false) }
    val canAutoRefreshGame = game != null &&
        !state.isLoading &&
        !state.isSubmittingGuess &&
        !state.isAdvancingRound
    val roomChangeEvents = remember(game?.room?.id) {
        game?.room?.id?.let(viewModel::observeRoomChanges)
    }
    val gameSyncKey = "${game?.room?.id}:${game?.currentRound?.id}"
    LifecyclePollingEffect(
        key = gameSyncKey,
        enabled = canAutoRefreshGame,
        intervalMillis = 2_500,
        onRefresh = { viewModel.refreshGameSilently(onFinished) },
    )
    LifecycleEventRefreshEffect(
        key = gameSyncKey,
        enabled = canAutoRefreshGame,
        events = roomChangeEvents,
        onEvent = { viewModel.refreshGameSilently(onFinished) },
    )

    AppBackground {
        ScreenColumn(
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 20.dp),
            scrollable = true,
        ) {
            if (game == null) {
                TopActionButton(text = "Домой", onClick = onBackHome)
                Spacer(modifier = Modifier.height(16.dp))
                EmptyRoundState()
                return@ScreenColumn
            }

            val round = game.currentRound
            if (round == null) {
                TopActionButton(text = "Домой", onClick = onBackHome)
                Spacer(modifier = Modifier.height(16.dp))
                MissingRoundState(
                    isLoading = state.isLoading,
                    onRefresh = { viewModel.refreshGame(onFinished) },
                )
                return@ScreenColumn
            }
            LaunchedEffect(round.id, round.status, game.isCurrentPlayerHost) {
                if (game.isCurrentPlayerHost && round.status == RoundStatus.Revealed) {
                    delay(850)
                    viewModel.advanceRound(onFinished)
                }
            }
            LaunchedEffect(round.id, round.status, game.currentPlayerGuess?.id) {
                val guess = game.currentPlayerGuess ?: return@LaunchedEffect
                if (game.isCurrentRoundRevealed) {
                    if (guess.isCorrect) feedback.success() else feedback.reject()
                }
            }

            RoundTopBar(
                roundNumber = round.roundNumber,
                totalRounds = game.totalRounds,
                onBackHome = onBackHome,
            )

            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth()) {
                GamePhotoFrame(
                    imageUrl = game.currentImageUrl,
                    imageKey = game.currentMedia?.id ?: round.id,
                    errorContent = { ImageStateText("Фото не загрузилось. Обнови раунд.") },
                )
                RoundReactionOverlay(
                    reactions = game.recentReactions,
                    players = game.players,
                    modifier = Modifier.matchParentSize(),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd,
            ) {
                ReactionQuickTray(
                    enabled = !state.isLoading && round.status == RoundStatus.Active,
                    expanded = isReactionTrayExpanded,
                    onExpandedChange = { isReactionTrayExpanded = it },
                    onReaction = { emoji ->
                        feedback.reactionSent()
                        viewModel.sendReaction(emoji)
                    },
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            AnswerOptions(
                game = game,
                isSubmitting = state.isSubmittingGuess,
                optimisticSelectedPlayerId = state.selectedGuessPlayerId,
                onGuess = { playerId ->
                    feedback.answerSelected()
                    viewModel.submitGuess(playerId)
                },
            )

            state.gameErrorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBanner(message = it)
            }

            if (state.isLoading || state.isAdvancingRound) {
                Spacer(modifier = Modifier.height(14.dp))
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Amber,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            RoundAction(
                game = game,
                isBusy = state.isLoading || state.isAdvancingRound,
                onRefresh = { viewModel.refreshGame(onFinished) },
                onNext = { viewModel.advanceRound(onFinished) },
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun RoundTopBar(
    roundNumber: Int,
    totalRounds: Int,
    onBackHome: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopActionButton(text = "Домой", onClick = onBackHome)
        Box(
            modifier = Modifier
                .background(Panel.copy(alpha = 0.74f), RoundedCornerShape(999.dp))
                .border(1.dp, Amber.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                .padding(horizontal = 11.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Раунд $roundNumber/$totalRounds",
                color = Amber,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun AnswerOptions(
    game: GameRoundSnapshot,
    isSubmitting: Boolean,
    optimisticSelectedPlayerId: String?,
    onGuess: (String) -> Unit,
) {
    val roundRevealed = game.isCurrentRoundRevealed
    Column(modifier = Modifier.fillMaxWidth()) {
        SectionTitle(
            title = "Кто на фото?",
            subtitle = when {
                optimisticSelectedPlayerId != null -> "выбор принят"
                game.currentPlayerGuess == null -> "один ответ на раунд"
                roundRevealed -> "кадр раскрыт"
                else -> "ответ принят · ${game.currentRoundGuessCount}/${game.eligiblePlayerCount}"
            },
            color = Amber,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            answerOrder(game.players, game.currentRound?.id.orEmpty()).forEachIndexed { index, player ->
                AnswerButton(
                    index = index,
                    player = player,
                    game = game,
                    optimisticSelected = optimisticSelectedPlayerId == player.id,
                    enabled = game.currentPlayerGuess == null &&
                        !isSubmitting &&
                        game.currentRound?.status == RoundStatus.Active,
                    onClick = { onGuess(player.id) },
                )
            }
        }
    }
}

@Composable
private fun AnswerButton(
    index: Int,
    player: Player,
    game: GameRoundSnapshot,
    optimisticSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val guess = game.currentPlayerGuess
    val isSelected = guess?.guessedPlayerId == player.id || optimisticSelected
    val isRevealed = game.isCurrentRoundRevealed
    val isCorrect = isRevealed && game.currentRound?.correctPlayerId == player.id
    val isResultEmphasized = (isRevealed && guess != null && (isSelected || isCorrect)) || optimisticSelected
    val isMuted = guess != null && !isSelected && !isCorrect
    val stateColor = when {
        guess == null && optimisticSelected -> Amber
        guess == null -> WarmViolet
        !isRevealed && isSelected -> Amber
        isCorrect -> Mint
        isSelected -> Danger
        else -> PanelStroke
    }
    val color = motionColor(targetValue = stateColor, label = "answerChoiceColor")
    val stateLabel = when {
        guess == null && optimisticSelected -> "принят"
        guess == null -> "выбрать"
        !isRevealed && isSelected -> "принят"
        isCorrect -> "верно"
        isSelected -> "мимо"
        else -> ""
    }
    val optionMark = ('A'.code + index).toChar().toString()
    val answerStateDescription = when {
        guess == null && optimisticSelected -> "Ответ принят"
        guess == null && enabled -> "Можно выбрать"
        guess == null -> "Недоступно"
        !isRevealed && isSelected -> "Ответ принят, правильный владелец пока скрыт"
        !isRevealed -> "Ответы еще скрыты"
        isCorrect -> "Правильный ответ"
        isSelected -> "Выбранный неверный ответ"
        else -> "Не выбран"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 66.dp)
            .tactilePress(
                interactionSource = interactionSource,
                enabled = enabled,
                pressedScale = 0.970f,
                pressedAlpha = 0.988f,
            )
            .feedbackMotion(
                active = isResultEmphasized,
                correct = isCorrect || optimisticSelected,
            )
            .motionContentSize()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        color.copy(
                            alpha = when {
                                guess == null && optimisticSelected -> 0.24f
                                guess == null -> 0.12f
                                isResultEmphasized -> 0.30f
                                isMuted -> 0.045f
                                else -> 0.08f
                            },
                        ),
                        Panel.copy(alpha = if (isMuted) 0.72f else 0.92f),
                        PanelDeep.copy(alpha = if (isMuted) 0.92f else 0.98f),
                    ),
                ),
                RoundedCornerShape(24.dp),
            )
            .border(
                1.dp,
                color.copy(
                    alpha = when {
                        guess == null && optimisticSelected -> 0.38f
                        guess == null -> 0.26f
                        isResultEmphasized -> 0.44f
                        isMuted -> 0.08f
                        else -> 0.14f
                    },
                ),
                RoundedCornerShape(24.dp),
            )
            .semantics {
                role = Role.Button
                contentDescription = "Вариант $optionMark, ${player.nickname}"
                stateDescription = answerStateDescription
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 17.dp, vertical = 15.dp),
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
                    .size(36.dp)
                    .background(
                        color.copy(alpha = if (isMuted) 0.08f else if (guess == null) 0.18f else 0.26f),
                        RoundedCornerShape(14.dp),
                    )
                    .border(
                        1.dp,
                        color.copy(alpha = if (isMuted) 0.12f else if (guess == null) 0.26f else 0.42f),
                        RoundedCornerShape(14.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = optionMark,
                    color = if (isMuted) TextMuted else TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.nickname,
                    color = if (isMuted) TextMuted else TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = when {
                        guess == null && optimisticSelected -> "ответ принят"
                        guess == null -> "выбрать как владельца"
                        !isRevealed && isSelected -> "остальные еще голосуют"
                        !isRevealed -> "ответ пока скрыт"
                        isCorrect -> "правильный владелец"
                        isSelected -> "твой выбор"
                        else -> "не выбран"
                    },
                    color = if (isMuted) TextMuted else if (guess == null) TextMuted else color,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        MotionContent(targetState = stateLabel, label = "answerStateLabel") { label ->
            Text(
                text = label,
                color = if (guess == null) TextMuted else color,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun RoundAction(
    game: GameRoundSnapshot,
    isBusy: Boolean,
    onRefresh: () -> Unit,
    onNext: () -> Unit,
) {
    val hasAnswered = game.currentPlayerGuess != null
    val actionState = when {
        game.currentRound?.status == RoundStatus.Revealed && game.isCurrentPlayerHost -> "next"
        !hasAnswered -> "refresh"
        else -> "quiet"
    }
    MotionContent(targetState = actionState, label = "roundActionState") { state ->
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd,
        ) {
            when (state) {
                "refresh" -> InlineUtilityButton(
                    text = "Обновить",
                    enabled = !isBusy,
                    onClick = onRefresh,
                )
                "next" -> InlineUtilityButton(
                    text = "Ускорить",
                    enabled = !isBusy,
                    onClick = onNext,
                )
            }
        }
    }
}

@Composable
private fun EmptyRoundState() {
    EmptyStateCard(
        title = "Раунд не открыт",
        subtitle = "Вернись в лобби и начни игру как хост.",
    )
}

@Composable
private fun MissingRoundState(
    isLoading: Boolean,
    onRefresh: () -> Unit,
) {
    EmptyStateCard(
        title = "Раунд не найден",
        subtitle = "Обнови игру или вернись на главный экран.",
    )
    Spacer(modifier = Modifier.height(12.dp))
    UtilityButton(
        text = "Обновить",
        enabled = !isLoading,
        onClick = onRefresh,
    )
}

@Composable
private fun ImageStateText(text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(20.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(WarmViolet.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
                .border(1.dp, WarmViolet.copy(alpha = 0.20f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            PhotoPrepAccent(width = 32.dp, height = 32.dp)
        }
        Text(
            text = text,
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private fun answerOrder(players: List<Player>, roundId: String): List<Player> {
    return players.sortedBy { player -> (roundId + player.id).hashCode() }
}
