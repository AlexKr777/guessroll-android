package com.guessroll.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.TextFaint
import com.guessroll.ui.theme.TextPrimary

@Composable
fun PlayerGlassCard(
    nickname: String,
    meta: String,
    score: Int,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        accent.copy(alpha = 0.035f),
                        Panel.copy(alpha = 0.72f),
                        PanelDeep.copy(alpha = 0.90f),
                    ),
                ),
            )
            .border(1.dp, accent.copy(alpha = 0.080f), RoundedCornerShape(20.dp))
            .semantics(mergeDescendants = true) {
                contentDescription = "$nickname, $meta, $score очков"
            }
            .padding(horizontal = 12.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerInitial(nickname = nickname, accent = accent)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nickname,
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = meta,
                    color = accent,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Sky.copy(alpha = if (score == 0) 0.030f else 0.070f))
                .border(1.dp, Sky.copy(alpha = if (score == 0) 0.085f else 0.18f), RoundedCornerShape(16.dp))
                .padding(horizontal = 9.dp, vertical = 5.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "$score очк.",
                color = if (score == 0) TextFaint else TextPrimary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PlayerInitial(
    nickname: String,
    accent: Color,
) {
    val initial = nickname.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = 0.080f),
                        PanelDeep.copy(alpha = 0.98f),
                    ),
                ),
            )
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(15.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = TextPrimary,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}
