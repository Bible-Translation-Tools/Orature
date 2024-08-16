package org.wycliffeassociates.otter.common.domain.audio.metadata

import org.wycliffeassociates.otter.common.audio.AudioCue
import org.wycliffeassociates.otter.common.data.audio.AudioMarker
import org.wycliffeassociates.otter.common.data.audio.OratureCueType

class OratureMarkers {
    private val cueMap: MutableMap<OratureCueType, MutableList<AudioMarker>> = mutableMapOf(
        OratureCueType.CHUNK to mutableListOf(),
        OratureCueType.VERSE to mutableListOf(),
        OratureCueType.CHAPTER_TITLE to mutableListOf(),
        OratureCueType.BOOK_TITLE to mutableListOf(),
        OratureCueType.LICENSE to mutableListOf(),
    )

    @Synchronized
    fun getCues(): List<AudioCue> {
        return cueMap.values.flatMap { it.map { it.toCue() } }
    }

    @Synchronized
    fun getMarkers(type: OratureCueType): List<AudioMarker> {
        return cueMap.getOrPut(type) { mutableListOf() }
    }

    @Synchronized
    fun getMarkers(): List<AudioMarker> = cueMap.values.flatten()

    @Synchronized
    fun addMarkers(type: OratureCueType, markers: List<AudioMarker>) {
        cueMap.getOrPut(type) { mutableListOf() }.addAll(markers)
    }

    @Synchronized
    fun addMarker(type: OratureCueType, marker: AudioMarker) {
        cueMap.getOrPut(type) { mutableListOf() }.add(marker)
    }

    @Synchronized
    fun clearMarkersOfType(type: OratureCueType) {
        cueMap[type] = mutableListOf()
    }

    @Synchronized
    private fun addEntry(entry: Map.Entry<OratureCueType, MutableList<AudioMarker>>) {
        cueMap.getOrPut(entry.key) { mutableListOf() }.addAll(entry.value)
    }

    /**
     * Deep copies markers into a new instance of OratureMarkers
     */
    @Synchronized
    fun copy(): OratureMarkers {
        val newCopy = OratureMarkers()
        cueMap.forEach { newCopy.addEntry(it) }
        return newCopy
    }

    /**
     * Copies all markers from the provided markers to the internal map of markers
     *
     * @param markers the markers to copy from
     */
    fun import(markers: OratureMarkers) {
        markers.cueMap.entries.forEach {
            addEntry(it)
        }
    }
}