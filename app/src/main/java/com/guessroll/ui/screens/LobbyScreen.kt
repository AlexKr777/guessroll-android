package com.guessroll.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.guessroll.data.media.PhotoSourceMode
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.guessroll.data.media.GalleryPermissionPolicy
import com.guessroll.domain.game.LobbySnapshot
import com.guessroll.domain.game.Player
import com.guessroll.domain.game.PlayerMediaProgress
import com.guessroll.domain.game.RoomStatus
import com.guessroll.ui.GalleryPreloadPhase
import com.guessroll.ui.GalleryPreloadStatus
import com.guessroll.ui.GuessRollUiState
import com.guessroll.ui.GuessRollViewModel
import com.guessroll.ui.components.AppBackground
import com.guessroll.ui.components.BannerTone
import com.guessroll.ui.components.ConsentGlassSheet
import com.guessroll.ui.components.EmptyStateCard
import com.guessroll.ui.components.GuessRollLogoMark
import com.guessroll.ui.components.InlineUtilityButton
import com.guessroll.ui.components.LoadingGlassState
import com.guessroll.ui.components.LifecycleEventRefreshEffect
import com.guessroll.ui.components.LifecyclePollingEffect
import com.guessroll.ui.components.MotionContent
import com.guessroll.ui.components.PhotoMysteryCard
import com.guessroll.ui.components.PlayerGlassCard
import com.guessroll.ui.components.PrimaryButton
import com.guessroll.ui.components.RevealBox
import com.guessroll.ui.components.RoomCodeGlassCard
import com.guessroll.ui.components.RoomQrInviteDialog
import com.guessroll.ui.components.ScreenColumn
import com.guessroll.ui.components.ScreenHeader
import com.guessroll.ui.components.SecondaryButton
import com.guessroll.ui.components.SectionTitle
import com.guessroll.ui.components.StatusBanner
import com.guessroll.ui.components.SubtleCard
import com.guessroll.ui.components.TopActionButton
import com.guessroll.ui.components.motionContentSize
import com.guessroll.ui.components.tactilePress
import com.guessroll.ui.theme.Amber
import com.guessroll.ui.theme.Ink
import com.guessroll.ui.theme.Mint
import com.guessroll.ui.theme.Panel
import com.guessroll.ui.theme.PanelDeep
import com.guessroll.ui.theme.Sky
import com.guessroll.ui.theme.TextMuted
import com.guessroll.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun LobbyScreen(
    viewModel: GuessRollViewModel,
    onBackHome: () -> Unit,
    onStartGame: () -> Unit,
    onGameStarted: () -> Unit,
    onFinished: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lobby = state.lobby
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var isRoomCodeCopied by remember(lobby?.room?.code) { mutableStateOf(false) }
    var isRoomQrVisible by remember(lobby?.room?.code) { mutableStateOf(false) }
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20),
    ) { uris ->
        viewModel.uploadSelectedPhotos(context, uris)
    }
    val galleryPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (GalleryPermissionPolicy.isAccessGranted(grants)) {
            viewModel.startBlindRandomPhotoUpload(context)
        } else {
            viewModel.showGalleryPermissionDenied()
        }
    }
    val canAutoRefreshLobby = lobby != null &&
        !state.isLoading &&
        !state.isUploadingPhotos &&
        !state.isScanningGallery &&
        !state.isBlindPhotoConsentVisible
    val roomChangeEvents = remember(lobby?.room?.id) {
        lobby?.room?.id?.let(viewModel::observeRoomChanges)
    }
    LifecyclePollingEffect(
        key = lobby?.room?.id,
        enabled = canAutoRefreshLobby,
        intervalMillis = 7_000,
        onRefresh = { viewModel.refreshLobbySilently() },
    )
    LifecycleEventRefreshEffect(
        key = lobby?.room?.id,
        enabled = canAutoRefreshLobby,
        events = roomChangeEvents,
        onEvent = { viewModel.refreshLobbySilently() },
    )
    LaunchedEffect(lobby?.room?.id, lobby?.room?.status) {
        when (lobby?.room?.status) {
            RoomStatus.Playing,
            RoomStatus.Finished -> viewModel.openGameFromLobby(
                onStarted = onGameStarted,
                onFinished = onFinished,
            )
            else -> Unit
        }
    }
    LaunchedEffect(isRoomCodeCopied) {
        if (isRoomCodeCopied) {
            delay(1500)
            isRoomCodeCopied = false
        }
    }

    fun startBlindUploadAfterConsent() {
        viewModel.dismissBlindPhotoConsent()
        if (GalleryPermissionPolicy.hasImageAccess(context)) {
            viewModel.startBlindRandomPhotoUpload(context)
            return
        }

        val permissions = GalleryPermissionPolicy.requiredPermissions()
        if (permissions.isEmpty()) {
            viewModel.startBlindRandomPhotoUpload(context)
        } else {
            galleryPermissionLauncher.launch(permissions)
        }
    }

    AppBackground {
        ScreenColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            scrollable = true,
        ) {
            TopActionButton(text = "Домой", onClick = onBackHome)

            if (lobby == null) {
                EmptyStateCard(
                    title = "Комната не открыта",
                    subtitle = "Сначала создай комнату или войди по коду.",
                )
                return@ScreenColumn
            }

            Spacer(modifier = Modifier.height(14.dp))
            ScreenHeader(
                eyebrow = if (lobby.isCurrentPlayerHost) "хост комнаты" else "игрок в комнате",
                title = "Комната",
                subtitle = if (lobby.isCurrentPlayerHost) {
                    "Ждём игроков и фото."
                } else {
                    "Добавь фото и жди старта."
                },
            )

            Spacer(modifier = Modifier.height(14.dp))
            RevealBox(delayMillis = 30) {
                RoomCodeGlassCard(
                    code = lobby.room.code,
                    onCopy = {
                        clipboardManager.setText(AnnotatedString(lobby.room.code))
                        isRoomCodeCopied = true
                    },
                    onShowQr = { isRoomQrVisible = true },
                )
            }
            if (isRoomCodeCopied) {
                Spacer(modifier = Modifier.height(8.dp))
                StatusBanner(
                    message = "Код скопирован.",
                    tone = BannerTone.Success,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            RevealBox(delayMillis = 70) {
                UploadBlock(
                    state = state,
                    lobby = lobby,
                    onPhotoSourceModeChange = viewModel::setPhotoSourceMode,
                    onRandomPhotos = viewModel::showBlindPhotoConsent,
                    onPickPhotos = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    onRetry = viewModel::retryPhotoUpload,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            RevealBox(delayMillis = 110) {
                PlayersCard(
                    lobby = lobby,
                    state = state,
                    onRefresh = viewModel::refreshLobby,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            RevealBox(delayMillis = 140) {
                LobbyActions(
                    state = state,
                    lobby = lobby,
                    onStartGame = onStartGame,
                )
            }

            state.errorMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                StatusBanner(message = it)
            }

            if (state.isBlindPhotoConsentVisible) {
                BlindPhotoConsentDialog(
                    sourceMode = state.photoSourceMode,
                    onDismiss = viewModel::dismissBlindPhotoConsent,
                    onConfirm = ::startBlindUploadAfterConsent,
                )
            }

            if (isRoomQrVisible) {
                RoomQrInviteDialog(
                    roomCode = lobby.room.code,
                    onCopyCode = {
                        clipboardManager.setText(AnnotatedString(lobby.room.code))
                        isRoomCodeCopied = true
                    },
                    onDismiss = { isRoomQrVisible = false },
                )
            }
        }
    }
}

@Composable
private fun UploadBlock(
    state: GuessRollUiState,
    lobby: LobbySnapshot,
    onPhotoSourceModeChange: (PhotoSourceMode) -> Unit,
    onRandomPhotos: () -> Unit,
    onPickPhotos: () -> Unit,
    onRetry: () -> Unit,
) {
    val currentProgress = lobby.mediaStatus.progressForPlayer(lobby.currentPlayerId)
    val currentPlayerReady = currentProgress?.isReady == true
    val requiredPhotoCount = currentProgress?.requiredPhotoCount ?: lobby.mediaStatus.requiredPhotoCount
    val canUpload = lobby.room.status == RoomStatus.Lobby &&
        !state.isUploadingPhotos &&
        !state.isScanningGallery &&
        !state.isBlindPhotoConsentVisible

    PhotoMysteryCard(
        modifier = Modifier.motionContentSize(),
        count = lobby.mediaStatus.currentPlayerPhotoCount,
        title = "Фото для игры",
        subtitle = "Кадры откроются только в раундах.",
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        if (lobby.room.status != RoomStatus.Lobby) {
            StatusBanner(
                message = "Фото закрыты после старта игры.",
                tone = BannerTone.Info,
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (lobby.room.status == RoomStatus.Lobby && !state.isScanningGallery && !state.isUploadingPhotos) {
            PhotoSourceModeControl(
                mode = state.photoSourceMode,
                preloadStatus = state.galleryPreloadStatus,
                enabled = canUpload,
                onModeChange = onPhotoSourceModeChange,
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        val uploadMotionState = when {
            state.isScanningGallery -> "scanning"
            state.isUploadingPhotos -> "uploading"
            else -> "idle"
        }
        MotionContent(
            targetState = uploadMotionState,
            label = "blindPhotoUploadState",
        ) { motionState ->
            when (motionState) {
                "scanning" -> LoadingGlassState(state.blindPhotoMessage ?: "Ищем подходящие фото...")
                "uploading" -> LoadingGlassState(
                    uploadProgressText(state),
                )
                else -> if (currentPlayerReady) {
                    PhotoReadyAction(
                        uploadedCount = currentProgress.uploadedPhotoCount,
                        requiredCount = requiredPhotoCount,
                        enabled = canUpload,
                        onRefreshPhotos = onRandomPhotos,
                    )
                } else {
                    PrimaryButton(
                        text = if (lobby.mediaStatus.currentPlayerPhotoCount == 0) {
                            "Подобрать случайные фото"
                        } else {
                            "Добавить случайные фото"
                        },
                        enabled = canUpload,
                        onClick = onRandomPhotos,
                    )
                }
            }
        }

        state.blindPhotoMessage?.takeIf {
            !state.isScanningGallery && !state.isUploadingPhotos && !currentPlayerReady
        }?.let {
            Spacer(modifier = Modifier.height(10.dp))
            StatusBanner(message = it, tone = BannerTone.Success)
        }

        state.galleryErrorMessage?.let {
            Spacer(modifier = Modifier.height(10.dp))
            StatusBanner(message = it, tone = BannerTone.Warning)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton(
                text = "Выбрать вручную",
                onClick = onPickPhotos,
                enabled = canUpload,
            )
        }

        state.uploadErrorMessage?.let {
            Spacer(modifier = Modifier.height(10.dp))
            StatusBanner(message = it)
            Spacer(modifier = Modifier.height(10.dp))
            SecondaryButton(
                text = "Повторить загрузку",
                onClick = onRetry,
                enabled = !state.isUploadingPhotos,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        ReadinessInline(lobby = lobby)
    }
}

@Composable
private fun PhotoSourceModeControl(
    mode: PhotoSourceMode,
    preloadStatus: GalleryPreloadStatus,
    enabled: Boolean,
    onModeChange: (PhotoSourceMode) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Источник",
                color = TextMuted,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = photoSourceHelper(mode, preloadStatus),
                color = TextMuted,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.032f),
                            PanelDeep.copy(alpha = 0.92f),
                        ),
                    ),
                    RoundedCornerShape(18.dp),
                )
                .border(BorderStroke(1.dp, Sky.copy(alpha = 0.10f)), RoundedCornerShape(18.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            PhotoSourceModeSegment(
                text = PhotoSourceMode.PHOTOS_ONLY.label,
                selected = mode == PhotoSourceMode.PHOTOS_ONLY,
                enabled = enabled,
                onClick = { onModeChange(PhotoSourceMode.PHOTOS_ONLY) },
                modifier = Modifier.weight(1f),
            )
            PhotoSourceModeSegment(
                text = PhotoSourceMode.ALL_GALLERY.label,
                selected = mode == PhotoSourceMode.ALL_GALLERY,
                enabled = enabled,
                onClick = { onModeChange(PhotoSourceMode.ALL_GALLERY) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PhotoReadyAction(
    uploadedCount: Int,
    requiredCount: Int,
    enabled: Boolean,
    onRefreshPhotos: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        StatusBanner(
            message = "Фото готовы: ${uploadedCount.coerceAtLeast(0)}/${requiredCount.coerceAtLeast(1)} твоих.",
            tone = BannerTone.Success,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InlineUtilityButton(
                text = "Обновить фото",
                enabled = enabled,
                onClick = onRefreshPhotos,
            )
        }
    }
}

@Composable
private fun PhotoSourceModeSegment(
    text: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(15.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val textColor = when {
        selected -> TextPrimary
        enabled -> TextMuted
        else -> TextMuted.copy(alpha = 0.52f)
    }
    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .tactilePress(
                interactionSource = interactionSource,
                enabled = enabled,
                pressedScale = 0.986f,
                pressedAlpha = 0.992f,
            )
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        listOf(
                            Sky.copy(alpha = 0.20f),
                            Amber.copy(alpha = 0.075f),
                            Panel.copy(alpha = 0.94f),
                        ),
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.014f),
                            PanelDeep.copy(alpha = 0.78f),
                        ),
                    )
                },
                shape,
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (selected) Sky.copy(alpha = 0.24f) else Color.White.copy(alpha = 0.030f),
                ),
                shape,
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ReadinessInline(lobby: LobbySnapshot) {
    val ready = lobby.mediaStatus.readiness.canStart
    val color = if (ready) Mint else Amber
    val shape = RoundedCornerShape(18.dp)
    val reason = readinessText(lobby)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .motionContentSize()
            .background(
                Brush.horizontalGradient(
                    listOf(
                        color.copy(alpha = 0.045f),
                        PanelDeep.copy(alpha = 0.90f),
                    ),
                ),
                shape,
            )
            .border(BorderStroke(1.dp, color.copy(alpha = 0.10f)), shape)
            .semantics(mergeDescendants = true) {
                contentDescription = "Готовность: ${lobby.mediaStatus.playersWithPhotos} из ${lobby.mediaStatus.totalPlayers} игроков готовы. $reason"
                stateDescription = if (ready) "Готово к старту" else "Ещё готовимся"
            }
            .padding(horizontal = 9.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            MotionContent(targetState = ready, label = "readinessTitle") { isReady ->
                Text(
                    text = if (isReady) "Готово к игре" else "Ждём фото",
                    color = TextPrimary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
            MotionContent(targetState = reason, label = "readinessReason") { reasonText ->
                Text(
                    text = reasonText,
                    color = TextMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        MotionContent(
            targetState = "${lobby.mediaStatus.playersWithPhotos}/${lobby.mediaStatus.totalPlayers}",
            label = "readinessCount",
        ) { countText ->
            Text(
                text = countText,
                color = color,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(color.copy(alpha = 0.060f), RoundedCornerShape(18.dp))
                    .border(BorderStroke(1.dp, color.copy(alpha = 0.10f)), RoundedCornerShape(18.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

@Composable
private fun PlayersCard(
    lobby: LobbySnapshot,
    state: GuessRollUiState,
    onRefresh: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                SectionTitle("Игроки", "${lobby.players.size} в комнате", color = Sky)
            }
            InlineUtilityButton(
                text = "Обновить",
                enabled = !state.isLoading && !state.isUploadingPhotos && !state.isScanningGallery,
                onClick = onRefresh,
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        if (lobby.players.isEmpty()) {
            EmptyPlayersState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 188.dp),
            ) {
                items(
                    items = lobby.players,
                    key = { player -> player.id },
                ) { player ->
                    PlayerRow(
                        player = player,
                        isCurrent = player.id == lobby.currentPlayerId,
                        progress = lobby.mediaStatus.progressForPlayer(player.id),
                        isUploadingCurrent = player.id == lobby.currentPlayerId && state.isUploadingPhotos,
                        uploadCompletedPhotoCount = state.uploadCompletedPhotoCount,
                        uploadTotalPhotoCount = state.pendingPhotoCount,
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerRow(
    player: Player,
    isCurrent: Boolean,
    progress: PlayerMediaProgress?,
    isUploadingCurrent: Boolean,
    uploadCompletedPhotoCount: Int,
    uploadTotalPhotoCount: Int,
    modifier: Modifier = Modifier,
) {
    val accent = when {
        isCurrent -> Sky
        player.isHost -> Mint
        else -> Sky
    }
    PlayerGlassCard(
        nickname = player.nickname,
        meta = playerMetaText(
            player = player,
            isCurrent = isCurrent,
            progress = progress,
            isUploadingCurrent = isUploadingCurrent,
            uploadCompletedPhotoCount = uploadCompletedPhotoCount,
            uploadTotalPhotoCount = uploadTotalPhotoCount,
        ),
        score = player.score,
        accent = accent,
        modifier = modifier,
    )
}

@Composable
private fun LobbyActions(
    state: GuessRollUiState,
    lobby: LobbySnapshot,
    onStartGame: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val canStart = canStartGame(lobby, state)
        if (canStart) {
            PrimaryButton(
                text = startButtonText(lobby),
                enabled = true,
                onClick = onStartGame,
            )
            Spacer(modifier = Modifier.height(8.dp))
        } else if (shouldShowBottomStartStatus(lobby)) {
            DisabledStartState(lobby = lobby)
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (state.isLoading) {
            Spacer(modifier = Modifier.height(14.dp))
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = Amber,
            )
        }
    }
}

@Composable
private fun DisabledStartState(lobby: LobbySnapshot) {
    val title = startButtonText(lobby)
    val reason = when {
        lobby.room.status == RoomStatus.Lobby && !lobby.isCurrentPlayerHost -> {
            "Хост запустит игру, когда будет готов."
        }
        lobby.room.status == RoomStatus.Lobby -> readinessText(lobby)
        else -> "Игра сейчас не в состоянии запуска."
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .motionContentSize()
            .semantics(mergeDescendants = true) {
                contentDescription = "$title. $reason"
                stateDescription = "Старт недоступен"
            }
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = title,
            color = TextMuted,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
        Text(
            text = reason,
            color = TextMuted,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private fun shouldShowBottomStartStatus(lobby: LobbySnapshot): Boolean {
    return lobby.room.status != RoomStatus.Lobby ||
        !lobby.isCurrentPlayerHost ||
        !lobby.mediaStatus.readiness.canStart
}

@Composable
private fun BlindPhotoConsentDialog(
    sourceMode: PhotoSourceMode,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
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
                        Column {
                            SectionTitle("Случайный выбор", sourceMode.label, color = Sky)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Фото попадут в игру вслепую",
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "GuessRoll выберет случайные фото. Точные кадры откроются только в раундах.",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ConsentLine("ты не увидишь выбранные кадры заранее")
                        ConsentLine(sourceMode.helperText.lowercase())
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    PrimaryButton(
                        text = "Разрешить доступ",
                        onClick = onConfirm,
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SecondaryButton(
                        text = "Отмена",
                        onClick = onDismiss,
                    )
                }
            }
        }
    }
}

@Composable
private fun ConsentLine(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(Sky.copy(alpha = 0.68f), RoundedCornerShape(99.dp)),
        )
        Text(text = text, color = TextMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun EmptyPlayersState() {
    SubtleCard {
        Text(
            text = "Пока никого нет. Поделись кодом комнаты.",
            color = TextMuted,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

private fun readinessText(lobby: LobbySnapshot): String {
    return lobby.mediaStatus.readiness.message
}

private fun startButtonText(lobby: LobbySnapshot): String {
    return when {
        lobby.room.status == RoomStatus.Playing -> "Войти в игру"
        lobby.room.status == RoomStatus.Finished -> "Игра завершена"
        !lobby.isCurrentPlayerHost -> "Ждём хоста"
        lobby.mediaStatus.readiness.canStart -> "Начать игру"
        lobby.players.size < 2 -> "Нужно минимум 2 игрока"
        else -> "Ждём фото от всех"
    }
}

private fun canStartGame(lobby: LobbySnapshot, state: GuessRollUiState): Boolean {
    if (lobby.room.status == RoomStatus.Playing) {
        return !state.isLoading && !state.isUploadingPhotos && !state.isScanningGallery
    }
    return lobby.isCurrentPlayerHost &&
        lobby.room.status == RoomStatus.Lobby &&
        lobby.mediaStatus.readiness.canStart &&
        !state.isLoading &&
        !state.isUploadingPhotos &&
        !state.isScanningGallery
}

private fun uploadProgressText(state: GuessRollUiState): String {
    val total = state.pendingPhotoCount
    val completed = state.uploadCompletedPhotoCount.coerceIn(0, total.coerceAtLeast(0))
    return if (total > 0) {
        "Загружаем $completed/$total."
    } else {
        state.blindPhotoMessage ?: "Загружаем случайные фото..."
    }
}

private fun photoSourceHelper(
    mode: PhotoSourceMode,
    preloadStatus: GalleryPreloadStatus,
): String {
    return when (preloadStatus.phase) {
        GalleryPreloadPhase.Loading -> "Готовим список фото заранее..."
        GalleryPreloadPhase.Ready -> "Список фото готов."
        GalleryPreloadPhase.Error,
        GalleryPreloadPhase.Idle -> mode.helperText
    }
}

private fun playerMetaText(
    player: Player,
    isCurrent: Boolean,
    progress: PlayerMediaProgress?,
    isUploadingCurrent: Boolean,
    uploadCompletedPhotoCount: Int,
    uploadTotalPhotoCount: Int,
): String {
    val role = when {
        isCurrent && player.isHost -> "ты / хост"
        isCurrent -> "ты"
        player.isHost -> "хост"
        else -> "игрок"
    }
    val status = when {
        isUploadingCurrent && uploadTotalPhotoCount > 0 -> {
            val completed = uploadCompletedPhotoCount.coerceIn(0, uploadTotalPhotoCount)
            "загружает $completed/$uploadTotalPhotoCount"
        }
        progress == null -> "0 фото"
        progress.isReady -> "готов"
        else -> "${progress.displayProgress} фото"
    }
    return "$role · $status"
}

private val PhotoSourceMode.label: String
    get() = when (this) {
        PhotoSourceMode.PHOTOS_ONLY -> "Только фото"
        PhotoSourceMode.ALL_GALLERY -> "Вся галерея"
    }

private val PhotoSourceMode.helperText: String
    get() = when (this) {
        PhotoSourceMode.PHOTOS_ONLY -> "Скрины стараемся пропускать."
        PhotoSourceMode.ALL_GALLERY -> "Могут попасться скрины и сохранёнки."
    }
