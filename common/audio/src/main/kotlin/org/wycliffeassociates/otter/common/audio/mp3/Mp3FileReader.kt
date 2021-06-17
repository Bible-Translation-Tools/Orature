package org.wycliffeassociates.otter.common.audio.mp3

import java.io.File
import java.io.IOException
import java.lang.Integer.max
import java.lang.Integer.min
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFileReader
import org.wycliffeassociates.otter.common.audio.AudioFormatStrategy
import org.yellowcouch.javazoom.RandomAccessDecoder

// arbitrary size, though setting this too small results in choppy playback
private const val MP3_BUFFER_SIZE = 24576

class MP3FileReader(
    val file: File, start: Int? = null, end: Int? = null
) : AudioFormatStrategy, AudioFileReader {

    val start = start ?: 0
    val end = end ?: Int.MAX_VALUE

    private var pos = min(max(0, this.start), this.end)


    private var decoder: RandomAccessDecoder = RandomAccessDecoder(file.absolutePath)

    override val sampleRate: Int = 44100
    override val channels: Int = 1
    override val sampleSize: Int = 16
    override val framePosition: Int
        get() = pos
    override val totalFrames: Int
        get() = decoder.sampleCount
    override val bitsPerSample = 16

    override val metadata = Mp3Metadata(File(file.parent, "${file.nameWithoutExtension}.cue"))

    override fun addCue(location: Int, label: String) {
        metadata.addCue(location, label)
    }

    override fun getCues(): List<AudioCue> {
        return metadata.getCues()
    }

    override fun update() {
        metadata.write()
    }

    private val buff = ShortArray(MP3_BUFFER_SIZE * 2)

    private fun getPCMData(outBuff: ByteArray, pos: Int) {
        fillBuffers(pos, buff)
        val n = buff.size
        var j = 0
        for (i in 0 until min(n, outBuff.size) step 2) {
            val leftShort = buff[i].toInt()
            outBuff[j++] = (leftShort and 0xff).toByte()
            outBuff[j++] = (leftShort ushr 0x08 and 0xff).toByte()
        }
    }

    private fun fillBuffers(pos: Int, leftRight: ShortArray) {
        val sourceAudio = decoder.audioShorts
        var sourceIdx = 0
        try {
            sourceIdx = decoder.seek(pos, leftRight.size / 2) and RandomAccessDecoder.BUFFER_LAST
        } catch (e: IOException) {
            e.printStackTrace()
        }
        for (i in leftRight.indices) {
            leftRight[i] = sourceAudio[sourceIdx++]
            sourceIdx = sourceIdx and RandomAccessDecoder.BUFFER_LAST
        }
    }

    override fun hasRemaining(): Boolean {
        return framePosition < min(decoder.sampleCount, end)
    }

    override fun getPcmBuffer(bytes: ByteArray): Int {
        getPCMData(bytes, pos)
        pos += bytes.size / 2
        return bytes.size
    }

    override fun seek(sample: Int) {
        pos = max(start, min(sample, end))
    }

    override fun open() {

    }

    override fun release() {
        decoder.stop()
        pos = 0
    }
}
