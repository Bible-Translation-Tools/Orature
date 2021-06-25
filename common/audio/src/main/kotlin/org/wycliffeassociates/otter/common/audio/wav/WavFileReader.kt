package org.wycliffeassociates.otter.common.audio.wav

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import java.io.RandomAccessFile
import java.lang.Exception
import java.lang.IllegalStateException
import java.lang.Integer.max
import java.lang.Integer.min
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

internal class WavFileReader(val wav: WavFile, val start: Int? = null, val end: Int? = null) : AudioFileReader {

    private val logger = LoggerFactory.getLogger(WavFileReader::class.java)

    override val totalFrames = wav.totalFrames
    override val sampleRate: Int = wav.sampleRate
    override val channels: Int = wav.channels
    override val sampleSize: Int = wav.bitsPerSample
    override val framePosition: Int
        get() = (mappedFile?.position() ?: 0) / wav.frameSizeInBytes

    private var mappedFile: MappedByteBuffer? = null
    private var channel: FileChannel? = null

    override fun open() {
        mappedFile?.let { release() }
        val totalFrames = wav.totalFrames
        var begin = if (start != null) min(max(0, start), totalFrames) else 0
        var end = if (end != null) min(max(begin, end), totalFrames) else totalFrames
        begin *= wav.frameSizeInBytes
        begin += WAV_HEADER_SIZE
        end *= wav.frameSizeInBytes
        end += WAV_HEADER_SIZE
        mappedFile =
            RandomAccessFile(wav.file, "r").use {
                channel = it.channel
                channel!!.map(
                    FileChannel.MapMode.READ_ONLY,
                    begin.toLong(),
                    (end - begin).toLong()
                )
            }
    }

    override fun getPcmBuffer(bytes: ByteArray): Int {
        mappedFile?.let { _mappedFile ->
            val written = _mappedFile.remaining().coerceAtMost(bytes.size)
            _mappedFile.get(bytes, 0, written)
            return written
        } ?: run {
            throw IllegalStateException("Tried to get pcm buffer before opening file")
        }
    }

    @Throws(ArrayIndexOutOfBoundsException::class)
    override fun seek(sample: Int) {
        mappedFile?.let { _mappedFile ->
            val index = min(wav.sampleIndex(sample), _mappedFile.limit())
            _mappedFile.position(index)
        } ?: run {
            throw IllegalStateException("Tried to seek before opening file")
        }
    }

    override fun hasRemaining(): Boolean {
        return mappedFile?.hasRemaining() ?: throw IllegalStateException("hasRemaining called before opening file")
    }

    override fun release() {
        if (mappedFile != null) {
            try {
                // https://stackoverflow.com/questions/25238110/how-to-properly-close-mappedbytebuffer/25239834#25239834
                // TODO: Replace with https://docs.oracle.com/en/java/javase/14/docs/api/jdk.incubator.foreign/jdk/incubator/foreign/MemorySegment.html#ofByteBuffer(java.nio.ByteBuffer)
                val unsafeClass = Class.forName("sun.misc.Unsafe")
                val unsafeField = unsafeClass.getDeclaredField("theUnsafe")
                unsafeField.isAccessible = true
                val unsafe: Any = unsafeField.get(null)
                val invokeCleaner = unsafeClass.getMethod("invokeCleaner", ByteBuffer::class.java)
                invokeCleaner.invoke(unsafe, mappedFile)
            } catch (e: Exception) {
                logger.error("Error releasing memory mapped file: ${wav.file.name}", e)
            }
            channel?.close()
            mappedFile = null
            channel = null
            System.gc()
        }
    }
}
