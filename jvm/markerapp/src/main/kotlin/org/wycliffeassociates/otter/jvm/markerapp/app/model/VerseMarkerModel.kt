package org.wycliffeassociates.otter.jvm.markerapp.app.model

import io.reactivex.Completable
import io.reactivex.Single
import javafx.beans.property.SimpleIntegerProperty
import org.wycliffeassociates.otter.common.audio.wav.WavCue
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import tornadofx.isInt
import java.lang.Integer.min

class VerseMarkerModel(private val audio: WavFile, val markerTotal: Int) {

    val cues = sanitizeCues(audio)
    val markers: List<ChunkMarker>

    val markerCountProperty = SimpleIntegerProperty(1)
    val audioEnd = audio.totalFrames
    var changesSaved = true
        private set

    init {
        cues as MutableList
        if (cues.isEmpty()) cues.add(WavCue(0, "1"))
        cues.sortBy { it.location }
        markerCountProperty.value = cues.size

        markers = initializeMarkers(markerTotal, cues)
    }

    fun findMarkerById(id: Int): ChunkMarker {
        return markers.find { id == id }!!
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

    private fun sanitizeCues(audio: WavFile): List<WavCue> {
        return audio.metadata.getCues().filter { it.label.isInt() }
    }

    private fun initializeMarkers(markerTotal: Int, cues: List<WavCue>): List<ChunkMarker> {
        cues as MutableList
        cues.sortBy { it.location }

        val markers = mutableListOf<ChunkMarker>()
        for (i in 1..markerTotal) {
            var marker = ChunkMarker(0, i.toString(), false)
            if (cues.size >= markerTotal) {
                marker.frame = cues[i - 1].location
                marker.placed = true
            }
            markers.add(marker)
        }
        return markers
    }
}

data class ChunkMarker(
    var frame: Int,
    var label: String,
    var placed: Boolean
) {

    val id = allocateId()

    constructor(wavCue: WavCue) : this(wavCue.location, wavCue.label, true)

    fun toWavCue(): WavCue {
        return WavCue(frame, label)
    }

    private companion object {
        var counter = 0

        fun allocateId(): Int {
            return counter++
        }
    }
}
