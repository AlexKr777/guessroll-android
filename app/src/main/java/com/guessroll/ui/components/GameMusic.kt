package com.guessroll.ui.components

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.guessroll.R

@Composable
fun GameMusicHost(
    enabled: Boolean,
) {
    val appContext = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val music = remember(appContext) { GameMusicPlayer(appContext) }
    var isForeground by remember(lifecycle) {
        mutableStateOf(lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED))
    }

    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> isForeground = true
                Lifecycle.Event.ON_STOP -> isForeground = false
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(enabled, isForeground) {
        if (enabled && isForeground) {
            music.play()
        } else {
            music.pause()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            music.release()
        }
    }
}

private class GameMusicPlayer(
    private val context: Context,
) {
    private var mediaPlayer: MediaPlayer? = null

    fun play() {
        val player = mediaPlayer ?: buildPlayer()?.also { mediaPlayer = it } ?: return
        if (!player.isPlaying) {
            runCatching { player.start() }
                .onFailure {
                    release()
                }
        }
    }

    fun pause() {
        mediaPlayer?.takeIf { it.isPlaying }?.let { player ->
            runCatching { player.pause() }
        }
    }

    fun release() {
        mediaPlayer?.let { player ->
            runCatching { player.pause() }
            runCatching { player.release() }
        }
        mediaPlayer = null
    }

    private fun buildPlayer(): MediaPlayer? {
        return runCatching {
            val player = MediaPlayer()
            runCatching {
                player.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                )
                context.resources.openRawResourceFd(R.raw.guessroll_music_loop).use { descriptor ->
                    player.setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                }
                player.isLooping = true
                player.setVolume(MusicVolume, MusicVolume)
                player.setOnErrorListener { erroredPlayer, _, _ ->
                    if (mediaPlayer === erroredPlayer) {
                        mediaPlayer = null
                    }
                    runCatching { erroredPlayer.release() }
                    true
                }
                player.prepare()
                player
            }.onFailure {
                runCatching { player.release() }
            }.getOrThrow()
        }.getOrNull()
    }

    private companion object {
        const val MusicVolume = 0.16f
    }
}
