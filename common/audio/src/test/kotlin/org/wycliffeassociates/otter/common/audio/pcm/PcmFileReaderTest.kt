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
package org.wycliffeassociates.otter.common.audio.pcm

import junit.framework.Assert
import org.junit.Test
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class PcmFileReaderTest {

    @Test
    fun `test that mapped buffer is released`() {
        val file = File.createTempFile("file", ".pcm")
        file.deleteOnExit()
        val pcmFile = PcmFile(file)
        val pos = PcmOutputStream(pcmFile)
        pos.use { for (i in 0 until 100) it.write(i) }
        val reader = PcmFileReader(pcmFile).also { it.open() }
        val buffer = ByteArray(100)
        reader.getPcmBuffer(buffer)
        val bb = ByteBuffer.wrap(buffer)
        bb.order(ByteOrder.LITTLE_ENDIAN)
        val shorts = bb
        var i = 0
        while (shorts.hasRemaining()) {
            Assert.assertEquals("", i.toByte(), shorts.get())
            i++
        }
        reader.release()
        Assert.assertTrue(file.delete())
    }
}