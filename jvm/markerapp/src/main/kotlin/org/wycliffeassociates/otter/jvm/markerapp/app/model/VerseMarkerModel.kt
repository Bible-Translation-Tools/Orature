package org.wycliffeassociates.otter.jvm.markerapp.app.model

import io.reactivex.Completable
import io.reactivex.Single
import javafx.beans.property.SimpleIntegerProperty
import org.wycliffeassociates.otter.common.audio.wav.WavCue
import org.wycliffeassociates.otter.common.audio.wav.WavFile
import tornadofx.isInt
import java.lang.Integer.min

private const val SEEK_EPSILON = 100

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
        for (marker in markers) {
            if (!marker.placed) {
                marker.frame = location
                marker.placed = true
                break
            }
        }
        markers as MutableList
        markers.sortWith(compareBy({ !it.placed }, { it.frame }))
        markers.forEachIndexed { index, chunkMarker -> chunkMarker.label = (index + 1).toString() }
        markerCountProperty.value = markers.filter { it.placed == true }.size
    }

    fun seekNext(location: Int): Int {
        for (marker in markers.filter { it.placed }) {
            if (location < marker.frame) {
                return marker.frame
            }
        }
        return audioEnd
    }

    fun seekPrevious(location: Int): Int {
        val filtered = markers.filter { it.placed }
        filtered.forEachIndexed { idx, marker ->
            if (location < marker.frame) {
                return if (idx - 2 >= 0) {
                    filtered[idx - 2].frame
                } else {
                    0
                }
            } else if (idx == filtered.size - 1 && idx - 1 >= 0) {
                // allows for seeking back and not getting stuck on the last marker
                return if (location > filtered[idx].frame + SEEK_EPSILON) {
                    filtered[idx].frame
                } else {
                    filtered[idx - 1].frame
                }
            }
        }
        return 0
    }

    fun writeMarkers(): Completable {
        return Single.fromCallable {
            cues as MutableList
            cues.clear()
            markers.forEach {
                if (it.placed) {
                    cues.add(it.toWavCue())
                }
            }
            val audioFileCues = audio.metadata.getCues() as MutableList
            audioFileCues.clear()
            audioFileCues.addAll(cues)
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
        for ((idx, cue) in cues.withIndex()) {
            if (idx < markerTotal) {
                markers.add(ChunkMarker(cue))
            }
        }
        for (i in markers.size until markerTotal) {
            markers.add(ChunkMarker(0, (i + 1).toString(), false))
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
