package com.guessroll.data.media

data class LocalPhotoCandidate(
    val uri: String,
    val displayName: String?,
    val mimeType: String,
    val sizeBytes: Long,
    val dateTakenMillis: Long?,
    val bucketName: String? = null,
    val relativePath: String? = null,
)
