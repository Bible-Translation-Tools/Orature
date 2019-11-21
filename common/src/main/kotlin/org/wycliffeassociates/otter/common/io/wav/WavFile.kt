package org.wycliffeassociates.otter.common.io.wav

import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Created by sarabiaj on 6/2/2016.
 */
class WavFile(internal val file: File) {

    companion object {
        const val SAMPLE_RATE = 44100
        const val NUM_CHANNELS = 1
        const val HEADER_SIZE = 44
        const val AUDIO_LENGTH_LOCATION = 40
        const val BIT_RATE = 16
        const val PCM = 1
    }

    internal var totalAudioLength = 0
    internal var totalDataLength = 0

    init {
        parseHeader()
    }

    @Throws(IOException::class)
    fun finishWrite(totalAudioLength: Int) {
        this.totalAudioLength = totalAudioLength
    }

    fun initializeWavFile() {
        totalDataLength = HEADER_SIZE - 8 // the 8 accounts for chunk id and chunk size fields
        totalAudioLength = 0

        FileOutputStream(file, false).use {
            it.write(generateHeaderArray())
        }
    }

    // http://soundfile.sapp.org/doc/WaveFormat/ for equations
    private fun generateHeaderArray(): ByteArray {
        val header = ByteBuffer.allocate(HEADER_SIZE)
        val longSampleRate = SAMPLE_RATE
        val byteRate = (BIT_RATE * SAMPLE_RATE * NUM_CHANNELS) / 8

        header.order(ByteOrder.BIG_ENDIAN)
        header.put("RIFF".toByteArray(Charsets.US_ASCII))

        header.order(ByteOrder.LITTLE_ENDIAN)
        header.putInt(totalDataLength)

        header.order(ByteOrder.BIG_ENDIAN)
        header.put("WAVE".toByteArray(Charsets.US_ASCII))
        header.put("fmt ".toByteArray(Charsets.US_ASCII))

        header.order(ByteOrder.LITTLE_ENDIAN)
        header.putInt(BIT_RATE)
        header.putShort(PCM.toShort()) // format = 1 for pcm
        header.putShort(NUM_CHANNELS.toShort()) // number of channels
        header.putInt(longSampleRate)
        header.putInt(byteRate)
        header.putShort(((NUM_CHANNELS * BIT_RATE) / 8).toShort()) // block align
        header.putShort(BIT_RATE.toShort()) // bits per sample

        header.order(ByteOrder.BIG_ENDIAN)
        header.put("data".toByteArray(Charsets.US_ASCII))

        header.order(ByteOrder.LITTLE_ENDIAN)
        header.putInt(totalAudioLength) // initial size

        header.flip()

        return header.array()
    }

    private fun parseHeader() {
        if (file.length() >= HEADER_SIZE) {
            RandomAccessFile(file, "r").use {
                val header = ByteArray(HEADER_SIZE)
                it.read(header)
                val bb = ByteBuffer.wrap(header)
                bb.order(ByteOrder.LITTLE_ENDIAN)
                // Skip over "RIFF"
                bb.int
                this.totalDataLength = bb.int
                // Seek to the audio length field
                bb.position(AUDIO_LENGTH_LOCATION)
                totalAudioLength = bb.int
            }
        } else {
            initializeWavFile()
        }
    }
}
