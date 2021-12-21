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
        // generated verse marker count
        testObserver.assertValue { file ->
            AudioFile(file).metadata.getCues().size == 3
        }
    }

    @Test
    fun testEmptyFileListFails() {
        ConcatenateAudio(mockDirectoryProvider).execute(listOf()).subscribe(testObserver)
        testObserver.assertNotComplete()
        testObserver.assertErrorMessage("List is empty.")
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
