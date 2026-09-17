package com.guessroll.domain.game

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFlowLogicTest {
    @Test
    fun startGameAllowsHostWhenLobbyHasPlayersAndPhotos() {
        val result = GameStartRules.evaluate(
            isHost = true,
            playerCount = 2,
            playersWithPhotos = 2,
            mediaCount = 8,
            requestedRoundCount = 8,
            roomStatus = RoomStatus.Lobby,
        )

        assertTrue(result.canStart)
        assertEquals(8, result.actualRoundCount)
        assertEquals(null, result.blockReason)
    }

    @Test
    fun startGameBlocksNonHostAndMissingPhotos() {
        val nonHost = GameStartRules.evaluate(
            isHost = false,
            playerCount = 2,
            playersWithPhotos = 2,
            mediaCount = 8,
            requestedRoundCount = 8,
            roomStatus = RoomStatus.Lobby,
        )
        val missingPhotos = GameStartRules.evaluate(
            isHost = true,
            playerCount = 2,
            playersWithPhotos = 1,
            mediaCount = 7,
            requestedRoundCount = 8,
            roomStatus = RoomStatus.Lobby,
        )

        assertFalse(nonHost.canStart)
        assertEquals(GameStartBlockReason.NotHost, nonHost.blockReason)
        assertFalse(missingPhotos.canStart)
        assertEquals(GameStartBlockReason.MissingPhotos, missingPhotos.blockReason)
    }

    @Test
    fun startGameBlocksWhenTotalMediaCannotCoverRequestedRounds() {
        val result = GameStartRules.evaluate(
            isHost = true,
            playerCount = 2,
            playersWithPhotos = 2,
            mediaCount = 7,
            requestedRoundCount = 8,
            roomStatus = RoomStatus.Lobby,
        )

        assertFalse(result.canStart)
        assertEquals(GameStartBlockReason.MissingPhotos, result.blockReason)
    }

    @Test
    fun roundPlannerUsesAvailableMediaWithoutDuplicates() {
        val media = listOf(
            media("m1", "p1"),
            media("m2", "p2"),
            media("m3", "p1"),
        )

        val rounds = RoundPlanner.plan(
            mediaItems = media,
            requestedRoundCount = 10,
            random = Random(7),
        )

        assertEquals(3, rounds.size)
        assertEquals(rounds.map { it.mediaItemId }.toSet().size, rounds.size)
        assertTrue(rounds.all { it.correctPlayerId == media.first { item -> item.id == it.mediaItemId }.ownerPlayerId })
        assertEquals(listOf(1, 2, 3), rounds.map { it.roundNumber })
    }

    @Test
    fun guessFeedbackMarksCorrectAndWrongAnswers() {
        val round = GameRound(
            id = "r1",
            roomId = "room",
            gameSessionId = "s1",
            mediaItemId = "m1",
            correctPlayerId = "p2",
            roundNumber = 1,
            status = RoundStatus.Active,
        )

        assertTrue(GuessScoring.isCorrect(round, guessedPlayerId = "p2"))
        assertFalse(GuessScoring.isCorrect(round, guessedPlayerId = "p1"))
    }

    @Test
    fun correctAnswerStaysHiddenUntilEveryEligiblePlayerAnswered() {
        val eligiblePlayers = listOf("p1", "p2", "p3")

        assertFalse(
            RoundRevealRules.hasEveryoneAnswered(
                eligiblePlayerIds = eligiblePlayers,
                answeredPlayerIds = listOf("p1", "p2"),
            ),
        )
        assertTrue(
            RoundRevealRules.hasEveryoneAnswered(
                eligiblePlayerIds = eligiblePlayers,
                answeredPlayerIds = listOf("p1", "p2", "p3"),
            ),
        )
    }

    @Test
    fun revealRuleIgnoresDuplicateAnswerCounts() {
        assertFalse(
            RoundRevealRules.hasEveryoneAnswered(
                eligiblePlayerIds = listOf("p1", "p2"),
                answeredPlayerIds = listOf("p1", "p1"),
            ),
        )
        assertTrue(RoundRevealRules.hasEveryoneAnswered(eligiblePlayerCount = 2, answerCount = 2))
    }

    @Test
    fun autoAdvanceChoosesNextRoundOrFinishAfterReveal() {
        val next = RoundAdvanceRules.afterReveal(currentRoundNumber = 2, totalRounds = 4)
        val finish = RoundAdvanceRules.afterReveal(currentRoundNumber = 4, totalRounds = 4)

        assertEquals(3, next.nextRoundNumber)
        assertFalse(next.finishGame)
        assertEquals(null, finish.nextRoundNumber)
        assertTrue(finish.finishGame)
    }

    @Test
    fun leaderboardRanksPlayersByScoreWithTieHandling() {
        val players = listOf(
            player("p1", "Mira", 2),
            player("p2", "Alex", 4),
            player("p3", "Nika", 4),
            player("p4", "Dan", 1),
        )

        val leaderboard = LeaderboardRanker.rank(players)

        assertEquals(listOf("Alex", "Nika", "Mira", "Dan"), leaderboard.map { it.player.nickname })
        assertEquals(listOf(1, 1, 3, 4), leaderboard.map { it.rank })
        assertTrue(leaderboard[0].isTied)
        assertTrue(leaderboard[1].isTied)
        assertFalse(leaderboard[2].isTied)
    }

    @Test
    fun leaderboardHandlesAllZeroScoresAsSharedFirstPlace() {
        val players = listOf(
            player("p1", "Mira", 0),
            player("p2", "Alex", 0),
            player("p3", "Nika", 0),
        )

        val leaderboard = LeaderboardRanker.rank(players)

        assertEquals(listOf("Alex", "Mira", "Nika"), leaderboard.map { it.player.nickname })
        assertEquals(listOf(1, 1, 1), leaderboard.map { it.rank })
        assertTrue(leaderboard.all { it.isTied })
    }

    @Test
    fun sessionScoresIgnoreOldPlayerTotals() {
        val players = listOf(
            player("p1", "Mira", 9),
            player("p2", "Alex", 7),
        )
        val guesses = listOf(
            guess("g1", "s2", "p2", isCorrect = true),
            guess("g2", "s2", "p2", isCorrect = true),
            guess("g3", "s2", "p1", isCorrect = false),
        )

        val scoredPlayers = SessionScoreRules.applySessionScores(players, guesses)

        assertEquals(0, scoredPlayers.first { it.id == "p1" }.score)
        assertEquals(2, scoredPlayers.first { it.id == "p2" }.score)
    }

    @Test
    fun rematchCanUseSameRoundNumbersInDifferentSessions() {
        val firstSessionRound = GameRound(
            id = "r1",
            roomId = "room",
            gameSessionId = "s1",
            mediaItemId = "m1",
            correctPlayerId = "p1",
            roundNumber = 1,
            status = RoundStatus.Finished,
        )
        val rematchRound = firstSessionRound.copy(
            id = "r2",
            gameSessionId = "s2",
            status = RoundStatus.Active,
        )

        assertEquals(firstSessionRound.roundNumber, rematchRound.roundNumber)
        assertTrue(firstSessionRound.gameSessionId != rematchRound.gameSessionId)
    }

    private fun media(id: String, ownerId: String) = MediaItem(
        id = id,
        roomId = "room",
        ownerPlayerId = ownerId,
        storagePath = "rooms/room/players/$ownerId/$id.jpg",
        mediaType = GameMode.Photo,
    )

    private fun player(id: String, nickname: String, score: Int) = Player(
        id = id,
        roomId = "room",
        nickname = nickname,
        isHost = false,
        score = score,
    )

    private fun guess(id: String, sessionId: String, playerId: String, isCorrect: Boolean) = Guess(
        id = id,
        roundId = "round-$id",
        gameSessionId = sessionId,
        playerId = playerId,
        guessedPlayerId = if (isCorrect) "correct" else "wrong",
        isCorrect = isCorrect,
    )
}
