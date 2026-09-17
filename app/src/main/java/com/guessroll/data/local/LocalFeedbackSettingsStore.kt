package com.guessroll.data.local

import android.content.Context

data class LocalFeedbackSettings(
    val soundEnabled: Boolean = false,
    val musicEnabled: Boolean = false,
    val hapticsEnabled: Boolean = true,
)

class LocalFeedbackSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        PreferencesName,
        Context.MODE_PRIVATE,
    )

    fun get(): LocalFeedbackSettings {
        return LocalFeedbackSettings(
            soundEnabled = preferences.getBoolean(SoundKey, false),
            musicEnabled = preferences.getBoolean(MusicKey, false),
            hapticsEnabled = preferences.getBoolean(HapticsKey, true),
        )
    }

    fun setSoundEnabled(enabled: Boolean): LocalFeedbackSettings {
        preferences.edit().putBoolean(SoundKey, enabled).apply()
        return get()
    }

    fun setHapticsEnabled(enabled: Boolean): LocalFeedbackSettings {
        preferences.edit().putBoolean(HapticsKey, enabled).apply()
        return get()
    }

    fun setMusicEnabled(enabled: Boolean): LocalFeedbackSettings {
        preferences.edit().putBoolean(MusicKey, enabled).apply()
        return get()
    }

    private companion object {
        const val PreferencesName = "guessroll_feedback_settings"
        const val SoundKey = "sound.enabled"
        const val MusicKey = "music.enabled"
        const val HapticsKey = "haptics.enabled"
    }
}
