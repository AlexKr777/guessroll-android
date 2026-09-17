package com.guessroll.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guessroll.data.media.GalleryPermissionPolicy
import com.guessroll.ui.GuessRollViewModel
import com.guessroll.ui.components.AppBackground
import com.guessroll.ui.components.ConsentGlassSheet
import com.guessroll.ui.components.FeedbackSettingsDialog
import com.guessroll.ui.components.GuessRollLogoMark
import com.guessroll.ui.components.HeroCard
import com.guessroll.ui.components.IdentityRevealScene
import com.guessroll.ui.components.InlineUtilityButton
import com.guessroll.ui.components.PartyTextField
import com.guessroll.ui.components.PremiumTopBar
import com.guessroll.ui.components.PrimaryButton
import com.guessroll.ui.components.RevealBox
import com.guessroll.ui.components.ScreenColumn
import com.guessroll.ui.components.SecondaryButton
import com.guessroll.ui.components.SectionTitle
import com.guessroll.ui.components.StatusBanner
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Ink
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary

@Composable
fun NicknameScreen(
    viewModel: GuessRollViewModel,
    onContinue: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isFeedbackSheetVisible by remember { mutableStateOf(false) }
    val earlyPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        viewModel.dismissEarlyPhotoPrepConsent()
        if (GalleryPermissionPolicy.isAccessGranted(grants)) {
            viewModel.preloadGalleryCandidates(context)
        }
        onContinue()
    }

    fun continueAfterNickname() {
        if (!viewModel.confirmNickname()) return
        if (GalleryPermissionPolicy.hasImageAccess(context)) {
            viewModel.preloadGalleryCandidates(context)
            onContinue()
        } else {
            viewModel.showEarlyPhotoPrepConsent()
        }
    }

    AppBackground {
        ScreenColumn(
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 28.dp),
            scrollable = true,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PremiumTopBar(label = "GuessRoll")
                InlineUtilityButton(
                    text = "Аудио",
                    onClick = { isFeedbackSheetVisible = true },
                )
            }
            Spacer(modifier = Modifier.height(30.dp))

            HeroIntro()

            Spacer(modifier = Modifier.height(22.dp))
            RevealBox(delayMillis = 50) {
                HeroCard(
                    accent = Amber,
                ) {
                    Text(
                        text = "Введи ник",
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(7.dp))
                    Text(
                        text = "Так друзья увидят тебя в раундах.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(17.dp))
                    PartyTextField(
                        value = state.nicknameInput,
                        onValueChange = viewModel::updateNickname,
                        label = "Ник в игре",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (state.localProfileStats.hasHistory) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Стрик: ${state.localProfileStats.currentStreak} · лучший: ${state.localProfileStats.bestStreak} · игр: ${state.localProfileStats.gamesPlayed}",
                            color = Amber,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    PrimaryButton(
                        text = "Продолжить",
                        onClick = ::continueAfterNickname,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            RevealBox(delayMillis = 110) {
                IdentityRevealScene(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp),
                    alpha = 0.82f,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            state.errorMessage?.let {
                StatusBanner(message = it)
            }

            if (state.isEarlyPhotoPrepConsentVisible) {
                EarlyPhotoPrepDialog(
                    onAllow = {
                        val permissions = GalleryPermissionPolicy.requiredPermissions()
                        if (permissions.isEmpty() || GalleryPermissionPolicy.hasImageAccess(context)) {
                            viewModel.dismissEarlyPhotoPrepConsent()
                            viewModel.preloadGalleryCandidates(context)
                            onContinue()
                        } else {
                            earlyPermissionLauncher.launch(permissions)
                        }
                    },
                    onLater = {
                        viewModel.dismissEarlyPhotoPrepConsent()
                        onContinue()
                    },
                )
            }

            if (isFeedbackSheetVisible) {
                FeedbackSettingsDialog(
                    soundEnabled = state.soundEnabled,
                    musicEnabled = state.musicEnabled,
                    hapticsEnabled = state.hapticsEnabled,
                    onSoundChange = viewModel::setSoundEnabled,
                    onMusicChange = viewModel::setMusicEnabled,
                    onHapticsChange = viewModel::setHapticsEnabled,
                    onDismiss = { isFeedbackSheetVisible = false },
                )
            }
        }
    }
}

@Composable
private fun EarlyPhotoPrepDialog(
    onAllow: () -> Unit,
    onLater: () -> Unit,
) {
    Dialog(
        onDismissRequest = onLater,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Ink.copy(alpha = 0.96f))
                .padding(horizontal = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            RevealBox {
                ConsentGlassSheet {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GuessRollLogoMark(size = 40.dp, contentDescription = null)
                        SectionTitle("Подготовить фото", "до лобби", color = Sky)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Чтобы потом не ждать в лобби",
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "GuessRoll заранее подготовит список случайных фото. Кадры всё равно откроются только в раундах.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Разрешить доступ",
                        onClick = onAllow,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SecondaryButton(
                        text = "Позже",
                        onClick = onLater,
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroIntro() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Кто ты в игре?",
            color = TextPrimary,
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "Выбери имя для комнаты и угадываний.",
            color = TextMuted,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
