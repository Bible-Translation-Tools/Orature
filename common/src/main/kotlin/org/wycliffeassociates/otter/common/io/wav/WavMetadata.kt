package org.wycliffeassociates.otter.common.io.wav

import java.io.OutputStream
import java.nio.ByteBuffer

class WavMetadata {

    private val cueChunk = CueChunk()
    private val chunks = setOf<RiffChunk>(cueChunk)

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