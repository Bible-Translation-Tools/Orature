package org.wycliffeassociates.otter.common.data.audio

import org.wycliffeassociates.otter.common.audio.AudioCue

interface AudioMarker {
    /**
     * The marker label which does not contain any namespacing, most often a verse number or verse range
     */
    val label: String
    val location: Int

    /**
     * The marker label with the appropriate namespacing (such as "orature-vm-{number}"
     */
    val formattedLabel: String

    fun toCue(): AudioCue {
        return AudioCue(location, formattedLabel)
    }
}

data class UnknownMarker(override val location: Int, override val label: String) : AudioMarker {
    constructor(cue: AudioCue) : this(cue.location, cue.label)

    override val formattedLabel
        get() = label

    override fun toString(): String {
        return formattedLabel
    }
}

data class VerseMarker(val start: Int, val end: Int, override val location: Int) : AudioMarker {
    override val label: String
        get() = if (end != start) "$start-$end" else "$start"

    override val formattedLabel
        get() = "orature-vm-${label}"

    override fun toString(): String {
        return formattedLabel
    }
}

data class ChunkMarker(val chunk: Int, override val location: Int) : AudioMarker {
    override val label = "$chunk"
    override val formattedLabel
        get() = "orature-chunk-${label}"

    override fun toString(): String {
        return formattedLabel
    }
}