/**
 * Copyright (C) 2020-2024 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.domain.chunking

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.createWavFile
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.audio.AudioConverter
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.domain.translation.ChunkAudioUseCase
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import org.wycliffeassociates.otter.common.readTextFromAudioFile
import java.io.File
import kotlin.math.PI
import kotlin.math.sin

class ChunkAudioUseCaseTest {

    @JvmField @Rule val projectSourceDir = TemporaryFolder()
    @JvmField @Rule val directoryProviderTempDir = TemporaryFolder()

    private val mockedDirectoryProvider = mock<IDirectoryProvider>()
    private lateinit var useCase: ChunkAudioUseCase

    // Cue files store positions in CD frames (1/75 of a second), so mp3 cue locations are only exact for multiples
    // of 588 samples at 44.1 kHz.
    private val cues = listOf(
        AudioCue(11760, "3"),
        AudioCue(0, "1"),
        AudioCue(5880, "2")
    )

    @Before
    fun setup() {
        whenever(mockedDirectoryProvider.tempDirectory).thenReturn(directoryProviderTempDir.root)
        useCase = ChunkAudioUseCase(mockedDirectoryProvider, mock())
    }

    @Test
    fun chunkMarkersWrittenToWavInPlaceAndSorted() {
        val wav = createWavFile(projectSourceDir.root, "source.wav", "123456".toByteArray())

        useCase.updateChunkMarkers(wav, cues)

        val audio = OratureAudioFile(wav)
        Assert.assertEquals("123456", readTextFromAudioFile(audio, 6))
        Assert.assertEquals(
            listOf(ChunkMarker(1, 0), ChunkMarker(2, 5880), ChunkMarker(3, 11760)),
            audio.getMarker<ChunkMarker>().sortedBy { it.location }
        )
    }

    @Test
    fun updatingChunkMarkersReplacesOldChunksAndKeepsOtherMarkers() {
        val wav = createWavFile(projectSourceDir.root, "source.wav", "123456".toByteArray())
        OratureAudioFile(wav).apply {
            addMarker(VerseMarker(1, 1, 0))
            addMarker(ChunkMarker(1, 100))
            addMarker(ChunkMarker(2, 200))
            update()
        }

        useCase.updateChunkMarkers(wav, listOf(AudioCue(50, "1")))

        val audio = OratureAudioFile(wav)
        Assert.assertEquals(listOf(ChunkMarker(1, 50)), audio.getMarker<ChunkMarker>())
        Assert.assertEquals(listOf(VerseMarker(1, 1, 0)), audio.getMarker<VerseMarker>())
        Assert.assertEquals("123456", readTextFromAudioFile(audio, 6))
    }

    @Test
    fun chunkMarkersWrittenToMp3CueWithoutRewritingMp3() {
        val mp3 = createMp3File(projectSourceDir.root, "source.mp3")
        val mp3Bytes = mp3.readBytes()

        useCase.updateChunkMarkers(mp3, cues)

        Assert.assertArrayEquals("the mp3 itself should not be rewritten", mp3Bytes, mp3.readBytes())
        Assert.assertTrue(File(projectSourceDir.root, "source.cue").exists())
        Assert.assertEquals(
            listOf(ChunkMarker(1, 0), ChunkMarker(2, 5880), ChunkMarker(3, 11760)),
            OratureAudioFile(mp3).getMarker<ChunkMarker>().sortedBy { it.location }
        )
    }

    @Test
    fun workingCopyOfWavHasSameAudioAndMarkers() {
        val wav = createWavFile(projectSourceDir.root, "source.wav", "123456".toByteArray())
        useCase.updateChunkMarkers(wav, cues)

        val copy = useCase.createWorkingCopy(wav)

        Assert.assertNotEquals(wav, copy)
        Assert.assertEquals(directoryProviderTempDir.root, copy.parentFile)
        Assert.assertEquals("wav", copy.extension)
        Assert.assertEquals(
            OratureAudioFile(wav).getMarker<ChunkMarker>(),
            OratureAudioFile(copy).getMarker<ChunkMarker>()
        )
    }

    @Test
    fun workingCopiesOfSameFileDoNotCollide() {
        val wav = createWavFile(projectSourceDir.root, "source.wav", "123456".toByteArray())

        val first = useCase.createWorkingCopy(wav)
        val second = useCase.createWorkingCopy(wav)

        Assert.assertNotEquals(first, second)
        Assert.assertTrue(first.exists())
        Assert.assertTrue(second.exists())
    }

    @Test
    fun workingCopyOfMp3IncludesItsCueFile() {
        val mp3 = createMp3File(projectSourceDir.root, "source.mp3")
        useCase.updateChunkMarkers(mp3, cues)

        val copy = useCase.createWorkingCopy(mp3)

        Assert.assertTrue(File(copy.parentFile, "${copy.nameWithoutExtension}.cue").exists())
        Assert.assertEquals(3, OratureAudioFile(copy).getMarker<ChunkMarker>().size)
    }

    @Test
    fun deleteWorkingCopyRemovesAudioAndCue() {
        val mp3 = createMp3File(projectSourceDir.root, "source.mp3")
        useCase.updateChunkMarkers(mp3, cues)
        val copy = useCase.createWorkingCopy(mp3)
        val copyCue = File(copy.parentFile, "${copy.nameWithoutExtension}.cue")

        useCase.deleteWorkingCopy(copy)

        Assert.assertFalse(copy.exists())
        Assert.assertFalse(copyCue.exists())
        Assert.assertTrue("the original should be untouched", mp3.exists())
    }

    /**
     * Encodes one second of a 440 Hz tone to mp3, since mp3 files can't be built from arbitrary bytes.
     */
    private fun createMp3File(dir: File, name: String): File {
        val sampleRate = 44100
        val pcm = ByteArray(sampleRate * 2)
        for (i in 0 until sampleRate) {
            val sample = (sin(2 * PI * 440 * i / sampleRate) * Short.MAX_VALUE / 2).toInt()
            pcm[i * 2] = (sample and 0xff).toByte()
            pcm[i * 2 + 1] = (sample shr 8 and 0xff).toByte()
        }
        val wav = createWavFile(directoryProviderTempDir.newFolder(), "tone.wav", pcm)
        val mp3 = File(dir, name)
        AudioConverter().wavToMp3(wav, mp3).blockingAwait()
        return mp3
    }
}
