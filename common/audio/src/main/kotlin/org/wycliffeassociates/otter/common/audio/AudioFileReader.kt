package org.wycliffeassociates.otter.common.audio

interface AudioFileReader {
    val sampleRate: Int
    val channels: Int
    val sampleSize: Int
    val framePosition: Int
    val totalFrames: Int
    fun hasRemaining(): Boolean

    /**
     * Reads from the underlying audio file at the current frame position and writes
     * decoded PCM data to the provided buffer.
     *
     * @param bytes A byte array to write PCM data to
     *
     * @return the number of bytes written. This will be either the size of the byte array
     * or less in the case of end of file, or end of a frame limit set on the reader. Data in
     * the buffer beyond this value are invalid.
     */
    fun getPcmBuffer(bytes: ByteArray): Int
    fun seek(sample: Int)
    fun open()
    fun release()
}
