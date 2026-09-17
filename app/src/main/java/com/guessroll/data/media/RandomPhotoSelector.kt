package com.guessroll.data.media

import kotlin.random.Random

object RandomPhotoSelector {
    const val DefaultRandomPhotoCount = 8
    const val MinPhotoSizeBytes = 30_000L
    const val MaxPhotoSizeBytes = 15L * 1024L * 1024L

    fun select(
        candidates: List<LocalPhotoCandidate>,
        maxCount: Int = DefaultRandomPhotoCount,
        sourceMode: PhotoSourceMode = PhotoSourceMode.PHOTOS_ONLY,
        random: Random = Random.Default,
    ): List<LocalPhotoCandidate> {
        if (maxCount <= 0) return emptyList()

        return candidates
            .asSequence()
            .filter(::isSupported)
            .filter { candidate -> PhotoSourceFilter.shouldIncludeForSourceMode(candidate, sourceMode) }
            .distinctBy { it.uri }
            .toList()
            .shuffled(random)
            .take(maxCount)
    }

    fun isSupported(candidate: LocalPhotoCandidate): Boolean {
        val mimeType = candidate.mimeType.lowercase()
        return candidate.uri.isNotBlank() &&
            mimeType.startsWith("image/") &&
            mimeType != "image/gif" &&
            candidate.sizeBytes in MinPhotoSizeBytes..MaxPhotoSizeBytes
    }

    fun supportedCount(
        candidates: List<LocalPhotoCandidate>,
        sourceMode: PhotoSourceMode = PhotoSourceMode.PHOTOS_ONLY,
    ): Int {
        return candidates
            .asSequence()
            .filter(::isSupported)
            .filter { candidate -> PhotoSourceFilter.shouldIncludeForSourceMode(candidate, sourceMode) }
            .distinctBy { it.uri }
            .count()
    }
}

data class BlindPhotoUploadPublicSummary(
    val selectedCount: Int,
    val message: String,
)

data class BlindPhotoUploadPlan(
    val photosForUpload: List<LocalPhotoCandidate>,
    val publicSummary: BlindPhotoUploadPublicSummary,
) {
    val canUpload: Boolean = photosForUpload.isNotEmpty()
}

object BlindRandomPhotoUploadPlanner {
    fun plan(
        candidates: List<LocalPhotoCandidate>,
        maxCount: Int = RandomPhotoSelector.DefaultRandomPhotoCount,
        sourceMode: PhotoSourceMode = PhotoSourceMode.PHOTOS_ONLY,
        random: Random = Random.Default,
    ): BlindPhotoUploadPlan {
        val selected = RandomPhotoSelector.select(
            candidates = candidates,
            maxCount = maxCount,
            sourceMode = sourceMode,
            random = random,
        )
        val message = if (selected.isEmpty()) {
            "Не нашли подходящие фото для случайного набора."
        } else {
            "${selected.size} случайных фото готовы к загрузке."
        }

        return BlindPhotoUploadPlan(
            photosForUpload = selected,
            publicSummary = BlindPhotoUploadPublicSummary(
                selectedCount = selected.size,
                message = message,
            ),
        )
    }
}
