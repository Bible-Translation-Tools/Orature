package org.wycliffeassociates.otter.common.domain.narration

import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.data.audio.*
import org.wycliffeassociates.otter.common.data.primitives.Marker
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Book
import org.wycliffeassociates.otter.common.data.workbook.Chapter
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


val chapterTakeAudioFile = File(testDirWithAudio, "testChapterTake.wav")
val tempAudioFile = File(testDirWithAudio, "tempAudio.pcm")

class NarrationTakeModifierTest {
    lateinit var chapterTake: Take
    val testBookSlug = "gen"
    val testChapterNumber = 1

    private fun mockTake(): Take {
        return mockk<Take> {
            every { format } returns MimeType.WAV
            every { file } returns OratureAudioFile(
                chapterTakeAudioFile,
                DEFAULT_CHANNELS,
                DEFAULT_SAMPLE_RATE,
                DEFAULT_BITS_PER_SAMPLE
            ).file
        }
    }

    private fun mockDirectoryProvider(): IDirectoryProvider {
        return mockk<IDirectoryProvider> {}
    }

    @Before
    fun setup() {
        createTestAudioFolders()
        chapterTake = mockTake()
    }

    @After
    fun cleanup() {
        try {
            testDirWithAudio.deleteRecursively()
        } catch (e: IOException) {
            println("Failed to delete test audio folders at '$testDataRootFilePath': ${e.message}")
        }
    }


    private fun createTestAudioFolders() {
        val testProjectChapterDirWithAudio = "testProjectChapterDirWithAudio"

        val withAudioPath = Paths.get(testDataRootFilePath, testProjectChapterDirWithAudio)

        try {
            Files.createDirectories(withAudioPath)
        } catch (e: Exception) {
            println("Failed to create test audio folders or WAV file at '$testDataRootFilePath': ${e.message}")
        }
    }


    fun addBytesToFile(file: File, numberOfBytes: Int) {
        try {
            val outputStream = FileOutputStream(file, false)

            val byteArray = ByteArray(numberOfBytes) { 1 }
            outputStream.write(byteArray)

            outputStream.close()
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    private fun makeTestChapterTakeRecording(
        audioFile: OratureAudioFile,
        secondsOfAudio: Int,
        markers: List<AudioMarker>
    ) {
        addBytesToFile(tempAudioFile, DEFAULT_SAMPLE_RATE * 2 * secondsOfAudio)
        val audioFileUtils = AudioFileUtils(mockDirectoryProvider())
        audioFileUtils.appendFile(audioFile, tempAudioFile)

        audioFile.clearCues()
        markers.forEach { marker ->
            audioFile.addMarker(audioFile.getMarkerTypeFromClass(marker::class), marker)
        }
        audioFile.update()
    }

    private fun makeAudioMarkers(secondsOfAudioPerMarker: Int, numberOfMarkers: Int): List<AudioMarker> {
        val audioMarkers = mutableListOf<AudioMarker>()
        var verseCount = 1
        for (i in 0 until numberOfMarkers) {
            if (i == 0) {
                audioMarkers.add(BookMarker(testBookSlug, 0))
            } else if (i == 1) {
                audioMarkers.add(
                    ChapterMarker(testChapterNumber, secondsOfAudioPerMarker * DEFAULT_SAMPLE_RATE * i)
                )
            } else {
                audioMarkers.add(
                    VerseMarker(verseCount, verseCount, secondsOfAudioPerMarker * DEFAULT_SAMPLE_RATE * i)
                )
                verseCount++
            }
        }
        return audioMarkers
    }

    private fun moveAudioMarker(markers: List<AudioMarker>, markerToMove: AudioMarker, delta: Int): List<AudioMarker> {
        val newAudioMarkers = mutableListOf<AudioMarker>()

        markers.forEach {
            if (it == markerToMove) {
                val newLocation = it.location + delta
                if (it::class == BookMarker::class) {
                    newAudioMarkers.add(BookMarker(testBookSlug, newLocation))
                } else if (it::class == ChapterMarker::class) {
                    newAudioMarkers.add(ChapterMarker(testChapterNumber, newLocation))
                } else if (it::class == VerseMarker::class) {
                    val verseStart = (it as VerseMarker).start
                    val verseEnd = it.end
                    newAudioMarkers.add(VerseMarker(verseStart, verseEnd, newLocation))
                } else {
                    // no op, only considering book, chapter, and verse markers
                }
            } else {
                newAudioMarkers.add(it)
            }
        }

        return newAudioMarkers
    }

    private fun moveAllAudioMarkers(markers: List<AudioMarker>, delta: Int): List<AudioMarker> {
        var newMarkers = markers
        newMarkers.forEach { audioMarker ->
            newMarkers = moveAudioMarker(newMarkers, audioMarker, delta)
        }
        return newMarkers
    }


    // Test that Wav file metadata is updated properly when keeping the same markers, but changing their location
    @Test
    fun `modifyMetadata with same amount of markers, same labels, and with different locations`() {
        val takeModifier = NarrationTakeAudioModifier(chapterTake)
        val secondsOfAudio = 10

        val originalAudioMarkers = makeAudioMarkers(1, 5)

        makeTestChapterTakeRecording(
            takeModifier.audioFile,
            secondsOfAudio,
            originalAudioMarkers
        )

        // Verify that we have the expected amount of audio data
        Assert.assertEquals(takeModifier.audioFile.totalFrames, secondsOfAudio * DEFAULT_SAMPLE_RATE)

        // Verify that we have the expected cues specified by originalAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertTrue(originalAudioMarkers.map { it.toCue() }.contains(cue))
        }

        // Simulates moving each marker by 400 samples
        val newAudioMarkers = moveAllAudioMarkers(originalAudioMarkers, 400)

        takeModifier.modifyMetaData(newAudioMarkers)

        // Verify that we have the expected amount of cues / Markers in the Wav file
        Assert.assertEquals(newAudioMarkers.size, takeModifier.audioFile.getCues().size)

        // Verify that we have the expected cues specified by newAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertTrue(newAudioMarkers.map { it.toCue() }.contains(cue))
        }
    }

    // Test that Wav file metadata is updated properly when using a different amount of markers with different locations
    @Test
    fun `modifyMetadata with different amount of markers, same labels, and with different locations`() {
        val takeModifier = NarrationTakeAudioModifier(chapterTake)
        val secondsOfAudio = 10

        val originalAudioMarkers = makeAudioMarkers(1, 5)
        val markersToKeep = 3

        makeTestChapterTakeRecording(
            takeModifier.audioFile,
            secondsOfAudio,
            originalAudioMarkers
        )

        // Verify that we have the expected amount of audio data
        Assert.assertEquals(takeModifier.audioFile.totalFrames, secondsOfAudio * DEFAULT_SAMPLE_RATE)

        // Verify that we have the expected cues specified by originalAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertTrue(originalAudioMarkers.map { it.toCue() }.contains(cue))
        }

        // Simulates moving all markers by 100 and removing all but the first markersToKeep number of markers
        val newAudioMarkers = moveAllAudioMarkers(originalAudioMarkers, 100).subList(0, markersToKeep)

        takeModifier.modifyMetaData(newAudioMarkers)

        // Verify that we have the expected amount of cues / Markers in the Wav file
        Assert.assertEquals(markersToKeep, takeModifier.audioFile.getCues().size)

        // Verify that we have the expected cues specified by newAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertTrue(newAudioMarkers.map { it.toCue() }.contains(cue))
        }
    }

    @Test
    fun `modifyMetadata multiple times with same markers`() {
        val takeModifier = NarrationTakeAudioModifier(chapterTake)
        val secondsOfAudio = 10

        val originalAudioMarkers = makeAudioMarkers(1, 5)

        // Initializes audio and adds markers
        makeTestChapterTakeRecording(
            takeModifier.audioFile,
            secondsOfAudio,
            originalAudioMarkers
        )

        // Verify that we have the expected cues specified by originalAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertTrue(originalAudioMarkers.map { it.toCue() }.contains(cue))
        }

        // Re-inserts markers
        var newAudioMarkers = originalAudioMarkers.map { it }

        takeModifier.modifyMetaData(newAudioMarkers)

        // Verify that we have the expected amount of cues / Markers in the Wav file
        Assert.assertEquals(newAudioMarkers.size, takeModifier.audioFile.getCues().size)

        // Verify that we have the expected cues specified by newAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertTrue(newAudioMarkers.map { it.toCue() }.contains(cue))
        }

        // Re-inserts markers
        newAudioMarkers = originalAudioMarkers.map { it }

        takeModifier.modifyMetaData(newAudioMarkers)

        // Verify that we have the expected amount of cues / Markers in the Wav file
        Assert.assertEquals(newAudioMarkers.size, takeModifier.audioFile.getCues().size)

        // Verify that we have the expected cues specified by newAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertTrue(newAudioMarkers.map { it.toCue() }.contains(cue))
        }
    }
}
