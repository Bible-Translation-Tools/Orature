package org.wycliffeassociates.otter.common.audio

interface AudioFileReader {
    val sampleRate: Int
    val channels: Int
    val sampleSize: Int
    val framePosition: Int
    val totalFrames: Int
    fun hasRemaining(): Boolean
    fun getPcmBuffer(bytes: ByteArray): Int
    fun seek(sample: Int)
}