/**
 * Copyright (C) 2020-2024 Wycliffe Associates
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
package org.wycliffeassociates.otter.common.domain.model

import io.reactivex.Completable
import io.reactivex.Single
import java.util.*
import org.slf4j.LoggerFactory
import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.IUndoable
import java.util.regex.Pattern

private const val SEEK_EPSILON = 15_000

class VerseMarkerModel(
    private val audio: OratureAudioFile,
    markerTotal: Int,
    markerLabels: List<String>
) {
    private val logger = LoggerFactory.getLogger(VerseMarkerModel::class.java)

    private val undoStack: Deque<MarkerOperation> = ArrayDeque()
    private val redoStack: Deque<MarkerOperation> = ArrayDeque()

    private val markers = mutableListOf<AudioMarker>()
    val markerModels = mutableListOf<ChunkMarkerModel>()

    private var placedMarkersCount = 0
    private val audioEnd = audio.totalFrames

    val markerTotal
        get() = markers.size

    private val labelIndex
        get() = undoStack.size

    init {
        loadMarkersFromAudio()
    }

    private fun loadMarkersFromAudio() {
        val bookMarker = audio.getMarker<BookMarker>()
            .firstOrNull()
            ?.let { arrayOf(ChunkMarkerModel(it, true)) }
            ?: arrayOf()
        val chapterMarker = audio.getMarker<ChapterMarker>()
            .firstOrNull()
            ?.let { arrayOf(ChunkMarkerModel(it, true)) }
            ?: arrayOf()
        val verses = audio.getMarker<VerseMarker>().map { ChunkMarkerModel(it, true) }.toTypedArray()

        undoStack.clear()
        redoStack.clear()
        markerModels.clear()
        markerModels.addAll(listOf(*bookMarker, *chapterMarker, *verses))
        markerModels.forEach { markers.add(it.marker) }
        markerModels.forEach { undoStack.push(Add(it)) }
    }

    fun loadMarkers(chunkMarkers: List<ChunkMarkerModel>) {
        markerModels.clear()
        markerModels.addAll(chunkMarkers)
        refreshMarkers()
    }

    fun addMarker(location: Int) {
        if (markerModels.size < markerTotal) {
            val marker = markers.getOrNull(labelIndex) ?: return
            val markerModel = ChunkMarkerModel(marker.clone(location), true)
            val op = Add(markerModel)
            undoStack.push(op)
            op.execute()
            redoStack.clear()

            refreshMarkers()
        }
    }

    private fun parseLabel(label: String): Pair<Int, Int> {
        val pattern = Pattern.compile("(\\d)(?:-(\\d))?")
        val match = pattern.matcher(label)
        if (match.matches()) {
            val start: Int = match.group(1)!!.toInt()
            val end: Int = match.group(2)?.toInt() ?: start
            return Pair(start, end)
        } else {
            throw NumberFormatException("Invalid verse label: $label, which could not be parsed to a verse or verse range")
        }
    }

    fun deleteMarker(id: Int) {
        if (placedMarkersCount > 0) {
            val op = Delete(id)
            undoStack.push(op)
            op.execute()
            redoStack.clear()

            refreshMarkers()
        }
    }

    fun moveMarker(id: Int, start: Int, end: Int) {
        val op = Move(id, start, end)
        undoStack.push(op)
        op.execute()
        redoStack.clear()

        refreshMarkers()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val op = undoStack.pop()
            redoStack.push(op)
            op.undo()

            refreshMarkers()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val op = redoStack.pop()
            undoStack.push(op)
            op.redo()

            refreshMarkers()
        }
    }

    fun seekCurrent(location: Int): Int {
        // find the nearest frame preceding the location
        return markerModels.filter { it.placed }.lastOrNull {
            it.frame <= location
        }?.frame ?: 0
    }

    fun seekNext(location: Int): Int {
        for (marker in markerModels.filter { it.placed }) {
            if (location < marker.frame) {
                return marker.frame
            }
        }
        return audioEnd
    }

    fun seekPrevious(location: Int): Int {
        val filtered = markerModels.filter { it.placed }
        return if (filtered.isNotEmpty()) {
            findMarkerPrecedingPosition(location, filtered).frame
        } else {
            0
        }
    }

    fun hasDirtyMarkers() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()

    private fun refreshMarkers() {
        markerModels.sortBy { it.frame }
        markerModels.forEachIndexed { index, chunkMarker ->
            if (index < markers.size) {
                chunkMarker.marker = markers[index]
            }
        }
        placedMarkersCount = markerModels.filter { it.placed }.size
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
            audio.clearMarkersOfType<BookMarker>()
            audio.clearMarkersOfType<ChapterMarker>()
            audio.clearMarkersOfType<VerseMarker>()

            markerModels.forEach {
                if (it.placed) {
                    when (val marker = it.marker) {
                        is BookMarker -> audio.addMarker<BookMarker>(marker.copy(location = it.frame))
                        is ChapterMarker -> audio.addMarker<ChapterMarker>(marker.copy(location = it.frame))
                        is VerseMarker -> audio.addMarker<VerseMarker>(marker.copy(it.frame))
                    }
                }
            }

            audio.update()
        }.ignoreElement()
    }

    private abstract inner class MarkerOperation(val markerId: Int) : IUndoable

    private inner class Add(val marker: ChunkMarkerModel) : MarkerOperation(marker.id) {
        override fun execute() {
            //labelIndex++
            markerModels.add(marker)
        }

        override fun undo() {
            //labelIndex--
            markerModels.remove(marker)
        }

        override fun redo() = execute()
    }

    private inner class Delete(id: Int) : MarkerOperation(id) {
        var marker: ChunkMarkerModel? = null

        override fun execute() {
            //labelIndex--
            marker = markerModels.find { it.id == markerId }
            marker?.let {
                markerModels.remove(it)
            }
        }

        override fun undo() {
            //labelIndex++
            marker?.let {
                markerModels.add(it)
            }
        }

        override fun redo() = execute()
    }

    private inner class Move(
        id: Int,
        val start: Int,
        val end: Int
    ): MarkerOperation(id) {
        var marker: ChunkMarkerModel? = null

        override fun execute() {
            marker = markerModels.find { it.id == markerId }
            marker?.frame = end
        }

        override fun undo() {
            marker?.frame = start
        }

        override fun redo() = execute()
    }
}

data class ChunkMarkerModel(
    var marker: AudioMarker,
    var placed: Boolean,
    var id: Int = idGen++
) {
    var frame: Int = marker.location
    var label: String = marker.label

    fun toAudioCue(): AudioCue {
        return AudioCue(frame, label)
    }

    companion object {
        var idGen = 0
    }
}