package org.wycliffeassociates.otter.common.audio

import org.wycliffeassociates.otter.common.audio.wav.WavCue

internal interface AudioFileFormat {
    val sampleRate: Int
    val channels: Int
    val bitsPerSample: Int
    val totalFrames: Int
    val metadata: AudioMetadata

    fun addCue(location: Int, label: String)
    fun getCues(): List<WavCue>
    fun update()
}
