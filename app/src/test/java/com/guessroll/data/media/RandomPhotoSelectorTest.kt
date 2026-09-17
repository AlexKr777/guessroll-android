package com.guessroll.data.media

import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RandomPhotoSelectorTest {
    @Test
    fun selectReturnsRequestedCountFromSupportedImages() {
        val candidates = (1..10).map { index ->
            candidate(uri = "content://photo/$index", sizeBytes = 400_000L + index)
        }

        val selected = RandomPhotoSelector.select(
            candidates = candidates,
            maxCount = 5,
            random = Random(4),
        )

        assertEquals(5, selected.size)
        assertEquals(selected.map { it.uri }.toSet().size, selected.size)
    }

    @Test
    fun selectFiltersUnsupportedTinyAndHugeFiles() {
        val candidates = listOf(
            candidate(uri = "content://photo/good-jpeg", mimeType = "image/jpeg", sizeBytes = 600_000),
            candidate(uri = "content://photo/good-webp", mimeType = "image/webp", sizeBytes = 700_000),
            candidate(uri = "content://photo/video", mimeType = "video/mp4", sizeBytes = 900_000),
            candidate(uri = "content://photo/gif", mimeType = "image/gif", sizeBytes = 900_000),
            candidate(uri = "content://photo/tiny", mimeType = "image/jpeg", sizeBytes = 2_000),
            candidate(uri = "content://photo/huge", mimeType = "image/jpeg", sizeBytes = 30_000_000),
        )

        val selected = RandomPhotoSelector.select(
            candidates = candidates,
            maxCount = 10,
            random = Random(1),
        )

        assertEquals(listOf("content://photo/good-jpeg", "content://photo/good-webp"), selected.map { it.uri }.sorted())
    }

    @Test
    fun selectDeduplicatesByUri() {
        val candidates = listOf(
            candidate(uri = "content://photo/1", displayName = "first"),
            candidate(uri = "content://photo/1", displayName = "duplicate"),
            candidate(uri = "content://photo/2", displayName = "second"),
        )

        val selected = RandomPhotoSelector.select(
            candidates = candidates,
            maxCount = 8,
            random = Random(2),
        )

        assertEquals(2, selected.size)
        assertEquals(setOf("content://photo/1", "content://photo/2"), selected.map { it.uri }.toSet())
    }

    @Test
    fun photosOnlyExcludesObviousScreenshots() {
        val candidates = listOf(
            candidate(
                uri = "content://photo/camera",
                displayName = "IMG_2048.jpg",
                bucketName = "Camera",
                relativePath = "DCIM/Camera/",
            ),
            candidate(
                uri = "content://photo/screenshot-bucket",
                displayName = "IMG_2049.jpg",
                bucketName = "Screenshots",
                relativePath = "Pictures/Screenshots/",
            ),
            candidate(
                uri = "content://photo/screenshot-name",
                displayName = "Screenshot_20260528.png",
                bucketName = "Download",
                relativePath = "Download/",
            ),
        )

        val selected = RandomPhotoSelector.select(
            candidates = candidates,
            maxCount = 8,
            sourceMode = PhotoSourceMode.PHOTOS_ONLY,
            random = Random(3),
        )

        assertEquals(listOf("content://photo/camera"), selected.map { it.uri })
    }

    @Test
    fun allGalleryIncludesScreenshots() {
        val candidates = listOf(
            candidate(uri = "content://photo/camera", displayName = "IMG_2048.jpg"),
            candidate(
                uri = "content://photo/screenshot",
                displayName = "Screenshot_20260528.png",
                bucketName = "Screenshots",
                relativePath = "Pictures/Screenshots/",
            ),
        )

        val selected = RandomPhotoSelector.select(
            candidates = candidates,
            maxCount = 8,
            sourceMode = PhotoSourceMode.ALL_GALLERY,
            random = Random(3),
        )

        assertEquals(setOf("content://photo/camera", "content://photo/screenshot"), selected.map { it.uri }.toSet())
    }

    @Test
    fun normalCameraNamesAreNotTreatedAsScreenshots() {
        val photo = candidate(
            uri = "content://photo/normal",
            displayName = "IMG_20260528_120000.jpg",
            bucketName = "Camera",
            relativePath = "DCIM/Camera/",
        )

        assertFalse(PhotoSourceFilter.isLikelyScreenshot(photo))
        assertTrue(PhotoSourceFilter.shouldIncludeForSourceMode(photo, PhotoSourceMode.PHOTOS_ONLY))
    }

    @Test
    fun supportedCountReflectsSourceModeFallback() {
        val candidates = listOf(
            candidate(uri = "content://photo/screenshot-1", displayName = "Screenshot_1.png"),
            candidate(uri = "content://photo/screenshot-2", displayName = "Скрин_2.png"),
        )

        assertEquals(0, RandomPhotoSelector.supportedCount(candidates, PhotoSourceMode.PHOTOS_ONLY))
        assertEquals(2, RandomPhotoSelector.supportedCount(candidates, PhotoSourceMode.ALL_GALLERY))
    }

    @Test
    fun blindPlannerExposesOnlyCountWhileKeepingUploadSelectionInternal() {
        val candidates = (1..6).map { index ->
            candidate(uri = "content://photo/$index")
        }

        val plan = BlindRandomPhotoUploadPlanner.plan(
            candidates = candidates,
            maxCount = 4,
            random = Random(5),
        )

        assertTrue(plan.canUpload)
        assertEquals(4, plan.photosForUpload.size)
        assertEquals(4, plan.publicSummary.selectedCount)
        assertEquals("4 случайных фото готовы к загрузке.", plan.publicSummary.message)
    }

    @Test
    fun blindPlannerReturnsFallbackSummaryWhenNoSupportedPhotos() {
        val candidates = listOf(
            candidate(uri = "content://photo/video", mimeType = "video/mp4", sizeBytes = 900_000),
            candidate(uri = "content://photo/tiny", mimeType = "image/jpeg", sizeBytes = 2_000),
        )

        val plan = BlindRandomPhotoUploadPlanner.plan(
            candidates = candidates,
            maxCount = 4,
            random = Random(5),
        )

        assertFalse(plan.canUpload)
        assertEquals(0, plan.photosForUpload.size)
        assertEquals(0, plan.publicSummary.selectedCount)
        assertEquals("Не нашли подходящие фото для случайного набора.", plan.publicSummary.message)
    }

    private fun candidate(
        uri: String,
        displayName: String = "photo.jpg",
        mimeType: String = "image/jpeg",
        sizeBytes: Long = 512_000,
        dateTakenMillis: Long? = 1_700_000_000_000,
        bucketName: String? = null,
        relativePath: String? = null,
    ) = LocalPhotoCandidate(
        uri = uri,
        displayName = displayName,
        mimeType = mimeType,
        sizeBytes = sizeBytes,
        dateTakenMillis = dateTakenMillis,
        bucketName = bucketName,
        relativePath = relativePath,
    )
}
