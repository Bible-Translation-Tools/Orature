package org.wycliffeassociates.otter.common.audio.wav

import java.io.OutputStream
import java.nio.ByteBuffer

class WavMetadata(parsableChunks: List<RiffChunk>? = null) {

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

    fun addCue(location: Int, label: String) {
        cueChunk.addCue(WavCue(location, label))
    }

    fun getCues(): List<WavCue> {
        return cueChunk.cues
    }
}
