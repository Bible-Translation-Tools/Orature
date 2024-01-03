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

import org.junit.Assert
import org.junit.Test
import java.io.File
import java.io.FileInputStream

class PcmIOTest {

    @Test
    fun `buffered pcm produces equivalent file`() {
        val temp = File.createTempFile("testpcm", ".pcm")
        val temp2 = File.createTempFile("test2pcm", ".pcm")
        val pcm = PcmFile(temp)
        val pcm2 = PcmFile(temp2)

        val audioSamples = 700_000

        PcmOutputStream(
            pcm,
            append = false,
            buffered = false
        ).use {
            for (i in 1..audioSamples) {
                it.write(i)
            }
        }
        PcmOutputStream(
            pcm2,
            append = false,
            buffered = true
        ).use {
            for (i in 1..audioSamples) {
                it.write(i)
            }
        }

        FileInputStream(temp).use { ifs ->
            FileInputStream(temp2).use { ifs2 ->
                val array = ifs.readAllBytes()
                val array2 = ifs2.readAllBytes()
                Assert.assertEquals("file size is the same", array.size, array2.size)
                for (i in array.indices) {
                    Assert.assertEquals(array[i], array2[i])
                }
            }
        }
        temp.delete()
        temp2.delete()
    }

    @Test
    fun `writing byte array buffered pcm produces equivalent file`() {
        val temp = File.createTempFile("testpcm", ".pcm")
        val temp2 = File.createTempFile("test2pcm", ".pcm")
        val pcm = PcmFile(temp)
        val pcm2 = PcmFile(temp2)

        val audioSamples = 700_000
        val byteArray = ByteArray(audioSamples)
        for (i in 0 until audioSamples) {
            byteArray[i] = (i % 255).toByte()
        }

        PcmOutputStream(
            pcm,
            append = false,
            buffered = false
        ).use {
            it.write(byteArray)
        }
        PcmOutputStream(
            pcm2,
            append = false,
            buffered = true
        ).use {
            it.write(byteArray)
        }

        FileInputStream(temp).use { ifs ->
            FileInputStream(temp2).use { ifs2 ->
                val array = ifs.readAllBytes()
                val array2 = ifs2.readAllBytes()
                Assert.assertEquals("file size is the same", array.size, array2.size)
                for (i in array.indices) {
                    Assert.assertEquals(array[i], array2[i])

                    Assert.assertEquals("read file matches written bytes", array[i], byteArray[i])
                }
            }
        }
        temp.delete()
        temp2.delete()
    }
}