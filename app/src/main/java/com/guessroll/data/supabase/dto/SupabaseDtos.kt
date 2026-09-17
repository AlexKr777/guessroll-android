package com.guessroll.data.supabase.dto

import com.guessroll.domain.game.GameMode
import com.guessroll.domain.game.GameRound
import com.guessroll.domain.game.GameRoom
import com.guessroll.domain.game.GameSession
import com.guessroll.domain.game.GameSessionStatus
import com.guessroll.domain.game.Guess
import com.guessroll.domain.game.MediaItem
import com.guessroll.domain.game.Player
import com.guessroll.domain.game.RoundReaction
import com.guessroll.domain.game.RoundStatus
import com.guessroll.domain.game.RoomStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomDto(
    val id: String,
    val code: String,
    @SerialName("host_player_id")
    val hostPlayerId: String? = null,
    val mode: String = "photo",
    @SerialName("round_count")
    val roundCount: Int,
    val status: String = "lobby",
    @SerialName("current_round_index")
    val currentRoundIndex: Int = 0,
    @SerialName("current_game_session_id")
    val currentGameSessionId: String? = null,
) {
    fun toDomain(): GameRoom = GameRoom(
        id = id,
        code = code,
        hostPlayerId = hostPlayerId,
        mode = GameMode.fromWire(mode),
        roundCount = roundCount,
        status = RoomStatus.fromWire(status),
        currentRoundIndex = currentRoundIndex,
        currentGameSessionId = currentGameSessionId,
    )
}

@Serializable
data class RoomInsertDto(
    val code: String,
    val mode: String = "photo",
    @SerialName("round_count")
    val roundCount: Int,
    val status: String = "lobby",
)

@Serializable
data class RoomHostUpdateDto(
    @SerialName("host_player_id")
    val hostPlayerId: String,
)

@Serializable
data class PlayerDto(
    val id: String,
    @SerialName("room_id")
    val roomId: String,
    val nickname: String,
    @SerialName("is_host")
    val isHost: Boolean = false,
    val score: Int = 0,
) {
    fun toDomain(): Player = Player(
        id = id,
        roomId = roomId,
        nickname = nickname,
        isHost = isHost,
        score = score,
    )
}

@Serializable
data class PlayerInsertDto(
    @SerialName("room_id")
    val roomId: String,
    val nickname: String,
    @SerialName("is_host")
    val isHost: Boolean,
    val score: Int = 0,
)

@Serializable
data class MediaItemDto(
    val id: String,
    @SerialName("room_id")
    val roomId: String,
    @SerialName("owner_player_id")
    val ownerPlayerId: String,
    @SerialName("storage_path")
    val storagePath: String,
    @SerialName("media_type")
    val mediaType: String = "photo",
) {
    fun toDomain(): MediaItem = MediaItem(
        id = id,
        roomId = roomId,
        ownerPlayerId = ownerPlayerId,
        storagePath = storagePath,
        mediaType = GameMode.fromWire(mediaType),
    )
}

@Serializable
data class MediaItemInsertDto(
    val id: String,
    @SerialName("room_id")
    val roomId: String,
    @SerialName("owner_player_id")
    val ownerPlayerId: String,
    @SerialName("storage_path")
    val storagePath: String,
    @SerialName("media_type")
    val mediaType: String = "photo",
)

@Serializable
data class RoomGameStateUpdateDto(
    val status: String,
    @SerialName("current_round_index")
    val currentRoundIndex: Int,
)

@Serializable
data class RoomGameSessionStateUpdateDto(
    val status: String,
    @SerialName("current_round_index")
    val currentRoundIndex: Int,
    @SerialName("current_game_session_id")
    val currentGameSessionId: String,
)

@Serializable
data class GameSessionDto(
    val id: String,
    @SerialName("room_id")
    val roomId: String,
    val status: String = "playing",
    @SerialName("round_count")
    val roundCount: Int,
    @SerialName("current_round_index")
    val currentRoundIndex: Int = 0,
) {
    fun toDomain(): GameSession = GameSession(
        id = id,
        roomId = roomId,
        status = GameSessionStatus.fromWire(status),
        roundCount = roundCount,
        currentRoundIndex = currentRoundIndex,
    )
}

@Serializable
data class GameSessionInsertDto(
    @SerialName("room_id")
    val roomId: String,
    val status: String = "playing",
    @SerialName("round_count")
    val roundCount: Int,
    @SerialName("current_round_index")
    val currentRoundIndex: Int = 0,
)

@Serializable
data class GameSessionStateUpdateDto(
    val status: String,
    @SerialName("current_round_index")
    val currentRoundIndex: Int,
)

@Serializable
data class GameSessionFinishedUpdateDto(
    val status: String = "finished",
    @SerialName("current_round_index")
    val currentRoundIndex: Int,
    @SerialName("ended_at")
    val endedAt: String,
)

@Serializable
data class RoundDto(
    val id: String,
    @SerialName("room_id")
    val roomId: String,
    @SerialName("game_session_id")
    val gameSessionId: String,
    @SerialName("media_item_id")
    val mediaItemId: String,
    @SerialName("correct_player_id")
    val correctPlayerId: String,
    @SerialName("round_number")
    val roundNumber: Int,
    val status: String = "pending",
) {
    fun toDomain(): GameRound = GameRound(
        id = id,
        roomId = roomId,
        gameSessionId = gameSessionId,
        mediaItemId = mediaItemId,
        correctPlayerId = correctPlayerId,
        roundNumber = roundNumber,
        status = RoundStatus.fromWire(status),
    )
}

@Serializable
data class RoundInsertDto(
    @SerialName("room_id")
    val roomId: String,
    @SerialName("game_session_id")
    val gameSessionId: String,
    @SerialName("media_item_id")
    val mediaItemId: String,
    @SerialName("correct_player_id")
    val correctPlayerId: String,
    @SerialName("round_number")
    val roundNumber: Int,
    val status: String = "pending",
)

@Serializable
data class RoundStatusUpdateDto(
    val status: String,
)

@Serializable
data class GuessDto(
    val id: String,
    @SerialName("room_id")
    val roomId: String? = null,
    @SerialName("round_id")
    val roundId: String,
    @SerialName("game_session_id")
    val gameSessionId: String,
    @SerialName("player_id")
    val playerId: String,
    @SerialName("guessed_player_id")
    val guessedPlayerId: String,
    @SerialName("is_correct")
    val isCorrect: Boolean,
) {
    fun toDomain(): Guess = Guess(
        id = id,
        roundId = roundId,
        gameSessionId = gameSessionId,
        playerId = playerId,
        guessedPlayerId = guessedPlayerId,
        isCorrect = isCorrect,
    )
}

@Serializable
data class GuessInsertDto(
    @SerialName("room_id")
    val roomId: String,
    @SerialName("round_id")
    val roundId: String,
    @SerialName("game_session_id")
    val gameSessionId: String,
    @SerialName("player_id")
    val playerId: String,
    @SerialName("guessed_player_id")
    val guessedPlayerId: String,
    @SerialName("is_correct")
    val isCorrect: Boolean,
)

@Serializable
data class RoundReactionDto(
    val id: String,
    @SerialName("room_id")
    val roomId: String,
    @SerialName("game_session_id")
    val gameSessionId: String,
    @SerialName("round_id")
    val roundId: String,
    @SerialName("player_id")
    val playerId: String,
    val emoji: String,
    @SerialName("created_at")
    val createdAt: String,
) {
    fun toDomain(): RoundReaction = RoundReaction(
        id = id,
        roomId = roomId,
        gameSessionId = gameSessionId,
        roundId = roundId,
        playerId = playerId,
        emoji = emoji,
        createdAt = createdAt,
    )
}

@Serializable
data class RoundReactionInsertDto(
    val id: String,
    @SerialName("room_id")
    val roomId: String,
    @SerialName("game_session_id")
    val gameSessionId: String,
    @SerialName("round_id")
    val roundId: String,
    @SerialName("player_id")
    val playerId: String,
    val emoji: String,
)

@Serializable
data class PlayerScoreUpdateDto(
    val score: Int,
)
