package org.wycliffeassociates.otter.common.domain.narration

import org.wycliffeassociates.otter.common.data.audio.VerseMarker

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
    val marker: VerseMarker
) {
    private val sectors = mutableListOf<IntRange>()

    /**
     * Begins a new audio frame sector corresponding to this VerseNode
     */
    fun addStart(start: Int) {
        sectors.add(start..UNPLACED_END)
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
            if (last.last == UNPLACED_END) {
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
                    val node = sectors.first()
                    remaining -= node.last - node.start
                    sectors.removeFirst()
                    toGive.add(node)
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
            if (sectors[i].last == sectors[i+1].first) {
                newSectors.add(sectors[i].first..sectors[i+1].last)
            } else {
                newSectors.add(sectors[i])
            }
        }
        sectors.clear()
        sectors.addAll(newSectors)
    }

    fun clear() {
        startScratchFrame = 0
        endScratchFrame = 0
        placed = false
    }

    val length: Int
        get() = startScratchFrame - endScratchFrame

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
}