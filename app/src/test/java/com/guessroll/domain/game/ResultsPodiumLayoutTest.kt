package com.guessroll.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ResultsPodiumLayoutTest {
    @Test
    fun mapsTopThreeIntoPodiumSlots() {
        val leaderboard = LeaderboardRanker.rank(
            listOf(
                player("p1", "Mira", 2),
                player("p2", "Alex", 5),
                player("p3", "Nika", 3),
                player("p4", "Dan", 1),
            ),
        )

        val slots = ResultsPodiumLayout.fromLeaderboard(leaderboard)

        assertEquals("Alex", slots.first?.player?.nickname)
        assertEquals("Nika", slots.second?.player?.nickname)
        assertEquals("Mira", slots.third?.player?.nickname)
        assertEquals(1, slots.first?.rank)
        assertEquals(2, slots.second?.rank)
        assertEquals(3, slots.third?.rank)
    }

    @Test
    fun handlesShortLeaderboards() {
        val slots = ResultsPodiumLayout.fromLeaderboard(
            LeaderboardRanker.rank(listOf(player("p1", "Solo", 0))),
        )

        assertEquals("Solo", slots.first?.player?.nickname)
        assertNull(slots.second)
        assertNull(slots.third)
    }

    @Test
    fun keepsTwoPlayerPodiumSeparate() {
        val slots = ResultsPodiumLayout.fromLeaderboard(
            LeaderboardRanker.rank(
                listOf(
                    player("p1", "Winner", 4),
                    player("p2", "Runner", 2),
                ),
            ),
        )

        assertEquals("Winner", slots.first?.player?.nickname)
        assertEquals("Runner", slots.second?.player?.nickname)
        assertNull(slots.third)
    }

    @Test
    fun ignoresPlayersBelowTopThree() {
        val slots = ResultsPodiumLayout.fromLeaderboard(
            LeaderboardRanker.rank(
                listOf(
                    player("p1", "Winner", 8),
                    player("p2", "Second", 6),
                    player("p3", "Third", 4),
                    player("p4", "Fourth", 2),
                    player("p5", "Fifth", 1),
                ),
            ),
        )

        assertEquals("Winner", slots.first?.player?.nickname)
        assertEquals("Second", slots.second?.player?.nickname)
        assertEquals("Third", slots.third?.player?.nickname)
    }

    @Test
    fun keepsTieOrderingStableWithoutChangingRanks() {
        val slots = ResultsPodiumLayout.fromLeaderboard(
            LeaderboardRanker.rank(
                listOf(
                    player("p1", "Mira", 3),
                    player("p2", "Alex", 3),
                    player("p3", "Nika", 3),
                ),
            ),
        )

        assertEquals("Alex", slots.first?.player?.nickname)
        assertEquals("Mira", slots.second?.player?.nickname)
        assertEquals("Nika", slots.third?.player?.nickname)
        assertEquals(1, slots.first?.rank)
        assertEquals(1, slots.second?.rank)
        assertEquals(1, slots.third?.rank)
        assertEquals(true, slots.first?.isTied)
        assertEquals(true, slots.second?.isTied)
        assertEquals(true, slots.third?.isTied)
    }

    private fun player(id: String, nickname: String, score: Int) = Player(
        id = id,
        roomId = "room",
        nickname = nickname,
        isHost = false,
        score = score,
    )
}
