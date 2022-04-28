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
package org.wycliffeassociates.otter.common.audio

import java.io.File
import java.io.OutputStream
import org.wycliffeassociates.otter.common.audio.mp3.MP3FileReader
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavFileReader
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream

const val DEFAULT_SAMPLE_RATE = 44100
const val DEFAULT_CHANNELS = 1
const val DEFAULT_BITS_PER_SAMPLE = 16

class AudioFile private constructor() {

    lateinit var file: File
        private set

    private lateinit var strategy: AudioFormatStrategy

    val metadata: AudioMetadata
        get() = strategy.metadata

    constructor(file: File, metadata: AudioMetadata) : this() {
        this.file = file
        strategy = when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFile(file, metadata as WavMetadata)
            AudioFileFormat.MP3 -> MP3FileReader(file).apply {
                release() // clean up resource after parsing the metadata
            }
        }
    }

    constructor(file: File) : this() {
        this.file = file
        strategy = when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFile(file)
            AudioFileFormat.MP3 -> MP3FileReader(file).apply {
                release()
            }
        }
    }

    constructor(
        file: File,
        channels: Int = DEFAULT_CHANNELS,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE
    ) : this() {
        this.file = file
        strategy = when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFile(file, channels, sampleRate, bitsPerSample)
            AudioFileFormat.MP3 -> MP3FileReader(file).apply {
                release()
            }
        }
    }

    constructor(
        file: File,
        channels: Int = DEFAULT_CHANNELS,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE,
        metadata: AudioMetadata
    ) : this() {
        this.file = file
        strategy = when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFile(file, channels, sampleRate, bitsPerSample, metadata as WavMetadata)
            AudioFileFormat.MP3 -> MP3FileReader(file).apply {
                release()
            }
        }
    }

    val sampleRate: Int
        get() = strategy.sampleRate

    val channels: Int
        get() = strategy.channels

    val bitsPerSample: Int
        get() = strategy.bitsPerSample

    val totalFrames: Int
        get() = strategy.totalFrames

    fun update() {
        strategy.update()
    }

    fun reader(start: Int? = null, end: Int? = null): AudioFileReader {
        return when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFileReader(strategy as WavFile, start, end)
            AudioFileFormat.MP3 -> MP3FileReader(file, start, end)
        }
    }

    fun writer(append: Boolean = false, buffered: Boolean = true): OutputStream {
        return when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavOutputStream(strategy as WavFile, append, buffered)
            AudioFileFormat.MP3 -> TODO()
        }
    }
}
