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
            AudioFileFormat.MP3 -> MP3FileReader(file)
        }
    }

    constructor(file: File) : this() {
        this.file = file
        strategy = when (AudioFileFormat.of(file.extension)) {
            AudioFileFormat.WAV -> WavFile(file)
            AudioFileFormat.MP3 -> MP3FileReader(file)
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
            AudioFileFormat.MP3 -> MP3FileReader(file)
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
            AudioFileFormat.MP3 -> MP3FileReader(file)
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
