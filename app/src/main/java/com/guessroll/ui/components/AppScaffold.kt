package com.guessroll.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.DeepBlue
import com.guessroll.ui.theme.GlowBlue
import com.guessroll.ui.theme.GlowPink
import com.guessroll.ui.theme.GlowViolet
import com.guessroll.ui.theme.Ink
import com.guessroll.ui.theme.InkRaised
import com.guessroll.ui.theme.LocalGuessRollMotion
import com.guessroll.ui.theme.Midnight
import com.guessroll.ui.theme.NightPlum
import com.guessroll.ui.theme.PanelDeep

val LocalGuessRollSky = staticCompositionLocalOf<Sky?> { null }

@Composable
fun GuessRollBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val sky = rememberSky()
    val reducedMotion = LocalGuessRollMotion.current.reducedMotion
    val slowDrift: Float
    val slowCounterDrift: Float
    if (reducedMotion) {
        slowDrift = 0f
        slowCounterDrift = 0f
    } else {
        val ambientMotion = rememberInfiniteTransition(label = "ambientDepth")
        slowDrift = ambientMotion.animateFloat(
            initialValue = -8f,
            targetValue = 8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 18000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "slowDrift",
        ).value
        slowCounterDrift = ambientMotion.animateFloat(
            initialValue = 6f,
            targetValue = -6f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 22000),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "slowCounterDrift",
        ).value
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        InkRaised,
                        Ink,
                        NightPlum,
                        DeepBlue,
                        Midnight,
                        Ink,
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .sky(sky),
        ) {
            AmbientAura(
                color = Amber,
                modifier = Modifier
                    .size(318.dp)
                    .offset(x = ((-216) + slowDrift).dp, y = (-90).dp),
                alpha = 0.032f,
            )
            AmbientAura(
                color = GlowPink,
                modifier = Modifier
                    .size(286.dp)
                    .offset(x = (214 + slowCounterDrift).dp, y = 104.dp),
                alpha = 0.014f,
            )
            AmbientAura(
                color = GlowBlue,
                modifier = Modifier
                    .size(250.dp)
                    .offset(x = ((-146) + slowCounterDrift).dp, y = 510.dp),
                alpha = 0.034f,
            )
            AmbientAura(
                color = GlowViolet,
                modifier = Modifier
                    .size(220.dp)
                    .offset(x = (236 + slowDrift).dp, y = 562.dp),
                alpha = 0.024f,
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            GlowBlue.copy(alpha = 0.015f),
                            Amber.copy(alpha = 0.012f),
                            GlowViolet.copy(alpha = 0.012f),
                            Color.Transparent,
                        ),
                            radius = 1040f,
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.008f),
                                Color.Transparent,
                                GlowBlue.copy(alpha = 0.006f),
                            ),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                PanelDeep.copy(alpha = 0.70f),
                                Ink.copy(alpha = 0.90f),
                            ),
                            startY = 720f,
                        ),
                    ),
            )
        }
        CompositionLocalProvider(LocalGuessRollSky provides sky) {
            content()
        }
    }
}

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    GuessRollBackground(modifier = modifier, content = content)
}

@Composable
private fun AmbientAura(
    color: Color,
    modifier: Modifier,
    alpha: Float,
) {
    Box(
        modifier = modifier
            .blur(72.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = alpha),
                        color.copy(alpha = alpha * 0.30f),
                        Color.Transparent,
                    ),
                ),
            ),
    )
}

@Composable
fun ScreenColumn(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 22.dp),
    scrollable: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollModifier = if (scrollable) {
        Modifier.verticalScroll(rememberScrollState())
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .then(scrollModifier)
            .padding(contentPadding)
            .padding(top = 6.dp, bottom = 18.dp),
    ) {
        content()
    }
}
