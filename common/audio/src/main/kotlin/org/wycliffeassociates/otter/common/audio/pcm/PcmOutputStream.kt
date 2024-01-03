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

import java.io.BufferedOutputStream
import java.io.Closeable
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * Created by sarabiaj on 10/4/2016.
 */
class PcmOutputStream @Throws(FileNotFoundException::class)
@JvmOverloads constructor(
    private val pcm: PcmFile,
    private val append: Boolean = true,
    private val buffered: Boolean = false
) : OutputStream(), Closeable, AutoCloseable {

    private val outputStream: OutputStream
    private lateinit var bos: BufferedOutputStream

    init {
        outputStream = FileOutputStream(pcm.file, append)
        if (buffered) {
            bos = BufferedOutputStream(outputStream)
        }
    }

    @Throws(IOException::class)
    override fun write(oneByte: Int) {
        if (buffered) {
            bos.write(oneByte)
        } else {
            outputStream.write(oneByte)
        }
    }

    @Throws(IOException::class)
    override fun flush() {
        if (buffered) {
            bos.flush()
        }
        outputStream.flush()
    }

    @Throws(IOException::class)
    override fun write(bytes: ByteArray) {
        if (buffered) {
            bos.write(bytes)
        } else {
            outputStream.write(bytes)
        }
    }

    @Throws(IOException::class)
    override fun close() {
        if (buffered) {
            bos.flush()
        }
        outputStream.flush()
        outputStream.close()
    }
}
