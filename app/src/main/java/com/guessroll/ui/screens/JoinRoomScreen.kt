package com.guessroll.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guessroll.ui.GuessRollViewModel
import com.guessroll.ui.components.AppBackground
import com.guessroll.ui.components.GlassCard
import com.guessroll.ui.components.PartyTextField
import com.guessroll.ui.components.PrimaryButton
import com.guessroll.ui.components.QrScannerDialog
import com.guessroll.ui.components.RevealBox
import com.guessroll.ui.components.ScreenColumn
import com.guessroll.ui.components.ScreenHeader
import com.guessroll.ui.components.SecondaryButton
import com.guessroll.ui.components.SectionTitle
import com.guessroll.ui.components.StatusBanner
import com.guessroll.ui.components.TopActionButton
import com.guessroll.ui.components.rememberGameFeedbackController
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.TextMuted

@Composable
fun JoinRoomScreen(
    viewModel: GuessRollViewModel,
    onBack: () -> Unit,
    onJoined: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var isQrScannerVisible by remember { mutableStateOf(false) }
    val feedback = rememberGameFeedbackController(
        soundEnabled = state.soundEnabled,
        hapticsEnabled = state.hapticsEnabled,
    )
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            isQrScannerVisible = true
        } else {
            viewModel.showInviteScanError("Камера нужна только для скана QR комнаты.")
        }
    }

    fun openQrScanner() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            isQrScannerVisible = true
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    AppBackground {
        ScreenColumn(
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 24.dp),
            scrollable = true,
        ) {
            TopActionButton(
                text = "Назад",
                onClick = {
                    viewModel.clearTransientErrors()
                    onBack()
                },
            )
            Spacer(modifier = Modifier.height(14.dp))
            ScreenHeader(
                title = "Войти в комнату",
                subtitle = "Введи код или сканируй QR от хоста.",
            )

            Spacer(modifier = Modifier.height(22.dp))
            GlassCard(
                accent = Amber,
                compact = true,
                featured = true,
                dense = true,
            ) {
                SectionTitle("Код комнаты", "буквы и цифры без пробелов", color = Amber)
                Spacer(modifier = Modifier.height(14.dp))
                PartyTextField(
                    value = state.roomCodeInput,
                    onValueChange = viewModel::updateRoomCode,
                    label = "Код",
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "После входа добавишь фото для игры.",
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.height(16.dp))
                PrimaryButton(
                    text = "Войти",
                    enabled = !state.isLoading,
                    onClick = { viewModel.joinRoom(onJoined) },
                )
                Spacer(modifier = Modifier.height(10.dp))
                SecondaryButton(
                    text = "Сканировать QR",
                    enabled = !state.isLoading,
                    onClick = ::openQrScanner,
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            state.errorMessage?.let {
                RevealBox {
                    StatusBanner(message = it)
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Amber,
                )
            }
            if (isQrScannerVisible) {
                QrScannerDialog(
                    onScanned = { raw ->
                        isQrScannerVisible = false
                        if (viewModel.applyRoomInvite(raw)) {
                            feedback.success()
                        }
                    },
                    onDismiss = {
                        isQrScannerVisible = false
                        viewModel.clearTransientErrors()
                    },
                )
            }
        }
    }
}
