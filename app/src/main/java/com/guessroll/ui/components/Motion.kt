package com.guessroll.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.guessroll.ui.theme.LocalGuessRollMotion
import kotlinx.coroutines.delay

private object GuessRollMotionTokens {
    const val Press = 100
    const val StateEnter = 190
    const val StateExit = 130
    const val Reveal = 240
    const val Size = 200
    const val ChoiceColor = 180
}

@Composable
fun Modifier.tactilePress(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    pressedScale: Float = 0.976f,
    pressedAlpha: Float = 0.985f,
): Modifier {
    val reducedMotion = LocalGuessRollMotion.current.reducedMotion
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (!reducedMotion && enabled && pressed) pressedScale else 1f,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else GuessRollMotionTokens.Press),
        label = "tactilePressScale",
    )
    val alpha by animateFloatAsState(
        targetValue = if (!reducedMotion && enabled && pressed) pressedAlpha else 1f,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else GuessRollMotionTokens.Press),
        label = "tactilePressAlpha",
    )
    return graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

@Composable
fun Modifier.motionContentSize(): Modifier {
    val reducedMotion = LocalGuessRollMotion.current.reducedMotion
    return animateContentSize(
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else GuessRollMotionTokens.Size),
    )
}

@Composable
fun motionColor(
    targetValue: Color,
    label: String,
): Color {
    val reducedMotion = LocalGuessRollMotion.current.reducedMotion
    val color by animateColorAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = if (reducedMotion) 0 else GuessRollMotionTokens.ChoiceColor),
        label = label,
    )
    return color
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun <T> MotionContent(
    targetState: T,
    modifier: Modifier = Modifier,
    label: String = "motionContent",
    content: @Composable (T) -> Unit,
) {
    val reducedMotion = LocalGuessRollMotion.current.reducedMotion
    if (reducedMotion) {
        Box(modifier = modifier) {
            content(targetState)
        }
        return
    }

    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        transitionSpec = {
            (
                fadeIn(animationSpec = tween(durationMillis = GuessRollMotionTokens.StateEnter)) +
                    scaleIn(
                        initialScale = 0.985f,
                        animationSpec = tween(durationMillis = GuessRollMotionTokens.StateEnter),
                    )
                ).togetherWith(
                fadeOut(animationSpec = tween(durationMillis = GuessRollMotionTokens.StateExit)) +
                    scaleOut(
                        targetScale = 0.995f,
                        animationSpec = tween(durationMillis = GuessRollMotionTokens.StateExit),
                    ),
            ).using(SizeTransform(clip = false))
        },
        label = label,
    ) { state ->
        content(state)
    }
}

@Composable
fun Modifier.feedbackMotion(
    active: Boolean,
    correct: Boolean,
): Modifier {
    val reducedMotion = LocalGuessRollMotion.current.reducedMotion
    val scale = remember { Animatable(1f) }
    val offset = remember { Animatable(0f) }

    LaunchedEffect(active, correct) {
        if (reducedMotion || !active) {
            scale.snapTo(1f)
            offset.snapTo(0f)
            return@LaunchedEffect
        }

        if (correct) {
            scale.snapTo(1f)
            scale.animateTo(1.018f, animationSpec = tween(durationMillis = 110))
            scale.animateTo(1f, animationSpec = tween(durationMillis = 140))
        } else {
            offset.snapTo(0f)
            offset.animateTo(-7f, animationSpec = tween(durationMillis = 45))
            offset.animateTo(6f, animationSpec = tween(durationMillis = 65))
            offset.animateTo(-3f, animationSpec = tween(durationMillis = 55))
            offset.animateTo(0f, animationSpec = tween(durationMillis = 70))
        }
    }

    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
        translationX = offset.value
    }
}

@Composable
fun RevealBox(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit,
) {
    val reducedMotion = LocalGuessRollMotion.current.reducedMotion
    if (reducedMotion) {
        Box(modifier = modifier) {
            content()
        }
        return
    }

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) {
            delay(delayMillis.toLong())
        }
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(durationMillis = GuessRollMotionTokens.Reveal)) +
            slideInVertically(
                animationSpec = tween(durationMillis = GuessRollMotionTokens.Reveal),
                initialOffsetY = { it / 12 },
            ),
    ) {
        Box(modifier = modifier) {
            content()
        }
    }
}
