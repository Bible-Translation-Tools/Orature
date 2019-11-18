package org.wycliffeassociates.otter.jvm.markerapp.audio

import java.io.*
import java.lang.Exception
import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset


private const val RIFF = "RIFF"
private const val WAVE = "WAVE"
private const val FMT = "fmt "
private const val DATA = "data"
private const val PCM: Short = 1

private const val DEFAULT_SAMPLE_RATE = 44100
private const val DEFAULT_CHANNELS = 1
private const val DEFAULT_BITS_PER_SAMPLE = 16

private const val HEADER_SIZE = 44
private const val AUDIO_LENGTH_LOCATION = 40
private const val PCM_POSITION = 20
private const val CHANNEL_POSITION = 22
private const val SAMPLE_RATE_POSITION = 24
private const val BITS_PER_SAMPLE_POSITION = 34

class InvalidWavFileException : Exception()

/**
 * Wraps a file for the purposes of reading wav header metadata
 */
class WavFile private constructor() {

    internal lateinit var file: File
        private set

    var sampleRate: Int = 44100
        private set
    var channels: Int = 1
        private set
    var bitsPerSample: Int = 16
        private set
    val frameSizeInBytes = channels * (bitsPerSample / 8)

    internal var totalAudioLength = 0
    internal var totalDataLength = 0

    /**
     * Reads the file header of the provided wav file.
     *
     * @param file the file to read
     *
     * @throws InvalidWavFileException Throws an exception if the file length
     * is less than 44 bytes or if the header provides invalid
     * information suggesting the file is not a wav file.
     */
    @Throws(InvalidWavFileException::class)
    constructor(file: File) : this() {
        this.file = file
        parseHeader()
    }

    /**
     * Initializes a wav file header in the provided empty file. This will overwrite the file
     * if it already contains data.
     *
     * @param file the file to initialize as a wav file
     * @param channels the number of audio channels, default is 1 (mono)
     * @param sampleRate the sample rate, default is 44100 hz
     * @param bitsPerSample the number of bits per sample, default is 16
     */
    constructor(
            file: File,
            channels: Int = DEFAULT_CHANNELS,
            sampleRate: Int = DEFAULT_SAMPLE_RATE,
            bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE
    ) : this() {
        this.file = file
        this.channels = channels
        this.sampleRate = sampleRate
        this.bitsPerSample = bitsPerSample
        initializeWavFile()
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
        val longSampleRate = sampleRate
        val byteRate = (bitsPerSample * sampleRate * channels) / 8

        header.order(ByteOrder.BIG_ENDIAN)
        header.put(RIFF.toByteArray(Charsets.US_ASCII))

        header.order(ByteOrder.LITTLE_ENDIAN)
        header.putInt(totalDataLength)

        header.order(ByteOrder.BIG_ENDIAN)
        header.put(WAVE.toByteArray(Charsets.US_ASCII))
        header.put(FMT.toByteArray(Charsets.US_ASCII))

        header.order(ByteOrder.LITTLE_ENDIAN)
        header.putInt(bitsPerSample)
        header.putShort(PCM.toShort()) // format = 1 for pcm
        header.putShort(channels.toShort()) // number of channels
        header.putInt(longSampleRate)
        header.putInt(byteRate)
        header.putShort(((channels * bitsPerSample) / 8).toShort()) // block align
        header.putShort(bitsPerSample.toShort()) // bits per sample

        header.order(ByteOrder.BIG_ENDIAN)
        header.put(DATA.toByteArray(Charsets.US_ASCII))

        header.order(ByteOrder.LITTLE_ENDIAN)
        header.putInt(totalAudioLength) // initial size

        header.flip()

        return header.array()
    }

    @Throws(InvalidWavFileException::class)
    private fun parseHeader() {
        if (file.length() >= HEADER_SIZE) {
            RandomAccessFile(file, "r").use {
                val header = ByteArray(HEADER_SIZE)
                it.read(header)
                val bb = ByteBuffer.wrap(header)
                bb.order(ByteOrder.LITTLE_ENDIAN)
                val riff = bb.getText(4)
                this.totalDataLength = bb.int
                val wave = bb.getText(4)
                val fmt = bb.getText(4)
                bb.position(PCM_POSITION)
                val pcm = bb.short
                channels = bb.short.toInt()
                sampleRate = bb.int
                bb.position(BITS_PER_SAMPLE_POSITION)
                bitsPerSample = bb.short.toInt()
                // Seek to the audio length field
                bb.position(AUDIO_LENGTH_LOCATION)
                totalAudioLength = bb.int
                if (!validate(riff, wave, fmt, pcm)) {
                    throw InvalidWavFileException()
                } else {
                    println(riff)
                    println(wave)
                    println(fmt)
                    println(pcm)
                    println(sampleRate)
                    println(bitsPerSample)
                    println(channels)
                }
            }
        } else {
            throw InvalidWavFileException()
        }
    }

    private fun validate(
            riff: String,
            wave: String,
            fmt: String,
            pcm: Short
    ): Boolean {
        return booleanArrayOf(
                riff == RIFF,
                wave == WAVE,
                fmt == FMT,
                pcm == PCM
        ).all { true }
    }

    fun sampleIndex(sample: Int) = sample * frameSizeInBytes

    @Throws(BufferUnderflowException::class)
    private fun ByteBuffer.getText(bytesToRead: Int, charset: Charset = Charsets.US_ASCII): String {
        val bytes = ByteArray(bytesToRead)
        this.get(bytes)
        return bytes.toString(charset)
    }
}
