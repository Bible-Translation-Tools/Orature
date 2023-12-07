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
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.data.primitives.MimeType
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
    lateinit var chapterTake : Take

    private fun mockTake() : Take {
        return mockk<Take> {
            every { format } returns MimeType.WAV
            every { file } returns OratureAudioFile(
                chapterTakeAudioFile,
                DEFAULT_CHANNELS,
                DEFAULT_SAMPLE_RATE,
                DEFAULT_BITS_PER_SAMPLE).file
        }
    }

    private fun mockDirectoryProvider() : IDirectoryProvider {
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

    private fun makeTestChapterTakeRecording(audioFile: OratureAudioFile, secondsOfAudio: Int, markers: List<AudioMarker>) {
        addBytesToFile(tempAudioFile, DEFAULT_SAMPLE_RATE * 2 * secondsOfAudio)
        val audioFileUtils = AudioFileUtils(mockDirectoryProvider())
        audioFileUtils.appendFile(audioFile, tempAudioFile)

        audioFile.clearCues()
        markers.forEach { marker ->
            audioFile.addMarker(marker)
        }
        audioFile.update()
    }



    @Test
    fun `modifyMetadata with same amount of markers, same labels, and with different locations`() {
        val takeModifier = NarrationTakeAudioModifier(chapterTake)
        val secondsOfAudio = 10

        val originalAudioMarkers = listOf<AudioMarker>(
            BookMarker("gen", 0),
            ChapterMarker(1, 44100),
            VerseMarker(1, 1, 88200),
            VerseMarker(2, 2, 132300),
            VerseMarker(3, 3, 176400)
        )

        makeTestChapterTakeRecording(
            takeModifier.audioFile,
            secondsOfAudio,
            originalAudioMarkers
        )

        // Verify that we have the expected amount of audio data
        Assert.assertEquals(takeModifier.audioFile.totalFrames, secondsOfAudio * DEFAULT_SAMPLE_RATE)

        // Verify that we have the expected cues specified by originalAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertEquals(originalAudioMarkers[idx].toCue(), cue)
        }

        val newAudioMarkers = listOf<AudioMarker>(
            BookMarker("gen", 100),
            ChapterMarker(1, 44500),
            VerseMarker(1, 1, 88600),
            VerseMarker(2, 2, 132700),
            VerseMarker(3, 3, 176800)
        )

        takeModifier.modifyMetaData(newAudioMarkers)

        // Verify that we have the expected amount of cues / Markers in the Wav file
        Assert.assertEquals(newAudioMarkers.size, takeModifier.audioFile.getCues().size)

        // Verify that we have the expected cues specified by newAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertEquals(newAudioMarkers[idx].toCue(), cue)
        }
    }

    @Test
    fun `modifyMetadata with different amount of markers, same labels, and with different locations`() {
        val takeModifier = NarrationTakeAudioModifier(chapterTake)
        val secondsOfAudio = 10

        val originalAudioMarkers = listOf<AudioMarker>(
            BookMarker("gen", 0),
            ChapterMarker(1, 44100),
            VerseMarker(1, 1, 88200),
            VerseMarker(2, 2, 132300),
            VerseMarker(3, 3, 176400)
        )

        makeTestChapterTakeRecording(
            takeModifier.audioFile,
            secondsOfAudio,
            originalAudioMarkers
        )

        // Verify that we have the expected amount of audio data
        Assert.assertEquals(takeModifier.audioFile.totalFrames, secondsOfAudio * DEFAULT_SAMPLE_RATE)

        // Verify that we have the expected cues specified by originalAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertEquals(originalAudioMarkers[idx].toCue(), cue)
        }

        val newAudioMarkers = listOf<AudioMarker>(
            BookMarker("gen", 100),
            ChapterMarker(1, 44200),
            VerseMarker(1, 1, 88300),
        )

        takeModifier.modifyMetaData(newAudioMarkers)

        // Verify that we have the expected amount of cues / Markers in the Wav file
        Assert.assertEquals(newAudioMarkers.size, takeModifier.audioFile.getCues().size)

        // Verify that we have the expected cues specified by newAudioMarkers
        takeModifier.audioFile.getCues().forEachIndexed { idx, cue ->
            Assert.assertEquals(newAudioMarkers[idx].toCue(), cue)
        }
    }
}
