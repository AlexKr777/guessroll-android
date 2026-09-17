package com.guessroll.domain.game

data class ResultsPodiumSlots(
    val first: LeaderboardEntry?,
    val second: LeaderboardEntry?,
    val third: LeaderboardEntry?,
)

object ResultsPodiumLayout {
    fun fromLeaderboard(entries: List<LeaderboardEntry>): ResultsPodiumSlots {
        val top = entries.take(3)
        return ResultsPodiumSlots(
            first = top.getOrNull(0),
            second = top.getOrNull(1),
            third = top.getOrNull(2),
        )
    }
}
