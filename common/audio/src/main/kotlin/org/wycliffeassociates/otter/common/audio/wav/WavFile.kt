/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
import java.io.IOException
import java.lang.Exception
import java.nio.ByteBuffer
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.*


enum class WavType {
    NORMAL_WAV,
    WAV_WITH_EXTENSION
}
class InvalidWavFileException(message: String? = null) : Exception(message)

/**
 * Wraps a file for the purposes of reading wav header metadata
 */
class WavFile private constructor() : AudioFormatStrategy {

    val logger = LoggerFactory.getLogger(WavFile::class.java)

    internal lateinit var file: File
        private set

    override val sampleRate: Int
        get() = header.sampleRate

    override val channels: Int
        get() = header.channels

    override val bitsPerSample: Int
        get() = header.bitsPerSample

    val frameSizeInBytes: Int
        get() = header.blockAlign

    override val totalFrames: Int
        get() = totalAudioLength / frameSizeInBytes

    val headerSize
        get() = header.totalHeaderSize

    override fun addCue(location: Int, label: String) {
        metadata.addCue(location, label)
    }

    override fun getCues(): List<AudioCue> {
        return metadata.getCues()
    }

    val totalAudioLength: Int
        get() = header.totalAudioLength

    internal val totalDataLength: Int
        get() = header.totalDataLength
    var header: WavHeader = WavHeader()
        private set

    override var metadata = WavMetadata()
        private set

    val hasMetadata
        get() = metadata.totalSize > 0

    var wavType = WavType.NORMAL_WAV

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

        header = WavHeader()

        val parseResult = header.parse(file)
        when (parseResult) {
            WavHeaderParseResult.VALID_EXTENDED_HEADER_WAV -> wavType = WavType.WAV_WITH_EXTENSION
            WavHeaderParseResult.VALID_NORMAL_WAV -> wavType = WavType.NORMAL_WAV
        }

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
        header = WavHeader()
        initializeWavFile()
    }

    internal fun writeMetadata(outputStream: OutputStream) {
        metadata.writeMetadata(outputStream)
    }

    /**
     * Updates the wav header with the file size and audio size to the riff and data chunks respectively
     */
    @Throws(IOException::class)
    internal fun finishWrite(totalAudioLength: Int) {
        header.totalAudioLength = totalAudioLength
        header.totalDataLength = headerSize - CHUNK_HEADER_SIZE + totalAudioLength + metadata.totalSize
    }

    internal fun initializeWavFile() {
        header.totalDataLength = headerSize - CHUNK_HEADER_SIZE
        header.totalAudioLength = 0

        FileOutputStream(file, false).use {
            it.write(header.generateHeaderArray())
        }
    }

    private fun parseMetadata() {
        val nonMetadataSize = totalAudioLength + (headerSize - CHUNK_HEADER_SIZE)
        if (totalDataLength > nonMetadataSize) {
            try {
                val metadataSize = totalDataLength - nonMetadataSize
                val bytes = ByteArray(metadataSize)
                file.inputStream().use {
                    val metadataStart = headerSize + wordAlign(totalAudioLength)
                    it.skip(metadataStart.toLong())
                    it.read(bytes)
                }
                metadata.parseMetadata(ByteBuffer.wrap(bytes))
            } catch (e: Exception) {
                logger.error("Error parsing metadata for file: ${file.name}", e)
            }
        }
    }

    fun sampleIndex(sample: Int) = sample * frameSizeInBytes

    /**
     * Updates the wav file, writing out any changes made to the wav file metadata.
     */
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

    override fun reader(start: Int?, end: Int?): AudioFileReader {
        return WavFileReader(this, start, end)
    }

    override fun writer(append: Boolean, buffered: Boolean): OutputStream {
        return WavOutputStream(this, append, buffered)
    }
}
