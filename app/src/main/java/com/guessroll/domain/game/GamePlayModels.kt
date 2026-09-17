package com.guessroll.domain.game

import kotlin.math.min
import kotlin.random.Random

data class MediaItem(
    val id: String,
    val roomId: String,
    val ownerPlayerId: String,
    val storagePath: String,
    val mediaType: GameMode,
)

data class GameRound(
    val id: String,
    val roomId: String,
    val gameSessionId: String,
    val mediaItemId: String,
    val correctPlayerId: String,
    val roundNumber: Int,
    val status: RoundStatus,
)

data class Guess(
    val id: String,
    val roundId: String,
    val gameSessionId: String,
    val playerId: String,
    val guessedPlayerId: String,
    val isCorrect: Boolean,
)

data class RoundSeed(
    val mediaItemId: String,
    val correctPlayerId: String,
    val roundNumber: Int,
)

data class GameRoundSnapshot(
    val room: GameRoom,
    val session: GameSession?,
    val players: List<Player>,
    val currentPlayerId: String,
    val rounds: List<GameRound>,
    val currentRound: GameRound?,
    val currentMedia: MediaItem?,
    val currentImageUrl: String?,
    val currentPlayerGuess: Guess?,
    val currentRoundGuessCount: Int,
    val eligiblePlayerCount: Int,
    val recentReactions: List<RoundReaction> = emptyList(),
) {
    val totalRounds: Int
        get() = rounds.size

    val isFinished: Boolean
        get() = room.status == RoomStatus.Finished

    val currentPlayer: Player?
        get() = players.firstOrNull { it.id == currentPlayerId }

    val isCurrentPlayerHost: Boolean
        get() = currentPlayer?.isHost == true

    val isCurrentRoundRevealed: Boolean
        get() = currentRound?.status == RoundStatus.Revealed ||
            currentRound?.status == RoundStatus.Finished ||
            isFinished

    val hasCurrentPlayerAnswered: Boolean
        get() = currentPlayerGuess != null

    val isWaitingForReveal: Boolean
        get() = hasCurrentPlayerAnswered && !isCurrentRoundRevealed

    val haveAllEligiblePlayersAnswered: Boolean
        get() = RoundRevealRules.hasEveryoneAnswered(
            eligiblePlayerCount = eligiblePlayerCount,
            answerCount = currentRoundGuessCount,
        )

    val correctPlayer: Player?
        get() = if (isCurrentRoundRevealed) {
            players.firstOrNull { it.id == currentRound?.correctPlayerId }
        } else {
            null
        }
}

data class ResultsSnapshot(
    val room: GameRoom,
    val session: GameSession?,
    val players: List<Player>,
    val currentPlayerId: String,
    val leaderboard: List<LeaderboardEntry>,
    val sessionGuesses: List<Guess> = emptyList(),
) {
    val winners: List<LeaderboardEntry>
        get() = leaderboard.filter { it.rank == 1 }

    val currentPlayer: Player?
        get() = players.firstOrNull { it.id == currentPlayerId }

    val isCurrentPlayerHost: Boolean
        get() = currentPlayer?.isHost == true
}

data class LeaderboardEntry(
    val player: Player,
    val rank: Int,
    val isTied: Boolean,
)

data class GameStartCheck(
    val canStart: Boolean,
    val actualRoundCount: Int,
    val blockReason: GameStartBlockReason?,
)

enum class GameStartBlockReason {
    NotHost,
    NotEnoughPlayers,
    MissingPhotos,
    NoMedia,
    RoomNotInLobby,
}

enum class RoundStatus(val wireValue: String) {
    Pending("pending"),
    Active("active"),
    Revealed("revealed"),
    Finished("finished");

    companion object {
        fun fromWire(value: String?): RoundStatus = entries.firstOrNull { it.wireValue == value } ?: Pending
    }
}

object GameStartRules {
    private const val MinimumPlayers = 2

    fun evaluate(
        isHost: Boolean,
        playerCount: Int,
        playersWithPhotos: Int,
        mediaCount: Int,
        requestedRoundCount: Int,
        roomStatus: RoomStatus,
    ): GameStartCheck {
        val blockReason = when {
            !isHost -> GameStartBlockReason.NotHost
            roomStatus != RoomStatus.Lobby -> GameStartBlockReason.RoomNotInLobby
            playerCount < MinimumPlayers -> GameStartBlockReason.NotEnoughPlayers
            playersWithPhotos < playerCount -> GameStartBlockReason.MissingPhotos
            mediaCount <= 0 -> GameStartBlockReason.NoMedia
            mediaCount < requestedRoundCount -> GameStartBlockReason.MissingPhotos
            else -> null
        }
        return GameStartCheck(
            canStart = blockReason == null,
            actualRoundCount = if (blockReason == null) requestedRoundCount else 0,
            blockReason = blockReason,
        )
    }
}

object RoundPlanner {
    fun plan(
        mediaItems: List<MediaItem>,
        requestedRoundCount: Int,
        random: Random = Random.Default,
    ): List<RoundSeed> {
        require(requestedRoundCount > 0) { "Requested round count must be positive." }
        val photoItems = mediaItems.filter { it.mediaType == GameMode.Photo }
        require(photoItems.isNotEmpty()) { "At least one photo is required to generate rounds." }
        return photoItems
            .shuffled(random)
            .take(min(requestedRoundCount, photoItems.size))
            .mapIndexed { index, mediaItem ->
                RoundSeed(
                    mediaItemId = mediaItem.id,
                    correctPlayerId = mediaItem.ownerPlayerId,
                    roundNumber = index + 1,
                )
            }
    }
}

object GuessScoring {
    fun isCorrect(round: GameRound, guessedPlayerId: String): Boolean {
        return round.correctPlayerId == guessedPlayerId
    }
}

object SessionScoreRules {
    fun applySessionScores(players: List<Player>, guesses: List<Guess>): List<Player> {
        val scores = guesses
            .filter { it.isCorrect }
            .groupingBy { it.playerId }
            .eachCount()
        return players.map { player -> player.copy(score = scores[player.id] ?: 0) }
    }
}

data class RoundAdvanceDecision(
    val nextRoundNumber: Int?,
    val finishGame: Boolean,
)

object RoundRevealRules {
    fun hasEveryoneAnswered(eligiblePlayerCount: Int, answerCount: Int): Boolean {
        return eligiblePlayerCount > 0 && answerCount >= eligiblePlayerCount
    }

    fun hasEveryoneAnswered(eligiblePlayerIds: Collection<String>, answeredPlayerIds: Collection<String>): Boolean {
        return eligiblePlayerIds.isNotEmpty() && answeredPlayerIds.toSet().containsAll(eligiblePlayerIds)
    }
}

object RoundAdvanceRules {
    fun afterReveal(currentRoundNumber: Int, totalRounds: Int): RoundAdvanceDecision {
        val isFinalRound = currentRoundNumber >= totalRounds
        return RoundAdvanceDecision(
            nextRoundNumber = if (isFinalRound) null else currentRoundNumber + 1,
            finishGame = isFinalRound,
        )
    }
}

object LeaderboardRanker {
    fun rank(players: List<Player>): List<LeaderboardEntry> {
        val sortedPlayers = players.sortedWith(
            compareByDescending<Player> { it.score }
                .thenBy { it.nickname.lowercase() },
        )
        return sortedPlayers.map { player ->
            val rank = sortedPlayers.count { it.score > player.score } + 1
            val tied = sortedPlayers.count { it.score == player.score } > 1
            LeaderboardEntry(
                player = player,
                rank = rank,
                isTied = tied,
            )
        }
    }
}
