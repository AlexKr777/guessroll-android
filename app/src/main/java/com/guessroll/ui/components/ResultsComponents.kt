package com.guessroll.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.guessroll.domain.game.LeaderboardEntry
import com.guessroll.domain.game.ResultsPodiumLayout
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Mint
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.PanelStroke
import com.guessroll.ui.theme.PanelStrokeStrong
import com.guessroll.ui.theme.Pink
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary
import com.guessroll.ui.theme.WarmViolet

@Composable
fun WinnerGlassCard(
    names: String,
    subtitle: String,
    label: String = "победитель",
    scoreText: String? = null,
    accent: Color = Amber,
    modifier: Modifier = Modifier,
) {
    GlassCard(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = listOfNotNull(label, names, scoreText, subtitle).joinToString(". ")
        },
        accent = accent,
        featured = true,
        blurred = true,
        dense = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(accent.copy(alpha = 0.13f), RoundedCornerShape(999.dp))
                    .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                SectionLabel(text = label, color = accent)
            }
            Box(
                modifier = Modifier
                    .background(
                        Brush.radialGradient(
                            listOf(
                                accent.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                        ),
                        RoundedCornerShape(999.dp),
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "финал игры",
                    color = TextMuted,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = names,
            color = TextPrimary,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
        )
        scoreText?.let {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 4.dp)
                    .background(Panel.copy(alpha = 0.84f), RoundedCornerShape(18.dp))
                    .border(1.dp, accent.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = it,
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Text(
            text = subtitle,
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun ResultsPodiumCard(
    entries: List<LeaderboardEntry>,
    isTie: Boolean,
    modifier: Modifier = Modifier,
) {
    val topEntries = entries.take(3)
    val slots = ResultsPodiumLayout.fromLeaderboard(topEntries)
    GlassCard(
        modifier = modifier.semantics(mergeDescendants = true) {
            contentDescription = if (topEntries.isEmpty()) {
                "Финальный подиум. Очков пока нет."
            } else {
                "Финальный подиум. " + topEntries.joinToString(". ") {
                    "Место ${it.rank}: ${it.player.nickname}, ${it.player.score} ${scoreLabel(it.player.score)}"
                }
            }
        },
        accent = Amber,
        featured = true,
        blurred = true,
        dense = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(text = if (isTie) "финальная ничья" else "подиум", color = Amber)
            Text(
                text = "топ ${topEntries.size.coerceAtLeast(1)}",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        if (topEntries.isEmpty()) {
            Text(
                text = "Очки еще не появились.",
                color = TextMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            return@GlassCard
        }

        FinaleStage {
            when (topEntries.size) {
                1 -> {
                    slots.first?.let { entry ->
                        PodiumSlot(
                            entry = entry,
                            featured = true,
                            accent = Amber,
                            height = 184.dp,
                            delayMillis = 70,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                2 -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        slots.first?.let { entry ->
                            PodiumSlot(
                                entry = entry,
                                featured = true,
                                accent = Amber,
                                height = 172.dp,
                                delayMillis = 70,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        slots.second?.let { entry ->
                            PodiumSlot(
                                entry = entry,
                                featured = false,
                                accent = WarmViolet,
                                height = 116.dp,
                                delayMillis = 135,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                else -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(226.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        slots.second?.let { entry ->
                            PodiumSlot(
                                entry = entry,
                                featured = false,
                                accent = WarmViolet,
                                height = 136.dp,
                                delayMillis = 135,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        slots.first?.let { entry ->
                            PodiumSlot(
                                entry = entry,
                                featured = true,
                                accent = Amber,
                                height = 188.dp,
                                delayMillis = 70,
                                modifier = Modifier.weight(1.12f),
                            )
                        }
                        slots.third?.let { entry ->
                            PodiumSlot(
                                entry = entry,
                                featured = false,
                                accent = Mint,
                                height = 128.dp,
                                delayMillis = 165,
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinaleStage(
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Panel.copy(alpha = 0.86f),
                        PanelDeep.copy(alpha = 0.95f),
                    ),
                ),
            )
            .border(1.dp, PanelStroke.copy(alpha = 0.68f), RoundedCornerShape(28.dp))
            .padding(12.dp),
    ) {
        FinaleStageGlow()
        content()
    }
}

@Composable
private fun BoxScope.FinaleStageGlow() {
    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth(0.82f)
            .height(118.dp)
            .background(
                Brush.radialGradient(
                    listOf(
                        Amber.copy(alpha = 0.16f),
                        WarmViolet.copy(alpha = 0.08f),
                        Color.Transparent,
                    ),
                ),
                RoundedCornerShape(999.dp),
            ),
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(24.dp)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Pink.copy(alpha = 0.16f),
                        Amber.copy(alpha = 0.14f),
                        Color.Transparent,
                    ),
                ),
                RoundedCornerShape(999.dp),
            ),
    )
}

@Composable
private fun PodiumSlot(
    entry: LeaderboardEntry,
    featured: Boolean,
    accent: Color,
    height: Dp,
    delayMillis: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.height(height),
        contentAlignment = Alignment.BottomCenter,
    ) {
        RevealBox(delayMillis = delayMillis) {
            PodiumPlace(
                entry = entry,
                featured = featured,
                accent = accent,
                modifier = Modifier
                    .fillMaxSize()
                    .height(height),
            )
        }
    }
}

@Composable
private fun PodiumPlace(
    entry: LeaderboardEntry,
    featured: Boolean,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(if (featured) 28.dp else 24.dp)
    val titleStyle = if (featured) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        accent.copy(alpha = if (featured) 0.22f else 0.13f),
                        Panel.copy(alpha = 0.96f),
                        PanelDeep.copy(alpha = 0.98f),
                    ),
                ),
                shape,
            )
            .border(1.dp, accent.copy(alpha = if (featured) 0.44f else 0.26f), shape)
            .padding(horizontal = if (featured) 16.dp else 11.dp, vertical = if (featured) 15.dp else 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        RankBadge(
            rank = entry.rank,
            featured = featured,
            accent = accent,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (featured) {
                Text(
                    text = "Победитель",
                    color = accent,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = entry.player.nickname,
                color = TextPrimary,
                style = titleStyle,
                fontWeight = FontWeight.Bold,
                maxLines = if (featured) 2 else 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        ScorePill(
            score = entry.player.score,
            accent = accent,
            featured = featured,
        )
    }
}

@Composable
private fun RankBadge(
    rank: Int,
    featured: Boolean,
    accent: Color,
) {
    Box(
        modifier = Modifier
            .background(accent.copy(alpha = if (featured) 0.19f else 0.12f), RoundedCornerShape(999.dp))
            .border(1.dp, accent.copy(alpha = if (featured) 0.34f else 0.22f), RoundedCornerShape(999.dp))
            .padding(horizontal = if (featured) 13.dp else 10.dp, vertical = if (featured) 7.dp else 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "#$rank",
            color = accent,
            style = if (featured) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ScorePill(
    score: Int,
    accent: Color,
    featured: Boolean,
) {
    Box(
        modifier = Modifier
            .background(PanelDeep.copy(alpha = if (featured) 0.64f else 0.50f), RoundedCornerShape(18.dp))
            .border(1.dp, PanelStrokeStrong.copy(alpha = if (featured) 0.38f else 0.22f), RoundedCornerShape(18.dp))
            .padding(horizontal = if (featured) 12.dp else 9.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "$score ${scoreLabel(score)}",
            color = if (featured) TextPrimary else TextMuted,
            style = if (featured) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun scoreLabel(score: Int): String {
    val normalized = score % 100
    return when {
        normalized in 11..14 -> "очков"
        score % 10 == 1 -> "очко"
        score % 10 in 2..4 -> "очка"
        else -> "очков"
    }
}
