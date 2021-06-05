package org.wycliffeassociates.otter.common.audio

import java.io.File
import java.io.OutputStream
import javax.sound.sampled.spi.AudioFileWriter
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

    private lateinit var strategy: AudioFileFormat

    val metadata: AudioMetadata
        get() = strategy.metadata

    constructor(file: File, metadata: AudioMetadata) : this() {
        this.file = file
        strategy = when (file.extension) {
            "wav" -> WavFile(file)
            else -> WavFile(file)
        }
    }

    constructor(file: File, wavMetadata: WavMetadata = WavMetadata()) : this() {
        this.file = file
        strategy = when (file.extension) {
            "wav" -> WavFile(file)
            else -> WavFile(file)
        }
    }

    constructor(
        file: File,
        channels: Int = DEFAULT_CHANNELS,
        sampleRate: Int = DEFAULT_SAMPLE_RATE,
        bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE,
        wavMetadata: WavMetadata = WavMetadata()
    ) : this() {
        this.file = file
        strategy = when (file.extension) {
            "wav" -> WavFile(file, channels, sampleRate, bitsPerSample, wavMetadata)
            else -> WavFile(file)
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
        return when(file.extension) {
            "wav" -> WavFileReader(strategy as WavFile, start, end)
            else -> WavFileReader(strategy as WavFile, start, end)
        }
    }

    fun writer(append:Boolean = false, buffered: Boolean = true): OutputStream {
        return when(file.extension) {
            "wav" -> WavOutputStream(strategy as WavFile, append, buffered)
            else -> WavOutputStream(strategy as WavFile, append, buffered)
        }
    }
}
