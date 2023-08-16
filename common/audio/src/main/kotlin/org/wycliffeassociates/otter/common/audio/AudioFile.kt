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

import org.wycliffeassociates.otter.common.audio.mp3.MP3FileReader
import java.io.File
import java.io.OutputStream
import org.wycliffeassociates.otter.common.audio.pcm.PcmFile
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata

const val DEFAULT_SAMPLE_RATE = 44100
const val DEFAULT_CHANNELS = 1
const val DEFAULT_BITS_PER_SAMPLE = 16

open class AudioFile protected constructor() {

    lateinit var file: File
        private set

    private lateinit var strategy: AudioFormatStrategy

    val metadata: AudioMetadata
        get() = strategy.metadata

    constructor(file: File, metadata: AudioMetadata) : this() {
        this.file = file
        strategy = strategySelector(file, metadata)
    }

    constructor(file: File) : this() {
        this.file = file
        strategy = strategySelector(file)
    }

    constructor(
        file: File,
        channels: Int = DEFAULT_CHANNELS,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE
    ) : this() {
        this.file = file
        strategy = strategySelector(file, channels, sampleRate, bitsPerSample)
    }

    constructor(
        file: File,
        channels: Int = DEFAULT_CHANNELS,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE,
        metadata: AudioMetadata
    ) : this() {
        this.file = file
        strategy = strategySelector(file, channels, sampleRate, bitsPerSample, metadata)
    }

    open fun strategySelector(file: File): AudioFormatStrategy {
        return when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFile(file)
            AudioFileFormat.MP3 -> MP3FileReader(file).apply {
                release()
            }
            AudioFileFormat.PCM -> PcmFile(file)
        }
    }

    open fun strategySelector(file: File, metadata: AudioMetadata): AudioFormatStrategy {
        return when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFile(file, metadata as WavMetadata)
            AudioFileFormat.MP3 -> MP3FileReader(file).apply {
                release() // clean up resource after parsing the metadata
            }
            AudioFileFormat.PCM -> PcmFile(file)
        }
    }

    open fun strategySelector(
        file: File,
        channels: Int = DEFAULT_CHANNELS,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE
    ): AudioFormatStrategy {
        return when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFile(file, channels, sampleRate, bitsPerSample)
            AudioFileFormat.MP3 -> MP3FileReader(file).apply {
                release()
            }
            AudioFileFormat.PCM -> PcmFile(file)
        }
    }

    open fun strategySelector(
        file: File,
        channels: Int = DEFAULT_CHANNELS,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE,
        metadata: AudioMetadata
    ): AudioFormatStrategy {
        return when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFile(file, channels, sampleRate, bitsPerSample, metadata as WavMetadata)
            AudioFileFormat.MP3 -> MP3FileReader(file).apply {
                release()
            }
            AudioFileFormat.PCM -> PcmFile(file)
        }
    }

    val sampleRate: Int
        get() = strategy.sampleRate

    val channels: Int
        get() = strategy.channels

    val bitsPerSample: Int
        get() = strategy.bitsPerSample

    val frameSizeBytes: Int
        get() = (strategy.bitsPerSample / Byte.SIZE_BITS) * channels

    val totalFrames: Int
        get() = strategy.totalFrames

    open fun update() {
        strategy.update()
    }

    open fun addCue(location: Int, label: String) {
        metadata.addCue(location, label)
    }

    open fun getCues(): List<AudioCue> {
        return metadata.getCues()
    }

    fun clearCues() {
        metadata.clearMarkers()
    }

    fun reader(start: Int? = null, end: Int? = null): AudioFileReader {
        return strategy.reader(start, end)
    }

    fun writer(append: Boolean = false, buffered: Boolean = true): OutputStream {
        return strategy.writer(append, buffered)
    }
}
