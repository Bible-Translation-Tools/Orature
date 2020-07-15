package org.wycliffeassociates.otter.jvm.markerapp.app.model

import javafx.beans.property.SimpleIntegerProperty
import org.wycliffeassociates.otter.common.io.wav.WavCue
import org.wycliffeassociates.otter.common.io.wav.WavFile
import java.lang.Integer.min

class VerseMarkers(private val audio: WavFile) {

    private val cues = audio.metadata.getCues()
    val markerCountProperty = SimpleIntegerProperty(0)

    init {
        cues as MutableList
        cues.sortBy { it.location }
        markerCountProperty.value = cues.size
    }

    fun addMarker(location: Int) {
        cues as MutableList
        cues.sortBy { it.location }
        var index = 0
        for ((i, c) in cues.withIndex()) {
            if (c.location < location) {
                index = i
            }
        }
        cues.add(index, WavCue(location, "${index+1}"))
        markerCountProperty.value = cues.size
    }

    fun seekNext(location: Int): Int {
        for (cue in cues) {
            if (location < cue.location) {
                return cue.location
            }
        }
        return Int.MAX_VALUE
    }

    fun seekPrevious(location: Int): Int {
        cues as MutableList
        cues.sortBy { it.location }
        for ((i, cue) in cues.reversed().withIndex()) {
            if (location > cue.location) {
                return cues.reversed().get(min(cues.size-1, i+1)).location
            }
        }
        return 0
    }
}