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

import java.io.OutputStream
import java.nio.ByteBuffer
import org.wycliffeassociates.otter.common.audio.AudioMetadata
import org.wycliffeassociates.otter.common.audio.AudioCue

internal class WavMetadata(parsableChunks: List<RiffChunk>? = null): AudioMetadata {

    private val cueChunk: CueChunk
    private val chunks: Set<RiffChunk>

    init {
        chunks = mutableSetOf()
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
        get() = chunks.sumBy { it.totalSize }

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
}
