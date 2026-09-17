package com.guessroll.domain.game

import org.junit.Assert.assertEquals
import org.junit.Test

class WinStreakRulesTest {
    @Test
    fun winOrTieForFirstIncrementsCurrentAndBestStreak() {
        val next = WinStreakRules.next(LocalWinStreak(current = 2, best = 4), wonOrTiedForFirst = true)

        assertEquals(3, next.current)
        assertEquals(4, next.best)
    }

    @Test
    fun lossResetsCurrentButKeepsBestStreak() {
        val next = WinStreakRules.next(LocalWinStreak(current = 5, best = 5), wonOrTiedForFirst = false)

        assertEquals(0, next.current)
        assertEquals(5, next.best)
    }

    @Test
    fun normalizesNicknameForDeviceStorage() {
        assertEquals("alex", WinStreakRules.normalizeNickname("  Alex  "))
    }

    @Test
    fun profileStatsWinUpdatesStreakWinsAndAccuracy() {
        val next = LocalProfileStatsRules.next(
            current = LocalProfileStats(
                currentStreak = 1,
                bestStreak = 3,
                gamesPlayed = 2,
                wins = 1,
                totalGuesses = 6,
                correctGuesses = 2,
            ),
            wonOrTiedForFirst = true,
            correctGuesses = 3,
            totalGuesses = 4,
        )

        assertEquals(2, next.currentStreak)
        assertEquals(3, next.bestStreak)
        assertEquals(3, next.gamesPlayed)
        assertEquals(2, next.wins)
        assertEquals(10, next.totalGuesses)
        assertEquals(5, next.correctGuesses)
        assertEquals(50, next.accuracyPercent)
    }

    @Test
    fun profileStatsLossResetsCurrentStreakButKeepsBest() {
        val next = LocalProfileStatsRules.next(
            current = LocalProfileStats(currentStreak = 4, bestStreak = 4, gamesPlayed = 7, wins = 5),
            wonOrTiedForFirst = false,
            correctGuesses = 0,
            totalGuesses = 3,
        )

        assertEquals(0, next.currentStreak)
        assertEquals(4, next.bestStreak)
        assertEquals(8, next.gamesPlayed)
        assertEquals(5, next.wins)
    }
}
