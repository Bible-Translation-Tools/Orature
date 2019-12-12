package org.wycliffeassociates.otter.common.audio.wav

import java.nio.ByteBuffer

interface RiffChunk {
    fun parse(chunk: ByteBuffer)
    fun create(): ByteArray
    val totalSize: Int
}