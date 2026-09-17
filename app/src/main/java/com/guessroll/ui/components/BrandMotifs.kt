package com.guessroll.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.TextPrimary
import com.guessroll.ui.theme.WarmViolet

@Composable
fun HomeRevealScene(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val motifAlpha = alpha.coerceIn(0f, 1f)
    Canvas(modifier = modifier.clearAndSetSemantics {}) {
        val min = minOf(size.width, size.height)
        val sceneWidth = minOf(size.width * 0.94f, size.height * 2.62f)
        val sceneHeight = size.height * 0.94f
        val sceneLeft = (size.width - sceneWidth) / 2f
        val sceneTop = size.height * 0.025f
        val strokeWidth = min * 0.013f
        val shadowOffset = min * 0.070f

        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Sky.copy(alpha = 0.24f * motifAlpha),
                    Sky.copy(alpha = 0.090f * motifAlpha),
                    Color.Transparent,
                ),
                center = Offset(sceneLeft + sceneWidth * 0.58f, sceneTop + sceneHeight * 0.48f),
                radius = sceneWidth * 0.56f,
            ),
            topLeft = Offset(sceneLeft + sceneWidth * 0.00f, sceneTop + sceneHeight * 0.08f),
            size = Size(sceneWidth, sceneHeight * 0.82f),
            cornerRadius = CornerRadius(min * 0.24f, min * 0.24f),
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Amber.copy(alpha = 0.13f * motifAlpha),
                    Color.Transparent,
                ),
                center = Offset(sceneLeft + sceneWidth * 0.77f, sceneTop + sceneHeight * 0.22f),
                radius = sceneWidth * 0.24f,
            ),
            radius = sceneWidth * 0.24f,
            center = Offset(sceneLeft + sceneWidth * 0.77f, sceneTop + sceneHeight * 0.22f),
        )
        drawRoundRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    PanelDeep.copy(alpha = 0.50f * motifAlpha),
                    Sky.copy(alpha = 0.10f * motifAlpha),
                    Color.Transparent,
                ),
            ),
            topLeft = Offset(sceneLeft + sceneWidth * 0.08f, sceneTop + sceneHeight * 0.82f),
            size = Size(sceneWidth * 0.82f, sceneHeight * 0.15f),
            cornerRadius = CornerRadius(min * 0.22f, min * 0.22f),
        )

        val leftTopLeft = Offset(sceneLeft + sceneWidth * 0.08f, sceneTop + sceneHeight * 0.34f)
        val leftSize = Size(sceneWidth * 0.34f, sceneHeight * 0.54f)
        rotate(degrees = -11f, pivot = Offset(leftTopLeft.x + leftSize.width * 0.52f, leftTopLeft.y + leftSize.height * 0.52f)) {
            drawRoundRect(
                color = PanelDeep.copy(alpha = 0.62f * motifAlpha),
                topLeft = Offset(leftTopLeft.x, leftTopLeft.y + shadowOffset),
                size = leftSize,
                cornerRadius = CornerRadius(min * 0.14f, min * 0.14f),
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Sky.copy(alpha = 0.16f * motifAlpha),
                        Panel.copy(alpha = 0.64f * motifAlpha),
                        PanelDeep.copy(alpha = 0.96f * motifAlpha),
                    ),
                    start = leftTopLeft,
                    end = Offset(leftTopLeft.x + leftSize.width, leftTopLeft.y + leftSize.height),
                ),
                topLeft = leftTopLeft,
                size = leftSize,
                cornerRadius = CornerRadius(min * 0.14f, min * 0.14f),
            )
            drawRoundRect(
                color = TextPrimary.copy(alpha = 0.15f * motifAlpha),
                topLeft = leftTopLeft,
                size = leftSize,
                cornerRadius = CornerRadius(min * 0.14f, min * 0.14f),
                style = Stroke(width = strokeWidth),
            )
            drawRoundRect(
                color = Sky.copy(alpha = 0.080f * motifAlpha),
                topLeft = Offset(leftTopLeft.x + leftSize.width * 0.16f, leftTopLeft.y + leftSize.height * 0.15f),
                size = Size(leftSize.width * 0.64f, leftSize.height * 0.16f),
                cornerRadius = CornerRadius(min * 0.08f, min * 0.08f),
            )
            drawLine(
                color = TextPrimary.copy(alpha = 0.12f * motifAlpha),
                start = Offset(leftTopLeft.x + leftSize.width * 0.18f, leftTopLeft.y + leftSize.height * 0.78f),
                end = Offset(leftTopLeft.x + leftSize.width * 0.72f, leftTopLeft.y + leftSize.height * 0.78f),
                strokeWidth = min * 0.010f,
                cap = StrokeCap.Round,
            )
        }

        val farTopLeft = Offset(sceneLeft + sceneWidth * 0.62f, sceneTop + sceneHeight * 0.38f)
        val farSize = Size(sceneWidth * 0.24f, sceneHeight * 0.42f)
        rotate(degrees = 15f, pivot = Offset(farTopLeft.x + farSize.width * 0.50f, farTopLeft.y + farSize.height * 0.50f)) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Sky.copy(alpha = 0.10f * motifAlpha),
                        Panel.copy(alpha = 0.58f * motifAlpha),
                        PanelDeep.copy(alpha = 0.88f * motifAlpha),
                    ),
                    start = farTopLeft,
                    end = Offset(farTopLeft.x + farSize.width, farTopLeft.y + farSize.height),
                ),
                topLeft = farTopLeft,
                size = farSize,
                cornerRadius = CornerRadius(min * 0.13f, min * 0.13f),
            )
            drawRoundRect(
                color = Sky.copy(alpha = 0.13f * motifAlpha),
                topLeft = farTopLeft,
                size = farSize,
                cornerRadius = CornerRadius(min * 0.13f, min * 0.13f),
                style = Stroke(width = strokeWidth * 0.85f),
            )
        }

        val rightTopLeft = Offset(sceneLeft + sceneWidth * 0.57f, sceneTop + sceneHeight * 0.15f)
        val rightSize = Size(sceneWidth * 0.32f, sceneHeight * 0.60f)
        rotate(degrees = 9f, pivot = Offset(rightTopLeft.x + rightSize.width * 0.50f, rightTopLeft.y + rightSize.height * 0.48f)) {
            drawRoundRect(
                color = PanelDeep.copy(alpha = 0.58f * motifAlpha),
                topLeft = Offset(rightTopLeft.x, rightTopLeft.y + shadowOffset),
                size = rightSize,
                cornerRadius = CornerRadius(min * 0.15f, min * 0.15f),
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Amber.copy(alpha = 0.060f * motifAlpha),
                        Sky.copy(alpha = 0.13f * motifAlpha),
                        Panel.copy(alpha = 0.66f * motifAlpha),
                        PanelDeep.copy(alpha = 0.98f * motifAlpha),
                    ),
                    start = rightTopLeft,
                    end = Offset(rightTopLeft.x + rightSize.width, rightTopLeft.y + rightSize.height),
                ),
                topLeft = rightTopLeft,
                size = rightSize,
                cornerRadius = CornerRadius(min * 0.15f, min * 0.15f),
            )
            drawRoundRect(
                color = Sky.copy(alpha = 0.22f * motifAlpha),
                topLeft = rightTopLeft,
                size = rightSize,
                cornerRadius = CornerRadius(min * 0.15f, min * 0.15f),
                style = Stroke(width = strokeWidth),
            )
            val corner = Path().apply {
                moveTo(rightTopLeft.x + rightSize.width * 0.68f, rightTopLeft.y)
                lineTo(rightTopLeft.x + rightSize.width, rightTopLeft.y)
                lineTo(rightTopLeft.x + rightSize.width, rightTopLeft.y + rightSize.height * 0.30f)
                close()
            }
            drawPath(
                path = corner,
                brush = Brush.linearGradient(
                    colors = listOf(
                        TextPrimary.copy(alpha = 0.10f * motifAlpha),
                        Sky.copy(alpha = 0.020f * motifAlpha),
                    ),
                    start = rightTopLeft,
                    end = Offset(rightTopLeft.x + rightSize.width, rightTopLeft.y + rightSize.height * 0.35f),
                ),
            )
        }

        val frontTopLeft = Offset(sceneLeft + sceneWidth * 0.24f, sceneTop + sceneHeight * 0.12f)
        val frontSize = Size(sceneWidth * 0.50f, sceneHeight * 0.72f)
        rotate(degrees = -2.5f, pivot = Offset(frontTopLeft.x + frontSize.width * 0.54f, frontTopLeft.y + frontSize.height * 0.54f)) {
            drawRoundRect(
                color = PanelDeep.copy(alpha = 0.70f * motifAlpha),
                topLeft = Offset(frontTopLeft.x, frontTopLeft.y + shadowOffset),
                size = frontSize,
                cornerRadius = CornerRadius(min * 0.17f, min * 0.17f),
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Sky.copy(alpha = 0.24f * motifAlpha),
                        Panel.copy(alpha = 0.86f * motifAlpha),
                        PanelDeep.copy(alpha = 0.99f * motifAlpha),
                    ),
                    start = Offset(frontTopLeft.x, frontTopLeft.y),
                    end = Offset(frontTopLeft.x + frontSize.width, frontTopLeft.y + frontSize.height),
                ),
                topLeft = frontTopLeft,
                size = frontSize,
                cornerRadius = CornerRadius(min * 0.17f, min * 0.17f),
            )
            drawRoundRect(
                color = TextPrimary.copy(alpha = 0.22f * motifAlpha),
                topLeft = frontTopLeft,
                size = frontSize,
                cornerRadius = CornerRadius(min * 0.17f, min * 0.17f),
                style = Stroke(width = strokeWidth * 1.10f),
            )

            val innerTopLeft = Offset(frontTopLeft.x + frontSize.width * 0.14f, frontTopLeft.y + frontSize.height * 0.16f)
            val innerSize = Size(frontSize.width * 0.72f, frontSize.height * 0.58f)
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Sky.copy(alpha = 0.18f * motifAlpha),
                        Panel.copy(alpha = 0.16f * motifAlpha),
                        PanelDeep.copy(alpha = 0.38f * motifAlpha),
                    ),
                    start = innerTopLeft,
                    end = Offset(innerTopLeft.x + innerSize.width, innerTopLeft.y + innerSize.height),
                ),
                topLeft = innerTopLeft,
                size = innerSize,
                cornerRadius = CornerRadius(min * 0.10f, min * 0.10f),
            )
            drawRoundRect(
                color = Sky.copy(alpha = 0.12f * motifAlpha),
                topLeft = innerTopLeft,
                size = innerSize,
                cornerRadius = CornerRadius(min * 0.10f, min * 0.10f),
                style = Stroke(width = strokeWidth * 0.80f),
            )
            drawRoundRect(
                color = PanelDeep.copy(alpha = 0.24f * motifAlpha),
                topLeft = Offset(innerTopLeft.x + innerSize.width * 0.10f, innerTopLeft.y + innerSize.height * 0.66f),
                size = Size(innerSize.width * 0.34f, innerSize.height * 0.16f),
                cornerRadius = CornerRadius(min * 0.06f, min * 0.06f),
            )
            drawRoundRect(
                color = Sky.copy(alpha = 0.09f * motifAlpha),
                topLeft = Offset(innerTopLeft.x + innerSize.width * 0.52f, innerTopLeft.y + innerSize.height * 0.18f),
                size = Size(innerSize.width * 0.30f, innerSize.height * 0.16f),
                cornerRadius = CornerRadius(min * 0.06f, min * 0.06f),
            )
            val revealStrip = Path().apply {
                moveTo(innerTopLeft.x + innerSize.width * 0.00f, innerTopLeft.y + innerSize.height * 0.78f)
                lineTo(innerTopLeft.x + innerSize.width * 0.14f, innerTopLeft.y + innerSize.height * 0.89f)
                lineTo(innerTopLeft.x + innerSize.width * 1.00f, innerTopLeft.y + innerSize.height * 0.30f)
                lineTo(innerTopLeft.x + innerSize.width * 0.88f, innerTopLeft.y + innerSize.height * 0.18f)
                close()
            }
            drawPath(
                path = revealStrip,
                brush = Brush.linearGradient(
                    colors = listOf(
                        TextPrimary.copy(alpha = 0.10f * motifAlpha),
                        TextPrimary.copy(alpha = 0.42f * motifAlpha),
                        Sky.copy(alpha = 0.24f * motifAlpha),
                    ),
                    start = Offset(innerTopLeft.x, innerTopLeft.y + innerSize.height),
                    end = Offset(innerTopLeft.x + innerSize.width, innerTopLeft.y),
                ),
            )
            drawLine(
                color = TextPrimary.copy(alpha = 0.70f * motifAlpha),
                start = Offset(innerTopLeft.x + innerSize.width * 0.04f, innerTopLeft.y + innerSize.height * 0.80f),
                end = Offset(innerTopLeft.x + innerSize.width * 0.96f, innerTopLeft.y + innerSize.height * 0.18f),
                strokeWidth = min * 0.024f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Sky.copy(alpha = 0.52f * motifAlpha),
                start = Offset(innerTopLeft.x + innerSize.width * 0.08f, innerTopLeft.y + innerSize.height * 0.87f),
                end = Offset(innerTopLeft.x + innerSize.width * 0.94f, innerTopLeft.y + innerSize.height * 0.30f),
                strokeWidth = min * 0.011f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Amber.copy(alpha = 0.42f * motifAlpha),
                start = Offset(innerTopLeft.x + innerSize.width * 0.58f, innerTopLeft.y + innerSize.height * 0.46f),
                end = Offset(innerTopLeft.x + innerSize.width * 0.90f, innerTopLeft.y + innerSize.height * 0.25f),
                strokeWidth = min * 0.008f,
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = Amber.copy(alpha = 0.18f * motifAlpha),
                radius = min * 0.072f,
                center = Offset(frontTopLeft.x + frontSize.width * 0.85f, frontTopLeft.y + frontSize.height * 0.18f),
            )
            drawCircle(
                color = PanelDeep.copy(alpha = 0.68f * motifAlpha),
                radius = min * 0.047f,
                center = Offset(frontTopLeft.x + frontSize.width * 0.85f, frontTopLeft.y + frontSize.height * 0.18f),
            )
            drawCircle(
                color = Amber.copy(alpha = 0.92f * motifAlpha),
                radius = min * 0.027f,
                center = Offset(frontTopLeft.x + frontSize.width * 0.85f, frontTopLeft.y + frontSize.height * 0.18f),
            )
        }

        drawCircle(
            color = Sky.copy(alpha = 0.18f * motifAlpha),
            radius = min * 0.018f,
            center = Offset(sceneLeft + sceneWidth * 0.15f, sceneTop + sceneHeight * 0.76f),
        )
    }
}

@Composable
fun IdentityRevealScene(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val motifAlpha = alpha.coerceIn(0f, 1f)
    Canvas(modifier = modifier.clearAndSetSemantics {}) {
        val min = minOf(size.width, size.height)
        val sceneWidth = minOf(size.width * 0.74f, size.height * 2.55f)
        val sceneHeight = size.height * 0.86f
        val sceneLeft = (size.width - sceneWidth) / 2f
        val sceneTop = size.height * 0.06f
        val strokeWidth = min * 0.016f

        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Sky.copy(alpha = 0.14f * motifAlpha),
                    Color.Transparent,
                ),
                center = Offset(sceneLeft + sceneWidth * 0.52f, sceneTop + sceneHeight * 0.55f),
                radius = sceneWidth * 0.44f,
            ),
            topLeft = Offset(sceneLeft + sceneWidth * 0.05f, sceneTop + sceneHeight * 0.20f),
            size = Size(sceneWidth * 0.90f, sceneHeight * 0.62f),
            cornerRadius = CornerRadius(min * 0.22f, min * 0.22f),
        )

        val backTopLeft = Offset(sceneLeft + sceneWidth * 0.13f, sceneTop + sceneHeight * 0.28f)
        val backSize = Size(sceneWidth * 0.30f, sceneHeight * 0.52f)
        rotate(degrees = -13f, pivot = Offset(backTopLeft.x + backSize.width * 0.52f, backTopLeft.y + backSize.height * 0.52f)) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Sky.copy(alpha = 0.11f * motifAlpha),
                        Panel.copy(alpha = 0.58f * motifAlpha),
                        PanelDeep.copy(alpha = 0.92f * motifAlpha),
                    ),
                    start = backTopLeft,
                    end = Offset(backTopLeft.x + backSize.width, backTopLeft.y + backSize.height),
                ),
                topLeft = backTopLeft,
                size = backSize,
                cornerRadius = CornerRadius(min * 0.13f, min * 0.13f),
            )
            drawRoundRect(
                color = Sky.copy(alpha = 0.15f * motifAlpha),
                topLeft = backTopLeft,
                size = backSize,
                cornerRadius = CornerRadius(min * 0.13f, min * 0.13f),
                style = Stroke(width = strokeWidth),
            )
        }

        val frontTopLeft = Offset(sceneLeft + sceneWidth * 0.33f, sceneTop + sceneHeight * 0.16f)
        val frontSize = Size(sceneWidth * 0.44f, sceneHeight * 0.66f)
        rotate(degrees = 3f, pivot = Offset(frontTopLeft.x + frontSize.width * 0.50f, frontTopLeft.y + frontSize.height * 0.52f)) {
            drawRoundRect(
                color = PanelDeep.copy(alpha = 0.50f * motifAlpha),
                topLeft = Offset(frontTopLeft.x, frontTopLeft.y + min * 0.050f),
                size = frontSize,
                cornerRadius = CornerRadius(min * 0.16f, min * 0.16f),
            )
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Sky.copy(alpha = 0.19f * motifAlpha),
                        Panel.copy(alpha = 0.76f * motifAlpha),
                        PanelDeep.copy(alpha = 0.98f * motifAlpha),
                    ),
                    start = frontTopLeft,
                    end = Offset(frontTopLeft.x + frontSize.width, frontTopLeft.y + frontSize.height),
                ),
                topLeft = frontTopLeft,
                size = frontSize,
                cornerRadius = CornerRadius(min * 0.16f, min * 0.16f),
            )
            drawRoundRect(
                color = TextPrimary.copy(alpha = 0.17f * motifAlpha),
                topLeft = frontTopLeft,
                size = frontSize,
                cornerRadius = CornerRadius(min * 0.16f, min * 0.16f),
                style = Stroke(width = strokeWidth),
            )

            val slotTopLeft = Offset(frontTopLeft.x + frontSize.width * 0.17f, frontTopLeft.y + frontSize.height * 0.22f)
            val slotSize = Size(frontSize.width * 0.66f, frontSize.height * 0.40f)
            drawRoundRect(
                color = Sky.copy(alpha = 0.090f * motifAlpha),
                topLeft = slotTopLeft,
                size = slotSize,
                cornerRadius = CornerRadius(min * 0.10f, min * 0.10f),
            )
            val reveal = Path().apply {
                moveTo(slotTopLeft.x + slotSize.width * 0.06f, slotTopLeft.y + slotSize.height * 0.82f)
                lineTo(slotTopLeft.x + slotSize.width * 0.20f, slotTopLeft.y + slotSize.height * 0.96f)
                lineTo(slotTopLeft.x + slotSize.width * 0.96f, slotTopLeft.y + slotSize.height * 0.34f)
                lineTo(slotTopLeft.x + slotSize.width * 0.84f, slotTopLeft.y + slotSize.height * 0.18f)
                close()
            }
            drawPath(
                path = reveal,
                brush = Brush.linearGradient(
                    colors = listOf(
                        TextPrimary.copy(alpha = 0.12f * motifAlpha),
                        Sky.copy(alpha = 0.28f * motifAlpha),
                    ),
                    start = Offset(slotTopLeft.x, slotTopLeft.y + slotSize.height),
                    end = Offset(slotTopLeft.x + slotSize.width, slotTopLeft.y),
                ),
            )
            drawLine(
                color = TextPrimary.copy(alpha = 0.52f * motifAlpha),
                start = Offset(slotTopLeft.x + slotSize.width * 0.10f, slotTopLeft.y + slotSize.height * 0.76f),
                end = Offset(slotTopLeft.x + slotSize.width * 0.90f, slotTopLeft.y + slotSize.height * 0.24f),
                strokeWidth = min * 0.020f,
                cap = StrokeCap.Round,
            )
        }

        drawRoundRect(
            color = PanelDeep.copy(alpha = 0.34f * motifAlpha),
            topLeft = Offset(sceneLeft + sceneWidth * 0.28f, sceneTop + sceneHeight * 0.82f),
            size = Size(sceneWidth * 0.46f, sceneHeight * 0.10f),
            cornerRadius = CornerRadius(min * 0.18f, min * 0.18f),
        )
        drawCircle(
            color = Amber.copy(alpha = 0.18f * motifAlpha),
            radius = min * 0.062f,
            center = Offset(sceneLeft + sceneWidth * 0.78f, sceneTop + sceneHeight * 0.27f),
        )
        drawCircle(
            color = Amber.copy(alpha = 0.82f * motifAlpha),
            radius = min * 0.026f,
            center = Offset(sceneLeft + sceneWidth * 0.78f, sceneTop + sceneHeight * 0.27f),
        )
    }
}

@Composable
fun RevealStackMotif(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
    showClueDot: Boolean = true,
) {
    val motifAlpha = alpha.coerceIn(0f, 1f)
    Canvas(modifier = modifier.clearAndSetSemantics {}) {
        val min = minOf(size.width, size.height)
        val backTopLeft = Offset(size.width * 0.07f, size.height * 0.24f)
        val backSize = Size(size.width * 0.47f, size.height * 0.64f)
        val frontTopLeft = Offset(size.width * 0.28f, size.height * 0.14f)
        val frontSize = Size(size.width * 0.48f, size.height * 0.66f)
        val backRadius = CornerRadius(min * 0.13f, min * 0.13f)
        val frontRadius = CornerRadius(min * 0.15f, min * 0.15f)
        val strokeWidth = min * 0.022f

        rotate(degrees = -7f, pivot = Offset(size.width * 0.28f, size.height * 0.48f)) {
            drawRoundRect(
                color = PanelDeep.copy(alpha = 0.62f * motifAlpha),
                topLeft = backTopLeft,
                size = backSize,
                cornerRadius = backRadius,
            )
            drawRoundRect(
                color = Sky.copy(alpha = 0.16f * motifAlpha),
                topLeft = backTopLeft,
                size = backSize,
                cornerRadius = backRadius,
                style = Stroke(width = strokeWidth),
            )
        }

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Sky.copy(alpha = 0.26f * motifAlpha),
                    Panel.copy(alpha = 0.58f * motifAlpha),
                    PanelDeep.copy(alpha = 0.74f * motifAlpha),
                ),
                start = frontTopLeft,
                end = Offset(frontTopLeft.x + frontSize.width, frontTopLeft.y + frontSize.height),
            ),
            topLeft = frontTopLeft,
            size = frontSize,
            cornerRadius = frontRadius,
        )
        drawRoundRect(
            color = TextPrimary.copy(alpha = 0.18f * motifAlpha),
            topLeft = frontTopLeft,
            size = frontSize,
            cornerRadius = frontRadius,
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = TextPrimary.copy(alpha = 0.46f * motifAlpha),
            start = Offset(size.width * 0.38f, size.height * 0.58f),
            end = Offset(size.width * 0.73f, size.height * 0.34f),
            strokeWidth = min * 0.045f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Sky.copy(alpha = 0.36f * motifAlpha),
            start = Offset(size.width * 0.41f, size.height * 0.72f),
            end = Offset(size.width * 0.62f, size.height * 0.72f),
            strokeWidth = min * 0.038f,
            cap = StrokeCap.Round,
        )
        if (showClueDot) {
            drawCircle(
                color = Amber.copy(alpha = 0.72f * motifAlpha),
                radius = min * 0.055f,
                center = Offset(size.width * 0.72f, size.height * 0.29f),
            )
        }
    }
}

@Composable
fun RevealClueBadge(
    modifier: Modifier = Modifier,
    width: Dp = 52.dp,
    height: Dp = 34.dp,
) {
    val shape = RoundedCornerShape(999.dp)
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .clearAndSetSemantics {}
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Amber.copy(alpha = 0.13f),
                        Panel.copy(alpha = 0.78f),
                        PanelDeep.copy(alpha = 0.96f),
                    ),
                ),
            )
            .border(1.dp, Amber.copy(alpha = 0.22f), shape),
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
        ) {
            val min = minOf(size.width, size.height)
            drawLine(
                color = TextPrimary.copy(alpha = 0.76f),
                start = Offset(size.width * 0.20f, size.height * 0.68f),
                end = Offset(size.width * 0.68f, size.height * 0.32f),
                strokeWidth = min * 0.17f,
                cap = StrokeCap.Round,
            )
            drawCircle(
                color = Amber.copy(alpha = 0.92f),
                radius = min * 0.13f,
                center = Offset(size.width * 0.74f, size.height * 0.29f),
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.035f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )
    }
}

@Composable
fun PhotoPrepAccent(
    modifier: Modifier = Modifier,
    width: Dp = 30.dp,
    height: Dp = 30.dp,
) {
    Canvas(
        modifier = modifier
            .width(width)
            .height(height)
            .clearAndSetSemantics {},
    ) {
        val min = minOf(size.width, size.height)
        val strokeWidth = min * 0.045f
        val backTopLeft = Offset(size.width * 0.14f, size.height * 0.30f)
        val backSize = Size(size.width * 0.42f, size.height * 0.54f)
        val frontTopLeft = Offset(size.width * 0.36f, size.height * 0.18f)
        val frontSize = Size(size.width * 0.45f, size.height * 0.60f)
        val backRadius = CornerRadius(min * 0.16f, min * 0.16f)
        val frontRadius = CornerRadius(min * 0.18f, min * 0.18f)

        rotate(degrees = -7f, pivot = Offset(size.width * 0.34f, size.height * 0.55f)) {
            drawRoundRect(
                color = Sky.copy(alpha = 0.13f),
                topLeft = backTopLeft,
                size = backSize,
                cornerRadius = backRadius,
            )
            drawRoundRect(
                color = TextPrimary.copy(alpha = 0.18f),
                topLeft = backTopLeft,
                size = backSize,
                cornerRadius = backRadius,
                style = Stroke(width = strokeWidth),
            )
        }
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Sky.copy(alpha = 0.20f),
                    Panel.copy(alpha = 0.76f),
                    PanelDeep.copy(alpha = 0.96f),
                ),
                start = frontTopLeft,
                end = Offset(frontTopLeft.x + frontSize.width, frontTopLeft.y + frontSize.height),
            ),
            topLeft = frontTopLeft,
            size = frontSize,
            cornerRadius = frontRadius,
        )
        drawRoundRect(
            color = TextPrimary.copy(alpha = 0.22f),
            topLeft = frontTopLeft,
            size = frontSize,
            cornerRadius = frontRadius,
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = TextPrimary.copy(alpha = 0.72f),
            start = Offset(size.width * 0.44f, size.height * 0.62f),
            end = Offset(size.width * 0.78f, size.height * 0.39f),
            strokeWidth = min * 0.075f,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = Amber.copy(alpha = 0.88f),
            radius = min * 0.075f,
            center = Offset(size.width * 0.79f, size.height * 0.24f),
        )
    }
}

@Composable
fun LobbyInvitePassMotif(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val motifAlpha = alpha.coerceIn(0f, 1f)
    Canvas(modifier = modifier.clearAndSetSemantics {}) {
        val min = minOf(size.width, size.height)
        val strokeWidth = min * 0.020f
        val ticketTopLeft = Offset(size.width * 0.020f, size.height * 0.080f)
        val ticketSize = Size(size.width * 0.960f, size.height * 0.840f)
        val ticketRadius = CornerRadius(min * 0.21f, min * 0.21f)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Amber.copy(alpha = 0.12f * motifAlpha),
                    WarmViolet.copy(alpha = 0.13f * motifAlpha),
                    Panel.copy(alpha = 0.66f * motifAlpha),
                    PanelDeep.copy(alpha = 0.88f * motifAlpha),
                ),
                start = ticketTopLeft,
                end = Offset(ticketTopLeft.x + ticketSize.width, ticketTopLeft.y + ticketSize.height),
            ),
            topLeft = ticketTopLeft,
            size = ticketSize,
            cornerRadius = ticketRadius,
        )
        drawRoundRect(
            color = TextPrimary.copy(alpha = 0.12f * motifAlpha),
            topLeft = ticketTopLeft,
            size = ticketSize,
            cornerRadius = ticketRadius,
            style = Stroke(width = strokeWidth),
        )
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Amber.copy(alpha = 0.16f * motifAlpha),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.20f, size.height * 0.18f),
                radius = size.width * 0.42f,
            ),
            topLeft = ticketTopLeft,
            size = ticketSize,
            cornerRadius = ticketRadius,
        )

        val seamX = ticketTopLeft.x + ticketSize.width * 0.78f
        drawLine(
            color = TextPrimary.copy(alpha = 0.13f * motifAlpha),
            start = Offset(seamX, ticketTopLeft.y + ticketSize.height * 0.18f),
            end = Offset(seamX, ticketTopLeft.y + ticketSize.height * 0.82f),
            strokeWidth = strokeWidth * 0.70f,
            cap = StrokeCap.Round,
        )
        repeat(5) { index ->
            val y = ticketTopLeft.y + ticketSize.height * (0.20f + index * 0.15f)
            drawCircle(
                color = PanelDeep.copy(alpha = 0.55f * motifAlpha),
                radius = min * 0.030f,
                center = Offset(seamX, y),
            )
            drawCircle(
                color = TextPrimary.copy(alpha = 0.10f * motifAlpha),
                radius = min * 0.017f,
                center = Offset(seamX, y),
            )
        }

        val route = Path().apply {
            moveTo(ticketTopLeft.x + ticketSize.width * 0.09f, ticketTopLeft.y + ticketSize.height * 0.68f)
            cubicTo(
                ticketTopLeft.x + ticketSize.width * 0.26f,
                ticketTopLeft.y + ticketSize.height * 0.22f,
                ticketTopLeft.x + ticketSize.width * 0.46f,
                ticketTopLeft.y + ticketSize.height * 0.84f,
                ticketTopLeft.x + ticketSize.width * 0.64f,
                ticketTopLeft.y + ticketSize.height * 0.42f,
            )
        }
        drawPath(
            path = route,
            color = Sky.copy(alpha = 0.23f * motifAlpha),
            style = Stroke(width = strokeWidth * 0.92f, cap = StrokeCap.Round),
        )
        drawCircle(
            color = Amber.copy(alpha = 0.76f * motifAlpha),
            radius = min * 0.052f,
            center = Offset(ticketTopLeft.x + ticketSize.width * 0.11f, ticketTopLeft.y + ticketSize.height * 0.66f),
        )
        drawCircle(
            color = WarmViolet.copy(alpha = 0.62f * motifAlpha),
            radius = min * 0.040f,
            center = Offset(ticketTopLeft.x + ticketSize.width * 0.64f, ticketTopLeft.y + ticketSize.height * 0.42f),
        )

        val qrTopLeft = Offset(ticketTopLeft.x + ticketSize.width * 0.835f, ticketTopLeft.y + ticketSize.height * 0.255f)
        val qrSize = Size(ticketSize.width * 0.095f, ticketSize.height * 0.46f)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    TextPrimary.copy(alpha = 0.13f * motifAlpha),
                    Sky.copy(alpha = 0.08f * motifAlpha),
                    PanelDeep.copy(alpha = 0.28f * motifAlpha),
                ),
                start = qrTopLeft,
                end = Offset(qrTopLeft.x + qrSize.width, qrTopLeft.y + qrSize.height),
            ),
            topLeft = qrTopLeft,
            size = qrSize,
            cornerRadius = CornerRadius(min * 0.055f, min * 0.055f),
        )
        val dot = min * 0.016f
        repeat(3) { row ->
            repeat(3) { column ->
                if ((row + column) != 2) {
                    drawRoundRect(
                        color = PanelDeep.copy(alpha = 0.72f * motifAlpha),
                        topLeft = Offset(
                            qrTopLeft.x + qrSize.width * (0.18f + column * 0.25f),
                            qrTopLeft.y + qrSize.height * (0.18f + row * 0.25f),
                        ),
                        size = Size(dot, dot),
                        cornerRadius = CornerRadius(dot * 0.45f, dot * 0.45f),
                    )
                }
            }
        }

        drawLine(
            color = TextPrimary.copy(alpha = 0.18f * motifAlpha),
            start = Offset(ticketTopLeft.x + ticketSize.width * 0.14f, ticketTopLeft.y + ticketSize.height * 0.22f),
            end = Offset(ticketTopLeft.x + ticketSize.width * 0.46f, ticketTopLeft.y + ticketSize.height * 0.22f),
            strokeWidth = strokeWidth * 0.72f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Amber.copy(alpha = 0.36f * motifAlpha),
            start = Offset(ticketTopLeft.x + ticketSize.width * 0.16f, ticketTopLeft.y + ticketSize.height * 0.30f),
            end = Offset(ticketTopLeft.x + ticketSize.width * 0.33f, ticketTopLeft.y + ticketSize.height * 0.30f),
            strokeWidth = strokeWidth * 0.62f,
            cap = StrokeCap.Round,
        )
    }
}

@Composable
fun PhotoPrepHiddenStackMotif(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val motifAlpha = alpha.coerceIn(0f, 1f)
    Canvas(modifier = modifier.clearAndSetSemantics {}) {
        val min = minOf(size.width, size.height)
        val strokeWidth = min * 0.026f
        val baseTopLeft = Offset(size.width * 0.035f, size.height * 0.16f)
        val baseSize = Size(size.width * 0.93f, size.height * 0.70f)

        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    WarmViolet.copy(alpha = 0.10f * motifAlpha),
                    Sky.copy(alpha = 0.07f * motifAlpha),
                    PanelDeep.copy(alpha = 0.68f * motifAlpha),
                ),
                start = baseTopLeft,
                end = Offset(baseTopLeft.x + baseSize.width, baseTopLeft.y + baseSize.height),
            ),
            topLeft = baseTopLeft,
            size = baseSize,
            cornerRadius = CornerRadius(min * 0.22f, min * 0.22f),
        )
        drawRoundRect(
            color = TextPrimary.copy(alpha = 0.08f * motifAlpha),
            topLeft = baseTopLeft,
            size = baseSize,
            cornerRadius = CornerRadius(min * 0.22f, min * 0.22f),
            style = Stroke(width = strokeWidth * 0.64f),
        )

        val backCardTopLeft = Offset(size.width * 0.12f, size.height * 0.24f)
        val backCardSize = Size(size.width * 0.22f, size.height * 0.45f)
        rotate(degrees = -9f, pivot = Offset(backCardTopLeft.x + backCardSize.width * 0.52f, backCardTopLeft.y + backCardSize.height * 0.55f)) {
            drawRoundRect(
                color = Sky.copy(alpha = 0.12f * motifAlpha),
                topLeft = backCardTopLeft,
                size = backCardSize,
                cornerRadius = CornerRadius(min * 0.12f, min * 0.12f),
            )
            drawRoundRect(
                color = TextPrimary.copy(alpha = 0.12f * motifAlpha),
                topLeft = backCardTopLeft,
                size = backCardSize,
                cornerRadius = CornerRadius(min * 0.12f, min * 0.12f),
                style = Stroke(width = strokeWidth * 0.78f),
            )
        }

        val sideCardTopLeft = Offset(size.width * 0.27f, size.height * 0.18f)
        val sideCardSize = Size(size.width * 0.25f, size.height * 0.53f)
        rotate(degrees = 7f, pivot = Offset(sideCardTopLeft.x + sideCardSize.width * 0.48f, sideCardTopLeft.y + sideCardSize.height * 0.52f)) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WarmViolet.copy(alpha = 0.14f * motifAlpha),
                        Panel.copy(alpha = 0.56f * motifAlpha),
                        PanelDeep.copy(alpha = 0.84f * motifAlpha),
                    ),
                    start = sideCardTopLeft,
                    end = Offset(sideCardTopLeft.x + sideCardSize.width, sideCardTopLeft.y + sideCardSize.height),
                ),
                topLeft = sideCardTopLeft,
                size = sideCardSize,
                cornerRadius = CornerRadius(min * 0.13f, min * 0.13f),
            )
            drawRoundRect(
                color = WarmViolet.copy(alpha = 0.18f * motifAlpha),
                topLeft = sideCardTopLeft,
                size = sideCardSize,
                cornerRadius = CornerRadius(min * 0.13f, min * 0.13f),
                style = Stroke(width = strokeWidth * 0.78f),
            )
        }

        val frontTopLeft = Offset(size.width * 0.19f, size.height * 0.11f)
        val frontSize = Size(size.width * 0.30f, size.height * 0.62f)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Amber.copy(alpha = 0.08f * motifAlpha),
                    Sky.copy(alpha = 0.16f * motifAlpha),
                    Panel.copy(alpha = 0.70f * motifAlpha),
                    PanelDeep.copy(alpha = 0.94f * motifAlpha),
                ),
                start = frontTopLeft,
                end = Offset(frontTopLeft.x + frontSize.width, frontTopLeft.y + frontSize.height),
            ),
            topLeft = frontTopLeft,
            size = frontSize,
            cornerRadius = CornerRadius(min * 0.15f, min * 0.15f),
        )
        drawRoundRect(
            color = TextPrimary.copy(alpha = 0.18f * motifAlpha),
            topLeft = frontTopLeft,
            size = frontSize,
            cornerRadius = CornerRadius(min * 0.15f, min * 0.15f),
            style = Stroke(width = strokeWidth),
        )

        val maskedWindow = Offset(frontTopLeft.x + frontSize.width * 0.14f, frontTopLeft.y + frontSize.height * 0.16f)
        val maskedSize = Size(frontSize.width * 0.72f, frontSize.height * 0.50f)
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    PanelDeep.copy(alpha = 0.46f * motifAlpha),
                    WarmViolet.copy(alpha = 0.12f * motifAlpha),
                    Sky.copy(alpha = 0.11f * motifAlpha),
                ),
                start = maskedWindow,
                end = Offset(maskedWindow.x + maskedSize.width, maskedWindow.y + maskedSize.height),
            ),
            topLeft = maskedWindow,
            size = maskedSize,
            cornerRadius = CornerRadius(min * 0.09f, min * 0.09f),
        )
        val blindSlash = Path().apply {
            moveTo(maskedWindow.x + maskedSize.width * 0.02f, maskedWindow.y + maskedSize.height * 0.82f)
            lineTo(maskedWindow.x + maskedSize.width * 0.18f, maskedWindow.y + maskedSize.height * 0.98f)
            lineTo(maskedWindow.x + maskedSize.width * 0.98f, maskedWindow.y + maskedSize.height * 0.24f)
            lineTo(maskedWindow.x + maskedSize.width * 0.84f, maskedWindow.y + maskedSize.height * 0.06f)
            close()
        }
        drawPath(
            path = blindSlash,
            brush = Brush.linearGradient(
                colors = listOf(
                    TextPrimary.copy(alpha = 0.10f * motifAlpha),
                    TextPrimary.copy(alpha = 0.32f * motifAlpha),
                    Amber.copy(alpha = 0.16f * motifAlpha),
                ),
                start = Offset(maskedWindow.x, maskedWindow.y + maskedSize.height),
                end = Offset(maskedWindow.x + maskedSize.width, maskedWindow.y),
            ),
        )
        drawLine(
            color = TextPrimary.copy(alpha = 0.52f * motifAlpha),
            start = Offset(maskedWindow.x + maskedSize.width * 0.05f, maskedWindow.y + maskedSize.height * 0.83f),
            end = Offset(maskedWindow.x + maskedSize.width * 0.95f, maskedWindow.y + maskedSize.height * 0.18f),
            strokeWidth = strokeWidth * 1.05f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Amber.copy(alpha = 0.32f * motifAlpha),
            start = Offset(maskedWindow.x + maskedSize.width * 0.55f, maskedWindow.y + maskedSize.height * 0.51f),
            end = Offset(maskedWindow.x + maskedSize.width * 0.92f, maskedWindow.y + maskedSize.height * 0.24f),
            strokeWidth = strokeWidth * 0.44f,
            cap = StrokeCap.Round,
        )

        val railY = size.height * 0.64f
        drawLine(
            color = TextPrimary.copy(alpha = 0.13f * motifAlpha),
            start = Offset(size.width * 0.57f, railY),
            end = Offset(size.width * 0.88f, railY),
            strokeWidth = strokeWidth * 0.72f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Sky.copy(alpha = 0.16f * motifAlpha),
            start = Offset(size.width * 0.61f, size.height * 0.47f),
            end = Offset(size.width * 0.88f, size.height * 0.47f),
            strokeWidth = strokeWidth * 0.54f,
            cap = StrokeCap.Round,
        )
        repeat(4) { index ->
            val cx = size.width * (0.60f + index * 0.088f)
            val isReadyNode = index < 2
            drawCircle(
                color = if (isReadyNode) Amber.copy(alpha = 0.70f * motifAlpha) else Sky.copy(alpha = 0.30f * motifAlpha),
                radius = min * if (isReadyNode) 0.042f else 0.031f,
                center = Offset(cx, railY),
            )
            drawCircle(
                color = PanelDeep.copy(alpha = 0.62f * motifAlpha),
                radius = min * if (isReadyNode) 0.023f else 0.017f,
                center = Offset(cx, railY),
            )
        }
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Amber.copy(alpha = 0.18f * motifAlpha),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.84f, size.height * 0.28f),
                radius = min * 0.52f,
            ),
            radius = min * 0.52f,
            center = Offset(size.width * 0.84f, size.height * 0.28f),
        )
        drawCircle(
            color = Amber.copy(alpha = 0.80f * motifAlpha),
            radius = min * 0.050f,
            center = Offset(size.width * 0.83f, size.height * 0.28f),
        )
        drawCircle(
            color = TextPrimary.copy(alpha = 0.42f * motifAlpha),
            radius = min * 0.016f,
            center = Offset(size.width * 0.83f, size.height * 0.28f),
        )
    }
}

@Composable
fun RoundRevealFrameAccent(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val motifAlpha = alpha.coerceIn(0f, 1f)
    Canvas(modifier = modifier.clearAndSetSemantics {}) {
        val min = minOf(size.width, size.height)
        val strokeWidth = min * 0.011f
        val cornerSize = Size(size.width * 0.18f, size.height * 0.18f)
        drawRoundRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    Sky.copy(alpha = 0.13f * motifAlpha),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.14f, size.height * 0.16f),
                radius = min * 0.22f,
            ),
            topLeft = Offset(size.width * 0.02f, size.height * 0.04f),
            size = cornerSize,
            cornerRadius = CornerRadius(min * 0.05f, min * 0.05f),
        )
        drawLine(
            color = Sky.copy(alpha = 0.20f * motifAlpha),
            start = Offset(size.width * 0.07f, size.height * 0.10f),
            end = Offset(size.width * 0.31f, size.height * 0.10f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Sky.copy(alpha = 0.20f * motifAlpha),
            start = Offset(size.width * 0.07f, size.height * 0.10f),
            end = Offset(size.width * 0.07f, size.height * 0.28f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = TextPrimary.copy(alpha = 0.42f * motifAlpha),
            start = Offset(size.width * 0.08f, size.height * 0.13f),
            end = Offset(size.width * 0.29f, size.height * 0.36f),
            strokeWidth = min * 0.017f,
            cap = StrokeCap.Round,
        )
        val bottomReveal = Path().apply {
            moveTo(size.width * 0.70f, size.height * 0.91f)
            lineTo(size.width * 0.90f, size.height * 0.91f)
            lineTo(size.width * 0.91f, size.height * 0.71f)
            lineTo(size.width * 0.85f, size.height * 0.77f)
            lineTo(size.width * 0.78f, size.height * 0.86f)
            close()
        }
        drawPath(
            path = bottomReveal,
            brush = Brush.linearGradient(
                colors = listOf(
                    PanelDeep.copy(alpha = 0.28f * motifAlpha),
                    Sky.copy(alpha = 0.12f * motifAlpha),
                    TextPrimary.copy(alpha = 0.10f * motifAlpha),
                ),
                start = Offset(size.width * 0.70f, size.height * 0.92f),
                end = Offset(size.width * 0.92f, size.height * 0.70f),
            ),
        )
        drawLine(
            color = Sky.copy(alpha = 0.24f * motifAlpha),
            start = Offset(size.width * 0.63f, size.height * 0.88f),
            end = Offset(size.width * 0.91f, size.height * 0.88f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = Sky.copy(alpha = 0.18f * motifAlpha),
            start = Offset(size.width * 0.90f, size.height * 0.70f),
            end = Offset(size.width * 0.90f, size.height * 0.88f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = Amber.copy(alpha = 0.76f * motifAlpha),
            radius = min * 0.019f,
            center = Offset(size.width * 0.88f, size.height * 0.84f),
        )
    }
}

@Composable
fun FinaleSealMotif(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val motifAlpha = alpha.coerceIn(0f, 1f)
    Canvas(modifier = modifier.clearAndSetSemantics {}) {
        val min = minOf(size.width, size.height)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    WarmViolet.copy(alpha = 0.10f * motifAlpha),
                    Color.Transparent,
                ),
                center = Offset(size.width * 0.62f, size.height * 0.46f),
                radius = min * 0.58f,
            ),
            radius = min * 0.58f,
            center = Offset(size.width * 0.62f, size.height * 0.46f),
        )
        val baseTopLeft = Offset(size.width * 0.09f, size.height * 0.42f)
        val baseSize = Size(size.width * 0.46f, size.height * 0.40f)
        val backTopLeft = Offset(size.width * 0.20f, size.height * 0.27f)
        val backSize = Size(size.width * 0.36f, size.height * 0.52f)
        val frontTopLeft = Offset(size.width * 0.48f, size.height * 0.13f)
        val frontSize = Size(size.width * 0.39f, size.height * 0.58f)
        val strokeWidth = min * 0.020f

        rotate(degrees = -16f, pivot = Offset(size.width * 0.32f, size.height * 0.62f)) {
            drawRoundRect(
                color = Sky.copy(alpha = 0.070f * motifAlpha),
                topLeft = baseTopLeft,
                size = baseSize,
                cornerRadius = CornerRadius(min * 0.12f, min * 0.12f),
            )
            drawRoundRect(
                color = TextPrimary.copy(alpha = 0.075f * motifAlpha),
                topLeft = baseTopLeft,
                size = baseSize,
                cornerRadius = CornerRadius(min * 0.12f, min * 0.12f),
                style = Stroke(width = strokeWidth),
            )
        }
        rotate(degrees = -9f, pivot = Offset(size.width * 0.34f, size.height * 0.50f)) {
            drawRoundRect(
                color = Sky.copy(alpha = 0.12f * motifAlpha),
                topLeft = backTopLeft,
                size = backSize,
                cornerRadius = CornerRadius(min * 0.13f, min * 0.13f),
            )
            drawRoundRect(
                color = TextPrimary.copy(alpha = 0.10f * motifAlpha),
                topLeft = backTopLeft,
                size = backSize,
                cornerRadius = CornerRadius(min * 0.13f, min * 0.13f),
                style = Stroke(width = strokeWidth),
            )
        }
        rotate(degrees = 6f, pivot = Offset(size.width * 0.66f, size.height * 0.42f)) {
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        WarmViolet.copy(alpha = 0.12f * motifAlpha),
                        Panel.copy(alpha = 0.62f * motifAlpha),
                        PanelDeep.copy(alpha = 0.90f * motifAlpha),
                    ),
                    start = frontTopLeft,
                    end = Offset(frontTopLeft.x + frontSize.width, frontTopLeft.y + frontSize.height),
                ),
                topLeft = frontTopLeft,
                size = frontSize,
                cornerRadius = CornerRadius(min * 0.14f, min * 0.14f),
            )
            drawRoundRect(
                color = WarmViolet.copy(alpha = 0.12f * motifAlpha),
                topLeft = frontTopLeft,
                size = frontSize,
                cornerRadius = CornerRadius(min * 0.14f, min * 0.14f),
                style = Stroke(width = strokeWidth),
            )
            val finalReveal = Path().apply {
                moveTo(frontTopLeft.x + frontSize.width * 0.14f, frontTopLeft.y + frontSize.height * 0.78f)
                lineTo(frontTopLeft.x + frontSize.width * 0.28f, frontTopLeft.y + frontSize.height * 0.94f)
                lineTo(frontTopLeft.x + frontSize.width * 0.94f, frontTopLeft.y + frontSize.height * 0.34f)
                lineTo(frontTopLeft.x + frontSize.width * 0.82f, frontTopLeft.y + frontSize.height * 0.20f)
                close()
            }
            drawPath(
                path = finalReveal,
                brush = Brush.linearGradient(
                    colors = listOf(
                        TextPrimary.copy(alpha = 0.11f * motifAlpha),
                        WarmViolet.copy(alpha = 0.14f * motifAlpha),
                    ),
                    start = Offset(frontTopLeft.x, frontTopLeft.y + frontSize.height),
                    end = Offset(frontTopLeft.x + frontSize.width, frontTopLeft.y),
                ),
            )
        }
        drawLine(
            color = TextPrimary.copy(alpha = 0.56f * motifAlpha),
            start = Offset(size.width * 0.55f, size.height * 0.64f),
            end = Offset(size.width * 0.81f, size.height * 0.38f),
            strokeWidth = min * 0.046f,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = WarmViolet.copy(alpha = 0.18f * motifAlpha),
            start = Offset(size.width * 0.58f, size.height * 0.70f),
            end = Offset(size.width * 0.80f, size.height * 0.48f),
            strokeWidth = min * 0.014f,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = Amber.copy(alpha = 0.70f * motifAlpha),
            radius = min * 0.040f,
            center = Offset(size.width * 0.83f, size.height * 0.21f),
        )
    }
}

@Composable
fun FinaleAccentMotif(
    modifier: Modifier = Modifier,
    alpha: Float = 1f,
) {
    val motifAlpha = alpha.coerceIn(0f, 1f)
    Canvas(modifier = modifier.clearAndSetSemantics {}) {
        val min = minOf(size.width, size.height)
        val strokeWidth = min * 0.022f
        val baseTopLeft = Offset(size.width * 0.08f, size.height * 0.50f)
        val baseSize = Size(size.width * 0.80f, size.height * 0.24f)
        val shadowTopLeft = Offset(size.width * 0.22f, size.height * 0.22f)
        val shadowSize = Size(size.width * 0.32f, size.height * 0.44f)
        val frontTopLeft = Offset(size.width * 0.46f, size.height * 0.14f)
        val frontSize = Size(size.width * 0.36f, size.height * 0.52f)

        drawRoundRect(
            color = Sky.copy(alpha = 0.055f * motifAlpha),
            topLeft = baseTopLeft,
            size = baseSize,
            cornerRadius = CornerRadius(min * 0.17f, min * 0.17f),
        )
        drawRoundRect(
            color = WarmViolet.copy(alpha = 0.11f * motifAlpha),
            topLeft = baseTopLeft,
            size = baseSize,
            cornerRadius = CornerRadius(min * 0.17f, min * 0.17f),
            style = Stroke(width = strokeWidth),
        )
        rotate(degrees = -8f, pivot = Offset(size.width * 0.38f, size.height * 0.44f)) {
            drawRoundRect(
                color = Sky.copy(alpha = 0.10f * motifAlpha),
                topLeft = shadowTopLeft,
                size = shadowSize,
                cornerRadius = CornerRadius(min * 0.12f, min * 0.12f),
            )
            drawRoundRect(
                color = TextPrimary.copy(alpha = 0.10f * motifAlpha),
                topLeft = shadowTopLeft,
                size = shadowSize,
                cornerRadius = CornerRadius(min * 0.12f, min * 0.12f),
                style = Stroke(width = strokeWidth),
            )
        }
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    WarmViolet.copy(alpha = 0.10f * motifAlpha),
                    Panel.copy(alpha = 0.50f * motifAlpha),
                    PanelDeep.copy(alpha = 0.82f * motifAlpha),
                ),
                start = frontTopLeft,
                end = Offset(frontTopLeft.x + frontSize.width, frontTopLeft.y + frontSize.height),
            ),
            topLeft = frontTopLeft,
            size = frontSize,
            cornerRadius = CornerRadius(min * 0.13f, min * 0.13f),
        )
        drawRoundRect(
            color = TextPrimary.copy(alpha = 0.16f * motifAlpha),
            topLeft = frontTopLeft,
            size = frontSize,
            cornerRadius = CornerRadius(min * 0.13f, min * 0.13f),
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = TextPrimary.copy(alpha = 0.44f * motifAlpha),
            start = Offset(size.width * 0.54f, size.height * 0.49f),
            end = Offset(size.width * 0.78f, size.height * 0.33f),
            strokeWidth = min * 0.041f,
            cap = StrokeCap.Round,
        )
        drawCircle(
            color = Amber.copy(alpha = 0.58f * motifAlpha),
            radius = min * 0.038f,
            center = Offset(size.width * 0.80f, size.height * 0.22f),
        )
    }
}
