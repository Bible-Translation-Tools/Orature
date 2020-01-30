package org.wycliffeassociates.otter.common.io.wav

import org.wycliffeassociates.otter.common.io.AudioFileReader
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class WavFileReader(val wav: WavFile) : AudioFileReader {

    override val sampleRate: Int = wav.sampleRate
    override val channels: Int = wav.channels
    override val sampleSize: Int = wav.bitsPerSample
    override val framePosition: Int
        get() = mappedFile.position() / wav.frameSizeInBytes

    private val mappedFile: MappedByteBuffer =
        RandomAccessFile(wav.file, "r").use {
            it.channel.map(
                FileChannel.MapMode.READ_ONLY,
                44,
                wav.totalAudioLength.toLong()
            )
        }

    override fun getPcmBuffer(bytes: ByteArray): Int {
        val written = mappedFile.remaining().coerceAtMost(bytes.size)
        mappedFile.get(bytes, 0, written)
        return written
    }

    @Throws(ArrayIndexOutOfBoundsException::class)
    override fun seek(sample: Int) {
        val index = wav.sampleIndex(sample)
        mappedFile.position(index)
    }

    override fun hasRemaining() = mappedFile.hasRemaining()

    override val totalFrames = wav.totalAudioLength / wav.frameSizeInBytes
}