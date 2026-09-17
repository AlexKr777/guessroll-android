package com.guessroll.ui.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

@Stable
data class GuessRollMotionSettings(
    val reducedMotion: Boolean = false,
)

val LocalGuessRollMotion = staticCompositionLocalOf { GuessRollMotionSettings() }

@Composable
fun rememberGuessRollMotionSettings(): GuessRollMotionSettings {
    val context = LocalContext.current
    return remember(context) {
        val animatorScale = runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            )
        }.getOrDefault(1f)

        GuessRollMotionSettings(reducedMotion = animatorScale == 0f)
    }
}
