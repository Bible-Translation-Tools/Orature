package org.wycliffeassociates.otter.common.audio.wav

import java.nio.ByteBuffer

internal const val CHUNK_HEADER_SIZE = 8
internal const val CHUNK_LABEL_SIZE = 4
internal const val DWORD_SIZE = 4

interface RiffChunk {
    fun parse(chunk: ByteBuffer)
    fun toByteArray(): ByteArray
    val totalSize: Int
}
