package com.guessroll.data.supabase

object SignedUrlCachePolicy {
    const val SignedUrlDurationMillis: Long = 30 * 60 * 1000L
    const val RefreshBufferMillis: Long = 5 * 60 * 1000L

    fun isFresh(
        expiresAtMillis: Long,
        nowMillis: Long,
        refreshBufferMillis: Long = RefreshBufferMillis,
    ): Boolean {
        return expiresAtMillis - nowMillis > refreshBufferMillis
    }
}
