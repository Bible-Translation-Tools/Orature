/**
 * Copyright (C) 2020, 2021 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.common.audio.wav

import java.io.File
import java.io.OutputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioFormatStrategy
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.DEFAULT_BITS_PER_SAMPLE
import org.wycliffeassociates.otter.common.audio.DEFAULT_CHANNELS
import org.wycliffeassociates.otter.common.audio.DEFAULT_SAMPLE_RATE

private const val RIFF = "RIFF"
private const val WAVE = "WAVE"
private const val FMT = "fmt "
private const val DATA = "data"
private const val PCM: Short = 1

internal const val WAV_HEADER_SIZE = 44
private const val AUDIO_LENGTH_LOCATION = 40
private const val PCM_POSITION = 20
private const val CHANNEL_POSITION = 22
private const val SAMPLE_RATE_POSITION = 24
private const val BITS_PER_SAMPLE_POSITION = 34
private const val BITS_IN_BYTE = 8

class InvalidWavFileException(message: String? = null) : Exception(message)

/**
 * Wraps a file for the purposes of reading wav header metadata
 */
internal class WavFile private constructor() : AudioFormatStrategy {

    val logger = LoggerFactory.getLogger(WavFile::class.java)

    internal lateinit var file: File
        private set

    override var sampleRate: Int = DEFAULT_SAMPLE_RATE
        private set
    override var channels: Int = DEFAULT_CHANNELS
        private set
    override var bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE
        private set
    val frameSizeInBytes = channels * (bitsPerSample / BITS_IN_BYTE)

    override val totalFrames: Int
        get() = totalAudioLength / frameSizeInBytes

    override fun addCue(location: Int, label: String) {
        metadata.addCue(location, label)
    }

    override fun getCues(): List<AudioCue> {
        return metadata.getCues()
    }

    var totalAudioLength = 0
        internal set

    internal var totalDataLength = 0

    override var metadata = WavMetadata()
        private set

    val hasMetadata
        get() = metadata.totalSize > 0

    /**
     * Reads the file header of the provided wav file.
     *
     * @param file the file to read
     *
     * @throws InvalidWavFileException Throws an exception if the file length
     * is less than HEADER_SIZE bytes or if the header provides invalid
     * information suggesting the file is not a wav file.
     */
    @Throws(InvalidWavFileException::class)
    constructor(file: File, wavMetadata: WavMetadata = WavMetadata()) : this() {
        this.file = file
        this.metadata = wavMetadata
        parseHeader()
        parseMetadata()
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
        bitsPerSample: Int = DEFAULT_BITS_PER_SAMPLE,
        wavMetadata: WavMetadata = WavMetadata()
    ) : this() {
        this.file = file
        this.metadata = wavMetadata
        this.channels = channels
        this.sampleRate = sampleRate
        this.bitsPerSample = bitsPerSample
        initializeWavFile()
    }

    internal fun writeMetadata(outputStream: OutputStream) {
        metadata.writeMetadata(outputStream)
    }

    @Throws(IOException::class)
    internal fun finishWrite(totalAudioLength: Int) {
        this.totalAudioLength = totalAudioLength
        this.totalDataLength = WAV_HEADER_SIZE - CHUNK_HEADER_SIZE + totalAudioLength + metadata.totalSize
    }

    internal fun initializeWavFile() {
        totalDataLength = WAV_HEADER_SIZE - CHUNK_HEADER_SIZE
        totalAudioLength = 0

        FileOutputStream(file, false).use {
            it.write(generateHeaderArray())
        }
    }

    // http://soundfile.sapp.org/doc/WaveFormat/ for equations
    private fun generateHeaderArray(): ByteArray {
        val header = ByteBuffer.allocate(WAV_HEADER_SIZE)
        val longSampleRate = sampleRate
        val byteRate = (bitsPerSample * sampleRate * channels) / BITS_IN_BYTE

        header.order(ByteOrder.LITTLE_ENDIAN)
        header.put(RIFF.toByteArray(Charsets.US_ASCII))
        header.putInt(totalDataLength)
        header.put(WAVE.toByteArray(Charsets.US_ASCII))
        header.put(FMT.toByteArray(Charsets.US_ASCII))
        header.putInt(bitsPerSample)
        header.putShort(PCM) // format = 1 for pcm
        header.putShort(channels.toShort()) // number of channels
        header.putInt(longSampleRate)
        header.putInt(byteRate)
        header.putShort(((channels * bitsPerSample) / BITS_IN_BYTE).toShort()) // block align
        header.putShort(bitsPerSample.toShort()) // bits per sample
        header.put(DATA.toByteArray(Charsets.US_ASCII))
        header.putInt(totalAudioLength) // initial size

        header.flip()

        return header.array()
    }

    @Throws(InvalidWavFileException::class)
    private fun parseHeader() {
        if (file.length() >= WAV_HEADER_SIZE) {
            RandomAccessFile(file, "r").use {
                val header = ByteArray(WAV_HEADER_SIZE)
                it.read(header)
                val bb = ByteBuffer.wrap(header)
                bb.order(ByteOrder.LITTLE_ENDIAN)
                val riff = bb.getText(CHUNK_LABEL_SIZE)
                this.totalDataLength = bb.int
                val wave = bb.getText(CHUNK_LABEL_SIZE)
                val fmt = bb.getText(CHUNK_LABEL_SIZE)
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
                }
            }
        } else {
            throw InvalidWavFileException()
        }
    }

    private fun parseMetadata() {
        if (totalDataLength > totalAudioLength + (WAV_HEADER_SIZE - CHUNK_HEADER_SIZE)) {
            val metadataSize = totalDataLength - totalAudioLength - (WAV_HEADER_SIZE - CHUNK_HEADER_SIZE)
            val bytes = ByteArray(metadataSize)
            file.inputStream().use {
                val metadataStart = WAV_HEADER_SIZE + totalAudioLength
                it.skip(metadataStart.toLong())
                it.read(bytes)
            }
            try {
                metadata.parseMetadata(ByteBuffer.wrap(bytes))
            } catch (e: Exception) {
                logger.error("Error parsing metadata for file: ${file.name}")
                throw e
            }
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

    override fun update() {
        // the use block will write nothing, but will call .close()
        // which will truncate the file at the end of the audio section,
        // write out metadata, and update the header
        WavOutputStream(
            this,
            append = true,
            buffered = true
        ).use {}
    }
}
