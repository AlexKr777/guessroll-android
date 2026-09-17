package com.guessroll.data.supabase

data class PhotoUploadData(
    val bytes: ByteArray,
    val mimeType: String = "image/jpeg",
)

data class PhotoUploadProgress(
    val completed: Int,
    val total: Int,
) {
    val safeCompleted: Int
        get() = completed.coerceIn(0, total.coerceAtLeast(0))
}
