package org.wycliffeassociates.otter.common.io.wav

import java.nio.ByteBuffer

interface RiffChunk {
    fun parse(chunk: ByteBuffer)
    fun toByteArray(): ByteArray
    val totalSize: Int
}