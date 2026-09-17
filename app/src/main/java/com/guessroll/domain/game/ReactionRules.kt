package com.guessroll.domain.game

data class RoundReaction(
    val id: String,
    val roomId: String,
    val gameSessionId: String,
    val roundId: String,
    val playerId: String,
    val emoji: String,
    val createdAt: String,
) {
    fun nickname(players: List<Player>): String {
        return players.firstOrNull { it.id == playerId }?.nickname.orEmpty()
    }
}

data class ReactionMotionSpec(
    val lane: Int,
    val spawnXPercent: Int,
    val spawnYDp: Int,
    val driftXDp: Int,
    val travelYDp: Int,
    val rotationDegrees: Float,
    val lifetimeMillis: Int,
)

object ReactionRules {
    val AllowedEmojis = setOf("😂", "😱", "💀", "👀", "🔥", "🤯")
    const val CooldownMillis: Long = 1_100
    const val MaxVisibleReactions: Int = 8

    fun isAllowed(emoji: String): Boolean = emoji in AllowedEmojis

    fun canSend(lastSentAtMillis: Long?, nowMillis: Long): Boolean {
        return lastSentAtMillis == null || nowMillis - lastSentAtMillis >= CooldownMillis
    }

    fun motionSpecFor(reaction: RoundReaction): ReactionMotionSpec {
        val seed = stableSeed("${reaction.id}:${reaction.playerId}:${reaction.createdAt}:${reaction.emoji}")
        val lane = seed % 4
        val spawnX = 20 + (seed / 7) % 48
        val spawnY = 18 + (seed / 13) % 54
        val drift = -28 + (seed / 17) % 57
        val travel = 78 + (seed / 23) % 62
        val rotation = -8 + (seed / 29) % 17
        val lifetime = 1_840 + (seed / 31) % 520
        return ReactionMotionSpec(
            lane = lane,
            spawnXPercent = spawnX,
            spawnYDp = spawnY,
            driftXDp = drift,
            travelYDp = travel,
            rotationDegrees = rotation.toFloat(),
            lifetimeMillis = lifetime,
        )
    }

    fun cappedActive(reactions: List<RoundReaction>): List<RoundReaction> {
        return reactions.takeLast(MaxVisibleReactions)
    }

    private fun stableSeed(value: String): Int {
        var hash = 17
        value.forEach { char ->
            hash = hash * 31 + char.code
        }
        return hash and Int.MAX_VALUE
    }
}
