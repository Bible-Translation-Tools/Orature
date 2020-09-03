package org.wycliffeassociates.otter.common.audio.wav

import org.wycliffeassociates.otter.common.audio.AudioFileReader
import java.io.RandomAccessFile
import java.lang.Integer.max
import java.lang.Integer.min
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class WavFileReader(val wav: WavFile, val start: Int? = null, val end: Int? = null) :
    AudioFileReader {

    override val sampleRate: Int = wav.sampleRate
    override val channels: Int = wav.channels
    override val sampleSize: Int = wav.bitsPerSample
    override val framePosition: Int
        get() = mappedFile.position() / wav.frameSizeInBytes

    private val mappedFile: MappedByteBuffer

    init {
        val totalFrames = wav.totalFrames
        var begin = if (start != null) min(max(0, start), totalFrames) else 0
        var end = if (end != null) min(max(begin, end), totalFrames) else totalFrames
        begin *= wav.frameSizeInBytes
        begin += WAV_HEADER_SIZE
        end *= wav.frameSizeInBytes
        end += WAV_HEADER_SIZE
        mappedFile =
            RandomAccessFile(wav.file, "r").use {
                it.channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    begin.toLong(),
                    (end - begin).toLong()
                )
            }
    }

    override fun getPcmBuffer(bytes: ByteArray): Int {
        val written = mappedFile.remaining().coerceAtMost(bytes.size)
        mappedFile.get(bytes, 0, written)
        return written
    }

    @Throws(ArrayIndexOutOfBoundsException::class)
    override fun seek(sample: Int) {
        val index = min(wav.sampleIndex(sample), mappedFile.limit())
        mappedFile.position(index)
    }

    override fun hasRemaining() = mappedFile.hasRemaining()

    override val totalFrames = wav.totalAudioLength / wav.frameSizeInBytes
}
