package org.wycliffeassociates.otter.common.audio

import java.io.File
import java.io.OutputStream
import org.wycliffeassociates.otter.common.audio.wav.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.wav.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.wav.DEFAULT_SAMPLE_RATE
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import org.wycliffeassociates.otter.common.audio.wav.WavFileReader
import org.wycliffeassociates.otter.common.audio.wav.WavMetadata
import org.wycliffeassociates.otter.common.audio.wav.WavOutputStream

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
        }
    }

    constructor(file: File) : this() {
        this.file = file
        strategy = when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFile(file)
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
        }
    }

    fun writer(append: Boolean = false, buffered: Boolean = true): OutputStream {
        return when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavOutputStream(strategy as WavFile, append, buffered)
        }
    }
}
