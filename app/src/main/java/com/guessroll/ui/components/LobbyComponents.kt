package com.guessroll.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Mint
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary

@Composable
fun RoomCodeGlassCard(
    code: String,
    modifier: Modifier = Modifier,
    onCopy: (() -> Unit)? = null,
    onShowQr: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    GlassCard(
        modifier = modifier
            .then(
                if (onCopy != null) {
                    Modifier
                        .tactilePress(
                            interactionSource = interactionSource,
                            enabled = true,
                            pressedScale = 0.982f,
                            pressedAlpha = 0.992f,
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onCopy,
                        )
                } else {
                    Modifier
                },
            )
            .semantics(mergeDescendants = true) {
                contentDescription = if (onCopy != null) {
                    "Код комнаты $code. Нажми, чтобы скопировать."
                } else {
                    "Код комнаты $code"
                }
                if (onCopy != null) {
                    role = Role.Button
                }
            },
        accent = Amber,
        compact = true,
        featured = true,
        blurred = true,
        dense = true,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GuessRollLogoMark(size = 34.dp, contentDescription = null)
                Column(modifier = Modifier.weight(1f)) {
                    SectionTitle("Код комнаты", "нажми, чтобы скопировать", color = Amber)
                }
                if (onShowQr != null) {
                    InlineUtilityButton(
                        text = "QR",
                        onClick = onShowQr,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Amber.copy(alpha = 0.10f),
                                Panel.copy(alpha = 0.96f),
                                PanelDeep.copy(alpha = 0.98f),
                            ),
                        ),
                    )
                    .border(1.dp, Amber.copy(alpha = 0.28f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center,
            ) {
                LobbyInvitePassMotif(
                    modifier = Modifier
                        .matchParentSize()
                        .padding(3.dp),
                    alpha = 0.92f,
                )
                Text(
                    text = code,
                    color = TextPrimary,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
                )
            }
        }
    }
}

@Composable
fun PhotoMysteryCard(
    modifier: Modifier = Modifier,
    count: Int,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(
        modifier = modifier.semantics {
            contentDescription = "$title. $subtitle. Загружено твоих фото: $count"
        },
        accent = Sky,
        compact = true,
        featured = false,
        blurred = false,
        dense = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MysteryGlyph()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    SectionLabel(text = "фото в раундах", color = Sky)
                    Text(
                        text = title,
                        color = TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subtitle,
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            MysteryCount(count = count)
        }
        Spacer(modifier = Modifier.height(11.dp))
        PhotoPrepHiddenStackMotif(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp),
            alpha = if (count > 0) 0.92f else 0.76f,
        )
        content()
    }
}

@Composable
private fun MysteryGlyph() {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Sky.copy(alpha = 0.10f),
                        PanelDeep.copy(alpha = 0.96f),
                    ),
                ),
            )
            .border(1.dp, Sky.copy(alpha = 0.16f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        PhotoPrepAccent(
            width = 27.dp,
            height = 27.dp,
        )
    }
}

@Composable
private fun MysteryCount(count: Int) {
    Box(
        modifier = Modifier
            .background(
                Brush.verticalGradient(
                    listOf(
                        Mint.copy(alpha = if (count > 0) 0.080f else 0.026f),
                        Sky.copy(alpha = 0.035f),
                        PanelDeep.copy(alpha = 0.94f),
                    ),
                ),
                RoundedCornerShape(18.dp),
            )
            .border(BorderStroke(1.dp, Sky.copy(alpha = 0.085f)), RoundedCornerShape(18.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MotionContent(targetState = count, label = "photoCount") { countValue ->
                Text(
                    text = countValue.toString(),
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = "твоих",
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
