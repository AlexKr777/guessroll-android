package com.guessroll.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Danger
import com.guessroll.ui.theme.Mint
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.TextPrimary

@Composable
fun StatusBanner(
    message: String,
    modifier: Modifier = Modifier,
    tone: BannerTone = BannerTone.Error,
) {
    val color = when (tone) {
        BannerTone.Error -> Danger
        BannerTone.Warning -> Amber
        BannerTone.Info -> Sky
        BannerTone.Success -> Mint
    }
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .motionContentSize()
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        color.copy(alpha = 0.078f),
                        Color.White.copy(alpha = 0.022f),
                    ),
                ),
            )
            .border(BorderStroke(1.dp, color.copy(alpha = 0.14f)), shape)
            .semantics(mergeDescendants = true) {
                contentDescription = message
                liveRegion = if (tone == BannerTone.Error) LiveRegionMode.Assertive else LiveRegionMode.Polite
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(color),
        )
        Text(
            text = message,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(start = 11.dp)
                .weight(1f),
        )
    }
}

@Composable
fun LoadingGlassState(
    message: String,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .motionContentSize()
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Sky.copy(alpha = 0.078f),
                        Color.White.copy(alpha = 0.022f),
                    ),
                ),
            )
            .border(BorderStroke(1.dp, Sky.copy(alpha = 0.14f)), shape)
            .semantics(mergeDescendants = true) {
                contentDescription = message
                liveRegion = LiveRegionMode.Polite
            }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(18.dp),
            color = Sky,
            strokeWidth = 2.dp,
        )
        Text(
            text = message,
            color = TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(start = 11.dp)
                .weight(1f),
        )
    }
}

enum class BannerTone {
    Error,
    Warning,
    Info,
    Success,
}
