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
        TODO("Not yet implemented")
    }

    override fun getCues(): List<AudioCue> {
        TODO("Not yet implemented")
    }

    override fun update() {
        TODO("Not yet implemented")
    }

    override fun reader(start: Int?, end: Int?): AudioFileReader {
        return PcmFileReader(this, start, end)
    }

    override fun writer(append: Boolean, buffered: Boolean): OutputStream {
        return PcmOutputStream(this, append, buffered)
    }
}
