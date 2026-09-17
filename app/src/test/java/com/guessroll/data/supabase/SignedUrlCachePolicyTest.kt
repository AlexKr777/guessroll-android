package com.guessroll.data.supabase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignedUrlCachePolicyTest {
    @Test
    fun keepsSignedUrlFreshUntilRefreshBuffer() {
        val now = 1_000L
        val expiresAt = now + SignedUrlCachePolicy.SignedUrlDurationMillis

        assertTrue(SignedUrlCachePolicy.isFresh(expiresAtMillis = expiresAt, nowMillis = now))
    }

    @Test
    fun refreshesSignedUrlNearExpiry() {
        val now = 1_000L
        val expiresAt = now + SignedUrlCachePolicy.RefreshBufferMillis

        assertFalse(SignedUrlCachePolicy.isFresh(expiresAtMillis = expiresAt, nowMillis = now))
    }
}
