package com.guessroll.data.media

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MediaStorePhotoSource {
    suspend fun scanImages(context: Context): Result<List<LocalPhotoCandidate>> = runCatching {
        withContext(Dispatchers.IO) {
            val resolver = context.contentResolver
            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.MIME_TYPE,
                MediaStore.Images.Media.SIZE,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.RELATIVE_PATH
                } else {
                    MediaStore.Images.Media.DATA
                },
            )
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            resolver.query(
                collection,
                projection,
                null,
                null,
                sortOrder,
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val mimeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val takenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val addedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val bucketColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME)
                val pathColumn = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Images.Media.RELATIVE_PATH)
                } else {
                    cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                }

                buildList {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(idColumn)
                        val uri = ContentUris.withAppendedId(collection, id)
                        val dateTaken = cursor.getLongOrNull(takenColumn)
                        val dateAdded = cursor.getLongOrNull(addedColumn)?.times(1000L)

                        add(
                            LocalPhotoCandidate(
                                uri = uri.toString(),
                                displayName = cursor.getStringOrNull(nameColumn),
                                mimeType = cursor.getStringOrNull(mimeColumn) ?: "image/jpeg",
                                sizeBytes = cursor.getLongOrNull(sizeColumn) ?: 0L,
                                dateTakenMillis = dateTaken?.takeIf { it > 0L } ?: dateAdded?.takeIf { it > 0L },
                                bucketName = cursor.getStringOrNull(bucketColumn),
                                relativePath = cursor.getStringOrNull(pathColumn),
                            ),
                        )
                    }
                }
            } ?: emptyList()
        }
    }
}

private fun android.database.Cursor.getStringOrNull(index: Int): String? {
    if (index < 0) return null
    return if (isNull(index)) null else getString(index)
}

private fun android.database.Cursor.getLongOrNull(index: Int): Long? {
    if (index < 0) return null
    return if (isNull(index)) null else getLong(index)
}
