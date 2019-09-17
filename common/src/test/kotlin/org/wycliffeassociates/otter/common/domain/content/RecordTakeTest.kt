package org.wycliffeassociates.otter.common.domain.content

import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.wycliffeassociates.otter.common.data.model.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.doAssertEquals
import org.wycliffeassociates.otter.common.domain.plugins.LaunchPlugin
import org.wycliffeassociates.otter.common.persistence.EMPTY_WAVE_FILE_SIZE
import java.io.File
import java.time.LocalDate

class RecordTakeTest {
    private val recordTake = RecordTake(mock(), mock())
    private val insertTake: (Take) -> Unit = mock()

    private fun createTakeWithMockFile(): Take = doCreateTakeWithMockFileLength(EMPTY_WAVE_FILE_SIZE + 1)
    private fun createTakeWithMockEmptyFile(): Take = doCreateTakeWithMockFileLength(EMPTY_WAVE_FILE_SIZE)
    private fun doCreateTakeWithMockFileLength(fileLength: Long): Take {
        val file = mock<File>().apply {
            whenever(this.length()).thenReturn(fileLength)
        }
        return Take(
            "name",
            file,
            1,
            MimeType.WAV,
            LocalDate.now()
        )
    }

    @Test
    fun testHandleEmptyWaveFile() {
        val take = createTakeWithMockEmptyFile()
        val result = recordTake.handlePluginResult(insertTake, take, LaunchPlugin.Result.SUCCESS)
        doAssertEquals(RecordTake.Result.NO_AUDIO, result)
        verify(insertTake, times(0)).invoke(any())
        verify(take.file, times(1)).delete()
    }

    @Test
    fun testHandleSuccess() {
        val take = createTakeWithMockFile()
        val result = recordTake.handlePluginResult(insertTake, take, LaunchPlugin.Result.SUCCESS)
        doAssertEquals(RecordTake.Result.SUCCESS, result)
        verify(insertTake, times(1)).invoke(take)
        verify(take.file, times(0)).delete()
    }

    @Test
    fun testHandleNoPlugin() {
        val take = createTakeWithMockFile()
        val result = recordTake.handlePluginResult(mock(), take, LaunchPlugin.Result.NO_PLUGIN)
        doAssertEquals(RecordTake.Result.NO_RECORDER, result)
        verify(insertTake, times(0)).invoke(any())
        verify(take.file, times(1)).delete()
    }
}