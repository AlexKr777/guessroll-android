package com.guessroll.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.GlassSoft
import com.guessroll.ui.theme.GlowBlue
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.PanelElevated
import com.guessroll.ui.theme.PanelStroke
import com.guessroll.ui.theme.PanelStrokeStrong
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.TextDisabled
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary
import com.guessroll.ui.theme.Violet
import com.guessroll.ui.theme.WarmViolet
import com.guessroll.ui.theme.VioletBright

@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable BoxScope.() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(23.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val fillBrush = if (enabled) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to WarmViolet,
                0.38f to Violet,
                0.76f to PanelElevated,
                1.00f to PanelDeep,
            ),
        )
    } else {
        Brush.verticalGradient(
            listOf(
                PanelDeep.copy(alpha = 0.98f),
                GlassSoft.copy(alpha = 0.86f),
            ),
        )
    }
    val glowBrush = Brush.radialGradient(
        colorStops = arrayOf(
            0.00f to Amber.copy(alpha = if (enabled) 0.105f else 0.00f),
            0.42f to GlowBlue.copy(alpha = if (enabled) 0.070f else 0.00f),
            0.70f to WarmViolet.copy(alpha = if (enabled) 0.036f else 0.00f),
            1.00f to Color.Transparent,
        ),
        radius = 620f,
    )
    val sheenBrush = Brush.verticalGradient(
        colorStops = arrayOf(
            0.00f to Color.White.copy(alpha = if (enabled) 0.090f else 0.00f),
            0.18f to Amber.copy(alpha = if (enabled) 0.045f else 0.00f),
            0.42f to Color.Transparent,
            1.00f to Color.Transparent,
        ),
    )
    val borderBrush = if (enabled) {
        Brush.linearGradient(
            colorStops = arrayOf(
                0.00f to Color.White.copy(alpha = 0.22f),
                0.24f to Sky.copy(alpha = 0.30f),
                0.62f to Amber.copy(alpha = 0.36f),
                1.00f to VioletBright.copy(alpha = 0.25f),
            ),
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.08f),
                PanelStroke.copy(alpha = 0.30f),
            ),
        )
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .tactilePress(
                interactionSource = interactionSource,
                enabled = enabled,
                pressedScale = 0.970f,
                pressedAlpha = 0.990f,
            )
            .shadow(
                elevation = if (enabled) 18.dp else 0.dp,
                shape = shape,
                ambientColor = Color(0xFF000000).copy(alpha = 0.36f),
                spotColor = Amber.copy(alpha = 0.14f),
            )
            .clip(shape)
            .background(fillBrush, shape)
            .background(glowBrush, shape)
            .background(sheenBrush, shape)
            .border(
                BorderStroke(1.dp, borderBrush),
                shape,
            )
            .semantics {
                role = Role.Button
                contentDescription = text
                if (!enabled) {
                    stateDescription = "Недоступно"
                }
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        leading?.invoke(this)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = text,
                color = if (enabled) TextPrimary else TextDisabled,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun SecondaryGlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(21.dp)
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .tactilePress(
                interactionSource = interactionSource,
                enabled = enabled,
                pressedScale = 0.982f,
                pressedAlpha = 0.992f,
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (enabled) 0.032f else 0.010f),
                        Sky.copy(alpha = if (enabled) 0.028f else 0.006f),
                        Amber.copy(alpha = if (enabled) 0.016f else 0.006f),
                        PanelElevated.copy(alpha = if (enabled) 0.78f else 0.52f),
                        Panel.copy(alpha = if (enabled) 0.96f else 0.70f),
                        GlassSoft.copy(alpha = if (enabled) 0.98f else 0.82f),
                    ),
                ),
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (enabled) PanelStrokeStrong.copy(alpha = 0.38f) else PanelStroke.copy(alpha = 0.28f),
                ),
                shape,
            )
            .semantics {
                role = Role.Button
                contentDescription = text
                if (!enabled) {
                    stateDescription = "Недоступно"
                }
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) TextPrimary else TextDisabled,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun UtilityButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(18.dp)
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .tactilePress(
                interactionSource = interactionSource,
                enabled = enabled,
                pressedScale = 0.988f,
                pressedAlpha = 0.995f,
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (enabled) 0.018f else 0.008f),
                        Sky.copy(alpha = if (enabled) 0.018f else 0.000f),
                        Amber.copy(alpha = if (enabled) 0.010f else 0.000f),
                        Panel.copy(alpha = if (enabled) 0.82f else 0.58f),
                        PanelDeep.copy(alpha = if (enabled) 0.94f else 0.78f),
                    ),
                ),
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (enabled) PanelStroke.copy(alpha = 0.56f) else PanelStroke.copy(alpha = 0.28f),
                ),
                shape,
            )
            .semantics {
                role = Role.Button
                contentDescription = text
                if (!enabled) {
                    stateDescription = "Недоступно"
                }
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) TextMuted else TextDisabled,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun InlineUtilityButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val shape = RoundedCornerShape(999.dp)
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .sizeIn(minHeight = 40.dp)
            .tactilePress(
                interactionSource = interactionSource,
                enabled = enabled,
                pressedScale = 0.990f,
                pressedAlpha = 0.995f,
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = if (enabled) 0.018f else 0.008f),
                        Sky.copy(alpha = if (enabled) 0.016f else 0.000f),
                        Amber.copy(alpha = if (enabled) 0.010f else 0.000f),
                        Panel.copy(alpha = if (enabled) 0.80f else 0.54f),
                        PanelDeep.copy(alpha = if (enabled) 0.92f else 0.76f),
                    ),
                ),
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = if (enabled) PanelStroke.copy(alpha = 0.48f) else PanelStroke.copy(alpha = 0.26f),
                ),
                shape,
            )
            .semantics {
                role = Role.Button
                contentDescription = text
                if (!enabled) {
                    stateDescription = "Недоступно"
                }
            }
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 13.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) TextMuted else TextDisabled,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    GradientButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    )
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    SecondaryGlassButton(
        text = text,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    )
}
