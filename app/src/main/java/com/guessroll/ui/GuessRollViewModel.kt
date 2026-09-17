package com.guessroll.ui

import android.content.Context
import android.net.Uri
import android.util.Log
import com.guessroll.data.media.BlindRandomPhotoUploadPlanner
import com.guessroll.data.media.LocalPhotoCandidate
import com.guessroll.data.media.MediaStorePhotoSource
import com.guessroll.data.media.PhotoSourceMode
import com.guessroll.data.media.PhotoUriReader
import com.guessroll.data.media.RandomPhotoSelector
import com.guessroll.data.supabase.PhotoUploadData
import com.guessroll.data.local.LocalFeedbackSettingsStore
import com.guessroll.data.local.LocalWinStreakStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.guessroll.domain.game.GameRoundSnapshot
import com.guessroll.data.supabase.RoomRepository
import com.guessroll.domain.game.LobbySnapshot
import com.guessroll.domain.game.LocalProfileStats
import com.guessroll.domain.game.LocalWinStreak
import com.guessroll.domain.game.NicknameValidation
import com.guessroll.domain.game.NicknameValidator
import com.guessroll.domain.game.ReactionRules
import com.guessroll.domain.game.ResultsSnapshot
import com.guessroll.domain.game.RoomStatus
import com.guessroll.domain.invite.RoomInviteParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

enum class GalleryPreloadPhase {
    Idle,
    Loading,
    Ready,
    Error,
}

data class GalleryPreloadStatus(
    val phase: GalleryPreloadPhase = GalleryPreloadPhase.Idle,
    val candidateCount: Int = 0,
    val message: String? = null,
) {
    val isLoading: Boolean
        get() = phase == GalleryPreloadPhase.Loading
}

data class GuessRollUiState(
    val nicknameInput: String = "",
    val nickname: String? = null,
    val roomCodeInput: String = "",
    val selectedRoundCount: Int = 8,
    val photoSourceMode: PhotoSourceMode = PhotoSourceMode.PHOTOS_ONLY,
    val lobby: LobbySnapshot? = null,
    val game: GameRoundSnapshot? = null,
    val results: ResultsSnapshot? = null,
    val isLoading: Boolean = false,
    val isEarlyPhotoPrepConsentVisible: Boolean = false,
    val isBlindPhotoConsentVisible: Boolean = false,
    val isScanningGallery: Boolean = false,
    val isUploadingPhotos: Boolean = false,
    val isSubmittingGuess: Boolean = false,
    val isAdvancingRound: Boolean = false,
    val isStartingRematch: Boolean = false,
    val pendingPhotoCount: Int = 0,
    val uploadCompletedPhotoCount: Int = 0,
    val selectedGuessPlayerId: String? = null,
    val localWinStreak: Int = 0,
    val localBestWinStreak: Int = 0,
    val localProfileStats: LocalProfileStats = LocalProfileStats.Empty,
    val soundEnabled: Boolean = false,
    val musicEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val pendingInviteRoomCode: String? = null,
    val galleryPreloadStatus: GalleryPreloadStatus = GalleryPreloadStatus(),
    val errorMessage: String? = null,
    val galleryErrorMessage: String? = null,
    val uploadErrorMessage: String? = null,
    val gameErrorMessage: String? = null,
    val blindPhotoMessage: String? = null,
)

class GuessRollViewModel(
    private val repository: RoomRepository,
    private val localWinStreakStore: LocalWinStreakStore? = null,
    private val localFeedbackSettingsStore: LocalFeedbackSettingsStore? = null,
) : ViewModel() {
    private val _uiState = MutableStateFlow(GuessRollUiState())
    val uiState: StateFlow<GuessRollUiState> = _uiState
    private var pendingPhotoUpload: List<PhotoUploadData> = emptyList()
    private var pendingBlindPhotoSelection: List<LocalPhotoCandidate> = emptyList()
    private var cachedGalleryCandidates: List<LocalPhotoCandidate>? = null
    private var galleryPreloadJob: Job? = null
    private var lastReactionSentAtMillis: Long? = null

    init {
        val settings = localFeedbackSettingsStore?.get()
        _uiState.update {
            it.copy(
                soundEnabled = settings?.soundEnabled ?: it.soundEnabled,
                musicEnabled = settings?.musicEnabled ?: it.musicEnabled,
                hapticsEnabled = settings?.hapticsEnabled ?: it.hapticsEnabled,
            )
        }
    }

    fun observeRoomChanges(roomId: String): Flow<Unit> = repository.observeRoomChanges(roomId)

    fun updateNickname(value: String) {
        val stats = profileStatsFor(value)
        _uiState.update {
            it.copy(
                nicknameInput = value,
                localWinStreak = stats.currentStreak,
                localBestWinStreak = stats.bestStreak,
                localProfileStats = stats,
                errorMessage = null,
            )
        }
    }

    fun confirmNickname(): Boolean {
        return when (val validation = NicknameValidator.validate(_uiState.value.nicknameInput)) {
            is NicknameValidation.Valid -> {
                val stats = profileStatsFor(validation.value)
                _uiState.update {
                    it.copy(
                        nickname = validation.value,
                        nicknameInput = validation.value,
                        localWinStreak = stats.currentStreak,
                        localBestWinStreak = stats.bestStreak,
                        localProfileStats = stats,
                        errorMessage = null,
                    )
                }
                true
            }

            is NicknameValidation.Invalid -> {
                _uiState.update { it.copy(errorMessage = validation.message) }
                false
            }
        }
    }

    fun showEarlyPhotoPrepConsent() {
        _uiState.update {
            it.copy(
                isEarlyPhotoPrepConsentVisible = true,
                errorMessage = null,
            )
        }
    }

    fun dismissEarlyPhotoPrepConsent() {
        _uiState.update { it.copy(isEarlyPhotoPrepConsentVisible = false) }
    }

    fun preloadGalleryCandidates(context: Context, forceRefresh: Boolean = false) {
        if (!forceRefresh && cachedGalleryCandidates != null) return
        if (galleryPreloadJob?.isActive == true) return

        galleryPreloadJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    galleryPreloadStatus = GalleryPreloadStatus(
                        phase = GalleryPreloadPhase.Loading,
                        message = "Готовим список фото заранее...",
                    ),
                )
            }
            loadGalleryCandidates(
                context = context.applicationContext,
                forceRefresh = forceRefresh,
                waitForActivePreload = false,
            )
        }
    }

    fun setRoundCount(value: Int) {
        _uiState.update {
            it.copy(selectedRoundCount = value.coerceIn(MinRounds, MaxRounds))
        }
    }

    fun updateRoomCode(value: String) {
        _uiState.update {
            it.copy(
                roomCodeInput = value.uppercase().filter { char -> char.isLetterOrDigit() }.take(8),
                errorMessage = null,
            )
        }
    }

    fun applyRoomInvite(rawValue: String): Boolean {
        val roomCode = RoomInviteParser.parseRoomCode(rawValue)
        if (roomCode == null) {
            showError("QR не похож на код комнаты GuessRoll.")
            return false
        }
        updateRoomCode(roomCode)
        return true
    }

    fun applyIncomingInvite(rawValue: String): Boolean {
        val roomCode = RoomInviteParser.parseRoomCode(rawValue)
        if (roomCode == null) {
            showError("Ссылка не похожа на приглашение GuessRoll.")
            return false
        }
        _uiState.update {
            it.copy(
                roomCodeInput = roomCode,
                pendingInviteRoomCode = roomCode,
                errorMessage = null,
            )
        }
        return true
    }

    fun consumePendingInviteRoute(): Boolean {
        val roomCode = _uiState.value.pendingInviteRoomCode ?: return false
        _uiState.update {
            it.copy(
                roomCodeInput = roomCode,
                pendingInviteRoomCode = null,
                errorMessage = null,
            )
        }
        return true
    }

    fun showInviteScanError(message: String) {
        showError(message)
    }

    fun createRoom(onCreated: () -> Unit) {
        val nickname = _uiState.value.nickname ?: return showError("Сначала введи ник.")
        launchRoomOperation(
            failureMessage = "Не удалось создать комнату. Попробуй ещё раз.",
        ) {
            repository.createRoom(
                nickname = nickname,
                roundCount = _uiState.value.selectedRoundCount,
            )
        }.invokeOnCompletion { error ->
            if (error == null && _uiState.value.lobby != null) {
                onCreated()
            }
        }
    }

    fun joinRoom(onJoined: () -> Unit) {
        val nickname = _uiState.value.nickname ?: return showError("Сначала введи ник.")
        val roomCode = _uiState.value.roomCodeInput.trim()
        if (roomCode.length < 4) {
            showError("Введи код комнаты.")
            return
        }

        launchRoomOperation(
            failureMessage = "Не удалось войти в комнату. Проверь код и попробуй ещё раз.",
        ) {
            repository.joinRoom(
                nickname = nickname,
                roomCode = roomCode,
            )
        }.invokeOnCompletion { error ->
            if (error == null && _uiState.value.lobby != null) {
                onJoined()
            }
        }
    }

    fun refreshLobby() {
        val lobby = _uiState.value.lobby ?: return
        launchRoomOperation(
            failureMessage = "Не удалось обновить комнату. Попробуй ещё раз.",
        ) {
            repository.refreshLobby(
                roomId = lobby.room.id,
                currentPlayerId = lobby.currentPlayerId,
            )
        }
    }

    fun refreshLobbySilently() {
        val lobby = _uiState.value.lobby ?: return
        viewModelScope.launch {
            repository.refreshLobby(
                roomId = lobby.room.id,
                currentPlayerId = lobby.currentPlayerId,
            )
                .onSuccess { snapshot ->
                    _uiState.update { it.copy(lobby = snapshot) }
                }
        }
    }

    fun startGame(
        onStarted: () -> Unit,
        onFinished: () -> Unit,
    ) {
        val lobby = _uiState.value.lobby ?: return showError("Сначала создай комнату или войди по коду.")
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    gameErrorMessage = null,
                )
            }
            repository.startGame(
                roomId = lobby.room.id,
                currentPlayerId = lobby.currentPlayerId,
            )
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            game = snapshot,
                            results = null,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                    if (snapshot.isFinished) {
                        loadResults(lobby.room.id, lobby.currentPlayerId, onFinished)
                    } else {
                        onStarted()
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Не удалось начать игру.",
                        )
                    }
                }
        }
    }

    fun startRematch(onStarted: () -> Unit) {
        val results = _uiState.value.results ?: return showGameError("Результаты ещё не загружены.")
        if (_uiState.value.isStartingRematch || _uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isStartingRematch = true,
                    isLoading = true,
                    gameErrorMessage = null,
                )
            }
            repository.startRematch(
                roomId = results.room.id,
                currentPlayerId = results.currentPlayerId,
            )
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            game = snapshot,
                            results = null,
                            isStartingRematch = false,
                            isLoading = false,
                            gameErrorMessage = null,
                        )
                    }
                    onStarted()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isStartingRematch = false,
                            isLoading = false,
                            gameErrorMessage = throwable.message ?: "Не удалось запустить реванш.",
                        )
                    }
                }
        }
    }

    fun refreshGame(onFinished: () -> Unit) {
        val game = _uiState.value.game ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, gameErrorMessage = null) }
            repository.refreshGame(
                roomId = game.room.id,
                currentPlayerId = game.currentPlayerId,
            )
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            game = snapshot,
                            results = null,
                            isLoading = false,
                        )
                    }
                    if (snapshot.isFinished) {
                        loadResults(snapshot.room.id, snapshot.currentPlayerId, onFinished)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            gameErrorMessage = throwable.message ?: "Не удалось обновить раунд.",
                        )
                    }
                }
        }
    }

    fun refreshGameSilently(onFinished: () -> Unit) {
        val game = _uiState.value.game ?: return
        viewModelScope.launch {
            repository.refreshGame(
                roomId = game.room.id,
                currentPlayerId = game.currentPlayerId,
            )
                .onSuccess { snapshot ->
                    _uiState.update { it.copy(game = snapshot, results = null) }
                    if (snapshot.isFinished) {
                        loadResults(snapshot.room.id, snapshot.currentPlayerId, onFinished)
                    }
                }
        }
    }

    fun openGameFromLobby(
        onStarted: () -> Unit,
        onFinished: () -> Unit,
    ) {
        val lobby = _uiState.value.lobby ?: return
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, gameErrorMessage = null) }
            repository.refreshGame(
                roomId = lobby.room.id,
                currentPlayerId = lobby.currentPlayerId,
            )
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            game = snapshot,
                            results = null,
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
                    if (snapshot.isFinished) {
                        loadResults(snapshot.room.id, snapshot.currentPlayerId, onFinished)
                    } else {
                        onStarted()
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: "Не удалось открыть игру.",
                        )
                    }
                }
        }
    }

    fun submitGuess(guessedPlayerId: String) {
        val game = _uiState.value.game ?: return
        val round = game.currentRound ?: return showGameError("Раунд не найден.")
        if (game.currentPlayerGuess != null) {
            showGameError("Ты уже ответил в этом раунде.")
            return
        }
        if (game.isCurrentRoundRevealed) {
            showGameError("Раунд уже открыт.")
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmittingGuess = true,
                    selectedGuessPlayerId = guessedPlayerId,
                    gameErrorMessage = null,
                )
            }
            repository.submitGuess(
                roomId = game.room.id,
                currentPlayerId = game.currentPlayerId,
                roundId = round.id,
                guessedPlayerId = guessedPlayerId,
            )
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            game = snapshot,
                            isSubmittingGuess = false,
                            selectedGuessPlayerId = null,
                            gameErrorMessage = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSubmittingGuess = false,
                            selectedGuessPlayerId = null,
                            gameErrorMessage = throwable.message ?: "Не удалось отправить ответ.",
                        )
                    }
                }
        }
    }

    fun sendReaction(emoji: String) {
        val game = _uiState.value.game ?: return
        val round = game.currentRound ?: return
        val nowMillis = System.currentTimeMillis()
        if (!ReactionRules.isAllowed(emoji)) return
        if (!ReactionRules.canSend(lastReactionSentAtMillis, nowMillis)) {
            showGameError("Р РµР°РєС†РёРё РјРѕР¶РЅРѕ РѕС‚РїСЂР°РІР»СЏС‚СЊ С‡СѓС‚СЊ СЂРµР¶Рµ.")
            return
        }

        lastReactionSentAtMillis = nowMillis
        viewModelScope.launch {
            repository.sendReaction(
                roomId = game.room.id,
                currentPlayerId = game.currentPlayerId,
                roundId = round.id,
                emoji = emoji,
            )
                .onSuccess {
                    repository.refreshGame(game.room.id, game.currentPlayerId)
                        .onSuccess { snapshot ->
                            _uiState.update { state -> state.copy(game = snapshot) }
                        }
                }
                .onFailure { throwable ->
                    lastReactionSentAtMillis = null
                    _uiState.update {
                        it.copy(gameErrorMessage = throwable.message ?: "РќРµ СѓРґР°Р»РѕСЃСЊ РѕС‚РїСЂР°РІРёС‚СЊ СЂРµР°РєС†РёСЋ.")
                    }
                }
        }
    }

    fun advanceRound(onFinished: () -> Unit) {
        val game = _uiState.value.game ?: return
        if (!game.isCurrentPlayerHost) {
            showGameError("Следующий раунд переключает хост.")
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isAdvancingRound = true,
                    gameErrorMessage = null,
                )
            }
            repository.advanceRound(
                roomId = game.room.id,
                currentPlayerId = game.currentPlayerId,
            )
                .onSuccess { snapshot ->
                    _uiState.update {
                        it.copy(
                            game = snapshot,
                            isAdvancingRound = false,
                        )
                    }
                    if (snapshot.isFinished) {
                        loadResults(snapshot.room.id, snapshot.currentPlayerId, onFinished)
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isAdvancingRound = false,
                            gameErrorMessage = throwable.message ?: "Не удалось перейти дальше.",
                        )
                    }
                }
        }
    }

    fun showResults(onLoaded: () -> Unit) {
        val game = _uiState.value.game
        val lobby = _uiState.value.lobby
        val roomId = game?.room?.id ?: lobby?.room?.id ?: return showError("Комната не найдена.")
        val playerId = game?.currentPlayerId ?: lobby?.currentPlayerId ?: return showError("Игрок не найден.")
        loadResults(roomId, playerId, onLoaded)
    }

    fun refreshResultsOrOpenGame(onGameStarted: () -> Unit) {
        val results = _uiState.value.results ?: return
        if (_uiState.value.isLoading || _uiState.value.isStartingRematch) return
        viewModelScope.launch {
            repository.refreshGame(
                roomId = results.room.id,
                currentPlayerId = results.currentPlayerId,
            )
                .onSuccess { snapshot ->
                    if (!snapshot.isFinished) {
                        _uiState.update {
                            it.copy(
                                game = snapshot,
                                results = null,
                                gameErrorMessage = null,
                            )
                        }
                        onGameStarted()
                    } else {
                        repository.fetchResults(results.room.id, results.currentPlayerId)
                            .onSuccess { refreshedResults ->
                                val stats = applyLocalProfileStats(refreshedResults)
                                _uiState.update {
                                    it.copy(
                                        results = refreshedResults,
                                        localWinStreak = stats.currentStreak,
                                        localBestWinStreak = stats.bestStreak,
                                        localProfileStats = stats,
                                    )
                                }
                            }
                    }
                }
        }
    }

    fun returnHome() {
        _uiState.update {
            it.copy(
                lobby = null,
                game = null,
                results = null,
                roomCodeInput = "",
                errorMessage = null,
                galleryErrorMessage = null,
                uploadErrorMessage = null,
                gameErrorMessage = null,
                isStartingRematch = false,
                blindPhotoMessage = null,
                selectedGuessPlayerId = null,
                uploadCompletedPhotoCount = 0,
            )
        }
        pendingBlindPhotoSelection = emptyList()
    }

    fun showBlindPhotoConsent() {
        val lobby = _uiState.value.lobby ?: return showGalleryError("Сначала создай комнату или войди по коду.")
        if (lobby.room.status != RoomStatus.Lobby) {
            showGalleryError("Фото можно добавлять только до старта игры.")
            return
        }
        _uiState.update {
            it.copy(
                isBlindPhotoConsentVisible = true,
                galleryErrorMessage = null,
                uploadErrorMessage = null,
                blindPhotoMessage = null,
            )
        }
    }

    fun dismissBlindPhotoConsent() {
        _uiState.update { it.copy(isBlindPhotoConsentVisible = false) }
    }

    fun setPhotoSourceMode(mode: PhotoSourceMode) {
        _uiState.update {
            it.copy(
                photoSourceMode = mode,
                galleryErrorMessage = null,
                uploadErrorMessage = null,
                blindPhotoMessage = null,
            )
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        val settings = localFeedbackSettingsStore?.setSoundEnabled(enabled)
        _uiState.update {
            it.copy(soundEnabled = settings?.soundEnabled ?: enabled)
        }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        val settings = localFeedbackSettingsStore?.setHapticsEnabled(enabled)
        _uiState.update {
            it.copy(hapticsEnabled = settings?.hapticsEnabled ?: enabled)
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        val settings = localFeedbackSettingsStore?.setMusicEnabled(enabled)
        _uiState.update {
            it.copy(musicEnabled = settings?.musicEnabled ?: enabled)
        }
    }

    private suspend fun loadGalleryCandidates(
        context: Context,
        forceRefresh: Boolean = false,
        waitForActivePreload: Boolean = true,
    ): Result<List<LocalPhotoCandidate>> {
        if (!forceRefresh) {
            cachedGalleryCandidates?.let { return Result.success(it) }
            if (waitForActivePreload) {
                galleryPreloadJob?.takeIf { it.isActive }?.join()
            }
            cachedGalleryCandidates?.let { return Result.success(it) }
        }

        _uiState.update {
            it.copy(
                galleryPreloadStatus = GalleryPreloadStatus(
                    phase = GalleryPreloadPhase.Loading,
                    message = "Готовим список фото заранее...",
                ),
            )
        }

        return MediaStorePhotoSource.scanImages(context.applicationContext)
            .onSuccess { candidates ->
                cachedGalleryCandidates = candidates
                _uiState.update {
                    it.copy(
                        galleryPreloadStatus = GalleryPreloadStatus(
                            phase = GalleryPreloadPhase.Ready,
                            candidateCount = candidates.size,
                            message = "Список фото готов.",
                        ),
                    )
                }
            }
            .onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        galleryPreloadStatus = GalleryPreloadStatus(
                            phase = GalleryPreloadPhase.Error,
                            message = throwable.message ?: "Не удалось подготовить список фото.",
                        ),
                    )
                }
            }
    }

    fun startBlindRandomPhotoUpload(context: Context) {
        val lobby = _uiState.value.lobby ?: return showGalleryError("Сначала создай комнату или войди по коду.")
        if (_uiState.value.isScanningGallery || _uiState.value.isUploadingPhotos) return
        if (lobby.room.status != RoomStatus.Lobby) {
            showGalleryError("Фото можно добавлять только до старта игры.")
            return
        }
        val sourceMode = _uiState.value.photoSourceMode

        viewModelScope.launch {
            pendingBlindPhotoSelection = emptyList()
            _uiState.update {
                it.copy(
                    isBlindPhotoConsentVisible = false,
                    isScanningGallery = true,
                    isUploadingPhotos = false,
                    pendingPhotoCount = 0,
                    uploadCompletedPhotoCount = 0,
                    galleryErrorMessage = null,
                    uploadErrorMessage = null,
                    blindPhotoMessage = sourceMode.scanningMessage(),
                )
            }

            loadGalleryCandidates(context.applicationContext)
                .onSuccess { candidates ->
                    val plan = BlindRandomPhotoUploadPlanner.plan(
                        candidates = candidates,
                        maxCount = lobby.room.roundCount,
                        sourceMode = sourceMode,
                    )
                    if (!plan.canUpload) {
                        val allGallerySupportedCount = RandomPhotoSelector.supportedCount(
                            candidates = candidates,
                            sourceMode = PhotoSourceMode.ALL_GALLERY,
                        )
                        _uiState.update {
                            it.copy(
                                isScanningGallery = false,
                                blindPhotoMessage = null,
                                galleryErrorMessage = if (candidates.isEmpty()) {
                                    "Фото не найдены. Можно выбрать фото вручную."
                                } else if (sourceMode == PhotoSourceMode.PHOTOS_ONLY && allGallerySupportedCount > 0) {
                                    "Нашлось мало обычных фото. Можно попробовать режим «Вся галерея»."
                                } else {
                                    plan.publicSummary.message + " Можно выбрать фото вручную."
                                },
                            )
                        }
                        return@onSuccess
                    }

                    if (plan.photosForUpload.size < lobby.room.roundCount) {
                        val allGallerySupportedCount = RandomPhotoSelector.supportedCount(
                            candidates = candidates,
                            sourceMode = PhotoSourceMode.ALL_GALLERY,
                        )
                        _uiState.update {
                            it.copy(
                                isScanningGallery = false,
                                pendingPhotoCount = 0,
                                uploadCompletedPhotoCount = 0,
                                blindPhotoMessage = null,
                                galleryErrorMessage = if (sourceMode == PhotoSourceMode.PHOTOS_ONLY && allGallerySupportedCount >= lobby.room.roundCount) {
                                    "Нашлось мало обычных фото: ${plan.photosForUpload.size}/${lobby.room.roundCount}. Можно попробовать режим «Вся галерея»."
                                } else {
                                    "Нужно ${lobby.room.roundCount} фото для этой комнаты, найдено ${plan.photosForUpload.size}. Можно попробовать «Вся галерея» или выбрать фото вручную."
                                },
                            )
                        }
                        return@onSuccess
                    }

                    pendingBlindPhotoSelection = plan.photosForUpload
                    _uiState.update {
                        it.copy(
                            isScanningGallery = false,
                            isUploadingPhotos = true,
                            pendingPhotoCount = plan.publicSummary.selectedCount,
                            blindPhotoMessage = sourceMode.uploadingMessage(plan.publicSummary.selectedCount),
                        )
                    }

                    PhotoUriReader.readLocalPhotos(
                        context = context.applicationContext,
                        photos = plan.photosForUpload,
                    )
                        .onSuccess { uploadData ->
                            uploadPhotoBytes(
                                photos = uploadData,
                                onUploaded = {
                                    pendingBlindPhotoSelection = emptyList()
                                    _uiState.update {
                                        it.copy(blindPhotoMessage = null)
                                    }
                                },
                            )
                        }
                        .onFailure { throwable ->
                            pendingBlindPhotoSelection = emptyList()
                            _uiState.update {
                                it.copy(
                                    isUploadingPhotos = false,
                                    pendingPhotoCount = 0,
                                    uploadCompletedPhotoCount = 0,
                                    blindPhotoMessage = null,
                                    uploadErrorMessage = throwable.message
                                        ?: "Не удалось прочитать случайные фото.",
                                )
                            }
                        }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isScanningGallery = false,
                            blindPhotoMessage = null,
                            galleryErrorMessage = throwable.message
                                ?: "Не удалось прочитать галерею. Можно выбрать фото вручную.",
                        )
                    }
                }
        }
    }

    fun showGalleryPermissionDenied() {
        _uiState.update {
            it.copy(
                isBlindPhotoConsentVisible = false,
                isScanningGallery = false,
                blindPhotoMessage = null,
                galleryErrorMessage = "Доступ к фото не выдан. Можно выбрать фото вручную через системный picker.",
            )
        }
    }

    fun uploadSelectedPhotos(context: Context, uris: List<Uri>) {
        if (_uiState.value.isUploadingPhotos) return
        if (uris.isEmpty()) {
            _uiState.update {
                it.copy(uploadErrorMessage = "Выбери хотя бы одно фото.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploadingPhotos = true,
                    pendingPhotoCount = uris.size,
                    uploadCompletedPhotoCount = 0,
                    blindPhotoMessage = null,
                    galleryErrorMessage = null,
                    uploadErrorMessage = null,
                )
            }

            PhotoUriReader.readSelectedPhotos(context.applicationContext, uris)
                .onSuccess { photos ->
                    uploadPhotoBytes(photos)
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isUploadingPhotos = false,
                            pendingPhotoCount = 0,
                            uploadCompletedPhotoCount = 0,
                            uploadErrorMessage = throwable.message ?: "Не удалось прочитать выбранные фото.",
                        )
                    }
                }
        }
    }

    fun retryPhotoUpload() {
        val photos = pendingPhotoUpload
        if (photos.isEmpty()) {
            _uiState.update { it.copy(uploadErrorMessage = "Выбери фото заново, чтобы повторить.") }
            return
        }
        uploadPhotoBytes(photos)
    }

    fun clearError() {
        _uiState.update {
            it.copy(
                errorMessage = null,
                galleryErrorMessage = null,
                uploadErrorMessage = null,
                gameErrorMessage = null,
                blindPhotoMessage = null,
            )
        }
    }

    fun clearTransientErrors(
        preservePhotoPrep: Boolean = false,
        preserveGame: Boolean = false,
    ) {
        _uiState.update {
            it.copy(
                errorMessage = null,
                galleryErrorMessage = if (preservePhotoPrep) it.galleryErrorMessage else null,
                uploadErrorMessage = if (preservePhotoPrep) it.uploadErrorMessage else null,
                blindPhotoMessage = if (preservePhotoPrep) it.blindPhotoMessage else null,
                gameErrorMessage = if (preserveGame) it.gameErrorMessage else null,
            )
        }
    }

    private fun launchRoomOperation(
        failureMessage: String,
        operation: suspend () -> Result<LobbySnapshot>,
    ) = viewModelScope.launch {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
            )
        }

        operation()
            .onSuccess { snapshot ->
                _uiState.update {
                    it.copy(
                        lobby = snapshot,
                        isLoading = false,
                        errorMessage = null,
                        pendingInviteRoomCode = null,
                    )
                }
            }
            .onFailure { throwable ->
                Log.e(RoomOperationLogTag, "Room operation failed", throwable)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = failureMessage,
                    )
                }
            }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }

    private fun showGalleryError(message: String) {
        _uiState.update { it.copy(galleryErrorMessage = message) }
    }

    private fun showGameError(message: String) {
        _uiState.update { it.copy(gameErrorMessage = message) }
    }

    private fun streakFor(nickname: String): LocalWinStreak {
        return profileStatsFor(nickname).toWinStreak()
    }

    private fun profileStatsFor(nickname: String): LocalProfileStats {
        return localWinStreakStore?.getStats(nickname) ?: LocalProfileStats.Empty
    }

    private fun applyLocalProfileStats(results: ResultsSnapshot): LocalProfileStats {
        val nickname = results.currentPlayer?.nickname ?: _uiState.value.nickname.orEmpty()
        val sessionId = results.session?.id ?: return profileStatsFor(nickname)
        val currentEntry = results.leaderboard.firstOrNull { it.player.id == results.currentPlayerId }
        val wonOrTiedForFirst = currentEntry?.rank == 1
        val currentPlayerGuesses = results.sessionGuesses.filter { guess -> guess.playerId == results.currentPlayerId }
        return localWinStreakStore?.applyProfileGameResult(
            nickname = nickname,
            sessionId = sessionId,
            wonOrTiedForFirst = wonOrTiedForFirst,
            correctGuesses = currentPlayerGuesses.count { it.isCorrect },
            totalGuesses = currentPlayerGuesses.size,
        ) ?: LocalProfileStats.Empty
    }

    private fun loadResults(
        roomId: String,
        currentPlayerId: String,
        onLoaded: () -> Unit,
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    gameErrorMessage = null,
                )
            }
            repository.fetchResults(roomId, currentPlayerId)
                .onSuccess { results ->
                    val stats = applyLocalProfileStats(results)
                    _uiState.update {
                        it.copy(
                            results = results,
                            isLoading = false,
                            localWinStreak = stats.currentStreak,
                            localBestWinStreak = stats.bestStreak,
                            localProfileStats = stats,
                        )
                    }
                    onLoaded()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            gameErrorMessage = throwable.message ?: "Не удалось открыть результаты.",
                        )
                    }
                }
        }
    }

    private fun uploadPhotoBytes(
        photos: List<PhotoUploadData>,
        onUploaded: (() -> Unit)? = null,
    ) {
        val lobby = _uiState.value.lobby
        if (lobby == null) {
            _uiState.update {
                it.copy(
                    isUploadingPhotos = false,
                    pendingPhotoCount = 0,
                    uploadCompletedPhotoCount = 0,
                    uploadErrorMessage = "Сначала создай комнату или войди по коду.",
                )
            }
            return
        }

        pendingPhotoUpload = photos
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isUploadingPhotos = true,
                    pendingPhotoCount = photos.size,
                    uploadCompletedPhotoCount = 0,
                    uploadErrorMessage = null,
                    galleryErrorMessage = null,
                )
            }

            repository.uploadPhotos(
                roomId = lobby.room.id,
                playerId = lobby.currentPlayerId,
                photos = photos,
                onProgress = { progress ->
                    _uiState.update {
                        it.copy(
                            uploadCompletedPhotoCount = progress.safeCompleted,
                            pendingPhotoCount = progress.total,
                            blindPhotoMessage = if (progress.total > 0) {
                                "Загружаем ${progress.safeCompleted}/${progress.total}."
                            } else {
                                it.blindPhotoMessage
                            },
                        )
                    }
                },
            )
                .onSuccess { snapshot ->
                    pendingPhotoUpload = emptyList()
                    _uiState.update {
                        it.copy(
                            lobby = snapshot,
                            isUploadingPhotos = false,
                            pendingPhotoCount = 0,
                            uploadCompletedPhotoCount = 0,
                            uploadErrorMessage = null,
                            galleryErrorMessage = null,
                        )
                    }
                    onUploaded?.invoke()
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isUploadingPhotos = false,
                            uploadCompletedPhotoCount = 0,
                            uploadErrorMessage = throwable.message ?: "Загрузка не удалась. Попробуй ещё раз.",
                        )
                    }
                }
        }
    }

    companion object {
        private const val RoomOperationLogTag = "GuessRollRoom"
        const val MinRounds = 3
        const val MaxRounds = 20
    }
}

private fun PhotoSourceMode.scanningMessage(): String {
    return when (this) {
        PhotoSourceMode.PHOTOS_ONLY -> "Подбираем обычные фото без скринов..."
        PhotoSourceMode.ALL_GALLERY -> "Подбираем кадры из всей галереи..."
    }
}

private fun PhotoSourceMode.uploadingMessage(count: Int): String {
    val sourceText = when (this) {
        PhotoSourceMode.PHOTOS_ONLY -> "обычных фото"
        PhotoSourceMode.ALL_GALLERY -> "кадров из галереи"
    }
    return "Загружаем $count случайных $sourceText. Сами фото не показываем — так веселее."
}
