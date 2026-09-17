package com.guessroll.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Ink
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.PanelStroke
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun FeedbackSettingsDialog(
    soundEnabled: Boolean,
    musicEnabled: Boolean,
    hapticsEnabled: Boolean,
    onSoundChange: (Boolean) -> Unit,
    onMusicChange: (Boolean) -> Unit,
    onHapticsChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val feedback = rememberGameFeedbackController(
        soundEnabled = soundEnabled,
        hapticsEnabled = hapticsEnabled,
    )
    var soundPreviewArmed by remember { mutableStateOf(false) }
    LaunchedEffect(soundEnabled, soundPreviewArmed) {
        if (soundEnabled && soundPreviewArmed) {
            delay(110)
            feedback.soundPreview()
            soundPreviewArmed = false
        }
    }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Ink.copy(alpha = 0.96f))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            RevealBox {
                ConsentGlassSheet {
                    SectionTitle("Звук и отклик", "локально на устройстве", color = Amber)
                    Spacer(modifier = Modifier.height(12.dp))
                    FeedbackToggleRow(
                        title = "Эффекты",
                        subtitle = "Короткие игровые сигналы.",
                        checked = soundEnabled,
                        onCheckedChange = { enabled ->
                            soundPreviewArmed = enabled
                            onSoundChange(enabled)
                        },
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeedbackToggleRow(
                        title = "Музыка",
                        subtitle = "Тихий фон в меню и лобби.",
                        checked = musicEnabled,
                        onCheckedChange = onMusicChange,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FeedbackToggleRow(
                        title = "Вибрация",
                        subtitle = "Отклик на выбор и реакции.",
                        checked = hapticsEnabled,
                        onCheckedChange = onHapticsChange,
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    SecondaryButton(
                        text = "Готово",
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = subtitle,
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Amber,
                checkedTrackColor = Amber.copy(alpha = 0.34f),
                checkedBorderColor = Amber.copy(alpha = 0.42f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = PanelDeep.copy(alpha = 0.82f),
                uncheckedBorderColor = PanelStroke.copy(alpha = 0.72f),
            ),
        )
    }
}
