package com.guessroll.data.supabase

import com.guessroll.domain.game.LobbySnapshot
import com.guessroll.domain.game.GameRoundSnapshot
import com.guessroll.domain.game.ResultsSnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface RoomRepository {
    fun observeRoomChanges(roomId: String): Flow<Unit> = emptyFlow()

    suspend fun createRoom(nickname: String, roundCount: Int): Result<LobbySnapshot>
    suspend fun joinRoom(nickname: String, roomCode: String): Result<LobbySnapshot>
    suspend fun refreshLobby(roomId: String, currentPlayerId: String): Result<LobbySnapshot>
    suspend fun uploadPhotos(
        roomId: String,
        playerId: String,
        photos: List<PhotoUploadData>,
        onProgress: (PhotoUploadProgress) -> Unit = {},
    ): Result<LobbySnapshot>
    suspend fun startGame(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot>
    suspend fun startRematch(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot>
    suspend fun refreshGame(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot>
    suspend fun submitGuess(
        roomId: String,
        currentPlayerId: String,
        roundId: String,
        guessedPlayerId: String,
    ): Result<GameRoundSnapshot>
    suspend fun sendReaction(
        roomId: String,
        currentPlayerId: String,
        roundId: String,
        emoji: String,
    ): Result<Unit>
    suspend fun advanceRound(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot>
    suspend fun fetchResults(roomId: String, currentPlayerId: String): Result<ResultsSnapshot>
}
