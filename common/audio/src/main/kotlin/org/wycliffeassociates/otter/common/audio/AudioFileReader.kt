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
package org.wycliffeassociates.otter.common.audio

interface AudioFileReader {
    val sampleRate: Int
    val channels: Int
    val sampleSize: Int
    val framePosition: Int
    val totalFrames: Int
    fun hasRemaining(): Boolean

    /**
     * Reads from the underlying audio file at the current frame position and writes
     * decoded PCM data to the provided buffer.
     *
     * @param bytes A byte array to write PCM data to
     *
     * @return the number of bytes written. This will be either the size of the byte array
     * or less in the case of end of file, or end of a frame limit set on the reader. Data in
     * the buffer beyond this value are invalid.
     */
    fun getPcmBuffer(bytes: ByteArray): Int
    fun seek(sample: Int)
    fun open()
    fun release()
}
