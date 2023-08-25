package org.wycliffeassociates.otter.common.domain.narration

import com.fasterxml.jackson.annotation.JsonIgnore
import org.wycliffeassociates.otter.common.data.audio.VerseMarker
import kotlin.math.min

private const val UNPLACED_END = -1

internal data class VerseNode(
    /**
     * Start location in audio frames within the scratch audio recording. This is an absolute frame position into
     * the file.
     */
    var startScratchFrame: Int,
    /**
     * End location in audio frames within the scratch audio recording. This is an absolute frame position into
     * the file.
     */
    var endScratchFrame: Int,

    var placed: Boolean = false,
    val marker: VerseMarker,
    internal val sectors: MutableList<IntRange> = mutableListOf()
) {
    @get:JsonIgnore
    val length: Int
        get() = sectors.sumOf { it.length() }

    fun firstFrame(): Int {
        if (sectors.isEmpty()) return 0
        return sectors.first().first
    }

    fun lastFrame(): Int {
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
     * Completes a VerseNode by setting the end value to the last audio frame range sector.
     *
     * @throws Throws an illegalStateException if the last sector is already finalized.
     */
    @Throws(java.lang.IllegalStateException::class)
    fun finalize(end: Int) {
        if (sectors.isNotEmpty()) {
            val last = sectors.last()
            if (last.last == UNPLACED_END || last.first == last.last) {
                sectors.removeLast()
                sectors.add(last.first..end)
            } else {
                throw IllegalStateException("Tried to finalize a finalized VerseNode ${marker.label}!")
            }

        } else {
            throw IllegalStateException("Tried to finalize VerseNode ${marker.label} that was not started!")
        }
    }

    /**
     * Removes audio frames from one verse node to give to another
     *
     * This function takes audio samples from the start of the VerseNode range
     *
     * @param framesToTake The number of audio samples to donate to another VerseNode
     * @return a list of frame ranges to donate
     */
    fun takeFramesFromStart(framesToTake: Int): List<IntRange> {
        var remaining = framesToTake
        val toGive = mutableListOf<IntRange>()
        while (remaining >= 0) {
            when {
                // Consume the rest
                remaining >= length -> {
                    val total = sectors.map { it }
                    sectors.clear()
                    return toGive.apply { this.addAll(total) }
                }
                // Consume whole node
                remaining > sectors.first().last - sectors.first().first -> {
                    val sector = sectors.first()
                    remaining -= sector.length()
                    sectors.removeFirst()
                    toGive.add(sector)
                }
                // Split node
                else -> {
                    val node = sectors.first()
                    toGive.add(node.first..(node.first + remaining))
                    sectors[0] = (node.first + remaining)..node.last
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
     * @param framesToTake The number of audio samples to donate to another VerseNode
     * @return a list of frame ranges to donate
     */
    fun takeFramesFromEnd(framesToTake: Int): List<IntRange> {
        var remaining = framesToTake
        val toGive = mutableListOf<IntRange>()
        while (remaining >= 0) {
            when {
                // Consume the rest
                remaining >= length -> {
                    val total = sectors.map { it }
                    sectors.clear()
                    return toGive.apply { this.addAll(total) }
                }
                // Consume whole node
                remaining > sectors.last().last - sectors.last().first -> {
                    val node = sectors.last()
                    remaining -= node.last - node.start
                    sectors.removeLast()
                    toGive.add(node)
                }
                // Split node
                else -> {
                    val node = sectors.last()
                    toGive.add(node.last..(node.last - remaining))
                    sectors[sectors.lastIndex] = node.first..(node.last - remaining)
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
        sectors.sortBy { it.first }
        flattenSectors()
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
            startScratchFrame,
            endScratchFrame,
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
     * Returns the number of frames from the beginning of this verse node to the given absolute frame
     */
    fun framesToPosition(absoluteFrame: Int): Int {
        if (absoluteFrame !in this) {
            throw IndexOutOfBoundsException("Frame $absoluteFrame is not in ranges of $sectors")
        }

        var frameOffset = 0
        sectors.forEach { sector ->
            if (absoluteFrame in sector) {
                frameOffset += (sector.last - sector.first)
            } else {
                frameOffset += absoluteFrame - sector.first
            }
        }
        return frameOffset
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
                return sector.first + remaining
            }
        }

        throw IndexOutOfBoundsException("Requested offset $framesFromStart exceeded boundaries within ranges $sectors")
    }

    fun getSectorsFromOffset(framePosition: Int, ftr: Int): List<IntRange> {
        if (ftr <= 0 || framePosition !in this) return listOf()

        var framesToRead = ftr
        val stuff = mutableListOf<IntRange>()

        val startIndex = sectors.indexOfFirst { framePosition in it }

        val start = framePosition
        val end = (start + min(sectors[startIndex].last - start, framesToRead))
        val firstRange = start..end
        stuff.add(firstRange)
        framesToRead -= firstRange.length()

        for (idx in startIndex + 1 until sectors.size) {
            if (framesToRead <= 0) break
            val sector = sectors[idx]
            val end = (start + min(sectors[startIndex].last - start, framesToRead))
            val range = (sector.first..end)
            framesToRead -= range.length()
            stuff.add(range)
        }

        return stuff
    }
}

internal fun IntRange.length(): Int {
    return last - first
}