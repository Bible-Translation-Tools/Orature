package org.wycliffeassociates.otter.common.audio

import org.wycliffeassociates.otter.common.audio.wav.WavCue

interface AudioMetadata {
    fun addCue(location: Int, label: String)
    fun getCues(): List<WavCue>
}
