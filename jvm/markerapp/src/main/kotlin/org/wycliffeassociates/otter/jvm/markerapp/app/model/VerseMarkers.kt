package org.wycliffeassociates.otter.jvm.markerapp.app.model

import io.reactivex.Completable
import io.reactivex.Single
import javafx.beans.property.SimpleIntegerProperty
import org.wycliffeassociates.otter.common.audio.wav.WavCue
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import java.lang.Integer.min

class VerseMarkers(private val audio: WavFile, private val markerTotal: Int) {

    private val cues = audio.metadata.getCues()
    val markerCountProperty = SimpleIntegerProperty(0)
    val audioEnd = audio.totalFrames
    var changesSaved = true
        private set

    init {
        cues as MutableList
        cues.sortBy { it.location }
        markerCountProperty.value = cues.size
    }

    fun addMarker(location: Int) {
        changesSaved = false
        if (cues.size >= markerTotal) {
            return
        }
        cues as MutableList
        cues.sortBy { it.location }
        var index = 0
        for ((i, c) in cues.withIndex()) {
            if (c.location < location) {
                index = i + 1
            }
        }
        cues.add(index, WavCue(location, "${index + 1}"))
        cues.replaceAll {
            if (it.location > location) {
                WavCue(it.location, "${it.label.toInt() + 1}")
            } else it
        }
        markerCountProperty.value = cues.size
    }

    fun seekNext(location: Int): Int {
        for (cue in cues) {
            if (location < cue.location) {
                return cue.location
            }
        }
        return audioEnd
    }

    fun seekPrevious(location: Int): Int {
        cues as MutableList
        cues.sortBy { it.location }
        for ((i, cue) in cues.reversed().withIndex()) {
            if (location > cue.location) {
                return cues.reversed().get(min(cues.size - 1, i + 1)).location
            }
        }
        return 0
    }

    fun writeMarkers(): Completable {
        return Single.fromCallable {
            audio.update()
            changesSaved = true
        }.ignoreElement()
    }
}
