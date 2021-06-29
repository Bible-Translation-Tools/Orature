/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
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

import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.wycliffeassociates.otter.common.data.primitives.MimeType
import org.wycliffeassociates.otter.common.data.workbook.Take
import org.wycliffeassociates.otter.common.doAssertEquals
import org.wycliffeassociates.otter.common.audio.wav.EMPTY_WAVE_FILE_SIZE
import java.io.File
import java.time.LocalDate

class RecordTakeTest {
    private val recordTake = TakeActions(mock(), mock())
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
        val result = recordTake.handleRecorderPluginResult(insertTake, take, TakeActions.Result.SUCCESS)
        doAssertEquals(TakeActions.Result.NO_AUDIO, result)
        verify(insertTake, times(0)).invoke(any())
        verify(take.file, times(1)).delete()
    }

    @Test
    fun testHandleSuccess() {
        val take = createTakeWithMockFile()
        val result = recordTake.handleRecorderPluginResult(insertTake, take, TakeActions.Result.SUCCESS)
        doAssertEquals(TakeActions.Result.SUCCESS, result)
        verify(insertTake, times(1)).invoke(take)
        verify(take.file, times(0)).delete()
    }

    @Test
    fun testHandleNoPlugin() {
        val take = createTakeWithMockFile()
        val result = recordTake.handleRecorderPluginResult(mock(), take, TakeActions.Result.NO_PLUGIN)
        doAssertEquals(TakeActions.Result.NO_PLUGIN, result)
        verify(insertTake, times(0)).invoke(any())
        verify(take.file, times(1)).delete()
    }
}
