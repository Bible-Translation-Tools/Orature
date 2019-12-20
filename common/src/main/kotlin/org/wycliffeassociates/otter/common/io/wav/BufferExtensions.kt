package org.wycliffeassociates.otter.common.io.wav

import java.nio.BufferUnderflowException
import java.nio.ByteBuffer
import java.nio.charset.Charset

@Throws(BufferUnderflowException::class)
internal fun ByteBuffer.getText(bytesToRead: Int, charset: Charset = Charsets.US_ASCII): String {
    val bytes = ByteArray(bytesToRead)
    this.get(bytes)
    return bytes.toString(charset)
}

internal fun ByteBuffer.seek(bytesToSeek: Int) {
    val bytes = ByteArray(bytesToSeek)
    this.get(bytes)
}