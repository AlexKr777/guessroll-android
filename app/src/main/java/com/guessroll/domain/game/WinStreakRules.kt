package com.guessroll.domain.game

data class LocalWinStreak(
    val current: Int = 0,
    val best: Int = 0,
) {
    companion object {
        val Empty = LocalWinStreak()
    }
}

data class LocalProfileStats(
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val gamesPlayed: Int = 0,
    val wins: Int = 0,
    val totalGuesses: Int = 0,
    val correctGuesses: Int = 0,
) {
    val accuracyPercent: Int
        get() = if (totalGuesses <= 0) 0 else (correctGuesses * 100 / totalGuesses)

    val hasHistory: Boolean
        get() = gamesPlayed > 0 || bestStreak > 0

    fun toWinStreak(): LocalWinStreak = LocalWinStreak(
        current = currentStreak,
        best = bestStreak,
    )

    companion object {
        val Empty = LocalProfileStats()
    }
}

object WinStreakRules {
    fun normalizeNickname(nickname: String): String {
        return nickname.trim().lowercase()
    }

    fun next(current: LocalWinStreak, wonOrTiedForFirst: Boolean): LocalWinStreak {
        val nextCurrent = if (wonOrTiedForFirst) current.current + 1 else 0
        return LocalWinStreak(
            current = nextCurrent,
            best = maxOf(current.best, nextCurrent),
        )
    }
}

object LocalProfileStatsRules {
    fun next(
        current: LocalProfileStats,
        wonOrTiedForFirst: Boolean,
        correctGuesses: Int,
        totalGuesses: Int,
    ): LocalProfileStats {
        val nextStreak = WinStreakRules.next(
            current = current.toWinStreak(),
            wonOrTiedForFirst = wonOrTiedForFirst,
        )
        return current.copy(
            currentStreak = nextStreak.current,
            bestStreak = nextStreak.best,
            gamesPlayed = current.gamesPlayed + 1,
            wins = current.wins + if (wonOrTiedForFirst) 1 else 0,
            correctGuesses = current.correctGuesses + correctGuesses.coerceAtLeast(0),
            totalGuesses = current.totalGuesses + totalGuesses.coerceAtLeast(0),
        )
    }
}
