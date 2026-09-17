package com.guessroll.domain.game

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReactionRulesTest {
    @Test
    fun validatesOnlyAllowedPartyReactions() {
        assertTrue(ReactionRules.isAllowed("😂"))
        assertTrue(ReactionRules.isAllowed("🔥"))
        assertFalse(ReactionRules.isAllowed("🎰"))
        assertFalse(ReactionRules.isAllowed("hello"))
    }

    @Test
    fun enforcesLocalCooldown() {
        assertTrue(ReactionRules.canSend(lastSentAtMillis = null, nowMillis = 10_000))
        assertFalse(ReactionRules.canSend(lastSentAtMillis = 10_000, nowMillis = 10_600))
        assertTrue(ReactionRules.canSend(lastSentAtMillis = 10_000, nowMillis = 11_200))
    }

    @Test
    fun reactionMotionSpecIsStableAndVaried() {
        val reaction = RoundReaction(
            id = "reaction-1",
            roomId = "room",
            gameSessionId = "session",
            roundId = "round",
            playerId = "player-a",
            emoji = "🔥",
            createdAt = "2026-05-29T10:00:00Z",
        )

        val first = ReactionRules.motionSpecFor(reaction)
        val second = ReactionRules.motionSpecFor(reaction)

        assertTrue(first == second)
        assertTrue(first.travelYDp in 78..139)
        assertTrue(first.driftXDp in -28..28)
        assertTrue(first.rotationDegrees in -8f..8f)
        assertTrue(first.lifetimeMillis in 1_840..2_359)
    }

    @Test
    fun capsActiveOverlayReactions() {
        val reactions = (1..12).map { index ->
            RoundReaction(
                id = "reaction-$index",
                roomId = "room",
                gameSessionId = "session",
                roundId = "round",
                playerId = "player-$index",
                emoji = "😂",
                createdAt = "created-$index",
            )
        }

        val capped = ReactionRules.cappedActive(reactions)

        assertTrue(capped.size == ReactionRules.MaxVisibleReactions)
        assertTrue(capped.first().id == "reaction-5")
        assertTrue(capped.last().id == "reaction-12")
    }
}
