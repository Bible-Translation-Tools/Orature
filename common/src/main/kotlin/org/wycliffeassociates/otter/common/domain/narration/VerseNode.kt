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
package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.annotation.JsonIgnore
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.BookMarker
import org.wycliffeassociates.otter.common.data.audio.ChapterMarker
import org.wycliffeassociates.otter.common.data.audio.UnknownMarker
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import java.lang.UnsupportedOperationException
import kotlin.math.min

internal data class VerseNode(
    var placed: Boolean = false,
    val marker: AudioMarker,
    var sectors: MutableList<IntRange> = mutableListOf()
) {
    @get:JsonIgnore
    val length: Int
        get() = sectors.sumOf { it.length() }

    fun firstIndex(): Int {
        if (sectors.isEmpty()) return 0
        return sectors.first().first
    }

    fun lastIndex(): Int {
        if (sectors.isEmpty()) return 0
        return sectors.last().last
    }

    /**
     * Begins a new audio frame sector corresponding to this VerseNode
     */
    fun addStart(start: Int) {
        sectors.add(start..start)
    }

    /**
     * Completes a VerseNode by setting the end value to the last audio index range sector.
     *
     * @throws Throws an illegalStateException if the last sector is already finalized.
     */
    @Throws(java.lang.IllegalStateException::class)
    fun finalize(end: Int) {
        if (sectors.isNotEmpty()) {
            val last = sectors.last()
            sectors.removeLast()
            sectors.add(last.first until end)
        } else {
            throw IllegalStateException("Tried to finalize VerseNode ${marker.label} that was not started!")
        }
    }

    /**
     * Removes audio frames from one verse node to give to another
     *
     * This function takes audio samples from the start of the VerseNode range
     *
     * @param indicesToTake The number of audio samples to donate to another VerseNode
     * @return a list of frame ranges to donate
     */
    fun takeIndicesFromStart(indicesToTake: Int): List<IntRange> {
        var remaining = indicesToTake
        val toGive = mutableListOf<IntRange>()
        while (remaining > 0) {
            when {
                // Consume the rest
                remaining >= length -> {
                    val total = sectors.map { it } as MutableList
                    val lastSector = total.last()
                    total.removeLast()
                    if (lastSector.first != lastSector.last) {
                        total.add(lastSector.first until lastSector.last)
                    }
                    sectors.clear()
                    // the verse marker should not relinquish its absolute last frame
                    sectors.add(lastSector.last..lastSector.last)
                    return toGive.apply { this.addAll(total) }
                }
                // Consume whole node
                remaining >= sectors.first().last - sectors.first().first -> {
                    val sector = sectors.first()
                    remaining -= sector.length()
                    sectors.removeFirst()
                    toGive.add(sector)
                }
                // Split node
                else -> {
                    val node = sectors.first()
                    toGive.add(node.first until (node.first + remaining))
                    sectors[0] = (node.first + remaining)..node.last
                    break
                }
            }
        }
        return toGive
    }

    /**
     * Removes audio frames from one verse node to give to another
     *
     * This function takes audio samples from the end of the VerseNode range
     *
     * @param indicesToTake The number of audio samples to donate to another VerseNode
     * @return a list of frame ranges to donate
     */
    fun takeIndicesFromEnd(indicesToTake: Int): List<IntRange> {
        var remaining = indicesToTake
        val toGive = mutableListOf<IntRange>()
        while (remaining > 0) {
            when {
                // Consume the rest
                remaining >= length -> {
                    val total = sectors.map { it } as MutableList
                    val firstSector = total.first()
                    total.removeFirst()
                    if (firstSector.first != firstSector.last) {
                        total.add(0, firstSector.first + 1..firstSector.last)
                    }
                    sectors.clear()
                    // the verse needs to at least hold onto its first frame
                    sectors.add(firstSector.first..firstSector.first)
                    return toGive.apply { this.addAll(0, total) }
                }
                // Consume whole node
                remaining >= sectors.last().length() -> {
                    val node = sectors.last()
                    remaining -= node.length()
                    sectors.removeLast()
                    toGive.add(0, node)
                }
                // Split node
                else -> {
                    val node = sectors.last()
                    toGive.add(0, (node.last - remaining + 1)..node.last)
                    sectors[sectors.lastIndex] = node.first until (node.last - remaining + 1)
                    break
                }
            }
        }
        return toGive
    }

    /**
     * Adds a list of Audio Frame ranges to this VerseNode's sectors
     */
    fun addRange(ranges: List<IntRange>) {
        sectors.addAll(ranges)
    }

    /**
     * Reduces the amount of range objects if two adjacent ranges can be merged
     */
    private fun flattenSectors() {
        val newSectors = mutableListOf<IntRange>()
        for (i in 0 until sectors.size - 1) {
            if (sectors[i].last == sectors[i + 1].first) {
                newSectors.add(sectors[i].first..sectors[i + 1].last)
            } else {
                newSectors.add(sectors[i])
            }
        }
        sectors.clear()
        sectors.addAll(newSectors)
    }

    fun clear() {
        sectors.clear()
        placed = false
    }

    /**
     * Deep copies this VerseNode
     */
    fun copy(): VerseNode {
        val vn = VerseNode(
            placed,
            marker
        )
        vn.sectors.addAll(sectors.map { it })
        return vn
    }

    operator fun contains(frame: Int): Boolean {
        sectors.forEach {
            if (frame in it) return true
        }
        return false
    }

    /**
     * Returns the number of indices from the beginning of this verse node to the given absolute index
     */
    fun indicesToPosition(absoluteIndex: Int): Int {
        if (absoluteIndex !in this) {
            throw IndexOutOfBoundsException("Frame $absoluteIndex is not in ranges of $sectors")
        }

        var indexOffset = 0
        for (sector in sectors) {
            if (absoluteIndex in sector) {
                //  NOTE: The question this function is answering is "how many frames are behind my current position?"
                indexOffset += absoluteIndex - sector.first
                break
            } else {
                indexOffset += sector.length()
            }
        }
        return indexOffset
    }

    fun absoluteFrameFromOffset(framesFromStart: Int): Int {
        if (framesFromStart > length) {
            throw IndexOutOfBoundsException("Frame offset: $framesFromStart exceeds the boundaries within ranges $sectors")
        }

        var remaining = framesFromStart
        sectors.forEach { sector ->
            if (remaining > sector.length()) {
                remaining -= sector.length()
            } else {
                return sector.first +  remaining - 1 // Add - 1 to account for inclusive start
                // Depending on in the intended functionality, remove the - 1.
                //  If the question this is answering is "Get the next frame after some offset?", then
                //  remove the - 1, if the question this is answering is "Offset by some amount, then get me that frame?"
                //  then we want to keep the -1
            }
        }

        throw IndexOutOfBoundsException("Requested offset $framesFromStart exceeded boundaries within ranges $sectors")
    }

    fun getSectorsFromOffset(index: Int, btr: Int): List<IntRange> {
        if (btr <= 0 || index !in this) return listOf()

        var bytesToRead = btr
        val stuff = mutableListOf<IntRange>()

        val startIndex = sectors.indexOfFirst { index in it }

        var start = index
        val end = min(sectors[startIndex].last, start + bytesToRead - 1)
        val firstRange = start..end
        stuff.add(firstRange)
        bytesToRead -= firstRange.length()

        for (idx in startIndex + 1 until sectors.size) {
            if (bytesToRead <= 0) break
            val sector = sectors[idx]
            val start = sector.first
            val end = min(sectors[idx].last, start + bytesToRead - 1)
            val range = (sector.first..end)
            bytesToRead -= range.length()
            stuff.add(range)
        }

        return stuff
    }

    fun copyMarker(frame: Int): AudioMarker {
        return when (marker) {
            is ChapterMarker -> marker.copy(location = frame)
            is BookMarker -> marker.copy(location = frame)
            is VerseMarker -> marker.copy(location = frame)
            is UnknownMarker -> marker.copy(location = frame)
            else -> { throw UnsupportedOperationException("Copy is not supported for marker $marker") }
        }
    }
}

internal fun IntRange.length(): Int {
    return if (first != last) last - first + 1 else 0
}