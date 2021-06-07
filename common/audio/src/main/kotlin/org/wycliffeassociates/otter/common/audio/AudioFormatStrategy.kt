package org.wycliffeassociates.otter.common.audio

internal interface AudioFormatStrategy {
    val sampleRate: Int
    val channels: Int
    val bitsPerSample: Int
    val totalFrames: Int
    val metadata: AudioMetadata

    fun addCue(location: Int, label: String)
    fun getCues(): List<AudioCue>
    fun update()
}
