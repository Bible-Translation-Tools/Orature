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
