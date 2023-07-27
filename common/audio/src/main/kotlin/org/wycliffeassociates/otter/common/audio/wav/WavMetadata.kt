/**
 * Copyright (C) 2020-2022 Wycliffe Associates
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

import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioMetadata
import java.io.OutputStream
import java.nio.ByteBuffer


/**
 * WavMetadata is a class for reading and writing metadata from wav files.
 * The wav file spec details various kinds of metadata chunks that can occur after
 * the audio section of a wav file. Chunks have a standard header, and a chunk size, allowing
 * for parsers to skip over unrecognized chunks.
 *
 * By default, a parser is included for cue chunks
 *
 * @param parsableChunks Parsable chunks are a list of optional parsers which implement the RiffChunk interface.
 * This allows users of the WavMetadata library to add custom parsers beyond what is officially supported.
 */
class WavMetadata(parsableChunks: List<RiffChunk>? = null) : AudioMetadata {

    companion object {

        /**
         * Holds the chunk parsers used to parse Wav metadata
         */
        private var parsers: List<Class<out RiffChunk?>> = arrayListOf<Class<out RiffChunk?>>(CueChunk::class.java)

        /**
         * Allows for configuration of the WavMetadata when using this audio library. As The ideal use of this library
         * is to open a file using the AudioFile class, it can be difficult to configure and or extend custom Wav Chunk
         * parsers beyond what is provided by this library.
         *
         * In your main function, or where application configuration is done prior to use of the AudioFile class,
         * make a call to this function and provide which RiffChunk parsers should be used by the WavMetadata class
         * when reading wav files. All AudioFiles will then use the provided parsers instead of the default configuration.
         * (Currently the default chunks parsed are only CueChunks).
         */
        fun configureParsers(vararg parsers: Class<out RiffChunk>) {
            this.parsers = arrayListOf(*parsers)
        }
    }

    private val logger = LoggerFactory.getLogger(WavMetadata::class.java)

    private val cueChunk: CueChunk
    private val chunks: Set<RiffChunk>

    init {
        chunks = mutableSetOf()
        parsers.forEach {
            val chunk = it.getConstructor().newInstance()
            chunk?.let { chunk ->
                chunks.add(chunk)
            }
        }

        if (parsableChunks != null) {
            chunks.addAll(parsableChunks)
        }
        val cue = chunks.find { it is CueChunk }
        if (cue != null) {
            cueChunk = cue as CueChunk
        } else {
            cueChunk = CueChunk()
            chunks.add(cueChunk)
        }
    }

    val totalSize
        get() = chunks.sumOf { it.totalSize }

    fun parseMetadata(buffer: ByteBuffer) {
        chunks.forEach { it.parse(buffer.slice()) }
    }

    fun writeMetadata(out: OutputStream) {
        chunks.forEach { out.write(it.toByteArray()) }
    }

    override fun addCue(location: Int, label: String) {
        cueChunk.addCue(AudioCue(location, label))
    }

    override fun getCues(): List<AudioCue> {
        return cueChunk.cues
    }

    override fun clearMarkers() {
        cueChunk.clearCues()
    }

    override fun artists(): List<String> {
        TODO()
    }

    override fun setArtists(artists: List<String>) {
        TODO()
    }

    override fun getLegalInformationUrl(): String {
        TODO()
    }

    override fun setLegalInformationUrl(url: String) {
        TODO()
    }
}
