package com.guessroll.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guessroll.domain.game.Player
import com.guessroll.domain.game.ReactionRules
import com.guessroll.domain.game.RoundReaction
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.LocalGuessRollMotion
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.WarmViolet
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary

@Composable
fun ReactionQuickTray(
    enabled: Boolean,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onReaction: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reducedMotion = LocalGuessRollMotion.current.reducedMotion
    val transitionDuration = if (reducedMotion) 0 else 150
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InlineUtilityButton(
            text = if (expanded) "Скрыть" else "Реакции",
            enabled = enabled,
            onClick = { onExpandedChange(!expanded) },
        )
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(animationSpec = tween(durationMillis = transitionDuration)) +
                scaleIn(initialScale = 0.98f, animationSpec = tween(durationMillis = transitionDuration)),
            exit = fadeOut(animationSpec = tween(durationMillis = transitionDuration)) +
                scaleOut(targetScale = 0.98f, animationSpec = tween(durationMillis = transitionDuration)),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ReactionRules.AllowedEmojis.forEach { emoji ->
                    ReactionChip(
                        emoji = emoji,
                        enabled = enabled,
                        onClick = { onReaction(emoji) },
                    )
                }
            }
        }
    }
}

@Composable
fun RoundReactionOverlay(
    reactions: List<RoundReaction>,
    players: List<Player>,
    modifier: Modifier = Modifier,
) {
    val visibleReactions = remember { mutableStateListOf<RoundReaction>() }
    val seenIds = remember { mutableSetOf<String>() }
    val latestIds = reactions.map { it.id }

    LaunchedEffect(latestIds) {
        reactions.forEach { reaction ->
            if (seenIds.add(reaction.id)) {
                visibleReactions.add(reaction)
                if (visibleReactions.size > ReactionRules.MaxVisibleReactions) {
                    visibleReactions.removeAt(0)
                }
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier.clearAndSetSemantics {},
    ) {
        ReactionRules.cappedActive(visibleReactions.toList()).forEach { reaction ->
            ReactionBubble(
                reaction = reaction,
                playerName = reaction.nickname(players),
                spec = ReactionRules.motionSpecFor(reaction),
                containerWidth = maxWidth,
                onFinished = { visibleReactions.removeAll { it.id == reaction.id } },
            )
        }
    }
}

@Composable
private fun ReactionChip(
    emoji: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(40.dp)
            .tactilePress(
                interactionSource = interactionSource,
                enabled = enabled,
                pressedScale = 0.94f,
                pressedAlpha = 0.96f,
            )
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (enabled) 0.052f else 0.022f),
                        Amber.copy(alpha = if (enabled) 0.048f else 0.018f),
                        WarmViolet.copy(alpha = if (enabled) 0.026f else 0.00f),
                        Panel.copy(alpha = if (enabled) 0.86f else 0.58f),
                        PanelDeep.copy(alpha = 0.96f),
                    ),
                ),
            )
            .border(BorderStroke(1.dp, Amber.copy(alpha = if (enabled) 0.22f else 0.09f)), RoundedCornerShape(16.dp))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .semantics {
                contentDescription = reactionContentDescription(emoji)
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = emoji,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun BoxScope.ReactionBubble(
    reaction: RoundReaction,
    playerName: String,
    spec: com.guessroll.domain.game.ReactionMotionSpec,
    containerWidth: androidx.compose.ui.unit.Dp,
    onFinished: () -> Unit,
) {
    val reducedMotion = LocalGuessRollMotion.current.reducedMotion
    val progress = remember(reaction.id) { Animatable(0f) }
    LaunchedEffect(reaction.id) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = if (reducedMotion) 760 else spec.lifetimeMillis,
                easing = if (reducedMotion) FastOutLinearInEasing else LinearOutSlowInEasing,
            ),
        )
        onFinished()
    }

    val value = progress.value
    val alpha = reactionAlpha(value, reducedMotion)
    val scale = reactionScale(value, reducedMotion)
    val horizontalDrift = if (reducedMotion) 0.dp else (spec.driftXDp * value).dp
    val verticalTravel = if (reducedMotion) 0.dp else (spec.travelYDp * value).dp
    val baseX = containerWidth * (spec.spawnXPercent / 100f)
    val baseY = spec.spawnYDp.dp + (spec.lane * 8).dp

    Row(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset(
                x = baseX + horizontalDrift,
                y = baseY + verticalTravel,
            )
            .graphicsLayer {
                this.alpha = alpha
                scaleX = scale
                scaleY = scale
                rotationZ = if (reducedMotion) 0f else spec.rotationDegrees * value
            }
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Amber.copy(alpha = 0.14f),
                        WarmViolet.copy(alpha = 0.046f),
                        Sky.copy(alpha = 0.046f),
                        Panel.copy(alpha = 0.92f),
                        PanelDeep.copy(alpha = 0.96f),
                    ),
                ),
                    RoundedCornerShape(999.dp),
            )
            .border(1.dp, Amber.copy(alpha = 0.20f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = reaction.emoji, style = MaterialTheme.typography.titleMedium)
        if (playerName.isNotBlank()) {
            Text(
                text = playerName,
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun reactionAlpha(progress: Float, reducedMotion: Boolean): Float {
    return when {
        progress < 0.12f -> (progress / 0.12f).coerceIn(0f, 1f)
        reducedMotion && progress > 0.70f -> ((1f - progress) / 0.30f).coerceIn(0f, 1f)
        !reducedMotion && progress > 0.74f -> ((1f - progress) / 0.26f).coerceIn(0f, 1f)
        else -> 1f
    }
}

private fun reactionScale(progress: Float, reducedMotion: Boolean): Float {
    if (reducedMotion) return 1f
    return when {
        progress < 0.16f -> lerpFloat(0.82f, 1.08f, progress / 0.16f)
        progress < 0.32f -> lerpFloat(1.08f, 1.00f, (progress - 0.16f) / 0.16f)
        else -> 1f
    }
}

private fun lerpFloat(start: Float, stop: Float, fraction: Float): Float {
    return start + (stop - start) * fraction.coerceIn(0f, 1f)
}

private fun reactionContentDescription(emoji: String): String {
    val name = when (emoji) {
        "😂" -> "смеётся"
        "😱" -> "шок"
        "💀" -> "жёстко"
        "👀" -> "смотрю"
        "🔥" -> "огонь"
        "🤯" -> "взрыв мозга"
        else -> emoji
    }
    return "Отправить реакцию $name"
}
