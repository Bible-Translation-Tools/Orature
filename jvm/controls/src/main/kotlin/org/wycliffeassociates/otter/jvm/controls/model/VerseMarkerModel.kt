/**
 * Copyright (C) 2020-2022 Wycliffe Associates
 *
 * This file is part of Orature.
 *
 * Orature is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Orature is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Orature.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.wycliffeassociates.otter.jvm.controls.model

import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import javafx.beans.property.SimpleIntegerProperty
import javafx.collections.ObservableList
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.audio.AudioFile
import tornadofx.*

private const val SEEK_EPSILON = 15_000

class VerseMarkerModel(private val audio: AudioFile, private val markerTotal: Int) {

    private val undoStack: Deque<MarkerOperation> = ArrayDeque()
    private val redoStack: Deque<MarkerOperation> = ArrayDeque()

    private val cues = sanitizeCues(audio)
    val markers: ObservableList<ChunkMarkerModel> = observableListOf()

    val markerCountProperty = SimpleIntegerProperty(1)
    private val audioEnd = audio.totalFrames
    var changesSaved = true
        private set

    init {
        cues as MutableList
        if (cues.isEmpty()) cues.add(AudioCue(0, "1"))
        cues.sortBy { it.location }
        markerCountProperty.value = cues.size

        markers.setAll(initializeMarkers(markerTotal, cues))
    }

    fun addMarker(location: Int) {
        if (markers.size < markerTotal) {
            changesSaved = false

            val marker = ChunkMarkerModel(AudioCue(location, "${markers.size + 1}"))
            val op = Add(marker)
            undoStack.push(op)
            op.apply()
            redoStack.clear()

            markers.sortBy { it.frame }
            markers.forEachIndexed { index, chunkMarker -> chunkMarker.label = (index + 1).toString() }
            markerCountProperty.value = markers.filter { it.placed }.size
        }
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val op = undoStack.pop()
            redoStack.push(op)
            op.undo()

            markers.sortBy { it.frame }
            markers.forEachIndexed { index, chunkMarker -> chunkMarker.label = (index + 1).toString() }
            markerCountProperty.value = markers.filter { it.placed }.size
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val op = redoStack.pop()
            undoStack.push(op)
            op.apply()

            markers.sortBy { it.frame }
            markers.forEachIndexed { index, chunkMarker -> chunkMarker.label = (index + 1).toString() }
            markerCountProperty.value = markers.filter { it.placed }.size
        }
    }

    fun seekCurrent(location: Int): Int {
        // find the nearest frame preceding the location
        return markers.filter { it.placed }.lastOrNull {
            it.frame <= location
        }?.frame ?: 0
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
        return findMarkerPrecedingPosition(location, filtered).frame
    }

    private fun findMarkerPrecedingPosition(
        location: Int,
        list: List<ChunkMarkerModel>
    ): ChunkMarkerModel {
        list.forEachIndexed { idx, _ ->
            if (list.lastIndex == idx) return@forEachIndexed
            // Seek Epsilon used so that the user can seek back while playing
            if (location < list[idx + 1].frame + SEEK_EPSILON) return list[idx]
        }
        return list.last()
    }

    fun writeMarkers(): Completable {
        return Single.fromCallable {
            cues as MutableList
            cues.clear()
            markers.forEach {
                if (it.placed) {
                    cues.add(it.toAudioCue())
                }
            }
            val audioFileCues = audio.metadata.getCues() as MutableList
            audioFileCues.clear()
            audioFileCues.addAll(cues)
            audio.update()
            changesSaved = true
        }.ignoreElement()
    }

    private fun sanitizeCues(audio: AudioFile): List<AudioCue> {
        return audio.metadata.getCues().filter { it.label.isInt() }
    }

    private fun initializeMarkers(markerTotal: Int, cues: List<AudioCue>): List<ChunkMarkerModel> {
        cues as MutableList
        cues.sortBy { it.location }

        val markers = mutableListOf<ChunkMarkerModel>()
        for ((idx, cue) in cues.withIndex()) {
            if (idx < markerTotal) {
                markers.add(ChunkMarkerModel(cue))
            }
        }

        return markers
    }

    private fun initializeHighlights(markers: List<ChunkMarkerModel>): List<MarkerHighlightState> {
        val highlightState = mutableListOf<MarkerHighlightState>()
        markers.forEachIndexed { i, _ ->
            val highlight = MarkerHighlightState()
            if (i % 2 == 0) {
                highlight.styleClass.bind(highlight.secondaryStyleClass)
            } else {
                highlight.styleClass.bind(highlight.primaryStyleClass)
            }
            highlightState.add(highlight)
        }
        return highlightState
    }

    private abstract inner class MarkerOperation(val markerId: Int) {
        abstract fun apply()
        abstract fun undo()
    }

    private inner class Add(val marker: ChunkMarkerModel): MarkerOperation(marker.id) {
        override fun apply() {
            markers.add(marker)
        }

        override fun undo() {
            markers.remove(marker)
        }
    }

    private inner class Delete(id: Int): MarkerOperation(id) {
        var marker: ChunkMarkerModel? = null

        override fun apply() {
            marker = markers.find { it.id == markerId }
            marker?.let {
                markers.remove(it)
            }
        }

        override fun undo() {
            marker?.let {
                markers.add(it)
            }
        }
    }
}

data class ChunkMarkerModel(
    var frame: Int,
    var label: String,
    var placed: Boolean,
    var id: Int = idGen++
) {
    constructor(audioCue: AudioCue) : this(audioCue.location, audioCue.label, true)

    fun toAudioCue(): AudioCue {
        return AudioCue(frame, label)
    }

    companion object {
        var idGen = 0
    }
}
