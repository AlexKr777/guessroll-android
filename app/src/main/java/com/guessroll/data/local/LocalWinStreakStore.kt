package com.guessroll.data.local

import android.content.Context
import com.guessroll.domain.game.LocalProfileStats
import com.guessroll.domain.game.LocalProfileStatsRules
import com.guessroll.domain.game.LocalWinStreak
import com.guessroll.domain.game.WinStreakRules

class LocalWinStreakStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )

    fun get(nickname: String): LocalWinStreak {
        return getStats(nickname).toWinStreak()
    }

    fun getStats(nickname: String): LocalProfileStats {
        val key = keyFor(nickname)
        if (key.isEmpty()) return LocalProfileStats.Empty
        return LocalProfileStats(
            currentStreak = preferences.getInt("$key.current", 0),
            bestStreak = preferences.getInt("$key.best", 0),
            gamesPlayed = preferences.getInt("$key.games", 0),
            wins = preferences.getInt("$key.wins", 0),
            totalGuesses = preferences.getInt("$key.totalGuesses", 0),
            correctGuesses = preferences.getInt("$key.correctGuesses", 0),
        )
    }

    fun applyGameResult(
        nickname: String,
        sessionId: String,
        wonOrTiedForFirst: Boolean,
    ): LocalWinStreak {
        return applyProfileGameResult(
            nickname = nickname,
            sessionId = sessionId,
            wonOrTiedForFirst = wonOrTiedForFirst,
            correctGuesses = 0,
            totalGuesses = 0,
        ).toWinStreak()
    }

    fun applyProfileGameResult(
        nickname: String,
        sessionId: String,
        wonOrTiedForFirst: Boolean,
        correctGuesses: Int,
        totalGuesses: Int,
    ): LocalProfileStats {
        val key = keyFor(nickname)
        if (key.isEmpty() || sessionId.isBlank()) return getStats(nickname)

        val processedKey = "$key.processed.$sessionId"
        if (preferences.getBoolean(processedKey, false)) {
            return getStats(nickname)
        }

        val next = LocalProfileStatsRules.next(
            current = getStats(nickname),
            wonOrTiedForFirst = wonOrTiedForFirst,
            correctGuesses = correctGuesses,
            totalGuesses = totalGuesses,
        )
        preferences.edit()
            .putInt("$key.current", next.currentStreak)
            .putInt("$key.best", next.bestStreak)
            .putInt("$key.games", next.gamesPlayed)
            .putInt("$key.wins", next.wins)
            .putInt("$key.totalGuesses", next.totalGuesses)
            .putInt("$key.correctGuesses", next.correctGuesses)
            .putBoolean(processedKey, true)
            .apply()
        return next
    }

    private fun keyFor(nickname: String): String {
        val normalized = WinStreakRules.normalizeNickname(nickname)
        return normalized.filter { it.isLetterOrDigit() || it == '_' || it == '-' }.take(40)
    }

    private companion object {
        const val PreferencesName = "guessroll_local_win_streaks"
    }
}
