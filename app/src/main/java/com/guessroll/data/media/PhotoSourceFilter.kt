package com.guessroll.data.media

object PhotoSourceFilter {
    fun shouldIncludeForSourceMode(
        candidate: LocalPhotoCandidate,
        sourceMode: PhotoSourceMode,
    ): Boolean {
        return sourceMode == PhotoSourceMode.ALL_GALLERY || !isLikelyScreenshot(candidate)
    }

    fun isLikelyScreenshot(candidate: LocalPhotoCandidate): Boolean {
        return hasAnyToken(candidate.bucketName, ContainerTokens) ||
            hasAnyToken(candidate.relativePath, ContainerTokens) ||
            hasAnyToken(candidate.displayName, DisplayNameTokens)
    }

    private fun hasAnyToken(value: String?, tokens: List<String>): Boolean {
        val normalized = value
            ?.lowercase()
            ?.replace('\\', '/')
            ?: return false
        return tokens.any(normalized::contains)
    }

    private val ContainerTokens = listOf(
        "screenshots",
        "screenshot",
        "screen shots",
        "screen_shots",
        "screenrecorder",
        "screen recorder",
        "screen",
        "скриншоты",
        "скриншот",
        "снимки экрана",
        "снимок экрана",
    )

    private val DisplayNameTokens = listOf(
        "screenshot",
        "screen_shot",
        "screen-shot",
        "скрин",
        "снимок экрана",
        "снимок_экрана",
    )
}
