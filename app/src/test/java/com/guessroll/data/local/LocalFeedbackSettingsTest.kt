package com.guessroll.data.local

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalFeedbackSettingsTest {
    @Test
    fun defaultsKeepMusicAndEffectsOffWhileHapticsStayOn() {
        val settings = LocalFeedbackSettings()

        assertFalse(settings.soundEnabled)
        assertFalse(settings.musicEnabled)
        assertTrue(settings.hapticsEnabled)
    }

    @Test
    fun musicSoundAndHapticsAreIndependentSettings() {
        val settings = LocalFeedbackSettings()
            .copy(soundEnabled = true)
            .copy(musicEnabled = true)
            .copy(hapticsEnabled = false)

        assertTrue(settings.soundEnabled)
        assertTrue(settings.musicEnabled)
        assertFalse(settings.hapticsEnabled)
    }
}
