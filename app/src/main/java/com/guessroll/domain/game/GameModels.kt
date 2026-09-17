package com.guessroll.domain.game

data class GameRoom(
    val id: String,
    val code: String,
    val hostPlayerId: String?,
    val mode: GameMode,
    val roundCount: Int,
    val status: RoomStatus,
    val currentRoundIndex: Int,
    val currentGameSessionId: String?,
)

data class Player(
    val id: String,
    val roomId: String,
    val nickname: String,
    val isHost: Boolean,
    val score: Int,
)

data class PlayerMediaCount(
    val playerId: String,
    val photoCount: Int,
)

data class PlayerMediaProgress(
    val playerId: String,
    val nickname: String,
    val uploadedPhotoCount: Int,
    val requiredPhotoCount: Int,
) {
    val isReady: Boolean
        get() = uploadedPhotoCount >= requiredPhotoCount

    val displayProgress: String
        get() = "${uploadedPhotoCount.coerceAtLeast(0)}/${requiredPhotoCount.coerceAtLeast(0)}"
}

data class LobbyMediaStatus(
    val currentPlayerPhotoCount: Int,
    val playersWithPhotos: Int,
    val totalPlayers: Int,
    val readiness: LobbyReadinessStatus,
    val requiredPhotoCount: Int = 1,
    val playerProgress: List<PlayerMediaProgress> = emptyList(),
) {
    fun progressForPlayer(playerId: String): PlayerMediaProgress? = playerProgress.firstOrNull { it.playerId == playerId }

    companion object {
        val Empty = LobbyMediaStatus(
            currentPlayerPhotoCount = 0,
            playersWithPhotos = 0,
            totalPlayers = 0,
            readiness = LobbyReadiness.evaluate(playerCount = 0, playersWithPhotos = 0),
            requiredPhotoCount = 1,
            playerProgress = emptyList(),
        )
    }
}

data class LobbySnapshot(
    val room: GameRoom,
    val players: List<Player>,
    val currentPlayerId: String,
    val mediaStatus: LobbyMediaStatus = LobbyMediaStatus.Empty,
) {
    val currentPlayer: Player?
        get() = players.firstOrNull { it.id == currentPlayerId }

    val isCurrentPlayerHost: Boolean
        get() = currentPlayer?.isHost == true
}

data class LobbyReadinessStatus(
    val canStart: Boolean,
    val missingPlayerCount: Int,
    val missingPhotoCount: Int,
    val message: String,
)

object LobbyReadiness {
    private const val MinimumPlayers = 2

    fun evaluate(playerCount: Int, playersWithPhotos: Int): LobbyReadinessStatus {
        val missingPlayers = (MinimumPlayers - playerCount).coerceAtLeast(0)
        val missingPhotos = (playerCount - playersWithPhotos).coerceAtLeast(0)
        val canStart = missingPlayers == 0 && missingPhotos == 0
        val message = when {
            missingPlayers > 0 -> "Нужно еще игроков: $missingPlayers."
            missingPhotos > 0 -> "Ждем фото от игроков: $missingPhotos."
            else -> "Фото готовы для следующего шага."
        }

        return LobbyReadinessStatus(
            canStart = canStart,
            missingPlayerCount = missingPlayers,
            missingPhotoCount = missingPhotos,
            message = message,
        )
    }

    fun evaluate(
        playerCount: Int,
        playerProgress: List<PlayerMediaProgress>,
        requiredPhotoCount: Int,
    ): LobbyReadinessStatus {
        val requiredCount = requiredPhotoCount.coerceAtLeast(1)
        val missingPlayers = (MinimumPlayers - playerCount).coerceAtLeast(0)
        val missingProgress = playerProgress.filterNot { it.uploadedPhotoCount >= requiredCount }
        val missingPhotos = missingProgress.sumOf { progress ->
            (requiredCount - progress.uploadedPhotoCount).coerceAtLeast(0)
        }
        val firstMissing = missingProgress.firstOrNull()
        val canStart = missingPlayers == 0 && missingPhotos == 0 && playerProgress.size == playerCount
        val message = when {
            missingPlayers > 0 -> "Нужно минимум 2 игрока."
            firstMissing != null -> "У ${firstMissing.nickname} ${firstMissing.uploadedPhotoCount.coerceAtLeast(0)}/$requiredCount фото."
            else -> "Все игроки загрузили фото для раундов."
        }

        return LobbyReadinessStatus(
            canStart = canStart,
            missingPlayerCount = missingPlayers,
            missingPhotoCount = missingPhotos,
            message = message,
        )
    }
}

enum class GameMode(val wireValue: String) {
    Photo("photo"),
    Video("video");

    companion object {
        fun fromWire(value: String?): GameMode = entries.firstOrNull { it.wireValue == value } ?: Photo
    }
}

enum class RoomStatus(val wireValue: String) {
    Lobby("lobby"),
    Uploading("uploading"),
    Playing("playing"),
    Finished("finished");

    companion object {
        fun fromWire(value: String?): RoomStatus = entries.firstOrNull { it.wireValue == value } ?: Lobby
    }
}

data class GameSession(
    val id: String,
    val roomId: String,
    val status: GameSessionStatus,
    val roundCount: Int,
    val currentRoundIndex: Int,
)

enum class GameSessionStatus(val wireValue: String) {
    Playing("playing"),
    Finished("finished");

    companion object {
        fun fromWire(value: String?): GameSessionStatus = entries.firstOrNull { it.wireValue == value } ?: Playing
    }
}
