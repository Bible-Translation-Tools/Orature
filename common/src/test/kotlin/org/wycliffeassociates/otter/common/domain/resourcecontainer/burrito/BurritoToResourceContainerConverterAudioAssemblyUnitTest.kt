package org.wycliffeassociates.otter.common.domain.resourcecontainer.burrito

import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.content.BibleFileNamer
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class BurritoToResourceContainerConverterAudioAssemblyUnitTest {

    private class TestableConverter(directoryProvider: IDirectoryProvider) : BurritoToResourceContainerConverter(directoryProvider, mockk()) {
        fun assemble(chapter: Int, namer: BibleFileNamer, sections: Map<File, List<MarkerLocation>>): File {
            return constructChapterAudio(chapter, namer, sections)
        }
    }

    private fun tempDirectoryProvider(baseDir: File): IDirectoryProvider = mockk(relaxed = true) {
        every { tempDirectory } returns baseDir
    }

    private fun makeWav(file: File, verseValues: List<Int>, framesPerVerse: Int = 100) {
        val wav = WavFile(file, DEFAULT_CHANNELS, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE)
        val bytesPerSample = DEFAULT_BITS_PER_SAMPLE / 8
        val frameSize = bytesPerSample * DEFAULT_CHANNELS
        val buffer = ByteArray(framesPerVerse * frameSize)
        wav.writer(true).use { out ->
            for (v in verseValues) {
                var idx = 0
                val bb = ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(v.toShort())
                val arr = bb.array()
                while (idx < buffer.size) {
                    buffer[idx] = arr[0]
                    buffer[idx + 1] = arr[1]
                    idx += frameSize
                }
                out.write(buffer)
            }
        }
    }

    private fun readFirstOfEachBlock(wavFile: File, framesPerBlock: Int, blocks: Int): List<Short> {
        val wav = WavFile(wavFile)
        val headerSize = wav.headerSize
        FileInputStream(wavFile).use { fis ->
            fis.skip(headerSize.toLong())
            val bytes = ByteArray(blocks * framesPerBlock * 2)
            fis.read(bytes)
            val bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val out = mutableListOf<Short>()
            repeat(blocks) {
                out.add(bb.getShort(it * framesPerBlock * 2))
            }
            return out
        }
    }

    @Test
    fun testConstructChapterAudioConcatenatesVerseRangesAcrossFiles() {
        val baseDir = File(createTempDir(), "burrito_audio_unit").apply { mkdirs() }
        val provider = tempDirectoryProvider(baseDir)
        val converter = TestableConverter(provider)

        val fileA = File(baseDir, "a.wav")
        val fileB = File(baseDir, "b.wav")
        val framesPerVerse = 100
        makeWav(fileA, listOf(1, 2, 3)) // verses 1..3
        makeWav(fileB, listOf(4, 5, 6, 7)) // verses 4..7

        // Build MarkerLocation lists representing verse spans
        val sectionsA = listOf(
            Pair(VerseMarker(1, 1, 0), 0..framesPerVerse),
            Pair(VerseMarker(2, 2, framesPerVerse), framesPerVerse..2*framesPerVerse),
            Pair(VerseMarker(3, 3, 2*framesPerVerse), 2*framesPerVerse..3*framesPerVerse)
        )
        val sectionsB = listOf(
            Pair(VerseMarker(4, 4, 0), 0..framesPerVerse),
            Pair(VerseMarker(5, 5, framesPerVerse), framesPerVerse..2*framesPerVerse),
            Pair(VerseMarker(6, 6, 2*framesPerVerse), 2*framesPerVerse..3*framesPerVerse),
            Pair(VerseMarker(7, 7, 3*framesPerVerse), 3*framesPerVerse..4*framesPerVerse)
        )

        val relevant = linkedMapOf(fileA to sectionsA, fileB to sectionsB)

        val namer = BibleFileNamer("en", "gen", "reg")

        val output = converter.assemble(3, namer, relevant)

        val values = readFirstOfEachBlock(output, framesPerVerse, 7)
        // Expect 1..7 in order
        assertEquals(listOf<Short>(1,2,3,4,5,6,7).map { it.toShort() }, values)
    }
}
