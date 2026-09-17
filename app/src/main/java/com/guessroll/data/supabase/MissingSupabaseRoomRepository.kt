package com.guessroll.data.supabase

import com.guessroll.domain.game.LobbySnapshot
import com.guessroll.domain.game.GameRoundSnapshot
import com.guessroll.domain.game.ResultsSnapshot

class MissingSupabaseRoomRepository : RoomRepository {
    override suspend fun createRoom(nickname: String, roundCount: Int): Result<LobbySnapshot> {
        return Result.failure(missingConfigError())
    }

    override suspend fun joinRoom(nickname: String, roomCode: String): Result<LobbySnapshot> {
        return Result.failure(missingConfigError())
    }

    override suspend fun refreshLobby(roomId: String, currentPlayerId: String): Result<LobbySnapshot> {
        return Result.failure(missingConfigError())
    }

    override suspend fun uploadPhotos(
        roomId: String,
        playerId: String,
        photos: List<PhotoUploadData>,
        onProgress: (PhotoUploadProgress) -> Unit,
    ): Result<LobbySnapshot> {
        return Result.failure(missingConfigError())
    }

    override suspend fun startGame(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot> {
        return Result.failure(missingConfigError())
    }

    override suspend fun startRematch(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot> {
        return Result.failure(missingConfigError())
    }

    override suspend fun refreshGame(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot> {
        return Result.failure(missingConfigError())
    }

    override suspend fun submitGuess(
        roomId: String,
        currentPlayerId: String,
        roundId: String,
        guessedPlayerId: String,
    ): Result<GameRoundSnapshot> {
        return Result.failure(missingConfigError())
    }

    override suspend fun sendReaction(
        roomId: String,
        currentPlayerId: String,
        roundId: String,
        emoji: String,
    ): Result<Unit> {
        return Result.failure(missingConfigError())
    }

    override suspend fun advanceRound(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot> {
        return Result.failure(missingConfigError())
    }

    override suspend fun fetchResults(roomId: String, currentPlayerId: String): Result<ResultsSnapshot> {
        return Result.failure(missingConfigError())
    }

    private fun missingConfigError(): IllegalStateException = IllegalStateException(
        "Supabase не настроен. Добавь SUPABASE_URL и SUPABASE_ANON_KEY в local.properties.",
    )
}
