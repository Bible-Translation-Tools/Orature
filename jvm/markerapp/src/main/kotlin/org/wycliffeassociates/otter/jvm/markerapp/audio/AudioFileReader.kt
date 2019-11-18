package org.wycliffeassociates.otter.jvm.markerapp.audio

interface AudioFileReader {
    val sampleRate: Int
    val channels: Int
    val sampleSize: Int
    val framePosition: Int
    fun hasRemaining(): Boolean
    fun getPcmBuffer(bytes: ByteArray): Int
    fun seek(sample: Int)
}