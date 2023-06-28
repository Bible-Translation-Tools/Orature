package org.wycliffeassociates.otter.common.data.audio

import org.wycliffeassociates.otter.common.audio.AudioCue

interface AudioMarker {
    val label: String
    val location: Int

    fun formatMarkerText(): String

    fun toCue(): AudioCue {
        return AudioCue(location, formatMarkerText())
    }
}

class UnknownMarker(override val location: Int, override val label: String) : AudioMarker {
    constructor(cue: AudioCue) : this(cue.location, cue.label)

    override fun formatMarkerText() = label
}

class VerseMarker(val start: Int, val end: Int, override val location: Int) : AudioMarker {
    override val label: String
        get() = if (end != start) "$start-$end" else "$start"

    override fun formatMarkerText() = "orature-vm-${label}"
}

class ChunkMarker(val chunk: Int, override val location: Int) : AudioMarker {
    override val label = "$chunk"
    override fun formatMarkerText() = "orature-chunk-${label}"
}