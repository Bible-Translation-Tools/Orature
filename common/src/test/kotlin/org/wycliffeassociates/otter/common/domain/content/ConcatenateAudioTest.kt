/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.content

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.observers.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.wycliffeassociates.otter.common.audio.AudioFile
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.persistence.IDirectoryProvider
import java.io.File

class ConcatenateAudioTest {
    private lateinit var mockDirectoryProvider: IDirectoryProvider

    private lateinit var outputFile: File
    private val inputFiles = mutableListOf<File>()

    private val testObserver = TestObserver.create<File>()

    @Before
    fun setup() {
        val file1 = createWavFile("file1", "12".toByteArray())
        val file2 = createWavFile("file2", "34".toByteArray())
        val file3 = createWavFile("file3", "56".toByteArray())

        outputFile = createWavFile("output", byteArrayOf())

        mockDirectoryProvider = mock<IDirectoryProvider>().apply {
            whenever(this.createTempFile("output", ".wav")).thenReturn(outputFile)
        }

        inputFiles.add(file1)
        inputFiles.add(file2)
        inputFiles.add(file3)

        inputFiles.forEachIndexed { index, file ->
            AudioFile(file).apply {
                metadata.addCue(index, "${index + 1}")
                update()
            }
        }
    }

    @After
    fun tearDown() {
        inputFiles.forEach { it.delete() }
        outputFile.delete()
    }

    @Test
    fun testFilesConcatenated() {
        ConcatenateAudio(mockDirectoryProvider).execute(inputFiles).subscribe(testObserver)

        testObserver.assertComplete()
        testObserver.assertResult(outputFile)
        testObserver.assertValue(outputFile)
        testObserver.assertValue { file ->
            val audioFile = AudioFile(file)
            val reader = audioFile.reader()
            val buffer = ByteArray(6) // to store 6 characters (123456)
            reader.open()
            var outStr = ""
            while (reader.hasRemaining()) {
                reader.getPcmBuffer(buffer)
                outStr = buffer.decodeToString()
            }
            outStr == "123456"
        }
    }

    @Test
    fun testEmptyFileListFails() {
        ConcatenateAudio(mockDirectoryProvider).execute(listOf()).subscribe(testObserver)
        testObserver.assertNotComplete()
        testObserver.assertErrorMessage("List is empty.")
    }

    @Test
    fun testConcatWithMarkers() {
        ConcatenateAudio(mockDirectoryProvider)
            .execute(inputFiles, includeMarkers = true).subscribe(testObserver)

        testObserver.assertComplete()
        testObserver.assertResult(outputFile)
        testObserver.assertValue(outputFile)
        testObserver.assertValue { file ->
            val audioFile = AudioFile(file)
            val reader = audioFile.reader()
            val buffer = ByteArray(6) // to store 6 characters (123456)
            reader.open()
            var outStr = ""
            while (reader.hasRemaining()) {
                reader.getPcmBuffer(buffer)
                outStr = buffer.decodeToString()
            }
            outStr == "123456"
        }
        testObserver.assertValue { file ->
            val audioFile = AudioFile(file)
            val cues = audioFile.metadata.getCues()

            cues.size == 3 && cues.all {
                it.location == cues.indexOf(it)
                it.label == "${cues.indexOf(it) + 1}"
            }
        }
    }

    @Test
    fun testConcatWithoutMarkers() {
        ConcatenateAudio(mockDirectoryProvider)
            .execute(inputFiles, includeMarkers = false).subscribe(testObserver)

        testObserver.assertComplete()
        testObserver.assertResult(outputFile)
        testObserver.assertValue(outputFile)
        testObserver.assertValue { file ->
            val audioFile = AudioFile(file)
            val reader = audioFile.reader()
            val buffer = ByteArray(6) // to store 6 characters (123456)
            reader.open()
            var outStr = ""
            while (reader.hasRemaining()) {
                reader.getPcmBuffer(buffer)
                outStr = buffer.decodeToString()
            }
            outStr == "123456"
        }
        testObserver.assertValue { file ->
            val audioFile = AudioFile(file)
            val cues = audioFile.metadata.getCues()
            cues.isEmpty()
        }
    }

    private fun createWavFile(name: String, data: ByteArray): File {
        val file = File.createTempFile(name, ".wav")
        val audioFile = AudioFile(file, DEFAULT_CHANNELS, DEFAULT_SAMPLE_RATE, DEFAULT_BITS_PER_SAMPLE)
        audioFile.writer().use { os ->
            os.write(data)
        }
        return file
    }
}
