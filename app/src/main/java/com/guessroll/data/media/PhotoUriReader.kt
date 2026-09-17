package com.guessroll.data.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.guessroll.data.supabase.PhotoUploadData
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

object PhotoUriReader {
    suspend fun readSelectedPhotos(
        context: Context,
        uris: List<Uri>,
    ): Result<List<PhotoUploadData>> = runCatching {
        require(uris.isNotEmpty()) { "Фото не выбраны." }

        withContext(Dispatchers.IO) {
            uris.map { uri -> readPhoto(context, uri) }
        }
    }

    suspend fun readLocalPhotos(
        context: Context,
        photos: List<LocalPhotoCandidate>,
    ): Result<List<PhotoUploadData>> = runCatching {
        require(photos.isNotEmpty()) { "Фото не выбраны." }

        withContext(Dispatchers.IO) {
            photos.map { photo ->
                readPhoto(
                    context = context,
                    uri = Uri.parse(photo.uri),
                    fallbackMimeType = photo.mimeType,
                )
            }
        }
    }

    private fun readPhoto(
        context: Context,
        uri: Uri,
        fallbackMimeType: String = "image/jpeg",
    ): PhotoUploadData {
        val mimeType = context.contentResolver.getType(uri) ?: fallbackMimeType
        require(mimeType.startsWith("image/")) { "Поддерживаются только изображения." }

        compressForGameUpload(context = context, uri = uri, mimeType = mimeType)?.let { return it }

        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        } ?: error("Не удалось прочитать выбранное фото.")

        require(bytes.isNotEmpty()) { "Выбранное фото пустое." }

        return PhotoUploadData(
            bytes = bytes,
            mimeType = mimeType,
        )
    }

    private fun compressForGameUpload(
        context: Context,
        uri: Uri,
        mimeType: String,
    ): PhotoUploadData? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P || mimeType.equals("image/gif", ignoreCase = true)) {
            return null
        }

        return runCatching {
            val source = ImageDecoder.createSource(context.contentResolver, uri)
            val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                val width = info.size.width.coerceAtLeast(1)
                val height = info.size.height.coerceAtLeast(1)
                val longestSide = maxOf(width, height)
                val scale = (MaxUploadDimensionPx.toFloat() / longestSide).coerceAtMost(1f)
                if (scale < 1f) {
                    decoder.setTargetSize(
                        (width * scale).roundToInt().coerceAtLeast(1),
                        (height * scale).roundToInt().coerceAtLeast(1),
                    )
                }
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            }

            val output = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, JpegQuality, output)
            bitmap.recycle()
            val bytes = output.toByteArray()
            require(bytes.isNotEmpty()) { "Выбранное фото пустое." }
            PhotoUploadData(
                bytes = bytes,
                mimeType = "image/jpeg",
            )
        }.getOrNull()
    }

    private const val MaxUploadDimensionPx = 1600
    private const val JpegQuality = 82
}
