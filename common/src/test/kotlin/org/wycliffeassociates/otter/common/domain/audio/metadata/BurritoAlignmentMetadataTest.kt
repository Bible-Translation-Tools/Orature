package org.wycliffeassociates.otter.common.domain.audio.metadata

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import java.io.File

class BurritoAlignmentMetadataTest {

    private val testFilePath = "test_burrito_timing.json"
    private val testBookSlug = "gen"
    private val testChapterNumber = 1
    private val testAudioLength = 100000

    val markers = listOf(
        BookMarker("gen", 0),
        ChapterMarker(1, 10000),
        VerseMarker(1, 1, 15000),
        VerseMarker(2, 3, 20000),
        VerseMarker(4, 4, 40000)
    )

    @Before
    fun setUp() {
        File(testFilePath).delete()
        val testFile = File(testFilePath)
        testFile.createNewFile()
    }

    @After
    fun tearDown() {
        File(testFilePath).delete()
    }

    @Test
    fun testWriteValidMarkers() {
        val metadata = BurritoAlignmentMetadata(File(testFilePath))
        metadata.write(markers, testBookSlug, testChapterNumber, testAudioLength)

        val testMetadata = BurritoAlignmentMetadata(File(testFilePath))
        val outputMarkers = testMetadata.parseTimings().getMarkers().sortedBy { it.location }
        assertEquals(outputMarkers.size, markers.size)
        markers.forEachIndexed { index, audioMarker ->
            assertEquals(outputMarkers[index].formattedLabel, audioMarker.formattedLabel)
        }
    }
}