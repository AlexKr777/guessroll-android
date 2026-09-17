package com.guessroll.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.LocalGuessRollMotion
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.WarmViolet
import com.guessroll.ui.theme.TextPrimary
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun GamePhotoFrame(
    imageUrl: String?,
    imageKey: String? = imageUrl,
    modifier: Modifier = Modifier,
    errorContent: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(34.dp)
    val reducedMotion = LocalGuessRollMotion.current.reducedMotion
    val revealScale = remember { Animatable(if (imageUrl == null) 0.985f else 1f) }
    val revealAlpha = remember { Animatable(if (imageUrl == null) 0.92f else 1f) }

    LaunchedEffect(imageKey) {
        if (reducedMotion) {
            revealScale.snapTo(1f)
            revealAlpha.snapTo(1f)
            return@LaunchedEffect
        }

        if (imageUrl == null) {
            revealScale.snapTo(0.985f)
            revealAlpha.snapTo(0.92f)
        } else {
            revealScale.snapTo(0.975f)
            revealAlpha.snapTo(0.88f)
            coroutineScope {
                launch {
                    revealScale.animateTo(1f, animationSpec = tween(durationMillis = 260))
                }
                launch {
                    revealAlpha.animateTo(1f, animationSpec = tween(durationMillis = 220))
                }
            }
        }
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.80f)
            .graphicsLayer {
                scaleX = revealScale.value
                scaleY = revealScale.value
                alpha = revealAlpha.value
            }
            .shadow(
                elevation = 24.dp,
                shape = shape,
                ambientColor = Color(0xFF000000).copy(alpha = 0.42f),
                spotColor = Amber.copy(alpha = 0.10f),
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        WarmViolet.copy(alpha = 0.030f),
                        Sky.copy(alpha = 0.018f),
                        Panel.copy(alpha = 0.96f),
                        PanelDeep.copy(alpha = 0.99f),
                    ),
                ),
            )
            .border(1.dp, Sky.copy(alpha = 0.15f), shape),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl == null) {
            errorContent()
        } else {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = "Фото раунда",
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
                loading = {
                    CircularProgressIndicator(color = Amber)
                },
                error = {
                    errorContent()
                },
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Amber.copy(alpha = 0.022f),
                            WarmViolet.copy(alpha = 0.026f),
                            Color.Transparent,
                            PanelDeep.copy(alpha = 0.52f),
                        ),
                    ),
                ),
        )
        RoundRevealFrameAccent(
            modifier = Modifier
                .matchParentSize()
                .padding(8.dp),
            alpha = 0.92f,
        )
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(14.dp)
                .background(PanelDeep.copy(alpha = 0.78f), RoundedCornerShape(999.dp))
                .border(1.dp, Amber.copy(alpha = 0.20f), RoundedCornerShape(999.dp))
                .padding(horizontal = 11.dp, vertical = 7.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "кадр раунда",
                color = TextPrimary,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        RevealClueBadge(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(14.dp),
            width = 58.dp,
            height = 38.dp,
        )
    }
}
