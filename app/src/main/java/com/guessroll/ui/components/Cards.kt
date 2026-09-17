package com.guessroll.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.cloudy
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Glass
import com.guessroll.ui.theme.GlassStrong
import com.guessroll.ui.theme.InkRaised
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.PanelElevated
import com.guessroll.ui.theme.PanelStroke
import com.guessroll.ui.theme.PanelStrokeStrong
import com.guessroll.ui.theme.RadiusLarge
import com.guessroll.ui.theme.RadiusMedium
import com.guessroll.ui.theme.RadiusXL
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    featured: Boolean = false,
    compact: Boolean = false,
    blurred: Boolean = false,
    dense: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(if (featured) RadiusXL else RadiusMedium)
    val topTint = accent ?: Amber
    val sky = LocalGuessRollSky.current
    val cloudyModifier = if (blurred && sky != null) {
        Modifier.cloudy(
            sky = sky,
            radius = if (featured) 30 else 22,
            tint = PanelDeep.copy(
                alpha = when {
                    dense -> 0.62f
                    featured -> 0.46f
                    else -> 0.38f
                },
            ),
            cpuBlurEnabled = false,
        )
    } else {
        Modifier
    }
    val cardElevation = when {
        blurred && featured -> 18.dp
        blurred -> 10.dp
        featured -> 12.dp
        else -> 3.dp
    }
    val spotAlpha = when {
        blurred && featured -> 0.11f
        blurred -> 0.070f
        featured -> 0.060f
        else -> 0.025f
    }
    val fillColors = when {
        dense -> listOf(
            topTint.copy(alpha = 0.036f),
            PanelElevated.copy(alpha = 0.82f),
            PanelDeep.copy(alpha = 0.99f),
            InkRaised,
        )
        blurred && featured -> listOf(
            Color.White.copy(alpha = 0.024f),
            topTint.copy(alpha = 0.054f),
            GlassStrong.copy(alpha = 0.96f),
            PanelDeep.copy(alpha = 0.99f),
        )
        blurred -> listOf(
            Color.White.copy(alpha = 0.016f),
            topTint.copy(alpha = 0.034f),
            Glass.copy(alpha = 0.96f),
            PanelDeep.copy(alpha = 0.98f),
        )
        featured -> listOf(
            Color.White.copy(alpha = 0.014f),
            topTint.copy(alpha = 0.032f),
            GlassStrong.copy(alpha = 1f),
            PanelDeep.copy(alpha = 0.98f),
        )
        else -> listOf(
            topTint.copy(alpha = 0.010f),
            Glass.copy(alpha = 0.99f),
            PanelDeep.copy(alpha = 0.99f),
        )
    }
    val borderColor = accent?.copy(
        alpha = when {
            dense -> 0.22f
            blurred && featured -> 0.24f
            blurred -> 0.18f
            featured -> 0.15f
            else -> 0.08f
        },
    ) ?: when {
        blurred && featured -> PanelStrokeStrong.copy(alpha = 0.44f)
        blurred -> PanelStrokeStrong.copy(alpha = 0.34f)
        featured -> PanelStrokeStrong.copy(alpha = 0.32f)
        else -> PanelStroke
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = cardElevation,
                shape = shape,
                ambientColor = Color(0xFF000000).copy(alpha = 0.26f),
                spotColor = topTint.copy(alpha = spotAlpha),
            )
            .clip(shape)
            .then(cloudyModifier)
            .background(Brush.verticalGradient(colors = fillColors), shape)
            .border(
                BorderStroke(
                    width = 1.dp,
                    color = borderColor,
                ),
                shape,
            ),
    ) {
        Column(
            modifier = Modifier.padding(if (compact) 16.dp else if (featured) 22.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            content = content,
        )
    }
}

@Composable
fun PartyCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    featured: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(
        modifier = modifier,
        accent = accent,
        featured = featured,
        content = content,
    )
}

@Composable
fun HeroCard(
    modifier: Modifier = Modifier,
    accent: Color = Amber,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(
        modifier = modifier,
        accent = accent,
        featured = true,
        content = content,
    )
}

@Composable
fun QuietCard(
    modifier: Modifier = Modifier,
    accent: Color? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(
        modifier = modifier,
        accent = accent,
        compact = true,
        content = content,
    )
}

@Composable
fun SubtleCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    QuietCard(
        modifier = modifier,
        accent = Sky,
        content = content,
    )
}

@Composable
fun SectionTitle(
    title: String,
    subtitle: String? = null,
    color: Color = Amber,
) {
    SectionLabel(text = title, color = color)
    subtitle?.let {
        Text(
            text = it,
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun InfoPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        color.copy(alpha = 0.070f),
                        PanelDeep.copy(alpha = 0.90f),
                    ),
                ),
            )
            .border(BorderStroke(1.dp, color.copy(alpha = 0.16f)), RoundedCornerShape(20.dp))
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(color.copy(alpha = 0.86f)),
        )
        Column {
            Text(
                text = value,
                color = TextPrimary,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Amber,
) {
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier,
    )
}
