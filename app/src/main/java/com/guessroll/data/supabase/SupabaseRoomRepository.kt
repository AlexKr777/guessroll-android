package com.guessroll.data.supabase

import com.guessroll.data.supabase.dto.GuessDto
import com.guessroll.data.supabase.dto.GuessInsertDto
import com.guessroll.data.supabase.dto.GameSessionDto
import com.guessroll.data.supabase.dto.GameSessionFinishedUpdateDto
import com.guessroll.data.supabase.dto.GameSessionInsertDto
import com.guessroll.data.supabase.dto.GameSessionStateUpdateDto
import com.guessroll.data.supabase.dto.MediaItemDto
import com.guessroll.data.supabase.dto.MediaItemInsertDto
import com.guessroll.data.supabase.dto.PlayerDto
import com.guessroll.data.supabase.dto.PlayerInsertDto
import com.guessroll.data.supabase.dto.RoomDto
import com.guessroll.data.supabase.dto.RoomGameStateUpdateDto
import com.guessroll.data.supabase.dto.RoomGameSessionStateUpdateDto
import com.guessroll.data.supabase.dto.RoomHostUpdateDto
import com.guessroll.data.supabase.dto.RoomInsertDto
import com.guessroll.data.supabase.dto.RoundReactionDto
import com.guessroll.data.supabase.dto.RoundReactionInsertDto
import com.guessroll.data.supabase.dto.RoundDto
import com.guessroll.data.supabase.dto.RoundInsertDto
import com.guessroll.data.supabase.dto.RoundStatusUpdateDto
import com.guessroll.domain.game.GameRound
import com.guessroll.domain.game.GameRoundSnapshot
import com.guessroll.domain.game.GameSession
import com.guessroll.domain.game.GameStartBlockReason
import com.guessroll.domain.game.GameStartRules
import com.guessroll.domain.game.GuessScoring
import com.guessroll.domain.game.LeaderboardRanker
import com.guessroll.domain.game.LobbyMediaStatus
import com.guessroll.domain.game.LobbyReadiness
import com.guessroll.domain.game.LobbySnapshot
import com.guessroll.domain.game.MediaItem
import com.guessroll.domain.game.Player
import com.guessroll.domain.game.PlayerMediaProgress
import com.guessroll.domain.game.ReactionRules
import com.guessroll.domain.game.ResultsSnapshot
import com.guessroll.domain.game.RoundAdvanceRules
import com.guessroll.domain.game.RoundRevealRules
import com.guessroll.domain.game.RoundStatus
import com.guessroll.domain.game.RoomCodeGenerator
import com.guessroll.domain.game.RoomStatus
import com.guessroll.domain.game.SessionScoreRules
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import java.time.Instant
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlin.time.Duration.Companion.minutes

class SupabaseRoomRepository(
    private val client: SupabaseClient,
    private val codeGenerator: RoomCodeGenerator = RoomCodeGenerator(),
) : RoomRepository {
    private data class SignedUrlCacheEntry(
        val url: String,
        val expiresAtMillis: Long,
    )

    private val signedUrlCache = mutableMapOf<String, SignedUrlCacheEntry>()

    override fun observeRoomChanges(roomId: String): Flow<Unit> = flow {
        ensureAnonymousSession()
        val channel = client.channel("guessroll-room-$roomId")
        val roomChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "rooms"
            filter("id", FilterOperator.EQ, roomId)
        }.map { Unit }
        val playerChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "players"
            filter("room_id", FilterOperator.EQ, roomId)
        }.map { Unit }
        val mediaChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "media_items"
            filter("room_id", FilterOperator.EQ, roomId)
        }.map { Unit }
        val roundChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "rounds"
            filter("room_id", FilterOperator.EQ, roomId)
        }.map { Unit }
        val guessChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "guesses"
            filter("room_id", FilterOperator.EQ, roomId)
        }.map { Unit }
        val sessionChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "game_sessions"
            filter("room_id", FilterOperator.EQ, roomId)
        }.map { Unit }
        val reactionChanges = channel.postgresChangeFlow<PostgresAction>("public") {
            table = "round_reactions"
            filter("room_id", FilterOperator.EQ, roomId)
        }.map { Unit }

        try {
            channel.subscribe()
            merge(
                roomChanges,
                playerChanges,
                mediaChanges,
                roundChanges,
                guessChanges,
                sessionChanges,
                reactionChanges,
            ).collect {
                emit(Unit)
            }
        } finally {
            client.realtime.removeChannel(channel)
        }
    }.catch {
        // Polling remains the fallback when Realtime is not enabled for a table.
    }

    override suspend fun createRoom(nickname: String, roundCount: Int): Result<LobbySnapshot> = runCatching {
        ensureAnonymousSession()
        val room = createUniqueRoom(roundCount)
        val player = client.from("players")
            .insert(
                PlayerInsertDto(
                    roomId = room.id,
                    nickname = nickname,
                    isHost = true,
                ),
            ) {
                select()
            }
            .decodeSingle<PlayerDto>()

        client.from("rooms")
            .update(RoomHostUpdateDto(hostPlayerId = player.id)) {
                filter {
                    eq("id", room.id)
                }
            }

        snapshot(room.id, player.id)
    }

    override suspend fun joinRoom(nickname: String, roomCode: String): Result<LobbySnapshot> = runCatching {
        ensureAnonymousSession()
        val room = fetchRoomByCode(roomCode)
        require(room.toDomain().status == RoomStatus.Lobby) {
            "Игра уже началась. Попроси хоста создать новую комнату для следующей партии."
        }
        val player = client.from("players")
            .insert(
                PlayerInsertDto(
                    roomId = room.id,
                    nickname = nickname,
                    isHost = false,
                ),
            ) {
                select()
            }
            .decodeSingle<PlayerDto>()

        snapshot(room.id, player.id)
    }

    override suspend fun refreshLobby(roomId: String, currentPlayerId: String): Result<LobbySnapshot> = runCatching {
        snapshot(roomId, currentPlayerId)
    }

    override suspend fun uploadPhotos(
        roomId: String,
        playerId: String,
        photos: List<PhotoUploadData>,
        onProgress: (PhotoUploadProgress) -> Unit,
    ): Result<LobbySnapshot> = runCatching {
        ensureAnonymousSession()
        require(photos.isNotEmpty()) { "Сначала выбери хотя бы одно фото." }

        onProgress(PhotoUploadProgress(completed = 0, total = photos.size))
        val uploadedItems = uploadPhotosToStorage(
            roomId = roomId,
            playerId = playerId,
            photos = photos,
            onProgress = onProgress,
        )

        client.from("media_items")
            .insert(uploadedItems) {
                select()
            }
            .decodeList<MediaItemDto>()

        snapshot(roomId, playerId)
    }

    private suspend fun uploadPhotosToStorage(
        roomId: String,
        playerId: String,
        photos: List<PhotoUploadData>,
        onProgress: (PhotoUploadProgress) -> Unit,
    ): List<MediaItemInsertDto> = coroutineScope {
        val completed = AtomicInteger(0)
        val stagedItems = mutableListOf<MediaItemInsertDto>()
        photos.chunked(UploadConcurrency).forEach { chunk ->
            val chunkItems = chunk.map { photo ->
                async {
                    val mediaId = UUID.randomUUID().toString()
                    val path = StoragePathBuilder.photoPath(
                        roomId = roomId,
                        playerId = playerId,
                        mediaId = mediaId,
                    )

                    client.storage.from(GameMediaBucket).upload(path, photo.bytes) {
                        upsert = false
                        contentType = ContentType.parse(photo.mimeType)
                    }

                    val done = completed.incrementAndGet()
                    onProgress(PhotoUploadProgress(completed = done, total = photos.size))
                    MediaItemInsertDto(
                        id = mediaId,
                        roomId = roomId,
                        ownerPlayerId = playerId,
                        storagePath = path,
                    )
                }
            }.awaitAll()
            stagedItems += chunkItems
        }

        stagedItems
    }

    override suspend fun startGame(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot> = runCatching {
        ensureAnonymousSession()
        val room = fetchRoom(roomId)
        if (room.toDomain().status == RoomStatus.Playing) {
            return@runCatching gameSnapshot(roomId, currentPlayerId)
        }
        val players = fetchPlayers(roomId)
        val mediaItems = fetchMediaItems(roomId)
        val mediaStatus = buildMediaStatus(
            players = players,
            currentPlayerId = currentPlayerId,
            mediaCounts = mediaItems.groupingBy { it.ownerPlayerId }.eachCount(),
            requiredPhotoCount = room.roundCount,
        )
        val startCheck = GameStartRules.evaluate(
            isHost = players.firstOrNull { it.id == currentPlayerId }?.isHost == true,
            playerCount = players.size,
            playersWithPhotos = mediaStatus.playersWithPhotos,
            mediaCount = mediaItems.size,
            requestedRoundCount = room.roundCount,
            roomStatus = room.toDomain().status,
        )
        require(startCheck.canStart) { startBlockMessage(startCheck.blockReason) }
        createGameSessionWithRounds(
            roomId = roomId,
            requestedRoundCount = room.roundCount,
            mediaItems = mediaItems,
        )
        gameSnapshot(roomId, currentPlayerId)
    }

    override suspend fun startRematch(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot> = runCatching {
        ensureAnonymousSession()
        val room = fetchRoom(roomId)
        val domainRoom = room.toDomain()
        val players = fetchPlayers(roomId)
        require(players.firstOrNull { it.id == currentPlayerId }?.isHost == true) {
            "Только хост может запустить реванш."
        }
        if (domainRoom.status == RoomStatus.Playing) {
            return@runCatching gameSnapshot(roomId, currentPlayerId)
        }
        require(domainRoom.status == RoomStatus.Finished) {
            "Реванш можно запустить после завершения игры."
        }
        val mediaItems = fetchMediaItems(roomId)
        val mediaStatus = buildMediaStatus(
            players = players,
            currentPlayerId = currentPlayerId,
            mediaCounts = mediaItems.groupingBy { it.ownerPlayerId }.eachCount(),
            requiredPhotoCount = room.roundCount,
        )
        val startCheck = GameStartRules.evaluate(
            isHost = true,
            playerCount = players.size,
            playersWithPhotos = mediaStatus.playersWithPhotos,
            mediaCount = mediaItems.size,
            requestedRoundCount = room.roundCount,
            roomStatus = RoomStatus.Lobby,
        )
        require(startCheck.canStart) { startBlockMessage(startCheck.blockReason) }
        createGameSessionWithRounds(
            roomId = roomId,
            requestedRoundCount = room.roundCount,
            mediaItems = mediaItems,
        )
        gameSnapshot(roomId, currentPlayerId)
    }

    override suspend fun refreshGame(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot> = runCatching {
        ensureAnonymousSession()
        synchronizeRoundRevealIfHost(roomId, currentPlayerId)
        gameSnapshot(roomId, currentPlayerId)
    }

    override suspend fun submitGuess(
        roomId: String,
        currentPlayerId: String,
        roundId: String,
        guessedPlayerId: String,
    ): Result<GameRoundSnapshot> = runCatching {
        ensureAnonymousSession()
        val round = fetchRound(roundId)
        require(round.roomId == roomId) { "Раунд не относится к этой комнате." }
        require(round.status == RoundStatus.Active) { "Раунд уже закрыт для ответов." }
        val players = fetchPlayers(roomId)
        val currentPlayer = players.firstOrNull { it.id == currentPlayerId }
        require(currentPlayer != null) { "Игрок не найден в комнате." }
        require(players.any { it.id == guessedPlayerId }) { "Такого игрока нет в комнате." }
        val existingGuess = fetchGuess(roundId, currentPlayerId)
        if (existingGuess == null) {
            val isCorrect = GuessScoring.isCorrect(round, guessedPlayerId)
            runCatching {
                client.from("guesses")
                    .insert(
                        GuessInsertDto(
                            roomId = roomId,
                            roundId = roundId,
                            gameSessionId = round.gameSessionId,
                            playerId = currentPlayerId,
                            guessedPlayerId = guessedPlayerId,
                            isCorrect = isCorrect,
                        ),
                    ) {
                        select()
                    }
                    .decodeSingle<GuessDto>()
                    .toDomain()
            }.getOrElse { error ->
                fetchGuess(roundId, currentPlayerId) ?: throw error
            }
        }
        if (currentPlayer.isHost) {
            revealRoundIfEveryoneAnswered(roomId = roomId, round = round)
        }
        gameSnapshot(roomId, currentPlayerId)
    }

    override suspend fun sendReaction(
        roomId: String,
        currentPlayerId: String,
        roundId: String,
        emoji: String,
    ): Result<Unit> = runCatching {
        ensureAnonymousSession()
        require(ReactionRules.isAllowed(emoji)) { "Р­С‚Р° СЂРµР°РєС†РёСЏ РЅРµРґРѕСЃС‚СѓРїРЅР°." }
        val round = fetchRound(roundId)
        require(round.roomId == roomId) { "Р Р°СѓРЅРґ РЅРµ РѕС‚РЅРѕСЃРёС‚СЃСЏ Рє СЌС‚РѕР№ РєРѕРјРЅР°С‚Рµ." }
        val currentPlayer = fetchPlayer(currentPlayerId)
        require(currentPlayer.roomId == roomId) { "РРіСЂРѕРє РЅРµ РІ СЌС‚РѕР№ РєРѕРјРЅР°С‚Рµ." }

        client.from("round_reactions")
            .insert(
                RoundReactionInsertDto(
                    id = UUID.randomUUID().toString(),
                    roomId = roomId,
                    gameSessionId = round.gameSessionId,
                    roundId = round.id,
                    playerId = currentPlayerId,
                    emoji = emoji,
                ),
            )
    }

    override suspend fun advanceRound(roomId: String, currentPlayerId: String): Result<GameRoundSnapshot> = runCatching {
        ensureAnonymousSession()
        val room = fetchRoom(roomId).toDomain()
        val currentPlayer = fetchPlayer(currentPlayerId)
        require(currentPlayer.isHost) { "Только хост может переключать раунды." }
        val session = fetchCurrentSession(roomId) ?: error("Текущая игра не найдена.")
        val rounds = fetchRoundsForSession(session.id)
        require(rounds.isNotEmpty()) { "Раунды ещё не созданы." }
        val currentRound = rounds.firstOrNull { it.roundNumber == room.currentRoundIndex + 1 }
            ?: rounds.first()
        if (currentRound.status == RoundStatus.Active) {
            revealRoundIfEveryoneAnswered(roomId = roomId, round = currentRound)
            return@runCatching gameSnapshot(roomId, currentPlayerId)
        }
        require(currentRound.status == RoundStatus.Revealed) {
            "Ждём ответы всех игроков перед следующим раундом."
        }
        val decision = RoundAdvanceRules.afterReveal(
            currentRoundNumber = currentRound.roundNumber,
            totalRounds = rounds.size,
        )

        client.from("rounds").update(RoundStatusUpdateDto(status = "finished")) {
            filter {
                eq("id", currentRound.id)
            }
        }

        if (decision.finishGame) {
            client.from("game_sessions").update(
                GameSessionFinishedUpdateDto(
                    status = "finished",
                    currentRoundIndex = rounds.size,
                    endedAt = Instant.now().toString(),
                ),
            ) {
                filter {
                    eq("id", session.id)
                }
            }
            client.from("rooms").update(
                RoomGameStateUpdateDto(
                    status = "finished",
                    currentRoundIndex = rounds.size,
                ),
            ) {
                filter {
                    eq("id", roomId)
                }
            }
        } else {
            val nextRound = rounds.first { it.roundNumber == decision.nextRoundNumber }
            client.from("rounds").update(RoundStatusUpdateDto(status = "active")) {
                filter {
                    eq("id", nextRound.id)
                }
            }
            client.from("game_sessions").update(
                GameSessionStateUpdateDto(
                    status = "playing",
                    currentRoundIndex = nextRound.roundNumber - 1,
                ),
            ) {
                filter {
                    eq("id", session.id)
                }
            }
            client.from("rooms").update(
                RoomGameStateUpdateDto(
                    status = "playing",
                    currentRoundIndex = nextRound.roundNumber - 1,
                ),
            ) {
                filter {
                    eq("id", roomId)
                }
            }
        }
        gameSnapshot(roomId, currentPlayerId)
    }

    override suspend fun fetchResults(roomId: String, currentPlayerId: String): Result<ResultsSnapshot> = runCatching {
        ensureAnonymousSession()
        val room = fetchRoom(roomId).toDomain()
        val session = fetchCurrentSession(roomId)
        val players = fetchPlayers(roomId)
        val sessionGuesses = session?.let { fetchGuessesForSession(it.id) }.orEmpty()
        val scoredPlayers = SessionScoreRules.applySessionScores(players, sessionGuesses)
        ResultsSnapshot(
            room = room,
            session = session,
            players = scoredPlayers,
            currentPlayerId = currentPlayerId,
            leaderboard = LeaderboardRanker.rank(scoredPlayers),
            sessionGuesses = sessionGuesses,
        )
    }

    private suspend fun ensureAnonymousSession() {
        if (client.auth.currentUserOrNull() == null) {
            client.auth.signInAnonymously()
        }
    }

    private suspend fun createUniqueRoom(roundCount: Int): RoomDto {
        repeat(6) {
            val code = codeGenerator.generate()
            val existing = fetchRoomsByCode(code)
            if (existing.isEmpty()) {
                return client.from("rooms")
                    .insert(
                        RoomInsertDto(
                            code = code,
                            roundCount = roundCount,
                        ),
                    ) {
                        select()
                    }
                    .decodeSingle<RoomDto>()
            }
        }
        error("Не удалось создать уникальный код комнаты. Попробуй еще раз.")
    }

    private suspend fun createGameSessionWithRounds(
        roomId: String,
        requestedRoundCount: Int,
        mediaItems: List<MediaItem>,
    ): GameSession {
        val plannedRounds = com.guessroll.domain.game.RoundPlanner.plan(
            mediaItems = mediaItems,
            requestedRoundCount = requestedRoundCount,
        )
        val session = client.from("game_sessions")
            .insert(
                GameSessionInsertDto(
                    roomId = roomId,
                    roundCount = plannedRounds.size,
                    currentRoundIndex = 0,
                ),
            ) {
                select()
            }
            .decodeSingle<GameSessionDto>()

        plannedRounds.forEach { round ->
            client.from("rounds").insert(
                RoundInsertDto(
                    roomId = roomId,
                    gameSessionId = session.id,
                    mediaItemId = round.mediaItemId,
                    correctPlayerId = round.correctPlayerId,
                    roundNumber = round.roundNumber,
                    status = if (round.roundNumber == 1) RoundStatus.Active.wireValue else RoundStatus.Pending.wireValue,
                ),
            )
        }

        client.from("rooms").update(
            RoomGameSessionStateUpdateDto(
                status = RoomStatus.Playing.wireValue,
                currentRoundIndex = 0,
                currentGameSessionId = session.id,
            ),
        ) {
            filter {
                eq("id", roomId)
            }
        }
        return session.toDomain()
    }

    private suspend fun fetchRoomByCode(roomCode: String): RoomDto {
        val rooms = fetchRoomsByCode(roomCode.trim().uppercase())
        return rooms.firstOrNull() ?: error("Комната с таким кодом не найдена.")
    }

    private suspend fun fetchRoomsByCode(roomCode: String): List<RoomDto> {
        return client.from("rooms")
            .select {
                filter {
                    eq("code", roomCode)
                }
                limit(1)
            }
            .decodeList<RoomDto>()
    }

    private suspend fun snapshot(roomId: String, currentPlayerId: String): LobbySnapshot {
        val room = fetchRoom(roomId)
        val players = fetchPlayers(roomId)
        val mediaCounts = fetchMediaCountsByPlayer(roomId)
        val mediaStatus = buildMediaStatus(
            players = players,
            currentPlayerId = currentPlayerId,
            mediaCounts = mediaCounts,
            requiredPhotoCount = room.roundCount,
        )

        return LobbySnapshot(
            room = room.toDomain(),
            players = players,
            currentPlayerId = currentPlayerId,
            mediaStatus = mediaStatus,
        )
    }

    private suspend fun gameSnapshot(roomId: String, currentPlayerId: String): GameRoundSnapshot {
        val room = fetchRoom(roomId).toDomain()
        val session = fetchCurrentSession(roomId)
        val players = fetchPlayers(roomId)
        val rounds = session?.let { fetchRoundsForSession(it.id) }.orEmpty()
        val currentRound = if (room.status == RoomStatus.Finished) {
            null
        } else {
            rounds.firstOrNull { it.roundNumber == room.currentRoundIndex + 1 }
                ?: rounds.firstOrNull { it.status.wireValue == "active" }
        }
        val currentMedia = currentRound?.let { fetchMediaItem(it.mediaItemId) }
        val currentImageUrl = currentMedia?.let { media -> signedUrlFor(media) }
        val currentGuess = currentRound?.let { fetchGuess(it.id, currentPlayerId) }
        val currentRoundGuesses = currentRound?.let { fetchGuessesForRound(it.id) }.orEmpty()
        val eligiblePlayerIds = fetchEligiblePlayerIds(roomId)
        val recentReactions = currentRound?.let { round ->
            fetchRecentReactions(roundId = round.id, gameSessionId = round.gameSessionId)
        }.orEmpty()

        return GameRoundSnapshot(
            room = room,
            session = session,
            players = players,
            currentPlayerId = currentPlayerId,
            rounds = rounds,
            currentRound = currentRound,
            currentMedia = currentMedia,
            currentImageUrl = currentImageUrl,
            currentPlayerGuess = currentGuess,
            currentRoundGuessCount = currentRoundGuesses.map { it.playerId }.toSet().size,
            eligiblePlayerCount = eligiblePlayerIds.size,
            recentReactions = recentReactions,
        )
    }

    private suspend fun fetchRoom(roomId: String): RoomDto {
        return client.from("rooms")
            .select {
                filter {
                    eq("id", roomId)
                }
                limit(1)
            }
            .decodeSingle<RoomDto>()
    }

    private suspend fun fetchPlayers(roomId: String): List<Player> {
        return client.from("players")
            .select {
                filter {
                    eq("room_id", roomId)
                }
                order("joined_at", Order.ASCENDING)
            }
            .decodeList<PlayerDto>()
            .map { it.toDomain() }
    }

    private suspend fun fetchPlayer(playerId: String): Player {
        return client.from("players")
            .select {
                filter {
                    eq("id", playerId)
                }
                limit(1)
            }
            .decodeSingle<PlayerDto>()
            .toDomain()
    }

    private suspend fun fetchMediaItems(roomId: String): List<MediaItem> {
        return client.from("media_items")
            .select {
                filter {
                    eq("room_id", roomId)
                    eq("media_type", "photo")
                }
                order("created_at", Order.ASCENDING)
            }
            .decodeList<MediaItemDto>()
            .map { it.toDomain() }
    }

    private suspend fun fetchMediaItem(mediaItemId: String): MediaItem {
        return client.from("media_items")
            .select {
                filter {
                    eq("id", mediaItemId)
                }
                limit(1)
            }
            .decodeSingle<MediaItemDto>()
            .toDomain()
    }

    private suspend fun fetchCurrentSession(roomId: String): GameSession? {
        val room = fetchRoom(roomId).toDomain()
        val sessionId = room.currentGameSessionId ?: return null
        return client.from("game_sessions")
            .select {
                filter {
                    eq("id", sessionId)
                }
                limit(1)
            }
            .decodeList<GameSessionDto>()
            .firstOrNull()
            ?.toDomain()
    }

    private suspend fun fetchRoundsForSession(gameSessionId: String): List<GameRound> {
        return client.from("rounds")
            .select {
                filter {
                    eq("game_session_id", gameSessionId)
                }
                order("round_number", Order.ASCENDING)
            }
            .decodeList<RoundDto>()
            .map { it.toDomain() }
    }

    private suspend fun fetchRounds(roomId: String): List<GameRound> {
        return client.from("rounds")
            .select {
                filter {
                    eq("room_id", roomId)
                }
                order("round_number", Order.ASCENDING)
            }
            .decodeList<RoundDto>()
            .map { it.toDomain() }
    }

    private suspend fun fetchRound(roundId: String): GameRound {
        return client.from("rounds")
            .select {
                filter {
                    eq("id", roundId)
                }
                limit(1)
            }
            .decodeSingle<RoundDto>()
            .toDomain()
    }

    private suspend fun fetchGuess(roundId: String, playerId: String) = client.from("guesses")
        .select {
            filter {
                eq("round_id", roundId)
                eq("player_id", playerId)
            }
            limit(1)
        }
        .decodeList<GuessDto>()
        .firstOrNull()
        ?.toDomain()

    private suspend fun fetchGuessesForRound(roundId: String) = client.from("guesses")
        .select {
            filter {
                eq("round_id", roundId)
            }
        }
        .decodeList<GuessDto>()
        .map { it.toDomain() }

    private suspend fun fetchGuessesForSession(gameSessionId: String) = client.from("guesses")
        .select {
            filter {
                eq("game_session_id", gameSessionId)
            }
        }
        .decodeList<GuessDto>()
        .map { it.toDomain() }

    private suspend fun fetchRecentReactions(roundId: String, gameSessionId: String) = client.from("round_reactions")
        .select {
            filter {
                eq("round_id", roundId)
                eq("game_session_id", gameSessionId)
            }
            order("created_at", Order.DESCENDING)
            limit(12)
        }
        .decodeList<RoundReactionDto>()
        .asReversed()
        .map { it.toDomain() }

    private suspend fun fetchMediaCountsByPlayer(roomId: String): Map<String, Int> {
        return client.from("media_items")
            .select {
                filter {
                    eq("room_id", roomId)
                    eq("media_type", "photo")
                }
            }
            .decodeList<MediaItemDto>()
            .groupingBy { it.ownerPlayerId }
            .eachCount()
    }

    private suspend fun fetchEligiblePlayerIds(roomId: String): Set<String> {
        val players = fetchPlayers(roomId)
        val mediaCounts = fetchMediaCountsByPlayer(roomId)
        return players
            .filter { player -> (mediaCounts[player.id] ?: 0) > 0 }
            .map { player -> player.id }
            .toSet()
    }

    private suspend fun synchronizeRoundRevealIfHost(roomId: String, currentPlayerId: String) {
        val player = fetchPlayer(currentPlayerId)
        if (!player.isHost) return
        val room = fetchRoom(roomId).toDomain()
        if (room.status != RoomStatus.Playing) return
        val session = fetchCurrentSession(roomId) ?: return
        val currentRound = fetchRoundsForSession(session.id).firstOrNull { it.roundNumber == room.currentRoundIndex + 1 }
            ?: return
        if (currentRound.status != RoundStatus.Active) return
        revealRoundIfEveryoneAnswered(roomId = roomId, round = currentRound)
    }

    private suspend fun revealRoundIfEveryoneAnswered(roomId: String, round: GameRound) {
        if (round.status != RoundStatus.Active) return
        val eligiblePlayerIds = fetchEligiblePlayerIds(roomId)
        val answeredPlayerIds = fetchGuessesForRound(round.id).map { guess -> guess.playerId }
        if (!RoundRevealRules.hasEveryoneAnswered(eligiblePlayerIds, answeredPlayerIds)) return

        client.from("rounds").update(RoundStatusUpdateDto(status = RoundStatus.Revealed.wireValue)) {
            filter {
                eq("id", round.id)
                eq("status", RoundStatus.Active.wireValue)
            }
        }
    }

    private suspend fun signedUrlFor(media: MediaItem): String? {
        val nowMillis = System.currentTimeMillis()
        signedUrlCache[media.id]?.let { cached ->
            if (SignedUrlCachePolicy.isFresh(cached.expiresAtMillis, nowMillis)) {
                return cached.url
            }
        }
        val signedUrl = runCatching {
            client.storage.from(GameMediaBucket).createSignedUrl(
                path = media.storagePath,
                expiresIn = 30.minutes,
            )
        }.getOrNull() ?: return null
        signedUrlCache[media.id] = SignedUrlCacheEntry(
            url = signedUrl,
            expiresAtMillis = nowMillis + SignedUrlCachePolicy.SignedUrlDurationMillis,
        )
        return signedUrl
    }

    private fun buildMediaStatus(
        players: List<Player>,
        currentPlayerId: String,
        mediaCounts: Map<String, Int>,
        requiredPhotoCount: Int,
    ): LobbyMediaStatus {
        val requiredCount = requiredPhotoCount.coerceAtLeast(1)
        val playerProgress = players.map { player ->
            PlayerMediaProgress(
                playerId = player.id,
                nickname = player.nickname,
                uploadedPhotoCount = mediaCounts[player.id] ?: 0,
                requiredPhotoCount = requiredCount,
            )
        }
        val playersWithPhotos = playerProgress.count { it.isReady }
        return LobbyMediaStatus(
            currentPlayerPhotoCount = mediaCounts[currentPlayerId] ?: 0,
            playersWithPhotos = playersWithPhotos,
            totalPlayers = players.size,
            requiredPhotoCount = requiredCount,
            playerProgress = playerProgress,
            readiness = LobbyReadiness.evaluate(
                playerCount = players.size,
                playerProgress = playerProgress,
                requiredPhotoCount = requiredCount,
            ),
        )
    }

    private fun startBlockMessage(reason: GameStartBlockReason?): String {
        return when (reason) {
            GameStartBlockReason.NotHost -> "Только хост может начать игру."
            GameStartBlockReason.NotEnoughPlayers -> "Нужно минимум 2 игрока."
            GameStartBlockReason.MissingPhotos -> "Не все игроки загрузили фото для выбранных раундов."
            GameStartBlockReason.NoMedia -> "Нет фото для раундов."
            GameStartBlockReason.RoomNotInLobby -> "Игру можно начать только из лобби."
            null -> "Пока рано начинать игру."
        }
    }

    private companion object {
        const val GameMediaBucket = "game-media"
        const val UploadConcurrency = 2
    }
}
