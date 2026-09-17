package com.guessroll.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guessroll.data.media.GalleryPermissionPolicy
import com.guessroll.domain.game.LocalProfileStats
import com.guessroll.ui.GuessRollViewModel
import com.guessroll.ui.components.AppBackground
import com.guessroll.ui.components.BannerTone
import com.guessroll.ui.components.HomeRevealScene
import com.guessroll.ui.components.PremiumTopBar
import com.guessroll.ui.components.PrimaryButton
import com.guessroll.ui.components.ScreenColumn
import com.guessroll.ui.components.ScreenHeader
import com.guessroll.ui.components.SecondaryButton
import com.guessroll.ui.components.StatusBanner
import com.guessroll.ui.components.SubtleCard
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.TextMuted

@Composable
fun HomeScreen(
    viewModel: GuessRollViewModel,
    isSupabaseConfigured: Boolean,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        if (GalleryPermissionPolicy.hasImageAccess(context)) {
            viewModel.preloadGalleryCandidates(context)
        }
    }

    AppBackground {
        ScreenColumn(
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 24.dp),
            scrollable = true,
        ) {
            PremiumTopBar(label = "игрок: ${state.nickname ?: "гость"}")
            if (state.localProfileStats.hasHistory) {
                Spacer(modifier = Modifier.height(10.dp))
                HomeProfileStrip(stats = state.localProfileStats)
            }
            Spacer(modifier = Modifier.height(20.dp))
            ScreenHeader(
                eyebrow = "фото-игра с друзьями",
                title = "Запусти игру",
                subtitle = "Создай комнату или войди к друзьям.",
            )

            Spacer(modifier = Modifier.height(24.dp))
            if (!isSupabaseConfigured) {
                StatusBanner(
                    message = "Supabase ключи не найдены. Комнаты заработают после настройки local.properties.",
                    tone = BannerTone.Warning,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            ActionPanel(
                onCreateRoom = onCreateRoom,
                onJoinRoom = onJoinRoom,
            )
            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBanner(message = it)
            }
        }
    }
}

@Composable
private fun HomeProfileStrip(
    stats: LocalProfileStats,
) {
    SubtleCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = "Стрик: ${stats.currentStreak}",
                color = Amber,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Игр: ${stats.gamesPlayed} · Побед: ${stats.wins} · Точность: ${stats.accuracyPercent}%",
                color = TextMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ActionPanel(
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        PrimaryButton(
            text = "Создать комнату",
            onClick = onCreateRoom,
        )
        Spacer(modifier = Modifier.height(12.dp))
        SecondaryButton(
            text = "Войти в комнату",
            onClick = onJoinRoom,
        )
        Spacer(modifier = Modifier.height(18.dp))
        HomeRevealScene(
            modifier = Modifier
                .fillMaxWidth()
                .height(174.dp),
            alpha = 0.96f,
        )
    }
}
