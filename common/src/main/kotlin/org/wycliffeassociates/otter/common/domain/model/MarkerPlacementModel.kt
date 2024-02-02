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
import org.wycliffeassociates.otter.common.data.audio.ChunkMarker
import org.wycliffeassociates.otter.common.domain.audio.OratureAudioFile
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import org.wycliffeassociates.otter.common.domain.IUndoable


private const val SEEK_EPSILON = 15_000
const val DEFAULT_CHUNK_MARKER_TOTAL = 500

enum class MarkerPlacementType {
    CHUNK,
    VERSE
}

/**
 * State Machine for placing markers
 *
 * @param primaryPlacementType This is the primary marker type that is being placed- Chunk or Verse
 *
 * @param audio The audio file being operated on
 *
 * @param reservedMarkers the exhaustive list of all AudioMarkers that need to be placed and accounted for, which
 * can include markers that are not of the primaryPlacementType, such as BookMarker and ChapterMarker
 */
class MarkerPlacementModel(
    primaryPlacementType: MarkerPlacementType,
    private val audio: OratureAudioFile,
    reservedMarkers: List<AudioMarker>
) {
    private val logger = LoggerFactory.getLogger(MarkerPlacementModel::class.java)

    private val primaryType: Class<out AudioMarker> = when (primaryPlacementType) {
        MarkerPlacementType.CHUNK -> ChunkMarker::class.java
        MarkerPlacementType.VERSE -> VerseMarker::class.java
    }

    private val undoStack: Deque<MarkerOperation> = ArrayDeque()
    private val redoStack: Deque<MarkerOperation> = ArrayDeque()

    private val markers = mutableListOf<AudioMarker>()
    val markerItems = mutableListOf<MarkerItem>()

    private val placedMarkersCount
        get() = markerItems.count { it.placed }

    private val audioEnd = audio.totalFrames

    init {
        loadMarkersFromAudio(reservedMarkers)
    }

    val markerTotal: Int
        get() = markers.size

    private val labelIndex
        get(): Int {
            val unplacedIndex = markerItems.indexOfFirst { !it.placed }
            return if (unplacedIndex != -1) unplacedIndex else markerItems.size
        }

    private inline fun <reified T : AudioMarker> getTitlesAsModels(
        audio: OratureAudioFile,
    ): Array<MarkerItem> {
        return audio.getMarker<T>()
            .firstOrNull()
            ?.let { arrayOf(MarkerItem(it, true)) }
            ?: arrayOf()
    }

    private fun loadMarkersFromAudio(markerLabels: List<AudioMarker>) {
        val bookMarker = getTitlesAsModels<BookMarker>(audio)
        val chapterMarker = getTitlesAsModels<ChapterMarker>(audio)

        val chunks = when (primaryType) {
            VerseMarker::class.java -> audio.getMarker<VerseMarker>().map { MarkerItem(it, true) }.toTypedArray()
            ChunkMarker::class.java -> audio.getMarker<ChunkMarker>().map { MarkerItem(it, true) }.toTypedArray()
            else -> {
                throw IllegalArgumentException("Invalid MarkerPlacementModel primary type: $primaryType")
            }
        }

        undoStack.clear()
        redoStack.clear()
        markerItems.clear()

        val finalizedVerses = when (primaryType) {
            VerseMarker::class.java -> addUnplacedMarkers(markerLabels, arrayOf(*bookMarker, *chapterMarker, *chunks))
            ChunkMarker::class.java -> padExtraChunks(chunks)
            else -> {
                throw IllegalArgumentException("Invalid MarkerPlacementModel primary type: $primaryType")
            }
        }

        markerItems.addAll(listOf(*finalizedVerses))

        markerItems.forEach { markers.add(it.marker) }
        markerItems.removeIf { !it.placed }
        markerItems.forEach { if (it.placed) undoStack.push(Add(it)) }
    }

    /**
     * Adds all extra chunks that need to be placed but were not found in the audio file
     */
    private fun padExtraChunks(markerModels: Array<MarkerItem>): Array<MarkerItem> {
        val existing = markerItems.size + 1
        val chunkCount = markers.count { it is ChunkMarker }
        val toAdd = buildList {
            for (i in existing..chunkCount) {
                add(MarkerItem(ChunkMarker(i, 0), false))
            }
        }.toTypedArray()
        return listOf(*markerModels, *toAdd).toTypedArray()
    }

    /**
     * Adds the remaining MarkerItems for markers which were not found from the audio file
     */
    private fun addUnplacedMarkers(
        markerLabels: List<AudioMarker>,
        markerModels: Array<MarkerItem>
    ): Array<MarkerItem> {
        if (markerModels.size == markerLabels.size) return markerModels

        val all = markerLabels.map { marker -> MarkerItem(marker, false) }

        val missing = all
            .filter { marker ->
                markerModels.any { it.marker.label == marker.label }.not()
            }
            .toTypedArray()

        val final = listOf(*markerModels, *missing)

        return final.sortedBy { it.marker.sort }.toTypedArray()
    }

    fun loadMarkers(chunkMarkers: List<MarkerItem>) {
        markerItems.clear()
        markerItems.addAll(chunkMarkers)
        refreshMarkers()
    }

    fun addMarker(location: Int) {
        val marker = markers.getOrNull(labelIndex) ?: return
        val markerModel = MarkerItem(marker.clone(location), true)

        val op = Add(markerModel)
        undoStack.push(op)
        op.execute()
        redoStack.clear()

        refreshMarkers()
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
        return markerItems.filter { it.placed }.lastOrNull {
            it.frame <= location
        }?.frame ?: 0
    }

    fun seekNext(location: Int): Int {
        for (marker in markerItems.filter { it.placed }) {
            if (location < marker.frame) {
                return marker.frame
            }
        }
        return audioEnd
    }

    fun seekPrevious(location: Int): Int {
        val filtered = markerItems.filter { it.placed }
        return if (filtered.isNotEmpty()) {
            findMarkerPrecedingPosition(location, filtered).frame
        } else {
            0
        }
    }

    fun hasDirtyMarkers() = undoStack.isNotEmpty()
    fun canRedo() = redoStack.isNotEmpty()

    private fun refreshMarkers() {
        markerItems.sortBy { it.frame }
        markerItems.forEachIndexed { index, chunkMarker ->
            if (index < markers.size) {
                // We want the marker from the index, but the position of chunkMarker
                // This keeps the markers in verse/chunk order and may "swap" markers around
                chunkMarker.marker = markers[index].clone(chunkMarker.frame)
            }
        }
    }

    private fun findMarkerPrecedingPosition(
        location: Int,
        list: List<MarkerItem>
    ): MarkerItem {
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
            if (primaryType == VerseMarker::class.java) {
                audio.clearMarkersOfType<VerseMarker>()
            }
            if (primaryType == ChunkMarker::class.java) {
                audio.clearMarkersOfType<ChunkMarker>()
            }

            markerItems.forEach {
                if (it.placed) {
                    when (val marker = it.marker) {
                        is BookMarker -> audio.addMarker<BookMarker>(marker.copy(location = it.frame))
                        is ChapterMarker -> audio.addMarker<ChapterMarker>(marker.copy(location = it.frame))
                        is VerseMarker -> {
                            if (primaryType == VerseMarker::class.java)
                                audio.addMarker<VerseMarker>(marker.copy(location = it.frame))
                        }

                        is ChunkMarker -> {
                            if (primaryType == ChunkMarker::class.java) {
                                audio.addMarker<ChunkMarker>(marker.copy(location = it.frame))
                            }
                        }
                    }
                }
            }

            audio.update()
        }.ignoreElement()
    }

    private abstract inner class MarkerOperation(val markerId: Int) : IUndoable

    private inner class Add(val marker: MarkerItem) : MarkerOperation(marker.id) {
        override fun execute() {
            markerItems.add(marker)
        }

        override fun undo() {
            markerItems.remove(marker)
        }

        override fun redo() = execute()
    }

    private inner class Delete(id: Int) : MarkerOperation(id) {
        var marker: MarkerItem? = null

        override fun execute() {
            marker = markerItems.find { it.id == markerId }
            marker?.let {
                markerItems.remove(it)
            }
        }

        override fun undo() {
            marker?.let {
                markerItems.add(it)
            }
        }

        override fun redo() = execute()
    }

    private inner class Move(
        id: Int,
        val start: Int,
        val end: Int
    ) : MarkerOperation(id) {
        var marker: MarkerItem? = null

        override fun execute() {
            marker = markerItems.find { it.id == markerId }
            marker?.let {
                it.marker = it.marker.clone(location = end)
            }
        }

        override fun undo() {
            marker?.let {
                it.marker = it.marker.clone(location = start)
            }

        }

        override fun redo() = execute()
    }
}

data class MarkerItem(
    var marker: AudioMarker,
    var placed: Boolean,
    var id: Int = idGen++
) {
    val frame: Int
        get() = marker.location

    val label: String
        get() = marker.label

    fun toAudioCue(): AudioCue {
        return AudioCue(frame, marker.formattedLabel)
    }

    companion object {
        var idGen = 0
    }
}