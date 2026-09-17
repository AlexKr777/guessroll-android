package com.guessroll.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.isActive

@Composable
fun LifecyclePollingEffect(
    key: Any?,
    enabled: Boolean,
    intervalMillis: Long,
    immediate: Boolean = true,
    onRefresh: suspend () -> Unit,
) {
    val isResumed = rememberLifecycleResumed()
    val latestOnRefresh by rememberUpdatedState(onRefresh)

    LaunchedEffect(key, enabled, isResumed, intervalMillis, immediate) {
        if (!enabled || !isResumed || key == null) return@LaunchedEffect
        if (immediate) {
            latestOnRefresh()
        }
        while (isActive) {
            delay(intervalMillis)
            latestOnRefresh()
        }
    }
}

@Composable
fun LifecycleEventRefreshEffect(
    key: Any?,
    enabled: Boolean,
    events: Flow<Unit>?,
    onEvent: suspend () -> Unit,
) {
    val isResumed = rememberLifecycleResumed()
    val latestOnEvent by rememberUpdatedState(onEvent)

    LaunchedEffect(key, enabled, isResumed, events) {
        if (!enabled || !isResumed || key == null || events == null) return@LaunchedEffect
        events.collect {
            latestOnEvent()
        }
    }
}

@Composable
private fun rememberLifecycleResumed(): Boolean {
    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember(lifecycleOwner) {
        mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, _ ->
            isResumed = lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return isResumed
}
