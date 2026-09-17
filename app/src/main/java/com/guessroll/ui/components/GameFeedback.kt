package com.guessroll.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.guessroll.R

class GameFeedbackController internal constructor(
    private val view: View,
    private val soundPlayer: GameSoundPlayer?,
    private val hapticsEnabled: Boolean,
) {
    fun buttonTap() {
        haptic(HapticFeedbackConstants.KEYBOARD_TAP)
        soundPlayer?.play(GameSound.Tap)
    }

    fun answerSelected() {
        haptic(HapticFeedbackConstants.KEYBOARD_TAP)
        soundPlayer?.play(GameSound.Answer)
    }

    fun reactionSent() {
        haptic(HapticFeedbackConstants.CLOCK_TICK)
        soundPlayer?.play(GameSound.Pop)
    }

    fun success() {
        haptic(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.LONG_PRESS)
        soundPlayer?.play(GameSound.Success)
    }

    fun reject() {
        haptic(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.REJECT else HapticFeedbackConstants.KEYBOARD_TAP)
        soundPlayer?.play(GameSound.Wrong)
    }

    fun resultsReveal() {
        haptic(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) HapticFeedbackConstants.CONFIRM else HapticFeedbackConstants.CLOCK_TICK)
        soundPlayer?.play(GameSound.Results)
    }

    fun soundPreview() {
        soundPlayer?.play(GameSound.Success)
    }

    private fun haptic(type: Int) {
        if (!hapticsEnabled) return
        view.performHapticFeedback(type)
    }
}

internal enum class GameSound(val resId: Int, val volume: Float) {
    Tap(R.raw.feedback_tap, 0.44f),
    Answer(R.raw.feedback_answer, 0.44f),
    Pop(R.raw.feedback_pop, 0.40f),
    Success(R.raw.feedback_success, 0.42f),
    Wrong(R.raw.feedback_wrong, 0.38f),
    Results(R.raw.feedback_results, 0.40f),
}

internal class GameSoundPlayer(
    context: Context,
) {
    private val loadedIds = mutableSetOf<Int>()
    private val soundPool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()
    private val samples = GameSound.values().associateWith { sound ->
        soundPool.load(context, sound.resId, 1)
    }

    init {
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedIds.add(sampleId)
            }
        }
    }

    fun play(sound: GameSound) {
        val sampleId = samples[sound] ?: return
        if (sampleId !in loadedIds) return
        soundPool.play(
            sampleId,
            sound.volume,
            sound.volume,
            1,
            0,
            1f,
        )
    }

    fun release() {
        soundPool.release()
    }
}

@Composable
fun rememberGameFeedbackController(
    soundEnabled: Boolean,
    hapticsEnabled: Boolean,
): GameFeedbackController {
    val view = LocalView.current
    val appContext = LocalContext.current.applicationContext
    val soundPlayer = remember(appContext, soundEnabled) {
        if (soundEnabled) GameSoundPlayer(appContext) else null
    }
    DisposableEffect(soundPlayer) {
        onDispose {
            soundPlayer?.release()
        }
    }
    return remember(view, soundPlayer, soundEnabled, hapticsEnabled) {
        GameFeedbackController(
            view = view,
            soundPlayer = soundPlayer,
            hapticsEnabled = hapticsEnabled,
        )
    }
}
