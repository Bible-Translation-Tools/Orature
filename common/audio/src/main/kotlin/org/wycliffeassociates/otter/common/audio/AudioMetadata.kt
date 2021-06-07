package org.wycliffeassociates.otter.common.audio

interface AudioMetadata {
    fun addCue(location: Int, label: String)
    fun getCues(): List<AudioCue>
}
