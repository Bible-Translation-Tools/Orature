package org.wycliffeassociates.otter.common.wav

import java.io.OutputStream
import java.nio.ByteBuffer

class WavMetadata {

    private val cueChunk = CueChunk()
    private val chunks = setOf<RiffChunk>(cueChunk)

    val totalSize
        get() = run {
            var sum = 0
            for (chunk in chunks) {
                sum += chunk.totalSize
            }
            sum
        }

    fun parseMetadata(buffer: ByteBuffer) {
        for (parser in chunks) {
            parser.parse(buffer.slice())
        }
    }

    fun writeMetadata(out: OutputStream) {
        for (chunk in chunks) {
            out.write(chunk.create())
        }
    }

    fun addCue(location: Int, label: String) {
        cueChunk.addCue(WavCue(location, label))
    }

    fun getCues(): List<WavCue> {
        return cueChunk.cues
    }
}