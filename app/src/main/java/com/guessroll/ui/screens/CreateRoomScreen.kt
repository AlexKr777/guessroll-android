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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guessroll.ui.GuessRollViewModel
import com.guessroll.ui.components.AppBackground
import com.guessroll.ui.components.GlassCard
import com.guessroll.ui.components.MotionContent
import com.guessroll.ui.components.PrimaryButton
import com.guessroll.ui.components.ScreenColumn
import com.guessroll.ui.components.ScreenHeader
import com.guessroll.ui.components.SectionTitle
import com.guessroll.ui.components.StatusBanner
import com.guessroll.ui.components.TopActionButton
import com.guessroll.ui.components.tactilePress
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.PanelStroke
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary
import com.guessroll.ui.theme.WarmViolet

@Composable
fun CreateRoomScreen(
    viewModel: GuessRollViewModel,
    onBack: () -> Unit,
    onRoomCreated: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    AppBackground {
        ScreenColumn(
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 22.dp),
            scrollable = true,
        ) {
            TopActionButton(text = "Назад", onClick = onBack)
            Spacer(modifier = Modifier.height(14.dp))
            ScreenHeader(
                title = "Настрой игру",
                subtitle = "Выбери формат и число раундов.",
            )

            Spacer(modifier = Modifier.height(18.dp))
            GlassCard(
                accent = WarmViolet,
                compact = true,
                featured = true,
                dense = true,
            ) {
                SectionTitle("Параметры игры", color = WarmViolet)
                Spacer(modifier = Modifier.height(12.dp))
                ModeRow()
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(PanelStroke.copy(alpha = 0.42f), RoundedCornerShape(99.dp)),
                )
                Spacer(modifier = Modifier.height(12.dp))
                SectionTitle("Раунды", "одно фото на раунд", color = WarmViolet)
                Spacer(modifier = Modifier.height(10.dp))
                RoundSelector(
                    value = state.selectedRoundCount,
                    canDecrease = state.selectedRoundCount > GuessRollViewModel.MinRounds,
                    canIncrease = state.selectedRoundCount < GuessRollViewModel.MaxRounds,
                    onDecrease = { viewModel.setRoundCount(state.selectedRoundCount - 1) },
                    onIncrease = { viewModel.setRoundCount(state.selectedRoundCount + 1) },
                )
            }

            Spacer(modifier = Modifier.height(18.dp))
            state.errorMessage?.let {
                StatusBanner(message = it)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Amber,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            PrimaryButton(
                text = "Создать комнату",
                enabled = !state.isLoading,
                onClick = { viewModel.createRoom(onRoomCreated) },
            )
        }
    }
}

@Composable
private fun ModeRow() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        ModeTile(
            title = "Фото",
            subtitle = "Сейчас",
            color = Amber,
            modifier = Modifier.weight(1f),
        )
        ModeTile(
            title = "Видео",
            subtitle = "Позже",
            color = WarmViolet,
            enabled = false,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ModeTile(
    title: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier = modifier
            .height(92.dp)
            .background(
                Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = if (enabled) 0.16f else 0.045f),
                        Panel.copy(alpha = if (enabled) 0.94f else 0.74f),
                        PanelDeep.copy(alpha = if (enabled) 0.98f else 0.88f),
                    ),
                ),
                RoundedCornerShape(26.dp),
            )
            .border(
                width = 1.dp,
                color = if (enabled) color.copy(alpha = 0.26f) else PanelStroke,
                shape = RoundedCornerShape(26.dp),
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "$title, $subtitle"
                if (!enabled) {
                    stateDescription = "Недоступно"
                }
            }
            .padding(14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .background(color.copy(alpha = if (enabled) 0.095f else 0.040f), RoundedCornerShape(999.dp))
                .border(1.dp, color.copy(alpha = if (enabled) 0.18f else 0.08f), RoundedCornerShape(999.dp))
                .padding(horizontal = 9.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (enabled) "сейчас" else "позже",
                color = if (enabled) color else TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(end = 54.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                color = if (enabled) TextPrimary else TextMuted,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                color = if (enabled) color else TextMuted,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun RoundSelector(
    value: Int,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RoundButton(text = "-", enabled = canDecrease, onClick = onDecrease)
        Box(
            modifier = Modifier
                .weight(1f)
                .height(72.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.026f),
                            WarmViolet.copy(alpha = 0.040f),
                            PanelDeep.copy(alpha = 0.98f),
                        ),
                    ),
                    RoundedCornerShape(28.dp),
                )
                .border(1.dp, PanelStroke, RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center,
        ) {
            MotionContent(targetState = value, label = "roundCount") { count ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = count.toString(),
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "раундов",
                        color = TextMuted,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        RoundButton(text = "+", enabled = canIncrease, onClick = onIncrease)
    }
}

@Composable
private fun RoundButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val color = if (enabled) WarmViolet else TextMuted
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 56.dp)
            .tactilePress(
                interactionSource = interactionSource,
                enabled = enabled,
                pressedScale = 0.970f,
                pressedAlpha = 0.990f,
            )
            .background(color.copy(alpha = if (enabled) 0.14f else 0.050f), RoundedCornerShape(22.dp))
            .border(1.dp, color.copy(alpha = if (enabled) 0.28f else 0.11f), RoundedCornerShape(22.dp))
            .semantics {
                role = Role.Button
                contentDescription = if (text == "-") "Уменьшить число раундов" else "Увеличить число раундов"
                if (!enabled) {
                    stateDescription = "Недоступно"
                }
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextMuted,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}
