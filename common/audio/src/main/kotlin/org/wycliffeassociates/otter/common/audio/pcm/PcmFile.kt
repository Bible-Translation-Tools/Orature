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

import org.wycliffeassociates.otter.common.audio.*
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import java.io.File
import java.io.OutputStream

class PcmFile private constructor() : AudioFormatStrategy {
    internal lateinit var file: File
        private set

    val frameSizeInBytes: Int
        get() = channels * (bitsPerSample / 8)

    val totalAudioLength: Int
        get() = file.length().toInt()

    override val sampleRate: Int = DEFAULT_SAMPLE_RATE
    override val channels: Int = DEFAULT_CHANNELS
    override val bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE
    override val totalFrames: Int
        get() = totalAudioLength / frameSizeInBytes

    override var metadata = WavMetadata()
        private set

    constructor(file: File) : this() {
        this.file = file
    }

    fun sampleIndex(sample: Int) = sample * frameSizeInBytes

    override fun addCue(location: Int, label: String) {
    }

    override fun getCues(): List<AudioCue> {
        return listOf()
    }

    override fun update() {
    }

    override fun reader(start: Int?, end: Int?): AudioFileReader {
        return PcmFileReader(this, start, end)
    }

    override fun writer(append: Boolean, buffered: Boolean): OutputStream {
        return PcmOutputStream(this, append, buffered)
    }
}
